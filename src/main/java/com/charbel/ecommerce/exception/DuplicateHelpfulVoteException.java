package com.charbel.ecommerce.exception;

public class DuplicateHelpfulVoteException extends RuntimeException {
    public DuplicateHelpfulVoteException(String message) {
        super(message);
    }
}