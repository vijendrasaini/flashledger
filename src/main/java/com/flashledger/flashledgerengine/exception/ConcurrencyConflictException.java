package com.flashledger.flashledgerengine.exception;

public class ConcurrencyConflictException extends RuntimeException{
    public ConcurrencyConflictException(String message) {
        super(message);
    }
}
