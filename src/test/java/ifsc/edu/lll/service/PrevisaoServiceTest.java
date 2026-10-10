package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.shared.Coordenadas;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrevisaoService — orquestração de previsões futuras e históricas")
class PrevisaoServiceTest {

    @Mock
    private GeocodingCoordenadasService geocodingCoordenadasService;

    @Mock
    private ForecastClimaService forecastClimaService;

    @Mock
    private NasaClimaService nasaClimaService;

    @Mock
    private LocalidadesReferencia localidadesReferencia;

    @InjectMocks
    private PrevisaoService previsaoService;

    private final Coordenadas coordsFpolis = new Coordenadas(-27.59, -48.54);

    @Test
    @DisplayName("Data de hoje ou futura deve consultar ForecastClimaService (Open-Meteo)")
    void dataFuturaDeveConsultarForecast() {
        LocalDate hoje = LocalDate.now();
        DadosClimaticos dado = new DadosClimaticos(
                hoje, 20.0, 25.0, 15.0, 0.0, 70.0, 5.0, "CEU_LIMPO", "OPEN_METEO");

        when(geocodingCoordenadasService.buscaCoordenadas("Florianópolis", "Brasil"))
                .thenReturn(coordsFpolis);
        when(forecastClimaService.buscaPrevisao(eq(coordsFpolis), anyInt()))
                .thenReturn(List.of(dado));

        DadosClimaticos resultado = previsaoService.buscaPrevisaoPorCidade("Brasil", "Florianópolis", hoje);

        assertThat(resultado).isEqualTo(dado);
        verify(forecastClimaService).buscaPrevisao(eq(coordsFpolis), anyInt());
        verifyNoInteractions(nasaClimaService);
    }

    @Test
    @DisplayName("Data passada deve consultar NasaClimaService (NASA POWER)")
    void dataPassadaDeveConsultarNasa() {
        LocalDate passado = LocalDate.now().minusDays(5);
        DadosClimaticos dadoHistorico = new DadosClimaticos(
                passado, 18.0, null, null, 2.0, 80.0, 4.0, "GAROA", "NASA_POWER");

        when(geocodingCoordenadasService.buscaCoordenadas("Florianópolis", "Brasil"))
                .thenReturn(coordsFpolis);
        when(nasaClimaService.buscarClimaDiario(coordsFpolis, passado, passado))
                .thenReturn(List.of(dadoHistorico));

        DadosClimaticos resultado = previsaoService.buscaPrevisaoPorCidade("Brasil", "Florianópolis", passado);

        assertThat(resultado).isEqualTo(dadoHistorico);
        verify(nasaClimaService).buscarClimaDiario(coordsFpolis, passado, passado);
        verifyNoInteractions(forecastClimaService);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException se o provedor futuro não contiver a data")
    void deveLancarExcecaoQuandoPrevisaoFuturaIndisponivel() {
        LocalDate hoje = LocalDate.now();
        LocalDate amanha = hoje.plusDays(1);
        DadosClimaticos dadoOutroDia = new DadosClimaticos(
                hoje, 20.0, 25.0, 15.0, 0.0, 70.0, 5.0, "CEU_LIMPO", "OPEN_METEO");

        when(geocodingCoordenadasService.buscaCoordenadas("Florianópolis", "Brasil"))
                .thenReturn(coordsFpolis);
        when(forecastClimaService.buscaPrevisao(eq(coordsFpolis), anyInt()))
                .thenReturn(List.of(dadoOutroDia)); // não contém "amanha"

        assertThatThrownBy(() -> previsaoService.buscaPrevisaoPorCidade("Brasil", "Florianópolis", amanha))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Previsão indisponível");
    }

    @Test
    @DisplayName("Deve consultar todas as cidades de referência do país")
    void deveBuscarPrevisaoPorPais() {
        LocalDate hoje = LocalDate.now();
        when(localidadesReferencia.cidadesDoPais("Brasil"))
                .thenReturn(List.of("Florianópolis", "Curitiba"));

        when(geocodingCoordenadasService.buscaCoordenadas(anyString(), eq("Brasil")))
                .thenReturn(coordsFpolis);

        DadosClimaticos dado = new DadosClimaticos(
                hoje, 20.0, 25.0, 15.0, 0.0, 70.0, 5.0, "CEU_LIMPO", "OPEN_METEO");
        when(forecastClimaService.buscaPrevisao(eq(coordsFpolis), anyInt()))
                .thenReturn(List.of(dado));

        List<DadosClimaticos> resultado = previsaoService.buscaPrevisaoPorPais("Brasil", hoje);

        assertThat(resultado).hasSize(2);
        verify(geocodingCoordenadasService, times(2)).buscaCoordenadas(anyString(), eq("Brasil"));
    }

    @Test
    @DisplayName("Deve consultar todas as cidades de referência do estado")
    void deveBuscarPrevisaoPorEstado() {
        LocalDate hoje = LocalDate.now();
        when(localidadesReferencia.cidadesDoEstado("Brasil", "SC"))
                .thenReturn(List.of("Florianópolis", "Joinville"));

        when(geocodingCoordenadasService.buscaCoordenadas(anyString(), eq("Brasil")))
                .thenReturn(coordsFpolis);

        DadosClimaticos dado = new DadosClimaticos(
                hoje, 20.0, 25.0, 15.0, 0.0, 70.0, 5.0, "CEU_LIMPO", "OPEN_METEO");
        when(forecastClimaService.buscaPrevisao(eq(coordsFpolis), anyInt()))
                .thenReturn(List.of(dado));

        List<DadosClimaticos> resultado = previsaoService.buscaPrevisaoPorEstado("Brasil", "SC", hoje);

        assertThat(resultado).hasSize(2);
        verify(geocodingCoordenadasService, times(2)).buscaCoordenadas(anyString(), eq("Brasil"));
    }
}
