package com.cafecrowded.cafe;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.cafecrowded.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

	@Autowired MockMvc mvc;
	@Autowired CafeRepository cafes;
	@Autowired CafeCrowdReportRepository reports;

	Long cafeId;

	@BeforeEach
	void setUp() {
		reports.deleteAll();
		cafes.deleteAll();
		cafeId = cafes.save(new Cafe("r-1", "제보 카페", "서울", 37.5, 127.0)).getId();
	}

	ResultActions report(Long id, String ip, String json) throws Exception {
		return mvc.perform(post("/api/cafes/" + id + "/reports").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(r -> { r.setRemoteAddr(ip); return r; }));
	}

	ResultActions report(String ip, String level) throws Exception {
		return report(cafeId, ip, "{\"level\":\"" + level + "\"}");
	}

	@Test
	void acceptedReportShowsUpInSearchWithSourceAndTime() throws Exception {
		report("10.0.0.1", "CROWDED").andExpect(status().isCreated())
			.andExpect(jsonPath("$.level").value("CROWDED"))
			.andExpect(jsonPath("$.source").value("USER_REPORT"))
			.andExpect(jsonPath("$.reportCount").value(1))
			.andExpect(content().string(not(containsString("reporter"))));

		mvc.perform(get("/api/cafes").param("q", "제보"))
			.andExpect(jsonPath("$.items[0].crowd.level").value("CROWDED"))
			.andExpect(jsonPath("$.items[0].crowd.observedAt").isNotEmpty());
	}

	@Test
	void majorityOfFreshReportsWins() throws Exception {
		report("10.0.0.1", "CROWDED").andExpect(status().isCreated());
		report("10.0.0.2", "CROWDED").andExpect(status().isCreated());
		report("10.0.0.3", "QUIET").andExpect(status().isCreated())
			.andExpect(jsonPath("$.level").value("CROWDED"))
			.andExpect(jsonPath("$.reportCount").value(3));
	}

	@Test
	void staleReportsAreNotShownAsCurrent() throws Exception {
		reports.save(new CafeCrowdReport(cafeId, CrowdLevel.CROWDED, "x".repeat(64),
				Instant.now().minus(2, ChronoUnit.HOURS)));
		mvc.perform(get("/api/cafes").param("q", "제보"))
			.andExpect(jsonPath("$.items[0].crowd.level").value("UNKNOWN"))
			.andExpect(jsonPath("$.items[0].crowd.reportCount").value(0));
	}

	@Test
	void sameReporterCannotRepeatForSameCafeWithinCooldown() throws Exception {
		report("10.0.0.1", "QUIET").andExpect(status().isCreated());
		report("10.0.0.1", "CROWDED").andExpect(status().isTooManyRequests());
	}

	@Test
	void reporterHasHourlyCapAcrossCafes() throws Exception {
		for (int i = 0; i < 20; i++) {
			Long id = cafes.save(new Cafe("cap-" + i, "캡 " + i, null, 37.5, 127.0)).getId();
			report(id, "10.0.0.9", "{\"level\":\"NORMAL\"}").andExpect(status().isCreated());
		}
		report(cafeId, "10.0.0.9", "{\"level\":\"NORMAL\"}").andExpect(status().isTooManyRequests());
	}

	@Test
	void rejectsInvalidInput() throws Exception {
		report(cafeId, "10.0.0.1", "{\"level\":\"FULL\"}").andExpect(status().isBadRequest());
		report(cafeId, "10.0.0.1", "{}").andExpect(status().isBadRequest());
		report(cafeId, "10.0.0.1", "not json").andExpect(status().isBadRequest());
		report(0L, "10.0.0.1", "{\"level\":\"QUIET\"}").andExpect(status().isBadRequest());
	}

	@Test
	void unknownCafeIsNotFound() throws Exception {
		report(999_999L, "10.0.0.1", "{\"level\":\"QUIET\"}").andExpect(status().isNotFound());
	}

	@Test
	void clientCannotSetObservationTime() throws Exception {
		report(cafeId, "10.0.0.1", "{\"level\":\"QUIET\",\"observedAt\":\"2000-01-01T00:00:00Z\"}")
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.observedAt").value(not(containsString("2000-01-01"))));
	}
}
