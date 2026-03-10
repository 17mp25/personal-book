package com.example.demo.google;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for GoogleBookService.
 * Uses MockWebServer to simulate Google Books API responses without real network calls.
 *
 * Assumption: Test JSON fixtures are located in src/test/resources/.
 */
class GoogleBookServiceTest {

    static MockWebServer server;
    static GoogleBookService googleBookService;

    /** Starts the mock web server and initializes GoogleBookService before all tests. */
    @BeforeAll
    static void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        googleBookService = new GoogleBookService(server.url("/").toString(), "test-api-key");
    }

    /** Shuts down the mock web server after all tests. */
    @AfterAll
    static void tearDown() throws IOException {
        server.shutdown();
    }

    /**
     * Verifies that searchBooks returns a valid GoogleBook response on success.
     */
    @Test
    void searchBooks_returnsResults() throws IOException {
        String body = Files.readString(
                Paths.get("src", "test", "resources", "effectivejava.json"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        GoogleBook result = googleBookService.searchBooks("Effective Java", 10, 0);

        assertThat(result).isNotNull();
        assertThat(result.items()).isNotEmpty();
    }

    /**
     * Verifies that getBookById returns the correct book item on success.
     */
    @Test
    void getBookById_returnsBook() throws IOException {
        String body = Files.readString(
                Paths.get("src", "test", "resources", "effectivejava-single.json"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        GoogleBook.Item result = googleBookService.getBookById("ka2VUBqHiWkC");

        assertThat(result).isNotNull();
        assertThat(result.volumeInfo().title()).isEqualTo("Effective Java");
    }

    /**
     * Verifies that getBookById throws HttpClientErrorException.NotFound on 404.
     */
    @Test
    void getBookById_throws404WhenNotFound() {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 404, \"message\": \"Volume not found\"}}"));

        assertThatThrownBy(() -> googleBookService.getBookById("INVALID_ID"))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    /**
     * Verifies that getBookById throws HttpClientErrorException.Unauthorized on 401.
     */
    @Test
    void getBookById_throws401WhenUnauthorized() {
        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 401, \"message\": \"Unauthorized\"}}"));

        assertThatThrownBy(() -> googleBookService.getBookById("ka2VUBqHiWkC"))
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    /**
     * Verifies that getBookById throws HttpClientErrorException.Forbidden on 403.
     */
    @Test
    void getBookById_throws403WhenForbidden() {
        server.enqueue(new MockResponse()
                .setResponseCode(403)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 403, \"message\": \"Forbidden\"}}"));

        assertThatThrownBy(() -> googleBookService.getBookById("ka2VUBqHiWkC"))
                .isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    /**
     * Verifies that getBookById throws HttpServerErrorException on 500.
     */
    @Test
    void getBookById_throws500WhenServerError() {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 500, \"message\": \"Internal Server Error\"}}"));

        assertThatThrownBy(() -> googleBookService.getBookById("ka2VUBqHiWkC"))
                .isInstanceOf(HttpServerErrorException.class);
    }

    /**
     * Verifies that searchBooks throws HttpClientErrorException.Unauthorized on 401.
     */
    @Test
    void searchBooks_throws401WhenUnauthorized() {
        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 401, \"message\": \"Unauthorized\"}}"));

        assertThatThrownBy(() -> googleBookService.searchBooks("Effective Java", 10, 0))
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    /**
     * Verifies that searchBooks throws HttpServerErrorException on 503.
     */
    @Test
    void searchBooks_throws503WhenServiceUnavailable() {
        server.enqueue(new MockResponse()
                .setResponseCode(503)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": {\"code\": 503, \"message\": \"Service Unavailable\"}}"));

        assertThatThrownBy(() -> googleBookService.searchBooks("Effective Java", 10, 0))
                .isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
    }
}