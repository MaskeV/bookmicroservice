package com.bookmgmt.bookservice.exception;


public class OutOfStockException extends RuntimeException {
    public OutOfStockException(int id) {
        super("No copies available for book id: " + id);
    }
}