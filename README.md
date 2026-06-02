# Grankain Platform API

API REST stateless de autenticação e gerenciamento de contas para plataforma MMORPG (Lineage 2), construída com Java 21 e Spring Boot 3.

> **Porta padrão:** `4000` &nbsp;|&nbsp; **Perfis:** `dev`, `prod`

---

## 🚀 Stack Tecnológica

| Tecnologia | Versão | Função |
|---|---|---|
| Java (Eclipse Temurin) | 21 (LTS) | Runtime |
| Spring Boot | 3.5.14 | Framework principal |
| Spring Security + OAuth2 Resource Server | (via Boot) | Autenticação JWT HS256 |
| Spring Data JPA + Hibernate | (via Boot) | Persistência |
| MySQL | 8.0+ | Banco de dados (externo) |
| Bouncy Castle | 1.84 | Provider Argon2id |
| Jakarta Validation | (via Boot) | Validação de entrada |
| Lombok | (via Boot) | Redução de boilerplate |
| Docker + Docker Compose | — | Containerização multi-stage |

---

## 🏗️ Arquitetura

O projeto segue **Domain-Driven Design (DDD)** e **Clean Architecture**:

```
com.grankain.platformapi
├── auth/           → Domínio: entidades, VOs, repositórios, serviços, DTOs
├── user/               → Bounded context: conta da plataforma web, perfil e configurações
├── gamer/              → Bounded context: contas in-game do Lineage 2
├── config/         → Configuração explícita do DataSource (MySQL)
├── infra/          → GlobalExceptionHandler, validações customizadas, DataMasker
├── security/       → SecurityConfig, PasswordEncoderConfig (Argon2id), TokenGenerator
└── util/           → IpUtil (extração de IP real via headers de proxy)
```

Padrões aplicados: Constructor Injection, Rich Domain Model, Value Objects (`@Embeddable` Records), DTO Pattern, Repository Pattern, `@ControllerAdvice` centralizado.

---

## 🔑 Endpoints Principais

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `POST` | `/auth/register` | Pública | Registra nova conta |
| `POST` | `/auth/login` | Pública | Autentica e retorna JWT (1h) |
| `GET` | `/actuator/health` | Pública | Health check |
| `GET` | `/posts/**` | Pública | Leitura de posts/eventos |
| Qualquer | Demais rotas | `Bearer <token>` | Rotas protegidas |

---

## 🛠️ Como Executar

### Pré-requisitos

- Docker Engine 24+ e Docker Compose v2
- Containers MySQL (`l2-game`, `l2-login`, `l2-web`) rodando e associados à rede Docker `mmorpg-net`
  - *Nota:* Se os containers do banco de dados foram iniciados por outros projetos Compose, eles podem estar em redes isoladas (ex: `game_mmorpg-net`). Você **deve** conectá-los à rede compartilhada executando:
    ```bash
    docker network connect mmorpg-net l2-game
    docker network connect mmorpg-net l2-login
    docker network connect mmorpg-net l2-web
    ```
- *(Opcional)* Java 21+ e Maven para execução local sem Docker

### 1. Configurar variáveis de ambiente

```bash
cp .env.example .env
# Edite o .env com suas credenciais reais
```

**Variáveis obrigatórias no `.env`:**

```dotenv
SPRING_PROFILES_ACTIVE=dev

# Banco de dados (container mysql-l2_game na rede mmorpg-net)
MYSQL_DATABASE=gk_web_user
MYSQL_USER=gk_web_user
MYSQL_PASSWORD=SUA_SENHA

# CORS (URL do frontend)
CORS_ORIGINS=http://localhost:3000
```

> ⚠️ O `.env` está no `.gitignore` e **nunca deve ser commitado**.
> A variável `JWT_SECRET_KEY` deve ser injetada via ambiente — nunca commite chaves secretas.

### 2. Subir com Docker Compose

```bash
# Build e inicialização
docker compose up -d --build

# Verificar logs
docker compose logs -f api

# Health check
curl http://localhost:4000/actuator/health
# { "status": "UP" }

# Parar
docker compose down
```

### 3. Execução local (sem Docker)

```bash
export LOGIN_DB_URL=jdbc:mysql://localhost:3306/gk_web_user?useSSL=false&serverTimezone=UTC
export LOGIN_DB_USER=gk_web_user
export LOGIN_DB_PASS=SUA_SENHA
export SPRING_APPLICATION_CORS_ORIGINS=http://localhost:3000
export JWT_SECRET_KEY=SUA_CHAVE_SECRETA

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 🧪 Testes

```bash
# Todos os testes
./mvnw test

# Classe específica
./mvnw test -Dtest=EncodedPasswordTest
```

---

## 🔒 Segurança

- **Hash de senhas:** Argon2id (60 MB memória, 10 iterações, salt 16 bytes)
- **JWT:** HS256, expiração de 1 hora, injetado via `JWT_SECRET_KEY`
- **Brute-force:** bloqueio de conta após 5 falhas; bloqueio por IP após 7 falhas
- **Headers HTTP:** `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, HSTS em `prod`
- **Container:** execução com usuário não-root (`appuser`)
- **Erros:** stack trace e detalhes omitidos em `prod` (hardening)

---

## 📂 Documentação

| Arquivo | Conteúdo |
|---|---|
| [`documentation/technical-overview.md`](documentation/technical-overview.md) | Documentação técnica completa (arquitetura, modelagem, segurança, getting started) |
| [`documentation/api-integration-guide-frontend.md`](documentation/api-integration-guide-frontend.md) | Guia de integração para o time de Frontend (Next.js/TypeScript) com interfaces TS |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Visão de camadas e padrões de arquitetura do projeto |
| [`documentation/security.md`](documentation/security.md) | Políticas de autenticação e proteção de dados |

---

## 📄 Licença

Este projeto é de uso privado. Todos os direitos reservados.

