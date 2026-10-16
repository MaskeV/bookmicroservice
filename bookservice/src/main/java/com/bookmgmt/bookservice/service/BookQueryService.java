package com.bookmgmt.bookservice.service;



import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.bookmgmt.bookservice.document.BookDocument;
import com.bookmgmt.bookservice.dto.FilterOptions;
import com.bookmgmt.bookservice.dto.PageResponse;
import com.bookmgmt.bookservice.entity.Book;
import com.bookmgmt.bookservice.repository.BookRepository;



// Read side of the books API: paged search (Elasticsearch) + dropdown values (MySQL)
@Service
public class BookQueryService {

	// ?sort= value  ->  Elasticsearch field to sort on (anything else is ignored).
	// text fields can't be sorted directly, so title/author use their ".keyword" sub-field.
	private static final Map<String, String> SORT_FIELDS = Map.of(
			"id", "bookId",
			"title", "title.keyword",
			"author", "author.keyword",
			"category", "category",
			"language", "language",
			"publicationYear", "publicationYear",
			"quantity", "quantity",
			"available", "available");
	private static final int MAX_PAGE_SIZE = 100;

	private final BookRepository bookRepository;
	private final ElasticsearchOperations elasticsearchOperations;

	public BookQueryService(BookRepository bookRepository, ElasticsearchOperations elasticsearchOperations) {
		this.bookRepository = bookRepository;
		this.elasticsearchOperations = elasticsearchOperations;
	}

	public PageResponse<Book> search(String query, String category, String language, String publisher,
			Boolean available, int page, int size, String sort, String dir) {

		Sort order = Sort.unsorted();
		String sortField = sort == null ? null : SORT_FIELDS.get(sort);
		if (sortField != null) {
			order = Sort.by("desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC, sortField);
		}
		// bookId as the last sort key keeps the order stable between pages
		order = order.and(Sort.by(Sort.Direction.ASC, "bookId"));

		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), order);

		NativeQuery nativeQuery = NativeQuery.builder()
				.withQuery(q -> q.bool(b -> {

					// free-text search: every word must appear in title, author or description
					if (hasText(query)) {
						for (String word : query.trim().toLowerCase().split("\\s+")) {
							String pattern = "*" + escapeWildcard(word) + "*";
							b.must(m -> m.bool(inner -> inner
									.should(s -> s.wildcard(w -> w.field("title").value(pattern)))
									.should(s -> s.wildcard(w -> w.field("author").value(pattern)))
									.should(s -> s.wildcard(w -> w.field("description").value(pattern)))
									.minimumShouldMatch("1")));
						}
					}

					// exact-match filters
					if (hasText(category))
						b.filter(f -> f.term(t -> t.field("category").value(category.trim())));
					if (hasText(language))
						b.filter(f -> f.term(t -> t.field("language").value(language.trim())));
					if (hasText(publisher))
						b.filter(f -> f.term(t -> t.field("publisher").value(publisher.trim())));
					if (available != null)
						b.filter(f -> f.term(t -> t.field("available").value(available.booleanValue())));

					return b;
				}))
				.withPageable(pageable)
				.withTrackTotalHits(true) // exact total even above 10,000 hits
				.build();

		SearchHits<BookDocument> hits = elasticsearchOperations.search(nativeQuery, BookDocument.class);
		List<Book> books = hits.getSearchHits().stream().map(SearchHit::getContent).map(this::toBook).toList();

		return PageResponse.of(new PageImpl<>(books, pageable, hits.getTotalHits()));
	}

	// filter dropdown values still come from MySQL (small DISTINCT queries)
	public FilterOptions filterOptions() {
		return new FilterOptions(bookRepository.findDistinctCategories(), bookRepository.findDistinctLanguages(),
				bookRepository.findDistinctPublishers());
	}

	private Book toBook(BookDocument d) {
		Book b = new Book();
		b.setId(d.getId() != null ?  Integer.valueOf(d.getId()):0);
		b.setTitle(d.getTitle());
		b.setAuthor(d.getAuthor());
		b.setDescription(d.getDescription());
		b.setCategory(d.getCategory());
		b.setPublisher(d.getPublisher());
		b.setPublicationYear(d.getPublicationYear());
		b.setLanguage(d.getLanguage());
		b.setQuantity(d.getQuantity());
		b.setAvailable(d.getAvailable());
		return b;
	}

	// a user typing * or ? must not act as a wildcard
	private static String escapeWildcard(String s) {
		return s.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");
	}

	private static boolean hasText(String s) {
		return s != null && !s.isBlank();
	}
}