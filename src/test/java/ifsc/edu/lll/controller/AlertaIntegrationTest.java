package ifsc.edu.lll.controller;

import ifsc.edu.lll.model.Alerta;
import ifsc.edu.lll.repository.AlertaRepository;
import ifsc.edu.lll.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=segredo-muito-seguro-com-mais-de-32-bytes-para-os-testes",
        "jwt.expiration-minutes=30"
})
@DisplayName("AlertaController — testes de integração para alertas e canais SSE")
class AlertaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        alertaRepository.deleteAll();
    }

    @Test
    @DisplayName("Cenário 16: Dado cliente A com canalId 42, quando A cadastrar alerta, então o dono é A mesmo que o corpo traga outro dono (ALR-01, ALR-02)")
    void cenario16_donoDoAlertaVemSempreDoToken() throws Exception {
        Long clienteA = 10L;
        String tokenA = jwtService.gerar(clienteA, "clienteA@exemplo.com");

        String json = """
                {
                    "canalId": 42,
                    "pais": "Brasil",
                    "cidade": "Florianópolis",
                    "temperaturaMaxima": 35.0,
                    "precipitacao": 20.0,
                    "clienteId": 9999
                }
                """;

        mockMvc.perform(post("/alertas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.canalId").value(42));

        Alerta alertaNoBanco = alertaRepository.findAll().getFirst();
        assertThat(alertaNoBanco.getClienteId()).isEqualTo(clienteA);
    }

    @Test
    @DisplayName("Cenário 17: Dado alertas de A e de B, quando A listar, então vê só os de A (ALR-03)")
    void cenario17_listagemMostraApenasAlertasDoClienteAutenticado() throws Exception {
        Long clienteA = 10L;
        Long clienteB = 20L;
        String tokenA = jwtService.gerar(clienteA, "clienteA@exemplo.com");

        alertaRepository.save(new Alerta(clienteA, 1L, "Brasil", "Florianópolis", 30.0, null, null));
        alertaRepository.save(new Alerta(clienteA, 2L, "Brasil", "Joinville", 28.0, null, null));
        alertaRepository.save(new Alerta(clienteB, 1L, "Brasil", "Curitiba", 25.0, null, null));

        mockMvc.perform(get("/alertas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].cidade").value("Florianópolis"))
                .andExpect(jsonPath("$.data[1].cidade").value("Joinville"));
    }

    @Test
    @DisplayName("Cenário 18: Dado A e B com alertas no canal 42, quando ambos abrem stream, então os dois são aceitos (ALR-04, ALR-05)")
    void cenario18_mesmoCanalAceitaClientesDiferentes() throws Exception {
        Long clienteA = 10L;
        Long clienteB = 20L;
        String tokenA = jwtService.gerar(clienteA, "clienteA@exemplo.com");
        String tokenB = jwtService.gerar(clienteB, "clienteB@exemplo.com");

        alertaRepository.save(new Alerta(clienteA, 42L, "Brasil", "Florianópolis", 30.0, null, null));
        alertaRepository.save(new Alerta(clienteB, 42L, "Brasil", "Curitiba", 25.0, null, null));

        mockMvc.perform(get("/alertas/stream?canalId=42")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

        mockMvc.perform(get("/alertas/stream?canalId=42")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    @Test
    @DisplayName("Cenário 21: Dado A sem alerta no canal 7, quando A abrir stream canal 7, então 403 em JSON; mesma resposta se for de B (SSE-02)")
    void cenario21_canalNaoHabilitadoRetorna403Json() throws Exception {
        Long clienteA = 10L;
        Long clienteB = 20L;
        String tokenA = jwtService.gerar(clienteA, "clienteA@exemplo.com");

        // Canal 7 pertence a B
        alertaRepository.save(new Alerta(clienteB, 7L, "Brasil", "São Paulo", 32.0, null, null));

        // Cliente A tenta abrir canal 7 (de B)
        mockMvc.perform(get("/alertas/stream?canalId=7")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.title").value("Canal não habilitado para este cliente"))
                .andExpect(jsonPath("$.data").doesNotExist());

        // Cliente A tenta abrir canal 999 (inexistente)
        mockMvc.perform(get("/alertas/stream?canalId=999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Canal não habilitado para este cliente"));
    }

    @Test
    @DisplayName("Cenário 22: Quando abrir stream sem canalId, com texto ou com valor 0, então 400, 400 e 422 (SSE-03)")
    void cenario22_validacaoCanalIdNoStream() throws Exception {
        String token = jwtService.gerar(1L, "ana@exemplo.com");

        // 1. Sem canalId -> 400
        mockMvc.perform(get("/alertas/stream")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Parâmetro ausente ou em formato inválido"));

        // 2. Com texto -> 400
        mockMvc.perform(get("/alertas/stream?canalId=textoInvalido")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Parâmetro ausente ou em formato inválido"));

        // 3. Com valor 0 -> 422 (@Positive)
        mockMvc.perform(get("/alertas/stream?canalId=0")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.title").value("Dados inválidos"));
    }

    @Test
    @DisplayName("Cenário 23: Quando o stream abrir, então o primeiro evento é 'conectado' com o canalId (SSE-01, SSE-05)")
    void cenario23_streamAbertoEmiteEventoConectado() throws Exception {
        Long clienteId = 1L;
        String token = jwtService.gerar(clienteId, "ana@exemplo.com");

        alertaRepository.save(new Alerta(clienteId, 42L, "Brasil", "Florianópolis", 30.0, null, null));

        MvcResult result = mockMvc.perform(get("/alertas/stream?canalId=42")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("event:conectado");
        assertThat(body).contains("{\"canalId\":42}");
    }
}
