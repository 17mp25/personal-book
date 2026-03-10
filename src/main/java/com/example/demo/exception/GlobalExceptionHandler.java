package com.example.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for the application.
 * Intercepts exceptions thrown by controllers and returns appropriate HTTP responses.
 *
 * Assumption: All Google Books API errors are propagated up and handled here.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles requests to non-existent endpoints.
     *
     * @param ex the NoResourceFoundException thrown
     * @return HTTP 404 with error details
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        log.error("Endpoint not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Endpoint not found: " + ex.getResourcePath());
    }

    /**
     * Handles validation errors from invalid input arguments.
     *
     * @param ex the IllegalArgumentException thrown
     * @return HTTP 400 with error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Validation error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid input: " + ex.getMessage());
    }

    /**
     * Handles 401 Unauthorized from Google Books API (invalid API key).
     *
     * @param ex the HttpClientErrorException thrown
     * @return HTTP 401 with error details
     */
    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public ResponseEntity<ErrorResponse> handleHttpClientUnauthorized(HttpClientErrorException ex) {
        log.error("Unauthorized - Invalid API key: {} - {}", ex.getStatusCode(), ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid or Google Books API key missing");
    }

    /**
     * Handles 403 Forbidden from Google Books API.
     *
     * @param ex the HttpClientErrorException thrown
     * @return HTTP 403 with error details
     */
    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public ResponseEntity<ErrorResponse> handleHttpClientForbidden(HttpClientErrorException ex) {
        log.error("Forbidden - Access denied: {} - {}", ex.getStatusCode(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Access forbidden to Google Books API");
    }

    /**
     * Handles 404 Not Found from Google Books API.
     *
     * @param ex the HttpClientErrorException thrown
     * @return HTTP 404 with error details
     */
    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleHttpClientNotFound(HttpClientErrorException ex) {
        log.error("Not Found - Endpoint not found: {} - {}", ex.getStatusCode(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Google Books API endpoint not found");
    }

    /**
     * Handles 400 Bad Request from Google Books API.
     *
     * @param ex the HttpClientErrorException thrown
     * @return HTTP 400 with error details
     */
    @ExceptionHandler(HttpClientErrorException.BadRequest.class)
    public ResponseEntity<ErrorResponse> handleHttpClientBadRequest(HttpClientErrorException ex) {
        log.error("Bad Request - Client error: {} - {}", ex.getStatusCode(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Google Books API key. Please check your API key.");
    }

    /**
     * Handles 429 Too Many Requests from Google Books API (quota exceeded).
     *
     * @param ex the HttpClientErrorException.TooManyRequests thrown
     * @return HTTP 429 with error details
     */
    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(HttpClientErrorException.TooManyRequests ex) {
        log.error("Too Many Requests - Quota exceeded: {}", ex.getMessage());
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "Google Books API quota exceeded. Please try again later.");
    }

    /**
     * Handles duplicate book saves.
     *
     * @param ex the BookAlreadyExistsException thrown
     * @return HTTP 409 with error details
     */
    @ExceptionHandler(BookAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBookAlreadyExists(BookAlreadyExistsException ex) {
        log.error("Book already exists: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles 503 Service Unavailable from Google Books API.
     *
     * @param ex the HttpServerErrorException thrown
     * @return HTTP 503 with error details
     */
    @ExceptionHandler(HttpServerErrorException.ServiceUnavailable.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(HttpServerErrorException ex) {
        log.error("Service Unavailable - Google Books API is down: {} - {}", ex.getStatusCode(), ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE,
                "Google Books API is temporarily unavailable. Please try again later.");
    }

    /**
     * Handles 504 Gateway Timeout from Google Books API.
     *
     * @param ex the HttpServerErrorException thrown
     * @return HTTP 504 with error details
     */
    @ExceptionHandler(HttpServerErrorException.GatewayTimeout.class)
    public ResponseEntity<ErrorResponse> handleGatewayTimeout(HttpServerErrorException ex) {
        log.error("Gateway Timeout - Google Books API timed out: {} - {}", ex.getStatusCode(), ex.getMessage());
        return buildResponse(HttpStatus.GATEWAY_TIMEOUT, "Google Books API request timed out. Please try again later.");
    }

    /**
     * Handles all other server errors from Google Books API (500, 502, etc.).
     *
     * @param ex the HttpServerErrorException thrown
     * @return HTTP 502 with error details
     */
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpServerError(HttpServerErrorException ex) {
        log.error("Google API server error: {} - {}", ex.getStatusCode(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_GATEWAY, "Google Books API is currently unavailable");
    }

    /**
     * Handles all unexpected errors not caught by other handlers.
     *
     * @param ex the Exception thrown
     * @return HTTP 500 with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message));
    }
}