package ifsc.edu.lll.service;

import ifsc.edu.lll.client.NasaPowerClient;
import ifsc.edu.lll.dto.nasa.NasaPowerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class NasaClimaService {

    private final NasaPowerClient nasaPowerClient;

    private static final String PARAMETROS =
            "T2M,PRECTOTCORR,RH2M,WS2M"; // temp média, precipitação corrigida, umidade, vento a 2m

    private static final DateTimeFormatter FORMATO_NASA = DateTimeFormatter.ofPattern("yyyyMMdd");

    public NasaClimaService(NasaPowerClient nasaPowerClient) {
        this.nasaPowerClient = nasaPowerClient;
    }

    public NasaPowerResponse buscarClimaDiario(double lat, double lon, LocalDate inicio, LocalDate fim) {
        // Realizar o tratamento para transformar em um objeto mais legivel dentro da api
        return nasaPowerClient.buscaClimaDiario(
                PARAMETROS,
                "AG",
                lat,
                lon,
                inicio.format(FORMATO_NASA),
                fim.format(FORMATO_NASA),
                "JSON"
        );
    }
}