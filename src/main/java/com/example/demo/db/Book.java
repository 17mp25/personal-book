package com.example.demo.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a book stored in the local database.
 * Mapped from Google Books API response via BookMapper.
 *
 * Assumption: Only the first author is stored when a book has multiple authors.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    /**
     * Google Books API volume ID, used as the primary key.
     */
    @Id
    @NotBlank(message = "Book ID cannot be blank")
    private String id;

    /** Title of the book. */
    @NotBlank(message = "Title cannot be blank")
    private String title;

    /** First author of the book. */
    @NotBlank(message = "Author cannot be blank")
    private String author;

    /** Total number of pages in the book. */
    private Integer pageCount;

    /**
     * Constructs a Book without pageCount.
     *
     * @param id     Google Books volume ID
     * @param title  title of the book
     * @param author first author of the book
     */
    public Book(String id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
    }
}