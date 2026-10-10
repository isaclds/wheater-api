package ifsc.edu.lll.dto.alerta;

import ifsc.edu.lll.model.Alerta;
import io.swagger.v3.oas.annotations.media.Schema;

/** DTO de saída para evitar expor a entidade JPA diretamente */
@Schema(description = "Alerta climático cadastrado")
public record AlertaResponseDTO(
        @Schema(description = "Identificador único do alerta", example = "1")
        Long id,

        @Schema(description = "Identificador do canal SSE que receberá os disparos", example = "42")
        Long canalId,

        @Schema(description = "Código ISO do país", example = "BR")
        String pais,

        @Schema(description = "Nome da cidade monitorada", example = "Florianópolis")
        String cidade,

        @Schema(description = "Temperatura máxima em °C que dispara o alerta", example = "35.0")
        Double temperaturaMaxima,

        @Schema(description = "Temperatura mínima em °C que dispara o alerta", example = "5.0")
        Double temperaturaMinima,

        @Schema(description = "Precipitação em mm que dispara o alerta", example = "20.0")
        Double precipitacao,

        @Schema(description = "Indica se o alerta está ativo", example = "true")
        boolean ativo
) {
    public static AlertaResponseDTO de(Alerta alerta) {
        return new AlertaResponseDTO(
                alerta.getId(),
                alerta.getCanalId(),
                alerta.getPais(),
                alerta.getCidade(),
                alerta.getTemperaturaMaxima(),
                alerta.getTemperaturaMinima(),
                alerta.getPrecipitacao(),
                alerta.isAtivo()
        );
    }
}
