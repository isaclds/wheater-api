package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.shared.Coordenadas;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PrevisaoService {

    private final GeocodingCoordenadasService geocodingCoordenadasService;
    private final ForecastClimaService forecastClimaService;
    private final NasaClimaService nasaClimaService;

    public PrevisaoService(GeocodingCoordenadasService geocodingCoordenadasService,
                           ForecastClimaService forecastClimaService,
                           NasaClimaService nasaClimaService) {
        this.geocodingCoordenadasService = geocodingCoordenadasService;
        this.forecastClimaService = forecastClimaService;
        this.nasaClimaService = nasaClimaService;
    }

    public DadosClimaticos buscaPrevisaoPorCidade(String pais, String cidade, LocalDate data) {
        Coordenadas coordenadas = geocodingCoordenadasService.buscaCoordenadas(cidade, pais);
        return buscarClima(coordenadas, data);
    }

    public DadosClimaticos buscaPrevisaoPorPais(String pais, LocalDate data) {
        Coordenadas coordenadas = geocodingCoordenadasService.buscaCoordenadas(pais, pais);
        return buscarClima(coordenadas, data);
    }

    public DadosClimaticos buscaPrevisaoPorEstado(String pais, String estado, LocalDate data) {
        Coordenadas coordenadas = geocodingCoordenadasService.buscaCoordenadas(estado, pais);
        return buscarClima(coordenadas, data);
    }

    private DadosClimaticos buscarClima(Coordenadas coordenadas, LocalDate data) {
        if (data.isBefore(LocalDate.now())) {
            return buscarClimaPassado(coordenadas, data);
        }
        return buscarClimaFuturo(coordenadas, data);
    }

    private DadosClimaticos buscarClimaPassado(Coordenadas coordenadas, LocalDate data) {
        List<DadosClimaticos> dados = nasaClimaService.buscarClimaDiario(coordenadas, data, data);
        return dados.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nenhum dado histórico da NASA disponível para " + data));
    }

    private DadosClimaticos buscarClimaFuturo(Coordenadas coordenadas, LocalDate data) {
        int diasNecessarios = (int) ChronoUnit.DAYS.between(LocalDate.now(), data) + 1;
        List<DadosClimaticos> previsao = forecastClimaService.buscaPrevisao(coordenadas, Math.max(diasNecessarios, 1));

        return previsao.stream()
                .filter(d -> d.data().equals(data))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Previsão indisponível para " + data + " (fora da janela do provedor)"));
    }
}