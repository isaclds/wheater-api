package ifsc.edu.lll.model;

import ifsc.edu.lll.dto.shared.DadosClimaticos;
import jakarta.persistence.*;

/**
 * Entidade JPA que representa um alerta climático cadastrado por um canal.
 * Um alerta dispara (via SSE) quando as condições climáticas previstas
 * ultrapassam os limites definidos pelo usuário.
 */
@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "canal_id", nullable = false)
    private Long canalId;

    @Column(nullable = false, length = 10)
    private String pais;

    @Column(nullable = false, length = 100)
    private String cidade;

    /** Dispara se a previsão ultrapassar esse valor (°C) */
    @Column(name = "temperatura_maxima")
    private Double temperaturaMaxima;

    /** Dispara se a previsão cair abaixo desse valor (°C) */
    @Column(name = "temperatura_minima")
    private Double temperaturaMinima;

    /** Dispara se a precipitação prevista ultrapassar esse valor (mm) */
    @Column
    private Double precipitacao;

    @Column(nullable = false)
    private boolean ativo = true;

    /** Construtor padrão exigido pelo JPA */
    protected Alerta() {}

    public Alerta(Long canalId, String pais, String cidade,
                  Double temperaturaMaxima, Double temperaturaMinima,
                  Double precipitacao) {
        this.canalId = canalId;
        this.pais = pais;
        this.cidade = cidade;
        this.temperaturaMaxima = temperaturaMaxima;
        this.temperaturaMinima = temperaturaMinima;
        this.precipitacao = precipitacao;
        this.ativo = true;
    }

    /** Verifica se as condições da previsão ativam esse alerta */
    public boolean foiAtingidoPor(DadosClimaticos previsao) {
        if (temperaturaMaxima != null && previsao.temperaturaMaxima() != null
                && previsao.temperaturaMaxima() > temperaturaMaxima) return true;
        if (temperaturaMinima != null && previsao.temperaturaMinima() != null
                && previsao.temperaturaMinima() < temperaturaMinima) return true;
        if (precipitacao != null && previsao.precipitacao() != null
                && previsao.precipitacao() > precipitacao) return true;
        return false;
    }

    // ── Getters ──────────────────────────────────────────────────
    public Long getId()                { return id; }
    public Long getCanalId()           { return canalId; }
    public String getPais()            { return pais; }
    public String getCidade()          { return cidade; }
    public Double getTemperaturaMaxima() { return temperaturaMaxima; }
    public Double getTemperaturaMinima() { return temperaturaMinima; }
    public Double getPrecipitacao()    { return precipitacao; }
    public boolean isAtivo()           { return ativo; }

    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
