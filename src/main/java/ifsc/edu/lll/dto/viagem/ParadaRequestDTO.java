package ifsc.edu.lll.dto.viagem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Schema(description = "Parada do roteiro: cidade e período de permanência")
public record ParadaRequestDTO(

        @NotBlank(message = "pais é obrigatório")
        @Schema(description = "País da parada (nome ou código ISO)", example = "Brasil", requiredMode = Schema.RequiredMode.REQUIRED)
        String pais,

        @NotBlank(message = "cidade é obrigatória")
        @Schema(description = "Cidade da parada", example = "Florianópolis", requiredMode = Schema.RequiredMode.REQUIRED)
        String cidade,

        @NotNull(message = "dataInicio é obrigatória")
        @Schema(description = "Primeiro dia na cidade (yyyy-MM-dd)", example = "2026-10-15", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate dataInicio,

        @NotNull(message = "dataFim é obrigatória")
        @Schema(description = "Último dia na cidade (yyyy-MM-dd)", example = "2026-10-18", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate dataFim
) {

    /** Máximo de dias por parada*/
    public static final int MAX_DIAS = 16;

    @AssertTrue(message = "dataFim deve ser igual ou posterior a dataInicio")
    @Schema(hidden = true)
    public boolean isPeriodoCoerente() {
        return dataInicio == null || dataFim == null || !dataFim.isBefore(dataInicio);
    }

    @AssertTrue(message = "cada parada pode ter no máximo " + MAX_DIAS + " dias")
    @Schema(hidden = true)
    public boolean isPeriodoDentroDoLimite() {
        return dataInicio == null || dataFim == null
                || dataFim.isBefore(dataInicio)
                || ChronoUnit.DAYS.between(dataInicio, dataFim) < MAX_DIAS;
    }
}
