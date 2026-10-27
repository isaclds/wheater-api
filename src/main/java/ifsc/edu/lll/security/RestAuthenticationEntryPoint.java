package ifsc.edu.lll.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res,
                         AuthenticationException ex) throws IOException {
        boolean expirado = "expirado".equals(req.getAttribute(JwtAuthenticationFilter.ATRIBUTO_ERRO));
        res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        RespostaJson.erro(res, 401, expirado ? "Token expirado" : "Não autenticado");
    }
}
