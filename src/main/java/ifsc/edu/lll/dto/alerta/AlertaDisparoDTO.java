package ifsc.edu.lll.dto.alerta;

import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.model.Alerta;
import io.swagger.v3.oas.annotations.media.Schema;

/** Dados enviados ao cliente via SSE quando as condições do alerta são atingidas */
@Schema(description = "Evento SSE disparado quando as condições climáticas do alerta são atingidas")
public record AlertaDisparoDTO(
        @Schema(description = "ID do alerta que foi disparado", example = "1")
        Long alertaId,

        @Schema(description = "Nome da cidade monitorada", example = "Florianópolis")
        String cidade,

        @Schema(description = "Código do país", example = "BR")
        String pais,

        @Schema(description = "Dados climáticos que causaram o disparo")
        DadosClimaticos previsao
) {
    public static AlertaDisparoDTO de(Alerta alerta, DadosClimaticos previsao) {
        return new AlertaDisparoDTO(alerta.getId(), alerta.getCidade(), alerta.getPais(), previsao);
    }
}
