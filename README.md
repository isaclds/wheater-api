# Weather Api

API de previsão do tempo que abstrai APIs externas de dados climáticos, oferecendo cache e busca por localização (país, estado ou cidade).

## INFOS PARA DESENVOLVIMENTO

Service da Nasa deve ser utilizado para informações passadas enquanto o Forecast para opreações futuras

## Objetivo

Fornecer uma API com 4 endpoints de previsão do tempo, escondendo a complexidade de lidar com múltiplos provedores, cache e formatos de dados diferentes.

## Stack

- Java 25
- Spring Boot

## Funcionalidades planejadas

### Endpoints

- Previsão do tempo por país (GET /previsao/:pais)
- Previsão do tempo por estado (GET /previsao/:pais/:estado)
- Previsão do tempo por cidade (GET /previsao/:pais/:estado/:cidade)
- Quando a busca for mais ampla (país ou estado), listar os climas predominantes das localidades relacionadas
- Health check (GET /ping)

### Cache

- Cache de respostas por localização
- TTL(Time to live) configurável
- Fallback para cache(mesmo que expirado) em caso de falha da API provedora

### Geral

- Agregação dos dados de diferentes provedores em uma previsão combinada

## Integrantes

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/Bernardo-Lisboa">
        <img src="https://github.com/Bernardo-Lisboa.png?size=100" width="100" style="border-radius:50%"/><br/>
        <sub><b>Bernardo Amaral Lisboa</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/Daniellrc">
        <img src="https://github.com/Daniellrc.png?size=100" width="100" style="border-radius:50%"/><br/>
        <sub><b>Daniel Luiz da Rocha Cordeiro</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/isaclds">
        <img src="https://github.com/isaclds.png?size=100" width="100" style="border-radius:50%"/><br/>
        <sub><b>Isac Lehmkuhl dos Santos</b></sub>
      </a>
    </td>
  </tr>
</table>
