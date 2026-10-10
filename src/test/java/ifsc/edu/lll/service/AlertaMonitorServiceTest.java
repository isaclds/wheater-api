package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.alerta.AlertaDisparoDTO;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.model.Alerta;
import ifsc.edu.lll.repository.AlertaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaMonitorService — verificação periódica de alertas")
class AlertaMonitorServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private PrevisaoService previsaoService;

    @Mock
    private AlertaStreamService streamService;

    @InjectMocks
    private AlertaMonitorService monitorService;

    @Test
    @DisplayName("Deve disparar evento SSE quando as condições do alerta forem atingidas")
    void deveDispararAlertaQuandoCondicaoAtingida() {
        // Alerta configurado com limite máximo de 30°C
        Alerta alerta = new Alerta(100L, "BR", "Florianópolis", 30.0, null, null);
        when(alertaRepository.findByAtivoTrue()).thenReturn(List.of(alerta));

        // Previsão do dia com 35°C (ultrapassa limite)
        DadosClimaticos previsao = new DadosClimaticos(
                LocalDate.now(), 28.0, 35.0, 20.0, 0.0, 70.0, 5.0, "CEU_LIMPO", "OPEN_METEO");
        when(previsaoService.buscaPrevisaoPorCidade(eq("BR"), eq("Florianópolis"), any(LocalDate.class)))
                .thenReturn(previsao);

        monitorService.verificarAlertas();

        verify(streamService, times(1)).enviar(eq(100L), any(AlertaDisparoDTO.class));
    }

    @Test
    @DisplayName("Não deve disparar evento SSE quando a previsão não atingir os limites")
    void naoDeveDispararAlertaQuandoCondicaoNaoAtingida() {
        Alerta alerta = new Alerta(100L, "BR", "Florianópolis", 35.0, null, null);
        when(alertaRepository.findByAtivoTrue()).thenReturn(List.of(alerta));

        // Previsão com 25°C (menor que limite)
        DadosClimaticos previsao = new DadosClimaticos(
                LocalDate.now(), 22.0, 25.0, 18.0, 0.0, 60.0, 4.0, "CEU_LIMPO", "OPEN_METEO");
        when(previsaoService.buscaPrevisaoPorCidade(eq("BR"), eq("Florianópolis"), any(LocalDate.class)))
                .thenReturn(previsao);

        monitorService.verificarAlertas();

        verify(streamService, never()).enviar(any(), any());
    }

    @Test
    @DisplayName("Deve continuar processando outros alertas mesmo se um falhar com exceção")
    void deveContinuarExecucaoAposErroEmUmAlerta() {
        Alerta alertaComErro = new Alerta(1L, "XX", "CidadeInvalida", 30.0, null, null);
        Alerta alertaSucesso = new Alerta(2L, "BR", "Florianópolis", 20.0, null, null);
        when(alertaRepository.findByAtivoTrue()).thenReturn(List.of(alertaComErro, alertaSucesso));

        when(previsaoService.buscaPrevisaoPorCidade(eq("XX"), eq("CidadeInvalida"), any()))
                .thenThrow(new IllegalArgumentException("Localidade inválida"));

        DadosClimaticos previsao = new DadosClimaticos(
                LocalDate.now(), 22.0, 28.0, 18.0, 0.0, 60.0, 4.0, "CEU_LIMPO", "OPEN_METEO");
        when(previsaoService.buscaPrevisaoPorCidade(eq("BR"), eq("Florianópolis"), any()))
                .thenReturn(previsao);

        monitorService.verificarAlertas();

        verify(streamService, times(1)).enviar(eq(2L), any(AlertaDisparoDTO.class));
    }
}
