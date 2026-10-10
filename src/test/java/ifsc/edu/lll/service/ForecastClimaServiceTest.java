package ifsc.edu.lll.service;

import ifsc.edu.lll.client.ForecastClient;
import ifsc.edu.lll.dto.forecast.DailyData;
import ifsc.edu.lll.dto.forecast.ForecastResponse;
import ifsc.edu.lll.dto.shared.Coordenadas;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.service.mapper.ForecastResponseMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ForecastClimaService — integração com API Open-Meteo Forecast")
class ForecastClimaServiceTest {

    @Mock
    private ForecastClient forecastClient;

    @Mock
    private ForecastResponseMapper mapper;

    @InjectMocks
    private ForecastClimaService forecastClimaService;

    @Test
    @DisplayName("Deve chamar ForecastClient com parâmetros corretos e mapear resposta")
    void deveBuscarPrevisaoEMapear() {
        Coordenadas coords = new Coordenadas(-27.59, -48.54);
        ForecastResponse apiResponse = new ForecastResponse(
                -27.59, -48.54, "America/Sao_Paulo",
                new DailyData(List.of("2026-10-10"), List.of(25.0), List.of(15.0), List.of(0.0), List.of(0))
        );
        DadosClimaticos esperado = new DadosClimaticos(
                LocalDate.now(), 20.0, 25.0, 15.0, 0.0, 70.0, 5.0, "CEU_LIMPO", "OPEN_METEO");

        when(forecastClient.buscaPrevisao(
                eq(-27.59), eq(-48.54), anyString(), anyString(), anyString(), eq("auto"), eq(7), eq("celsius")))
                .thenReturn(apiResponse);
        when(mapper.paraDadosClimaticos(apiResponse)).thenReturn(List.of(esperado));

        List<DadosClimaticos> resultado = forecastClimaService.buscaPrevisao(coords, 7);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst()).isEqualTo(esperado);
        verify(forecastClient).buscaPrevisao(
                eq(-27.59), eq(-48.54), anyString(), anyString(), anyString(), eq("auto"), eq(7), eq("celsius"));
    }
}
