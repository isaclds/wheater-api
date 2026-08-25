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
    //Criar classe buscar por clima predominante na cidade, usar o buscaCoordenadas no geocodingservice

    //Criar classe buscar por clima predominante no pais, mexer no GeocodingCordenadasService para a busca

    //Criar classe buscar por clima predominante no estado, mexer no GeocodingCordenadasService para a busca

    //Fazer um metodo para comparar se a data passada é no passado ou futuro, usar os metodos abaixo dependendo da resposta

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