package ifsc.edu.lll.dto.viagem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Roteiro de viagem: lista de paradas em ordem")
public record ViagemRequestDTO(

        @NotEmpty(message = "informe ao menos uma parada")
        @Size(max = 10, message = "o roteiro pode ter no máximo 10 paradas")
        @Valid
        @Schema(description = "Paradas do roteiro, na ordem da viagem", requiredMode = Schema.RequiredMode.REQUIRED)
        List<ParadaRequestDTO> paradas
) {}
