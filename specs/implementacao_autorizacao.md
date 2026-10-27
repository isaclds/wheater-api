# Autorização na Weather API: especificação EARS

Oct 27, 2026 · @Isac

## 1. Visão geral e escopo

A Weather API passa a exigir login (JWT) apenas nas rotas de alertas e SSE, e cada cliente só enxerga e ouve o que é dele. A previsão do tempo e o `/ping` continuam públicos.

O que muda para quem usa a API:

1. O cliente se cadastra (`POST /auth/clientes`) e faz login (`POST /auth/login`), recebendo um token JWT.
2. Toda chamada a `/alertas/**` leva o header `Authorization: Bearer <token>`.
3. Ao abrir um canal SSE, o token é enviado no mesmo header. O servidor só entrega eventos do canal daquele cliente.
4. Dois clientes podem usar o mesmo número de canal (ex.: 42) sem se misturar: o canal é identificado pelo par (cliente, número).

### Decisões já tomadas

| Tema | Decisão |
| --- | --- |
| Identificador de login | E-mail, único por cliente |
| Papéis (roles) | Não há. Todo cliente é igual; o isolamento é por dono |
| Habilitação de canal | O primeiro alerta cadastrado com um `canalId` reivindica o canal para o cliente |
| Rotas públicas | `/auth/**`, `/previsao/**`, `/ping`, Swagger (`/docs`, `/swagger-ui/**`, `/docs-json`) |
| Rotas protegidas | `/alertas` (POST, GET) e `/alertas/stream` |
| Transporte do token | Header `Authorization: Bearer <JWT>` em todas as rotas protegidas, inclusive SSE |
| Notação dos requisitos | EARS (Easy Approach to Requirements Syntax) |

### Notação EARS usada

| Padrão | Forma | Exemplo desta entrega |
| --- | --- | --- |
| Ubíquo | O sistema deve ... | **JWT-01** O sistema deve emitir tokens JWT assinados com HS256. |
| Orientado a evento | Quando `<evento>`, o sistema deve ... | **LOG-01** Quando receber `POST /auth/login` com `email` e `senha` corretos, o sistema deve responder `200 OK` com `accessToken`. |
| Orientado a estado | Enquanto `<estado>`, o sistema deve ... | **SSE-08** Enquanto um stream estiver aberto, o sistema deve encerrá-lo no instante em que o token usado para abri-lo expirar. |
| Comportamento indesejado | Se `<condição indesejada>`, então o sistema deve ... | **SSE-02** Se o cliente pedir um canal que não está habilitado para ele, então o sistema deve responder `403 Forbidden` sem abrir o stream. |
| Funcionalidade opcional | Onde `<recurso existir>`, o sistema deve ... | **SSE-10** Onde `sse.max-conexoes-por-cliente` estiver definido, o sistema deve responder `429` ao exceder o limite. |

## 2. Análise do projeto atual

A Weather API (`isaclds/wheater-api`) está bem organizada em camadas, mas hoje não tem nenhuma autenticação: qualquer pessoa cadastra alertas, lista os de todos e escuta qualquer canal SSE.

### Visão técnica

| Item | Situação atual |
| --- | --- |
| Stack | Java 25, Spring Boot 4.1.0, Gradle, Feign, Spring Data JPA, H2 em memória, springdoc |
| Segurança | Sem `spring-boot-starter-security` nas dependências |
| Camadas | controller, service, client (Feign), mapper, repository, model, dto, exception, config |
| Resposta padrão | Envelope `ApiResponse` com `status`, `success`, `title`, `data` |
| Erros | `GlobalExceptionHandler`: 422 (validação), 404 (`IllegalArgumentException`), 500 (qualquer outra) |
| Agendamento | `AlertaMonitorService` roda a cada 15 s e consulta a previsão de cada alerta ativo |
| Banco | `ddl-auto=create-drop`: os dados somem a cada reinício |

### Rotas existentes

| Método | Rota | Hoje | Depois |
| --- | --- | --- | --- |
| GET | `/previsao/**` | Aberta | Aberta |
| GET | `/ping` | Aberta | Aberta |
| POST | `/alertas` | Aberta | Protegida |
| GET | `/alertas` | Aberta, lista alertas de todos | Protegida, lista só os do cliente |
| GET | `/alertas/stream?canalId=` | Aberta, qualquer um ouve qualquer canal | Protegida, só canais do cliente |
| POST | `/auth/clientes` | Não existe | Aberta |
| POST | `/auth/login` | Não existe | Aberta |

### Como o SSE funciona hoje

`AlertaStreamService` guarda um `Map<Long, List<SseEmitter>>` indexado só pelo número do canal. `registrar(canalId)` cria um emitter sem timeout e `enviar(canalId, alerta)` entrega o evento `alerta-disparado` a todos os emitters daquele número. O `Alerta` tem `canalId`, mas não tem dono.

### Lacunas que a autorização precisa fechar

1. **Vazamento entre clientes.** Dois clientes que usem o canal 42 recebem os alertas um do outro, porque a chave do mapa é só o número.
2. **Canal sem dono.** Nada impede alguém de abrir `?canalId=1` e escutar alertas alheios.
3. **Listagem global.** `GET /alertas` devolve todos os alertas ativos de todos.
4. **Erros de segurança virariam 500.** O `GlobalExceptionHandler` captura `Exception`. Um `AccessDeniedException` lançado por `@PreAuthorize` cairia nesse handler e viraria 500 em vez de 403.
5. **Console H2 público.** `/h2-console` está habilitado sem proteção.
6. **Limpeza de conexões incompleta.** O emitter registra `onCompletion` e `onTimeout`, mas não `onError`. Com a chave composta, a remoção precisa ser feita sem risco de `NullPointerException`.
7. **Disparo repetido.** O monitor reenvia o evento a cada 15 s enquanto a condição for verdadeira. Não é problema de segurança, mas afeta o volume de eventos por cliente (ver seção 9).
8. **Documentação divergente.** O README cita `/swagger-ui/index.html` e `/v3/api-docs`, mas o `application.properties` configura `/docs` e `/docs-json`.

## 3. Diretrizes de implementação

Esta seção fixa as escolhas técnicas para quem for implementar, pessoa ou agente de código. O código completo de referência está na seção 7; os requisitos, na seção 4.

### 3.1 Ambiente e dependências

| Item | Valor |
| --- | --- |
| Linguagem e build | Java 25, Gradle (já no projeto) |
| Framework | Spring Boot 4.1.0 (já no projeto) |
| Segurança | `spring-boot-starter-security` (versão gerenciada pelo Boot; traz o BCrypt) |
| JWT | jjwt 0.12.6: `jjwt-api`, `jjwt-impl`, `jjwt-jackson` |
| Pacote base | `ifsc.edu.lll` (os arquivos do exemplo usam `pbe`; não copiar esse pacote) |
| Dependências proibidas | Qualquer outra biblioteca de JWT ou de segurança além das listadas |

```groovy
// build.gradle, dentro de dependencies { ... }
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly    'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly    'io.jsonwebtoken:jjwt-jackson:0.12.6'
testImplementation 'org.springframework.security:spring-security-test'
```

### 3.2 Configuração

```properties
# application.properties
jwt.secret=${JWT_SECRET}
jwt.expiration-minutes=30
jwt.issuer=weather-api
sse.heartbeat-seconds=25
sse.max-conexoes-por-cliente=5
spring.h2.console.enabled=false
```

