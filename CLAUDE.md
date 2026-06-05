# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```sh
./mvnw test                          # Run all tests
./mvnw -Dtest=ClassName test         # Run a single test class
./mvnw clean package                 # Build executable JAR in target/
./mvnw spring-boot:run               # Run locally on port 8080
```

Docker:
```sh
docker build -t xoftspace/smart-seaman-bos-api:local .
docker run --name smart-seaman-bos-api -e COMPANY=smart-seaman -e ENV=dev -p 20000:8080 xoftspace/smart-seaman-bos-api:local
```

Swagger UI is available at `/swagger-ui/index.html` and API docs at `/smart-seaman-swagger`.

## Architecture

Spring Boot 2.6 / Java 11 BOS (Back Office System) REST API for the Smart Seaman platform. All routes are prefixed `/v1` (defined in `Routes.java`). The stack is stateless JWT auth over MySQL via `NamedParameterJdbcTemplate` (no JPA/ORM).

### Request Lifecycle

Every `/v1/**` request passes through a three-layer interceptor chain before reaching the controller:

1. **`TokenFilter`** (Spring Security filter) — extracts the Bearer JWT from `Authorization` header and sets the `SecurityContext`. Stateless; does not validate session.
2. **`APIInterceptor`** (order 1) — validates mandatory headers (`Language`, `device-model`, `correlation-id`), assigns a trace UUID, records request start time, and captures client IP.
3. **`AuthInterceptor`** (order 2) — validates the JWT, looks up the session from DB, enforces single-session login (checks `is_online = YES` and token match), then sets `sessionObject` and `userObject` as request attributes for downstream use in controllers.

Public endpoints (login, register, master data, `/actuator/**`, Swagger) are excluded from `AuthInterceptor` and some from `TokenFilter`.

### Layer Responsibilities

- **`controller/`** — thin REST controllers extending `BaseController`. Read `sessionObject`/`userObject` from request attributes when needed. Exception handling is centralized in `controller/advice/ExceptionAdvice.java`.
- **`service/`** — all business logic lives here. Services are injected via `@RequiredArgsConstructor`.
- **`repository/`** — raw SQL via `NamedParameterJdbcTemplate`. All repositories extend `CommonRepository` which provides the shared `template` bean. No JPA.
- **`model/request/` and `model/response/`** — DTOs. Request models validated with `@Valid`; `StringOnlyDeserializer` enforces string-only fields.
- **`entity/`** — result-mapping POJOs for JDBC queries (not JPA entities).
- **`config/`** — Spring configuration: `DataSourceSmartSeaman` (custom datasource + template bean), `SecurityConfiguration` (Spring Security + CORS), `WebMvcConfig` (interceptor registration), `GoogleAuthConfig` (Firebase Admin SDK — `GoogleCredentials`, `FirebaseApp`, `FirebaseMessaging`), `ObjectStorageConfig` (AWS S3-compatible), `CacheConfig` (Caffeine cache), `CryptographyConfig`.
- **`event/`** — async Spring application events. `EventHandler` handles `InsertDbNotifactionEvent` (DB insert) and `FcmNotiEvent` (push notification) asynchronously via `@Async`.
- **`push/noti/`** — FCM push notification sender using Firebase Admin SDK.
- **`constant/`** — `Routes`, `AppStatus` (error codes), `AppSys` (header names, language constants), `BusinessConstant`, `PermissionCode`.
- **`exception/`** — `BusinessException` and `CommonException` carry a `code` string that maps to human-readable messages via `MessageCodeService` (Caffeine-cached, supports TH/EN).

### Key Configuration

All required environment variables — no defaults are provided, so the app will fail to start if any are missing:

| Variable | Purpose | Notes |
|---|---|---|
| `DB_URL` | MySQL JDBC URL | e.g. `jdbc:mysql://host:3306/smartseaman?autoreconnect=true` |
| `DB_USERNAME` | DB username | |
| `DB_PASSWORD` | DB password | |
| `JWT_SECRET` | JWT signing secret | Must be **standard Base64** (no `-` or `_`). Generate: `openssl rand -base64 32` |
| `ENCRYPT_KEY` | Certificate encryption key | Must be **standard Base64**. Generate: `openssl rand -base64 32` |
| `FCM_CREDENTIAL_FILE` | Path to Firebase service account JSON | Classpath name or absolute path. e.g. `firebase-service-account.json` |
| `DO_SPACES_KEY` | DigitalOcean Spaces access key | S3-compatible object storage |
| `DO_SPACES_SECRET` | DigitalOcean Spaces secret key | S3-compatible object storage |
| `MAIL_PASSWORD` | Gmail SMTP password | For `info.smartseaman` account |

The following are hardcoded in `application.properties` (not overridable via env var):
- `object.store.endpoint` — DigitalOcean Spaces endpoint URL
- `object.store.region` — `Singapore`
- `object.store.bucket` — `smart-seaman-bucket`

Firebase service account JSON must be placed at the path configured by `FCM_CREDENTIAL_FILE`. The file `firebase-service-account.example.json` shows the expected structure.

For local development, copy `.env.example` to `.env` and fill in the values — `spring-dotenv` will load it automatically.

### Coding Conventions

- Constructor injection via Lombok `@RequiredArgsConstructor` throughout.
- Four-space indentation, PascalCase class names, lowercase package names.
- Controllers stay thin; validation and business rules go in services.
- Error codes (e.g. `MA00005`, `WA00007`) are string keys looked up in the `message_code` DB table. New error codes must be inserted there.
- The `Language` request header drives TH/EN response messages; hardcoded to `"TH"` in `TokenFilter` (noted as a known simplification).
