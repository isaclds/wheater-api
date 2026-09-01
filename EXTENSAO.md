# O que mudou?

## Controller

Foi adicionado o controller `AvaliacaoController` que se encarrega de cuidar de todas as requisições que vão para o endpoint `avaliacao`

## Service

Foi criado o service `AvaliacaoService` que valida se a nota é válida e chama o repositorio para guardar ela no banco

## Repository e Model

Foi criado o model e o repository para se encarregarem de salvar as avaliações em uma banco em memória, dessa forma a cada reinicio todas as avaliações são reiniciadas.

## Testes

Para testar é necessário rodar a aplicação e então é possível chamar o endpoint `avaliacao`, se for POST ele cria uma avaliação e se for `GET` ele lista todas as avaliacoes

### Criar avaliação

```curl
curl --location 'localhost:8080/avaliacao/' \
--header 'Content-Type: application/json' \
--data '{
    "nome": "Isac",
    "nota": 4,
    "comentario": "Aqui vai um comentario avaliativo"
}'
```

### Listar avaliações 

```curl
curl --location 'localhost:8080/avaliacao/'
```

### Tratativa de Erros

#### Nome em branco

```curl
curl --location 'localhost:8080/avaliacao/' \
--header 'Content-Type: application/json' \
--data '{
    "nome": "",
    "nota": 4,
    "comentario": "Aqui vai um comentario avaliativo"
}'
```

#### Nota maior que 5 ou menor que 1

```curl
curl --location 'localhost:8080/avaliacao/' \
--header 'Content-Type: application/json' \
--data '{
    "nome": "Isac",
    "nota": 8,
    "comentario": "Aqui vai um comentario avaliativo"
}'
```

#### Comentário maior que 1000 caracteres

```curl
curl --location 'localhost:8080/avaliacao/' \
--header 'Content-Type: application/json' \
--data '{
    "nome": "Isac",
    "nota": 4,
    "comentario": "Aqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativoAqui vai um comentario avaliativo"
}'
```
