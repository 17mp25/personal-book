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

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;


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


    @Test
    void addBookFromGoogle_returns201() throws Exception {
        Book book = new Book("id1", "Clean Code", "Robert Martin", 431);
        when(bookService.addBookFromGoogle("id1")).thenReturn(book);
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert Martin"));
    }

    @Test
    void addBookFromGoogle_returns404WhenNotFound() throws Exception {
        when(bookService.addBookFromGoogle("invalidId"))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null));
        mockMvc.perform(post("/books/invalidId"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBookFromGoogle_returns409WhenDuplicate() throws Exception {
        when(bookService.addBookFromGoogle("id1"))
                .thenThrow(new BookAlreadyExistsException("Book already exists"));
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isConflict());
    }

    @Test
    void addBookFromGoogle_returns400WhenInvalidData() throws Exception {
        when(bookService.addBookFromGoogle("id1"))
                .thenThrow(new IllegalArgumentException("Invalid book data"));
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBookFromGoogle_returns401WhenUnauthorized() throws Exception {
        when(bookService.addBookFromGoogle("id1"))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addBookFromGoogle_returns403WhenForbidden() throws Exception {
        when(bookService.addBookFromGoogle("id1"))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.FORBIDDEN, "Forbidden", null, null, null));
        mockMvc.perform(post("/books/id1"))
                .andExpect(status().isForbidden());
    }
}
