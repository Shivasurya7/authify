# 🔐 Authify

A full-stack authentication system built with **Spring Boot 3** and **React 19**, featuring JWT-based stateless authentication, two-factor authentication (2FA/TOTP), email verification, password reset, role-based access control, and a dark/light theme toggle.

---

## 📑 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Architecture Overview](#-architecture-overview)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [Configuration](#-configuration)
- [API Reference](#-api-reference)
- [Authentication Flow](#-authentication-flow)
- [Two-Factor Authentication (2FA)](#-two-factor-authentication-2fa)
- [Email Verification](#-email-verification)
- [Password Reset](#-password-reset)
- [Token Management](#-token-management)
- [Security](#-security)
- [Database](#-database)
- [Frontend Pages & Components](#-frontend-pages--components)
- [Error Handling](#-error-handling)
- [Build & Deployment](#-build--deployment)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

| Feature | Description |
|---|---|
| **User Registration** | Sign up with first name, last name, email, and password (min 8 chars) with server-side validation |
| **User Login** | Email + password authentication with optional "Remember Me" for persistent sessions |
| **JWT Authentication** | Stateless authentication using HttpOnly cookies for access and refresh tokens |
| **Refresh Tokens** | Automatic silent token refresh when access tokens expire (via Axios interceptor) |
| **Email Verification** | Sends a verification email on registration with a 24-hour expiry token |
| **Forgot Password** | Request a password reset link via email (1-hour expiry) |
| **Reset Password** | Set a new password using a one-time-use reset token |
| **Two-Factor Authentication** | TOTP-based 2FA using authenticator apps (Google Authenticator, Authy, etc.) |
| **Role-Based Access Control** | `ROLE_USER` and `ROLE_ADMIN` roles with seed data initialization |
| **Dark / Light Theme** | Persistent theme toggle stored in `localStorage` |
| **Protected Routes** | Frontend route guards that redirect unauthenticated users to login |
| **Global Error Handling** | Centralized exception handling with structured JSON error responses |
| **CORS Configuration** | Configured to allow frontend origin with credentials |
| **Input Validation** | Bean Validation on backend DTOs + client-side validation on frontend forms |

---

## 🛠 Tech Stack

### Backend

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Programming language |
| Spring Boot | 3.5.0 | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | Database ORM & repositories |
| Spring Boot Mail | 3.x | Email sending (SMTP) |
| H2 Database | 2.x | In-memory SQL database (dev) |
| JJWT | 0.12.6 | JWT token generation & validation |
| TOTP (samstevens) | 1.7.1 | Time-based one-time password for 2FA |
| Lombok | latest | Boilerplate code reduction |
| Maven | 3.x | Build tool (via Maven Wrapper) |

### Frontend

| Technology | Version | Purpose |
|---|---|---|
| React | 19.2.4 | UI library |
| React Router DOM | 7.13.0 | Client-side routing |
| Axios | 1.13.5 | HTTP client |
| React Scripts | 5.0.1 | Build toolchain (Create React App) |
| CSS Variables | — | Theming (dark/light mode) |

---

## 📁 Project Structure

```
authify/
├── README.md
├── backend/                          # Spring Boot backend
│   ├── pom.xml                       # Maven project configuration
│   ├── mvnw / mvnw.cmd              # Maven Wrapper scripts
│   └── src/
│       ├── main/
│       │   ├── java/com/authify/backend/
│       │   │   ├── BackendApplication.java          # Main entry point
│       │   │   ├── config/
│       │   │   │   └── DataInitializer.java         # Seeds ROLE_USER & ROLE_ADMIN on startup
│       │   │   ├── controller/
│       │   │   │   └── AuthController.java          # REST endpoints for auth operations
│       │   │   ├── dto/
│       │   │   │   ├── AuthResponse.java            # Login/refresh/me response
│       │   │   │   ├── LoginRequest.java            # Login payload (email, password, rememberMe, tfaCode)
│       │   │   │   ├── RegisterRequest.java         # Registration payload
│       │   │   │   ├── ForgotPasswordRequest.java   # Forgot password payload
│       │   │   │   ├── ResetPasswordRequest.java    # Reset password payload
│       │   │   │   ├── MessageResponse.java         # Simple message response
│       │   │   │   ├── TfaSetupResponse.java        # 2FA setup (secret + QR URI)
│       │   │   │   └── VerifyTfaRequest.java        # 2FA verification payload
│       │   │   ├── exception/
│       │   │   │   ├── GlobalExceptionHandler.java  # Centralized @RestControllerAdvice
│       │   │   │   ├── InvalidTokenException.java   # Invalid token error
│       │   │   │   ├── TokenExpiredException.java   # Expired token error
│       │   │   │   └── UserAlreadyExistsException.java # Duplicate email error
│       │   │   ├── model/
│       │   │   │   ├── User.java                    # User entity (JPA)
│       │   │   │   ├── Role.java                    # Role entity with RoleName enum
│       │   │   │   ├── RefreshToken.java            # Refresh token entity
│       │   │   │   ├── EmailVerificationToken.java  # Email verification token entity
│       │   │   │   └── PasswordResetToken.java      # Password reset token entity
│       │   │   ├── repository/
│       │   │   │   ├── UserRepository.java          # User data access
│       │   │   │   ├── RoleRepository.java          # Role data access
│       │   │   │   ├── RefreshTokenRepository.java  # Refresh token data access
│       │   │   │   ├── EmailVerificationTokenRepository.java
│       │   │   │   └── PasswordResetTokenRepository.java
│       │   │   ├── security/
│       │   │   │   ├── SecurityConfig.java          # Spring Security configuration
│       │   │   │   ├── JwtTokenProvider.java        # JWT generation & validation
│       │   │   │   ├── JwtAuthenticationFilter.java # JWT filter (reads accessToken cookie)
│       │   │   │   ├── CustomUserDetailsService.java# Loads user by email for Spring Security
│       │   │   │   └── AuthEntryPoint.java          # 401 Unauthorized JSON response handler
│       │   │   └── service/
│       │   │       ├── AuthService.java             # Core authentication business logic
│       │   │       ├── TokenService.java            # Token CRUD operations
│       │   │       ├── EmailService.java            # Email sending (verification, reset)
│       │   │       └── TfaService.java              # TOTP 2FA (secret gen, QR code, verify)
│       │   └── resources/
│       │       └── application.properties           # App configuration
│       └── test/
│           └── java/com/authify/backend/
│               └── BackendApplicationTests.java     # Spring Boot test
│
└── frontend/                         # React frontend
    ├── package.json                  # npm dependencies & scripts
    ├── public/
    │   ├── index.html                # HTML template
    │   ├── favicon.ico               # App icon
    │   └── manifest.json             # PWA manifest
    └── src/
        ├── index.js                  # React entry point
        ├── index.css                 # Global styles (light/dark theme CSS variables)
        ├── App.js                    # Root component with routes
        ├── App.css                   # Default CRA styles
        ├── api/
        │   ├── axios.js              # Axios instance with interceptor for token refresh
        │   └── authApi.js            # Auth API method wrappers
        ├── components/
        │   ├── Alert.js              # Reusable alert component (error/success)
        │   ├── ProtectedRoute.js     # Route guard for authenticated users
        │   └── ThemeToggle.js        # Dark/light theme toggle button
        ├── context/
        │   ├── AuthContext.js        # Auth state management (user, login, logout, etc.)
        │   └── ThemeContext.js       # Theme state management (light/dark)
        └── pages/
            ├── LoginPage.js          # Login form with 2FA support
            ├── RegisterPage.js       # Registration form
            ├── ForgotPasswordPage.js # Forgot password form
            ├── ResetPasswordPage.js  # Reset password form (with token from URL)
            ├── VerifyEmailPage.js    # Email verification (auto-verifies from URL token)
            └── DashboardPage.js      # User dashboard (profile, 2FA management)
```

---

## 🏗 Architecture Overview

```
┌───────────────────────────────────────────────────────────────────┐
│                         FRONTEND (React)                          │
│                         Port: 3000                                │
│                                                                   │
│  ┌──────────┐  ┌──────────┐  ┌───────────┐  ┌──────────────────┐ │
│  │  Pages   │  │Components│  │  Context   │  │   API Layer      │ │
│  │          │  │          │  │            │  │                  │ │
│  │ Login    │  │ Alert    │  │ AuthContext│  │ axios.js         │ │
│  │ Register │  │ Protected│  │ ThemeCtx   │  │ (interceptors)   │ │
│  │ Dashboard│  │ Route    │  │            │  │ authApi.js       │ │
│  │ Forgot   │  │ Theme    │  │            │  │ (API wrappers)   │ │
│  │ Reset    │  │ Toggle   │  │            │  │                  │ │
│  │ Verify   │  │          │  │            │  │                  │ │
│  └──────────┘  └──────────┘  └───────────┘  └────────┬─────────┘ │
│                                                       │           │
└───────────────────────────────────────────────────────┼───────────┘
                            HTTP (cookies)              │
                                                        │
┌───────────────────────────────────────────────────────┼───────────┐
│                         BACKEND (Spring Boot)         │           │
│                         Port: 8080                    ▼           │
│                                                                   │
│  ┌──────────────┐  ┌─────────────────┐  ┌──────────────────────┐ │
│  │  Controller   │  │    Security      │  │     Services         │ │
│  │               │  │                 │  │                      │ │
│  │ AuthController│  │ SecurityConfig  │  │ AuthService          │ │
│  │               │  │ JwtFilter       │  │ TokenService         │ │
│  │ /api/auth/*   │  │ JwtProvider     │  │ EmailService         │ │
│  │               │  │ AuthEntryPoint  │  │ TfaService           │ │
│  │               │  │ UserDetailsSvc  │  │                      │ │
│  └───────┬───────┘  └────────┬────────┘  └──────────┬───────────┘ │
│          │                   │                       │            │
│          └───────────────────┼───────────────────────┘            │
│                              │                                    │
│                    ┌─────────┴─────────┐                          │
│                    │    Repository      │                          │
│                    │                   │                          │
│                    │ UserRepository    │                          │
│                    │ RoleRepository    │                          │
│                    │ RefreshTokenRepo  │                          │
│                    │ EmailTokenRepo    │                          │
│                    │ PwdResetTokenRepo │                          │
│                    └─────────┬─────────┘                          │
│                              │                                    │
│                    ┌─────────┴─────────┐                          │
│                    │   H2 Database     │                          │
│                    │   (In-Memory)     │                          │
│                    └───────────────────┘                          │
└───────────────────────────────────────────────────────────────────┘
```

---

## 📋 Prerequisites

| Software | Version | Required |
|---|---|---|
| **Java JDK** | 17+ | ✅ |
| **Node.js** | 18+ | ✅ |
| **npm** | 9+ | ✅ |
| **Git** | latest | ✅ |
| **Maven** | 3.x | ❌ (Included via Maven Wrapper) |

---

## 🚀 Getting Started

### Clone the Repository

```bash
git clone https://github.com/Shivasurya7/authify.git
cd authify
```

### Backend Setup

```bash
# Navigate to backend directory
cd backend

# Run the application (Maven Wrapper downloads Maven automatically)
./mvnw spring-boot:run          # Linux/macOS
mvnw.cmd spring-boot:run        # Windows
```

The backend starts on **http://localhost:8080**.

> **Note:** The H2 in-memory database is auto-configured. No external database setup is needed.

### Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

The frontend starts on **http://localhost:3000** and proxies API requests to `http://localhost:8080` (configured via `"proxy"` in `package.json`).

### Access the Application

1. Open **http://localhost:3000** in your browser
2. Register a new account
3. Check the backend console logs for the email verification link (if SMTP is not configured)
4. Log in to access the dashboard

### Access the H2 Database Console

- URL: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:authifydb`
- Username: `sa`
- Password: *(leave empty)*

---

## ⚙ Configuration

All backend configuration is in `backend/src/main/resources/application.properties`:

### Server

| Property | Default | Description |
|---|---|---|
| `server.port` | `8080` | Backend server port |

### Database (H2)

| Property | Default | Description |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:mem:authifydb` | In-memory H2 database URL |
| `spring.datasource.username` | `sa` | Database username |
| `spring.datasource.password` | *(empty)* | Database password |
| `spring.h2.console.enabled` | `true` | Enable H2 web console |
| `spring.h2.console.path` | `/h2-console` | H2 console URL path |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | Schema management (recreates on restart) |

### JWT

| Property | Default | Description |
|---|---|---|
| `app.jwt.secret` | Base64 encoded key | HMAC signing key for JWT tokens |
| `app.jwt.access-token-expiry` | `900000` (15 min) | Access token expiry in milliseconds |
| `app.jwt.refresh-token-expiry` | `604800000` (7 days) | Refresh token expiry in milliseconds |

### Email (SMTP)

| Property | Default | Description |
|---|---|---|
| `spring.mail.host` | `smtp.gmail.com` | SMTP server host |
| `spring.mail.port` | `587` | SMTP server port |
| `spring.mail.username` | `your-email@gmail.com` | SMTP username (replace with real email) |
| `spring.mail.password` | `your-app-password` | SMTP password / app password |
| `spring.mail.properties.mail.smtp.auth` | `true` | Enable SMTP authentication |
| `spring.mail.properties.mail.smtp.starttls.enable` | `true` | Enable STARTTLS encryption |

> **Development Tip:** If SMTP is not configured, the `EmailService` gracefully logs the email content to the console instead of failing. Check the backend logs for verification/reset links.

### Application

| Property | Default | Description |
|---|---|---|
| `app.base-url` | `http://localhost:8080` | Backend base URL |
| `app.frontend-url` | `http://localhost:3000` | Frontend URL (used for CORS & email links) |
| `app.name` | `Authify` | Application name (used in emails) |

---

## 📡 API Reference

All endpoints are prefixed with `/api/auth`. Public endpoints do not require authentication.

### Public Endpoints

#### Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123",
  "confirmPassword": "password123"
}
```

**Response (200):**
```json
{
  "message": "Registration successful! Please check your email to verify your account."
}
```

**Validation Rules:**
- `firstName` — required
- `lastName` — required
- `email` — required, valid email format, must be unique
- `password` — required, minimum 8 characters
- `confirmPassword` — required, must match `password`

---

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123",
  "rememberMe": true,
  "tfaCode": "123456"    // only if 2FA is enabled
}
```

**Response (200):**
```json
{
  "message": "Login successful",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["ROLE_USER"],
  "tfaEnabled": false,
  "tfaRequired": false
}
```

**Cookies Set:**
- `accessToken` — HttpOnly, 15-minute expiry, path `/`
- `refreshToken` — HttpOnly, 7-day expiry, path `/api/auth/refresh` (only if `rememberMe` is `true`)

**If 2FA is enabled but no code provided:**
```json
{
  "message": "2FA code required",
  "email": "john@example.com",
  "tfaEnabled": true,
  "tfaRequired": true
}
```

---

#### Refresh Token

```http
POST /api/auth/refresh
```

Reads `refreshToken` from cookie. Returns a new `accessToken` cookie.

**Response (200):**
```json
{
  "message": "Token refreshed",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["ROLE_USER"],
  "tfaEnabled": false,
  "tfaRequired": false
}
```

---

#### Verify Email

```http
GET /api/auth/verify-email?token=<uuid>
```

**Response (200):**
```json
{
  "message": "Email verified successfully!"
}
```

---

#### Forgot Password

```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "john@example.com"
}
```

**Response (200):**
```json
{
  "message": "If an account exists with that email, a password reset link has been sent."
}
```

> **Security:** Always returns success to prevent email enumeration attacks.

---

#### Reset Password

```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "<uuid>",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

**Response (200):**
```json
{
  "message": "Password reset successfully!"
}
```

---

### Authenticated Endpoints

> Requires a valid `accessToken` cookie.

#### Get Current User

```http
GET /api/auth/me
```

**Response (200):**
```json
{
  "message": "User info",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["ROLE_USER"],
  "tfaEnabled": false,
  "tfaRequired": false
}
```

---

#### Logout

```http
POST /api/auth/logout
```

Clears `accessToken` and `refreshToken` cookies. Deletes refresh tokens from the database.

**Response (200):**
```json
{
  "message": "Logout successful"
}
```

---

#### Enable 2FA

```http
POST /api/auth/tfa/enable
```

**Response (200):**
```json
{
  "secret": "JBSWY3DPEHPK3PXP...",
  "qrCodeUri": "data:image/png;base64,...",
  "message": "Scan the QR code with your authenticator app, then verify with a code"
}
```

---

#### Verify & Activate 2FA

```http
POST /api/auth/tfa/verify
Content-Type: application/json

{
  "code": "123456"
}
```

**Response (200):**
```json
{
  "message": "Two-factor authentication enabled successfully!"
}
```

---

#### Disable 2FA

```http
POST /api/auth/tfa/disable
```

**Response (200):**
```json
{
  "message": "Two-factor authentication disabled"
}
```

---

## 🔄 Authentication Flow

### Login Flow

```
┌─────────┐                     ┌─────────┐                    ┌──────┐
│ Browser  │                     │ Backend │                    │  DB  │
└────┬─────┘                     └────┬────┘                    └──┬───┘
     │  POST /api/auth/login          │                            │
     │  {email, password}             │                            │
     ├───────────────────────────────►│                            │
     │                                │  Validate credentials     │
     │                                ├───────────────────────────►│
     │                                │  ◄─── User found ─────────┤
     │                                │                            │
     │                                │  Generate JWT access token │
     │                                │  (if rememberMe: also     │
     │                                │   generate refresh token)  │
     │                                │                            │
     │  Set-Cookie: accessToken=...   │                            │
     │  Set-Cookie: refreshToken=...  │                            │
     │  ◄─────────────────────────────┤                            │
     │  {message, email, roles, ...}  │                            │
     │                                │                            │
     │  GET /api/auth/me              │                            │
     │  Cookie: accessToken=...       │                            │
     ├───────────────────────────────►│                            │
     │                                │  Validate JWT              │
     │                                │  Load user by email        │
     │  ◄─── {user data} ────────────┤                            │
     │                                │                            │
```

### Token Refresh Flow (Automatic via Axios Interceptor)

```
┌─────────┐                     ┌─────────┐
│ Browser  │                     │ Backend │
└────┬─────┘                     └────┬────┘
     │  GET /api/auth/me              │
     │  Cookie: accessToken=EXPIRED   │
     ├───────────────────────────────►│
     │  ◄─── 401 Unauthorized ───────┤
     │                                │
     │  POST /api/auth/refresh        │   (Axios interceptor
     │  Cookie: refreshToken=...      │    auto-retries)
     ├───────────────────────────────►│
     │  Set-Cookie: accessToken=NEW   │
     │  ◄─── 200 OK ─────────────────┤
     │                                │
     │  GET /api/auth/me (retry)      │
     │  Cookie: accessToken=NEW       │
     ├───────────────────────────────►│
     │  ◄─── {user data} ────────────┤
```

---

## 🔑 Two-Factor Authentication (2FA)

Authify supports **TOTP (Time-Based One-Time Password)** using the [samstevens/totp](https://github.com/samstevens/totp-java) library.

### Setup Flow

1. **User clicks "Enable 2FA"** on the dashboard
2. Backend generates a **32-character secret** and a **QR code** (PNG data URI)
3. User scans the QR code with an authenticator app (Google Authenticator, Authy, Microsoft Authenticator, etc.)
4. User enters the **6-digit verification code** from the app
5. Backend verifies the code against the secret → if valid, 2FA is activated

### Login with 2FA

1. User submits email + password
2. Backend detects `tfaEnabled = true` → returns `{ tfaRequired: true }`
3. Frontend shows a 2FA code input
4. User enters the 6-digit code → backend verifies → login completes

### Configuration

| Setting | Value |
|---|---|
| Algorithm | SHA-1 |
| Digits | 6 |
| Period | 30 seconds |
| Secret Length | 32 characters |

---

## ✉ Email Verification

1. On registration, a `UUID` token is generated and stored in the `email_verification_tokens` table
2. An email is sent with a link: `{frontend-url}/verify-email?token={uuid}`
3. Token expires in **24 hours**
4. When the user clicks the link, the `VerifyEmailPage` component calls `GET /api/auth/verify-email?token=...`
5. Backend validates the token, marks `emailVerified = true` on the user

> **Dev Mode:** If SMTP is not configured, the email content (including the verification link) is logged to the backend console.

---

## 🔒 Password Reset

1. User submits email on the **Forgot Password** page
2. Backend generates a `UUID` reset token (1-hour expiry) and sends an email with the link: `{frontend-url}/reset-password?token={uuid}`
3. User clicks the link → **Reset Password** page loads
4. User enters a new password (min 8 chars) + confirm password
5. Backend validates the token (not expired, not already used), updates the password, and marks the token as `used`

> **Security:** The forgot password endpoint always returns a success message regardless of whether the email exists, preventing email enumeration.

---

## 🎫 Token Management

### Access Token (JWT)

| Property | Value |
|---|---|
| **Type** | JWT (JSON Web Token) |
| **Storage** | HttpOnly cookie named `accessToken` |
| **Expiry** | 15 minutes (900,000 ms) |
| **Signing** | HMAC with Base64-decoded secret key |
| **Subject** | User's email address |
| **Path** | `/` (sent with every request) |

### Refresh Token

| Property | Value |
|---|---|
| **Type** | UUID string |
| **Storage** | HttpOnly cookie named `refreshToken` + stored in DB |
| **Expiry** | 7 days (604,800,000 ms) |
| **Path** | `/api/auth/refresh` (only sent to refresh endpoint) |
| **Condition** | Only created if `rememberMe = true` during login |

### Email Verification Token

| Property | Value |
|---|---|
| **Type** | UUID string |
| **Storage** | `email_verification_tokens` table |
| **Expiry** | 24 hours |

### Password Reset Token

| Property | Value |
|---|---|
| **Type** | UUID string |
| **Storage** | `password_reset_tokens` table |
| **Expiry** | 1 hour |
| **One-time use** | Yes (`used` flag prevents reuse) |

---

## 🛡 Security

### Spring Security Configuration

- **Session Policy:** `STATELESS` — no server-side sessions
- **CSRF:** Disabled (safe for stateless JWT + cookie auth)
- **CORS:** Allows requests from `app.frontend-url` with credentials
- **Password Encoding:** BCrypt
- **Public Endpoints:** `/api/auth/**`, `/h2-console/**`
- **Protected Endpoints:** Everything else requires a valid JWT

### Cookie Security

| Cookie | HttpOnly | Secure | Path | Max Age |
|---|---|---|---|---|
| `accessToken` | ✅ | ❌ (set `true` in prod) | `/` | 15 min |
| `refreshToken` | ✅ | ❌ (set `true` in prod) | `/api/auth/refresh` | 7 days |

> ⚠️ **Production Note:** Set `cookie.setSecure(true)` in `AuthService.java` when using HTTPS.

### JWT Filter

The `JwtAuthenticationFilter` is a `OncePerRequestFilter` that:
1. Extracts the JWT from the `accessToken` cookie
2. Validates the token signature and expiry
3. Extracts the email from the token's `subject` claim
4. Loads the `UserDetails` from the database
5. Sets the `SecurityContext` for the current request

### Unauthorized Handler

`AuthEntryPoint` returns a structured JSON response for unauthenticated requests:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "You need to login to access this resource",
  "path": "/api/protected-endpoint"
}
```

---

## 🗄 Database

### Engine

**H2 In-Memory Database** — data is lost on application restart (`ddl-auto: create-drop`).

### Entity-Relationship Diagram

```
┌──────────────────────┐       ┌───────────────────────┐
│        users          │       │        roles           │
├──────────────────────┤       ├───────────────────────┤
│ id (PK)              │       │ id (PK)               │
│ first_name           │       │ name (ENUM, UNIQUE)   │
│ last_name            │  M:N  │   - ROLE_USER         │
│ email (UNIQUE)       │◄─────►│   - ROLE_ADMIN        │
│ password (BCrypt)    │       └───────────────────────┘
│ email_verified       │           via user_roles
│ tfa_enabled          │           (user_id, role_id)
│ tfa_secret           │
│ enabled              │
│ created_at           │
│ updated_at           │
└──────────┬───────────┘
           │ 1:N
           │
    ┌──────┴──────────────────────────────────────┐
    │                    │                         │
    ▼                    ▼                         ▼
┌────────────┐  ┌─────────────────┐  ┌────────────────────────┐
│refresh_     │  │password_reset_  │  │email_verification_     │
│tokens       │  │tokens           │  │tokens                  │
├────────────┤  ├─────────────────┤  ├────────────────────────┤
│ id (PK)    │  │ id (PK)         │  │ id (PK)                │
│ token (UQ) │  │ token (UQ)      │  │ token (UQ)             │
│ user_id(FK)│  │ user_id (FK)    │  │ user_id (FK)           │
│ expiry_date│  │ expiry_date     │  │ expiry_date            │
└────────────┘  │ used            │  └────────────────────────┘
                └─────────────────┘
```

### Seed Data

On startup, `DataInitializer.java` (implements `CommandLineRunner`) creates two roles if they don't exist:
- `ROLE_USER`
- `ROLE_ADMIN`

---

## 🖥 Frontend Pages & Components

### Pages

| Page | Route | Auth Required | Description |
|---|---|---|---|
| `LoginPage` | `/login` | ❌ | Email + password login with 2FA support and "Remember Me" |
| `RegisterPage` | `/register` | ❌ | User registration with validation |
| `ForgotPasswordPage` | `/forgot-password` | ❌ | Request password reset email |
| `ResetPasswordPage` | `/reset-password?token=...` | ❌ | Set new password using reset token |
| `VerifyEmailPage` | `/verify-email?token=...` | ❌ | Auto-verifies email on page load |
| `DashboardPage` | `/dashboard` | ✅ | User profile, 2FA setup/management |

### Components

| Component | Description |
|---|---|
| `ProtectedRoute` | Route guard — redirects to `/login` if not authenticated |
| `Alert` | Reusable alert banner for error/success messages |
| `ThemeToggle` | Button to switch between light and dark themes |

### Context Providers

| Context | State | Description |
|---|---|---|
| `AuthContext` | `user`, `loading`, `isAuthenticated` | Manages authentication state; provides `login`, `register`, `logout`, `refreshUser` |
| `ThemeContext` | `theme` | Manages dark/light theme; persists to `localStorage` |

### API Layer

| File | Description |
|---|---|
| `axios.js` | Creates Axios instance with `baseURL: /api`, `withCredentials: true`, and a **response interceptor** that auto-retries failed 401 requests by calling `/auth/refresh` first |
| `authApi.js` | Exports named API methods: `login`, `register`, `logout`, `getCurrentUser`, `refreshToken`, `forgotPassword`, `resetPassword`, `verifyEmail`, `enableTfa`, `verifyTfa`, `disableTfa` |

### Theming

The app supports **light** and **dark** themes using CSS custom properties (variables) defined in `index.css`:

- Theme is toggled via the `ThemeToggle` component
- Persisted in `localStorage` under the key `theme`
- Applied by setting `data-theme` attribute on `<html>` element
- CSS variables change based on `[data-theme="dark"]` selector

### Routing

Routing is handled by **React Router DOM v7**:

- Authenticated users visiting `/login` or `/register` are redirected to `/dashboard`
- Unauthenticated users visiting `/dashboard` are redirected to `/login`
- The catch-all route `*` redirects based on auth state

---

## ❌ Error Handling

### Backend — Global Exception Handler

The `GlobalExceptionHandler` (`@RestControllerAdvice`) catches all exceptions and returns structured JSON:

| Exception | HTTP Status | Example Message |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | Field-level validation errors |
| `IllegalArgumentException` | 400 | "Passwords do not match", "Invalid 2FA code" |
| `InvalidTokenException` | 400 | "Invalid verification token" |
| `TokenExpiredException` | 400 | "Verification token has expired" |
| `BadCredentialsException` | 401 | "Invalid email or password" |
| `DisabledException` | 403 | "Account is disabled" |
| `UserAlreadyExistsException` | 409 | "Email is already registered" |
| `Exception` (generic) | 500 | "An unexpected error occurred" |

**Error Response Format:**
```json
{
  "timestamp": "2026-02-22T10:30:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid verification token"
}
```

**Validation Error Format:**
```json
{
  "timestamp": "2026-02-22T10:30:00.000",
  "status": 400,
  "error": "Validation Error",
  "errors": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  }
}
```

### Frontend Error Handling

- Each page has local `error` and `success` states
- Errors from API responses are extracted: `err.response?.data?.message`
- Displayed using the `Alert` component
- The Axios interceptor silently handles 401 errors by attempting a token refresh before surfacing the error

---

## 📦 Build & Deployment

### Backend

| Command | Description |
|---|---|
| `./mvnw spring-boot:run` | Run in development mode with hot-reload (devtools) |
| `./mvnw clean package` | Clean `target/` and build a production JAR |
| `./mvnw clean package -DskipTests` | Build without running tests |
| `./mvnw clean` | Remove `target/` directory |
| `./mvnw test` | Run unit and integration tests |
| `java -jar target/backend-0.0.1-SNAPSHOT.jar` | Run the production JAR |

### Frontend

| Command | Description |
|---|---|
| `npm start` | Run in development mode (port 3000, hot-reload) |
| `npm run build` | Create optimized production build in `build/` |
| `npm test` | Run tests with Jest |
| `npm run eject` | Eject CRA config (irreversible) |

### Production Deployment Checklist

- [ ] Replace H2 with a production database (PostgreSQL, MySQL, etc.)
- [ ] Change `spring.jpa.hibernate.ddl-auto` from `create-drop` to `update` or `validate`
- [ ] Set a strong, unique `app.jwt.secret`
- [ ] Configure real SMTP credentials for `spring.mail.*`
- [ ] Set `cookie.setSecure(true)` in `AuthService.java` for HTTPS
- [ ] Update `app.base-url` and `app.frontend-url` to production URLs
- [ ] Set `spring.h2.console.enabled=false`
- [ ] Set `spring.jpa.show-sql=false`
- [ ] Build the React frontend (`npm run build`) and serve via a CDN or reverse proxy
- [ ] Configure HTTPS / TLS
- [ ] Set up environment variables instead of hardcoding secrets in `application.properties`

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit changes: `git commit -m 'Add my feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

## 📄 License

This project is open-source. See the repository for license details.

---

<p align="center">
  Built with ❤️ using Spring Boot & React
</p>