- `JWT_SECRET` vem só do ambiente, com pelo menos 32 bytes. Sem valor padrão no código nem no `.properties`.
- Os testes definem o segredo por `@TestPropertySource(properties = "jwt.secret=...")` com um valor de teste de 32 bytes ou mais.
- O console do H2 só liga no perfil `dev` (`application-dev.properties`).

### 3.3 Arquivos a criar e alterar

Caminhos relativos a `src/main/java/ifsc/edu/lll/`.

| Caminho | Ação |
| --- | --- |
| `security/JwtService`, `ClienteAutenticado`, `TokenInvalidoException` | Criar |
| `security/JwtAuthenticationFilter`, `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`, `RespostaJson`, `SecurityConfig` | Criar |
| `model/Cliente`, `repository/ClienteRepository` | Criar |
| `dto/auth/ClienteRequestDTO`, `ClienteResponseDTO`, `LoginRequestDTO`, `TokenResponseDTO` | Criar |
| `service/ClienteService`, `service/AuthService`, `controller/AuthController` | Criar |
| `exception/EmailJaCadastradoException`, `CredenciaisInvalidasException`, `CanalNaoHabilitadoException`, `LimiteConexoesException` | Criar |
| `model/Alerta`, `repository/AlertaRepository` | Alterar: dono (`clienteId`) e novas consultas |
| `service/AlertaService`, `AlertaStreamService`, `AlertaMonitorService` | Alterar: receber `clienteId`; mapa por (cliente, canal) |
| `controller/AlertaController` | Alterar: ler o cliente do token |
| `exception/GlobalExceptionHandler`, `config/OpenApiConfig` | Alterar: novos handlers e esquema `bearerAuth` |
| `build.gradle`, `application.properties` | Alterar |
| `src/test/...` | Ajustar os testes existentes e criar os 30 cenários da seção 8 |

Não alterar: `PrevisaoController`, `HealthController`, clients, mappers e os DTOs de previsão.

### 3.4 Formatos

| Item | Formato |
| --- | --- |
| Header | `Authorization: Bearer <JWT>`, esquema sem diferenciar maiúsculas |
| Algoritmo | HS256 |
| Claims do JWT | `iss`=`weather-api`, `sub`=e-mail, `cid`=id do cliente (número), `iat`, `exp` |
| Resposta do login | `{ "accessToken": "...", "tokenType": "Bearer", "expiresIn": 1800 }` dentro de `data` do envelope `ApiResponse` |
| Erros | Sempre o envelope `ApiResponse` com `success=false`, `title` curto em português e `data=null` |
| E-mail | Salvo em minúsculas e sem espaços nas pontas |
| Senha | Entre 8 e 72 caracteres; salva só como hash BCrypt |
| Eventos SSE | `conectado`, `ping` e `alerta-disparado` (corpo `AlertaDisparoDTO`, sem mudança) |
| Códigos HTTP | 201 cadastro, 200 login, 401 sem credencial válida, 403 canal de outro cliente, 409 e-mail repetido, 422 dados inválidos, 429 limite de conexões |

Os corpos completos de cada rota estão na seção 5.

### 3.5 Regras de implementação

Fazer:

- Injeção por construtor, sem `@Autowired` em campo.
- API atual do jjwt: `signWith(SecretKey)` para assinar e `parser().verifyWith(chave).build().parseSignedClaims(token)` para validar.
- Criar o filtro JWT dentro do `SecurityConfig` (`new JwtAuthenticationFilter(jwtService)`), sem `@Component`, para o Spring Boot não registrá-lo em duplicidade.
- Sessão `STATELESS` e CSRF desligado.
- Negar por padrão: só as rotas do requisito SEC-01 ficam abertas.
- Tirar o dono do alerta sempre do token, nunca do corpo da requisição.
- Mesma resposta (mensagem e código) para e-mail inexistente e senha errada.

Não fazer:

- Segredo, senha, hash ou token no código-fonte, em log ou em mensagem de erro.
- Papéis (`ROLE_*`) e a claim `role`.
- Aceitar o token por query string ou cookie.
- Usar `ObjectMapper` nos handlers de segurança (o JSON é escrito à mão, ver 7.4).
- Renomear rotas, eventos SSE ou campos dos DTOs que já existem.

### 3.6 Armadilhas do SSE com segurança

1. O `EventSource` do navegador não envia headers. O front-end usa `fetch` com leitura de stream, por exemplo `@microsoft/fetch-event-source`.
2. Ao fechar um `SseEmitter`, o container faz um despacho `ASYNC`. O `SecurityConfig` precisa liberar `DispatcherType.ASYNC` e `ERROR`; a autorização já ocorreu na requisição original.
3. O cliente do stream manda `Accept: text/event-stream`. Os handlers de erro do `AlertaController` fixam `Content-Type: application/json` para não virar 406.
4. O `SseEmitter` nasce com timeout igual ao tempo que falta para o token expirar, e o stream fecha junto com ele.
5. Remover a conexão do mapa em `onCompletion`, `onTimeout` e `onError`, apagando a chave quando a lista esvaziar.

## 4. Requisitos EARS

São 60 requisitos em 8 grupos. Cada um tem um identificador estável (ex.: `SSE-04`) para ser citado em testes, issues e PRs. "O sistema" é a Weather API; "cliente" é quem se cadastra para usar alertas.

### 4.1 Cadastro de clientes (CLI)

- **CLI-01** (evento) Quando receber `POST /auth/clientes` com `nome`, `email` e `senha` válidos, o sistema deve criar o cliente e responder `201 Created` com `id`, `nome` e `email`.
- **CLI-02** (ubíquo) O sistema deve exigir `nome` não vazio com até 100 caracteres, `email` em formato válido com até 150 caracteres e `senha` com 8 a 72 caracteres.
- **CLI-03** (ubíquo) O sistema deve gravar o e-mail em minúsculas e sem espaços nas pontas, e tratar `Ana@x.com` e `ana@x.com` como o mesmo e-mail.
- **CLI-04** (indesejado) Se o e-mail já estiver cadastrado, então o sistema deve responder `409 Conflict` e não criar outro cliente.
- **CLI-05** (ubíquo) O sistema deve gravar a senha somente como hash BCrypt.
- **CLI-06** (ubíquo) O sistema não deve devolver a senha nem o hash em nenhuma resposta, log ou mensagem de erro.
- **CLI-07** (indesejado) Se algum campo for inválido, então o sistema deve responder `422 Unprocessable Entity` listando os campos com erro.

### 4.2 Login (LOG)

- **LOG-01** (evento) Quando receber `POST /auth/login` com `email` e `senha` corretos, o sistema deve responder `200 OK` com `accessToken`, `tokenType` igual a `Bearer` e `expiresIn` em segundos.
- **LOG-02** (indesejado) Se o e-mail não existir ou a senha estiver errada, então o sistema deve responder `401 Unauthorized` com a mesma mensagem genérica nos dois casos ("Credenciais inválidas").
- **LOG-03** (ubíquo) O sistema deve comparar a senha com o hash usando `PasswordEncoder.matches`.
- **LOG-04** (indesejado) Se o corpo do login estiver ausente ou com campo vazio, então o sistema deve responder `422`.
- **LOG-05** (opcional) Onde a proteção contra força bruta estiver habilitada, o sistema deve bloquear por 15 minutos o par e-mail + IP após 5 falhas seguidas e responder `429 Too Many Requests`.

