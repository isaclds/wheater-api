package ifsc.edu.lll.controller;

import ifsc.edu.lll.dto.alerta.AlertaRequestDTO;
import ifsc.edu.lll.dto.alerta.AlertaResponseDTO;
import ifsc.edu.lll.dto.response.ApiResponse;
import ifsc.edu.lll.exception.CanalNaoHabilitadoException;
import ifsc.edu.lll.security.ClienteAutenticado;
import ifsc.edu.lll.service.AlertaService;
import ifsc.edu.lll.service.AlertaStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/alertas")
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Alertas", description = "Cadastro e monitoramento de alertas climáticos com notificação em tempo real (SSE)")
public class AlertaController {

    private final AlertaService alertaService;
    private final AlertaStreamService stream;

    public AlertaController(AlertaService alertaService, AlertaStreamService stream) {
        this.alertaService = alertaService;
        this.stream = stream;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar alerta climático",
            description = "Cria um novo alerta que dispara notificações SSE quando as condições climáticas "
                    + "da cidade monitorada ultrapassam os limites definidos."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Alerta cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = AlertaResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "Dados de entrada inválidos"
            )
    })
    public ResponseEntity<ApiResponse<AlertaResponseDTO>> cadastrar(
            @AuthenticationPrincipal ClienteAutenticado cliente,
            @Valid @RequestBody AlertaRequestDTO dto) {
        AlertaResponseDTO alerta = alertaService.cadastrar(cliente.id(), dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Alerta cadastrado com sucesso", alerta));
    }

    @GetMapping
    @Operation(summary = "Listar alertas ativos", description = "Retorna todos os alertas climáticos atualmente ativos.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lista de alertas ativos"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            )
    })
    public ResponseEntity<ApiResponse<List<AlertaResponseDTO>>> listar(
            @AuthenticationPrincipal ClienteAutenticado cliente) {
        return ResponseEntity.ok(ApiResponse.success(alertaService.listarAtivos(cliente.id())));
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Assinar stream de alertas (SSE)",
            description = "Abre uma conexão Server-Sent Events. O servidor envia um evento 'alerta-disparado' "
                    + "sempre que as condições climáticas do canal atingirem os limites configurados."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Stream SSE aberto com sucesso"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Parâmetro ausente ou em formato inválido"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Canal não habilitado para este cliente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "Dados inválidos"
            )
    })
    public SseEmitter assinar(
            @AuthenticationPrincipal ClienteAutenticado cliente,
            @Parameter(description = "Identificador do canal do cliente", required = true, example = "42")
            @RequestParam @Positive Long canalId) {
        if (!alertaService.canalHabilitado(cliente.id(), canalId)) {
            throw new CanalNaoHabilitadoException();
        }
        return stream.registrar(cliente.id(), canalId, cliente.expiraEm());
    }
}
