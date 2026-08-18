package ifsc.edu.lll.dto.forecast;

public record ForecastResponse<CurrentUnits, CurrentData, HourlyUnits, HourlyData, DailyUnits, DailyData>(
        double latitude,
        double longitude,
        double elevation,
        String timezone,
        String timezone_abbreviation,
        long utc_offset_seconds,
        CurrentUnits current_units,
        CurrentData current,
        HourlyUnits hourly_units,
        HourlyData hourly,
        DailyUnits daily_units,
        DailyData daily
) {}