package se.sven.nhldataservice.exception;

/**
 * Thrown when attempting to create a user with a username that already exists.
 */
public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}