package ifsc.edu.lll.exception;

import ifsc.edu.lll.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Tratamento global de exceções da API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ApiResponse<Void>> emailDuplicado(EmailJaCadastradoException ex) {
        return json(HttpStatus.CONFLICT, "E-mail já cadastrado");
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ApiResponse<Void>> credenciais(CredenciaisInvalidasException ex) {
        return json(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    }

    @ExceptionHandler(CanalNaoHabilitadoException.class)
    public ResponseEntity<ApiResponse<Void>> canalNegado(CanalNaoHabilitadoException ex) {
        return json(HttpStatus.FORBIDDEN, "Canal não habilitado para este cliente");
    }

    @ExceptionHandler(LimiteConexoesException.class)
    public ResponseEntity<ApiResponse<Void>> limiteConexoes(LimiteConexoesException ex) {
        return json(HttpStatus.TOO_MANY_REQUESTS, "Muitas requisições");
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> parametroInvalido(Exception ex) {
        return json(HttpStatus.BAD_REQUEST, "Parâmetro ausente ou em formato inválido");
    }

    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> validacaoParametro(Exception ex) {
        return json(HttpStatus.UNPROCESSABLE_ENTITY, "Dados inválidos");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> acessoNegado(AccessDeniedException ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return json(HttpStatus.FORBIDDEN, "Acesso negado");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> naoAutenticado(AuthenticationException ex) {
        return json(HttpStatus.UNAUTHORIZED, "Não autenticado");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidation(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(422, "Dados inválidos", mensagem));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(404, "Recurso não encontrado", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneric(Exception ex) {
        log.error("Erro interno não tratado", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(500, "Erro interno do servidor",
                        "Entre em contato com o suporte técnico."));
    }

    private ResponseEntity<ApiResponse<Void>> json(HttpStatus status, String titulo) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(status.value(), titulo, null));
    }
}
