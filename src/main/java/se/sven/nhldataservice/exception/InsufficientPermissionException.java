package se.sven.nhldataservice.exception;

/**
 * Thrown when a user lacks sufficient permissions for an operation.
 * For example, when a regular user tries to access admin functionality
 * or modify another user's data.
 */
public class InsufficientPermissionException extends RuntimeException {
    public InsufficientPermissionException(String message) {
        super(message);
    }
}
