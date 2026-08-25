package ifsc.edu.lll.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LocalidadesReferencia {

    //Por enquanto pode ser só isso, da pra mudar pra application properties tmb
    private static final Map<String, List<String>> CIDADES_POR_PAIS = Map.of(
            "br", List.of("São Paulo", "Rio de Janeiro", "Brasília", "Salvador", "Florianópolis"),
            "us", List.of("New York", "Los Angeles", "Chicago", "Houston", "Miami")
    );

    private static final Map<String, List<String>> CIDADES_POR_ESTADO = Map.of(
            "sc", List.of("Florianópolis", "Joinville", "Blumenau", "Chapecó"),
            "sp", List.of("São Paulo", "Campinas", "Santos", "Ribeirão Preto")
    );

    public List<String> cidadesDoPais(String codigoPais) {
        List<String> cidades = CIDADES_POR_PAIS.get(codigoPais.toLowerCase());
        if (cidades == null || cidades.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma localidade de referência cadastrada para o país: " + codigoPais);
        }
        return cidades;
    }

    public List<String> cidadesDoEstado(String codigoPais, String estado) {
        List<String> cidades = CIDADES_POR_ESTADO.get(estado.toLowerCase());
        if (cidades == null || cidades.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma localidade de referência cadastrada para o estado: " + estado);
        }
        return cidades;
    }
}