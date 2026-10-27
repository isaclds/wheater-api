package ifsc.edu.lll.dto.auth;

import ifsc.edu.lll.model.Cliente;

public record ClienteResponseDTO(Long id, String nome, String email) {
    public static ClienteResponseDTO de(Cliente c) {
        return new ClienteResponseDTO(c.getId(), c.getNome(), c.getEmail());
    }
}
