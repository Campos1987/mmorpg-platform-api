# mmorpg-platform-api — Documentação Técnica

> **Projeto:** Grankain Platform API  
> **Versão documentada:** `0.0.1-SNAPSHOT`  
> **Última atualização:** 2026-05-23  
> **Porta padrão:** `4000`

---

## 1. Visão Geral do Projeto

### 1.1 Resumo Executivo

A **Grankain Platform API** é o backend de uma plataforma MMORPG (Lineage 2), responsável por toda a lógica de **autenticação e gerenciamento de contas de jogadores**. A API é construída como um serviço REST stateless, projetada para ser consumida por um frontend web (Next.js) e por eventuais clientes nativos.

O serviço gerencia o ciclo de vida completo de uma conta de jogador: desde o registro inicial com validação de dados, passando pela autenticação com emissão de tokens JWT, até os mecanismos de proteção contra ataques de força bruta a nível de conta e de endereço IP.

### 1.2 Objetivos Principais nesta Fase Inicial

| Objetivo | Status |
|---|---|
| Registro de novas contas com validação completa de dados | ✅ Implementado |
| Autenticação de usuários com emissão de JWT (HS256) | ✅ Implementado |
| Bloqueio progressivo de conta por falhas de senha | ✅ Implementado |
| Bloqueio progressivo por endereço IP de origem | ✅ Implementado |
| Tratamento centralizado e padronizado de erros (RFC-style) | ✅ Implementado |
| Segurança de transporte e headers HTTP defensivos | ✅ Implementado |
| Containerização com Docker multi-stage | ✅ Implementado |

---

## 2. Arquitetura do Sistema

### 2.1 Stack Tecnológica

| Camada | Tecnologia | Versão | Justificativa |
|---|---|---|---|
| **Runtime** | Java (Eclipse Temurin) | 21 (LTS) | Suporte a Virtual Threads, Records e Switch Expressions nativos |
| **Framework** | Spring Boot | 3.5.14 | Autoconfiguration madura com ecossistema de segurança robusto |
| **Segurança** | Spring Security + OAuth2 Resource Server | (via Boot) | Integração nativa com JWT sem dependências externas extras |
| **Criptografia** | Spring Security Crypto + Bouncy Castle | 1.84 | Argon2id via Bouncy Castle como provider para `PasswordEncoder` |
| **Persistência** | Spring Data JPA + Hibernate | (via Boot) | Repositórios declarativos com suporte a Value Objects embarcados |
| **Banco de Dados** | MySQL | (externo) | Banco existente da infraestrutura do jogo; conectado via rede Docker |
| **Validação** | Jakarta Validation (Bean Validation 3) | (via Boot) | Validações declarativas em DTOs com suporte a constraints customizadas |
| **Build** | Maven Wrapper (`mvnw`) | (via Boot parent) | Garante reprodutibilidade do build sem instalação local do Maven |
| **Infraestrutura** | Docker + Docker Compose | — | Containerização com build multi-stage e execução com usuário não-root |

### 2.2 Organização de Pacotes (Clean Architecture / DDD)

A estrutura de pacotes segue os princípios de **Domain-Driven Design (DDD)** com separação clara de responsabilidades:

```
com.grankain.platformapi
│
├── auth/                          ← Domínio de autenticação (bounded context)
│   ├── controller/                ← Camada de Apresentação (Entry Points HTTP)
│   │   └── AuthController.java
│   ├── domain/                    ← Modelo de Domínio (Entidades, Enums, Value Objects)
│   │   ├── Account.java           ← Aggregate Root principal
│   │   ├── AccountStatus.java     ← Enum de estado da conta
│   │   ├── BlockIpUser.java       ← Entidade de controle de bloqueio por IP
│   │   ├── UserAccess.java        ← Enum de nível de acesso (Role)
│   │   ├── login/
│   │   │   └── AccessCounterFailure.java  ← Componente de domínio para contagem de falhas
│   │   └── vo/                    ← Value Objects (imutáveis via Java Records)
│   │       ├── Email.java
│   │       ├── Password.java
│   │       └── Username.java
│   ├── dto/                       ← Data Transfer Objects (fronteira da API)
│   │   ├── request/
│   │   │   ├── RequestLogin.java
│   │   │   └── RequestRegister.java
│   │   └── response/
│   │       ├── ResponseLogin.java
│   │       └── ResponseRegister.java
│   ├── exceptions/
│   │   └── AccountAlreadyExistsException.java
│   ├── repository/                ← Contratos de persistência (Spring Data JPA)
│   │   ├── AccountRepository.java
│   │   └── BlockIpUserRepository.java
│   └── service/                   ← Camada de Aplicação (orquestração de casos de uso)
│       ├── LoginService.java
│       └── RegisterService.java
│
├── config/
│   └── DatabaseConfig.java        ← Configuração explícita do DataSource MySQL
│
├── infra/                         ← Infraestrutura transversal
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java  ← @ControllerAdvice centralizado
│   │   └── dto/
│   │       ├── ApiErrorException.java   ← Estrutura de resposta de erro
│   │       └── ApiTraceItem.java        ← Item do stack trace filtrado
│   ├── util/
│   │   └── DataMasker.java        ← Utilitário para mascaramento de dados sensíveis
│   └── validation/
│       ├── ValidPassword.java     ← Constraint customizada de complexidade de senha
│       └── ValidUser.java         ← Constraint customizada de formato de username
│
├── security/
│   ├── PasswordEncoderConfig.java ← Bean Argon2id explicitamente configurado
│   ├── SecurityConfig.java        ← SecurityFilterChain: CORS, JWT, autorização
│   └── TokenGenerator.java        ← Geração de JWT com claims de negócio
│
└── util/
    └── IpUtil.java                ← Extração de IP real considerando headers de proxy
```