### 4.3 Token JWT (JWT)

- **JWT-01** (ubíquo) O sistema deve emitir tokens JWT assinados com HS256.
- **JWT-02** (ubíquo) O sistema deve ler o segredo de assinatura da variável de ambiente `JWT_SECRET`, sem valor padrão no código.
- **JWT-03** (indesejado) Se `JWT_SECRET` estiver ausente ou tiver menos de 32 bytes, então o sistema deve falhar na inicialização com mensagem clara.
- **JWT-04** (ubíquo) O sistema deve incluir no token as claims `sub` (e-mail do cliente), `cid` (id do cliente), `iss` (`weather-api`), `iat` e `exp`.
- **JWT-05** (ubíquo) O sistema deve definir `exp` como `iat` mais o valor de `jwt.expiration-minutes`, cujo padrão é 30.
- **JWT-06** (ubíquo) O sistema não deve colocar senha, hash ou outros dados pessoais no token.
- **JWT-07** (evento) Quando receber um token, o sistema deve aceitá-lo somente se a assinatura, o `iss` e o `exp` forem válidos.
- **JWT-08** (ubíquo) O sistema deve validar o token sem consultar o banco, usando apenas as claims (modo sem estado).

### 4.4 Proteção das rotas (SEC)

- **SEC-01** (ubíquo) O sistema deve permitir sem token: `POST /auth/clientes`, `POST /auth/login`, `GET /previsao/**`, `GET /ping` e a documentação OpenAPI (`/docs`, `/swagger-ui/**`, `/docs-json`).
- **SEC-02** (ubíquo) O sistema deve exigir token válido em `POST /alertas`, `GET /alertas` e `GET /alertas/stream`.
- **SEC-03** (ubíquo) O sistema deve exigir autenticação em qualquer rota não listada em SEC-01 (negar por padrão).
- **SEC-04** (evento) Quando uma rota protegida receber o header `Authorization: Bearer <token>` com token válido, o sistema deve identificar o cliente pelo token e processar a requisição.
- **SEC-05** (indesejado) Se uma rota protegida não receber o header `Authorization`, então o sistema deve responder `401`.
- **SEC-06** (indesejado) Se o header `Authorization` não usar o esquema `Bearer` ou o token estiver malformado, com assinatura inválida ou expirado, então o sistema deve responder `401`.
- **SEC-07** (indesejado) Se o token vier na URL (`?token=`) ou em cookie, então o sistema deve ignorá-lo e tratar a requisição como sem credencial.
- **SEC-08** (ubíquo) O sistema deve operar sem sessão (`STATELESS`) e não deve emitir `JSESSIONID`.
- **SEC-09** (ubíquo) O sistema deve manter o CSRF desligado, pois a autenticação usa header e não cookie.
- **SEC-10** (ubíquo) O sistema deve executar o filtro JWT somente fora de `/auth/**`.

### 4.5 Alertas por cliente (ALR)

- **ALR-01** (evento) Quando um cliente autenticado enviar `POST /alertas` válido, o sistema deve criar o alerta com o cliente do token como dono e responder `201`.
- **ALR-02** (ubíquo) O sistema deve ignorar qualquer identificador de dono enviado no corpo; o dono vem sempre do token.
- **ALR-03** (evento) Quando um cliente autenticado enviar `GET /alertas`, o sistema deve devolver somente os alertas ativos dele.
- **ALR-04** (ubíquo) O sistema deve aceitar o mesmo `canalId` em clientes diferentes, tratando o canal como o par (cliente, `canalId`).
- **ALR-05** (evento) Quando um alerta for cadastrado com um `canalId` que o cliente ainda não usava, o sistema deve habilitar esse canal para o cliente.
- **ALR-06** (evento) Quando as condições de um alerta forem atingidas, o sistema deve enviar o evento `alerta-disparado` somente aos streams abertos do dono do alerta naquele `canalId`.
- **ALR-07** (indesejado) Se o dono do alerta não tiver stream aberto no canal, então o sistema não deve entregar o evento a nenhum outro cliente.
- **ALR-08** (ubíquo) O sistema deve manter nas respostas de alertas o mesmo formato de hoje (`id`, `canalId`, `pais`, `cidade`, limites, `ativo`).

### 4.6 Canais SSE (SSE)

- **SSE-01** (evento) Quando um cliente autenticado enviar `GET /alertas/stream?canalId=N` para um canal habilitado para ele, o sistema deve abrir um stream `text/event-stream`.
- **SSE-02** (indesejado) Se o cliente pedir um canal que não está habilitado para ele, então o sistema deve responder `403 Forbidden` sem abrir o stream, com a mesma resposta para canal inexistente e canal de outro cliente.
- **SSE-03** (indesejado) Se `canalId` estiver ausente ou não for um número positivo, então o sistema deve responder `400` (ausente ou formato errado) ou `422` (valor inválido).
- **SSE-04** (ubíquo) O sistema deve indexar as conexões SSE pela chave (`clienteId`, `canalId`), nunca só por `canalId`.
- **SSE-05** (evento) Quando o stream abrir, o sistema deve enviar de imediato o evento `conectado` com o `canalId`.
- **SSE-06** (estado) Enquanto um stream estiver aberto, o sistema deve enviar o evento `ping` a cada 25 segundos para manter a conexão viva.
- **SSE-07** (ubíquo) O sistema deve permitir várias conexões do mesmo cliente no mesmo canal e entregar o evento a todas.
- **SSE-08** (estado) Enquanto um stream estiver aberto, o sistema deve encerrá-lo no instante em que o token usado para abri-lo expirar.
- **SSE-09** (evento) Quando uma conexão terminar por conclusão, tempo esgotado ou erro, o sistema deve removê-la do registro e apagar a chave se não restarem conexões.
- **SSE-10** (opcional) Onde `sse.max-conexoes-por-cliente` estiver definido (padrão 5), o sistema deve responder `429` ao exceder o limite.
- **SSE-11** (ubíquo) O sistema deve manter o nome do evento (`alerta-disparado`) e o corpo (`AlertaDisparoDTO`) como são hoje.

### 4.7 Erros e respostas (ERR)

- **ERR-01** (ubíquo) O sistema deve devolver erros de segurança (401, 403, 429) no envelope `ApiResponse` com `status`, `success=false`, `title` e `data`.
- **ERR-02** (evento) Quando o Spring Security lançar `AuthenticationException` ou `AccessDeniedException`, o sistema deve responder 401 ou 403, e não 500.
- **ERR-03** (ubíquo) O sistema deve usar a mesma mensagem genérica para falhas de credencial, sem indicar se o e-mail existe.
- **ERR-04** (ubíquo) O sistema deve responder 401 com o header `WWW-Authenticate: Bearer`.
- **ERR-05** (ubíquo) O sistema não deve incluir stack trace, nome de classe ou detalhe do JWT em respostas de erro.

### 4.8 Operação e segurança (OPS)

