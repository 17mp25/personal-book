package com.example.demo.repository;

import com.example.demo.db.Book;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing Book entities.
 * Extends JpaRepository to provide standard CRUD operations.
 *
 * Assumption: Book ID is the Google Books volume ID (String type).
 */
public interface BookRepository extends JpaRepository<Book, String> {
}