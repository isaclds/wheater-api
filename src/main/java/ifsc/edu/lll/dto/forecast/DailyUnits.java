package ifsc.edu.lll.dto.forecast;

public record DailyUnits(
        String time,
        String temperature_2m_max,
        String temperature_2m_min,
        String precipitation_sum,
        String weather_code
) {}
