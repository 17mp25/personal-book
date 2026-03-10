package com.example.demo.mapper;

import com.example.demo.db.Book;
import com.example.demo.google.GoogleBook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {
    @Mapping(
            target = "author",
            expression = "java(item.volumeInfo() != null && item.volumeInfo().authors() != null && !item.volumeInfo().authors().isEmpty() ? item.volumeInfo().authors().get(0) : null)"
    )
    @Mapping(
            target = "pageCount",
            source = "volumeInfo.pageCount"
    )
    @Mapping(
            target = "title",
            source = "volumeInfo.title"
    )
    @Mapping(
            target = "id",
            source = "id"
    )
    Book toBook(GoogleBook.Item item);
}
