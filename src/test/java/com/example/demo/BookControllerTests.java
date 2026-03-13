package com.example.demo;

import com.example.demo.db.Book;
import com.example.demo.exception.BookAlreadyExistsException;
import com.example.demo.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for BookController.
 * Uses MockMvc to test HTTP layer without starting a full server.
 * BookService is mocked to isolate controller behavior.
 */
@WebMvcTest(BookController.class)
class BookControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    /**
     * Verifies that GET /books returns a list of books with HTTP 200.
     */
    @Test
    void getAllBooks_returnsBooks() throws Exception {
        List<Book> books = List.of(
                new Book("id1", "Clean Code", "Robert Martin", 431),
                new Book("id2", "Effective Java", "Joshua Bloch", 412)
        );
        when(bookService.getAllBooks()).thenReturn(books);
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[1].title").value("Effective Java"));
    }

    /**
     * Verifies that POST /books/{id} returns the saved book with HTTP 201.
     */
    @Test
    void addBookFromGoogle_returns201() throws Exception {
        Book book = new Book("id1", "Clean Code", "Robert Martin", 431);
        when(bookService.addBookFromGoogle("id1")).thenReturn(book);
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert Martin"));
    }

    /**
     * Verifies that HTTP 404 is returned when the book is not found in Google Books API.
     */
    @Test
    void addBookFromGoogle_returns404WhenNotFound() throws Exception {
        when(bookService.addBookFromGoogle("invalidId"))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null));
        mockMvc.perform(post("/books/invalidId"))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifies that HTTP 409 is returned when the book already exists in the database.
     */
    @Test
    void addBookFromGoogle_returns409WhenDuplicate() throws Exception {
        when(bookService.addBookFromGoogle("id1"))
                .thenThrow(new BookAlreadyExistsException("Book already exists"));
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isConflict());
    }

    /**
     * Verifies that HTTP 400 is returned when the book data is invalid (missing title or authors).
     */
    @Test
    void addBookFromGoogle_returns400WhenInvalidData() throws Exception {
        when(bookService.addBookFromGoogle("id1"))
                .thenThrow(new IllegalArgumentException("Invalid book data"));
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that HTTP 401 is returned when the Google Books API key is invalid.
     */
    @Test
    void addBookFromGoogle_returns401WhenUnauthorized() throws Exception {
        when(bookService.addBookFromGoogle("id1"))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Verifies that HTTP 403 is returned when access to Google Books API is forbidden.
     */
    @Test
    void addBookFromGoogle_returns403WhenForbidden() throws Exception {
        when(bookService.addBookFromGoogle("id1"))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.FORBIDDEN, "Forbidden", null, null, null));
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that HTTP 502 is returned when Google Books API returns a 500 server error.
     */
    @Test
    void addBookFromGoogle_returns502WhenGoogleApiIsDown() throws Exception {
        when(bookService.addBookFromGoogle("id1"))
                .thenThrow(HttpServerErrorException.create(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null, null, null));
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isBadGateway());
    }

    /**
     * Verifies that HTTP 503 is returned when Google Books API is unavailable.
     */
    @Test
    void addBookFromGoogle_returns503WhenServiceUnavailable() throws Exception {
        when(bookService.addBookFromGoogle("id1"))
                .thenThrow(HttpServerErrorException.create(
                        HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", null, null, null));
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isServiceUnavailable());
    }

    /**
     * Verifies that HTTP 504 is returned when Google Books API request times out.
     */
    @Test
    void addBookFromGoogle_returns504WhenGatewayTimeout() throws Exception {
        when(bookService.addBookFromGoogle("id1"))
                .thenThrow(HttpServerErrorException.create(
                        HttpStatus.GATEWAY_TIMEOUT, "Gateway Timeout", null, null, null));
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isGatewayTimeout());
    }

    /**
     * Verifies that HTTP 500 is returned when an unexpected error occurs.
     */
    @Test
    void addBookFromGoogle_returns500WhenUnexpectedError() throws Exception {
        when(bookService.addBookFromGoogle("id1"))
                .thenThrow(new RuntimeException("Unexpected error"));
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifies that HTTP 404 is returned when a non-existent endpoint is called.
     */
    @Test
    void addBookFromGoogle_returns404WhenEndpointNotFound() throws Exception {
        mockMvc.perform(post("/book/id1"))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifies that HTTP 400 is returned when googleBookId is blank.
     */
    @Test
    void addBookFromGoogle_returns400WhenBookIdIsBlank() throws Exception {
        mockMvc.perform(post("/books/ "))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that concurrent duplicate inserts return HTTP 409.
     */
    @Test
    void addBookFromGoogle_returns409OnConcurrentDuplicateInsert() throws Exception {
        Book book = new Book("id1", "Clean Code", "Robert Martin", 431);
        when(bookService.addBookFromGoogle("id1"))
                .thenReturn(book)
                .thenThrow(new BookAlreadyExistsException("Book already exists"));

        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isConflict());
    }
}