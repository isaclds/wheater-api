package ifsc.edu.lll.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=segredo-muito-seguro-com-mais-de-32-bytes-para-os-testes",
        "jwt.expiration-minutes=30"
})
@DisplayName("SecurityConfig — testes de integração para proteção de rotas")
class SecurityIntegrationTest {

    private static final String SEGREDO = "segredo-muito-seguro-com-mais-de-32-bytes-para-os-testes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("Cenário 10: Quando chamar rotas públicas sem token, então não é 401 (SEC-01)")
    void cenario10_rotasPublicasAcessiveisSemToken() throws Exception {
        mockMvc.perform(get("/ping"))
                .andExpect(status().is(not(401)));

        mockMvc.perform(get("/previsao/BR"))
                .andExpect(status().is(not(401)));

        mockMvc.perform(get("/docs"))
                .andExpect(status().is(not(401)));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is(not(401)));
    }

    @Test
    @DisplayName("Cenário 11: Quando chamar /alertas sem token, então 401 com WWW-Authenticate: Bearer e corpo no envelope (SEC-02, SEC-05, ERR-01, ERR-04)")
    void cenario11_rotasDeAlertasSemTokenRetornam401() throws Exception {
        mockMvc.perform(get("/alertas"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.title").value("Não autenticado"))
                .andExpect(jsonPath("$.data").doesNotExist());

        mockMvc.perform(post("/alertas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/alertas/stream?canalId=1"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Cenário 12: Dado header Basic, token malformado ou token na URL, quando chamar rota protegida, então 401 (SEC-06, SEC-07)")
    void cenario12_headerInvalidoOuTokenNaUrlRetorna401() throws Exception {
        String tokenValido = jwtService.gerar(1L, "teste@exemplo.com");

        // 1. Header Basic
        mockMvc.perform(get("/alertas")
                        .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));

        // 2. Token malformado
        mockMvc.perform(get("/alertas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.malformado"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.title").value("Não autenticado"));

        // 3. Token passado na query string
        mockMvc.perform(get("/alertas?token=" + tokenValido))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cenário 13: Dado token expirado, quando chamar rota protegida, então 401 com título 'Token expirado' (SEC-06)")
    void cenario13_tokenExpiradoRetorna401ComTituloExpirado() throws Exception {
        SecretKey chave = Keys.hmacShaKeyFor(SEGREDO.getBytes(StandardCharsets.UTF_8));
        String tokenExpirado = Jwts.builder()
                .issuer("weather-api")
                .subject("ana@exemplo.com")
                .claim("cid", 1L)
                .issuedAt(Date.from(Instant.now().minusSeconds(3600)))
                .expiration(Date.from(Instant.now().minusSeconds(1800)))
                .signWith(chave)
                .compact();

        mockMvc.perform(get("/alertas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenExpirado))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.title").value("Token expirado"));
    }

    @Test
    @DisplayName("Cenário 14: Quando chamar uma rota qualquer fora da lista pública, então exige token (SEC-03)")
    void cenario14_qualquerRotaDesconhecidaExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/rota-inexistente-qualquer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cenário 29: Dado perfil sem dev, quando acessar /h2-console, então não responde (OPS-02)")
    void cenario29_h2ConsoleDesabilitadoSemPerfilDev() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().is(not(200)));
    }
}
