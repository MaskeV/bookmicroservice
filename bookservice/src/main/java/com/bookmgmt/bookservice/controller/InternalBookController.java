package com.bookmgmt.bookservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bookmgmt.bookservice.dto.BookSnapshot;
import com.bookmgmt.bookservice.service.BookService;

@RestController
@RequestMapping("/internal/books")
public class InternalBookController {

    private final BookService bookService;

    public InternalBookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/{id}/reserve")
    public BookSnapshot reserve(@PathVariable Integer id) {
        return bookService.reserve(id);
    }

    @PostMapping("/{id}/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@PathVariable Integer id) {
        bookService.release(id);
    }
}