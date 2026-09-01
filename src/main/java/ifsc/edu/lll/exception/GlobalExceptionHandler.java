package ifsc.edu.lll.exception;

import ifsc.edu.lll.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneric(Exception e, WebRequest request) {
        ApiResponse error = ApiResponse.error(
                400,
                "Erro ao registrar avaliação",
                e.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }
}
