package ifsc.edu.lll.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LocalidadesReferencia — consulta de cidades cadastradas por país e estado")
class LocalidadesReferenciaTest {

    private LocalidadesReferencia localidadesReferencia;

    @BeforeEach
    void setUp() {
        localidadesReferencia = new LocalidadesReferencia();
    }

    @Test
    @DisplayName("Deve retornar cidades cadastradas para o Brasil")
    void deveRetornarCidadesDoBrasil() {
        List<String> cidades = localidadesReferencia.cidadesDoPais("Brasil");

        assertThat(cidades)
                .isNotEmpty()
                .contains("Florianópolis", "São Paulo", "Rio de Janeiro");
    }

    @Test
    @DisplayName("Deve retornar cidades cadastradas para os Estados Unidos")
    void deveRetornarCidadesDosEstadosUnidos() {
        List<String> cidades = localidadesReferencia.cidadesDoPais("US");

        assertThat(cidades)
                .isNotEmpty()
                .contains("New York", "Miami");
    }

    @Test
    @DisplayName("Deve lançar exceção para país sem localidades de referência")
    void deveLancarExcecaoParaPaisSemReferencia() {
        assertThatThrownBy(() -> localidadesReferencia.cidadesDoPais("Alemanha"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nenhuma localidade de referência cadastrada para o país");
    }

    @Test
    @DisplayName("Deve retornar cidades de Santa Catarina por sigla e por extenso")
    void deveRetornarCidadesDeSantaCatarina() {
        List<String> porSigla = localidadesReferencia.cidadesDoEstado("Brasil", "sc");
        List<String> porNome = localidadesReferencia.cidadesDoEstado("Brasil", "Santa Catarina");

        assertThat(porSigla).contains("Florianópolis", "Joinville");
        assertThat(porNome).contains("Florianópolis", "Joinville");
    }

    @Test
    @DisplayName("Deve retornar cidades de São Paulo por sigla e por extenso")
    void deveRetornarCidadesDeSaoPaulo() {
        List<String> porSigla = localidadesReferencia.cidadesDoEstado("Brasil", "sp");
        List<String> porNome = localidadesReferencia.cidadesDoEstado("Brasil", "São Paulo");

        assertThat(porSigla).contains("São Paulo", "Campinas");
        assertThat(porNome).contains("São Paulo", "Campinas");
    }

    @Test
    @DisplayName("Deve lançar exceção para estado sem localidades cadastradas")
    void deveLancarExcecaoParaEstadoSemReferencia() {
        assertThatThrownBy(() -> localidadesReferencia.cidadesDoEstado("Brasil", "Acre"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nenhuma localidade de referência cadastrada para o estado");
    }
}
