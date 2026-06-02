# Grankain Platform API — Guia de Integração para o Frontend

> **Para:** Time de Frontend (Next.js / TypeScript)
> **Módulo:** Autenticação e Gestão de Contas
> **Versão da API:** `0.0.1-SNAPSHOT`
> **Última atualização:** 2026-06-01

---

## 1. Informações Globais da API

### 1.1 Base URL por Ambiente

| Ambiente            | Base URL                      |
|---------------------|-------------------------------|
| **Desenvolvimento** | `http://localhost:4000`       |
| **Produção**        | *(a definir conforme deploy)* |

> Todos os paths de endpoint neste documento são **relativos** à Base URL.
> Exemplo: `POST /auth/register` → `POST http://localhost:4000/auth/register`.

### 1.2 Headers Padrão Obrigatórios

Toda requisição à API deve incluir os seguintes headers:

```http
Content-Type: application/json
Accept: application/json
```

Para endpoints que exigem autenticação (qualquer rota fora de `/auth/register` e `/auth/login`), inclua também:

```http
Authorization: Bearer <seu_jwt_token>
```

### 1.3 Configuração Recomendada com `fetch`

**Exemplo base com `fetch` (Next.js Server Action / Route Handler):**

```typescript
const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:4000';

async function apiRequest<T>(
    path: string,
    options: RequestInit = {}
): Promise<T> {
    const response = await fetch(`${BASE_URL}${path}`, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
            ...options.headers,
        },
    });

    if (!response.ok) {
        const error = await response.json();
        throw error; // lança o ApiError para ser tratado no chamador
    }

    return response.json() as Promise<T>;
}
```

---

## 2. Fluxo de Autenticação e Segurança

### 2.1 Como o Frontend deve se autenticar

A API utiliza **JWT (JSON Web Token)** assinado com o algoritmo **HS256**.
O token é retornado no campo `claims` da resposta do endpoint `POST /auth/login`.

**Fluxo completo:**

```
1. Frontend → POST /auth/login  (com credenciais)
2. API       ← 200 OK { loginTime, claims: "eyJ..." }
3. Frontend → Armazena o token (ver seção 2.2)
4. Frontend → Inclui em toda requisição protegida:
              Authorization: Bearer eyJ...
5. API       → Valida o token automaticamente antes de processar a requisição
```

### 2.2 Onde armazenar o token

| Estratégia                      | Segurança         | Recomendação                 |
|---------------------------------|-------------------|------------------------------|
| `localStorage`                  | Vulnerável a XSS  | Evitar                       |
| `sessionStorage`                | Vulnerável a XSS  | Evitar                       |
| Cookie `HttpOnly` (server-side) | Seguro contra XSS | **Recomendado** para Next.js |
| Memória (estado React em SSR)   | Seguro            | Alternativa para SPAs        |

> **Recomendação para Next.js:** Salve o token em um cookie `HttpOnly` via um Route Handler server-side. Nunca exponha o
> token no bundle do cliente.

```typescript
// Exemplo: app/api/auth/login/route.ts
import {cookies} from 'next/headers';

export async function POST(request: Request) {
    const body = await request.json();
    const apiResponse = await fetch('http://localhost:4000/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body),
    });

    const data = await apiResponse.json();

    if (!apiResponse.ok) {
        return Response.json(data, {status: apiResponse.status});
    }

    // Salva o JWT em um cookie HttpOnly (inacessível ao JavaScript do browser)
    cookies().set('auth_token', data.claims, {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'lax',
        maxAge: 3600, // 1 hora — deve corresponder à expiração do JWT
        path: '/',
    });

    return Response.json({loginTime: data.loginTime});
}
```

### 2.3 Expiração do Token

O JWT emitido pela API expira em **3600 segundos (1 hora)** a partir do momento da emissão.

Quando o token expirar, a API retornará:

```http
HTTP/1.1 401 Unauthorized
```

```json
{
  "timestamp": null,
  "status": null,
  "error": "UNAUTHORIZED",
  "message": null,
  "trace": null,
  "path": null
}
```

> **Não existe endpoint de Refresh Token nesta versão.**
> O Frontend deve redirecionar o usuário para a tela de login ao receber um `401` em uma rota protegida.

**Tratamento recomendado em Next.js Middleware:**

