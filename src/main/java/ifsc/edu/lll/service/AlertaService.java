package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.alerta.AlertaRequestDTO;
import ifsc.edu.lll.model.Alerta;
import ifsc.edu.lll.repository.AlertaRepository;
import org.springframework.stereotype.Service;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    public Alerta cadastrar(AlertaRequestDTO dto) {
        Alerta alerta = new Alerta(
                null, dto.canalId(), dto.pais(), dto.cidade(),
                dto.temperaturaMaxima(), dto.temperaturaMinima(),
                dto.precipitacao(), true
        );
        return alertaRepository.salvar(alerta);
    }
}
