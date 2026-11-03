---
name: implementar-autorizacao
description: >-
  Use this skill when the user asks to implement or update authentication, authorization,
  JWT security, or client isolation for alerts and SSE streams based on the EARS
  specification in specs/implementacao_autorizacao.md.
---

# Implementação de Autorização e Autenticação (Weather API)

Este runbook instrui o agente na execução completa da especificação EARS descrita em `specs/implementacao_autorizacao.md`.

## Objetivos da Implementação
- Adicionar segurança com Spring Security e JWT (`jjwt 0.12.6`).
- Proteger rotas de alertas (`POST /alertas`, `GET /alertas`, `GET /alertas/stream`).
- Manter públicas rotas de previsão (`/previsao/**`), health (`/ping`), auth (`/auth/**`) e Swagger (`/docs`, `/swagger-ui/**`, `/docs-json`).
- Isolar alertas e streams SSE por par `(clienteId, canalId)`.

---

## Roteiro de Execução Passo a Passo

### 1. Dependências no `build.gradle`
Adicionar ao bloco `dependencies`:
```groovy
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly    'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly    'io.jsonwebtoken:jjwt-jackson:0.12.6'
testImplementation 'org.springframework.security:spring-security-test'
```

### 2. Configurações em `src/main/resources/application.properties`
Configurar:
```properties
jwt.secret=${JWT_SECRET}
jwt.expiration-minutes=30
jwt.issuer=weather-api
sse.heartbeat-seconds=25
sse.max-conexoes-por-cliente=5
spring.h2.console.enabled=false
```
Criar `application-dev.properties` habilitando `spring.h2.console.enabled=true`.

### 3. Camada de Segurança (`ifsc.edu.lll.security`)
- **`ClienteAutenticado`**: Record com `Long id`, `String email`, `Instant expiraEm`.
- **`TokenInvalidoException`**: Exceção para tokens expirados ou corrompidos.
- **`JwtService`**: Geração e validação de tokens com HS256 e jjwt 0.12.6 (`verifyWith(chave).build().parseSignedClaims(token)`).
- **`JwtAuthenticationFilter`**: Estende `OncePerRequestFilter`, ignora caminhos `/auth/**`, lê header `Authorization: Bearer <token>` e injeta `ClienteAutenticado` no `SecurityContextHolder`.
- **`RestAuthenticationEntryPoint` & `RestAccessDeniedHandler`**: Retornam o envelope `ApiResponse` nos formatos 401 e 403 sem depender de serializadores complexos.
- **`SecurityConfig`**: Define a cadeia de filtros `SecurityFilterChain`:
  - `csrf.disable()`, `SessionCreationPolicy.STATELESS`.
  - Liberar `DispatcherType.ASYNC` e `DispatcherType.ERROR` (essencial para que o encerramento do SSE não seja bloqueado por autorização).
  - Rotas públicas: `POST /auth/clientes`, `POST /auth/login`, `GET /previsao/**`, `GET /ping`, `/docs/**`, `/swagger-ui/**`, `/docs-json/**`.
  - Qualquer outra rota: autenticada.
  - Bean de `PasswordEncoder` com `BCryptPasswordEncoder`.

### 4. Entidades e Repositórios
- **`Cliente` (`model/Cliente.java`)**:
  - Tabela `clientes`, campos: `id`, `nome` (max 100), `email` (max 150, unique), `senhaHash` (max 100), `criadoEm` (Instant).
- **`ClienteRepository`**: `findByEmail(String email)`, `existsByEmail(String email)`.
- **Alterações em `Alerta` (`model/Alerta.java`)**:
  - Adicionar coluna `clienteId` (obrigatória) e atualizar construtores e getters.
- **Alterações em `AlertaRepository`**:
  - Adicionar métodos: `findByClienteIdAndAtivoTrue(Long clienteId)`, `existsByClienteIdAndCanalId(Long clienteId, Long canalId)`.

### 5. Camada de Negócio e DTOs
- **DTOs (`dto/auth/`)**:
  - `ClienteRequestDTO`, `ClienteResponseDTO`, `LoginRequestDTO`, `TokenResponseDTO`.
- **`ClienteService`**: Cadastro de cliente com normalização de e-mail e hash BCrypt. Tratamento de e-mail duplicado (`EmailJaCadastradoException`).
- **`AuthService`**: Validação de credenciais com proteção contra timing attack (compara senha mesmo se cliente for nulo usando hash falso) e emissão de token JWT.
- **`AlertaStreamService`**:
  - Chave de conexões composta: `record CanalKey(Long clienteId, Long canalId)`.
  - Controle de conexões concorrentes, heartbeat periódico de ping (a cada 25s) e expiração do emitter alinhada à expiração do JWT.

### 6. Controllers e Tratamento de Exceções
- **`AuthController`**:
  - `POST /auth/clientes` -> 201 Created.
  - `POST /auth/login` -> 200 OK com `accessToken`.
- **`AlertaController`**:
  - Extrair o usuário via `@AuthenticationPrincipal ClienteAutenticado cliente`.
  - Vincular o alerta criado ao `cliente.id()`.
  - Validar canal habilitado antes de abrir stream (`existsByClienteIdAndCanalId`).
- **`GlobalExceptionHandler`**:
  - Adicionar handlers para `EmailJaCadastradoException` (409), `CredenciaisInvalidasException` (401), `CanalNaoHabilitadoException` (403), `LimiteConexoesException` (429).

### 7. Validação e Testes
- Configurar `@TestPropertySource(properties = "jwt.secret=segredo-muito-seguro-com-mais-de-32-bytes-para-os-testes")` em classes de teste.
- Executar testes automatizados:
  ```bash
  ./gradlew test
  ```
