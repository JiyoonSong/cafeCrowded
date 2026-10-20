package com.cafecrowded.cafe;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Initial rule for the cafe-level crowd value, based only on fresh user reports
 * (callers pass reports already filtered to the freshness window):
 * the level reported most often wins; ties go to the level of the most recent report.
 * No reports means UNKNOWN. Nothing is estimated or invented.
 */
final class CrowdCalculator {

	private CrowdCalculator() {
	}

	static CafeResponse.Crowd compute(List<CafeCrowdReport> freshReports) {
		if (freshReports == null || freshReports.isEmpty()) {
			return CafeResponse.Crowd.unknown();
		}
		Map<CrowdLevel, Integer> counts = new EnumMap<>(CrowdLevel.class);
		for (CafeCrowdReport r : freshReports) {
			counts.merge(r.getLevel(), 1, Integer::sum);
		}
		CafeCrowdReport latest = freshReports.stream()
				.max(Comparator.comparing(CafeCrowdReport::getObservedAt)).orElseThrow();
		int top = counts.values().stream().max(Integer::compare).orElseThrow();
		CrowdLevel level = counts.get(latest.getLevel()) == top
				? latest.getLevel()
				: counts.entrySet().stream().filter(e -> e.getValue() == top).map(Map.Entry::getKey).findFirst().orElseThrow();
		return new CafeResponse.Crowd(level.name(), CafeCrowdReport.SOURCE_USER_REPORT, latest.getObservedAt(),
				freshReports.size());
	}
}
