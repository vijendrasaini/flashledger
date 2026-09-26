package com.flashledger.flashledgerengine.exception;

public class ProductOutOfStockException extends RuntimeException{
    public ProductOutOfStockException(String message) {
        super(message);
    }
}
