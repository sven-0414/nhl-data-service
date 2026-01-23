package se.sven.nhldataservice.exception;

/**
 * Thrown when application fails to initialize properly during startup.
 * This is typically thrown by data loaders or configuration components.
 */
public class ApplicationInitializationException extends RuntimeException {
    @SuppressWarnings("unused")
    public ApplicationInitializationException(String message) {
        super(message);
    }
    public ApplicationInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}