package ifsc.edu.lll.dto.nasa;

import java.util.Map;

public record NasaProperties(
        Map<String, Map<String, Double>> parameter
) {}
