---
name: executar-testes
description: >-
  Use this skill when the user asks to run unit or integration tests, diagnose test failures,
  or verify application integrity and build status for the Weather API.
---

# Execução e Validação de Testes (Weather API)

Instruções para executar os testes da suíte Spring Boot / Gradle da Weather API.

## 1. Comandos Básicos

```bash
# Executar toda a suíte de testes
./gradlew test

# Executar com saída detalhada em caso de falha
./gradlew test --info

# Forçar reexecução de todos os testes ignorando cache
./gradlew test --rerun-tasks
```

## 2. Execução Direcionada por Pacote ou Classe

```bash
# Executar apenas testes de serviços
./gradlew test --tests "ifsc.edu.lll.service.*"

# Executar apenas testes de mappers
./gradlew test --tests "ifsc.edu.lll.service.mapper.*"

# Executar uma classe específica
./gradlew test --tests "ifsc.edu.lll.service.PrevisaoServiceTest"
./gradlew test --tests "ifsc.edu.lll.service.AlertaMonitorServiceTest"
```

## 3. Diagnóstico de Problemas Comuns

- **Falha de inicialização de contexto (`ApplicationContext`)**:
  - Verifique se dependências injetadas no construtor estão anotadas como `@Service`, `@Component` ou interfaces `@FeignClient`.
  - Verifique se propriedades obrigatórias (como `jwt.secret` caso o Spring Security esteja ativo) estão presentes em `@TestPropertySource` ou `application.properties`.
- **Falha em testes com Banco H2**:
  - O H2 utiliza modo em memória `create-drop`. A cada execução da suíte, tabelas são recriadas. Se houver inserções manuais no setup dos testes, garanta limpeza ou isolamento transacional com `@Transactional`.
- **Relatórios de Teste**:
  - Em caso de falhas, consulte o relatório HTML gerado pelo Gradle em:
    `build/reports/tests/test/index.html`.
