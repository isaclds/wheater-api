package ifsc.edu.lll.dto.alerta;

import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.model.Alerta;

// Dados enviados ao cliente via SSE quando as condições do alerta são atingidas
public record AlertaDisparoDTO(
        Long alertaId,
        String cidade,
        String pais,
        DadosClimaticos previsao
) {
    public static AlertaDisparoDTO de(Alerta alerta, DadosClimaticos previsao) {
        return new AlertaDisparoDTO(alerta.id(), alerta.cidade(), alerta.pais(), previsao);
    }
}
