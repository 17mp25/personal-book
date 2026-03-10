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

/**
 * Integration tests for the Book API.
 * Starts a full Spring context with H2 in-memory database.
 * Uses MockWebServer to simulate Google Books API responses without real network calls.
 *
 * Assumption: Test JSON fixtures are located in src/test/resources/.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookIntegrationTest {

    static MockWebServer server;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    /** Starts the mock web server before all tests. */
    @BeforeAll
    static void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    /** Shuts down the mock web server after all tests. */
    @AfterAll
    static void stopServer() throws IOException {
        server.shutdown();
    }

    /**
     * Overrides the Google Books API base URL to point to the mock server.
     *
     * @param registry the dynamic property registry
     */
    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("google.books.base-url", () -> server.url("/").toString());
    }

    /** Clears the database before each test to ensure isolation. */
    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    /** Enqueues a successful 200 response with Effective Java book data. */
    private void enqueueBook() throws IOException {
        String body = Files.readString(
                Paths.get("src", "test", "resources", "effectivejava-single.json"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));
    }

    /** Enqueues a 404 Not Found response from the mock server. */
    private void enqueue404() {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 404, \"message\": \"Volume not found\"}}"));
    }

    /** Enqueues a 401 Unauthorized response from the mock server. */
    private void enqueue401() {
        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 401, \"message\": \"Unauthorized\"}}"));
    }

    /**
     * Verifies that a valid book ID returns HTTP 201 and the book is saved to the database.
     */
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

    /**
     * Verifies that adding the same book twice returns HTTP 409 Conflict.
     */
    @Test
    void addBook_duplicateId_returns409() throws Exception {
        enqueueBook();
        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isConflict());
    }

    /**
     * Verifies that an invalid book ID returns HTTP 404.
     */
    @Test
    void addBook_invalidId_returns404() throws Exception {
        enqueue404();
        mockMvc.perform(post("/books/INVALID_ID"))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifies that an unauthorized request returns HTTP 401.
     */
    @Test
    void addBook_unauthorized_returns401() throws Exception {
        enqueue401();
        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Verifies that GET /books returns the book after it has been added.
     */
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

    /**
     * Verifies that GET /books returns an empty list when no books are saved.
     */
    @Test
    void getBooks_whenEmpty_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    /** Enqueues a 403 Forbidden response from the mock server. */
    private void enqueue403() {
        server.enqueue(new MockResponse()
                .setResponseCode(403)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 403, \"message\": \"Forbidden\"}}"));
    }

    /** Enqueues a 500 Internal Server Error response from the mock server. */
    private void enqueue500() {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 500, \"message\": \"Internal Server Error\"}}"));
    }

    /** Enqueues a 503 Service Unavailable response from the mock server. */
    private void enqueue503() {
        server.enqueue(new MockResponse()
                .setResponseCode(503)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 503, \"message\": \"Service Unavailable\"}}"));
    }

    /** Enqueues a 504 Gateway Timeout response from the mock server. */
    private void enqueue504() {
        server.enqueue(new MockResponse()
                .setResponseCode(504)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 504, \"message\": \"Gateway Timeout\"}}"));
    }

    /**
     * Verifies that a forbidden request returns HTTP 403.
     */
    @Test
    void addBook_forbidden_returns403() throws Exception {
        enqueue403();
        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that a Google API server error returns HTTP 502.
     */
    @Test
    void addBook_serverError_returns502() throws Exception {
        enqueue500();
        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isBadGateway());
    }

    /**
     * Verifies that a service unavailable error returns HTTP 503.
     */
    @Test
    void addBook_serviceUnavailable_returns503() throws Exception {
        enqueue503();
        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isServiceUnavailable());
    }

    /**
     * Verifies that a gateway timeout returns HTTP 504.
     */
    @Test
    void addBook_gatewayTimeout_returns504() throws Exception {
        enqueue504();
        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isGatewayTimeout());
    }

    /**
     * Verifies that a wrong endpoint returns HTTP 404.
     */
    @Test
    void addBook_wrongEndpoint_returns404() throws Exception {
        mockMvc.perform(post("/book/ka2VUBqHiWkC"))
                .andExpect(status().isNotFound());
    }
}