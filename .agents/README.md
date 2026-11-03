# .agents - Contexto de Agentes e Automações da Weather API

Este diretório contém as configurações, regras contextuais e habilidades (*skills*) disponíveis para os agentes de Inteligência Artificial (como o Antigravity e o Gemini CLI) que trabalham neste repositório.

## Estrutura de Diretórios

```text
.agents/
├── README.md                          # Este arquivo de visão geral
├── rules/                             # Regras contextuais aplicadas automaticamente
│   ├── padroes-projeto.md             # Regras de arquitetura, Java 25 e boas práticas
│   └── seguranca-e-specs.md           # Regras para implementação da spec EARS de segurança
└── skills/                            # Habilidades especializadas ativadas sob demanda
    ├── implementar-autorizacao/       # Runbook e procedimentos para implementar auth (JWT/SSE)
    │   └── SKILL.md
    ├── executar-testes/               # Guia de execução e depuração de testes Gradle/JUnit 5
    │   └── SKILL.md
    └── adicionar-provedor-clima/      # Workflow para integrar novos provedores Feign
        └── SKILL.md
```

## Resumo das Skills Disponíveis

| Skill | Gatilho / Finalidade |
|---|---|
| **`implementar-autorizacao`** | Ativar quando o usuário solicitar implementar autenticação, segurança JWT, proteção de alertas ou SSE da spec `specs/implementacao_autorizacao.md`. |
| **`executar-testes`** | Ativar para rodar bateria de testes unitários ou de integração, validar builds e depurar falhas de testes no Gradle com Java 25. |
| **`adicionar-provedor-clima`** | Ativar para integrar um novo provedor meteorológico externo via OpenFeign, criando DTOs, Mappers e adaptando a camada de serviço. |

## Resumo das Regras (`rules/`)

- **`padroes-projeto.md`**: Força injeção de dependência por construtor, imutabilidade em DTOs via `record`, empacotamento com `ApiResponse`, e proíbe anti-padrões como `@Autowired` em fields ou expor entidades JPA.
- **`seguranca-e-specs.md`**: Regras estritas para conformidade com a especificação EARS de autorização, manipulação segura de `JWT_SECRET`, criptografia de senhas com BCrypt e isolamento de SSE por cliente.
