package ifsc.edu.lll.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração centralizada da documentação OpenAPI 3.0.
 * Swagger UI em /docs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI weatherApiOpenAPI() {
        ApiResponse rateLimitResponse = new ApiResponse()
                .description("Limite de requisições excedido (Rate limit atingido)")
                .addHeaderObject("Retry-After", new Header()
                        .description("Tempo de espera em segundos até a próxima requisição permitida")
                        .schema(new IntegerSchema().example(60)))
                .addHeaderObject("X-RateLimit-Limit", new Header()
                        .description("Limite máximo de requisições por janela")
                        .schema(new IntegerSchema().example(100)))
                .addHeaderObject("X-RateLimit-Remaining", new Header()
                        .description("Quantidade de requisições restantes na janela atual")
                        .schema(new IntegerSchema().example(0)));

        return new OpenAPI()
                .info(new Info()
                        .title("Weather API")
                        .description("""
                                API de previsão do tempo que agrega dados de múltiplas fontes externas
                                (Open-Meteo para previsões futuras e NASA POWER para dados históricos).
                                Permite consultar condições climáticas por país, estado ou cidade,
                                além de cadastrar alertas automáticos com notificação em tempo real via SSE.

                                ### Rate Limiting (Controle de Taxa)
                                A API implementa limitação de taxa de requisições por IP de cliente (com suporte a `X-Forwarded-For`).
                                - Se o limite de requisições for excedido dentro da janela de tempo, a API responderá com **HTTP 429 Too Many Requests**.
                                - A resposta segue o envelope padronizado `ApiResponse` com `status: 429` e mensagem descritiva.
                                - Cabeçalhos informativos retornados: `Retry-After`, `X-RateLimit-Limit` e `X-RateLimit-Remaining`.
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Grupo B — IFSC")
                                .email("grupo-b@aluno.ifsc.edu.br"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addResponses("RateLimitExceeded", rateLimitResponse));
    }

    @Bean
    public OpenApiCustomizer rateLimitOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, pathItem) -> {
                    if (!"/ping".equals(path)) {
                        pathItem.readOperations().forEach(operation -> {
                            if (!operation.getResponses().containsKey("429")) {
                                operation.getResponses().addApiResponse("429",
                                        new ApiResponse().$ref("#/components/responses/RateLimitExceeded"));
                            }
                        });
                    }
                });
            }
        };
    }
}
