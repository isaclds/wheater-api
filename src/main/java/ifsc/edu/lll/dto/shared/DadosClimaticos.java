package ifsc.edu.lll.dto.shared;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dados climáticos de uma localidade para uma data específica")
public record DadosClimaticos(
        @Schema(description = "Data dos dados climáticos", example = "2026-10-09")
        LocalDate data,

        @Schema(description = "Temperatura média do dia em °C", example = "22.5")
        Double temperaturaMedia,

        @Schema(description = "Temperatura máxima do dia em °C", example = "28.0")
        Double temperaturaMaxima,

        @Schema(description = "Temperatura mínima do dia em °C", example = "17.0")
        Double temperaturaMinima,

        @Schema(description = "Precipitação acumulada no dia em mm", example = "3.2")
        Double precipitacao,

        @Schema(description = "Umidade relativa do ar em %", example = "78.0")
        Double umidadeRelativa,

        @Schema(description = "Velocidade do vento em m/s", example = "5.3")
        Double velocidadeVento,

        @Schema(description = "Condição climática descritiva", example = "CEU_LIMPO")
        String condicaoClima,

        @Schema(description = "Fonte dos dados (OPEN_METEO | NASA_POWER)", example = "OPEN_METEO")
        String fonte
) {}