### 2.3 Padrões de Design Adotados

| Padrão | Onde é Aplicado |
|---|---|
| **Constructor Injection** | Todos os `@Service`, `@Controller` e `@Configuration` evitam `@Autowired` em campo |
| **Rich Domain Model** | `Account` centraliza a lógica de formatação de nome e data de nascimento |
| **Value Objects (VO)** | `Email`, `Username` e `Password` são Java Records `@Embeddable` com validação no construtor compacto |
| **DTO Pattern** | Entidades JPA nunca são expostas nos controllers; `RequestRegister`, `ResponseLogin` etc. formam a fronteira pública |
| **Repository Pattern** | `AccountRepository` e `BlockIpUserRepository` são interfaces Spring Data JPA |
| **@ControllerAdvice** | `GlobalExceptionHandler` centraliza todo o tratamento de erro, com comportamento diferenciado por profile (`dev` vs `prod`) |
| **Explicit `@Bean`** | `PasswordEncoderConfig`, `SecurityConfig` e `DatabaseConfig` declaram beans explicitamente, sem "Spring magic" implícito |
| **Multi-stage Docker Build** | Estágio `build` usa JDK Alpine completo; estágio `runtime` usa apenas JRE Alpine, minimizando a superfície de ataque da imagem final |

---

## 3. Modelagem de Dados e Entidades

### 3.1 Entidade: `accounts`

Mapeada pela classe `Account.java`. É o **Aggregate Root** do domínio de autenticação.

| Coluna | Tipo Java | Tipo DB | Observação |
|---|---|---|---|
| `id` | `UUID` | `BINARY(16)` / `CHAR(36)` | PK gerada via `GenerationType.UUID` |
| `name` | `String` | `VARCHAR` | Nome completo capitalizado automaticamente |
| `email` | `String` (via VO `Email`) | `VARCHAR(100)` `UNIQUE` | `@Embedded` com `@AttributeOverride` |
| `birthday` | `LocalDate` | `DATE` | Parseado via `LocalDate.parse()` |
| `username` | `String` (via VO `Username`) | `VARCHAR` | `@Embedded` com `@AttributeOverride` |
| `status` | `AccountStatus` (Enum) | `ENUM/VARCHAR` | Salvo como String: `PENDING`, `ACTIVE`, `SUSPENDED`, `BANNED` |
| `password` | `String` | `TEXT/VARCHAR` | Hash Argon2id; nunca texto plano |
| `created_at` | `Instant` | `TIMESTAMP` | `@CreationTimestamp` — preenchido automaticamente no INSERT |
| `accessed_at` | `Instant` | `TIMESTAMP` | `@UpdateTimestamp` — atualizado em todo UPDATE |
| `failed_access_counter` | `int` | `INT` | Contador de tentativas de login malsucedidas |
| `failed_at` | `Instant` | `TIMESTAMP` (nullable) | Timestamp da última falha de autenticação |
| `last_ip` | `String` | `VARCHAR` (nullable) | Último IP de acesso registrado |
| `access` | `UserAccess` (Enum) | `ENUM/VARCHAR` | Role do usuário: `USER`, `ADM`, `MODERATOR` |

**Enums do Domínio:**

```java
// AccountStatus — ciclo de vida da conta
enum AccountStatus { PENDING, ACTIVE, SUSPENDED, BANNED }

// UserAccess — controle de acesso (Role)
enum UserAccess { USER, ADM, MODERATOR }
```

