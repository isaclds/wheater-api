package ifsc.edu.lll.controller;

import ifsc.edu.lll.dto.geocoding.Localizacao;
import ifsc.edu.lll.dto.response.ApiResponse;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.service.GeolocalizacaoService;
import ifsc.edu.lll.service.PrevisaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/previsao")
@Tag(name = "Previsão do Tempo", description = "Consulta de dados climáticos por localidade (futuro via Open-Meteo, histórico via NASA POWER)")
public class PrevisaoController {

    private final PrevisaoService previsaoService;
    private final GeolocalizacaoService geolocalizacaoService;

    public PrevisaoController(PrevisaoService previsaoService, GeolocalizacaoService geolocalizacaoService) {
        this.previsaoService = previsaoService;
        this.geolocalizacaoService = geolocalizacaoService;
    }

    @GetMapping("/{pais}")
    @Operation(
            summary = "Previsão por país",
            description = "Retorna a previsão do tempo para as principais cidades de referência do país informado. "
                    + "Aceita nome completo (ex: 'Brasil') ou código ISO-2 (ex: 'BR')."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Previsão retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosClimaticos.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "País não encontrado ou sem localidades cadastradas"
            )
    })
    public ResponseEntity<ApiResponse<List<DadosClimaticos>>> previsaoPorPais(
            @Parameter(description = "Nome ou código ISO-2 do país", example = "Brasil", required = true)
            @PathVariable String pais,
            @Parameter(description = "Data no formato ISO-8601 (yyyy-MM-dd). Padrão: hoje.", example = "2026-10-09")
            @RequestParam(required = false) String data) {
        List<DadosClimaticos> climas = previsaoService.buscaPrevisaoPorPais(pais, parseData(data));
        return ResponseEntity.ok(ApiResponse.success(climas));
    }

    @GetMapping("/{pais}/{estado}")
    @Operation(
            summary = "Previsão por estado",
            description = "Retorna a previsão do tempo para as principais cidades de referência do estado informado."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Previsão retornada com sucesso"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Estado não encontrado ou sem localidades cadastradas"
            )
    })
    public ResponseEntity<ApiResponse<List<DadosClimaticos>>> previsaoPorPaisEstado(
            @Parameter(description = "Nome ou código ISO-2 do país", example = "Brasil", required = true)
            @PathVariable String pais,
            @Parameter(description = "Nome ou sigla do estado", example = "SC", required = true)
            @PathVariable String estado,
            @Parameter(description = "Data no formato ISO-8601 (yyyy-MM-dd). Padrão: hoje.", example = "2026-10-09")
            @RequestParam(required = false) String data) {
        List<DadosClimaticos> climas = previsaoService.buscaPrevisaoPorEstado(pais, estado, parseData(data));
        return ResponseEntity.ok(ApiResponse.success(climas));
    }

    @GetMapping("/{pais}/{estado}/{cidade}")
    @Operation(
            summary = "Previsão por cidade",
            description = "Retorna a previsão do tempo para uma cidade específica. Para datas passadas, os dados "
                    + "são obtidos do NASA POWER. Para hoje ou datas futuras, os dados vêm do Open-Meteo."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Previsão retornada com sucesso"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Cidade não encontrada ou previsão indisponível para a data"
            )
    })
    public ResponseEntity<ApiResponse<DadosClimaticos>> previsaoPorPaisEstadoCidade(
            @Parameter(description = "Nome ou código ISO-2 do país", example = "Brasil", required = true)
            @PathVariable String pais,
            @Parameter(description = "Nome ou sigla do estado", example = "SC", required = true)
            @PathVariable String estado,
            @Parameter(description = "Nome da cidade", example = "Florianópolis", required = true)
            @PathVariable String cidade,
            @Parameter(description = "Data no formato ISO-8601 (yyyy-MM-dd). Padrão: hoje.", example = "2026-10-09")
            @RequestParam(required = false) String data) {
        DadosClimaticos clima = previsaoService.buscaPrevisaoPorCidade(pais, cidade, parseData(data));
        return ResponseEntity.ok(ApiResponse.success(clima));
    }

    @GetMapping("/aqui")
    @Operation(
            summary = "Previsão pela localização automática (IP)",
            description = "Detecta a localização do cliente pelo IP da requisição e retorna a previsão do dia."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Previsão retornada com sucesso"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Não foi possível determinar a localização pelo IP"
            )
    })
    public ResponseEntity<ApiResponse<DadosClimaticos>> meuLocal(HttpServletRequest request) {
        Localizacao loc = geolocalizacaoService.porIp(request.getRemoteAddr());
        DadosClimaticos clima = previsaoService.buscaPrevisaoPorCidade(
                loc.pais(), loc.cidade(), LocalDate.now());
        return ResponseEntity.ok(ApiResponse.success(clima));
    }

    private LocalDate parseData(String data) {
        return data != null ? LocalDate.parse(data) : LocalDate.now();
    }
}