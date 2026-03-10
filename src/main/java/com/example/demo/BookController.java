package com.example.demo;

import com.example.demo.db.Book;
import com.example.demo.google.GoogleBook;
import com.example.demo.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing books.
 * Provides endpoints to retrieve books from the local database,
 * search books via Google Books API, and persist a book by its Google Books ID.
 *
 * Assumption: No authentication is required to access these endpoints.
 */
@RestController
@Slf4j
@FieldDefaults(
        makeFinal = true,
        level = lombok.AccessLevel.PRIVATE
)
@RequiredArgsConstructor
public class BookController {

    BookService bookService;

    /**
     * Retrieves all books stored in the local database.
     *
     * @return list of all saved books, empty list if none exist
     */
    @GetMapping("/books")
    public List<Book> getAllBooks() {
        log.info("getAllBooks - Fetching all books from the database");
        return bookService.getAllBooks();
    }

    /**
     * Searches for books using the Google Books API.
     *
     * @param query the search query string (e.g. "effective java")
     * @param maxResults maximum number of results to return, optional
     * @param startIndex index of the first result, for pagination, optional
     * @return GoogleBook response containing matching book items
     */
    @GetMapping("/google")
    public GoogleBook searchGoogleBooks(@RequestParam("q") String query,
                                        @RequestParam(
                                                value = "maxResults",
                                                required = false
                                        ) Integer maxResults,
                                        @RequestParam(
                                                value = "startIndex",
                                                required = false
                                        ) Integer startIndex) {
        log.info("Get searchGoogleBooks - Searching for books on Google Books API with query: {}, maxResults: {}, " +
                "startIndex: {}", query, maxResults, startIndex);
        return bookService.searchGoogleBooks(query, maxResults, startIndex);
    }

    /**
     * Fetches a book from Google Books API and saves it to the local database.
     *
     * @param googleBookId the Google Books volume ID (e.g. "ka2VUBqHiWkC")
     * @return HTTP 201 with the saved Book entity
     * @throws com.example.demo.exception.BookAlreadyExistsException if the book already exists in the database
     * @throws IllegalArgumentException if the book data is invalid
     */
    @PostMapping("/books/{googleBookId}")
    public ResponseEntity<Book> addBookFromGoogle(@PathVariable String googleBookId) {
        log.info("addBookFromGoogle - Adding book from Google Books API with id: {}", googleBookId);
        Book book = bookService.addBookFromGoogle(googleBookId);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }
}