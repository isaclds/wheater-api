package ifsc.edu.lll.dto.geocoding;

public record GeoResult(
        String name,
        double latitude,
        double longitude,
        String country,
        String country_code,
        String admin1,
        String timezone
) {}