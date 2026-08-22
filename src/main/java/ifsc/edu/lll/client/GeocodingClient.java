package ifsc.edu.lll.client;

import ifsc.edu.lll.dto.geocoding.GeoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Busca as coordenadas do local passado
@FeignClient(name = "geocodingClient", url = "https://geocoding-api.open-meteo.com")
public interface GeocodingClient {

    @GetMapping("/v1/search")
    GeoResponse buscaCoordenadas(
            @RequestParam("name") String nome,
            @RequestParam(value = "count", defaultValue = "1") int count,
            @RequestParam(value = "language", defaultValue = "pt") String language,
            @RequestParam(value = "countryCode", required = false) String countryCode
    );
}