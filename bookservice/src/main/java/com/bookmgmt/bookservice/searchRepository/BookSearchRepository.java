package com.bookmgmt.bookservice.searchRepository;



import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.bookmgmt.bookservice.document.BookDocument;



public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, String> {

	
	 List<BookDocument> findByTitleContainingOrAuthorContainingOrDescriptionContaining(String title, String author, String description);
	 
}