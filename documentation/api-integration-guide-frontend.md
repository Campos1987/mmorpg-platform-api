# Grankain Platform API — Guia de Integração para o Frontend

> **Para:** Time de Frontend (Next.js / TypeScript)
> **Módulo:** Autenticação e Gestão de Contas
> **Versão da API:** `0.0.1-SNAPSHOT`
> **Última atualização:** 2026-05-23

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
    const isProtectedRoute = request.nextUrl.pathname.startsWith('/dashboard');

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
| `409 Conflict`              | `CONFLICT`              | E-mail ou username já cadastrado                                           |
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
  "birthday": "1990-07-15",
  "email": "joao.silva@email.com",
  "password": "Senha@Segura1",
  "recaptchaToken": "03AGdBq..."
}
```

| Campo            | Tipo     | Obrigatório | Restrições                                                                         |
|------------------|----------|-------------|------------------------------------------------------------------------------------|
| `user`           | `string` | Sim         | Mínimo 5, máximo 12 caracteres; apenas letras e números `[a-zA-Z0-9]`; sem espaços |
| `name`           | `string` | Sim         | Entre 3 e 15 caracteres; apenas letras (incluindo acentuadas)                      |
| `lastname`       | `string` | Sim         | Entre 3 e 15 caracteres; apenas letras (incluindo acentuadas)                      |
| `birthday`       | `string` | Sim         | Formato ISO: `YYYY-MM-DD` (ex: `"1990-07-15"`)                                     |
| `email`          | `string` | Sim         | E-mail válido; máximo 100 caracteres                                               |
| `password`       | `string` | Sim         | Validação de complexidade via `@ValidPassword` (regras definidas no backend)       |
| `recaptchaToken` | `string` | Sim         | Token obtido pelo provider de CAPTCHA                                              |

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

```json
{
  "timestamp": "2026-05-23T03:05:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "user: Username é obrigatório",
  "trace": null,
  "path": "/auth/register"
}
```

> Em `prod`, `message` será `null`. Use o campo `error` + HTTP status para exibir a mensagem correta ao usuário.

---

**`409 Conflict` — E-mail ou username já cadastrado:**

```json
{
  "timestamp": "2026-05-23T03:05:10Z",
  "status": 409,
  "error": "CONFLICT",
  "message": "Usuário ou e-mail já em uso.",
  "trace": [],
  "path": "/auth/register"
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
    birthday: string; // formato: "YYYY-MM-DD"
    email: string;
    password: string;
    recaptchaToken: string;
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
Em caso de sucesso, retorna um JWT Bearer Token com validade de **1 hora**.

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
  "userName": "Joao Silva"
}
```

| Campo      | Tipo                    | Descrição       |
|------------|-------------------------|-----------------|
| `userName` | `string` (ISO 8601 UTC) | Nome do Usuario |

**Estrutura do JWT decodificado (payload):**

```json
{
  "iss": "mmorpg-l2-api",
  "sub": "GankMaster",
  "scope": "ROLE_USER",
  "iat": 1748062600,
  "exp": 1748066200
}
```

| Claim   | Descrição                                                          |
|---------|--------------------------------------------------------------------|
| `iss`   | Issuer (emissor): sempre `"mmorpg-l2-api"`                         |
| `sub`   | Subject: username do jogador autenticado                           |
| `scope` | Role do usuário: `"ROLE_USER"`, `"ROLE_ADM"` ou `"ROLE_MODERATOR"` |
| `iat`   | Issued At: timestamp de emissão (Unix)                             |
| `exp`   | Expiration: `iat + 3600`. Token expira após **1 hora**             |

---

**`400 Bad Request` — Campo ausente ou inválido:**

```json
{
  "timestamp": "2026-05-23T03:11:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "user: Username é obrigatório",
  "trace": null,
  "path": "/auth/login"
}
```

---

**`401 Unauthorized` — Credenciais inválidas, conta suspensa ou IP bloqueado:**

```json
{
  "timestamp": "2026-05-23T03:11:30Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid User",
  "trace": [],
  "path": "/auth/login"
}
```

> **Atenção de UX:** Em produção, `message` será `null`.
> O backend **intencionalmente não diferencia** "usuário não encontrado" de "senha incorreta"
> para evitar enumeração de usuários (user enumeration attack).
> O Frontend deve exibir uma mensagem genérica única, como: *"Usuário ou senha inválidos."*

**Cenários que retornam `401 Unauthorized`:**

| Cenário                                    | `message` em DEV                      |
|--------------------------------------------|---------------------------------------|
| Usuário não existe no banco                | `"Invalid User"`                      |
| Senha incorreta                            | `"Invalid User"`                      |
| Conta com status `SUSPENDED` ou `BANNED`   | `"Account user suspended or blocked"` |
| IP bloqueado (7 ou mais tentativas falhas) | `"Account user suspended or blocked"` |

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
    /** Timestamp ISO 8601 do momento do login */
    loginTime: string;
    /** JWT Bearer Token. Armazenar em cookie HttpOnly, nunca em localStorage */
    claims: string;
}

/** Payload decodificado do JWT */
export interface JwtPayload {
    iss: string;      // "mmorpg-l2-api"
    sub: string;      // username do jogador
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
): Promise<{ data?: Pick<LoginResponse, 'loginTime'>; error?: ApiError }> {
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

    const {claims, loginTime} = json as LoginResponse;

    // Salva o JWT em cookie HttpOnly — inacessível ao JavaScript do browser
    cookies().set('auth_token', claims, {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'lax',
        maxAge: 3600,
        path: '/',
    });

    // Não retorna o token bruto para o client component
    return {data: {loginTime}};
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

| Método   | Endpoint           | Autenticação               | Descrição                            |
|----------|--------------------|----------------------------|--------------------------------------|
| `POST`   | `/auth/register`   | Pública                    | Registra uma nova conta de jogador   |
| `POST`   | `/auth/login`      | Pública                    | Autentica e retorna JWT              |
| `GET`    | `/actuator/health` | Pública                    | Health check do servidor             |
| `GET`    | `/posts/**`        | Pública                    | Leitura de posts, eventos e notícias |
| Qualquer | Demais rotas       | `Bearer token` obrigatório | Rotas protegidas exigem JWT válido   |
