package com.bookmgmt.bookservice.searchRepository;





import java.util.List;

import com.bookmgmt.bookservice.document.BookDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, String> {


	List<BookDocument> findByTitleContainingOrAuthorContainingOrDescriptionContaining(String title, String author, String description);

}