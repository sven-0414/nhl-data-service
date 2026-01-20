package se.sven.nhldataservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for user registration.
 * Creates a new user account with USER role by default.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String error;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}