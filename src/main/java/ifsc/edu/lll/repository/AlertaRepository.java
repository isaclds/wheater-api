package ifsc.edu.lll.repository;

import ifsc.edu.lll.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para a entidade Alerta.
 */
@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByAtivoTrue();
}
