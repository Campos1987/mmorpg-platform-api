# Arquitetura do Projeto

Este projeto utiliza uma abordagem baseada em **Domain-Driven Design (DDD)** e **Clean Architecture**, focada em separação de preocupações e testabilidade.

## Camadas

### 1. Domain (Domínio)
Localizado em `com.grankain.platformapi.auth.entity` e `valueObjects`.
- **Entities**: Representam os objetos de negócio persistentes (ex: `Account`).
- **Value Objects**: Objetos imutáveis que encapsulam lógica de validação e comportamento (ex: `Email`, `Password`, `Username`). Eles garantem que o estado do sistema seja sempre consistente.
- **Enums**: Definições de estados fixos (ex: `AccountStatus`).

### 2. Application/Service (Serviço)
Localizado em `com.grankain.platformapi.auth.service`.
- Orquestra as operações de negócio.
- Interage com os Repositórios e componentes de infraestrutura (como `PasswordEncoder`).
- Não expõe detalhes de implementação HTTP.

### 3. API/Controller (Controlador)
Localizado em `com.grankain.platformapi.auth.controller`.
- Ponto de entrada para requisições REST.
- Responsável pela validação de entrada via DTOs (`jakarta.validation`).
- Converte os resultados do serviço em respostas HTTP adequadas.

### 4. Infrastructure (Infraestrutura)
Localizado em `com.grankain.platformapi.infra`.
- **Exceptions**: Tratamento global de erros (`GlobalExceptionHandler`) e padronização de respostas de erro (RFC 7807/Problem Detail).
- **Validation**: Anotações customizadas para validação de dados.
- **Utils**: Utilitários transversais como mascaramento de dados sensíveis.

### 5. Configuration (Configuração)
Localizado em `com.grankain.platformapi.config` e `security`.
- Definição de Beans do Spring.
- Configurações de banco de dados (MySQL) e segurança (Spring Security).

## Padrões Adotados

- **Constructor Injection**: Todas as dependências são injetadas via construtor para facilitar testes unitários e garantir imutabilidade.
- **Data Transfer Objects (DTOs)**: Uso de `record` para transporte de dados, evitando a exposição de Entidades JPA na camada de API.
- **Rich Domain Model**: Lógica de domínio (como formatação de nomes e datas) reside nas entidades, evitando "Serviços Anêmicos".
- **Data Masking**: Proteção de dados PII (Personally Identifiable Information) em respostas da API.
