package ifsc.edu.lll.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@Table(name = "avaliacao")
public class Avaliacao {
    @Id
    @Column(name = "id_avaliacao")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name="nome")
    private String nome;

    @Column(name="nota")
    private int nota;

    @Column(name="comentario")
    private String comentario;

    // Loombok faz isso sozinho, mas sem isso não estava retornando o valor
    public String getNome() {
        return nome;
    }

    public int getNota() {
        return nota;
    }

    public String getComentario() {
        return comentario;
    }
}