package ifsc.edu.lll.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CodigoPais — conversão de nomes de países para código ISO")
class CodigoPaisTest {

    @ParameterizedTest(name = "País ''{0}'' deve resolver para ISO ''{1}''")
    @CsvSource({
            "Brasil, BR",
            "brasil, BR",
            "BR, BR",
            "br, BR",
            "Brazil, BR",
            "Estados Unidos, US",
            "United States, US",
            "US, US",
            "Alemanha, DE",
            "Germany, DE",
            "Japão, JP",
            "japao, JP",
            "Japan, JP"
    })
    @DisplayName("Deve resolver nomes válidos em português, inglês e códigos ISO")
    void deveResolverPaisesValidos(String entrada, String esperado) {
        assertThat(CodigoPais.de(entrada)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para país inexistente")
    void deveLancarExcecaoParaPaisInexistente() {
        assertThatThrownBy(() -> CodigoPais.de("Atlantida"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("País não reconhecido");
    }

    @Test
    @DisplayName("Deve ignorar acentos e espaços extras")
    void deveIgnorarAcentosEEspacos() {
        assertThat(CodigoPais.de("  áfrica do sul  ")).isEqualTo("ZA");
    }
}
