package ifsc.edu.lll.controller;

import ifsc.edu.lll.dto.alerta.AlertaRequestDTO;
import ifsc.edu.lll.dto.response.ApiResponse;
import ifsc.edu.lll.model.Alerta;
import ifsc.edu.lll.service.AlertaService;
import ifsc.edu.lll.service.AlertaStreamService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/alertas")
public class AlertaController {

    private final AlertaService alertaService;
    private final AlertaStreamService stream;

    public AlertaController(AlertaService alertaService, AlertaStreamService stream) {
        this.alertaService = alertaService;
        this.stream = stream;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Alerta>> cadastrar(@RequestBody AlertaRequestDTO dto) {
        Alerta alerta = alertaService.cadastrar(dto);
        return ResponseEntity.ok(ApiResponse.success(201, "Alerta cadastrado", alerta));
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter assinar(@RequestParam Long canalId) {
        return stream.registrar(canalId);
    }
}
