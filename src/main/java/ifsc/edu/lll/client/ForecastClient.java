package ifsc.edu.lll.client;

import ifsc.edu.lll.dto.forecast.ForecastResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "forecastClient", url = "https://api.open-meteo.com")
public interface ForecastClient {

    @GetMapping("/v1/forecast")
    ForecastResponse buscaPrevisao(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "current", required = false) String current,
            @RequestParam(value = "hourly", required = false) String hourly,
            @RequestParam(value = "daily", required = false) String daily,
            @RequestParam(value = "timezone", defaultValue = "auto") String timezone,
            @RequestParam(value = "forecast_days", defaultValue = "7") int forecastDays,
            @RequestParam(value = "temperature_unit", defaultValue = "celsius") String temperatureUnit
    );
}
