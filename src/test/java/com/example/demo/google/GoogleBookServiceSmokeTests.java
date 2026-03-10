package com.example.demo.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Smoke tests: Makes real calls to Google Books API.
 * Requires a valid API key in application.properties to run.
 * Excluded from mvn test by default - run manually in IntelliJ.
 *
 * Assumption: These tests require network access and a valid Google Books API key.
 */
@SpringBootTest
@Tag("smoke")
class GoogleBookServiceSmokeTests {

    @Autowired
    private GoogleBookService googleBookService;

    /**
     * Verifies that a real search call to Google Books API returns structured results.
     */
    @Test
    void search_effectiveJava_returnsStructuredResults() {
        GoogleBook result = googleBookService.searchBooks("effective+java", 5, 0);
        assertThat(result).isNotNull();
        assertThat(result.kind()).isEqualTo("books#volumes");
        assertThat(result.totalItems()).isGreaterThan(0);
        assertThat(result.items()).isNotNull();
        assertThat(result.items()).isNotEmpty();

        GoogleBook.Item first = result.items().get(0);
        assertThat(first.id()).isNotBlank();
        assertThat(first.selfLink()).isNotBlank();
        assertThat(first.volumeInfo()).isNotNull();
        assertThat(first.volumeInfo().title()).isEqualTo("Effective Java");
        assertThat(first.volumeInfo().authors()).isNotNull();
        assertThat(first.volumeInfo().language()).isNotNull();
        assertThat(first.searchInfo()).isNotNull();
        assertThat(first.searchInfo().textSnippet()).isNotNull();
    }

    /**
     * Verifies that an invalid API key results in 400 or 401 from Google Books API.
     * Confirms that API key is actually being used in requests.
     */
    @Test
    void search_withInvalidApiKey_returns400Or401() {
        GoogleBookService invalidKeyService = new GoogleBookService(
                "https://www.googleapis.com/books/v1", "INVALID_KEY");

        assertThatThrownBy(() -> invalidKeyService.searchBooks("effective java", 5, 0))
                .isInstanceOf(HttpClientErrorException.class)
                .satisfies(ex -> {
                    HttpClientErrorException httpEx = (HttpClientErrorException) ex;
                    assertThat(httpEx.getStatusCode().value()).isIn(400, 401, 403);
                });
    }

    /**
     * Verifies that a real getBookById call returns the correct book.
     */
    @Test
    void getBookById_effectiveJava_returnsCorrectBook() {
        GoogleBook.Item result = googleBookService.getBookById("ka2VUBqHiWkC");
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("ka2VUBqHiWkC");
        assertThat(result.volumeInfo()).isNotNull();
        assertThat(result.volumeInfo().title()).isEqualTo("Effective Java");
        assertThat(result.volumeInfo().authors()).contains("Joshua Bloch");
    }
}