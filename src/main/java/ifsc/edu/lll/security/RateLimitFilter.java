package ifsc.edu.lll.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int DEFAULT_LIMITE = 100;
    private static final long DEFAULT_JANELA_MS = 60_000;
    private static final int LIMPEZA_THRESHOLD = 500;

    private final int limite;
    private final long janelaMs;
    private final boolean habilitado;

    private record Janela(long inicio, int contagem) {}

    private final ConcurrentHashMap<String, Janela> requisicoes = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        this(DEFAULT_LIMITE, DEFAULT_JANELA_MS, true);
    }

    public RateLimitFilter(int limite, long janelaMs) {
        this(limite, janelaMs, true);
    }

    public RateLimitFilter(int limite, long janelaMs, boolean habilitado) {
        this.limite = limite;
        this.janelaMs = janelaMs;
        this.habilitado = habilitado;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!habilitado) {
            return true;
        }
        String path = request.getServletPath();
        return "/ping".equals(path)
                || path.startsWith("/docs")
                || path.startsWith("/docs-json")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = extrairIp(request);
        long agora = System.currentTimeMillis();

        if (requisicoes.size() > LIMPEZA_THRESHOLD) {
            limparExpirados(agora);
        }

        Janela janela = requisicoes.compute(ip, (k, v) -> {
            if (v == null || agora - v.inicio() >= janelaMs) {
                return new Janela(agora, 1);
            }
            return new Janela(v.inicio(), v.contagem() + 1);
        });

        if (janela.contagem() > limite) {
            long tempoRestanteSegundos = Math.max(1, (janela.inicio() + janelaMs - agora) / 1000);
            log.warn("Rate limit excedido para o IP: {}. Limite: {} req / {}ms", ip, limite, janelaMs);

            response.setHeader("Retry-After", String.valueOf(tempoRestanteSegundos));
            response.setHeader("X-RateLimit-Limit", String.valueOf(limite));
            response.setHeader("X-RateLimit-Remaining", "0");

            RespostaJson.erro(response, 429, "Limite de requisições excedido. Tente novamente mais tarde.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void limparExpirados(long agora) {
        requisicoes.entrySet().removeIf(entry -> agora - entry.getValue().inicio() >= janelaMs);
    }

    private String extrairIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "desconhecido";
    }

    int getLimite() {
        return limite;
    }

    long getJanelaMs() {
        return janelaMs;
    }

    boolean isHabilitado() {
        return habilitado;
    }

    void reset() {
        requisicoes.clear();
    }
}
