package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.alerta.AlertaDisparoDTO;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.exception.LimiteConexoesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AlertaStreamService — isolamento por cliente, limites e ciclo de vida SSE")
class AlertaStreamServiceIsolamentoTest {

    private AlertaStreamService streamService;
    private Instant futuro;

    @BeforeEach
    void setUp() {
        streamService = new AlertaStreamService(2); // limite de 2 conexões por cliente
        futuro = Instant.now().plusSeconds(1800);
    }

    private AlertaDisparoDTO criarDisparo(Long alertaId) {
        DadosClimaticos clima = new DadosClimaticos(
                LocalDate.now(), 25.0, 32.0, 18.0, 5.0, 70.0, 6.0, "CHUVOSO", "OPEN_METEO");
        return new AlertaDisparoDTO(alertaId, "Florianópolis", "BR", clima);
    }

    @Test
    @DisplayName("Cenário 19: Dado A e B conectados ao canal 42 e alerta de A disparado, quando enviar, então só A recebe alerta-disparado (ALR-06, SSE-04)")
    void cenario19_soDonoRecebeAlertaNoMesmoCanal() throws IOException {
        Long clienteA = 10L;
        Long clienteB = 20L;
        Long canalId = 42L;

        streamService.registrar(clienteA, canalId, futuro);
        streamService.registrar(clienteB, canalId, futuro);

        SseEmitter mockA = mock(SseEmitter.class);
        SseEmitter mockB = mock(SseEmitter.class);

        streamService.getConexoes().put(new AlertaStreamService.CanalKey(clienteA, canalId),
                new CopyOnWriteArrayList<>(List.of(mockA)));
        streamService.getConexoes().put(new AlertaStreamService.CanalKey(clienteB, canalId),
                new CopyOnWriteArrayList<>(List.of(mockB)));

        AlertaDisparoDTO disparo = criarDisparo(1L);
        streamService.enviar(clienteA, canalId, disparo);

        verify(mockA, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(mockB, never()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("Cenário 20: Dado só B conectado e alerta de A disparado, quando enviar, então B não recebe nada (ALR-07)")
    void cenario20_outroClienteConectadoNaoRecebeNada() throws IOException {
        Long clienteA = 10L;
        Long clienteB = 20L;
        Long canalId = 42L;

        streamService.registrar(clienteB, canalId, futuro);

        SseEmitter mockB = mock(SseEmitter.class);
        streamService.getConexoes().put(new AlertaStreamService.CanalKey(clienteB, canalId),
                new CopyOnWriteArrayList<>(List.of(mockB)));

        AlertaDisparoDTO disparo = criarDisparo(1L);
        streamService.enviar(clienteA, canalId, disparo);

        verify(mockB, never()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("Cenário 24: Dado stream aberto, quando o heartbeat rodar, então chega ping (SSE-06)")
    void cenario24_heartbeatEnviaPingParaTodosConectados() throws IOException {
        Long clienteId = 1L;
        Long canalId = 5L;

        streamService.registrar(clienteId, canalId, futuro);

        SseEmitter mockEmitter = mock(SseEmitter.class);
        streamService.getConexoes().put(new AlertaStreamService.CanalKey(clienteId, canalId),
                new CopyOnWriteArrayList<>(List.of(mockEmitter)));

        streamService.heartbeat();

        verify(mockEmitter, atLeastOnce()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("Cenário 25: Dado duas conexões de A no canal 42, quando um alerta disparar, então as duas recebem (SSE-07)")
    void cenario25_multiplasConexoesDoMesmoClienteRecebemEvento() throws IOException {
        Long clienteId = 1L;
        Long canalId = 42L;

        streamService.registrar(clienteId, canalId, futuro);
        streamService.registrar(clienteId, canalId, futuro);

        SseEmitter mock1 = mock(SseEmitter.class);
        SseEmitter mock2 = mock(SseEmitter.class);

        streamService.getConexoes().put(new AlertaStreamService.CanalKey(clienteId, canalId),
                new CopyOnWriteArrayList<>(List.of(mock1, mock2)));

        AlertaDisparoDTO disparo = criarDisparo(1L);
        streamService.enviar(clienteId, canalId, disparo);

        verify(mock1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(mock2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("Cenário 26: Dado token que expira em 1 s, quando abrir o stream, então a conexão tem timeout baseado na expiração (SSE-08)")
    void cenario26_timeoutAlinhadoComExpiracaoDoToken() {
        Long clienteId = 1L;
        Long canalId = 10L;
        Instant expiraEm = Instant.now().plusSeconds(1);

        SseEmitter emitter = streamService.registrar(clienteId, canalId, expiraEm);

        assertThat(emitter.getTimeout()).isNotNull();
        assertThat(emitter.getTimeout()).isGreaterThan(0L).isLessThanOrEqualTo(1000L);
    }

    @Test
    @DisplayName("Cenário 27: Dado stream que completa, estoura timeout ou dá erro, quando terminar, então sai do mapa e a chave some se vazia (SSE-09)")
    void cenario27_remocaoLimpaMapaEChaveQuandoVazia() {
        Long clienteId = 1L;
        Long canalId = 42L;
        AlertaStreamService.CanalKey chave = new AlertaStreamService.CanalKey(clienteId, canalId);

        SseEmitter emitter1 = streamService.registrar(clienteId, canalId, futuro);
        SseEmitter emitter2 = streamService.registrar(clienteId, canalId, futuro);

        assertThat(streamService.getConexoes().get(chave)).hasSize(2);

        // Remove primeiro emitter
        streamService.getConexoes().computeIfPresent(chave, (k, lista) -> {
            lista.remove(emitter1);
            return lista.isEmpty() ? null : lista;
        });
        assertThat(streamService.getConexoes().get(chave)).hasSize(1);

        // Remove segundo emitter -> chave deve ser apagada
        streamService.getConexoes().computeIfPresent(chave, (k, lista) -> {
            lista.remove(emitter2);
            return lista.isEmpty() ? null : lista;
        });
        assertThat(streamService.getConexoes().containsKey(chave)).isFalse();
    }

    @Test
    @DisplayName("Cenário 28: Dado limite 2, quando A abrir a terceira conexão, então 429 (SSE-10)")
    void cenario28_limiteConexoesLancaExcecao() {
        Long clienteId = 1L;

        streamService.registrar(clienteId, 1L, futuro);
        streamService.registrar(clienteId, 2L, futuro);

        assertThatThrownBy(() -> streamService.registrar(clienteId, 3L, futuro))
                .isInstanceOf(LimiteConexoesException.class);
    }
}
