package ifsc.edu.lll.controller;

import ifsc.edu.lll.model.Avaliacao;
import ifsc.edu.lll.dto.response.ApiResponse;
import ifsc.edu.lll.service.AvaliacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avaliacao")
public class AvaliacaoController {

    private final AvaliacaoService service;

    public AvaliacaoController(AvaliacaoService service) {
        this.service = service;
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<Avaliacao>> registra(@RequestBody Avaliacao avaliacao) {
        Avaliacao resultado = service.registra(avaliacao);
        return ResponseEntity.ok(ApiResponse.success(200, "Avaliação registrada!", resultado));
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<Avaliacao>>> buscaTodas(){
        List<Avaliacao> avaliacoes = service.buscaTodas();
        return ResponseEntity.ok(ApiResponse.success(200, "Todas as avaliações retornadas!", avaliacoes));
    }

}