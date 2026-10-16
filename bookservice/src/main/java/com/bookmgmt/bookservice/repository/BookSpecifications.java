package com.bookmgmt.bookservice.repository;



import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.bookmgmt.bookservice.entity.Book;

import jakarta.persistence.criteria.Predicate;


public final class BookSpecifications {

	private BookSpecifications() {
	}

	public static Specification<Book> search(String query, String category, String language, String publisher,
			Boolean available) {
		return (root, cq, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (query != null && !query.isBlank()) {
				for (String word : query.trim().toLowerCase().split("\\s+")) {
					// escape LIKE wildcards typed by the user
					String like = "%" + word.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
					predicates.add(cb.or(
							cb.like(cb.lower(root.<String>get("title")), like, '\\'),
							cb.like(cb.lower(root.<String>get("author")), like, '\\'),
							cb.like(cb.lower(root.<String>get("description")), like, '\\')));
				}
			}
			if (hasText(category))
				predicates.add(cb.equal(root.get("category"), category.trim()));
			if (hasText(language))
				predicates.add(cb.equal(root.get("language"), language.trim()));
			if (hasText(publisher))
				predicates.add(cb.equal(root.get("publisher"), publisher.trim()));
			if (available != null)
				predicates.add(cb.equal(root.get("available"), available));

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	private static boolean hasText(String s) {
		return s != null && !s.isBlank();
	}
}