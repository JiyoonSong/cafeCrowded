package com.cafecrowded.cafe;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.cafecrowded.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class CafeControllerTest {

	@Autowired MockMvc mvc;
	@Autowired CafeRepository repo;
	@Autowired CafeCrowdReportRepository reportRepo;

	@BeforeEach
	void seed() {
		reportRepo.deleteAll();
		repo.deleteAll();
		repo.save(new Cafe("t-1", "테스트 카페 강남", "서울 강남구 테헤란로 1", 37.498, 127.027));
		repo.save(new Cafe("t-2", "테스트 카페 홍대", "서울 마포구 양화로 2", 37.556, 126.923));
		repo.save(new Cafe("t-3", "100% Coffee", null, 37.500, 127.030));
	}

	@Test
	void searchesByNameAndReturnsUnknownCrowd() throws Exception {
		mvc.perform(get("/api/cafes").param("q", "홍대"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalElements").value(1))
			.andExpect(jsonPath("$.items[0].name").value("테스트 카페 홍대"))
			.andExpect(jsonPath("$.items[0].crowd.level").value("UNKNOWN"));
	}

	@Test
	void searchesByAddress() throws Exception {
		mvc.perform(get("/api/cafes").param("q", "마포구"))
			.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void filtersByBoundingBox() throws Exception {
		mvc.perform(get("/api/cafes")
				.param("minLat", "37.49").param("maxLat", "37.51")
				.param("minLng", "127.0").param("maxLng", "127.05"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalElements").value(2));
	}

	@Test
	void likeWildcardsInQueryAreLiteral() throws Exception {
		mvc.perform(get("/api/cafes").param("q", "%"))
			.andExpect(jsonPath("$.totalElements").value(1))
			.andExpect(jsonPath("$.items[0].name").value("100% Coffee"));
	}

	@Test
	void rejectsOversizedPageSize() throws Exception {
		mvc.perform(get("/api/cafes").param("size", "1000"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsOutOfRangeLatitude() throws Exception {
		mvc.perform(get("/api/cafes").param("minLat", "95").param("maxLat", "96")
				.param("minLng", "0").param("maxLng", "1"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsInvertedBoundingBox() throws Exception {
		mvc.perform(get("/api/cafes").param("minLat", "37.6").param("maxLat", "37.5")
				.param("minLng", "127.0").param("maxLng", "127.1"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsPartialBoundingBox() throws Exception {
		mvc.perform(get("/api/cafes").param("minLat", "37.5"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsTooLongQuery() throws Exception {
		mvc.perform(get("/api/cafes").param("q", "a".repeat(51)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void errorBodyLeaksNoInternals() throws Exception {
		mvc.perform(get("/api/cafes").param("size", "abc"))
			.andExpect(status().isBadRequest())
			.andExpect(content().string(not(containsString("Exception"))))
			.andExpect(content().string(not(containsString("com.cafecrowded"))));
	}

	@Test
	void unknownApiPathIsNotFoundNotServerError() throws Exception {
		mvc.perform(get("/api/nope")).andExpect(status().isNotFound());
	}
}