```typescript
// middleware.ts
import {NextResponse} from 'next/server';
import type {NextRequest} from 'next/server';

export function middleware(request: NextRequest) {
    const token = request.cookies.get('auth_token');
    const isProtectedRoute = request.nextUrl.pathname.startsWith('/user') || request.nextUrl.pathname.startsWith('/gamer');

    if (isProtectedRoute && !token) {
        return NextResponse.redirect(new URL('/login', request.url));
    }

    return NextResponse.next();
}
```

---

## 3. Padrão de Respostas Globais

### 3.1 Estrutura de Sucesso

A API **não encapsula** as respostas de sucesso em um wrapper genérico.
Cada endpoint retorna o DTO diretamente no corpo da resposta.

```http
HTTP/1.1 200 OK  (ou 201 Created)
Content-Type: application/json
```

```json
{
  "campo1": "valor1",
  "campo2": "valor2"
}
```

### 3.2 Estrutura de Erro — `ApiError`

Todos os erros seguem o contrato abaixo. O comportamento **varia por ambiente**:

```typescript
interface ApiTraceItem {
    className: string;
    fileName: string;
    lineNumber: number;
    methodName: string;
}

interface ApiError {
    timestamp: string | null;     // ISO 8601. Presente apenas em DEV
    status: number | null;        // Ex: 400, 409. Presente apenas em DEV
    error: string;                // Ex: "CONFLICT". SEMPRE presente
    message: string | null;       // Mensagem legível. Presente apenas em DEV
    trace: ApiTraceItem[] | null; // Stack trace filtrado. Presente apenas em DEV
    path: string | null;          // URL que gerou o erro. Presente apenas em DEV
}
```

**Em `dev` — todos os campos preenchidos:**

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

**Em `prod` — hardened (apenas `error` é retornado):**

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

### 3.3 Como o Frontend deve ler as mensagens de erro

Em produção, `message` é sempre `null`. O Frontend **não deve depender de `message`** para exibir textos ao usuário.
A lógica correta é mapear o campo `error` (sempre presente) e/ou o HTTP status code para mensagens definidas no próprio
frontend.

```typescript
// utils/api-error-handler.ts

const ERROR_MESSAGES: Record<string, string> = {
    CONFLICT: 'Este usuário ou e-mail já está cadastrado.',
    UNAUTHORIZED: 'Credenciais inválidas. Verifique seu usuário e senha.',
    BAD_REQUEST: 'Os dados enviados são inválidos. Verifique o formulário.',
    INTERNAL_SERVER_ERROR: 'Ocorreu um erro inesperado. Tente novamente mais tarde.',
};

export function getErrorMessage(apiError: ApiError): string {
    // Em DEV, usa a mensagem do backend diretamente (útil para debug)
    if (process.env.NODE_ENV === 'development' && apiError.message) {
        return apiError.message;
    }
    // Em PROD, mapeia o campo 'error' para uma mensagem definida no frontend
    return ERROR_MESSAGES[apiError.error] ?? 'Ocorreu um erro inesperado.';
}
```

### 3.4 Tabela de Status HTTP e Causas

| HTTP Status                 | `error`                 | Causa Mais Comum                                                           |
|-----------------------------|-------------------------|----------------------------------------------------------------------------|
| `400 Bad Request`           | `BAD_REQUEST`           | Campo obrigatório ausente, formato inválido, data incorreta                |
| `401 Unauthorized`          | `UNAUTHORIZED`          | Credenciais inválidas, conta suspensa/banida, IP bloqueado, token expirado |
| `404 Not Found`             | `NOT_FOUND`             | E-mail ou username já cadastrado / Conta Inexistente                       |
| `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` | Erro inesperado no servidor                                                |

---

## 4. Contratos de API

---

### 4.1 Registrar Nova Conta

#### Propósito

Cria uma nova conta de jogador na plataforma. A conta é criada com status `PENDING`.

#### Método e Endpoint

```
POST /auth/register
```

#### Headers

```http
Content-Type: application/json
Accept: application/json
```

> Não envie `Authorization` neste endpoint — ele é público.

#### Corpo da Requisição (Payload)

```json
{
  "user": "GankMaster",
  "name": "João",
  "lastname": "Silva",
  "email": "joao.silva@email.com",
  "password": "Senha@Segura1"
}
```

