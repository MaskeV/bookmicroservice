package com.bookmgmt.bookservice.service;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.bookmgmt.bookservice.dto.BookSnapshot;

import com.bookmgmt.bookservice.exception.OutOfStockException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;


import com.bookmgmt.bookservice.document.BookDocument;
import com.bookmgmt.bookservice.entity.Book;
import com.bookmgmt.bookservice.exception.BookNotFoundException;
import com.bookmgmt.bookservice.repository.BookRepository;
import com.bookmgmt.bookservice.searchRepository.BookSearchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class BookService {
	
	private final BookRepository bookRepository;
	private final BookSearchRepository bookSearchRepository;
	private final ElasticsearchOperations elasticsearchOperations;
	
	


	public BookService(BookRepository bookRepository, BookSearchRepository bookSearchRepository,
			ElasticsearchOperations elasticsearchOperations) {
		super();
		this.bookRepository = bookRepository;
		this.bookSearchRepository = bookSearchRepository;
		this.elasticsearchOperations = elasticsearchOperations;
	}

	public Book findBookById(int id) {
		
		 return bookRepository.findById(id)
		            .orElseThrow(() -> new BookNotFoundException(id));
		
	}

	public Book addBook(Book book) {
		book.setCreatedAt(LocalDateTime.now());
		
	  Book savedBook =	bookRepository.save(book);
	  
	 bookSearchRepository.save(mapToBookDocument(savedBook));
	 
	 return savedBook;

	}
	
	public void deleteBook(int id)  {
		
		if (!bookRepository.existsById(id)) {
	        throw new BookNotFoundException(id);
	    }
		bookRepository.deleteById(id);
		bookSearchRepository.deleteById(String.valueOf(id));
	}

	public Book updateBook(int id, Book updatedBook) {
		
		// TODO Auto-generated method stub
		
        Book book = findBookById(id);
		
		
		book.setTitle(updatedBook.getTitle());
		book.setAuthor(updatedBook.getAuthor());
		book.setDescription(updatedBook.getDescription());
		book.setCategory(updatedBook.getCategory());
		book.setPublisher(updatedBook.getPublisher());
		book.setPublicationYear(updatedBook.getPublicationYear());
		book.setLanguage(updatedBook.getLanguage());
		book.setQuantity(updatedBook.getQuantity());
		book.setAvailable(updatedBook.getAvailable());
		book.setUpdatedAt(LocalDateTime.now());
		
	 Book updated =	bookRepository.save(book);
	 bookSearchRepository.save(mapToBookDocument(updated));
	 

		
		return updated;
	}
	
	
	public List<Book> getAllBooks(){
		
		List<Book> books = bookRepository.findAll();
		
		if(books==null) {
			System.out.println("No books Available");
		
		}
		
		return books;
	}
	
	private BookDocument mapToBookDocument(Book book) {
		BookDocument bookDocument = new BookDocument();
		bookDocument.setId(String.valueOf(book.getId()));
		bookDocument.setBookId(book.getId());
		bookDocument.setTitle(book.getTitle());
	    bookDocument.setAuthor(book.getAuthor());
		bookDocument.setDescription(book.getDescription());
		bookDocument.setCategory(book.getCategory());
		bookDocument.setPublisher(book.getPublisher());
		bookDocument.setPublicationYear(book.getPublicationYear());
		bookDocument.setLanguage(book.getLanguage());
		bookDocument.setQuantity(book.getQuantity());
		bookDocument.setAvailable(book.getAvailable());
		return bookDocument;
	}

	public List<Book> addAllBooks(List<Book> books) {
		// TODO Auto-generated method stub
	
		for(Book book:books) {
			addBook(book);
		}
		
		return books;
		
	}

	public List<BookDocument> searchBooks(String query, String category, String language, String publisher,
			Boolean available) {
		// TODO Auto-generated method stub
		 NativeQuery nativeQuery = NativeQuery.builder()
			        .withQuery(q -> q.bool(b -> {

			            // 1) free-text search: every word must appear in title, author or description
			            if (query != null && !query.isBlank()) {
			                for (String word : query.trim().toLowerCase().split("\\s+")) {
			                    String pattern = "*" + word + "*";
			                    b.must(m -> m.bool(inner -> inner
			                        .should(s -> s.wildcard(w -> w.field("title").value(pattern)))
			                        .should(s -> s.wildcard(w -> w.field("author").value(pattern)))
			                        .should(s -> s.wildcard(w -> w.field("description").value(pattern)))
			                        .minimumShouldMatch("1")));
			                }
			            }

			            if (category != null && !category.isBlank())
			                b.filter(f -> f.term(t -> t.field("category").value(category.trim())));
			            if (language != null && !language.isBlank())
			                b.filter(f -> f.term(t -> t.field("language").value(language.trim())));
			            if (publisher != null && !publisher.isBlank())
			                b.filter(f -> f.term(t -> t.field("publisher").value(publisher.trim())));
			            if (available != null)
			                b.filter(f -> f.term(t -> t.field("available").value(available.booleanValue())));

			            return b;
			        }))
			        .withPageable(PageRequest.of(0, 1000))   // ES returns only 10 hits by default
			        .build();

			    return elasticsearchOperations.search(nativeQuery, BookDocument.class)
			            .stream()
			            .map(SearchHit::getContent)
			            .toList();
	}
	
	public List<Book> searchBooksFromDb(String query, String category, String language,
            String publisher, Boolean available) {

List<Book> books = bookRepository.findByFilters(
blankToNull(category), blankToNull(language), blankToNull(publisher), available);

if (query == null || query.isBlank()) {
return books;
}

String[] words = query.trim().toLowerCase().split("\\s+");

return books.stream()
.filter(b -> {
String text = (nz(b.getTitle()) + " " + nz(b.getAuthor()) + " " + nz(b.getDescription()))
.toLowerCase();
return Arrays.stream(words).allMatch(text::contains);   
})
.toList();
}

private String blankToNull(String s) {
return (s == null || s.isBlank()) ? null : s.trim();
}

private String nz(String s) {
return s == null ? "" : s;
}


	@Transactional
	public BookSnapshot reserve(int id) {
		Book book = findBookById(id);                       // 404 if the book doesn't exist
		if (bookRepository.reserveOne(id, LocalDateTime.now()) == 0)
			throw new OutOfStockException(id);              // 409: no copies left
		syncSearchIndex(id);
		return new BookSnapshot(book.getId(), book.getTitle(), book.getAuthor(), book.getCategory());
	}

	@Transactional
	public void release(int id) {
		if (bookRepository.releaseOne(id, LocalDateTime.now()) == 0)
			throw new BookNotFoundException(id);
		syncSearchIndex(id);
	}

	private void syncSearchIndex(int id) {
		bookRepository.findById(id).ifPresent(b -> bookSearchRepository.save(mapToBookDocument(b)));
	}
	
	
}