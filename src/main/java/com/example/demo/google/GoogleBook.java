package com.example.demo.google;

import java.util.List;

/**
 * Represents the response from the Google Books API.
 * Maps the JSON response to Java records.
 *
 * Assumption: Fields not present in the API response will be null.
 */
public record GoogleBook(
        String kind,
        int totalItems,
        List<Item> items
) {

    /**
     * Represents a single book item in the Google Books API response.
     */
    public record Item(
            String id,
            String selfLink,
            VolumeInfo volumeInfo,
            SearchInfo searchInfo
    ) {}

    /**
     * Represents the search info snippet returned by Google Books API.
     */
    public record SearchInfo(
            String textSnippet
    ) {}

    /**
     * Represents the volume info of a book returned by Google Books API.
     * Contains core book metadata such as title, authors, and page count.
     */
    public record VolumeInfo(
            String title,
            List<String> authors,
            String publishedDate,
            String publisher,
            Integer pageCount,
            String printType,
            String maturityRating,
            List<String> categories,
            String language,
            String previewLink,
            String infoLink
    ) {}
}