- **OPS-01** (ubíquo) O sistema deve registrar em log os eventos de cadastro, login com sucesso, login com falha e acesso negado, sem registrar senha, hash ou token.
- **OPS-02** (estado) Enquanto o perfil ativo não for `dev`, o sistema deve manter `/h2-console` desabilitado.
- **OPS-03** (ubíquo) O sistema deve usar BCrypt com fator de custo mínimo 10.
- **OPS-04** (ubíquo) O sistema deve documentar no OpenAPI o esquema `bearerAuth` (HTTP, `bearer`, `JWT`) e marcá-lo nas rotas protegidas.
- **OPS-05** (ubíquo) O sistema deve ter ao menos um teste automatizado para cada requisito deste documento (seção 8).
- **OPS-06** (opcional) Onde o front-end rodar em outra origem, o sistema deve configurar CORS para permitir o header `Authorization` apenas nas origens listadas em configuração.

## 5. Contratos da API

Todas as respostas seguem o envelope `ApiResponse` já usado no projeto. Apenas as rotas de alertas mudam de comportamento; o corpo delas continua o mesmo.

| Método | Rota | Token | Sucesso | Erros |
| --- | --- | --- | --- | --- |
| POST | `/auth/clientes` | Não | 201 | 409, 422 |
| POST | `/auth/login` | Não | 200 | 401, 422, 429 (opcional) |
| POST | `/alertas` | Sim | 201 | 401, 422 |
| GET | `/alertas` | Sim | 200 | 401 |
| GET | `/alertas/stream?canalId=` | Sim | 200 (stream) | 400, 401, 403, 422, 429 (opcional) |

### POST /auth/clientes

```json
// Requisição
{ "nome": "Ana Souza", "email": "ana@exemplo.com", "senha": "umaSenhaForte123" }

// 201 Created
{
  "status": 201, "success": true, "title": "Cliente cadastrado com sucesso",
  "data": { "id": 7, "nome": "Ana Souza", "email": "ana@exemplo.com" }
}

// 409 Conflict
{ "status": 409, "success": false, "title": "E-mail já cadastrado", "data": null }
```

### POST /auth/login

```json
// Requisição
{ "email": "ana@exemplo.com", "senha": "umaSenhaForte123" }

// 200 OK
{
  "status": 200, "success": true, "title": "OK",
  "data": { "accessToken": "eyJhbGciOi...", "tokenType": "Bearer", "expiresIn": 1800 }
}

// 401 Unauthorized (e-mail inexistente ou senha errada)
{ "status": 401, "success": false, "title": "Credenciais inválidas", "data": null }
```

### Rotas protegidas

Todas levam o header abaixo. O token é o `accessToken` do login.

```http
Authorization: Bearer eyJhbGciOi...
```

`POST /alertas` mantém o corpo atual, sem campo de dono:

```json
{ "canalId": 42, "pais": "Brasil", "cidade": "Florianópolis", "temperaturaMaxima": 35.0, "precipitacao": 20.0 }
```

`GET /alertas` devolve só os alertas do cliente do token.

### GET /alertas/stream?canalId=42

```http
GET /alertas/stream?canalId=42
Authorization: Bearer eyJhbGciOi...
Accept: text/event-stream
```

O stream emite estes eventos:

| Evento | Quando | Dados |
| --- | --- | --- |
| `conectado` | Logo após abrir | `{ "canalId": 42 }` |
| `ping` | A cada 25 s | vazio |
| `alerta-disparado` | Condição do alerta atingida | `AlertaDisparoDTO` (igual a hoje) |

```text
event: conectado
data: {"canalId":42}

event: alerta-disparado
data: {"alertaId":1,"cidade":"Florianópolis","pais":"BR","previsao":{...}}
```

Se o canal não for do cliente, a resposta é um JSON comum (o stream não chega a abrir):

```json
{ "status": 403, "success": false, "title": "Canal não habilitado para este cliente", "data": null }
```

### Códigos de erro de segurança

| Código | Quando | `title` |
| --- | --- | --- |
| 401 | Sem header, esquema diferente de Bearer, token malformado, assinatura inválida ou expirado | "Não autenticado" ou "Token expirado" |
| 401 | Login com credencial errada | "Credenciais inválidas" |
| 403 | Canal que não é do cliente | "Canal não habilitado para este cliente" |
| 409 | E-mail já cadastrado | "E-mail já cadastrado" |
| 422 | Campo inválido | "Dados inválidos" |
| 429 | Limite de tentativas ou de conexões (opcional) | "Muitas requisições" |

## 6. Modelo de dados

Só há uma tabela nova (`clientes`) e uma coluna nova em `alertas` (`cliente_id`). O canal não vira tabela: ele existe quando o cliente tem ao menos um alerta com aquele `canalId`.

### Tabela clientes (nova)

| Coluna | Tipo | Regra |
| --- | --- | --- |
| `id` | BIGINT, PK, identidade | Gerado pelo banco |
| `nome` | VARCHAR(100) | Obrigatório |
| `email` | VARCHAR(150) | Obrigatório, único, sempre em minúsculas |
| `senha_hash` | VARCHAR(100) | Obrigatório, hash BCrypt (60 caracteres) |
| `criado_em` | TIMESTAMP | Preenchido no cadastro |

### Tabela alertas (alterada)

| Coluna | Mudança |
| --- | --- |
| `cliente_id` | Nova. BIGINT, obrigatória, chave estrangeira para `clientes.id` |
| `canal_id` | Sem mudança de tipo. Passa a ser único só dentro do cliente |
| Índice | Novo índice em (`cliente_id`, `canal_id`) para validar o canal no SSE e filtrar a listagem |

### Relação

Um cliente tem muitos alertas (1:N). O canal é o par (`cliente_id`, `canal_id`) e pode ter vários alertas.

### Classes a criar ou alterar

| Classe | Ação |
| --- | --- |
| `model/Cliente` | Nova entidade JPA |
| `repository/ClienteRepository` | Novo: `findByEmail`, `existsByEmail` |
| `model/Alerta` | Recebe `clienteId` no construtor e getter |
| `repository/AlertaRepository` | Novos: `findByClienteIdAndAtivoTrue`, `existsByClienteIdAndCanalId` |
| `service/ClienteService` | Novo: cadastro com hash e e-mail normalizado |
| `service/AuthService` | Novo: valida credenciais e emite o token |
| `service/AlertaService` | `cadastrar` e `listarAtivos` passam a receber o id do cliente |
| `service/AlertaStreamService` | Chave do mapa passa de `Long` para `CanalKey(clienteId, canalId)` |
| `service/AlertaMonitorService` | Envia usando `alerta.getClienteId()` e `alerta.getCanalId()` |
| `controller/AuthController` | Novo: `/auth/clientes` e `/auth/login` |
| `controller/AlertaController` | Lê o cliente do token (`@AuthenticationPrincipal`) |
| `security/*` | Novos: `JwtService`, `JwtAuthenticationFilter`, `SecurityConfig`, handlers 401/403 |
| `exception/GlobalExceptionHandler` | Novos handlers: 409, 403, 401 |
| `config/OpenApiConfig` | Adiciona o esquema `bearerAuth` |
| `build.gradle` | Adiciona `spring-boot-starter-security` e jjwt 0.12.x |

## 7. Código de referência melhorado

O código abaixo é a versão melhorada do seu exemplo, já adaptada ao pacote `ifsc.edu.lll` e ao Spring Boot 4. Ele ainda não foi compilado contra o repositório: trate como ponto de partida e valide com `./gradlew test`. Cada trecho indica os requisitos que cobre.

### 7.1 Dependências e propriedades

