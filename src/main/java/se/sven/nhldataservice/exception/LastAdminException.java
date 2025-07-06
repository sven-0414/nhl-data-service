package se.sven.nhldataservice.exception;
/**
 * Thrown when attempting to delete or demote the last admin user.
 * Prevents the system from being left without any administrators.
 */
public class LastAdminException extends RuntimeException {
    public LastAdminException(String message) {
        super(message);
    }
}