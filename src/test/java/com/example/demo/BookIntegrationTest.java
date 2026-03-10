package com.example.demo;


import com.example.demo.repository.BookRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookIntegrationTest {

    static MockWebServer server;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @BeforeAll
    static void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        server.shutdown();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("google.books.base-url", () -> server.url("/").toString());
    }

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    private void enqueueBook() throws IOException {
        String body = Files.readString(
                Paths.get("src", "test", "resources", "effectivejava-single.json"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));
    }

    private void enqueue404() {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 404, \"message\": \"Volume not found\"}}"));
    }

    private void enqueue401() {
        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 401, \"message\": \"Unauthorized\"}}"));
    }


    @Test
    void addBook_validId_returns201AndSavesToDB() throws Exception {
        enqueueBook();
        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ka2VUBqHiWkC"))
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.author").value("Joshua Bloch"));

        assertThat(bookRepository.findById("ka2VUBqHiWkC")).isPresent();
    }

    @Test
    void addBook_duplicateId_returns409() throws Exception {
        enqueueBook();
        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isConflict());
    }

    @Test
    void addBook_invalidId_returns404() throws Exception {
        enqueue404();
        mockMvc.perform(post("/books/INVALID_ID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBook_unauthorized_returns401() throws Exception {
        enqueue401();
        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void getBooks_afterAddingBook_returnsBook() throws Exception {
        enqueueBook();
        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Effective Java"))
                .andExpect(jsonPath("$[0].author").value("Joshua Bloch"));
    }

    @Test
    void getBooks_whenEmpty_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}