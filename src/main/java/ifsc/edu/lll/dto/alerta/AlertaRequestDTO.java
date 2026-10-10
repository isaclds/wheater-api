package ifsc.edu.lll.dto.alerta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Corpo da requisição para cadastrar um novo alerta climático */
@Schema(description = "Dados para cadastro de um novo alerta climático")
public record AlertaRequestDTO(

        @NotNull(message = "canalId é obrigatório")
        @Positive(message = "canalId deve ser um número positivo")
        @Schema(description = "Identificador do canal SSE do cliente", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
        Long canalId,

        @NotBlank(message = "pais é obrigatório")
        @Schema(description = "País da localidade monitorada (nome ou código ISO)", example = "Brasil", requiredMode = Schema.RequiredMode.REQUIRED)
        String pais,

        @NotBlank(message = "cidade é obrigatória")
        @Schema(description = "Nome da cidade a monitorar", example = "Florianópolis", requiredMode = Schema.RequiredMode.REQUIRED)
        String cidade,

        @Schema(description = "Temperatura máxima em °C; alerta dispara se a previsão ultrapassar esse valor", example = "35.0")
        Double temperaturaMaxima,

        @Schema(description = "Temperatura mínima em °C; alerta dispara se a previsão cair abaixo desse valor", example = "5.0")
        Double temperaturaMinima,

        @Schema(description = "Precipitação em mm; alerta dispara se a previsão ultrapassar esse valor", example = "20.0")
        Double precipitacao
) {}
