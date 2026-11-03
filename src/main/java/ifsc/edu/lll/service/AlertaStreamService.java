package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.alerta.AlertaDisparoDTO;
import ifsc.edu.lll.exception.LimiteConexoesException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Service
public class AlertaStreamService {

    public record CanalKey(Long clienteId, Long canalId) {}

    private final Map<CanalKey, List<SseEmitter>> conexoes = new ConcurrentHashMap<>();
    private final int maxConexoesPorCliente;

    public AlertaStreamService(@Value("${sse.max-conexoes-por-cliente:5}") int maxConexoesPorCliente) {
        this.maxConexoesPorCliente = maxConexoesPorCliente;
    }

    public SseEmitter registrar(Long clienteId, Long canalId, Instant tokenExpiraEm) {
        if (contarConexoes(clienteId) >= maxConexoesPorCliente) {
            throw new LimiteConexoesException();
        }

        long restanteMs = Math.max(1, Duration.between(Instant.now(), tokenExpiraEm).toMillis());
        CanalKey chave = new CanalKey(clienteId, canalId);
        SseEmitter emitter = new SseEmitter(restanteMs); // encerra junto com o token

        conexoes.computeIfAbsent(chave, k -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable limpar = () -> remover(chave, emitter);
        emitter.onCompletion(limpar);
        emitter.onTimeout(limpar);
        emitter.onError(e -> limpar.run());

        enviarEvento(emitter, SseEmitter.event().name("conectado").data(Map.of("canalId", canalId)));
        return emitter;
    }

    /** Entrega só aos streams do dono do alerta naquele canal. */
    public void enviar(Long clienteId, Long canalId, AlertaDisparoDTO alerta) {
        for (SseEmitter e : conexoes.getOrDefault(new CanalKey(clienteId, canalId), List.of())) {
            enviarEvento(e, SseEmitter.event().name("alerta-disparado").data(alerta));
        }
    }

    @Scheduled(fixedRateString = "${sse.heartbeat-seconds:25}", timeUnit = TimeUnit.SECONDS)
    public void heartbeat() {
        conexoes.values().forEach(lista -> lista.forEach(
                e -> enviarEvento(e, SseEmitter.event().name("ping").data(""))));
    }

    private void enviarEvento(SseEmitter emitter, SseEmitter.SseEventBuilder evento) {
        try {
            emitter.send(evento);
        } catch (IOException | IllegalStateException erro) {
            emitter.completeWithError(erro);
        }
    }

    private void remover(CanalKey chave, SseEmitter emitter) {
        conexoes.computeIfPresent(chave, (k, lista) -> {
            lista.remove(emitter);
            return lista.isEmpty() ? null : lista; // apaga a chave quando esvazia
        });
    }

    private long contarConexoes(Long clienteId) {
        return conexoes.entrySet().stream()
                .filter(en -> en.getKey().clienteId().equals(clienteId))
                .mapToLong(en -> en.getValue().size()).sum();
    }

    public Map<CanalKey, List<SseEmitter>> getConexoes() {
        return conexoes;
    }
}
