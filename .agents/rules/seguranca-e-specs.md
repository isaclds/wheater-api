# Regras de Segurança e Conformidade com Specs (Weather API)

As regras abaixo garantem a conformidade com a especificação em `specs/implementacao_autorizacao.md` e os princípios de segurança da Weather API:

## 1. Segurança e Gerenciamento de Credenciais
- **Segredo JWT (`JWT_SECRET`)**:
  - O segredo de assinatura **nunca** deve ser hardcoded no código ou no arquivo `application.properties`.
  - Deve vir exclusivamente de variável de ambiente (`${JWT_SECRET}`).
  - Deve possuir no mínimo 32 bytes (256 bits).
  - Em testes automatizados, configure via `@TestPropertySource(properties = "jwt.secret=segredo-de-teste-com-mais-de-32-bytes-aqui")`.
- **Senhas e Hashes**:
  - Senhas devem ser criptografadas usando `BCryptPasswordEncoder` com custo padrão (10).
  - Senhas em texto plano, hashes ou tokens JWT **nunca** devem aparecer em respostas HTTP, logs (`log.info/debug`), stack traces ou mensagens de erro.
  - O e-mail do cliente deve ser sempre armazenado normalizado (minúsculas e sem espaços nas pontas).

## 2. Statelessness e Sessões
- A aplicação é estritamente **STATELESS**.
- CSRF deve estar desabilitado (`csrf.disable()`) e `SessionCreationPolicy.STATELESS` deve ser aplicado.
- Não deve ser emitido nem aceito cookie `JSESSIONID`.
- O token JWT deve ser transmitido unicamente pelo header `Authorization: Bearer <token>`. É proibido aceitar tokens via query params ou cookies.

## 3. Isolamento Multi-Inquilino (Multi-Tenant por Cliente)
- Um cliente nunca deve ter acesso a alertas ou canais SSE de outro cliente.
- A chave de conexões SSE deve ser composta: `(clienteId, canalId)`.
- Listagens (`GET /alertas`) devem filtrar apenas alertas cujo `clienteId` coincida com o do token JWT autenticado (`@AuthenticationPrincipal`).
- O `clienteId` de um alerta deve sempre ser extraído do token autenticado, e nunca confiado a partir do payload de requisição do usuário.
