package ifsc.edu.lll.dto.log;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record LogRequestDTO(
        @NotBlank Instant dataInicial,
        @NotBlank Instant dataFinal) {}