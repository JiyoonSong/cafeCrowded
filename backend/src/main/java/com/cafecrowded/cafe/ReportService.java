package com.cafecrowded.cafe;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportService {

	private final CafeRepository cafes;
	private final CafeCrowdReportRepository reports;
	private final CafeService cafeService;
	private final Clock clock;
	private final Duration sameCafeCooldown;
	private final int maxPerHour;
	private final byte[] salt;

	public ReportService(CafeRepository cafes, CafeCrowdReportRepository reports, CafeService cafeService, Clock clock,
			@Value("${app.report.same-cafe-cooldown-minutes}") long cooldownMinutes,
			@Value("${app.report.max-per-hour}") int maxPerHour,
			@Value("${app.report.hash-salt}") String salt) {
		if (salt == null || salt.length() < 8) {
			throw new IllegalStateException("REPORTER_HASH_SALT must be set (at least 8 characters)");
		}
		this.cafes = cafes;
		this.reports = reports;
		this.cafeService = cafeService;
		this.clock = clock;
		this.sameCafeCooldown = Duration.ofMinutes(cooldownMinutes);
		this.maxPerHour = maxPerHour;
		this.salt = salt.getBytes(StandardCharsets.UTF_8);
	}

	@Transactional
	public CafeResponse.Crowd submit(Long cafeId, CrowdLevel level, String clientAddress) {
		if (!cafes.existsById(cafeId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cafe not found");
		}
		String reporter = hash(clientAddress);
		Instant now = clock.instant();
		if (reports.existsByCafeIdAndReporterHashAndObservedAtAfter(cafeId, reporter, now.minus(sameCafeCooldown))
				|| reports.countByReporterHashAndObservedAtAfter(reporter, now.minus(Duration.ofHours(1))) >= maxPerHour) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many reports");
		}
		// The observation time is always the server's time; clients cannot backdate or future-date a report.
		reports.save(new CafeCrowdReport(cafeId, level, reporter, now));
		return cafeService.currentCrowd(cafeId);
	}

	private String hash(String address) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(salt, "HmacSHA256"));
			return HexFormat.of().formatHex(mac.doFinal(String.valueOf(address).getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException("Hashing unavailable", e);
		}
	}
}
