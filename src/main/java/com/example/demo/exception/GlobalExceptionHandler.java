package com.example.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Validation error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid input: ");
    }

    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public ResponseEntity<ErrorResponse> handleHttpClientUnauthorized(HttpClientErrorException ex) {
        log.error("Unauthorized - Invalid API key: {} - {}", ex.getStatusCode(), ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid or Google Books API key missing");
    }

    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public ResponseEntity<ErrorResponse> handleHttpClientForbidden(HttpClientErrorException ex) {
        log.error("Forbidden - Access denied: {} - {}", ex.getStatusCode(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Access forbidden to Google Books API");
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleHttpClientNotFound(HttpClientErrorException ex) {
        log.error("Not Found - Endpoint not found: {} - {}", ex.getStatusCode(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Google Books API endpoint not found");
    }

    @ExceptionHandler(HttpClientErrorException.BadRequest.class)
    public ResponseEntity<ErrorResponse> handleHttpClientBadRequest(HttpClientErrorException ex) {
        log.error("Bad Request - Client error: {} - {}", ex.getStatusCode(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Google Books API key. Please check your API key.");
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(HttpClientErrorException.TooManyRequests ex) {
        log.error("Too Many Requests - Quota exceeded: {}", ex.getMessage());
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "Google Books API quota exceeded. Please try again later.");
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpServerError(HttpServerErrorException ex) {
        log.error("Google API server error: {} - {}", ex.getStatusCode(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_GATEWAY, "Google Books API is currently unavailable");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: ");
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBookAlreadyExists(BookAlreadyExistsException ex) {
        log.error("Book already exists: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message));
    }

}
