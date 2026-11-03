package ifsc.edu.lll.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RateLimitFilter — testes unitários para controle de taxa de requisições")
class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        // Limite de 2 requisições por janela de 200ms para testes rápidos
        filter = new RateLimitFilter(2, 200, true);
    }

    @Test
    @DisplayName("Requisições dentro do limite passam normalmente pelo filtro")
    void dentroDoLimite_permiteRequisicao() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/previsao/BR");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("Requisições que excedem o limite retornam HTTP 429 com envelope padronizado")
    void excedeuLimite_retorna429ComEnvelope() throws Exception {
        String ip = "10.0.0.2";

        // 1ª requisição - OK
        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/previsao/BR");
        req1.setRemoteAddr(ip);
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilter(req1, res1, new MockFilterChain());
        assertThat(res1.getStatus()).isEqualTo(200);

        // 2ª requisição - OK
        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/previsao/BR");
        req2.setRemoteAddr(ip);
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilter(req2, res2, new MockFilterChain());
        assertThat(res2.getStatus()).isEqualTo(200);

        // 3ª requisição - Bloqueada com 429
        MockHttpServletRequest req3 = new MockHttpServletRequest("GET", "/previsao/BR");
        req3.setRemoteAddr(ip);
        MockHttpServletResponse res3 = new MockHttpServletResponse();
        MockFilterChain chain3 = new MockFilterChain();
        filter.doFilter(req3, res3, chain3);

        assertThat(res3.getStatus()).isEqualTo(429);
        assertThat(res3.getHeader("Retry-After")).isNotNull();
        assertThat(res3.getHeader("X-RateLimit-Limit")).isEqualTo("2");
        assertThat(res3.getHeader("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(res3.getContentType()).contains("application/json");

        String body = res3.getContentAsString();
        assertThat(body).contains("\"status\":429");
        assertThat(body).contains("\"success\":false");
        assertThat(body).contains("\"title\":\"Limite de requisições excedido. Tente novamente mais tarde.\"");
        assertThat(body).contains("\"data\":null");
        assertThat(chain3.getRequest()).isNull(); // Não deve ter prosseguido na cadeia
    }

    @Test
    @DisplayName("IPs diferentes possuem contadores isolados")
    void ipsDiferentes_saoIsolados() throws Exception {
        // IP A esgota suas 2 chamadas
        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest reqA = new MockHttpServletRequest("GET", "/previsao/BR");
            reqA.setRemoteAddr("192.168.1.100");
            filter.doFilter(reqA, new MockHttpServletResponse(), new MockFilterChain());
        }

        // IP A é bloqueado na 3ª
        MockHttpServletRequest reqA3 = new MockHttpServletRequest("GET", "/previsao/BR");
        reqA3.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse resA3 = new MockHttpServletResponse();
        filter.doFilter(reqA3, resA3, new MockFilterChain());
        assertThat(resA3.getStatus()).isEqualTo(429);

        // IP B faz a 1ª requisição e é aceito com sucesso
        MockHttpServletRequest reqB = new MockHttpServletRequest("GET", "/previsao/BR");
        reqB.setRemoteAddr("192.168.1.200");
        MockHttpServletResponse resB = new MockHttpServletResponse();
        MockFilterChain chainB = new MockFilterChain();
        filter.doFilter(reqB, resB, chainB);
        assertThat(resB.getStatus()).isEqualTo(200);
        assertThat(chainB.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("Identifica cliente via cabeçalho X-Forwarded-For")
    void xForwardedFor_utilizaIpDoCliente() throws Exception {
        String clientIp = "203.0.113.195";

        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/previsao/BR");
            req.setRemoteAddr("10.0.0.1"); // IP do proxy
            req.addHeader("X-Forwarded-For", clientIp + ", 10.0.0.1");
            filter.doFilter(req, new MockHttpServletResponse(), new MockFilterChain());
        }

        MockHttpServletRequest req3 = new MockHttpServletRequest("GET", "/previsao/BR");
        req3.setRemoteAddr("10.0.0.1");
        req3.addHeader("X-Forwarded-For", clientIp + ", 10.0.0.1");
        MockHttpServletResponse res3 = new MockHttpServletResponse();
        filter.doFilter(req3, res3, new MockFilterChain());

        assertThat(res3.getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("Expiração da janela reinicia a contagem permitindo novas requisições")
    void expiracaoJanela_reiniciaContagem() throws Exception {
        String ip = "10.0.0.3";

        // Esgota limite de 2 chamadas
        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/previsao/BR");
            req.setRemoteAddr(ip);
            filter.doFilter(req, new MockHttpServletResponse(), new MockFilterChain());
        }

        // Aguarda a janela de 200ms expirar
        Thread.sleep(250);

        // Nova requisição deve ser aceita
        MockHttpServletRequest reqNovo = new MockHttpServletRequest("GET", "/previsao/BR");
        reqNovo.setRemoteAddr(ip);
        MockHttpServletResponse resNovo = new MockHttpServletResponse();
        MockFilterChain chainNovo = new MockFilterChain();
        filter.doFilter(reqNovo, resNovo, chainNovo);

        assertThat(resNovo.getStatus()).isEqualTo(200);
        assertThat(chainNovo.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("Rotas públicas de infraestrutura (ping, docs) não são filtradas")
    void rotasInfra_saoIgnoradas() {
        MockHttpServletRequest pingReq = new MockHttpServletRequest("GET", "/ping");
        pingReq.setServletPath("/ping");
        assertThat(filter.shouldNotFilter(pingReq)).isTrue();

        MockHttpServletRequest docsReq = new MockHttpServletRequest("GET", "/docs");
        docsReq.setServletPath("/docs");
        assertThat(filter.shouldNotFilter(docsReq)).isTrue();

        MockHttpServletRequest swaggerReq = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        swaggerReq.setServletPath("/swagger-ui/index.html");
        assertThat(filter.shouldNotFilter(swaggerReq)).isTrue();

        MockHttpServletRequest apiReq = new MockHttpServletRequest("GET", "/previsao/BR");
        apiReq.setServletPath("/previsao/BR");
        assertThat(filter.shouldNotFilter(apiReq)).isFalse();
    }

    @Test
    @DisplayName("Quando desabilitado, todas as requisições passam")
    void desabilitado_ignoraFiltro() throws Exception {
        RateLimitFilter desabilitado = new RateLimitFilter(1, 1000, false);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/previsao/BR");
        req.setServletPath("/previsao/BR");

        assertThat(desabilitado.shouldNotFilter(req)).isTrue();
    }
}
