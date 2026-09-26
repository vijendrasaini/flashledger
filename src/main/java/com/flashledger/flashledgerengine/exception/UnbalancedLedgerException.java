package com.flashledger.flashledgerengine.exception;

public class UnbalancedLedgerException extends RuntimeException {
    public UnbalancedLedgerException(String message) {
        super(message);
    }
}
