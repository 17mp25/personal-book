package com.example.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private String error;
    private String timestamp;

    public static ErrorResponse of(org.springframework.http.HttpStatus httpStatus, String message) {
        return new ErrorResponse(
                httpStatus.value(),
                message,
                httpStatus.getReasonPhrase(),
                LocalDateTime.now().toString()
        );
    }
}
