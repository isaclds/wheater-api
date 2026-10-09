package ifsc.edu.lll.dto.alerta;

// Corpo da requisição para cadastrar um novo alerta
public record AlertaRequestDTO(
        Long canalId,
        String pais,
        String cidade,
        Double temperaturaMaxima,
        Double temperaturaMinima,
        Double precipitacao
) {}
