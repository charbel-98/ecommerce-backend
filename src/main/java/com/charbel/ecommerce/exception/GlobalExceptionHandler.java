package com.charbel.ecommerce.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest request) {
		log.warn("User already exists: {}", ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.CONFLICT.value())
				.error("User Already Exists").message(ex.getMessage())
				.path(request.getDescription(false).replace("uri=", "")).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex, WebRequest request) {
		log.warn("Invalid token: {}", ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.UNAUTHORIZED.value())
				.error("Invalid Token").message(ex.getMessage()).path(request.getDescription(false).replace("uri=", ""))
				.timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
		log.warn("Bad credentials: {}", ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.UNAUTHORIZED.value())
				.error("Invalid Credentials").message("Invalid email or password")
				.path(request.getDescription(false).replace("uri=", "")).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
			WebRequest request) {
		log.warn("Validation error: {}", ex.getMessage());

		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.BAD_REQUEST.value())
				.error("Validation Failed").message("Invalid input data")
				.path(request.getDescription(false).replace("uri=", "")).timestamp(LocalDateTime.now())
				.validationErrors(errors).build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			WebRequest request) {
		log.warn("Request body missing or malformed: {}", ex.getMessage());

		String message = "Request body is required";
		if (ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")) {
			message = "Request body is required and cannot be empty";
		} else if (ex.getMessage() != null && ex.getMessage().contains("JSON parse error")) {
			message = "Invalid JSON format in request body";
		}

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.BAD_REQUEST.value())
				.error("Bad Request").message(message).path(request.getDescription(false).replace("uri=", ""))
				.timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(ReviewNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleReviewNotFound(ReviewNotFoundException ex, WebRequest request) {
		log.warn("Review not found: {}", ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.NOT_FOUND.value())
				.error("Review Not Found").message(ex.getMessage())
				.path(request.getDescription(false).replace("uri=", "")).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(DuplicateReviewException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateReview(DuplicateReviewException ex, WebRequest request) {
		log.warn("Duplicate review: {}", ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.CONFLICT.value())
				.error("Duplicate Review").message(ex.getMessage())
				.path(request.getDescription(false).replace("uri=", "")).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	@ExceptionHandler(UnauthorizedReviewAccessException.class)
	public ResponseEntity<ErrorResponse> handleUnauthorizedReviewAccess(UnauthorizedReviewAccessException ex, WebRequest request) {
		log.warn("Unauthorized review access: {}", ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.FORBIDDEN.value())
				.error("Unauthorized Access").message(ex.getMessage())
				.path(request.getDescription(false).replace("uri=", "")).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
		log.error("Unexpected error: ", ex);

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.error("Internal Server Error").message("An unexpected error occurred")
				.path(request.getDescription(false).replace("uri=", "")).timestamp(LocalDateTime.now()).build();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
}
