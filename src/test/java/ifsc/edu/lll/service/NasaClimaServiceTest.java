package ifsc.edu.lll.service;

import ifsc.edu.lll.client.NasaPowerClient;
import ifsc.edu.lll.dto.nasa.NasaGeometry;
import ifsc.edu.lll.dto.nasa.NasaPowerResponse;
import ifsc.edu.lll.dto.nasa.NasaProperties;
import ifsc.edu.lll.dto.shared.Coordenadas;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.service.mapper.NasaResponseMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NasaClimaService — integração com API NASA POWER")
class NasaClimaServiceTest {

    @Mock
    private NasaPowerClient nasaPowerClient;

    @Mock
    private NasaResponseMapper mapper;

    @InjectMocks
    private NasaClimaService nasaClimaService;

    @Test
    @DisplayName("Deve formatar datas no padrão NASA (yyyyMMdd) e delegar chamada ao cliente")
    void deveBuscarClimaDiarioFormatandoDatas() {
        Coordenadas coords = new Coordenadas(-27.59, -48.54);
        LocalDate inicio = LocalDate.of(2025, 5, 10);
        LocalDate fim = LocalDate.of(2025, 5, 12);

        NasaPowerResponse apiResponse = new NasaPowerResponse(
                "Feature",
                new NasaGeometry("Point", List.of(-48.54, -27.59)),
                new NasaProperties(Map.of()),
                Map.of(),
                List.of()
        );
        DadosClimaticos dadoMock = new DadosClimaticos(
                inicio, 22.0, null, null, 1.5, 75.0, 3.2, "CEU_LIMPO", "NASA_POWER");

        when(nasaPowerClient.buscaClimaDiario(
                eq("T2M,PRECTOTCORR,RH2M,WS2M"),
                eq("AG"),
                eq(-27.59),
                eq(-48.54),
                eq("20250510"),
                eq("20250512"),
                eq("JSON")))
                .thenReturn(apiResponse);
        when(mapper.paraDadosClimaticos(apiResponse)).thenReturn(List.of(dadoMock));

        List<DadosClimaticos> resultado = nasaClimaService.buscarClimaDiario(coords, inicio, fim);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst()).isEqualTo(dadoMock);
        verify(nasaPowerClient).buscaClimaDiario(
                "T2M,PRECTOTCORR,RH2M,WS2M", "AG", -27.59, -48.54, "20250510", "20250512", "JSON");
    }
}
