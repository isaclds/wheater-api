package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.alerta.AlertaDisparoDTO;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.model.Alerta;
import ifsc.edu.lll.repository.AlertaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AlertaMonitorService {

    private static final Logger log = LoggerFactory.getLogger(AlertaMonitorService.class);

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

    /** Verifica todos os alertas ativos a cada 15 segundos */
    @Scheduled(fixedRate = 15_000)
    public void verificarAlertas() {
        List<Alerta> alertasAtivos = alertaRepository.findByAtivoTrue();
        log.debug("Verificando {} alerta(s) ativo(s)", alertasAtivos.size());

        for (Alerta alerta : alertasAtivos) {
            try {
                DadosClimaticos previsao = previsaoService
                        .buscaPrevisaoPorCidade(alerta.getPais(), alerta.getCidade(), LocalDate.now());

                if (alerta.foiAtingidoPor(previsao)) {
                    log.info("Alerta {} disparado para canal {}", alerta.getId(), alerta.getCanalId());
                    stream.enviar(alerta.getCanalId(), AlertaDisparoDTO.de(alerta, previsao));
                }
            } catch (Exception e) {
                log.warn("Erro ao verificar alerta {}: {}", alerta.getId(), e.getMessage());
            }
        }
    }
}