### 3.2 Entidade: `block_ip_user`

Mapeada pela classe `BlockIpUser.java`. Controla o bloqueio por endereço IP de origem após múltiplas tentativas de login malsucedidas provenientes do mesmo endereço.

| Coluna | Tipo Java | Tipo DB | Observação |
|---|---|---|---|
| `id` | `Long` | `BIGINT` | PK auto-gerada |
| `ip_user` | `String` | `VARCHAR` `UNIQUE` | Endereço IP do cliente |
| `count` | `int` | `INT` | Número de tentativas falhas acumuladas |
| `block_at` | `Instant` | `TIMESTAMP` (nullable) | Timestamp do primeiro registro de bloqueio |

### 3.3 Relacionamento entre Entidades

```
┌─────────────────────────────┐
│           accounts          │
│─────────────────────────────│
│ PK  id            UUID      │
│     email         VARCHAR   │◄── Unique Constraint
│     username      VARCHAR   │
│     status        ENUM      │─── PENDING | ACTIVE | SUSPENDED | BANNED
│     access        ENUM      │─── USER | ADM | MODERATOR
│     failed_access_counter   │
│     failed_at               │
│     last_ip       VARCHAR   │
└─────────────────────────────┘

┌─────────────────────────────┐
│        block_ip_user        │
│─────────────────────────────│
│ PK  id            BIGINT    │
│     ip_user       VARCHAR   │◄── Unique Constraint
│     count         INT       │
│     block_at      TIMESTAMP │
└─────────────────────────────┘
```

> **Nota:** As duas tabelas são independentes. A entidade `block_ip_user` não possui foreign key para `accounts`; o bloqueio por IP é ortogonal ao bloqueio por conta. Ambos os mecanismos atuam em conjunto durante o fluxo de login via `AccessCounterFailure`.

### 3.4 Value Objects Embarcados

Os Value Objects são Java Records anotados com `@Embeddable`. Suas colunas são "achatadas" na tabela `accounts` via `@AttributeOverride`.

| VO | Regras de Validação |
|---|---|
| `Email` | Regex RFC-compatível; lança `IllegalArgumentException` no construtor compacto se inválido |
| `Username` | Mínimo 5, máximo 12 caracteres; apenas alfanuméricos (`[a-zA-Z0-9]+`) |
| `Password` | Validação de complexidade via constraint customizada `@ValidPassword` (verificada no DTO, antes de instanciar o VO) |

---

## 4. Visão Geral da API / Endpoints

### 4.1 Base URL

```
http://localhost:4000
```

Todos os endpoints de autenticação estão sob o prefixo `/auth`.

### 4.2 Endpoints Públicos

Estes endpoints são liberados pela `SecurityFilterChain` sem necessidade de token.

---

#### `POST /auth/register` — Registro de nova conta

**Request Body:**

```json
{
  "user": "GankMaster",
  "name": "João",
  "lastname": "Silva",
  "birthday": "1990-07-15",
  "email": "joao.silva@email.com",
  "password": "Senha@Segura1",
  "recaptchaToken": "<token>"
}
```

| Campo | Tipo | Validações |
|---|---|---|
| `user` | `String` | `@NotBlank`, `@ValidUser` (5–12 chars, alfanumérico) |
| `name` | `String` | `@NotBlank`, `@Size(3,15)`, apenas letras (`[a-zA-ZÀ-ÿ ]`) |
| `lastname` | `String` | `@NotBlank`, `@Size(3,15)`, apenas letras (`[a-zA-ZÀ-ÿ ]`) |
| `birthday` | `String` | `@NotBlank`, formato `YYYY-MM-DD` |
| `email` | `String` | `@Email`, `@Size(max=100)` |
| `password` | `String` | `@ValidPassword` (constraint customizada de complexidade) |
| `recaptchaToken` | `String` | `@NotBlank` |

**Response — HTTP 201 Created:**

```json
{
  "username": "GankMaster",
  "email": "j***@email.com"
}
```

> **Nota de Segurança:** O e-mail retornado é mascarado pela classe `DataMasker` para evitar exposição desnecessária de dados pessoais.

---

#### `POST /auth/login` — Autenticação de conta

**Request Body:**

```json
{
  "user": "GankMaster",
  "password": "Senha@Segura1"
}
```

| Campo | Tipo | Validações |
|---|---|---|
| `user` | `String` | `@NotBlank`, `@Size(min=5, max=100)` |
| `password` | `String` | `@NotBlank` |

**Response — HTTP 200 OK:**

```json
{
  "loginTime": "2026-05-23T03:00:00Z",
  "claims": "eyJhbGciOiJIUzI1NiJ9..."
}
```

