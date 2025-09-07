package com.charbel.ecommerce.exception;

public class UnauthorizedReviewAccessException extends RuntimeException {
    public UnauthorizedReviewAccessException(String message) {
        super(message);
    }
}