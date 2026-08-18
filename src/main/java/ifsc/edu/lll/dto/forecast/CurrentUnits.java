package ifsc.edu.lll.dto.forecast;

public record CurrentUnits(
        String time,
        String interval,
        String temperature_2m,
        String relative_humidity_2m,
        String apparent_temperature,
        String precipitation,
        String weather_code,
        String wind_speed_10m
) {}
