package com.cafecrowded.cafe;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/cafes")
public class CafeController {

	private final CafeService service;

	public CafeController(CafeService service) {
		this.service = service;
	}

	@GetMapping
	public CafeResponse.PageResponse search(
			@RequestParam(required = false) @Size(max = 50) String q,
			@RequestParam(required = false) @DecimalMin("-90") @DecimalMax("90") Double minLat,
			@RequestParam(required = false) @DecimalMin("-90") @DecimalMax("90") Double maxLat,
			@RequestParam(required = false) @DecimalMin("-180") @DecimalMax("180") Double minLng,
			@RequestParam(required = false) @DecimalMin("-180") @DecimalMax("180") Double maxLng,
			@RequestParam(defaultValue = "0") @Min(0) @Max(10_000) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {

		CafeService.BoundingBox box = null;
		int given = (minLat != null ? 1 : 0) + (maxLat != null ? 1 : 0) + (minLng != null ? 1 : 0)
				+ (maxLng != null ? 1 : 0);
		if (given == 4) {
			if (minLat > maxLat || minLng > maxLng) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bounding box");
			}
			box = new CafeService.BoundingBox(minLat, maxLat, minLng, maxLng);
		} else if (given != 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide all four bounding box values");
		}
		return service.search(q, box, page, size);
	}
}
