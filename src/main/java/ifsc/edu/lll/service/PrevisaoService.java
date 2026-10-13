package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.shared.Coordenadas;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrevisaoService {

    private static final int LIMITE_DIAS_PREVISAO = 16;

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

    public List<DadosClimaticos> buscaPrevisaoPorPeriodo(String pais, String cidade, LocalDate inicio, LocalDate fim) {
        LocalDate hoje = LocalDate.now();
        validarJanelaDePrevisao(hoje, fim);

        Coordenadas coordenadas = geocodingCoordenadasService.buscaCoordenadas(cidade, pais);
        List<DadosClimaticos> resultado = new ArrayList<>();

        if (inicio.isBefore(hoje)) {
            LocalDate fimPassado = fim.isBefore(hoje) ? fim : hoje.minusDays(1);
            resultado.addAll(buscarPeriodoPassado(coordenadas, inicio, fimPassado));
        }
        if (!fim.isBefore(hoje)) {
            LocalDate inicioFuturo = inicio.isAfter(hoje) ? inicio : hoje;
            resultado.addAll(buscarPeriodoFuturo(coordenadas, hoje, inicioFuturo, fim));
        }
        return resultado;
    }

    private void validarJanelaDePrevisao(LocalDate hoje, LocalDate fim) {
        if (!fim.isBefore(hoje) && ChronoUnit.DAYS.between(hoje, fim) + 1 > LIMITE_DIAS_PREVISAO) {
            throw new IllegalArgumentException(
                    "Previsão indisponível para " + fim + " (fora da janela do provedor: máximo de "
                            + LIMITE_DIAS_PREVISAO + " dias a partir de hoje)");
        }
    }

    private List<DadosClimaticos> buscarPeriodoPassado(Coordenadas coordenadas, LocalDate inicio, LocalDate fim) {
        List<DadosClimaticos> dados = nasaClimaService.buscarClimaDiario(coordenadas, inicio, fim);
        if (dados.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nenhum dado histórico da NASA disponível para o período " + inicio + " a " + fim);
        }
        return dados;
    }

    private List<DadosClimaticos> buscarPeriodoFuturo(Coordenadas coordenadas, LocalDate hoje,
                                                      LocalDate inicio, LocalDate fim) {
        int diasNecessarios = (int) ChronoUnit.DAYS.between(hoje, fim) + 1;
        List<DadosClimaticos> previsao = forecastClimaService.buscaPrevisao(coordenadas, diasNecessarios);

        List<DadosClimaticos> doPeriodo = previsao.stream()
                .filter(d -> !d.data().isBefore(inicio) && !d.data().isAfter(fim))
                .toList();

        long diasDoPeriodo = ChronoUnit.DAYS.between(inicio, fim) + 1;
        if (doPeriodo.size() < diasDoPeriodo) {
            throw new IllegalArgumentException(
                    "Previsão indisponível para o período " + inicio + " a " + fim
                            + " (fora da janela do provedor)");
        }
        return doPeriodo;
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