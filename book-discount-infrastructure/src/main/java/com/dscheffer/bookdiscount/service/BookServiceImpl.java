package com.dscheffer.bookdiscount.service;

import com.dscheffer.bookdiscount.dto.Book;
import com.dscheffer.bookdiscount.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(b -> new Book(b.getId(), b.getName(), b.getPrice()))
                .toList();
    }
}
