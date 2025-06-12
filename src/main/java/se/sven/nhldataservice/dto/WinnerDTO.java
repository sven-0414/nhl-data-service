package se.sven.nhldataservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class WinnerDTO {
    private List<Integer> periods;
    private Integer gameOutcome;
}
