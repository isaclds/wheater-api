package ifsc.edu.lll.security;

public class TokenInvalidoException extends RuntimeException {
    private final boolean expirado;

    public TokenInvalidoException(boolean expirado, Throwable causa) {
        super("Token inválido", causa);
        this.expirado = expirado;
    }

    public boolean isExpirado() {
        return expirado;
    }
}
