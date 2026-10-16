package com.bookmgmt.bookservice.controller;



import java.util.List;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookmgmt.bookservice.entity.Book;
import com.bookmgmt.bookservice.service.BookService;


@RestController
@RequestMapping("/api/books")
public class BookController {
	
	private BookService bookService;
	
	

	public BookController(BookService bookService) {
		super();
		this.bookService = bookService;
	}

	@GetMapping("/{id}")
	public Book getBook(@PathVariable int id) {
		return bookService.findBookById(id);
	}
	
	@PostMapping
	public Book addBook(@RequestBody Book book) {
		
	 return	bookService.addBook(book);
	}
	
	@PostMapping("/addAll")
	public List<Book> addAllBooks(@RequestBody List<Book> books) {
	 return	bookService.addAllBooks(books);
	}
	
	@DeleteMapping("/{id}")
	public void deleteBook(@PathVariable int id) {
		bookService.deleteBook(id);
	}
	
	@PutMapping("/{id}")
	public Book updateBook(@PathVariable int id, @RequestBody Book book) {
		return bookService.updateBook(id,book);
	}

	
	@GetMapping
	public List<Book> getBooks(){
		return bookService.getAllBooks();
	}
	
	
}