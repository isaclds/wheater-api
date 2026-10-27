# Regras de Padrões de Projeto e Código (Weather API)

As regras a seguir devem ser obedecidas por agentes de IA e desenvolvedores em qualquer modificação ou adição de código neste repositório:

## 1. Convenções de Código Java 25 & Spring Boot 4
- **Injeção de Dependências**: Utilize exclusivamente injeção por construtor. É terminantemente proibido o uso de `@Autowired` em campos ou atributos de classe.
- **DTOs e Imutabilidade**:
  - Todo DTO de entrada e saída deve ser declarado como `record`.
  - Nunca exponha entidades JPA (`@Entity`) diretamente em retornos de Controllers.
- **Validação de Entrada**:
  - DTOs de entrada devem utilizar anotações do Jakarta Validation (`@NotBlank`, `@NotNull`, `@Size`, `@Email`, etc.).
  - Os parâmetros dos Controllers devem ser anotados com `@Valid`.

## 2. Padrão de Resposta HTTP e Envelope
- Todas as respostas de Controllers REST devem encapsular os dados dentro da classe `ifsc.edu.lll.dto.response.ApiResponse<T>`.
- Formato obrigatório:
  - Sucesso: `ResponseEntity.ok(ApiResponse.success(dados))` ou `ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Mensagem", dados))`.
  - Erro: `ResponseEntity.status(codigo).body(ApiResponse.error(codigo, "Título Curto", detalhe))`.
- Nunca devolva tipos primitivos, listas puras ou entidades JPA desempacotadas sem o envelope `ApiResponse`.

## 3. Tratamento Centralizado de Exceções
- Exceções de negócio devem ser tratadas pelo `GlobalExceptionHandler` (`@RestControllerAdvice`).
- Nunca realize `catch (Exception e)` silencioso ou com `e.printStackTrace()`.
- Use a abstração `org.slf4j.Logger` via `LoggerFactory.getLogger(...)` para logs de erro e advertência.

## 4. OpenAPI / Swagger
- Todo novo endpoint deve ser documentado com anotações `@Tag`, `@Operation` e `@ApiResponses`.
- As rotas devem refletir adequadamente os códigos HTTP esperados (200, 201, 400, 401, 403, 404, 422, 500).
