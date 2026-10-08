package ifsc.edu.lll.controller;

import ifsc.edu.lll.dto.geocoding.Localizacao;
import ifsc.edu.lll.dto.response.ApiResponse;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.service.GeolocalizacaoService;
import ifsc.edu.lll.service.PrevisaoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/previsao")
public class PrevisaoController {

    private final PrevisaoService previsaoService;
    private final GeolocalizacaoService geolocalizacaoService;

    public PrevisaoController(PrevisaoService previsaoService, GeolocalizacaoService geolocalizacaoService) {
        this.previsaoService = previsaoService;
        this.geolocalizacaoService = geolocalizacaoService;
    }

    @GetMapping("/{pais}")
    public ResponseEntity<ApiResponse<List<DadosClimaticos>>> previsaoPorPais(
            @PathVariable String pais,
            @RequestParam(required = false) String data) {
        List<DadosClimaticos> climas = previsaoService.buscaPrevisaoPorPais(pais, parseData(data));
        return ResponseEntity.ok(ApiResponse.success(climas));
    }
    @GetMapping("/{pais}/{estado}")
    public ResponseEntity<ApiResponse<List<DadosClimaticos>>> previsaoPorPaisEstado(
            @PathVariable String pais,
            @PathVariable String estado,
            @RequestParam(required = false) String data) {
        List<DadosClimaticos> climas = previsaoService.buscaPrevisaoPorEstado(pais, estado, parseData(data));
        return ResponseEntity.ok(ApiResponse.success(climas));
    }
    @GetMapping("/{pais}/{estado}/{cidade}")
    public ResponseEntity<ApiResponse<DadosClimaticos>> previsaoPorPaisEstadoCidade(
            @PathVariable String pais,
            @PathVariable String estado,
            @PathVariable String cidade,
            @RequestParam(required = false) String data) {
        DadosClimaticos clima = previsaoService.buscaPrevisaoPorCidade(pais, cidade, parseData(data));
        return ResponseEntity.ok(ApiResponse.success(clima));
    }

    @GetMapping("/aqui")
    public ResponseEntity<DadosClimaticos> meuLocal(HttpServletRequest request) {
        Localizacao loc = geolocalizacaoService.porIp(request.getRemoteAddr());
        return ResponseEntity.ok(
                previsaoService.buscaPrevisaoPorCidade(loc.pais(), loc.cidade(),LocalDate.now()));
    }

    private LocalDate parseData(String data) {
        return data != null ? LocalDate.parse(data) : LocalDate.now();
    }
}