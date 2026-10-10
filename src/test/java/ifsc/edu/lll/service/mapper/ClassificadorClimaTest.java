package ifsc.edu.lll.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ClassificadorClima — classificação por código WMO e precipitação")
class ClassificadorClimaTest {

    // ── porWeatherCode ────────────────────────────────────────────

    @Test
    @DisplayName("Código 0 → CEU_LIMPO")
    void weatherCode0DeveCeuLimpo() {
        assertThat(ClassificadorClima.porWeatherCode(0)).isEqualTo("CEU_LIMPO");
    }

    @ParameterizedTest(name = "Código {0} → PARCIALMENTE_NUBLADO")
    @CsvSource({"1", "2", "3"})
    @DisplayName("Códigos 1-3 → PARCIALMENTE_NUBLADO")
    void weatherCode1a3DeveParcialmenteNublado(int codigo) {
        assertThat(ClassificadorClima.porWeatherCode(codigo)).isEqualTo("PARCIALMENTE_NUBLADO");
    }

    @ParameterizedTest(name = "Código {0} → CHUVA")
    @CsvSource({"61", "63", "65", "66", "67"})
    @DisplayName("Códigos 61-67 → CHUVA")
    void weatherCode61a67DeveChuva(int codigo) {
        assertThat(ClassificadorClima.porWeatherCode(codigo)).isEqualTo("CHUVA");
    }

    @ParameterizedTest(name = "Código {0} → TEMPESTADE")
    @CsvSource({"95", "96", "99"})
    @DisplayName("Códigos 95/96/99 → TEMPESTADE")
    void weatherCode95a99DeveTempestade(int codigo) {
        assertThat(ClassificadorClima.porWeatherCode(codigo)).isEqualTo("TEMPESTADE");
    }

    @Test
    @DisplayName("Código null → DESCONHECIDO")
    void weatherCodeNullDeveDesconhecido() {
        assertThat(ClassificadorClima.porWeatherCode(null)).isEqualTo("DESCONHECIDO");
    }

    @Test
    @DisplayName("Código desconhecido (ex: 999) → DESCONHECIDO")
    void weatherCodeInvalidoDeveDesconhecido() {
        assertThat(ClassificadorClima.porWeatherCode(999)).isEqualTo("DESCONHECIDO");
    }

    // ── porPrecipitacao ───────────────────────────────────────────

    @Test
    @DisplayName("Precipitação 0.0 mm → CEU_LIMPO")
    void precipitacaoZeroDeveCeuLimpo() {
        assertThat(ClassificadorClima.porPrecipitacao(0.0)).isEqualTo("CEU_LIMPO");
    }

    @Test
    @DisplayName("Precipitação 2.0 mm → GAROA")
    void precipitacao2mmDeveGaroa() {
        assertThat(ClassificadorClima.porPrecipitacao(2.0)).isEqualTo("GAROA");
    }

    @Test
    @DisplayName("Precipitação 10.0 mm → CHUVA")
    void precipitacao10mmDeveChuva() {
        assertThat(ClassificadorClima.porPrecipitacao(10.0)).isEqualTo("CHUVA");
    }

    @Test
    @DisplayName("Precipitação 50.0 mm → TEMPESTADE")
    void precipitacao50mmDeveTempestade() {
        assertThat(ClassificadorClima.porPrecipitacao(50.0)).isEqualTo("TEMPESTADE");
    }

    @Test
    @DisplayName("Precipitação null → DESCONHECIDO")
    void precipitacaoNullDeveDesconhecido() {
        assertThat(ClassificadorClima.porPrecipitacao(null)).isEqualTo("DESCONHECIDO");
    }
}
