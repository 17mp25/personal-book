package com.example.demo.service;

import com.example.demo.db.Book;
import com.example.demo.exception.BookAlreadyExistsException;
import com.example.demo.google.GoogleBook;
import com.example.demo.google.GoogleBookService;
import com.example.demo.mapper.BookMapper;
import com.example.demo.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookServiceImpl.
 * All dependencies are mocked to isolate service layer logic.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private GoogleBookService googleBookService;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    /**
     * Verifies that getAllBooks returns the list from the repository.
     */
    @Test
    void getAllBooks_returnsAllBooks() {
        List<Book> books = List.of(
                new Book("id1", "Clean Code", "Robert Martin", 431),
                new Book("id2", "Effective Java", "Joshua Bloch", 412)
        );
        when(bookRepository.findAll()).thenReturn(books);

        List<Book> result = bookService.getAllBooks();

        assertThat(result).hasSize(2);
        verify(bookRepository).findAll();
    }

    /**
     * Verifies that addBookFromGoogle saves and returns the book when valid.
     */
    @Test
    void addBookFromGoogle_savesAndReturnsBook() {
        String bookId = "id1";
        GoogleBook.VolumeInfo volumeInfo = new GoogleBook.VolumeInfo(
                "Clean Code", List.of("Robert Martin"), null, null,
                431, null, null, null, null, null, null
        );
        GoogleBook.Item item = new GoogleBook.Item(bookId, null, volumeInfo, null);
        Book book = new Book(bookId, "Clean Code", "Robert Martin", 431);

        when(bookRepository.existsById(bookId)).thenReturn(false);
        when(googleBookService.getBookById(bookId)).thenReturn(item);
        when(bookMapper.toBook(item)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.addBookFromGoogle(bookId);

        assertThat(result.getTitle()).isEqualTo("Clean Code");
        verify(bookRepository).save(book);
    }

    /**
     * Verifies that BookAlreadyExistsException is thrown when book already exists.
     */
    @Test
    void addBookFromGoogle_throwsWhenBookAlreadyExists() {
        when(bookRepository.existsById("id1")).thenReturn(true);

        assertThatThrownBy(() -> bookService.addBookFromGoogle("id1"))
                .isInstanceOf(BookAlreadyExistsException.class)
                .hasMessageContaining("id1");

        verify(googleBookService, never()).getBookById(any());
    }

    /**
     * Verifies that IllegalArgumentException is thrown when book has no title.
     */
    @Test
    void addBookFromGoogle_throwsWhenTitleMissing() {
        GoogleBook.VolumeInfo volumeInfo = new GoogleBook.VolumeInfo(
                null, List.of("Robert Martin"), null, null,
                431, null, null, null, null, null, null
        );
        GoogleBook.Item item = new GoogleBook.Item("id1", null, volumeInfo, null);

        when(bookRepository.existsById("id1")).thenReturn(false);
        when(googleBookService.getBookById("id1")).thenReturn(item);

        assertThatThrownBy(() -> bookService.addBookFromGoogle("id1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");
    }

    /**
     * Verifies that IllegalArgumentException is thrown when book has no authors.
     */
    @Test
    void addBookFromGoogle_throwsWhenAuthorsMissing() {
        GoogleBook.VolumeInfo volumeInfo = new GoogleBook.VolumeInfo(
                "Clean Code", null, null, null,
                431, null, null, null, null, null, null
        );
        GoogleBook.Item item = new GoogleBook.Item("id1", null, volumeInfo, null);

        when(bookRepository.existsById("id1")).thenReturn(false);
        when(googleBookService.getBookById("id1")).thenReturn(item);

        assertThatThrownBy(() -> bookService.addBookFromGoogle("id1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("author");
    }

    /**
     * Verifies that IllegalArgumentException is thrown when Google Books API returns null item.
     */
    @Test
    void addBookFromGoogle_throwsWhenItemIsNull() {
        when(bookRepository.existsById("id1")).thenReturn(false);
        when(googleBookService.getBookById("id1")).thenReturn(null);

        assertThatThrownBy(() -> bookService.addBookFromGoogle("id1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}