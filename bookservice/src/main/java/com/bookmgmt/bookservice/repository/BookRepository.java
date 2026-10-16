package com.bookmgmt.bookservice.repository;



import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bookmgmt.bookservice.entity.Book;

import jakarta.persistence.LockModeType;

// JpaSpecificationExecutor adds findAll(Specification, Pageable) used by the paged search
public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

	@Query("""
	        SELECT b FROM Book b
	        WHERE (:category  IS NULL OR b.category  = :category)
	          AND (:language  IS NULL OR b.language  = :language)
	          AND (:publisher IS NULL OR b.publisher = :publisher)
	          AND (:available IS NULL OR b.available = :available)
	        """)
	List<Book> findByFilters(@Param("category") String category,
	                         @Param("language") String language,
	                         @Param("publisher") String publisher,
	                         @Param("available") Boolean available);

	@Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL AND b.category <> '' ORDER BY b.category")
	List<String> findDistinctCategories();

	@Query("SELECT DISTINCT b.language FROM Book b WHERE b.language IS NOT NULL AND b.language <> '' ORDER BY b.language")
	List<String> findDistinctLanguages();

	@Query("SELECT DISTINCT b.publisher FROM Book b WHERE b.publisher IS NOT NULL AND b.publisher <> '' ORDER BY b.publisher")
	List<String> findDistinctPublishers();
	

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT b FROM Book b WHERE b.id = :id")
	Optional<Book> findByIdForUpdate(@Param("id") Integer id);

}
