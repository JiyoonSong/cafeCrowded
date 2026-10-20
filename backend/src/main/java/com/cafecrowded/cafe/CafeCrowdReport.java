package com.cafecrowded.cafe;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cafe_crowd_report")
public class CafeCrowdReport {

	public static final String SOURCE_USER_REPORT = "USER_REPORT";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "cafe_id", nullable = false, updatable = false)
	private Long cafeId;

	@Enumerated(EnumType.STRING)
	@Column(name = "crowd_level", nullable = false, updatable = false)
	private CrowdLevel level;

	@Column(nullable = false, updatable = false)
	private String source;

	// HMAC of the client address; the raw address is never stored.
	@Column(name = "reporter_hash", nullable = false, updatable = false, length = 64)
	private String reporterHash;

	@Column(name = "observed_at", nullable = false, updatable = false)
	private Instant observedAt;

	protected CafeCrowdReport() {
	}

	public CafeCrowdReport(Long cafeId, CrowdLevel level, String reporterHash, Instant observedAt) {
		this.cafeId = cafeId;
		this.level = level;
		this.source = SOURCE_USER_REPORT;
		this.reporterHash = reporterHash;
		this.observedAt = observedAt;
	}

	public Long getCafeId() { return cafeId; }
	public CrowdLevel getLevel() { return level; }
	public String getSource() { return source; }
	public Instant getObservedAt() { return observedAt; }
}
