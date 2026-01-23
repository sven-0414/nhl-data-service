package se.sven.nhldataservice.exception;

/**
 * Thrown when a user is not found by ID or username.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
