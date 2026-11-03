package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.alerta.AlertaRequestDTO;
import ifsc.edu.lll.dto.alerta.AlertaResponseDTO;
import ifsc.edu.lll.model.Alerta;
import ifsc.edu.lll.repository.AlertaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    @Transactional
    public AlertaResponseDTO cadastrar(Long clienteId, AlertaRequestDTO dto) {
        Alerta alerta = new Alerta(
                clienteId,
                dto.canalId(), dto.pais(), dto.cidade(),
                dto.temperaturaMaxima(), dto.temperaturaMinima(),
                dto.precipitacao()
        );
        Alerta salvo = alertaRepository.save(alerta);
        return AlertaResponseDTO.de(salvo);
    }

    @Transactional(readOnly = true)
    public List<AlertaResponseDTO> listarAtivos(Long clienteId) {
        return alertaRepository.findByClienteIdAndAtivoTrue(clienteId)
                .stream()
                .map(AlertaResponseDTO::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean canalHabilitado(Long clienteId, Long canalId) {
        return alertaRepository.existsByClienteIdAndCanalId(clienteId, canalId);
    }
}
