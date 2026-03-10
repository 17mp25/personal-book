package com.example.demo.service;

import com.example.demo.db.Book;
import com.example.demo.exception.BookAlreadyExistsException;
import com.example.demo.google.GoogleBook;
import com.example.demo.google.GoogleBookService;
import com.example.demo.mapper.BookMapper;
import com.example.demo.repository.BookRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of BookService.
 * Handles fetching books from Google Books API and persisting them to the local database.
 *
 * Assumption: H2 in-memory database is used, data resets on restart.
 */
@Service
@Slf4j
@FieldDefaults(
        makeFinal = true,
        level = lombok.AccessLevel.PRIVATE
)
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    BookRepository bookRepository;
    GoogleBookService googleBookService;
    BookMapper bookMapper;

    /**
     * Retrieves all books from the local database.
     *
     * @return list of all saved books, empty list if none exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        log.info("Getting all books from the database");
        return bookRepository.findAll();
    }

    /**
     * Searches for books using the Google Books API.
     *
     * @param query the search query string
     * @param maxResults maximum number of results to return, optional
     * @param startIndex index of the first result, for pagination, optional
     * @return GoogleBook response containing matching book items
     */
    @Override
    public GoogleBook searchGoogleBooks(String query, Integer maxResults, Integer startIndex) {
        log.info("Searching for books on Google Books API with query: {}, maxResults: {}, startIndex: {}", query,
                maxResults, startIndex);
        return googleBookService.searchBooks(query, maxResults, startIndex);
    }

    /**
     * Fetches a book from Google Books API by its volume ID and saves it to the local database.
     * Validates that the book has a title and at least one author before saving.
     *
     * @param googleBookId the Google Books volume ID
     * @return the saved Book entity
     * @throws BookAlreadyExistsException if the book already exists in the database
     * @throws IllegalArgumentException if the book has no title or authors
     */
    @Override
    @Transactional
    public Book addBookFromGoogle(String googleBookId) {
        log.info("Adding book from Google Books API with id: {}", googleBookId);

        if (bookRepository.existsById(googleBookId)) {
            log.warn("Book with id {} already exists in the database", googleBookId);
            throw new BookAlreadyExistsException("Book with id " + googleBookId + " already exists in the database");
        }

        GoogleBook.Item item = googleBookService.getBookById(googleBookId);
        if (item == null || item.volumeInfo() == null) {
            log.error("Book with id {} not found in Google Books API", googleBookId);
            throw new IllegalArgumentException("Book with id " + googleBookId + " not found in Google Books API");
        }

        if (item.volumeInfo().authors() == null || item.volumeInfo().authors().isEmpty()) {
            log.error("Book with id {} does not have an author in Google Books API", googleBookId);
            throw new IllegalArgumentException(
                    "Book with id " + googleBookId + " does not have an author in Google Books API");
        }

        if (item.volumeInfo().title() == null || item.volumeInfo().title().isEmpty()) {
            log.error("Book with id {} does not have a title in Google Books API", googleBookId);
            throw new IllegalArgumentException(
                    "Book with id " + googleBookId + " does not have a title in Google Books API");
        }

        Book book = bookMapper.toBook(item);
        Book saved = bookRepository.save(book);
        log.info("Book with id {} added to the database", saved.getId());
        return saved;
    }
}