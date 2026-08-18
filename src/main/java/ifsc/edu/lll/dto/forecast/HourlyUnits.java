package ifsc.edu.lll.dto.forecast;

public record HourlyUnits(
        String time,
        String temperature_2m,
        String precipitation_probability,
        String precipitation,
        String weather_code
) {}
