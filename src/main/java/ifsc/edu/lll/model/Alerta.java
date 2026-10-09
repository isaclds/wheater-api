package ifsc.edu.lll.model;

import ifsc.edu.lll.dto.shared.DadosClimaticos;

public record Alerta(
        Long id,
        Long canalId,
        String pais,
        String cidade,
        Double temperaturaMaxima,
        Double temperaturaMinima,
        Double precipitacao,
        boolean ativo
) {
    public boolean foiAtingidoPor(DadosClimaticos previsao) {
        if (temperaturaMaxima != null && previsao.temperaturaMaxima() > temperaturaMaxima) return true;
        if (temperaturaMinima != null && previsao.temperaturaMinima() < temperaturaMinima) return true;
        if (precipitacao != null && previsao.precipitacao() > precipitacao) return true;
        return false;
    }
}
