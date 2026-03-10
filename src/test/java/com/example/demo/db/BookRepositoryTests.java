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

@SpringBootTest
@Transactional
class BookRepositoryTests {
    @Autowired private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

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

    @Test
    void shouldFindBookById() {
        bookRepository.save(new Book("id1", "Clean Code", "Robert Martin", 431));
        Optional<Book> found = bookRepository.findById("id1");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void shouldReturnEmptyWhenBookNotFound() {
        Optional<Book> found = bookRepository.findById("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenBookExists() {
        bookRepository.save(new Book("id1", "Clean Code", "Robert Martin", 431));
        assertThat(bookRepository.existsById("id1")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenBookDoesNotExist() {
        assertThat(bookRepository.existsById("nonexistent")).isFalse();
    }

    @Test
    void shouldDeleteBook() {
        bookRepository.save(new Book("id1", "Clean Code", "Robert Martin", 431));
        bookRepository.deleteById("id1");
        assertThat(bookRepository.findById("id1")).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoBooksExist() {
        List<Book> books = bookRepository.findAll();
        assertThat(books).isEmpty();
    }
}

