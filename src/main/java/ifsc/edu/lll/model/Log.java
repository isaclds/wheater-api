package ifsc.edu.lll.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String endpoint;

    @Column(nullable = false)
    private int status;

    @Column(name = "data_acesso", nullable = false, updatable = false)
    private Instant dataAcesso;

    public Log() {}

    public Log(String endpoint, int status) {
        this.endpoint = endpoint;
        this.status = status;
        this.dataAcesso = Instant.now();
    }


    public Long getId() {
        return id;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public int getStatus() {
        return status;
    }

    public Instant getDataAcesso() {
        return dataAcesso;
    }
}
