package ifsc.edu.lll.security;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ifsc.edu.lll.repository.ClienteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=segredo-muito-seguro-com-mais-de-32-bytes-para-os-testes",
        "jwt.expiration-minutes=30"
})
@DisplayName("Auditoria de Logs — verificação de não vazamento de senhas, hashes e tokens (OPS-01, CLI-06)")
class AuditoriaLogsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger rootLogger;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();
        rootLogger = (Logger) LoggerFactory.getLogger("ifsc.edu.lll");
        listAppender = new ListAppender<>();
        listAppender.start();
        rootLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        if (rootLogger != null && listAppender != null) {
            rootLogger.detachAppender(listAppender);
        }
    }

    @Test
    @DisplayName("Cenário 30: Dado cadastro, login ok, login falho e acesso negado, logs não contêm senha, hash ou token (OPS-01, CLI-06)")
    void cenario30_logsNaoVazamDadosSensiveis() throws Exception {
        String senhaSegura = "SenhaSegura123!";
        String senhaErrada = "SenhaErrada999!";

        // 1. Cadastro
        String cadastroJson = String.format("""
                {
                    "nome": "Marcos Teste",
                    "email": "marcos@exemplo.com",
                    "senha": "%s"
                }
                """, senhaSegura);

        mockMvc.perform(post("/auth/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson))
                .andExpect(status().isCreated());

        // 2. Login com sucesso
        String loginOkJson = String.format("""
                {
                    "email": "marcos@exemplo.com",
                    "senha": "%s"
                }
                """, senhaSegura);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginOkJson))
                .andExpect(status().isOk())
                .andReturn();

        String token = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.accessToken");

        // 3. Login com falha
        String loginFalhaJson = String.format("""
                {
                    "email": "marcos@exemplo.com",
                    "senha": "%s"
                }
                """, senhaErrada);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginFalhaJson))
                .andExpect(status().isUnauthorized());

        // 4. Acesso negado (Canal 999 não habilitado)
        mockMvc.perform(get("/alertas/stream?canalId=999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());

        // Verificação dos logs
        var eventos = listAppender.list;
        assertThat(eventos).isNotEmpty();

        boolean teveCadastro = false;
        boolean teveLoginSucesso = false;
        boolean teveLoginFalha = false;
        boolean teveAcessoNegado = false;

        for (ILoggingEvent evento : eventos) {
            String mensagem = evento.getFormattedMessage();

            // Nunca deve conter senha pura
            assertThat(mensagem).doesNotContain(senhaSegura);
            assertThat(mensagem).doesNotContain(senhaErrada);

            // Nunca deve conter hash BCrypt
            assertThat(mensagem).doesNotContain("$2a$");
            assertThat(mensagem).doesNotContain("$2b$");

            // Nunca deve conter o token JWT
            if (token != null) {
                assertThat(mensagem).doesNotContain(token);
            }

            if (mensagem.contains("Cliente cadastrado")) teveCadastro = true;
            if (mensagem.contains("Login realizado com sucesso")) teveLoginSucesso = true;
            if (mensagem.contains("Falha no login")) teveLoginFalha = true;
            if (mensagem.contains("Canal não habilitado") || mensagem.contains("Acesso negado")) teveAcessoNegado = true;
        }

        assertThat(teveCadastro).isTrue();
        assertThat(teveLoginSucesso).isTrue();
        assertThat(teveLoginFalha).isTrue();
    }
}
