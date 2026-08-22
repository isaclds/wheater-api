package ifsc.edu.lll.service;

import ifsc.edu.lll.client.GeocodingClient;
import ifsc.edu.lll.dto.geocoding.GeoResponse;
import ifsc.edu.lll.dto.geocoding.GeoResult;
import ifsc.edu.lll.dto.shared.Coordenadas;
import org.springframework.stereotype.Service;

@Service
public class GeocodingCoordenadasService {
    private final GeocodingClient geocodingClient;

    private static final String LANGUAGE;
    private static final int COUNT;

    static {
        LANGUAGE = "pt";
        COUNT = 1;
    }

    public GeocodingCoordenadasService(GeocodingClient geocodingClient) {
        this.geocodingClient = geocodingClient;
    }

    public Coordenadas buscaCoordenadas(String local, String codigoPais) {
        GeoResponse dados = this.buscaDados(local, codigoPais);

        if (dados.results() == null || dados.results().isEmpty()) {
            throw new IllegalArgumentException("Nenhum local encontrado para: " + local);
        }

        GeoResult resultado = dados.results().getFirst();
        return new Coordenadas(resultado.latitude(), resultado.longitude());
    }

    private GeoResponse buscaDados(String local, String codigoPais) {
        return this.geocodingClient.buscaCoordenadas(local, COUNT, LANGUAGE, codigoPais);
    }
}
