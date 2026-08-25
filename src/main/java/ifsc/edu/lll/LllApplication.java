package ifsc.edu.lll;

import ifsc.edu.lll.dto.forecast.ForecastResponse;
import ifsc.edu.lll.dto.nasa.NasaPowerResponse;
import ifsc.edu.lll.dto.shared.Coordenadas;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import ifsc.edu.lll.service.ForecastClimaService;
import ifsc.edu.lll.service.GeocodingCoordenadasService;
import ifsc.edu.lll.service.NasaClimaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
@EnableFeignClients
public class LllApplication {

    public static void main(String[] args) {
        SpringApplication.run(LllApplication.class, args);
    }

}
