package ifsc.edu.lll.controller;

import ifsc.edu.lll.dto.response.ApiResponse;
import ifsc.edu.lll.model.Log;
import ifsc.edu.lll.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Verificação de disponibilidade da API")
public class HealthController {

    private final LogService logService;

    public HealthController(LogService logService){
        this.logService = logService;
    }

    @GetMapping("/ping")
    @Operation(summary = "Health check", description = "Retorna 200 se a API estiver disponível.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "API disponível"
            )
    })
    public ResponseEntity<ApiResponse<String>> ping() {
        int status = 200;
        logService.criarLog("/ping", status);
        return ResponseEntity.ok(ApiResponse.success("pong"));
    }
}