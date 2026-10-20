package com.cafecrowded.cafe;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;

public record CafeResponse(Long id, String name, String address, double latitude, double longitude, Crowd crowd) {

	// Cafe-level crowd. UNKNOWN when there is no fresh user report (never invented).
	public record Crowd(String level, String source, Instant observedAt, int reportCount) {
		static Crowd unknown() {
			return new Crowd("UNKNOWN", null, null, 0);
		}
	}

	public record PageResponse(List<CafeResponse> items, int page, int size, long totalElements) {
	}

	static CafeResponse from(Cafe c, Crowd crowd) {
		return new CafeResponse(c.getId(), c.getName(), c.getAddress(), c.getLatitude(), c.getLongitude(), crowd);
	}

	static PageResponse from(Page<CafeResponse> page) {
		return new PageResponse(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
	}
}
