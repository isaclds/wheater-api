package ifsc.edu.lll.service;

import ifsc.edu.lll.client.GeocodingClient;
import ifsc.edu.lll.service.mapper.GeocodingResponseMapper;
import ifsc.edu.lll.dto.geocoding.GeoResponse;
import ifsc.edu.lll.dto.shared.Coordenadas;
import org.springframework.stereotype.Service;

@Service
public class GeocodingCoordenadasService {

    private final GeocodingClient geocodingClient;
    private final GeocodingResponseMapper mapper;

    private static final String LANGUAGE;
    private static final int COUNT;

    static {
        LANGUAGE = "pt";
        COUNT = 1;
    }

    public GeocodingCoordenadasService(GeocodingClient geocodingClient, GeocodingResponseMapper mapper) {
        this.geocodingClient = geocodingClient;
        this.mapper = mapper;
    }

    public Coordenadas buscaCoordenadas(String local, String codigoPais) {
        GeoResponse dados = this.buscaDados(local, codigoPais);
        return mapper.paraCoordenadas(dados);
    }

    private GeoResponse buscaDados(String local, String codigoPais) {
        return this.geocodingClient.buscaCoordenadas(local, COUNT, LANGUAGE, codigoPais);
    }
}