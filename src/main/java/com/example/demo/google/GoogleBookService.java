package com.example.demo.google;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

/**
 * Service for interacting with the Google Books API.
 * Handles searching for books and fetching a single book by its volume ID.
 *
 * Assumption: API key is required and must be configured in application.properties.
 */
@Service
@Slf4j
public class GoogleBookService {

    private final RestClient restClient;
    private final String apiKey;

    /**
     * Constructs GoogleBookService with base URL and API key from application properties.
     *
     * @param baseUrl the Google Books API base URL
     * @param apiKey  the Google Books API key
     */
    public GoogleBookService(@Value("${google.books.base-url:https://www.googleapis.com/books/v1}") String baseUrl,
                             @Value("${google.books.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Searches for books using the Google Books API.
     *
     * @param query      the search query string
     * @param maxResults maximum number of results to return, defaults to 10 if null
     * @param startIndex index of the first result, defaults to 0 if null
     * @return GoogleBook response containing matching book items
     * @throws HttpClientErrorException if a client error occurs (401, 403, 404, etc.)
     * @throws HttpServerErrorException if a server error occurs (500, 502, 503, 504)
     */
    public GoogleBook searchBooks(String query, Integer maxResults, Integer startIndex) {
        log.info("searchBooks - Searching for books with query: {}, maxResults: {}, startIndex: {}", query, maxResults,
                startIndex);
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/volumes")
                            .queryParam("q", query)
                            .queryParam("maxResults", maxResults != null ? maxResults : 10)
                            .queryParam("startIndex", startIndex != null ? startIndex : 0)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(GoogleBook.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("searchBooks - Invalid API key for Google Books API: {}", e.getMessage());
            throw e;
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("searchBooks - Access forbidden to Google Books API: {}", e.getMessage());
            throw e;
        } catch (HttpClientErrorException.NotFound e) {
            log.error("searchBooks - Google Books API endpoint not found: {}", e.getMessage());
            throw e;
        } catch (HttpClientErrorException e) {
            log.error("searchBooks - Client error[{}]: {}", e.getStatusCode(), e.getMessage());
            throw e;
        } catch (HttpServerErrorException e) {
            log.error("searchBooks - Google API server error[{}]: {}", e.getStatusCode(), e.getMessage());
            throw e;
        }
    }

    /**
     * Fetches a single book from Google Books API by its volume ID.
     *
     * @param googleBookId the Google Books volume ID
     * @return the matching GoogleBook.Item, or null if not found
     * @throws HttpClientErrorException if a client error occurs (401, 403, 404, etc.)
     * @throws HttpServerErrorException if a server error occurs (500, 502, 503, 504)
     */
    public GoogleBook.Item getBookById(String googleBookId) {
        log.info("getBookById - Getting book by id: {}", googleBookId);
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/volumes/{id}")
                            .queryParam("key", apiKey)
                            .build(googleBookId))
                    .retrieve()
                    .body(GoogleBook.Item.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("getBookById - Invalid API key for Google Books API: {}", e.getMessage());
            throw e;
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("getBookById - Access forbidden to Google Books API: {}", e.getMessage());
            throw e;
        } catch (HttpClientErrorException.NotFound e) {
            log.error("getBookById - Google Books API endpoint not found: {}", e.getMessage());
            throw e;
        } catch (HttpClientErrorException e) {
            log.error("getBookById - Client error[{}]: {}", e.getStatusCode(), e.getMessage());
            throw e;
        } catch (HttpServerErrorException e) {
            log.error("getBookById - Google API server error[{}]: {}", e.getStatusCode(), e.getMessage());
            throw e;
        }
    }
}