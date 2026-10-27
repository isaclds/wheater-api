package ifsc.edu.lll.controller;

import ifsc.edu.lll.dto.auth.ClienteRequestDTO;
import ifsc.edu.lll.dto.auth.ClienteResponseDTO;
import ifsc.edu.lll.dto.auth.LoginRequestDTO;
import ifsc.edu.lll.dto.auth.TokenResponseDTO;
import ifsc.edu.lll.dto.response.ApiResponse;
import ifsc.edu.lll.service.AuthService;
import ifsc.edu.lll.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Cadastro de clientes e login")
public class AuthController {

    private final ClienteService clienteService;
    private final AuthService authService;

    public AuthController(ClienteService clienteService, AuthService authService) {
        this.clienteService = clienteService;
        this.authService = authService;
    }

    @PostMapping("/clientes")
    @Operation(summary = "Cadastrar cliente", description = "Cadastra um novo cliente para acesso à Weather API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Cliente cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "E-mail já cadastrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    public ResponseEntity<ApiResponse<ClienteResponseDTO>> cadastrar(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Cliente cadastrado com sucesso", clienteService.cadastrar(dto)));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar cliente", description = "Autentica um cliente com email e senha e retorna o token JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Autenticação realizada com sucesso",
                    content = @Content(schema = @Schema(implementation = TokenResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    public ResponseEntity<ApiResponse<TokenResponseDTO>> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(dto)));
    }
}