| Campo      | Tipo     | Obrigatório | Restrições                                                                         |
|------------|----------|-------------|------------------------------------------------------------------------------------|
| `user`     | `string` | Sim         | Mínimo 5, máximo 12 caracteres; apenas letras e números `[a-zA-Z0-9]`; sem espaços |
| `name`     | `string` | Sim         | Entre 1 e 15 caracteres; apenas letras (incluindo acentuadas)                      |
| `lastname` | `string` | Sim         | Entre 1 e 15 caracteres; apenas letras (incluindo acentuadas)                      |
| `email`    | `string` | Sim         | E-mail válido; máximo 100 caracteres                                               |
| `password` | `string` | Sim         | Validação de complexidade via `@ValidPassword` (regras definidas no backend)       |

#### Respostas Esperadas

**`201 Created` — Conta criada com sucesso:**

```json
{
  "username": "GankMaster",
  "email": "jo***@email.com"
}
```

> O e-mail é mascarado na resposta por conformidade com LGPD/GDPR.
> Exemplo: `"joao.silva@email.com"` → `"jo*************@email.com"`.

---

**`400 Bad Request` — Validação de campos falhou:**
* Ocorre quando algum campo enviado não respeita as validações do DTO.
* **Exceção no Backend:** Lança `MethodArgumentNotValidException`.
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "user: Username é obrigatório",
  "trace": null,
  "path": "/auth/register"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "BAD_REQUEST",
  "message": null,
  "trace": null,
  "path": null
}
```

---

**`409 Conflict` — Usuário ou e-mail já cadastrado:**
* Ocorre quando o nome de usuário ou e-mail já existem no banco de dados.
* **Exceção no Backend:** Lança `UserAlreadyExistsException` com a mensagem `"Usuário ou e-mail já em uso."` (`throw new UserAlreadyExistsException("Usuário ou e-mail já em uso.");`).
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 409,
  "error": "CONFLICT",
  "message": "Usuário ou e-mail já em uso.",
  "trace": [],
  "path": "/auth/register"
}
```
* **Em PROD:**
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

---

#### TypeScript — Interfaces para este Endpoint

```typescript
// types/auth.ts

/** Payload enviado no corpo da requisição de registro */
export interface RegisterRequest {
    user: string;
    name: string;
    lastname: string;
    email: string;
    password: string;
}

/** Resposta de sucesso do endpoint POST /auth/register */
export interface RegisterResponse {
    username: string;
    email: string; // e-mail mascarado (ex: "jo***@email.com")
}
```

**Exemplo de uso em um Server Action (Next.js):**

```typescript
// actions/register.ts
'use server';

import type {RegisterRequest, RegisterResponse} from '@/types/auth';
import type {ApiError} from '@/types/api';

export async function registerAction(
    payload: RegisterRequest
): Promise<{ data?: RegisterResponse; error?: ApiError }> {
    const res = await fetch('http://localhost:4000/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
        },
        body: JSON.stringify(payload),
    });

    const json = await res.json();

    if (!res.ok) return {error: json as ApiError};

    return {data: json as RegisterResponse};
}
```

---

### 4.2 Login de Conta

#### Propósito

Autentica um jogador existente com `username` **ou** `email` + `password`.
Em caso de sucesso, retorna o JWT Bearer Token.

> **Recurso importante:** O campo `user` aceita tanto o **username** quanto o **e-mail** do jogador.
> O backend detecta automaticamente o tipo de input pela presença do caractere `@`.

#### Método e Endpoint

```
POST /auth/login
```

#### Headers

```http
Content-Type: application/json
Accept: application/json
```

> Não envie `Authorization` neste endpoint — ele é público.

#### Corpo da Requisição (Payload)

**Login com username:**

```json
{
  "user": "GankMaster",
  "password": "Senha@Segura1"
}
```

**Login com e-mail:**

```json
{
  "user": "joao.silva@email.com",
  "password": "Senha@Segura1"
}
```

| Campo      | Tipo     | Obrigatório | Restrições                                                 |
|------------|----------|-------------|------------------------------------------------------------|
| `user`     | `string` | Sim         | Mínimo 5, máximo 100 caracteres. Aceita username ou e-mail |
| `password` | `string` | Sim         | Obrigatório; sem restrição de formato na entrada           |

#### Respostas Esperadas

**`200 OK` — Login bem-sucedido:**

```json
{
  "token": "eyJ..."
}
```

**Estrutura do JWT decodificado (payload):**

