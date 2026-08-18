package ifsc.edu.lll.dto.forecast;

public record CurrentData(
        String time,
        int interval,
        double temperature_2m,
        double relative_humidity_2m,
        double apparent_temperature,
        double precipitation,
        int weather_code,
        double wind_speed_10m
) {}
