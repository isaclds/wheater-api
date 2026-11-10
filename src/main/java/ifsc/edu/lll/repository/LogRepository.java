package ifsc.edu.lll.repository;

import ifsc.edu.lll.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    List<Log> findLogByStatusEquals(int status);

    List<Log> findLogByEndpointEquals(String endpoint);

    List<Log> findLogsByDataAcessoBetween(Instant dataAcessoAfter, Instant dataAcessoBefore);
}