```groovy
// build.gradle, dentro de dependencies { ... }
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly    'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly    'io.jsonwebtoken:jjwt-jackson:0.12.6'
testImplementation 'org.springframework.security:spring-security-test'
// No Boot 4 o starter de testes de segurança pode se chamar spring-boot-starter-security-test; confirmar no build.
```

```properties
# application.properties
jwt.secret=${JWT_SECRET}
jwt.expiration-minutes=30
jwt.issuer=weather-api
sse.heartbeat-seconds=25
sse.max-conexoes-por-cliente=5
spring.h2.console.enabled=false
```

```properties
# application-dev.properties (perfil dev)
spring.h2.console.enabled=true
```

Para rodar: `export JWT_SECRET="uma-chave-com-no-minimo-32-caracteres-aleatorios"`. Cobre JWT-02, JWT-03, OPS-02.

### 7.2 Serviço JWT

Substitui o `JwtUtil`. Usa a API atual do jjwt, lê o segredo do ambiente e faz um único parse por token. Cobre JWT-01 a JWT-08.

```java
package ifsc.edu.lll.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/** Dados do cliente extraídos de um token válido. É o principal do Spring Security. */
public record ClienteAutenticado(Long id, String email, Instant expiraEm) {}

public class TokenInvalidoException extends RuntimeException {
    private final boolean expirado;
    public TokenInvalidoException(boolean expirado, Throwable causa) {
        super("Token inválido", causa);
        this.expirado = expirado;
    }
    public boolean isExpirado() { return expirado; }
}

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoSegundos;
    private final String emissor;

    public JwtService(@Value("${jwt.secret}") String segredo,
                      @Value("${jwt.expiration-minutes:30}") long expiracaoMinutos,
                      @Value("${jwt.issuer:weather-api}") String emissor) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 bytes");
        }
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.expiracaoSegundos = expiracaoMinutos * 60;
        this.emissor = emissor;
    }

    public String gerar(Long clienteId, String email) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .issuer(emissor)
                .subject(email)
                .claim("cid", clienteId)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expiracaoSegundos)))
                .signWith(chave)
                .compact();
    }

    public ClienteAutenticado validar(String token) {
        try {
            Claims c = Jwts.parser()
                    .verifyWith(chave)
                    .requireIssuer(emissor)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Number cid = c.get("cid", Number.class);
            if (cid == null) throw new JwtException("claim cid ausente");
            return new ClienteAutenticado(cid.longValue(), c.getSubject(), c.getExpiration().toInstant());
        } catch (ExpiredJwtException e) {
            throw new TokenInvalidoException(true, e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenInvalidoException(false, e);
        }
    }

    public long getExpiracaoSegundos() { return expiracaoSegundos; }
}
```

(Em um projeto real, cada tipo fica no seu próprio arquivo.)

### 7.3 Filtro de autenticação

Valida o header `Authorization`, monta o principal e, em caso de erro, apenas anota o motivo para o `AuthenticationEntryPoint` responder 401. Cobre SEC-04 a SEC-07 e SEC-10. Não leva `@Component`: ele é criado dentro do `SecurityConfig`, para o Spring Boot não registrá-lo uma segunda vez como filtro de servlet.

```java
package ifsc.edu.lll.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ATRIBUTO_ERRO = "jwt.erro";
    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.regionMatches(true, 0, PREFIXO, 0, PREFIXO.length())) {
            String token = header.substring(PREFIXO.length()).trim();
            try {
                ClienteAutenticado cliente = jwtService.validar(token);
                var autenticacao = new UsernamePasswordAuthenticationToken(cliente, null, List.of());
                autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            } catch (TokenInvalidoException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute(ATRIBUTO_ERRO, e.isExpirado() ? "expirado" : "invalido");
            }
        }
        chain.doFilter(request, response);
    }
}
```

### 7.4 Respostas 401 e 403 no envelope da API

Sem estes dois handlers, o Spring responde 403 sem corpo para quem não enviou token. Escrevem JSON à mão porque os títulos são constantes, o que evita depender de qual `ObjectMapper` (Jackson 2 ou 3) o Boot 4 expõe. Cobre SEC-05, SEC-06, ERR-01, ERR-04.

```java
package ifsc.edu.lll.security;

final class RespostaJson {
    private RespostaJson() {}

    static void erro(HttpServletResponse res, int status, String titulo) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"status\":" + status + ",\"success\":false,\"title\":\""
                + titulo + "\",\"data\":null}");
    }
}

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res,
                         AuthenticationException ex) throws IOException {
        boolean expirado = "expirado".equals(req.getAttribute(JwtAuthenticationFilter.ATRIBUTO_ERRO));
        res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        RespostaJson.erro(res, 401, expirado ? "Token expirado" : "Não autenticado");
    }
}

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res,
                       AccessDeniedException ex) throws IOException {
        RespostaJson.erro(res, 403, "Acesso negado");
    }
}
```

### 7.5 Configuração de segurança

Substitui o `SecurityConfig` do exemplo: nega por padrão, abre só as rotas públicas, desliga a sessão e liga os dois handlers. Cobre SEC-01, SEC-02, SEC-03, SEC-08, SEC-09, OPS-03.

```java
package ifsc.edu.lll.security;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtService jwtService,
                                           RestAuthenticationEntryPoint entryPoint,
                                           RestAccessDeniedHandler deniedHandler) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                    .authenticationEntryPoint(entryPoint)
                    .accessDeniedHandler(deniedHandler))
            .authorizeHttpRequests(auth -> auth
                    // A autorização já ocorreu no REQUEST. Sem isto, o fim de um SseEmitter
                    // (despacho ASYNC) é reavaliado sem credencial e quebra a resposta.
                    .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/clientes", "/auth/login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/previsao/**", "/ping").permitAll()
                    .requestMatchers("/docs", "/docs/**", "/docs-json", "/docs-json/**",
                                     "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthenticationFilter(jwtService),
                             UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // custo 10 (padrão)
    }
}
```

### 7.6 Cliente, cadastro e login

Cobre CLI-01 a CLI-07, LOG-01 a LOG-04 e OPS-03. O `AuthService` compara a senha mesmo quando o e-mail não existe, para que o tempo de resposta não revele quais e-mails estão cadastrados.

