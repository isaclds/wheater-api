# Weather Api

API de previsão do tempo que abstrai APIs externas de dados climáticos, oferecendo cache e busca por localização (país, estado ou cidade).

## INFOS PARA DESENVOLVIMENTO

Service da Nasa deve ser utilizado para informações passadas enquanto o Forecast para opreações futuras

## Objetivo

Fornecer uma API com 4 endpoints de previsão do tempo, escondendo a complexidade de lidar com múltiplos provedores, cache e formatos de dados diferentes.

## Problema a ser resolvido

- Fontes diferentes de dados trazem diferentes informações que podem confundir o consumidor, o nosso objetivo é centralizar mais as informações em uma única fonte.

## Público alvo

- Cidadãos que desejam consultar a previsão tempo em uma única fonte de dados para maior precisão, e empresas que se beneficiem com maior exatidão da previsão do tempo.

## Requisitos

### Funcionais - Casos de uso

- Consultar previsão por país: Como usuário, quero consultar a previsão do tempo informando apenas o país, para obter uma visão geral dos climas predominantes das localidades relacionadas. (GET /previsao/:pais)
- Consultar previsão por estado: Como usuário, quero consultar a previsão do tempo informando o país e o estado, para obter uma visão geral dos climas predominantes das localidades relacionadas. (GET /previsao/:pais/:estado)
- Consultar previsão por estado: Como usuário, quero consultar a previsão do tempo informando o país, o estado e cidade, para obter uma visão geral dos climas predominantes das localidades relacionadas. (GET /previsao/:pais/:estado/:cidade)
- Verificar disponibilidade da API: Como consumidor da API (desenvolvedor/sistema integrado), quero verificar se o serviço está no ar, para monitorar a saúde da aplicação. (GET /ping)

### Não-Funcionais

- Confiabilidade: O sistema deve manter disponibilidade mesmo diante de falha de provedores externos, utilizando fallback de cache (mesmo expirado).
- Consistência de dados: As respostas da API devem seguir um formato padronizado (JSON), independentemente do provedor de origem dos dados.
- Configurabilidade: O TTL do cache deve ser configurável sem necessidade de alteração de código.
- Portabilidade: A aplicação deve rodar em ambiente containerizado, compatível com Java 25 e Spring Boot.

## Modelagem

- Será apenas uma API então não terá uma interface web.

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
