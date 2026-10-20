package com.cafecrowded.cafe;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CafeService {

	private final CafeRepository repo;
	private final CafeCrowdReportRepository reports;
	private final Clock clock;
	private final Duration reportTtl;

	public CafeService(CafeRepository repo, CafeCrowdReportRepository reports, Clock clock,
			@Value("${app.crowd.report-ttl-minutes}") long ttlMinutes) {
		this.repo = repo;
		this.reports = reports;
		this.clock = clock;
		this.reportTtl = Duration.ofMinutes(ttlMinutes);
	}

	public record BoundingBox(double minLat, double maxLat, double minLng, double maxLng) {
		static final BoundingBox WORLD = new BoundingBox(-90, 90, -180, 180);
	}

	@Transactional(readOnly = true)
	public CafeResponse.PageResponse search(String q, BoundingBox box, int page, int size) {
		String pattern = "%" + escapeLike(q == null ? "" : q.trim().toLowerCase(Locale.ROOT)) + "%";
		BoundingBox b = box == null ? BoundingBox.WORLD : box;
		var pageable = PageRequest.of(page, size, Sort.by("name").and(Sort.by("id")));
		var found = repo.search(pattern, b.minLat(), b.maxLat(), b.minLng(), b.maxLng(), pageable);

		// One query for all cafes on the page; stale reports are excluded, not shown as current.
		List<Long> ids = found.getContent().stream().map(Cafe::getId).toList();
		Map<Long, List<CafeCrowdReport>> byCafe = ids.isEmpty() ? Map.of()
				: reports.findByCafeIdInAndObservedAtAfter(ids, freshSince())
						.stream().collect(Collectors.groupingBy(CafeCrowdReport::getCafeId));
		return CafeResponse.from(found.map(c -> CafeResponse.from(c, CrowdCalculator.compute(byCafe.get(c.getId())))));
	}

	@Transactional(readOnly = true)
	public CafeResponse.Crowd currentCrowd(Long cafeId) {
		return CrowdCalculator.compute(reports.findByCafeIdInAndObservedAtAfter(List.of(cafeId), freshSince()));
	}

	private Instant freshSince() {
		return clock.instant().minus(reportTtl);
	}

	static String escapeLike(String s) {
		return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}
}
