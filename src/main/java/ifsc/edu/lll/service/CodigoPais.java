package ifsc.edu.lll.service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class CodigoPais {

    private static final Map<String, String> POR_NOME = new HashMap<>();

    static {
        Locale ptBr = Locale.of("pt", "BR");
        for (String iso : Locale.getISOCountries()) {
            Locale pais = Locale.of("", iso);
            POR_NOME.put(normalizar(pais.getDisplayCountry(ptBr)), iso);
            POR_NOME.put(normalizar(pais.getDisplayCountry(Locale.ENGLISH)), iso);
            POR_NOME.put(normalizar(iso), iso);
        }
    }

    private CodigoPais() {}

    public static String de(String pais) {
        String iso = POR_NOME.get(normalizar(pais));
        if (iso == null) {
            throw new IllegalArgumentException("País não reconhecido: " + pais);
        }
        return iso;
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}