> O campo `claims` contém o **JWT Bearer Token** a ser utilizado nas requisições subsequentes via header `Authorization: Bearer <token>`.

---

### 4.3 Padrão de Resposta de Erro

Todas as exceções são capturadas pelo `GlobalExceptionHandler` e retornam o seguinte contrato JSON.

**Estrutura do erro (ambiente `dev`):**

```json
{
  "timestamp": "2026-05-23T03:01:00Z",
  "status": 409,
  "error": "CONFLICT",
  "message": "Usuário ou e-mail já em uso.",
  "trace": [
    {
      "className": "com.grankain.platformapi.auth.service.RegisterService",
      "fileName": "RegisterService.java",
      "lineNumber": 53,
      "methodName": "authRegister"
    }
  ],
  "path": "/auth/register"
}
```

**Estrutura do erro (ambiente `prod` — hardened):**

```json
{
  "timestamp": null,
  "status": null,
  "error": "CONFLICT",
  "message": null,
  "trace": null,
  "path": null
}
```

> Em produção, apenas o campo `error` é exposto para evitar **Information Exposure** (CWE-200). Nenhum detalhe técnico, caminho de arquivo ou stack trace é revelado ao cliente.

**Mapeamento de exceções para status HTTP:**

| Exceção | HTTP Status | Cenário |
|---|---|---|
| `AccountAlreadyExistsException` | `409 Conflict` | E-mail ou username já cadastrados |
| `BadCredentialsException` | `401 Unauthorized` | Senha incorreta, conta suspensa/banida, IP bloqueado |
| `MethodArgumentNotValidException` | `400 Bad Request` | Falha nas validações Jakarta Bean Validation (`@Valid`) |
| `DateTimeParseException` | `400 Bad Request` | Data de nascimento em formato inválido |
| `DataIntegrityViolationException` | `409 Conflict` | Violação de constraint no banco (fallback de duplicidade) |
| `ResponseStatusException` | (dinâmico) | Exceções HTTP lançadas programaticamente |
| `HttpClientErrorException` | (dinâmico) | Erros em chamadas a serviços externos |

---

## 5. Requisitos de Segurança

### 5.1 Autenticação via JWT (HS256)

A API é configurada como **OAuth2 Resource Server**. Após um login bem-sucedido, o `TokenGenerator` emite um JWT assinado com HMAC-SHA256 usando uma chave secreta injetada via variável de ambiente (`JWT_SECRET_KEY`).

**Claims do Token:**

```
iss  : "mmorpg-l2-api"
iat  : <timestamp de emissão>
exp  : <iat + 3600 segundos (1 hora)>
sub  : <username do jogador>
scope: "ROLE_USER" | "ROLE_ADM" | "ROLE_MODERATOR"
```

O `JwtDecoder` (bean `NimbusJwtDecoder`) valida assinatura e expiração automaticamente em cada requisição autenticada.

### 5.2 Hash de Senhas com Argon2id

O algoritmo selecionado para hashing de senhas é o **Argon2id**, vencedor do Password Hashing Competition (PHC) e recomendado pelo OWASP.

Configuração atual em `PasswordEncoderConfig`:

```java
new Argon2PasswordEncoder(
    16,    // saltLength (bytes)
    32,    // hashLength (bytes)
    1,     // parallelism
    60000, // memory cost (60 MB)
    10     // iterations
);
```

> Senhas nunca são armazenadas em texto plano. O hash é gerado antes de qualquer operação de persistência.

### 5.3 Proteção por Brute-Force

O componente `AccessCounterFailure` implementa dois mecanismos independentes de bloqueio progressivo:

| Gatilho | Limiar | Ação |
|---|---|---|
| Falhas de senha no mesmo e-mail/usuário | ≥ 5 tentativas | Status da conta muda para `SUSPENDED` |
| Requisições de login do mesmo endereço IP | ≥ 7 tentativas | `BadCredentialsException` lançada; IP registrado na tabela `block_ip_user` |

### 5.4 Headers de Segurança HTTP

Configurados no `SecurityFilterChain` dentro de `SecurityConfig`:

| Header | Valor | Propósito |
|---|---|---|
| `X-Content-Type-Options` | `nosniff` | Previne MIME-type sniffing pelo browser |
| `X-Frame-Options` | `DENY` | Previne ataques de Clickjacking via `<iframe>` |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` | Força HTTPS (habilitado **somente no profile `prod`**) |

### 5.5 Política de CORS

Configura permissões de Cross-Origin estritamente:

```yaml
# Origens permitidas lidas de variável de ambiente
CORS_ORIGINS: http://localhost:3000   # (dev)

