package se.sven.nhldataservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import se.sven.nhldataservice.dto.ErrorResponse;
import se.sven.nhldataservice.exception.InvalidDateFormatException;

import java.time.LocalDateTime;

/**
 * Centralized exception handling with consistent error responses.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidDateFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDateFormat(InvalidDateFormatException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        e.getMessage(),
                        LocalDateTime.now()  // <-- Ta bort .format()
                ));
    }
}