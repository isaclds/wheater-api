package ifsc.edu.lll.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class LocalidadesReferencia {

    //Por enquanto pode ser só isso, da pra mudar pra application properties tmb
    private static final Map<String, List<String>> CIDADES_POR_PAIS = Map.of(
            "br", List.of("São Paulo", "Rio de Janeiro", "Brasília", "Salvador", "Florianópolis"),
            "us", List.of("New York", "Los Angeles", "Chicago", "Houston", "Miami")
    );

    private static final List<String> SC = List.of("Florianópolis", "Joinville", "Blumenau", "Chapecó");
    private static final List<String> SP = List.of("São Paulo", "Campinas", "Santos", "Ribeirão Preto");

    private static final Map<String, Map<String, List<String>>> CIDADES_POR_ESTADO = Map.of(
            "br", Map.of(
                    "sc", SC, "santa catarina", SC,
                    "sp", SP, "sao paulo", SP
            )
    );

    public List<String> cidadesDoPais(String pais) {
        String iso = CodigoPais.de(pais).toLowerCase(Locale.ROOT);
        List<String> cidades = CIDADES_POR_PAIS.get(iso);
        if (cidades == null || cidades.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nenhuma localidade de referência cadastrada para o país: " + pais);
        }
        return cidades;
    }

    public List<String> cidadesDoEstado(String pais, String estado) {
        String iso = CodigoPais.de(pais).toLowerCase(Locale.ROOT);
        Map<String, List<String>> estados = CIDADES_POR_ESTADO.get(iso);
        List<String> cidades = estados == null ? null : estados.get(normalizar(estado));
        if (cidades == null || cidades.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nenhuma localidade de referência cadastrada para o estado: " + estado);
        }
        return cidades;
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}