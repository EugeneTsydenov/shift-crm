package org.example.exception;

public class NoTransactionsFoundException extends RuntimeException {
    public NoTransactionsFoundException(Long sellerId) {
        super("No transactions found for seller with id: " + sellerId);
    }

    public NoTransactionsFoundException(String period) {
        super("No transactions found for period: " + period);
    }
}
