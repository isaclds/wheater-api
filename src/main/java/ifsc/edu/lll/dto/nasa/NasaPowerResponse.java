package ifsc.edu.lll.dto.nasa;

import java.util.List;
import java.util.Map;

public record NasaPowerResponse(
        String type,
        NasaGeometry geometry,
        NasaProperties properties,
        Map<String, Object> header,
        List<String> messages
) {}