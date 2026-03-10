package com.example.demo.service;

import com.example.demo.db.Book;
import com.example.demo.google.GoogleBook;

import java.util.List;

public interface BookService {
    List<Book> getAllBooks();

    GoogleBook searchGoogleBooks(String query, Integer maxResults, Integer startIndex);

    Book addBookFrromGoogle(String googleBookId);
}
