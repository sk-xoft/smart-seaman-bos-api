# Repository Guidelines

## Project Structure & Module Organization

This is a Java 11 Spring Boot API using Maven. Application code lives under `src/main/java/com/seaman`, with the entry point at `SmartSeamanBosApiApplication.java`. Keep web endpoints in `controller`, business logic in `service`, persistence access in `repository`, request/response DTOs in `model/request` and `model/response`, and shared configuration in `config`. Runtime configuration and platform assets are in `src/main/resources`, including `application.properties`, logging config, Firebase credentials, and mobile association files. Tests belong under `src/test/java` and should mirror the production package structure.

## Build, Test, and Development Commands

Use the Maven wrapper so contributors do not need a global Maven install:

```sh
./mvnw test
```

Runs the Spring Boot test suite.

```sh
./mvnw clean package
```

Compiles, tests, and builds the executable JAR in `target/`.

```sh
./mvnw spring-boot:run
```

Starts the API locally using `src/main/resources/application.properties`.

```sh
docker build -t xoftspace/smart-seaman-bos-api:local .
docker run --name smart-seaman-bos-api -e COMPANY=smart-seaman -e ENV=dev -p 20000:8080 xoftspace/smart-seaman-bos-api:local
```

Builds and runs the containerized service.

## Coding Style & Naming Conventions

Follow the existing Spring style: four-space indentation, package names in lowercase, and PascalCase class names. Name classes by responsibility, such as `AuthController`, `AuthService`, `UserRepository`, `LoginRequest`, and `LoginResponse`. Prefer constructor injection with Lombok `@RequiredArgsConstructor`. Keep controllers thin; validation, persistence orchestration, and business rules should stay in services.

## Testing Guidelines

The project uses `spring-boot-starter-test` with JUnit through Maven. Add tests under `src/test/java/com/seaman/...` using names ending in `Test` or `Tests`. For controller behavior, prefer focused Spring MVC tests where possible; for service logic, use unit tests with mocked repositories. Run `./mvnw test` before submitting changes.

## Commit & Pull Request Guidelines

Git history currently contains only the initial commit, so use clear imperative commit messages such as `Add voucher validation` or `Fix login status check`. Pull requests should include a concise summary, affected endpoints or modules, test results, and linked issue or ticket when available. Include screenshots or request/response examples for API behavior changes.

## Security & Configuration Tips

Do not add new secrets to the repository. Treat `application.properties` and credential files in `src/main/resources` as sensitive configuration; prefer environment variables or deployment secrets for new values. Avoid logging tokens, passwords, personal data, or full request bodies unless explicitly required and masked.
