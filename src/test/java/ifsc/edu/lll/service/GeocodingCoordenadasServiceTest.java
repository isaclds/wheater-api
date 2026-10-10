package ifsc.edu.lll.service;

import ifsc.edu.lll.client.GeocodingClient;
import ifsc.edu.lll.dto.geocoding.GeoResponse;
import ifsc.edu.lll.dto.geocoding.GeoResult;
import ifsc.edu.lll.dto.shared.Coordenadas;
import ifsc.edu.lll.service.mapper.GeocodingResponseMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeocodingCoordenadasService — busca de coordenadas geográficas")
class GeocodingCoordenadasServiceTest {

    @Mock
    private GeocodingClient geocodingClient;

    @Mock
    private GeocodingResponseMapper mapper;

    @InjectMocks
    private GeocodingCoordenadasService service;

    @Test
    @DisplayName("Deve resolver código do país, chamar client e retornar coordenadas mapeadas")
    void deveBuscarCoordenadasComSucesso() {
        GeoResponse geoResponse = new GeoResponse(List.of(
                new GeoResult("Florianópolis", -27.5969, -48.5495, "Brazil", "BR", "Santa Catarina", "America/Sao_Paulo")
        ));
        Coordenadas esperado = new Coordenadas(-27.5969, -48.5495);

        // "Brasil" resolve para "BR"
        when(geocodingClient.buscaCoordenadas(eq("Florianópolis"), eq(1), eq("pt"), eq("BR")))
                .thenReturn(geoResponse);
        when(mapper.paraCoordenadas(geoResponse)).thenReturn(esperado);

        Coordenadas resultado = service.buscaCoordenadas("Florianópolis", "Brasil");

        assertThat(resultado).isEqualTo(esperado);
        verify(geocodingClient).buscaCoordenadas("Florianópolis", 1, "pt", "BR");
        verify(mapper).paraCoordenadas(geoResponse);
    }
}
