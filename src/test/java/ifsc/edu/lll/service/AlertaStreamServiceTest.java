package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.alerta.AlertaDisparoDTO;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("AlertaStreamService — gerenciamento de conexões e envio SSE")
class AlertaStreamServiceTest {

    private AlertaStreamService streamService;

    @BeforeEach
    void setUp() {
        streamService = new AlertaStreamService();
    }

    @Test
    @DisplayName("Deve registrar SseEmitter com sucesso para um canal")
    void deveRegistrarSseEmitter() {
        SseEmitter emitter = streamService.registrar(1L);

        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("Deve permitir registrar múltiplos emitters para o mesmo canal")
    void deveRegistrarMultiplosEmittersMesmoCanal() {
        SseEmitter emitter1 = streamService.registrar(1L);
        SseEmitter emitter2 = streamService.registrar(1L);

        assertThat(emitter1).isNotNull();
        assertThat(emitter2).isNotNull();
        assertThat(emitter1).isNotSameAs(emitter2);
    }

    @Test
    @DisplayName("Deve enviar alerta para canal sem lançar exceções mesmo se vazio")
    void deveEnviarParaCanalSemAssinantesSemErro() {
        DadosClimaticos clima = new DadosClimaticos(
                LocalDate.now(), 20.0, 25.0, 15.0, 0.0, 60.0, 5.0, "CEU_LIMPO", "OPEN_METEO");
        AlertaDisparoDTO disparo = new AlertaDisparoDTO(10L, "Florianópolis", "BR", clima);

        assertThatCode(() -> streamService.enviar(999L, disparo))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve enviar alerta para canal com conexões ativas")
    void deveEnviarParaCanalComAssinante() {
        streamService.registrar(1L);

        DadosClimaticos clima = new DadosClimaticos(
                LocalDate.now(), 30.0, 36.0, 24.0, 10.0, 80.0, 12.0, "TEMPESTADE", "OPEN_METEO");
        AlertaDisparoDTO disparo = new AlertaDisparoDTO(1L, "Florianópolis", "BR", clima);

        assertThatCode(() -> streamService.enviar(1L, disparo))
                .doesNotThrowAnyException();
    }
}