```json
{
  "iss": "mmorpg-l2-api",
  "sub": "4e73b22e-13c5-4309-8809-90604cfb2034",
  "scope": "ROLE_USER",
  "iat": 1748062600,
  "exp": 1748066200
}
```

| Claim   | Descrição                                                          |
|---------|--------------------------------------------------------------------|
| `iss`   | Issuer (emissor): sempre `"mmorpg-l2-api"`                         |
| `sub`   | Subject: UUID do jogador autenticado                               |
| `scope` | Role do usuário: `"ROLE_USER"`, `"ROLE_ADM"` ou `"ROLE_MODERATOR"` |
| `iat`   | Issued At: timestamp de emissão (Unix)                             |
| `exp`   | Expiration: `iat + 3600`. Token expira após **1 hora**             |

---

**`400 Bad Request` — Campo ausente ou inválido:**
* Ocorre se os campos não respeitarem o tamanho ou validações.
* **Exceção no Backend:** Lança `MethodArgumentNotValidException`.
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "user: Username é obrigatório",
  "trace": null,
  "path": "/auth/login"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "BAD_REQUEST",
  "message": null,
  "trace": null,
  "path": null
}
```

---

**`401 Unauthorized` — Credenciais inválidas (usuário incorreto ou senha incorreta):**
* Ocorre quando o usuário não existe ou a senha está incorreta.
* **Exceção no Backend:** Lança `BadCredentialsException` com a mensagem `"Invalid username or password"` (`throw new BadCredentialsException("Invalid username or password");`).
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid username or password",
  "trace": [],
  "path": "/auth/login"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "UNAUTHORIZED",
  "message": null,
  "trace": null,
  "path": null
}
```

---

**`401 Unauthorized` — IP bloqueado temporariamente por força bruta:**
* Ocorre após 7 ou mais tentativas falhas vindas do mesmo IP.
* **Exceção no Backend:** Lança `BadCredentialsException` com a mensagem `"IP address temporarily blocked due to excessive failures."` (`throw new BadCredentialsException("IP address temporarily blocked due to excessive failures.");`).
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "IP address temporarily blocked due to excessive failures.",
  "trace": [],
  "path": "/auth/login"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "UNAUTHORIZED",
  "message": null,
  "trace": null,
  "path": null
}
```

---

**`401 Unauthorized` — Conta suspensa temporariamente:**
* Ocorre após 5 ou mais tentativas de login falhas consecutivas nesta conta.
* **Exceção no Backend:** Lança `BadCredentialsException` com a mensagem `"Account is temporarily suspended."` (`throw new BadCredentialsException("Account is temporarily suspended.");`).
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Account is temporarily suspended.",
  "trace": [],
  "path": "/auth/login"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "UNAUTHORIZED",
  "message": null,
  "trace": null,
  "path": null
}
```

---

**`401 Unauthorized` — Status da conta inativo (ex: PENDING, BANNED):**
* Ocorre quando a conta está cadastrada mas com status inativo.
* **Exceção no Backend:** Lança `BadCredentialsException` com a mensagem sendo o nome do status do usuário (ex: `throw new BadCredentialsException(user.getStatus().name());`).
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "PENDING",
  "trace": [],
  "path": "/auth/login"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "UNAUTHORIZED",
  "message": null,
  "trace": null,
  "path": null
}
```

---

#### TypeScript — Interfaces para este Endpoint

```typescript
// types/auth.ts

/** Payload enviado no corpo da requisição de login */
export interface LoginRequest {
    /** Username (ex: "GankMaster") ou e-mail (ex: "user@mail.com") */
    user: string;
    password: string;
}

/** Resposta de sucesso do endpoint POST /auth/login */
export interface LoginResponse {
    /** JWT Bearer Token. Armazenar em cookie HttpOnly, nunca em localStorage */
    token: string;
}

/** Payload decodificado do JWT */
export interface JwtPayload {
    iss: string;      // "mmorpg-l2-api"
    sub: string;      // UUID do jogador
    scope: UserRole;  // role do usuário
    iat: number;      // Unix timestamp de emissão
    exp: number;      // Unix timestamp de expiração (iat + 3600)
}

export type UserRole = 'ROLE_USER' | 'ROLE_ADM' | 'ROLE_MODERATOR';
```

**Exemplo de uso em um Server Action (Next.js):**

```typescript
// actions/login.ts
'use server';

