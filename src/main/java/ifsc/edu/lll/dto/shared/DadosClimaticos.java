package ifsc.edu.lll.dto.shared;

import java.time.LocalDate;

public record DadosClimaticos(
        LocalDate data,
        Double temperaturaMedia,
        Double temperaturaMaxima,
        Double temperaturaMinima,
        Double precipitacao,
        Double umidadeRelativa,
        Double velocidadeVento,
        String fonte
) {}
