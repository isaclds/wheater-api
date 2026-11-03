package ifsc.edu.lll.controller;

import ifsc.edu.lll.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=segredo-muito-seguro-com-mais-de-32-bytes-para-os-testes",
        "jwt.expiration-minutes=30"
})
@DisplayName("AuthController — testes de integração para cadastro e login")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("Cenário 1: Dado e-mail novo, quando cadastrar, então 201 com id, nome, email e sem senha no corpo (CLI-01, CLI-06)")
    void cenario01_cadastrarClienteComSucesso() throws Exception {
        String json = """
                {
                    "nome": "Ana Souza",
                    "email": "ana@exemplo.com",
                    "senha": "umaSenhaForte123"
                }
                """;

        mockMvc.perform(post("/auth/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.title").value("Cliente cadastrado com sucesso"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.data.email").value("ana@exemplo.com"))
                .andExpect(jsonPath("$.data.senha").doesNotExist())
                .andExpect(jsonPath("$.data.senhaHash").doesNotExist())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("Cenário 2: Dado o mesmo e-mail em caixa diferente, quando cadastrar de novo, então 409 (CLI-03, CLI-04)")
    void cenario02_emailDuplicadoEmCaixaDiferenteRetorna409() throws Exception {
        String json1 = """
                {
                    "nome": "Ana Souza",
                    "email": "Ana@Exemplo.com",
                    "senha": "umaSenhaForte123"
                }
                """;

        mockMvc.perform(post("/auth/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json1))
                .andExpect(status().isCreated());

        String json2 = """
                {
                    "nome": "Ana Outra",
                    "email": "ana@exemplo.com",
                    "senha": "outraSenhaForte456"
                }
                """;

        mockMvc.perform(post("/auth/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.title").value("E-mail já cadastrado"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("Cenário 3: Dado senha com 7 caracteres, e-mail inválido ou nome vazio, quando cadastrar, então 422 listando os campos (CLI-02, CLI-07)")
    void cenario03_validacaoCamposInvalidosRetorna422() throws Exception {
        String jsonInvalido = """
                {
                    "nome": "",
                    "email": "email-invalido",
                    "senha": "curta"
                }
                """;

        mockMvc.perform(post("/auth/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.data", containsString("nome")))
                .andExpect(jsonPath("$.data", containsString("email")))
                .andExpect(jsonPath("$.data", containsString("senha")));
    }

    @Test
    @DisplayName("Cenário 5: Dado cliente cadastrado, quando logar com a senha certa, então 200 com accessToken, tokenType=Bearer e expiresIn=1800 (LOG-01)")
    void cenario05_loginComSucessoRetornaToken() throws Exception {
        String cadastroJson = """
                {
                    "nome": "Beatriz",
                    "email": "beatriz@exemplo.com",
                    "senha": "senhaValida123"
                }
                """;

        mockMvc.perform(post("/auth/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson))
                .andExpect(status().isCreated());

        String loginJson = """
                {
                    "email": "beatriz@exemplo.com",
                    "senha": "senhaValida123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(1800));
    }

    @Test
    @DisplayName("Cenário 6: Dado e-mail inexistente ou senha errada, quando logar, então 401 com o mesmo corpo nos dois casos (LOG-02, ERR-03)")
    void cenario06_credenciaisInvalidasRetornamMesmoCorpo401() throws Exception {
        // Cadastra cliente
        String cadastroJson = """
                {
                    "nome": "Beatriz",
                    "email": "beatriz@exemplo.com",
                    "senha": "senhaValida123"
                }
                """;
        mockMvc.perform(post("/auth/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson))
                .andExpect(status().isCreated());

        // 1. Email inexistente
        String loginInexistente = """
                {
                    "email": "inexistente@exemplo.com",
                    "senha": "senhaValida123"
                }
                """;
        MvcResult resInexistente = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginInexistente))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.title").value("Credenciais inválidas"))
                .andReturn();

        // 2. Senha errada
        String loginSenhaErrada = """
                {
                    "email": "beatriz@exemplo.com",
                    "senha": "senhaErrada123"
                }
                """;
        MvcResult resSenhaErrada = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginSenhaErrada))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.title").value("Credenciais inválidas"))
                .andReturn();

        assertThat(resInexistente.getResponse().getContentAsString())
                .isEqualTo(resSenhaErrada.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Cenário 15: Quando autenticar, então a resposta não traz Set-Cookie: JSESSIONID (SEC-08)")
    void cenario15_statelessNaoEmiteJsessionid() throws Exception {
        String cadastroJson = """
                {
                    "nome": "Daniel",
                    "email": "daniel@exemplo.com",
                    "senha": "senhaForte123"
                }
                """;
        mockMvc.perform(post("/auth/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson))
                .andExpect(status().isCreated());

        String loginJson = """
                {
                    "email": "daniel@exemplo.com",
                    "senha": "senhaForte123"
                }
                """;

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        if (setCookie != null) {
            assertThat(setCookie).doesNotContain("JSESSIONID");
        }
    }
}
