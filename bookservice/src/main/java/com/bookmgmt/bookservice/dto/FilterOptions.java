package com.bookmgmt.bookservice.dto;



import java.util.List;


public record FilterOptions(List<String> categories, List<String> languages, List<String> publishers) {
}