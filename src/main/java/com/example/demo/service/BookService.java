package com.example.demo.service;

import com.example.demo.db.Book;
import com.example.demo.google.GoogleBook;

import java.util.List;

/**
 * Service interface for managing books.
 * Defines operations for retrieving books from the local database,
 * searching via Google Books API, and persisting books by their Google Books ID.
 */
public interface BookService {

    /**
     * Retrieves all books stored in the local database.
     *
     * @return list of all saved books, empty list if none exist
     */
    List<Book> getAllBooks();

    /**
     * Searches for books using the Google Books API.
     *
     * @param query the search query string (e.g. "effective java")
     * @param maxResults maximum number of results to return, optional
     * @param startIndex index of the first result, for pagination, optional
     * @return GoogleBook response containing matching book items
     */
    GoogleBook searchGoogleBooks(String query, Integer maxResults, Integer startIndex);

    /**
     * Fetches a book from Google Books API by its volume ID and saves it to the local database.
     *
     * @param googleBookId the Google Books volume ID (e.g. "ka2VUBqHiWkC")
     * @return the saved Book entity
     * @throws com.example.demo.exception.BookAlreadyExistsException if the book already exists in the database
     * @throws IllegalArgumentException if the book data is invalid (missing title or authors)
     */
    Book addBookFromGoogle(String googleBookId);
}