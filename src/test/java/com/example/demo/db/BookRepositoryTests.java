package com.example.demo.db;

import com.example.demo.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository layer tests for BookRepository.
 * Uses H2 in-memory database with a full Spring context.
 * Each test runs in a transaction that is rolled back after completion.
 */
@SpringBootTest
@Transactional
class BookRepositoryTests {

    @Autowired
    private BookRepository bookRepository;

    /** Clears the database before each test to ensure isolation. */
    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    /**
     * Verifies that multiple books can be saved and retrieved.
     */
    @Test
    void testSaveAndFindAll() {
        Book book1 = new Book("one", "Title One", "Author A");
        Book book2 = new Book("two", "Title Two", "Author B");
        bookRepository.save(book1);
        bookRepository.save(book2);
        List<Book> books = bookRepository.findAll();
        assertThat(books).hasSize(2);
        assertThat(books).extracting(Book::getTitle).containsExactlyInAnyOrder("Title One", "Title Two");
    }

    /**
     * Verifies that a saved book can be found by its ID.
     */
    @Test
    void shouldFindBookById() {
        bookRepository.save(new Book("id1", "Clean Code", "Robert Martin", 431));
        Optional<Book> found = bookRepository.findById("id1");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Clean Code");
    }

    /**
     * Verifies that finding a non-existent book returns empty.
     */
    @Test
    void shouldReturnEmptyWhenBookNotFound() {
        Optional<Book> found = bookRepository.findById("nonexistent");
        assertThat(found).isEmpty();
    }

    /**
     * Verifies that existsById returns true for a saved book.
     */
    @Test
    void shouldReturnTrueWhenBookExists() {
        bookRepository.save(new Book("id1", "Clean Code", "Robert Martin", 431));
        assertThat(bookRepository.existsById("id1")).isTrue();
    }

    /**
     * Verifies that existsById returns false for a non-existent book.
     */
    @Test
    void shouldReturnFalseWhenBookDoesNotExist() {
        assertThat(bookRepository.existsById("nonexistent")).isFalse();
    }

    /**
     * Verifies that a book can be deleted by its ID.
     */
    @Test
    void shouldDeleteBook() {
        bookRepository.save(new Book("id1", "Clean Code", "Robert Martin", 431));
        bookRepository.deleteById("id1");
        assertThat(bookRepository.findById("id1")).isEmpty();
    }

    /**
     * Verifies that findAll returns an empty list when no books exist.
     */
    @Test
    void shouldReturnEmptyListWhenNoBooksExist() {
        List<Book> books = bookRepository.findAll();
        assertThat(books).isEmpty();
    }
}