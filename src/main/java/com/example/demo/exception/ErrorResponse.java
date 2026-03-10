package com.example.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a standardized error response returned by the API.
 * Used by GlobalExceptionHandler to return consistent error structures.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {

    /** HTTP status code (e.g. 404, 500). */
    private int status;

    /** Human-readable error message. */
    private String message;

    /** HTTP status reason phrase (e.g. "Not Found", "Internal Server Error"). */
    private String error;

    /** Timestamp of when the error occurred. */
    private String timestamp;

    /**
     * Creates an ErrorResponse from an HttpStatus and a message.
     *
     * @param httpStatus the HTTP status to use
     * @param message    the error message
     * @return a new ErrorResponse instance
     */
    public static ErrorResponse of(org.springframework.http.HttpStatus httpStatus, String message) {
        return new ErrorResponse(
                httpStatus.value(),
                message,
                httpStatus.getReasonPhrase(),
                LocalDateTime.now().toString()
        );
    }
}