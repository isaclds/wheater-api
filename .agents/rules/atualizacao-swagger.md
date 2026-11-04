# Regra de Sincronização Obrigatória do Swagger / OpenAPI (Weather API)

Esta regra é de cumprimento **obrigatório** para qualquer agente de IA ou desenvolvedor atuando neste repositório.

---

## 1. Princípio de Sincronização Contínua da Documentação

> [!IMPORTANT]
> **Toda alteração de comportamento, contrato ou regra de negócio na Weather API DEVE ser acompanhada da atualização correspondente na documentação do Swagger / OpenAPI (`/docs` e `/docs-json`).**

Nunca conclua uma tarefa que altere a API sem atualizar e verificar a documentação OpenAPI correspondente.

---

## 2. Gatilhos de Atualização Obrigatória

A documentação OpenAPI **deve** ser atualizada sempre que ocorrer qualquer um dos seguintes eventos:

1. **Alteração ou Criação de Endpoints / Controllers**:
   - Criação de novos endpoints (`@GetMapping`, `@PostMapping`, etc.).
   - Alteração de rotas, paths ou métodos HTTP.
   - Atualização ou adição de parâmetros de rota (`@PathVariable`), parâmetros de busca (`@RequestParam`) ou cabeçalhos (`@RequestHeader`).
   - Modificação das regras de negócio que afetam o que o endpoint faz, suas restrições ou seus fluxos alternativos.
   - Alteração dos códigos HTTP de retorno esperados (ex: 200, 201, 400, 401, 403, 404, 409, 422, 429, 500).

2. **Alteração em DTOs (Requests e Responses)**:
   - Adição, renomeação ou remoção de campos em `record` DTOs.
   - Alteração de regras de validação Jakarta Bean Validation (`@NotBlank`, `@NotNull`, `@Size`, `@Pattern`, `@Min`, `@Max`, `@Email`).
   - Alteração no envelope de resposta `ApiResponse<T>`.

3. **Mecanismos Transversais de Segurança e Infraestrutura**:
   - Políticas de controle de taxa (Rate Limiting) e cabeçalhos associados (`Retry-After`, `X-RateLimit-*`).
   - Métodos de autenticação e esquemas de segurança (`@SecurityRequirement(name = "bearerAuth")`, tokens JWT, headers de autenticação).
   - Novas políticas de acesso, permissões ou códigos de erro transversais (como 401, 403, 429).

---

## 3. Diretrizes de Atualização nos Códigos-Fonte

1. **Controllers**:
   - `@Operation`: Atualize `summary` e `description` refletindo as novas regras de negócio e restrições.
   - `@Parameter`: Documente tipo, exemplo e descrição de cada parâmetro.
   - `@ApiResponses`: Liste **todos** os códigos HTTP possíveis com suas respectivas descrições e schemas de retorno (`@Content(schema = @Schema(...))`).
   - `@SecurityRequirement`: Garanta que rotas autenticadas possuam `@SecurityRequirement(name = "bearerAuth")`.

2. **DTOs (`record`)**:
   - Enriqueça os campos com anotações `@Schema(description = "...", example = "...")` quando necessário para esclarecer formatos ou valores de exemplo.

3. **Configuração Global (`ifsc.edu.lll.config.OpenApiConfig`)**:
   - Atualize a descrição geral da API (`Info.description`) quando houver novas políticas transversais (como autenticação ou rate limiting).
   - Registre componentes globais (`Components.addResponses`, `Components.addSecuritySchemes`, etc.) ou customizadores (`OpenApiCustomizer`) quando o comportamento afetar múltiplos endpoints.

4. **Validação por Testes**:
   - Sempre execute a suíte de testes (`./gradlew test`) para certificar que o endpoint `/docs-json` gera o OpenAPI v3 válido sem quebrar integrações existentes.

Consulte a skill [`.agents/skills/atualizar-swagger/SKILL.md`](file:///home/aluno/wheater-api/.agents/skills/atualizar-swagger/SKILL.md) para o procedimento passo a passo.
