package com.cafecrowded.cafe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class CrowdCalculatorTest {

	static CafeCrowdReport r(CrowdLevel level, long epochSecond) {
		return new CafeCrowdReport(1L, level, "h", Instant.ofEpochSecond(epochSecond));
	}

	@Test
	void noReportsIsUnknown() {
		assertEquals("UNKNOWN", CrowdCalculator.compute(List.of()).level());
	}

	@Test
	void tieGoesToMostRecentReport() {
		var crowd = CrowdCalculator.compute(List.of(r(CrowdLevel.QUIET, 100), r(CrowdLevel.CROWDED, 200)));
		assertEquals("CROWDED", crowd.level());
		assertEquals(2, crowd.reportCount());
		assertEquals(Instant.ofEpochSecond(200), crowd.observedAt());
	}

	@Test
	void majorityBeatsRecency() {
		var crowd = CrowdCalculator.compute(
				List.of(r(CrowdLevel.QUIET, 100), r(CrowdLevel.QUIET, 110), r(CrowdLevel.CROWDED, 200)));
		assertEquals("QUIET", crowd.level());
	}
}
