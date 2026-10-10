package ifsc.edu.lll.service.mapper;

import ifsc.edu.lll.dto.nasa.NasaGeometry;
import ifsc.edu.lll.dto.nasa.NasaPowerResponse;
import ifsc.edu.lll.dto.nasa.NasaProperties;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NasaResponseMapper — mapeamento NasaPowerResponse → DadosClimaticos")
class NasaResponseMapperTest {

    private NasaResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NasaResponseMapper();
    }

    @Test
    @DisplayName("Deve mapear parâmetros da NASA para DadosClimaticos tratando valor sentinela -999.0")
    void deveMapearDadosNasaTratandoValoresSentinela() {
        Map<String, Map<String, Double>> parametros = Map.of(
                "T2M", Map.of("20250510", 22.5),
                "PRECTOTCORR", Map.of("20250510", 3.0),
                "RH2M", Map.of("20250510", 80.0),
                "WS2M", Map.of("20250510", -999.0) // valor sentinela ausente
        );

        NasaPowerResponse response = new NasaPowerResponse(
                "Feature",
                new NasaGeometry("Point", List.of(-48.54, -27.59)),
                new NasaProperties(parametros),
                Map.of(),
                List.of()
        );

        List<DadosClimaticos> resultado = mapper.paraDadosClimaticos(response);

        assertThat(resultado).hasSize(1);
        DadosClimaticos dado = resultado.getFirst();
        assertThat(dado.data()).isEqualTo(LocalDate.of(2025, 5, 10));
        assertThat(dado.temperaturaMedia()).isEqualTo(22.5);
        assertThat(dado.precipitacao()).isEqualTo(3.0);
        assertThat(dado.umidadeRelativa()).isEqualTo(80.0);
        assertThat(dado.velocidadeVento()).isNull(); // -999.0 convertido para null
        assertThat(dado.condicaoClima()).isEqualTo("GAROA");
        assertThat(dado.fonte()).isEqualTo("NASA_POWER");
    }
}
