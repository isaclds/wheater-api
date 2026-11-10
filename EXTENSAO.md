# Criação de Logs

## Arquivos criados e alterados

Para essas alterações foram criados os seguintes arquivos:

[LogController.java](src/main/java/ifsc/edu/lll/controller/LogController.java)

[LogRequestDTO.java](src/main/java/ifsc/edu/lll/dto/log/LogRequestDTO.java)

[Log.java](src/main/java/ifsc/edu/lll/model/Log.java)

[LogService.java](src/main/java/ifsc/edu/lll/service/LogService.java)

[LogRepository.java](src/main/java/ifsc/edu/lll/repository/LogRepository.java)

Durante os aprimoramentos foram alterados também todos os controllers para adicionar o log e também [SecurityConfig.java](src/main/java/ifsc/edu/lll/security/SecurityConfig.java) para que fosse possível acessar os endpoints de log sem estar autenticado, para facilitar os testes.

## Alterações realizadas

### Controllers

Todos os controllers agora chamam o método `criarLog` do `LogService` o mesmo cria o log e registra ele no banco, fazendo com que fique registrado quais endpoints foram acessados da API, status de resposta e momento em que o mesmo foi acesso.

### Logs

Foi criado o service de log para lidar com a lógica de negocio dos logs e centralizar a criação e busca, da mesma forma foi criado o model para declarar a classe log e sua criação no banco em memória e o repository para acessar os valores. 

Foi implementado o controller de `Log` que permite:

1. Listar todos os logs com 

```bash
curl -X 'GET' \
  'http://localhost:8080/logs' \
  -H 'accept: */*'
```

2. Listar todos os logs por status code
```bash
curl -X 'GET' \
  'http://localhost:8080/logs/200' \
  -H 'accept: */*'
```

3. Listar todos os logs por endpoint
```bash
   curl -X 'GET' \
   'http://localhost:8080/logs/%2Fauth%2Fclientes' \
   -H 'accept: */*'
```

4. Listar todos os logs por data
```bash
   curl -X 'GET' \
   'http://localhost:8080/logs/data' \
   -H 'accept: */*' \
   -H 'Content-Type: application/json' \
   -d '{
   "dataInicial": "2026-11-10T23:01:00.461Z",
   "dataFinal": "2026-11-10T23:10:27.461Z"
   }'
```

> Todos eles retornam o status, um titulo e uma lista com todos os logs que correspondem ao filtro

#### Dica

Recomendo testar via `http://localhost:8080/swagger-ui/index.html`, os retornos não estão bem documentados, entretanto facilita muito por ter uma interface que permite que você realize diversos requests com facildade, lembrando que antes de realizar os request é importante que você crie um cliente, realize login e dai sim comece a fazer as requisições após registrar o token. 

## Limitações conhecidas

1. Não foi possível implementar logs em caso de exceções, para isso deveria ser criado os logs no momento em que essas exceções são retornadas dentro de [GlobalExceptionHandler.java](src/main/java/ifsc/edu/lll/exception/GlobalExceptionHandler.java) entretanto por conta do tempo e também de que não sabia como puxar o endpoint que havia sido chamado no momento do request optei por focar em outros pontos
2. Não foi possível filtrar verdadeiramente por endpoints por conta de que os endpoints tem **/** separando eles, o que quebra a URL ao tentar filtrar, não sendo possível.