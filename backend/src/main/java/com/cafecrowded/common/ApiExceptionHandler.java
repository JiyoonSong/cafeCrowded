package com.cafecrowded.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolationException;

// Standard Spring MVC errors (400/404/405...) are handled by the parent as RFC 9457 problem details.
// Anything unexpected is logged server-side and returned as a generic 500 with no internals.
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(ConstraintViolationException.class)
	ProblemDetail handleConstraint(ConstraintViolationException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request parameters");
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception e) {
		log.error("Unhandled exception", e);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
	}
}
