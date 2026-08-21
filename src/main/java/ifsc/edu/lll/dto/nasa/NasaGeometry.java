package ifsc.edu.lll.dto.nasa;

import java.util.List;

public record NasaGeometry(
        String type,
        List<Double> coordinates
) {}