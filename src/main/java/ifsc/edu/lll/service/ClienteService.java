package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.auth.ClienteRequestDTO;
import ifsc.edu.lll.dto.auth.ClienteResponseDTO;
import ifsc.edu.lll.exception.EmailJaCadastradoException;
import ifsc.edu.lll.model.Cliente;
import ifsc.edu.lll.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class ClienteService {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository repo;
    private final PasswordEncoder encoder;

    public ClienteService(ClienteRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional
    public ClienteResponseDTO cadastrar(ClienteRequestDTO dto) {
        String email = dto.email().trim().toLowerCase(Locale.ROOT);
        if (repo.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }
        try {
            Cliente salvo = repo.saveAndFlush(
                    new Cliente(dto.nome().trim(), email, encoder.encode(dto.senha())));
            log.info("Cliente cadastrado com sucesso: id={}, email={}", salvo.getId(), salvo.getEmail());
            return ClienteResponseDTO.de(salvo);
        } catch (DataIntegrityViolationException corrida) {
            throw new EmailJaCadastradoException();
        }
    }
}
