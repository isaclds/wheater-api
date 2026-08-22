package ifsc.edu.lll.dto.forecast;

import java.util.Map;

public record ForecastResponse(
        double latitude,
        double longitude,
        String timezone,
        DailyData daily
) {}