# Métodos permitidos
Allowed-Methods: GET, POST

# Headers aceitos do cliente
Allowed-Headers: Authorization, Content-Type, Accept

# Headers expostos ao frontend
Exposed-Headers: Authorization
```

### 5.6 Configuração Stateless

A aplicação não cria ou usa sessão HTTP (`SessionCreationPolicy.STATELESS`). O `Request Cache` também é desabilitado, comportamento adequado para API REST autenticada via Bearer token.

### 5.7 Container sem Usuário Root

O `Dockerfile` cria um usuário dedicado `appuser` no grupo `appgroup` e executa o processo Java sem privilégios de root:

```dockerfile
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
```

---

## 6. Guia de Inicialização Rápida (Getting Started)

### 6.1 Pré-requisitos

| Ferramenta | Versão mínima | Observação |
|---|---|---|
| Docker Engine | 24+ | Para build e execução containerizada |
| Docker Compose | v2 (plugin) | `docker compose` (sem hífen) |
| Java JDK | 21 (opcional) | Necessário apenas para execução local fora do Docker |
| MySQL | 8.0+ | Container externo `mysql-l2_game` na rede `mmorpg-net` |

> **Pré-requisito de infraestrutura:** O container MySQL (`mysql-l2_game`) e a rede Docker (`mmorpg-net`) devem estar em execução antes de iniciar a API. A rede é gerenciada externamente por outro Compose.

### 6.2 Clonando o Repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd mmorpg-platform-api
```

### 6.3 Configurando Variáveis de Ambiente

```bash
# Copie o arquivo de exemplo
cp .env.example .env

# Edite com seus valores reais
nano .env
```

**Conteúdo do `.env` a ser configurado:**

```dotenv
# Perfil Spring ativo (dev | prod)
SPRING_PROFILES_ACTIVE=dev

# Credenciais do banco MySQL externo
MYSQL_DATABASE=gk_web_user
MYSQL_USER=gk_web_user
MYSQL_PASSWORD=SUA_SENHA_AQUI

# Origens permitidas pelo CORS (URL do frontend)
CORS_ORIGINS=http://localhost:3000
```

> ⚠️ O arquivo `.env` está listado no `.gitignore` e **nunca deve ser commitado** no repositório.

> ⚠️ A variável `JWT_SECRET_KEY` deve ser definida por variável de ambiente no ambiente de produção. Nunca commite chaves secretas.

### 6.4 Subindo a Aplicação com Docker Compose

```bash
# Build da imagem e inicialização do container
docker compose up -d --build

# Acompanhar os logs em tempo real
docker compose logs -f api

# Verificar se a API está no ar
curl http://localhost:4000/actuator/health
```

**Resposta esperada do health check:**
```json
{ "status": "UP" }
```

### 6.5 Parando a Aplicação

```bash
docker compose down
```

### 6.6 Execução Local (sem Docker)

Para desenvolvimento local sem containerização:

```bash
# Na raiz do projeto
export LOGIN_DB_URL=jdbc:mysql://localhost:3306/gk_web_user?useSSL=false&serverTimezone=UTC
export LOGIN_DB_USER=gk_web_user
export LOGIN_DB_PASS=SUA_SENHA
export SPRING_APPLICATION_CORS_ORIGINS=http://localhost:3000
export JWT_SECRET_KEY=SUA_CHAVE_SECRETA_AQUI

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 6.7 Executando os Testes

```bash
# Executar todos os testes
./mvnw test

# Executar apenas uma classe de teste específica
./mvnw test -Dtest=EncodedPasswordTest
```

---

## 7. Roadmap Futuro (Fora de Escopo Atual)

Os itens abaixo estão planejados para releases futuras e **não fazem parte do escopo desta versão inicial**:

- [ ] **Sistema de CAPTCHA** — Integração com provedor (ex: Google reCAPTCHA v3 ou hCaptcha) para validação anti-bot no registro e no login.
- [ ] **Dashboard de Métricas e Indicadores** — Painel administrativo para visualização de métricas de acesso, tentativas de login, contas por status e estatísticas gerais da plataforma.
- [ ] **Verificação de E-mail (Double Opt-in)** — Fluxo de ativação de conta via link enviado por e-mail; atualmente contas são criadas com status `PENDING`.
- [ ] **Refresh Token** — Mecanismo de renovação do JWT sem necessidade de novo login, aumentando a segurança e a experiência do usuário.
- [ ] **Auditoria e Logging Estruturado** — Integração com stack de observabilidade (ex: ELK Stack ou Loki/Grafana) para rastreamento de eventos de segurança.
