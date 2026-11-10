package ifsc.edu.lll.controller;

import ifsc.edu.lll.dto.alerta.AlertaRequestDTO;
import ifsc.edu.lll.dto.log.LogRequestDTO;
import ifsc.edu.lll.dto.response.ApiResponse;
import ifsc.edu.lll.model.Log;
import ifsc.edu.lll.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logs")
@Tag(name = "Logs", description = "Logs de endpoints")
public class LogController {

    private final String logEncontrado = "Logs encontrados";
    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    @Operation(summary = "Busca logs", description = "Busca todos os logs registrados no banco.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = logEncontrado
            )
    })
    public ResponseEntity<ApiResponse<List<Log>>> buscaLogs() {
        return  ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(200, logEncontrado, logService.buscaLog()));
    }

    @GetMapping("/{status}")
    @Operation(summary = "Busca logs por status", description = "Busca todos os logs registrados no banco com determinado status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = logEncontrado
            )
    })
    public ResponseEntity<ApiResponse<List<Log>>> buscaLogsPorStatus(
            @Parameter(description = "Busca log por status", example = "200", required = true)
            @PathVariable String status
    ) {
        int statusInt = Integer.parseInt(status);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(201, logEncontrado, logService.buscaLogPorStatus(statusInt)));
    }

    @GetMapping("/{endpoint}")
    @Operation(summary = "Busca logs por endpoint", description = "Busca todos os logs registrados no banco com determinado endpoint.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = logEncontrado
            )
    })
    public ResponseEntity<ApiResponse<List<Log>>> buscaLogPorEndpoint(
            @Parameter(description = "Endpoint desejado", example = "/auth/clientes", required = true)
            @PathVariable String endpoint
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(201, logEncontrado, logService.buscaLogPorEndpoint("/" + endpoint)));
    }

    @GetMapping("/data")
    @Operation(summary = "Busca logs entre duas datas", description = "Busca todos os logs registrados no intervalo de duas datas.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = logEncontrado
            )
    })
    public ResponseEntity<ApiResponse<List<Log>>> buscaLogPorData(
            @RequestBody LogRequestDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(201, logEncontrado, logService.buscaLogPorIntervalo(dto)));
    }
}
