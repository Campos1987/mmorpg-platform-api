# MMORPG Platform API

Uma API robusta e escalável para gerenciamento de contas e autenticação em uma plataforma de MMORPG, construída com Java 17+ e Spring Boot 3.

## 🚀 Tecnologias

- **Java 21**
- **Spring Boot 3.4.1**
- **Spring Data JPA**
- **Spring Security** (Argon2id Hashing)
- **MySQL**
- **Lombok**
- **Jakarta Validation**
- **Maven**

## 🏗️ Arquitetura

O projeto segue os princípios de **Clean Architecture** e **Domain-Driven Design (DDD)**, com forte foco em:
- **Imutabilidade**: Uso extensivo de `record` e Value Objects.
- **Segurança**: Criptografia de ponta (Argon2id) e proteção contra exposição de dados (Data Masking).
- **Testabilidade**: Design focado em Injeção de Dependência via construtor.

Para mais detalhes, veja a [Documentação de Arquitetura](documentation/architecture.md).

## 📂 Estrutura de Documentação

- [Arquitetura](documentation/architecture.md): Detalhes sobre camadas e padrões.
- [Segurança](documentation/security.md): Políticas de autenticação e proteção de dados.
- [Endpoints da API](documentation/api_endpoints.md): Guia de integração.

## 🛠️ Como Executar

### Pré-requisitos
- Java 21+
- Maven 3.8+
- MySQL 8+

### Configuração do Banco de Dados
Crie um banco de dados chamado `db_login` no MySQL e configure as credenciais no arquivo `src/main/resources/application.yml` ou via variáveis de ambiente:
- `SPRING_DATASOURCE_LOGIN_URL`
- `SPRING_DATASOURCE_LOGIN_USERNAME`
- `SPRING_DATASOURCE_LOGIN_PASSWORD`

### Rodando a aplicação
```bash
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

## 🧪 Testes

Execute os testes unitários e de integração com:
```bash
mvn test
```

## 📄 Licença

Este projeto é de uso privado. Todos os direitos reservados.
