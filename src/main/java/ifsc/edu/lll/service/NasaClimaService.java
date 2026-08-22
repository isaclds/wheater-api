package ifsc.edu.lll.service;

import ifsc.edu.lll.client.NasaPowerClient;
import ifsc.edu.lll.dto.nasa.NasaPowerResponse;
import ifsc.edu.lll.dto.shared.Coordenadas;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.service.mapper.NasaResponseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NasaClimaService {

    private final NasaPowerClient nasaPowerClient;
    private final NasaResponseMapper mapper;

    private static final String PARAMETROS;
    private static final DateTimeFormatter FORMATO_NASA;

    static {
        PARAMETROS = "T2M,PRECTOTCORR,RH2M,WS2M";
        FORMATO_NASA = DateTimeFormatter.ofPattern("yyyyMMdd");
    }

    public NasaClimaService(NasaPowerClient nasaPowerClient, NasaResponseMapper nasaResponseMapper) {
        this.nasaPowerClient = nasaPowerClient;
        this.mapper = nasaResponseMapper;
    }

    public List<DadosClimaticos> buscarClimaDiario(Coordenadas coordenadas, LocalDate inicio, LocalDate fim) {
        NasaPowerResponse resposta = nasaPowerClient.buscaClimaDiario(PARAMETROS,
                "AG",
                coordenadas.latitude(),
                coordenadas.longitude(),
                inicio.format(FORMATO_NASA),
                fim.format(FORMATO_NASA),
                "JSON");
        return mapper.paraDadosClimaticos(resposta);
    }
}