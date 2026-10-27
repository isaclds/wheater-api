package ifsc.edu.lll.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoSegundos;
    private final String emissor;

    public JwtService(@Value("${jwt.secret:}") String segredo,
                      @Value("${jwt.expiration-minutes:30}") long expiracaoMinutos,
                      @Value("${jwt.issuer:weather-api}") String emissor) {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 bytes");
        }
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.expiracaoSegundos = expiracaoMinutos * 60;
        this.emissor = emissor;
    }

    public String gerar(Long clienteId, String email) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .issuer(emissor)
                .subject(email)
                .claim("cid", clienteId)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expiracaoSegundos)))
                .signWith(chave)
                .compact();
    }

    public ClienteAutenticado validar(String token) {
        try {
            Claims c = Jwts.parser()
                    .verifyWith(chave)
                    .requireIssuer(emissor)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Number cid = c.get("cid", Number.class);
            if (cid == null) {
                throw new JwtException("claim cid ausente");
            }
            return new ClienteAutenticado(cid.longValue(), c.getSubject(), c.getExpiration().toInstant());
        } catch (ExpiredJwtException e) {
            throw new TokenInvalidoException(true, e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenInvalidoException(false, e);
        }
    }

    public long getExpiracaoSegundos() {
        return expiracaoSegundos;
    }
}
