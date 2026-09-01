package ifsc.edu.lll.service;

import ifsc.edu.lll.model.Avaliacao;
import ifsc.edu.lll.repository.AvaliacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvaliacaoService {
    @Autowired
    AvaliacaoRepository repository;

    public Avaliacao registra(Avaliacao avaliacao){
        int nota = avaliacao.getNota();
        if (nota > 5 || nota < 1) {
            throw new Error("Nota deve ser entre 1 e 5!");
        }

        if (avaliacao.getNome().isEmpty()) {
            throw new Error("Nome não pode estar em branco!");
        }

        if(avaliacao.getComentario().length() > 1000) {
            throw new Error("Comentário não pode ser maior que 1000 caracteres!");
        }

        return repository.save(avaliacao);
    }

    public List<Avaliacao> buscaTodas(){
        return repository.findAll();
    };
}
