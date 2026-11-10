package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.log.LogRequestDTO;
import ifsc.edu.lll.model.Log;
import ifsc.edu.lll.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public List<Log> buscaLog() {
        return logRepository.findAll();
    }

    @Transactional
    public void criarLog(String endpoint, int status){
        logRepository.save(new Log(endpoint, status));
    }

    public List<Log> buscaLogPorStatus(int status) {
        return logRepository.findLogByStatusEquals(status);
    }

    public List<Log> buscaLogPorEndpoint(String endpoint) {return logRepository.findLogByEndpointEquals(endpoint);}

    public List<Log> buscaLogPorIntervalo(LogRequestDTO dto) {
        Instant dataInicial = dto.dataInicial();
        Instant dataFinal = dto.dataFinal();

        return logRepository.findLogsByDataAcessoBetween(dataInicial, dataFinal);
    }
}