```java
// model/Cliente.java
@Entity
@Table(name = "clientes", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100) private String nome;
    @Column(nullable = false, length = 150) private String email;
    @Column(name = "senha_hash", nullable = false, length = 100) private String senhaHash;
    @Column(name = "criado_em", nullable = false, updatable = false) private Instant criadoEm = Instant.now();

    protected Cliente() {}
    public Cliente(String nome, String email, String senhaHash) {
        this.nome = nome; this.email = email; this.senhaHash = senhaHash;
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
}

// repository/ClienteRepository.java
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmail(String email);
    boolean existsByEmail(String email);
}

// dto/auth/*.java
public record ClienteRequestDTO(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Email @Size(max = 150) String email,
        // BCrypt aceita no máximo 72 BYTES; com acentos, 72 caracteres podem passar disso.
        @NotBlank @Size(min = 8, max = 72) String senha) {}

public record ClienteResponseDTO(Long id, String nome, String email) {
    public static ClienteResponseDTO de(Cliente c) {
        return new ClienteResponseDTO(c.getId(), c.getNome(), c.getEmail());
    }
}

public record LoginRequestDTO(@NotBlank @Email String email, @NotBlank String senha) {}

public record TokenResponseDTO(String accessToken, String tokenType, long expiresIn) {}

// exception/*.java
public class EmailJaCadastradoException extends RuntimeException {}
public class CredenciaisInvalidasException extends RuntimeException {}
public class CanalNaoHabilitadoException extends RuntimeException {}
public class LimiteConexoesException extends RuntimeException {}

// service/ClienteService.java
@Service
public class ClienteService {
    private final ClienteRepository repo;
    private final PasswordEncoder encoder;

    public ClienteService(ClienteRepository repo, PasswordEncoder encoder) {
        this.repo = repo; this.encoder = encoder;
    }

    @Transactional
    public ClienteResponseDTO cadastrar(ClienteRequestDTO dto) {
        String email = dto.email().trim().toLowerCase(Locale.ROOT);
        if (repo.existsByEmail(email)) throw new EmailJaCadastradoException();
        try {
            Cliente salvo = repo.saveAndFlush(
                    new Cliente(dto.nome().trim(), email, encoder.encode(dto.senha())));
            return ClienteResponseDTO.de(salvo);
        } catch (DataIntegrityViolationException corrida) {
            throw new EmailJaCadastradoException(); // dois cadastros simultâneos
        }
    }
}

// service/AuthService.java
@Service
public class AuthService {
    private final ClienteRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final String hashFalso;

    public AuthService(ClienteRepository repo, PasswordEncoder encoder, JwtService jwt) {
        this.repo = repo; this.encoder = encoder; this.jwt = jwt;
        this.hashFalso = encoder.encode("senha-que-nunca-sera-usada");
    }

    public TokenResponseDTO login(LoginRequestDTO dto) {
        String email = dto.email().trim().toLowerCase(Locale.ROOT);
        Cliente cliente = repo.findByEmail(email).orElse(null);
        boolean senhaOk = encoder.matches(dto.senha(),
                cliente != null ? cliente.getSenhaHash() : hashFalso);
        if (cliente == null || !senhaOk) throw new CredenciaisInvalidasException();
        return new TokenResponseDTO(jwt.gerar(cliente.getId(), cliente.getEmail()),
                "Bearer", jwt.getExpiracaoSegundos());
    }
}

// controller/AuthController.java
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Cadastro de clientes e login")
public class AuthController {
    private final ClienteService clienteService;
    private final AuthService authService;

    public AuthController(ClienteService clienteService, AuthService authService) {
        this.clienteService = clienteService; this.authService = authService;
    }

    @PostMapping("/clientes")
    public ResponseEntity<ApiResponse<ClienteResponseDTO>> cadastrar(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Cliente cadastrado com sucesso", clienteService.cadastrar(dto)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(dto)));
    }
}
```

### 7.7 Dono do alerta

Cobre ALR-01 a ALR-05.

```java
// model/Alerta.java (trechos que mudam)
@Table(name = "alertas",
       indexes = @Index(name = "idx_alertas_cliente_canal", columnList = "cliente_id, canal_id"))
public class Alerta {
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    public Alerta(Long clienteId, Long canalId, String pais, String cidade,
                  Double temperaturaMaxima, Double temperaturaMinima, Double precipitacao) {
        this.clienteId = clienteId;
        /* ...demais atribuições como hoje... */
    }
    public Long getClienteId() { return clienteId; }
}

// repository/AlertaRepository.java
List<Alerta> findByClienteIdAndAtivoTrue(Long clienteId);
boolean existsByClienteIdAndCanalId(Long clienteId, Long canalId);

// service/AlertaService.java
@Transactional
public AlertaResponseDTO cadastrar(Long clienteId, AlertaRequestDTO dto) {
    Alerta alerta = new Alerta(clienteId, dto.canalId(), dto.pais(), dto.cidade(),
            dto.temperaturaMaxima(), dto.temperaturaMinima(), dto.precipitacao());
    return AlertaResponseDTO.de(alertaRepository.save(alerta));
}

@Transactional(readOnly = true)
public List<AlertaResponseDTO> listarAtivos(Long clienteId) {
    return alertaRepository.findByClienteIdAndAtivoTrue(clienteId).stream()
            .map(AlertaResponseDTO::de).toList();
}

@Transactional(readOnly = true)
public boolean canalHabilitado(Long clienteId, Long canalId) {
    return alertaRepository.existsByClienteIdAndCanalId(clienteId, canalId);
}
```

### 7.8 Stream SSE isolado por cliente

A chave do mapa passa a ser (`clienteId`, `canalId`). O emitter nasce com timeout igual ao tempo que falta para o token expirar, o que atende SSE-08 sem tarefa extra. Cobre SSE-04 a SSE-09, SSE-11 e ALR-06.

```java
// service/AlertaStreamService.java
@Service
public class AlertaStreamService {

    public record CanalKey(Long clienteId, Long canalId) {}

    private final Map<CanalKey, List<SseEmitter>> conexoes = new ConcurrentHashMap<>();
    private final int maxConexoesPorCliente;

    public AlertaStreamService(@Value("${sse.max-conexoes-por-cliente:5}") int maxConexoesPorCliente) {
        this.maxConexoesPorCliente = maxConexoesPorCliente;
    }

    public SseEmitter registrar(Long clienteId, Long canalId, Instant tokenExpiraEm) {
        if (contarConexoes(clienteId) >= maxConexoesPorCliente) throw new LimiteConexoesException();

        long restanteMs = Math.max(1, Duration.between(Instant.now(), tokenExpiraEm).toMillis());
        CanalKey chave = new CanalKey(clienteId, canalId);
        SseEmitter emitter = new SseEmitter(restanteMs); // encerra junto com o token

        conexoes.computeIfAbsent(chave, k -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable limpar = () -> remover(chave, emitter);
        emitter.onCompletion(limpar);
        emitter.onTimeout(limpar);
        emitter.onError(e -> limpar.run());

        enviarEvento(emitter, SseEmitter.event().name("conectado").data(Map.of("canalId", canalId)));
        return emitter;
    }

    /** Entrega só aos streams do dono do alerta naquele canal. */
    public void enviar(Long clienteId, Long canalId, AlertaDisparoDTO alerta) {
        for (SseEmitter e : conexoes.getOrDefault(new CanalKey(clienteId, canalId), List.of())) {
            enviarEvento(e, SseEmitter.event().name("alerta-disparado").data(alerta));
        }
    }

    @Scheduled(fixedRateString = "${sse.heartbeat-seconds:25}", timeUnit = TimeUnit.SECONDS)
    public void heartbeat() {
        conexoes.values().forEach(lista -> lista.forEach(
                e -> enviarEvento(e, SseEmitter.event().name("ping").data(""))));
    }

    private void enviarEvento(SseEmitter emitter, SseEmitter.SseEventBuilder evento) {
        try {
            emitter.send(evento);
        } catch (IOException | IllegalStateException erro) {
            emitter.completeWithError(erro);
        }
    }

    private void remover(CanalKey chave, SseEmitter emitter) {
        conexoes.computeIfPresent(chave, (k, lista) -> {
            lista.remove(emitter);
            return lista.isEmpty() ? null : lista; // apaga a chave quando esvazia
        });
    }

    private long contarConexoes(Long clienteId) {
        return conexoes.entrySet().stream()
                .filter(en -> en.getKey().clienteId().equals(clienteId))
                .mapToLong(en -> en.getValue().size()).sum();
    }
}

// service/AlertaMonitorService.java: única linha que muda
stream.enviar(alerta.getClienteId(), alerta.getCanalId(), AlertaDisparoDTO.de(alerta, previsao));
```

