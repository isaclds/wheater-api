package ifsc.edu.lll.controller;

import ifsc.edu.lll.dto.response.ApiResponse;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.service.ForecastClimaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/previsao")
public class PrevisaoController {

    private final ForecastClimaService forecastClimaService;

    public PrevisaoController(ForecastClimaService forecastClimaService) {
        this.forecastClimaService = forecastClimaService;
    }

    @GetMapping("/{pais}")
    public ResponseEntity<Integer> previsaoPorPais(
            @PathVariable String pais) {
        return ResponseEntity.ok(0);
    }
    @GetMapping("/{pais}/{estado}")
    public ResponseEntity<Integer> previsaoPorPaisEstado(
            @PathVariable String pais,@PathVariable String estado) {
        return ResponseEntity.ok(0);
    }
    @GetMapping("/{pais}/{estado}/{cidade}")
    public ResponseEntity<Integer> previsaoPorPaisEstadoCidade(
            @PathVariable String pais,@PathVariable String estado,@PathVariable String cidade) {
        return ResponseEntity.ok(0);
    }
}