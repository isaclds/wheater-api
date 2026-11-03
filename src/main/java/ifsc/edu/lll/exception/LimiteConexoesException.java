package ifsc.edu.lll.exception;

public class LimiteConexoesException extends RuntimeException {
    public LimiteConexoesException() {
        super("Muitas requisições");
    }
}
