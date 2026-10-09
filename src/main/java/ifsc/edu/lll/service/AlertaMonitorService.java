package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.alerta.AlertaDisparoDTO;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.model.Alerta;
import ifsc.edu.lll.repository.AlertaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AlertaMonitorService {

    private final AlertaRepository alertaRepository;
    private final PrevisaoService previsaoService;
    private final AlertaStreamService stream;

    public AlertaMonitorService(
            AlertaRepository alertaRepository,
            PrevisaoService previsaoService,
            AlertaStreamService stream
    ) {
        this.alertaRepository = alertaRepository;
        this.previsaoService = previsaoService;
        this.stream = stream;
    }

    // Verifica todos os alertas ativos a cada 1 minuto
    @Scheduled(fixedRate = 15_000)
    public void verificarAlertas() {
        List<Alerta> alertasAtivos = alertaRepository.findByAtivoTrue();

        for (Alerta alerta : alertasAtivos) {
            DadosClimaticos previsao = previsaoService
                    .buscaPrevisaoPorCidade(alerta.pais(), alerta.cidade(), LocalDate.now());

            if (alerta.foiAtingidoPor(previsao)) {
                stream.enviar(alerta.canalId(), AlertaDisparoDTO.de(alerta, previsao));
            }
        }
    }
}