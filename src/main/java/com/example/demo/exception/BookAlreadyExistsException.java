package com.example.demo.exception;

/**
 * Exception thrown when attempting to save a book that already exists in the database.
 * Results in HTTP 409 Conflict response.
 */
public class BookAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a BookAlreadyExistsException with the given message.
     *
     * @param message description of the conflict
     */
    public BookAlreadyExistsException(String message) {
        super(message);
    }
}