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
    private final LocalidadesReferencia localidadesReferencia;

    public PrevisaoService(GeocodingCoordenadasService geocodingCoordenadasService,
                           ForecastClimaService forecastClimaService,
                           NasaClimaService nasaClimaService,
                           LocalidadesReferencia localidadesReferencia) {
        this.geocodingCoordenadasService = geocodingCoordenadasService;
        this.forecastClimaService = forecastClimaService;
        this.nasaClimaService = nasaClimaService;
        this.localidadesReferencia = localidadesReferencia;
    }

    public DadosClimaticos buscaPrevisaoPorCidade(String pais, String cidade, LocalDate data) {
        Coordenadas coordenadas = geocodingCoordenadasService.buscaCoordenadas(cidade, pais);
        return buscarClima(coordenadas, data);
    }

    public List<DadosClimaticos> buscaPrevisaoPorPais(String pais, LocalDate data) {
        List<String> cidades = localidadesReferencia.cidadesDoPais(pais);
        return cidades.stream()
                .map(cidade -> {
                    Coordenadas coordenadas = geocodingCoordenadasService.buscaCoordenadas(cidade, pais);
                    return buscarClima(coordenadas, data);
                })
                .toList();
    }

    public List<DadosClimaticos> buscaPrevisaoPorEstado(String pais, String estado, LocalDate data) {
        List<String> cidades = localidadesReferencia.cidadesDoEstado(pais, estado);
        return cidades.stream()
                .map(cidade -> {
                    Coordenadas coordenadas = geocodingCoordenadasService.buscaCoordenadas(cidade, pais);
                    return buscarClima(coordenadas, data);
                })
                .toList();
    }

    private DadosClimaticos buscarClima(Coordenadas coordenadas, LocalDate data) {
        if (data==null) data=LocalDate.now();
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