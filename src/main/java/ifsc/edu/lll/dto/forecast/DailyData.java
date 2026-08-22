package ifsc.edu.lll.dto.forecast;

import java.util.List;

public record DailyData(
        List<String> time,
        List<Double> temperature_2m_max,
        List<Double> temperature_2m_min,
        List<Double> precipitation_sum,
        List<Integer> weather_code
) {}