package org.example.exception;

public class SellerNotFoundException extends RuntimeException {
    public SellerNotFoundException(Long id) {
        super("Seller not found with id: " + id);
    }
}