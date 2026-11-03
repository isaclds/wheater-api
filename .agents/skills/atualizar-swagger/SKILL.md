---
name: atualizar-swagger
description: >-
  Use this skill whenever API business rules, endpoints, request or response DTOs, parameters,
  validation rules, headers, or HTTP status codes are added or modified in the Weather API to keep
  Swagger UI and OpenAPI documentation completely synchronized and compliant with project standards.
---

# Sincronização e Atualização do Swagger / OpenAPI (Weather API)

Este documento orienta agentes de IA e desenvolvedores sobre como manter a documentação Swagger / OpenAPI (`/docs` e `/docs-json`) 100% atualizada sempre que regras de negócio, contratos ou componentes da API forem alterados.

---

## 1. Princípio Fundamental

> [!IMPORTANT]
> **Nenhuma modificação de comportamento, endpoint, request ou regra na Weather API é considerada concluída sem que a documentação OpenAPI correspondente tenha sido atualizada e testada.**

---

## 2. Checklist de Verificação Rápida

Ao alterar qualquer código na API, verifique:

- [ ] **A regra de negócio ou objetivo do endpoint mudou?** -> Atualizar `summary` e `description` na anotação `@Operation`.
- [ ] **Novos parâmetros de URL ou query string foram criados/alterados?** -> Atualizar `@Parameter` com descrições e exemplos claros.
- [ ] **Novos status HTTP podem ser retornados (ex: 400, 401, 403, 404, 409, 422, 429)?** -> Declarar todos no `@ApiResponses`.
- [ ] **Campos de DTOs (`record`) de request/response foram alterados?** -> Validar `@Schema` e anotações do Jakarta Validation (`@NotBlank`, `@Size`, etc.).
- [ ] **A rota exige autenticação JWT?** -> Incluir `@SecurityRequirement(name = "bearerAuth")`.
- [ ] **A mudança é transversal (ex: Rate Limiting, autenticação, headers globais)?** -> Atualizar [`OpenApiConfig.java`](file:///home/aluno/wheater-api/src/main/java/ifsc/edu/lll/config/OpenApiConfig.java) (`Info.description`, `Components` e `OpenApiCustomizer`).
- [ ] **Os testes do OpenAPI passaram?** -> Executar `./gradlew test` garantindo que `/docs-json` gera o schema correto.

---

## 3. Guia de Implementação Passo a Passo

### Passo 1: Atualização dos Controllers (`src/main/java/ifsc/edu/lll/controller/`)

Em cada método alterado do Controller:

```java
@PostMapping("/exemplo")
@Operation(
    summary = "Título conciso da operação",
    description = "Descrição detalhada explicando as regras de negócio, restrições e comportamento esperado."
)
@ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", // ou 201
        description = "Operação realizada com sucesso",
        content = @Content(schema = @Schema(implementation = ExemploResponseDTO.class))
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Requisição malformada ou parâmetros inválidos"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "Não autenticado ou token ausente/inválido"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Acesso negado"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "Conflito de dados (ex: e-mail já existente)"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "Entidade improcessável (erros de validação de campos)"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "429",
        description = "Limite de requisições excedido (Rate limit)"
    )
})
public ResponseEntity<ApiResponse<ExemploResponseDTO>> metodo(
        @Parameter(description = "Identificador do recurso", example = "42", required = true)
        @PathVariable Long id,
        @Valid @RequestBody ExemploRequestDTO dto) {
    ...
}
```

### Passo 2: Atualização de DTOs (`src/main/java/ifsc/edu/lll/dto/`)

DTOs são sempre representados por `record` no Java. Para garantir clareza no Swagger:

```java
public record AlertaRequestDTO(
    @Schema(description = "ID do canal de notificação SSE", example = "42")
    @NotNull(message = "O canalId é obrigatório")
    Long canalId,

    @Schema(description = "País da cidade monitorada", example = "Brasil")
    @NotBlank(message = "O país é obrigatório")
    String pais,

    @Schema(description = "Temperatura máxima para disparo do alerta em °C", example = "35.5")
    Double tempMax
) {}
```

### Passo 3: Configuração Global em `OpenApiConfig.java`

Para regras transversais (como filtros de segurança, JWT, cabeçalhos de Rate Limiting ou políticas de versionamento):

1. **Descrição da API**: Adicione detalhes operacionais em `Info.description`:
   ```java
   .info(new Info()
       .title("Weather API")
       .description("""
           ...
           ### Rate Limiting
           A API limita requisições por IP. Quando o limite for atingido, retornará HTTP 429...
           """)
   ```

2. **Esquemas Reutilizáveis em `Components`**:
   Registre cabeçalhos e respostas comuns (`Components.addResponses(...)` ou `Components.addSecuritySchemes(...)`).

3. **Injeção Automática via `OpenApiCustomizer`**:
   Caso uma resposta se aplique a todos os endpoints (como HTTP 429 ou 500), utilize um `OpenApiCustomizer` para registrar a resposta automaticamente em todos os endpoints sem duplicar código.

---

## 4. Validação e Testes

Após atualizar o Swagger / OpenAPI:

1. Execute a suíte de testes de segurança e controladores:
   ```bash
   ./gradlew test --tests "ifsc.edu.lll.security.SecurityIntegrationTest"
   ```
2. Verifique se o endpoint `/docs-json` retorna status HTTP 200 e contém as informações atualizadas.
3. Execute todos os testes do projeto para assegurar que nada foi quebrado:
   ```bash
   ./gradlew test
   ```