import {cookies} from 'next/headers';
import type {LoginRequest, LoginResponse} from '@/types/auth';
import type {ApiError} from '@/types/api';

export async function loginAction(
    payload: LoginRequest
): Promise<{ success?: boolean; error?: ApiError }> {
    const res = await fetch('http://localhost:4000/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
        },
        body: JSON.stringify(payload),
    });

    const json = await res.json();

    if (!res.ok) return {error: json as ApiError};

    const {token} = json as LoginResponse;

    // Salva o JWT em cookie HttpOnly — inacessível ao JavaScript do browser
    cookies().set('auth_token', token, {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'lax',
        maxAge: 3600,
        path: '/',
    });

    return {success: true};
}
```

---

### 4.3 Obter Perfil do Usuário

#### Propósito

Retorna os dados detalhados do perfil do usuário autenticado na plataforma.

#### Método e Endpoint

```
POST /user/me
```

#### Headers

```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer <seu_jwt_token>
```

#### Corpo da Requisição (Payload)

*Não requer corpo na requisição. O usuário é identificado pelo JWT.*

#### Respostas Esperadas

**`200 OK` — Consulta realizada com sucesso:**

```json
{
  "login": "GankMaster",
  "fullName": "João Silva",
  "email": "joao.silva@email.com",
  "birthDate": "1990-07-15",
  "createdTime": "2026-06-01T12:00:00Z",
  "lastActive": "2026-06-02T12:45:00Z",
  "status": "ACTIVE"
}
```

---

**`401 Unauthorized` — Token expirado, inválido ou ausente:**
* Ocorre quando o header `Authorization` está ausente ou o JWT expirou.
* **Exceção no Backend:** Lançado pelos filtros de segurança do Spring Security.
```json
{
  "timestamp": null,
  "status": null,
  "error": "UNAUTHORIZED",
  "message": null,
  "trace": null,
  "path": null
}
```

---

**`401 Unauthorized` — Conta de usuário inativa:**
* Ocorre quando o usuário do JWT é válido, mas o status da conta no banco gk_web_user não é `ACTIVE`.
* **Exceção no Backend:** Lança `BadCredentialsException` com a mensagem `"Account is not activated."` (`throw new BadCredentialsException("Account is not activated.");`).
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Account is not activated.",
  "trace": [],
  "path": "/user/me"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "UNAUTHORIZED",
  "message": null,
  "trace": null,
  "path": null
}
```

---

#### TypeScript — Interfaces para este Endpoint

```typescript
// types/user.ts

/** Resposta de sucesso do endpoint POST /user/me */
export interface UserProfileResponse {
    login: string;
    fullName: string;
    email: string;
    birthDate: string | null;  // formato: "YYYY-MM-DD" ou null se não definido
    createdTime: string;       // ISO 8601 UTC
    lastActive: string;        // ISO 8601 UTC
    status: string;            // Ex: "ACTIVE", "PENDING", "SUSPENDED", "BANNED"
}
```

---

### 4.4 Definir Data de Nascimento

#### Propósito

Define a data de nascimento do usuário autenticado. **Regra de Negócio:** Só pode ser preenchida uma única vez; modificações posteriores não são permitidas.

#### Método e Endpoint

```
POST /user/setBirthday
```

#### Headers

```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer <seu_jwt_token>
```

#### Corpo da Requisição (Payload)

```json
{
  "birthday": "1990-07-15"
}
```

| Campo      | Tipo     | Obrigatório | Restrições                                                              |
|------------|----------|-------------|-------------------------------------------------------------------------|
| `birthday` | `string` | Sim         | Formato ISO `YYYY-MM-DD`. Deve ser uma data válida e no passado         |

#### Respostas Esperadas

**`200 OK` — Data gravada com sucesso:**

```json
true
```

---

**`400 Bad Request` — Validação falhou ou formato de data inválido:**
* Ocorre se a data for nula, no futuro, ou não obedecer ao padrão `YYYY-MM-DD`.
* **Exceção no Backend:** Lança `MethodArgumentNotValidException` (para anotações do DTO) ou `DateTimeParseException` (formato inadequado).
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "birthday: Birthday is required",
  "trace": null,
  "path": "/user/setBirthday"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "BAD_REQUEST",
  "message": null,
  "trace": null,
  "path": null
}
```

---

**`401 Unauthorized` — Token expirado ou Conta inativa:**
* **Exceção no Backend:** Lança `BadCredentialsException` com a mensagem `"Account is not activated."` (`throw new BadCredentialsException("Account is not activated.");`).
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Account is not activated.",
  "trace": [],
  "path": "/user/setBirthday"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "UNAUTHORIZED",
  "message": null,
  "trace": null,
  "path": null
}
```

