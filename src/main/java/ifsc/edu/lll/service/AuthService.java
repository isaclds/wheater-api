package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.auth.LoginRequestDTO;
import ifsc.edu.lll.dto.auth.TokenResponseDTO;
import ifsc.edu.lll.exception.CredenciaisInvalidasException;
import ifsc.edu.lll.model.Cliente;
import ifsc.edu.lll.repository.ClienteRepository;
import ifsc.edu.lll.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final ClienteRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final String hashFalso;

    public AuthService(ClienteRepository repo, PasswordEncoder encoder, JwtService jwt) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwt = jwt;
        this.hashFalso = encoder.encode("senha-que-nunca-sera-usada");
    }

    public TokenResponseDTO login(LoginRequestDTO dto) {
        String email = dto.email().trim().toLowerCase(Locale.ROOT);
        Cliente cliente = repo.findByEmail(email).orElse(null);
        boolean senhaOk = encoder.matches(dto.senha(),
                cliente != null ? cliente.getSenhaHash() : hashFalso);
        if (cliente == null || !senhaOk) {
            log.warn("Falha no login para email={}", email);
            throw new CredenciaisInvalidasException();
        }
        log.info("Login realizado com sucesso para email={}, clienteId={}", email, cliente.getId());
        return new TokenResponseDTO(
                jwt.gerar(cliente.getId(), cliente.getEmail()),
                "Bearer",
                jwt.getExpiracaoSegundos()
        );
    }
}
