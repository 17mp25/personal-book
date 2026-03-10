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

@RestController
@Slf4j
@FieldDefaults(
        makeFinal = true,
        level = lombok.AccessLevel.PRIVATE
)
@RequiredArgsConstructor
public class BookController {

    BookService bookService;

    @GetMapping("/books")
    public List<Book> getAllBooks() {
        log.info("getAllBooks - Fetching all books from the database");
        return bookService.getAllBooks();
    }

    @GetMapping("/google")
    public GoogleBook searchGoogleBooks(@RequestParam("q") String query,
                                        @RequestParam(
                                                value = "maxResults",
                                                required = false
                                        ) Integer maxResults,
                                        @RequestParam(
                                                value = "startIndex",
                                                required = false
                                        ) Integer startIndex)
    {
        log.info("Get searchGoogleBooks - Searching for books on Google Books API with query: {}, maxResults: {}, " +
                "startIndex: {}", query, maxResults, startIndex);
        return bookService.searchGoogleBooks(query, maxResults, startIndex);
    }

    @PostMapping("/books/{googleBookId}")
    public ResponseEntity<Book> addBookFromGoogle(@PathVariable String googleBookId) {
        log.info("addBookFromGoogle - Adding book from Google Books API with id: {}", googleBookId);
        Book book = bookService.addBookFromGoogle(googleBookId);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }
}