---

**`500 Internal Server Error` — Data de nascimento já definida anteriormente:**
* Ocorre se o usuário tentar preencher a data de nascimento após ela já ter sido registrada no banco.
* **Exceção no Backend:** Lança `IllegalStateException` com a mensagem `"Birthday has already been set and cannot be changed."` (`throw new IllegalStateException("Birthday has already been set and cannot be changed.");`).
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 500,
  "error": "INTERNAL_SERVER_ERROR",
  "message": "Birthday has already been set and cannot be changed.",
  "trace": [],
  "path": "/user/setBirthday"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "INTERNAL_SERVER_ERROR",
  "message": null,
  "trace": null,
  "path": null
}
```

---

#### TypeScript — Interfaces para este Endpoint

```typescript
// types/user.ts

/** Payload enviado no corpo da requisição de setBirthday */
export interface BirthdayRequest {
    birthday: string; // formato: "YYYY-MM-DD"
}
```

---

### 4.5 Alterar Senha do Usuário Autenticado

#### Propósito

Permite que o usuário autenticado altere sua própria senha. O endpoint exige a senha atual como confirmação de identidade antes de aplicar a nova senha.

> **Regra de negócio:** A nova senha não pode ser idêntica à senha atual.

#### Método e Endpoint

```
POST /user/changePassword
```

#### Headers

```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer <seu_jwt_token>
```

#### Corpo da Requisição (Payload)

```json
{
  "oldPassword": "SenhaAntiga@1",
  "newPassword": "NovaSenha@2"
}
```

| Campo         | Tipo     | Obrigatório | Restrições                                                              |
|---------------|----------|-------------|-------------------------------------------------------------------------|
| `oldPassword` | `string` | Sim         | Senha atual do usuário. Validada via `@ValidPassword` (complexidade)    |
| `newPassword` | `string` | Sim         | Nova senha. Validada via `@ValidPassword`. Não pode ser igual à antiga  |

#### Respostas Esperadas

**`200 OK` — Senha alterada com sucesso:**

```json
true
```

> Retorna o booleano `true` diretamente no corpo da resposta.

---

**`400 Bad Request` — Validação de campos falhou:**
* Ocorre se os novos campos não atenderem à complexidade exigida por `@ValidPassword`.
* **Exceção no Backend:** Lança `MethodArgumentNotValidException`.
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "newPassword: Password must contain at least...",
  "trace": null,
  "path": "/user/changePassword"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "BAD_REQUEST",
  "message": null,
  "trace": null,
  "path": null
}
```

---

**`401 Unauthorized` — Senha atual incorreta, nova senha igual à antiga ou conta inativa:**
* **Em DEV (Senha atual incorreta):**
  * **Exceção no Backend:** Lança `BadCredentialsException` com a mensagem `"Invalid old password."` (`throw new BadCredentialsException("Invalid old password.");`).
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid old password.",
  "trace": [],
  "path": "/user/changePassword"
}
```
* **Em DEV (Nova senha idêntica à antiga):**
  * **Exceção no Backend:** Lança `BadCredentialsException` com a mensagem `"New password cannot be same as old password."` (`throw new BadCredentialsException("New password cannot be same as old password.");`).
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "New password cannot be same as old password.",
  "trace": [],
  "path": "/user/changePassword"
}
```
* **Em DEV (Conta inativa):**
  * **Exceção no Backend:** Lança `BadCredentialsException` com a mensagem `"Account is not activated."` (`throw new BadCredentialsException("Account is not activated.");`).
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Account is not activated.",
  "trace": [],
  "path": "/user/changePassword"
}
```
* **Em PROD (Qualquer um dos cenários acima):**
```json
{
  "timestamp": null,
  "status": null,
  "error": "UNAUTHORIZED",
  "message": null,
  "trace": null,
  "path": null
}
```

---

#### TypeScript — Interfaces para este Endpoint

```typescript
// types/user.ts

