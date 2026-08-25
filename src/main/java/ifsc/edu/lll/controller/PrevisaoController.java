package ifsc.edu.lll.controller;

import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.service.PrevisaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/previsao")
public class PrevisaoController {

    private final PrevisaoService previsaoService;

    public PrevisaoController(PrevisaoService previsaoService) {
        this.previsaoService = previsaoService;
    }

    @GetMapping("/{pais}")
    public ResponseEntity<List<DadosClimaticos>> previsaoPorPais(
            @PathVariable String pais,
            @RequestParam(required = false) String data) {
        return ResponseEntity.ok(previsaoService.buscaPrevisaoPorPais(pais, parseData(data)));
    }
    @GetMapping("/{pais}/{estado}")
    public ResponseEntity<List<DadosClimaticos>> previsaoPorPaisEstado(
            @PathVariable String pais,
            @PathVariable String estado,
            @RequestParam(required = false) String data) {
        return ResponseEntity.ok(previsaoService.buscaPrevisaoPorEstado(pais, estado, parseData(data)));
    }
    @GetMapping("/{pais}/{estado}/{cidade}")
    public ResponseEntity<DadosClimaticos> previsaoPorPaisEstadoCidade(
            @PathVariable String pais,
            @PathVariable String estado,
            @PathVariable String cidade,
            @RequestParam(required = false) String data) {
        return ResponseEntity.ok(previsaoService.buscaPrevisaoPorCidade(pais, cidade, parseData(data)));
    }

    private LocalDate parseData(String data) {
        return data != null ? LocalDate.parse(data) : LocalDate.now();
    }
}