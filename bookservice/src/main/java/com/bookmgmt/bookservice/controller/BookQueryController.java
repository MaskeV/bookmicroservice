package com.bookmgmt.bookservice.controller;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookmgmt.bookservice.dto.FilterOptions;
import com.bookmgmt.bookservice.dto.PageResponse;
import com.bookmgmt.bookservice.entity.Book;
import com.bookmgmt.bookservice.service.BookQueryService;


// GET /api/books/search   -> one page of books (search + filters + sorting + paging)
// GET /api/books/filters  -> distinct categories / languages / publishers for the dropdowns
// NOTE: delete the old searchBook() method from BookController, otherwise "/search" is mapped twice.
@RestController
@RequestMapping("/api/books")
public class BookQueryController {

	private final BookQueryService queryService;

	public BookQueryController(BookQueryService queryService) {
		this.queryService = queryService;
	}

	@GetMapping("/search")
	public PageResponse<Book> search(@RequestParam(required = false) String query,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) String language,
			@RequestParam(required = false) String publisher,
			@RequestParam(required = false) Boolean available,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String sort,
			@RequestParam(defaultValue = "asc") String dir) {
		return queryService.search(query, category, language, publisher, available, page, size, sort, dir);
	}

	@GetMapping("/filters")
	public FilterOptions filters() {
		return queryService.filterOptions();
	}
}
