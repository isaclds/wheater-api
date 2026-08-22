package ifsc.edu.lll.service;

import ifsc.edu.lll.client.ForecastClient;
import ifsc.edu.lll.dto.forecast.ForecastResponse;
import org.springframework.stereotype.Service;

@Service
public class ForecastClimaService {

    private final ForecastClient forecastClient;

    private static final String CURRENT_VARS;
    private static final String HOURLY_VARS;
    private static final String DAILY_VARS;

    static {
        CURRENT_VARS = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m";
        HOURLY_VARS = "temperature_2m,precipitation_probability,precipitation,weather_code";
        DAILY_VARS = "temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code";
    }

    public ForecastClimaService(ForecastClient forecastClient) {
        this.forecastClient = forecastClient;
    }

    public ForecastResponse buscaPrevisao(double lat, double lon, int forecastDays) {
        return forecastClient.buscaPrevisao(
                lat,
                lon,
                CURRENT_VARS,
                HOURLY_VARS,
                DAILY_VARS,
                "auto",
                forecastDays,
                "celsius"
        );
    }
}