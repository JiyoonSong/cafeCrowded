package com.cafecrowded.cafe;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/cafes/{cafeId}/reports")
public class ReportController {

	public record ReportRequest(@NotNull CrowdLevel level) {
	}

	private final ReportService service;

	public ReportController(ReportService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CafeResponse.Crowd report(@PathVariable @Positive long cafeId, @Valid @RequestBody ReportRequest body,
			HttpServletRequest request) {
		// getRemoteAddr is the direct peer. Forwarded headers are not trusted unless a proxy is configured explicitly.
		return service.submit(cafeId, body.level(), request.getRemoteAddr());
	}
}