### 7.9 AlertaController

O cliente vem do token (`@AuthenticationPrincipal`), nunca do corpo. Cobre ALR-01 a ALR-03, SSE-01 a SSE-03. Para o `@Positive` no parâmetro funcionar, a classe precisa de `@Validated`.

```java
@RestController
@RequestMapping("/alertas")
@Validated
@SecurityRequirement(name = "bearerAuth")
public class AlertaController {

    @PostMapping
    public ResponseEntity<ApiResponse<AlertaResponseDTO>> cadastrar(
            @AuthenticationPrincipal ClienteAutenticado cliente,
            @Valid @RequestBody AlertaRequestDTO dto) {
        AlertaResponseDTO alerta = alertaService.cadastrar(cliente.id(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Alerta cadastrado com sucesso", alerta));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertaResponseDTO>>> listar(
            @AuthenticationPrincipal ClienteAutenticado cliente) {
        return ResponseEntity.ok(ApiResponse.success(alertaService.listarAtivos(cliente.id())));
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter assinar(@AuthenticationPrincipal ClienteAutenticado cliente,
                              @RequestParam @Positive Long canalId) {
        if (!alertaService.canalHabilitado(cliente.id(), canalId)) {
            throw new CanalNaoHabilitadoException();
        }
        return stream.registrar(cliente.id(), canalId, cliente.expiraEm());
    }
}
```

### 7.10 Novos handlers de erro

Acrescentar ao `GlobalExceptionHandler`. Os handlers de 403 e 429 fixam `Content-Type: application/json` porque o cliente do stream manda `Accept: text/event-stream`, e sem isso o Spring responderia 406 em vez do JSON. Cobre CLI-04, LOG-02, SSE-02, SSE-03, SSE-10, ERR-02 a ERR-05.

```java
@ExceptionHandler(EmailJaCadastradoException.class)
public ResponseEntity<ApiResponse<Void>> emailDuplicado(EmailJaCadastradoException ex) {
    return json(HttpStatus.CONFLICT, "E-mail já cadastrado");
}

@ExceptionHandler(CredenciaisInvalidasException.class)
public ResponseEntity<ApiResponse<Void>> credenciais(CredenciaisInvalidasException ex) {
    return json(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
}

@ExceptionHandler(CanalNaoHabilitadoException.class)
public ResponseEntity<ApiResponse<Void>> canalNegado(CanalNaoHabilitadoException ex) {
    return json(HttpStatus.FORBIDDEN, "Canal não habilitado para este cliente");
}

@ExceptionHandler(LimiteConexoesException.class)
public ResponseEntity<ApiResponse<Void>> limiteConexoes(LimiteConexoesException ex) {
    return json(HttpStatus.TOO_MANY_REQUESTS, "Muitas requisições");
}

// Antes do handler genérico, estes dois caem hoje em 500:
@ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
public ResponseEntity<ApiResponse<Void>> parametroInvalido(Exception ex) {
    return json(HttpStatus.BAD_REQUEST, "Parâmetro ausente ou em formato inválido");
}

@ExceptionHandler(HandlerMethodValidationException.class) // @Positive no parâmetro
public ResponseEntity<ApiResponse<Void>> validacaoParametro(HandlerMethodValidationException ex) {
    return json(HttpStatus.UNPROCESSABLE_ENTITY, "Dados inválidos");
}

// Sem estes, AccessDeniedException e AuthenticationException virariam 500 no handler genérico:
@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ApiResponse<Void>> acessoNegado(AccessDeniedException ex) {
    return json(HttpStatus.FORBIDDEN, "Acesso negado");
}

@ExceptionHandler(AuthenticationException.class)
public ResponseEntity<ApiResponse<Void>> naoAutenticado(AuthenticationException ex) {
    return json(HttpStatus.UNAUTHORIZED, "Não autenticado");
}

private ResponseEntity<ApiResponse<Void>> json(HttpStatus status, String titulo) {
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON)
            .body(ApiResponse.error(status.value(), titulo, null));
}
```

### 7.11 OpenAPI e uso

Cobre OPS-04.

```java
// config/OpenApiConfig.java: acrescentar ao OpenAPI
.components(new Components().addSecuritySchemes("bearerAuth",
        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
```

Teste manual com `curl`:

```bash
curl -X POST localhost:8080/auth/clientes -H 'Content-Type: application/json' \
  -d '{"nome":"Ana","email":"ana@exemplo.com","senha":"umaSenhaForte123"}'

TOKEN=$(curl -s -X POST localhost:8080/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"ana@exemplo.com","senha":"umaSenhaForte123"}' | jq -r .data.accessToken)

curl -X POST localhost:8080/alertas -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"canalId":42,"pais":"Brasil","cidade":"Florianópolis","temperaturaMaxima":25}'

curl -N localhost:8080/alertas/stream?canalId=42 \
  -H "Authorization: Bearer $TOKEN" -H 'Accept: text/event-stream'
```

No navegador, use `fetch` em vez do `EventSource`, porque o `EventSource` não envia headers:

```javascript
import { fetchEventSource } from '@microsoft/fetch-event-source';

await fetchEventSource(`${API}/alertas/stream?canalId=42`, {
  headers: { Authorization: `Bearer ${token}` },
  onmessage(ev) {
    if (ev.event === 'alerta-disparado') mostrar(JSON.parse(ev.data));
  },
  onerror(err) { throw err; }, // não reconectar sozinho: o token pode ter expirado
});
```

## 8. Critérios de aceitação e plano de testes

A entrega está pronta quando todos os cenários abaixo passam em `./gradlew test`. Os testes de integração usam `@SpringBootTest` com `MockMvc` e `spring-security-test`; os de serviço seguem o estilo atual do projeto (JUnit 5 e Mockito).

### Cenários de aceitação

