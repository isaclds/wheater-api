package ifsc.edu.lll.service.mapper;

import ifsc.edu.lll.dto.geocoding.GeoResponse;
import ifsc.edu.lll.dto.geocoding.GeoResult;
import ifsc.edu.lll.dto.shared.Coordenadas;
import org.springframework.stereotype.Component;

@Component
public class GeocodingResponseMapper {

    public Coordenadas paraCoordenadas(GeoResponse resposta) {
        if (resposta.results() == null || resposta.results().isEmpty()) {
            throw new IllegalArgumentException("Nenhum local encontrado");
        }

        GeoResult resultado = resposta.results().getFirst();
        return new Coordenadas(resultado.latitude(), resultado.longitude());
    }
}