/** Payload enviado no corpo da requisição de alteração de senha */
export interface ChangePasswordRequest {
    /** Senha atual do usuário (deve satisfazer @ValidPassword) */
    oldPassword: string;
    /** Nova senha desejada (deve satisfazer @ValidPassword e ser diferente da atual) */
    newPassword: string;
}
```

**Exemplo de uso em um Server Action (Next.js):**

```typescript
// actions/change-password.ts
'use server';

import { cookies } from 'next/headers';
import type { ChangePasswordRequest } from '@/types/user';
import type { ApiError } from '@/types/api';

export async function changePasswordAction(
    payload: ChangePasswordRequest
): Promise<{ success?: true; error?: ApiError }> {
    const token = cookies().get('auth_token')?.value;

    if (!token) {
        return { error: { error: 'UNAUTHORIZED', timestamp: null, status: null, message: null, trace: null, path: null } };
    }

    const res = await fetch('http://localhost:4000/user/changePassword', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
    });

    if (!res.ok) {
        const json = await res.json();
        return { error: json as ApiError };
    }

    return { success: true };
}
```

---

### 4.6 Consultar Contas de Jogo e Personagens

#### Propósito

Retorna as contas de jogo do Lineage 2 vinculadas ao usuário autenticado, juntamente com a lista de personagens de cada conta.

#### Método e Endpoint

```
POST /gamer/account
```

#### Headers

```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer <seu_jwt_token>
```

#### Corpo da Requisição (Payload)

*Não requer corpo na requisição. O usuário é identificado pelo JWT.*

#### Respostas Esperadas

**`200 OK` — Consulta bem-sucedida:**

Retorna um objeto onde as chaves são os nomes de login das contas de jogo, e os valores são listas de personagens.

```json
{
  "MinhaContaL2": [
    {
      "charName": "ElfMaster",
      "lvl": 40,
      "maxHp": 1200.5,
      "maxMp": 800.0,
      "maxCp": 300.0,
      "race": 1,
      "baseClassId": 25,
      "classId": 26,
      "exp": 2500000,
      "karma": 0
    }
  ]
}
```

---

**`401 Unauthorized` — Token expirado ou Conta inativa:**
* Ocorre se o JWT for inválido ou a conta da plataforma não estiver ativa.
* **Exceção no Backend:** Lança `BadCredentialsException` para conta inativa.

---

**`404 Not Found` — Nenhuma conta de jogo vinculada encontrada:**
* Ocorre se o usuário da plataforma não possuir nenhuma conta de jogo vinculada no banco de dados do emulador.
* **Exceção no Backend:** Lança `GameAccountNotFoundException` com a mensagem `"No gamer accounts found"` (`throw new GameAccountNotFoundException("No gamer accounts found");`).
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "No gamer accounts found",
  "trace": [],
  "path": "/gamer/account"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "NOT_FOUND",
  "message": null,
  "trace": null,
  "path": null
}
```

---

### 4.7 Criar Conta de Jogo

#### Propósito

Cria uma nova conta de jogo no emulador de Lineage 2 e a vincula ao usuário da plataforma.

#### Método e Endpoint

```
POST /gamer/create
```

#### Headers

```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer <seu_jwt_token>
```

#### Corpo da Requisição (Payload)

```json
{
  "login": "meuloginL2",
  "password": "Senha@Segura1"
}
```

| Campo      | Tipo     | Obrigatório | Restrições                                                                 |
|------------|----------|-------------|----------------------------------------------------------------------------|
| `login`    | `string` | Sim         | Mínimo 5, máximo 12 caracteres; apenas alfanuméricos (`[a-zA-Z0-9]+`)      |
| `password` | `string` | Sim         | Validação de complexidade via `@ValidPassword`                             |

#### Respostas Esperadas

**`200 OK` — Conta criada com sucesso:**

```json
true
```

---

**`400 Bad Request` — Validação falhou:**
* Ocorre se o login ou a senha fornecidos não atenderem às restrições.
* **Exceção no Backend:** Lança `MethodArgumentNotValidException`.
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "login: login must be alphanumeric",
  "trace": null,
  "path": "/gamer/create"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "BAD_REQUEST",
  "message": null,
  "trace": null,
  "path": null
}
```

---

**`401 Unauthorized` — Token expirado ou Conta inativa:**
* **Em DEV (Conta inativa):**
  * **Exceção no Backend:** Lança `BadCredentialsException` com a mensagem `"Account is not activated"` (`throw new BadCredentialsException("Account is not activated");`).
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Account is not activated.",
  "trace": [],
  "path": "/gamer/create"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "UNAUTHORIZED",
  "message": null,
  "trace": null,
  "path": null
}
```

