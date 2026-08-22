package ifsc.edu.lll.client;

import ifsc.edu.lll.dto.nasa.NasaPowerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "nasaPowerClient", url = "https://power.larc.nasa.gov")
public interface NasaPowerClient {

    @GetMapping("/api/temporal/daily/point")
    NasaPowerResponse buscaClimaDiario(
            @RequestParam("parameters") String parameters,
            @RequestParam("community") String community,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("start") String start,
            @RequestParam("end") String end,
            @RequestParam(value = "format", defaultValue = "JSON") String format
    );
}
