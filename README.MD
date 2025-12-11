## Sobre
Este repositório contém o Back-end do sistema COFFE para gestão de ativos.

O coffe-backend é uma API em JAVA com Maven que realiza o controle e monitoramento de ativos e controladores, bem como interage com o coffe-backend-python e o coffe-frontend

O coffe-backend permite as seguintes funcionalidades:
- API RESTful e Endpoints para todas as funcionalidades com SpringBoot
- Integração com banco de dados via ORM com Hibernate
- Banco de dados relacional com PostgreSQL
- Gerenciamento de ativos
- Comunicação com CLPs
  - Modbus/TCP
  - MQTT
  - OPC-UA

## Autores

[Davih de Andrade Machado Borges Santos](https://github.com/DavihDeAndrade)

[Pedro Quadro de Freitas](https://github.com/PQFreitas)

[Gabriel Trindade Santana](https://github.com/GabrielTrindade31)

Davi Muniz da Silva

José Santos Damasceno

## Responsável
[Davih de Andrade Machado Borges Santos](https://github.com/DavihDeAndrade)

## Instalação

Para instalação do PostgreSQL:
- Baixar e instalar o PostgreSQL 16.x no site oficial
    - Qualquer subversão funciona, mas é recomendado a mais recente

### Após a instalação do banco, em qualquer sistema operacional:
### Recomendado o uso de Intelij como IDE e JAVA 21

Para a instalação limpa via Maven:
```
mvn clean install
```

## Execução

```
spring-boot:run
```


