package com.bookmgmt.bookservice.dto;


public record BookSnapshot(Integer id, String title, String author, String category) {
}