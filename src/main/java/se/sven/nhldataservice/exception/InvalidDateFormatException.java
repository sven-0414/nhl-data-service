package se.sven.nhldataservice.exception;

/**
 * Thrown when date parsing fails in API requests.
 */
public class InvalidDateFormatException extends RuntimeException {
    public InvalidDateFormatException(String message) {
        super(message);
    }
}