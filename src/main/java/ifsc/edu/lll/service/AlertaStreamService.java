package ifsc.edu.lll.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import ifsc.edu.lll.dto.alerta.AlertaDisparoDTO;

@Service
public class AlertaStreamService {

    // ConcurrentHashMap garante acesso seguro em múltiplos threads
    private final Map<Long, List<SseEmitter>> conexoes = new ConcurrentHashMap<>();

    public SseEmitter registrar(Long canalId) {
        SseEmitter emitter = new SseEmitter(0L);

        // Registra a conexão do canal
        conexoes.computeIfAbsent(canalId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // Limpa quando a conexão é encerrada
        emitter.onCompletion(() -> conexoes.get(canalId).remove(emitter));
        emitter.onTimeout(() -> conexoes.get(canalId).remove(emitter));
        return emitter;
    }

    public void enviar(Long canalId, AlertaDisparoDTO alerta) {
        List<SseEmitter> conexoesDoCanal = conexoes.getOrDefault(canalId, List.of());

        for (SseEmitter conexao : conexoesDoCanal) {
            try {
                conexao.send(SseEmitter.event().name("alerta-disparado").data(alerta));
            } catch (IOException erroDeEntrega) {
                conexao.completeWithError(erroDeEntrega);
            }
        }
    }
}
