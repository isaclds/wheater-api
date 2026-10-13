package ifsc.edu.lll.dto.viagem;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Previsão do roteiro, uma entrada por parada na mesma ordem da requisição")
public record ViagemResponseDTO(List<ParadaResponseDTO> paradas) {}
