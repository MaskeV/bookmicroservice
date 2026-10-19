package com.bookmgmt.bookservice.service;

import java.util.Set;

import com.bookmgmt.bookservice.dto.FilterOptions;
import com.bookmgmt.bookservice.dto.PageResponse;
import com.bookmgmt.bookservice.entity.Book;
import com.bookmgmt.bookservice.repository.BookRepository;
import com.bookmgmt.bookservice.repository.BookSpecifications;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;



// Read side of the books API: paged search + dropdown values
@Service
public class BookQueryService {

    // only these columns may be used in ?sort= (anything else is ignored)
    private static final Set<String> SORTABLE = Set.of("id", "title", "author", "category", "language",
            "publicationYear", "quantity", "available");
    private static final int MAX_PAGE_SIZE = 100;

    private final BookRepository bookRepository;

    public BookQueryService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public PageResponse<Book> search(String query, String category, String language, String publisher,
                                     Boolean available, int page, int size, String sort, String dir) {

        Sort order = Sort.unsorted();
        if (sort != null && SORTABLE.contains(sort)) {
            order = Sort.by("desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC, sort);
        }
        // id as the last sort key keeps the order stable between pages
        order = order.and(Sort.by(Sort.Direction.ASC, "id"));

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), order);
        return PageResponse
                .of(bookRepository.findAll(BookSpecifications.search(query, category, language, publisher, available), pageable));
    }

    public FilterOptions filterOptions() {
        return new FilterOptions(bookRepository.findDistinctCategories(), bookRepository.findDistinctLanguages(),
                bookRepository.findDistinctPublishers());
    }
}