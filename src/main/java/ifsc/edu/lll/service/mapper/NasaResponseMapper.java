package ifsc.edu.lll.service.mapper;

import ifsc.edu.lll.dto.nasa.NasaPowerResponse;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class NasaResponseMapper {

    private static final double VALOR_AUSENTE = -999.0;
    private static final DateTimeFormatter FORMATO_NASA = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<DadosClimaticos> paraDadosClimaticos(NasaPowerResponse resposta) {
        Map<String, Map<String, Double>> p = resposta.properties().parameter();

        return p.get("T2M").keySet().stream()
                .sorted()
                .map(dataStr -> {
                    Double precipitacaoDia = tratar(valorDe(p, "PRECTOTCORR", dataStr));
                    return new DadosClimaticos(
                            LocalDate.parse(dataStr, FORMATO_NASA),
                            tratar(valorDe(p, "T2M", dataStr)),
                            null,
                            null,
                            precipitacaoDia,
                            tratar(valorDe(p, "RH2M", dataStr)),
                            tratar(valorDe(p, "WS2M", dataStr)),
                            ClassificadorClima.porPrecipitacao(precipitacaoDia),
                            "NASA_POWER"
                    );
                })
                .toList();
    }

    private Double valorDe(Map<String, Map<String, Double>> p, String parametro, String data) {
        Map<String, Double> valores = p.get(parametro);
        return valores != null ? valores.get(data) : null;
    }

    private Double tratar(Double valor) {
        return (valor != null && valor == VALOR_AUSENTE) ? null : valor;
    }
}