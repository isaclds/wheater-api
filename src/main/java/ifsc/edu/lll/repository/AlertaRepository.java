package ifsc.edu.lll.repository;

import ifsc.edu.lll.model.Alerta;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class AlertaRepository {

    // Gerador de IDs sequenciais thread-safe
    private final AtomicLong contador = new AtomicLong(1);
    private final List<Alerta> alertas = new CopyOnWriteArrayList<>();

    public Alerta salvar(Alerta alerta) {
        Alerta comId = new Alerta(
                contador.getAndIncrement(),
                alerta.canalId(), alerta.pais(), alerta.cidade(),
                alerta.temperaturaMaxima(), alerta.temperaturaMinima(),
                alerta.precipitacao(), true
        );
        alertas.add(comId);
        return comId;
    }

    public List<Alerta> findByAtivoTrue() {
        return alertas.stream().filter(Alerta::ativo).toList();
    }
}
