package ifsc.edu.lll.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração centralizada da documentação OpenAPI 3.0.
 * Swagger UI em /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI weatherApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Weather API")
                        .description("""
                                API de previsão do tempo que agrega dados de múltiplas fontes externas
                                (Open-Meteo para previsões futuras e NASA POWER para dados históricos).
                                Permite consultar condições climáticas por país, estado ou cidade,
                                além de cadastrar alertas automáticos com notificação em tempo real via SSE.
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Grupo B — IFSC")
                                .email("grupo-b@aluno.ifsc.edu.br"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
