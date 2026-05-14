package org.example.exception;

public class InvalidPeriodException extends RuntimeException {
    public InvalidPeriodException(String period) {
        super("Invalid period: " + period + ". Use day/month/quarter/year");
    }
}
