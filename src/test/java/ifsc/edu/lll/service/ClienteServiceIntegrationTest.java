package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.auth.ClienteRequestDTO;
import ifsc.edu.lll.dto.auth.ClienteResponseDTO;
import ifsc.edu.lll.model.Cliente;
import ifsc.edu.lll.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "jwt.secret=segredo-muito-seguro-com-mais-de-32-bytes-para-os-testes")
@DisplayName("ClienteService — integração com banco e hash de senha")
class ClienteServiceIntegrationTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Cenário 4: Dado cliente salvo, quando consultar o banco, então senha_hash começa com $2 e difere da senha (CLI-05)")
    void cenario04_senhaGravadaComoHashBcrypt() {
        String senhaOriginal = "senhaSuperSegura123";
        ClienteRequestDTO dto = new ClienteRequestDTO("Carlos Silva", "carlos@exemplo.com", senhaOriginal);

        ClienteResponseDTO response = clienteService.cadastrar(dto);
        assertThat(response.id()).isNotNull();

        Cliente salvo = clienteRepository.findById(response.id()).orElseThrow();
        assertThat(salvo.getSenhaHash())
                .isNotBlank()
                .isNotEqualTo(senhaOriginal)
                .startsWith("$2");

        assertThat(passwordEncoder.matches(senhaOriginal, salvo.getSenhaHash())).isTrue();
    }
}
