package ifsc.edu.lll;

import ifsc.edu.lll.dto.nasa.NasaPowerResponse;
import ifsc.edu.lll.service.NasaClimaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
@EnableFeignClients
public class LllApplication {

    public static void main(String[] args) {
        SpringApplication.run(LllApplication.class, args);
    }

    @Bean
    CommandLineRunner testarNasaClima(NasaClimaService nasaClimaService) {
        return args -> {
            NasaPowerResponse resposta = nasaClimaService.buscarClimaDiario(
                    -27.64, -48.67,
                    LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 8, 20)
            );
            System.out.println(resposta.properties().parameter());
        };
    }

}