---

**`404 Not Found` — Limite de 3 contas de jogo atingido:**
* Ocorre se o usuário da plataforma já possuir 3 contas de jogo cadastradas (limite máximo).
* **Exceção no Backend:** Lança `GameAccountNotFoundException` com a mensagem `"You can only have 3 gamer accounts"` (`throw new GameAccountNotFoundException("You can only have 3 gamer accounts");`).
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "You can only have 3 gamer accounts",
  "trace": [],
  "path": "/gamer/create"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "NOT_FOUND",
  "message": null,
  "trace": null,
  "path": null
}
```

---

**`404 Not Found` — Login da conta de jogo já existente:**
* Ocorre se o login de jogo solicitado já estiver cadastrado no emulador.
* **Exceção no Backend:** Lança `GameAccountNotFoundException` com a mensagem `"Account already exists"` (`throw new GameAccountNotFoundException("Account already exists");`).
* **Em DEV:**
```json
{
  "timestamp": "2026-06-02T12:45:00Z",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Account already exists",
  "trace": [],
  "path": "/gamer/create"
}
```
* **Em PROD:**
```json
{
  "timestamp": null,
  "status": null,
  "error": "NOT_FOUND",
  "message": null,
  "trace": null,
  "path": null
}
```

---

## 5. Interface Global de Erro (TypeScript)

Copie este arquivo para seu projeto e use em todos os tratamentos de erro da API.

```typescript
// types/api.ts

export interface ApiTraceItem {
    className: string;
    fileName: string;
    lineNumber: number;
    methodName: string;
}

/**
 * Estrutura de erro padrão retornada pela Grankain Platform API.
 *
 * ATENCAO: Em producao, apenas o campo `error` e garantido como nao-nulo.
 * Nao dependa de `message`, `trace`, `timestamp` ou `path` em logica de producao.
 */
export interface ApiError {
    /** Timestamp ISO 8601. Presente apenas em DEV. */
    timestamp: string | null;
    /** HTTP status code numerico. Presente apenas em DEV. */
    status: number | null;
    /** Nome do erro HTTP (ex: "CONFLICT", "UNAUTHORIZED"). Sempre presente. */
    error: string;
    /** Mensagem legivel. Presente apenas em DEV. Nunca exiba diretamente em producao. */
    message: string | null;
    /** Stack trace filtrado para classes do projeto. Presente apenas em DEV. */
    trace: ApiTraceItem[] | null;
    /** URL que originou o erro. Presente apenas em DEV. */
    path: string | null;
}

/** Verifica se um objeto desconhecido e um ApiError (type guard) */
export function isApiError(value: unknown): value is ApiError {
    return (
        typeof value === 'object' &&
        value !== null &&
        'error' in value &&
        typeof (value as ApiError).error === 'string'
    );
}
```

---

## 6. Referência Rápida de Endpoints

| Método   | Endpoint                  | Autenticação               | Descrição                                     |
|----------|---------------------------|----------------------------|-----------------------------------------------|
| `POST`   | `/auth/register`          | Pública                    | Registra uma nova conta de jogador            |
| `POST`   | `/auth/login`             | Pública                    | Autentica e retorna JWT                       |
| `GET`    | `/actuator/health`        | Pública                    | Health check do servidor                      |
| `GET`    | `/posts/**`               | Pública                    | Leitura de posts, eventos e notícias          |
| `POST`   | `/user/me`                | `Bearer token` obrigatório | Retorna os dados do perfil logado             |
| `POST`   | `/user/setBirthday`       | `Bearer token` obrigatório | Salva a data de nascimento do usuário logado  |
| `POST`   | `/user/changePassword`    | `Bearer token` obrigatório | Altera a senha do usuário autenticado         |
| `POST`   | `/gamer/account`          | `Bearer token` obrigatório | Retorna contas de jogo e personagens vinculados |
| `POST`   | `/gamer/create`           | `Bearer token` obrigatório | Cria uma nova conta de jogo vinculada         |
| Qualquer | Demais rotas              | `Bearer token` obrigatório | Rotas protegidas exigem JWT válido            |