| # | Cenário (dado / quando / então) | Requisitos | Tipo |
| --- | --- | --- | --- |
| 1 | Dado e-mail novo, quando cadastrar, então 201 com `id`, `nome`, `email` e sem senha no corpo | CLI-01, CLI-06 | Integração |
| 2 | Dado o mesmo e-mail em caixa diferente, quando cadastrar de novo, então 409 | CLI-03, CLI-04 | Integração |
| 3 | Dado senha com 7 caracteres, e-mail inválido ou nome vazio, quando cadastrar, então 422 listando os campos | CLI-02, CLI-07 | Integração |
| 4 | Dado cliente salvo, quando consultar o banco, então `senha_hash` começa com `$2` e difere da senha | CLI-05 | Serviço |
| 5 | Dado cliente cadastrado, quando logar com a senha certa, então 200 com `accessToken`, `tokenType=Bearer` e `expiresIn=1800` | LOG-01 | Integração |
| 6 | Dado e-mail inexistente ou senha errada, quando logar, então 401 com o mesmo corpo nos dois casos | LOG-02, ERR-03 | Integração |
| 7 | Dado token gerado, quando decodificar, então tem `sub`, `cid`, `iss`, `iat`, `exp` e não tem senha | JWT-04 a JWT-06 | Unitário |
| 8 | Dado `JWT_SECRET` com 31 bytes, quando subir o contexto, então falha com mensagem clara | JWT-03 | Unitário |
| 9 | Dado token adulterado, de outro segredo, de outro emissor ou expirado, quando validar, então `TokenInvalidoException` (expirado marcado só no último) | JWT-07 | Unitário |
| 10 | Quando chamar `GET /previsao/BR`, `GET /ping`, `/docs` e `/auth/login` sem token, então não é 401 | SEC-01 | Integração |
| 11 | Quando chamar `/alertas` (GET, POST) e `/alertas/stream` sem token, então 401 com `WWW-Authenticate: Bearer` e corpo no envelope | SEC-02, SEC-05, ERR-01, ERR-04 | Integração |
| 12 | Dado header `Basic ...`, token malformado ou token na URL, quando chamar rota protegida, então 401 | SEC-06, SEC-07 | Integração |
| 13 | Dado token expirado, quando chamar rota protegida, então 401 com título "Token expirado" | SEC-06 | Integração |
| 14 | Quando chamar uma rota qualquer fora da lista pública, então exige token | SEC-03 | Integração |
| 15 | Quando autenticar, então a resposta não traz `Set-Cookie: JSESSIONID` | SEC-08 | Integração |
| 16 | Dado cliente A com `canalId` 42, quando A cadastrar alerta, então o dono é A mesmo que o corpo traga outro dono | ALR-01, ALR-02 | Integração |
| 17 | Dado alertas de A e de B, quando A listar, então vê só os de A | ALR-03 | Integração |
| 18 | Dado A e B com alertas no canal 42, quando ambos abrem stream, então os dois são aceitos | ALR-04, ALR-05 | Integração |
| 19 | Dado A e B conectados ao canal 42 e alerta de A disparado, quando enviar, então só A recebe `alerta-disparado` | ALR-06, SSE-04 | Serviço |
| 20 | Dado só B conectado e alerta de A disparado, quando enviar, então B não recebe nada | ALR-07 | Serviço |
| 21 | Dado A sem alerta no canal 7, quando A abrir `/alertas/stream?canalId=7`, então 403 em JSON e stream não abre; mesma resposta se o canal 7 for de B | SSE-02 | Integração |
| 22 | Quando abrir stream sem `canalId`, com texto ou com valor 0, então 400, 400 e 422 | SSE-03 | Integração |
| 23 | Quando o stream abrir, então o primeiro evento é `conectado` com o `canalId` | SSE-01, SSE-05 | Integração |
| 24 | Dado stream aberto, quando o heartbeat rodar, então chega `ping` | SSE-06 | Serviço |
| 25 | Dado duas conexões de A no canal 42, quando um alerta disparar, então as duas recebem | SSE-07 | Serviço |
| 26 | Dado token que expira em 1 s, quando abrir o stream, então a conexão termina após o vencimento | SSE-08 | Serviço |
| 27 | Dado stream que completa, estoura timeout ou dá erro, quando terminar, então sai do mapa e a chave some se vazia | SSE-09 | Serviço |
| 28 | Dado limite 2, quando A abrir a terceira conexão, então 429 | SSE-10 | Serviço |
| 29 | Dado perfil sem `dev`, quando acessar `/h2-console`, então não responde | OPS-02 | Integração |
| 30 | Dado cadastro, login ok, login falho e acesso negado, quando consultar os logs, então nenhum contém senha, hash ou token | OPS-01, CLI-06 | Integração |

Os requisitos LOG-03, LOG-04, LOG-05 (opcional), SEC-09, SEC-10, JWT-01, JWT-02, JWT-08, ERR-02, ERR-05, OPS-03, OPS-04, OPS-05 e OPS-06 (opcional) se verificam por revisão de código e pelos cenários acima.

### Testes existentes que precisam mudar

| Arquivo | Ajuste |
| --- | --- |
| `AlertaServiceTest` | `cadastrar` e `listarAtivos` recebem `clienteId` |
| `AlertaStreamServiceTest` | `registrar` e `enviar` recebem `clienteId`; construtor recebe o limite de conexões |
| `AlertaMonitorServiceTest` | `stream.enviar` agora recebe `clienteId` e `canalId` |
| `LllApplicationTests` | Definir `JWT_SECRET` no ambiente de teste (ou `@TestPropertySource`) |

## 9. Riscos e pontos em aberto

O risco técnico mais provável é o despacho assíncrono do `SseEmitter` com o Spring Security; os demais são decisões de produto que ficaram fora do escopo pedido.

### Riscos técnicos

| Risco | Efeito | O que fazer |
| --- | --- | --- |
| Despacho `ASYNC` reavaliado pelo Spring Security ao fechar o `SseEmitter` | Erro "response already committed" ao encerrar o stream | O `SecurityConfig` já libera `ASYNC` e `ERROR`; confirmar com os cenários 23 e 26 |
| `jjwt-jackson` usa Jackson 2 e o Spring Boot 4 usa Jackson 3 | Duas versões do Jackson no classpath | Se der conflito, trocar por `jjwt-gson` |
| Nome do starter de teste de segurança no Boot 4 | Falha de resolução no Gradle | Conferir no build (seção 7.1) |
| Estado das conexões SSE fica na memória | Com mais de uma instância, o disparo não alcança o cliente conectado em outra | Manter uma instância ou mover a distribuição para um broker (Redis, por exemplo) |
| H2 com `create-drop` | Clientes e alertas somem a cada reinício | Para uso real, trocar por H2 em arquivo ou PostgreSQL |
| Token sem revogação | Um token vazado vale até expirar (30 min) | Aceitável com expiração curta; guardar `jti` e lista de bloqueio só se surgir necessidade |
| Segredo único HS256 | Trocar o segredo invalida todos os tokens | Documentar o procedimento de troca |

### Perguntas em aberto

1. **Disparo repetido.** Hoje o monitor reenvia o alerta a cada 15 s enquanto a condição for verdadeira. Decisão: fica como está, um disparo a cada 15 s enquanto a condição valer; um limite (no máximo um disparo por alerta a cada 30 min) fica fora desta entrega. Mudar isso alteraria o `AlertaMonitorService`, não a autorização.
2. **Renovação de token.** O stream cai quando o token expira (SSE-08) e o cliente precisa logar de novo. Quer um refresh token? Isso adiciona `POST /auth/refresh` e uma tabela de tokens.
3. **Gerenciar alertas.** Ainda não existe rota para desativar ou apagar um alerta, então um canal, uma vez habilitado, não deixa de existir. Quer `DELETE /alertas/{id}` restrito ao dono (e 404 para alerta de outro)?
4. **Limite mínimo.** O README diz que o alerta exige pelo menos um limite, mas o código aceita um alerta sem nenhum. Quer corrigir junto?
5. **Força bruta no login.** LOG-05 está como opcional. Quer incluir na primeira entrega?

### Ordem sugerida de implementação

1. Dependências, propriedades e `JwtService` com testes unitários (seção 7.1, 7.2).
2. `Cliente`, cadastro e login (7.6).
3. Filtro, handlers 401/403 e `SecurityConfig` (7.3 a 7.5); liberar tudo o que hoje funciona e proteger só `/alertas`.
4. Dono do alerta e `canalHabilitado` (7.7).
5. `AlertaStreamService` por (cliente, canal), `ping` e encerramento por expiração (7.8, 7.9).
6. Handlers de erro e OpenAPI (7.10, 7.11).
7. Ajuste dos testes existentes e dos 30 cenários (seção 8).
