package ifsc.edu.lll.dto.viagem;

import ifsc.edu.lll.dto.shared.DadosClimaticos;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Parada do roteiro com o clima de cada dia")
public record ParadaResponseDTO(
        String pais,
        String cidade,
        LocalDate dataInicio,
        LocalDate dataFim,
        @Schema(description = "Clima diário do período, em ordem de data")
        List<DadosClimaticos> dias
) {}
