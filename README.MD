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

Implementações futuras:
- Comunicação com CLPs
  - Siemens S7
  - Ethernet/IP

## Autores

[Davih de Andrade Machado Borges Santos](https://github.com/DavihDeAndrade)

[Pedro Quadro de Freitas](https://github.com/PQFreitas)

[Gabriel Trindade Santana](https://github.com/GabrielTrindade31)

Davi Muniz da Silva

José Santos Damasceno

## Responsáveis
[Davih de Andrade Machado Borges Santos](https://github.com/DavihDeAndrade)

José Marcelo de Assis Santos

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

## Uso

### Para acessar a documentação da API, acesse o Swagger a partir da URL:

```
http://localhost:8080/swagger-ui/index.html#/
```

Toda a documentação de uso, funcionalidades, tipagem, validação e etc estará no Swagger.

Para encontrar as classes e métodos responsáveis por cada funcionalidade, utilize os controllers da API para encontrar o que precisa.

Em caso de dúvidas, bugs ou similares, abra um Issue ou entre em contato com [Davih de Andrade](https://github.com/DavihDeAndrade).
