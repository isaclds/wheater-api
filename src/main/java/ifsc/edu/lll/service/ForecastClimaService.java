package ifsc.edu.lll.service;

import ifsc.edu.lll.client.ForecastClient;
import ifsc.edu.lll.dto.forecast.ForecastResponse;
import ifsc.edu.lll.dto.shared.Coordenadas;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.service.mapper.ForecastResponseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForecastClimaService {

    private final ForecastClient forecastClient;
    private final ForecastResponseMapper mapper;

    private static final String CURRENT_VARS;
    private static final String HOURLY_VARS;
    private static final String DAILY_VARS;

    static {
        CURRENT_VARS = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m";
        HOURLY_VARS = "temperature_2m,precipitation_probability,precipitation,weather_code";
        DAILY_VARS = "temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code";
    }

    public ForecastClimaService(ForecastClient forecastClient,  ForecastResponseMapper mapper) {
        this.forecastClient = forecastClient;
        this.mapper = mapper;
    }

    public List<DadosClimaticos> buscaPrevisao(Coordenadas coordenadas, int forecastDays) {
        ForecastResponse resposta = forecastClient.buscaPrevisao(
                coordenadas.latitude(),
                coordenadas.longitude(),
                CURRENT_VARS,
                HOURLY_VARS,
                DAILY_VARS,
                "auto",
                forecastDays,
                "celsius");
        return mapper.paraDadosClimaticos(resposta);
    }
}