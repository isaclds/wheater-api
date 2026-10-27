package ifsc.edu.lll.exception;

public class CanalNaoHabilitadoException extends RuntimeException {
    public CanalNaoHabilitadoException() {
        super("Canal não habilitado para este cliente");
    }
}
