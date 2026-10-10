package ifsc.edu.lll.service.mapper;

import ifsc.edu.lll.dto.forecast.DailyData;
import ifsc.edu.lll.dto.forecast.ForecastResponse;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ForecastResponseMapper — mapeamento Open-Meteo → DadosClimaticos")
class ForecastResponseMapperTest {

    private ForecastResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ForecastResponseMapper();
    }

    @Test
    @DisplayName("Deve mapear corretamente uma resposta com um dia")
    void deveMapearRespostaComUmDia() {
        DailyData daily = new DailyData(
                List.of("2026-10-09"),
                List.of(28.0),
                List.of(15.0),
                List.of(5.0),
                List.of(0)
        );
        ForecastResponse resposta = new ForecastResponse(-27.59, -48.54, "America/Sao_Paulo", daily);

        List<DadosClimaticos> resultado = mapper.paraDadosClimaticos(resposta);

        assertThat(resultado).hasSize(1);
        DadosClimaticos dado = resultado.get(0);
        assertThat(dado.temperaturaMaxima()).isEqualTo(28.0);
        assertThat(dado.temperaturaMinima()).isEqualTo(15.0);
        assertThat(dado.temperaturaMedia()).isEqualTo(21.5);
        assertThat(dado.precipitacao()).isEqualTo(5.0);
        assertThat(dado.condicaoClima()).isEqualTo("CEU_LIMPO");
        assertThat(dado.fonte()).isEqualTo("OPEN_METEO");
    }

    @Test
    @DisplayName("Deve calcular média corretamente como (max + min) / 2")
    void deveCalcularMediaCorretamente() {
        DailyData daily = new DailyData(
                List.of("2026-10-09"),
                List.of(30.0),
                List.of(10.0),
                List.of(0.0),
                List.of(0)
        );
        ForecastResponse resposta = new ForecastResponse(-27.59, -48.54, "America/Sao_Paulo", daily);

        DadosClimaticos dado = mapper.paraDadosClimaticos(resposta).get(0);

        assertThat(dado.temperaturaMedia()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("Deve mapear múltiplos dias preservando a ordem")
    void deveMapearMultiplosDias() {
        DailyData daily = new DailyData(
                List.of("2026-10-09", "2026-10-10"),
                List.of(28.0, 22.0),
                List.of(15.0, 12.0),
                List.of(0.0, 10.0),
                List.of(0, 61)
        );
        ForecastResponse resposta = new ForecastResponse(-27.59, -48.54, "America/Sao_Paulo", daily);

        List<DadosClimaticos> resultado = mapper.paraDadosClimaticos(resposta);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).condicaoClima()).isEqualTo("CEU_LIMPO");
        assertThat(resultado.get(1).condicaoClima()).isEqualTo("CHUVA");
    }
}
