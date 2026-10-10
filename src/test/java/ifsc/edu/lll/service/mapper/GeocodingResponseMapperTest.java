package ifsc.edu.lll.service.mapper;

import ifsc.edu.lll.dto.geocoding.GeoResponse;
import ifsc.edu.lll.dto.geocoding.GeoResult;
import ifsc.edu.lll.dto.shared.Coordenadas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GeocodingResponseMapper — mapeamento Open-Meteo GeoResponse → Coordenadas")
class GeocodingResponseMapperTest {

    private GeocodingResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GeocodingResponseMapper();
    }

    @Test
    @DisplayName("Deve extrair coordenadas do primeiro resultado com sucesso")
    void deveMapearParaCoordenadas() {
        GeoResult result = new GeoResult(
                "Florianópolis", -27.5969, -48.5495, "Brazil", "BR", "Santa Catarina", "America/Sao_Paulo");
        GeoResponse response = new GeoResponse(List.of(result));

        Coordenadas coords = mapper.paraCoordenadas(response);

        assertThat(coords.latitude()).isEqualTo(-27.5969);
        assertThat(coords.longitude()).isEqualTo(-48.5495);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a lista de resultados for nula")
    void deveLancarExcecaoQuandoResultadosNulos() {
        GeoResponse response = new GeoResponse(null);

        assertThatThrownBy(() -> mapper.paraCoordenadas(response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nenhum local encontrado");
    }

    @Test
    @DisplayName("Deve lançar exceção quando a lista de resultados for vazia")
    void deveLancarExcecaoQuandoResultadosVazios() {
        GeoResponse response = new GeoResponse(List.of());

        assertThatThrownBy(() -> mapper.paraCoordenadas(response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nenhum local encontrado");
    }
}
