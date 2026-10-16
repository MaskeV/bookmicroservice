package com.bookmgmt.bookservice.dto;

import java.util.List;

import org.springframework.data.domain.Page;

// Our own JSON shape for one page of results (page is 0-based)
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	public static <T> PageResponse<T> of(Page<T> p) {
		return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
	}
}