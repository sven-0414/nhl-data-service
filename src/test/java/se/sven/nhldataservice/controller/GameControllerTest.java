package se.sven.nhldataservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameControllerTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "not-a-date",
            "2024-13-01",
            "2024-02-30",
            "2024/06/30",     // Fel separator
            "24-06-30",       // Fel år-format
            "2024-6-30",      // Saknar leading zero
            "2024-06-",       // Saknar dag
            "abc-def-ghi",    // Bara bokstäver
            "2024--06--30",   // Dubbla bindestreck
            "",               // Tom sträng
            "2024-06-32"
    })
    void shouldThrowExceptionForInvalidDateFormats(String invalidDate) {
        // When & Then
        assertThatThrownBy(() -> {
            LocalDate.parse(invalidDate, DateTimeFormatter.ISO_LOCAL_DATE);
        }).isInstanceOf(DateTimeParseException.class);
    }

    @Test
    void shouldParseValidDateCorrectly() {
        // Given
        String validDate = "2024-06-30";

        // When & Then - ska INTE kasta exception
        LocalDate result = LocalDate.parse(validDate, DateTimeFormatter.ISO_LOCAL_DATE);
        assertThat(result).isEqualTo(LocalDate.of(2024, 6, 30));
    }
}