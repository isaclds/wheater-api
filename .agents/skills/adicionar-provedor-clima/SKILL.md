---
name: adicionar-provedor-clima
description: >-
  Use this skill when the user asks to add or integrate a new external weather or geocoding
  provider API into the Weather API using OpenFeign, DTOs, and Mappers.
---

# Integração de Novo Provedor de Clima (Weather API)

Este procedimento define o padrão arquitetural para estender a Weather API com um novo serviço meteorológico externo.

## Arquitetura de Integração

A Weather API adota o padrão Facade + Mappers para isolar os detalhes de fornecedores externos do modelo de domínio consumido pelos clientes.

```mermaid
flowchart LR
    ExternalAPI[Nova API Externa] --> FeignClient[Novo FeignClient]
    FeignClient --> ProviderDTO[DTO do Provedor]
    ProviderDTO --> Mapper[Novo ResponseMapper]
    Mapper --> DadosClimaticos[DadosClimaticos (Modelo Canônico)]
    DadosClimaticos --> PrevisaoService[PrevisaoService / Facade]
```

## Passo a Passo

### 1. Criar o Cliente Feign (`src/main/java/ifsc/edu/lll/client/`)
Crie uma interface anotada com `@FeignClient`:
```java
@FeignClient(name = "novoProvedorClient", url = "https://api.novo-provedor.com")
public interface NovoProvedorClient {
    @GetMapping("/v1/weather")
    NovoProvedorResponse buscaClima(
        @RequestParam("lat") double lat,
        @RequestParam("lon") double lon,
        @RequestParam("apikey") String apiKey
    );
}
```

### 2. Criar os DTOs do Provedor (`src/main/java/ifsc/edu/lll/dto/`)
Mapeie a resposta JSON da API externa utilizando `record` do Java:
```java
public record NovoProvedorResponse(
    double temp,
    double humidity,
    double rainProbability
) {}
```

### 3. Criar o Mapper (`src/main/java/ifsc/edu/lll/service/mapper/`)
Transforme os DTOs específicos do provedor no record canônico `DadosClimaticos`:
```java
@Component
public class NovoProvedorResponseMapper {
    public DadosClimaticos toDadosClimaticos(NovoProvedorResponse response, LocalDate data) {
        return new DadosClimaticos(
            data,
            response.temp(),
            response.temp(), // ou min/max
            response.rainProbability(),
            ClassificadorClima.classificar(...)
        );
    }
}
```

### 4. Integrar ao Serviço (`PrevisaoService.java`)
- Injete o novo cliente e mapper via construtor do `PrevisaoService`.
- Adicione a regra de seleção de provedor (por país, data, fallback ou preferência configurada).

### 5. Escrever Testes Unitários
- Crie teste unitário para o Mapper correspondente em `src/test/java/ifsc/edu/lll/service/mapper/`.
- Crie teste para o Service simulando o Feign Client com Mockito.
