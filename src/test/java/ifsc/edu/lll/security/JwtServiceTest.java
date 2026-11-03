package ifsc.edu.lll.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService — geração e validação de tokens JWT (HS256)")
class JwtServiceTest {

    private static final String SEGREDO_VALIDO = "segredo-super-seguro-com-mais-de-32-bytes-para-testes";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SEGREDO_VALIDO, 30, "weather-api");
    }

    @Test
    @DisplayName("Cenário 7: Dado token gerado, quando decodificar, então tem sub, cid, iss, iat, exp e não tem senha (JWT-04 a JWT-06)")
    void cenario07_tokenGeradoContemClaimsCorretasSemSenha() {
        Long clienteId = 42L;
        String email = "ana@exemplo.com";

        String token = jwtService.gerar(clienteId, email);
        assertThat(token).isNotBlank();

        SecretKey chave = Keys.hmacShaKeyFor(SEGREDO_VALIDO.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(email);
        assertThat(claims.get("cid", Number.class).longValue()).isEqualTo(clienteId);
        assertThat(claims.getIssuer()).isEqualTo("weather-api");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();

        assertThat(claims.containsKey("senha")).isFalse();
        assertThat(claims.containsKey("senhaHash")).isFalse();
        assertThat(claims.containsKey("password")).isFalse();
    }

    @Test
    @DisplayName("Cenário 8: Dado JWT_SECRET com 31 bytes ou menos, quando inicializar, então falha com mensagem clara (JWT-03)")
    void cenario08_falhaComSegredoCurtoOuAusente() {
        String segredo31Bytes = "1234567890123456789012345678901"; // 31 bytes
        assertThat(segredo31Bytes.getBytes(StandardCharsets.UTF_8).length).isEqualTo(31);

        assertThatThrownBy(() -> new JwtService(segredo31Bytes, 30, "weather-api"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET precisa ter pelo menos 32 bytes");

        assertThatThrownBy(() -> new JwtService("", 30, "weather-api"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET precisa ter pelo menos 32 bytes");

        assertThatThrownBy(() -> new JwtService(null, 30, "weather-api"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET precisa ter pelo menos 32 bytes");
    }

    @Test
    @DisplayName("Cenário 9: Dado token adulterado, de outro segredo, de outro emissor ou expirado, quando validar, então TokenInvalidoException (JWT-07)")
    void cenario09_validacaoFalhaComTokensInvalidos() {
        // 1. Token de outro segredo
        SecretKey outroSegredo = Keys.hmacShaKeyFor("outro-segredo-com-mais-de-32-bytes-diferente-123".getBytes(StandardCharsets.UTF_8));
        String tokenOutroSegredo = Jwts.builder()
                .issuer("weather-api")
                .subject("ana@exemplo.com")
                .claim("cid", 1L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(outroSegredo)
                .compact();

        assertThatThrownBy(() -> jwtService.validar(tokenOutroSegredo))
                .isInstanceOf(TokenInvalidoException.class)
                .satisfies(ex -> assertThat(((TokenInvalidoException) ex).isExpirado()).isFalse());

        // 2. Token de outro emissor
        SecretKey chave = Keys.hmacShaKeyFor(SEGREDO_VALIDO.getBytes(StandardCharsets.UTF_8));
        String tokenOutroEmissor = Jwts.builder()
                .issuer("outro-emissor")
                .subject("ana@exemplo.com")
                .claim("cid", 1L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(chave)
                .compact();

        assertThatThrownBy(() -> jwtService.validar(tokenOutroEmissor))
                .isInstanceOf(TokenInvalidoException.class)
                .satisfies(ex -> assertThat(((TokenInvalidoException) ex).isExpirado()).isFalse());

        // 3. Token adulterado
        String tokenValido = jwtService.gerar(1L, "ana@exemplo.com");
        String tokenAdulterado = tokenValido.substring(0, tokenValido.length() - 5) + "abcde";

        assertThatThrownBy(() -> jwtService.validar(tokenAdulterado))
                .isInstanceOf(TokenInvalidoException.class)
                .satisfies(ex -> assertThat(((TokenInvalidoException) ex).isExpirado()).isFalse());

        // 4. Token expirado
        String tokenExpirado = Jwts.builder()
                .issuer("weather-api")
                .subject("ana@exemplo.com")
                .claim("cid", 1L)
                .issuedAt(Date.from(Instant.now().minusSeconds(3600)))
                .expiration(Date.from(Instant.now().minusSeconds(1800)))
                .signWith(chave)
                .compact();

        assertThatThrownBy(() -> jwtService.validar(tokenExpirado))
                .isInstanceOf(TokenInvalidoException.class)
                .satisfies(ex -> assertThat(((TokenInvalidoException) ex).isExpirado()).isTrue());
    }
}
