package ifsc.edu.lll.service;

import ifsc.edu.lll.model.Log;
import ifsc.edu.lll.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
