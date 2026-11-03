package ifsc.edu.lll.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

final class RespostaJson {
    private RespostaJson() {}

    static void erro(HttpServletResponse res, int status, String titulo) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"status\":" + status + ",\"success\":false,\"title\":\""
                + titulo + "\",\"data\":null}");
    }
}
