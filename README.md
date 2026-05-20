# SmartResidential Backend


Spring Boot backend for the SmartResidential multi-tenant SaaS system.


The backend provides REST APIs for tenants, authentication, users, apartments, issues, technicians, maintenance work orders, attachments, notifications, dashboard summaries, caching, and AI-assisted issue triage.


## Tech Stack


- Java 17
- Spring Boot 3.5
- Spring Security with JWT
- PostgreSQL
- Hibernate/JPA
- Flyway
- Redis cache
- Swagger/OpenAPI
- Ollama for backend-side AI issue categorization
- Schema-based multi-tenancy


## Requirements


- Java 17
- Maven wrapper included in the repo
- PostgreSQL
- Redis
- Optional: Docker Desktop
- Optional: Ollama running locally


## Local Setup


Clone the repository and open the backend folder:


```powershell
cd C:\Users\Admin\Desktop\SmartResidential_Backend
```


Create a local `.env` file in the project root. This file is intentionally ignored by Git.


Example for local PostgreSQL on port `5432`:


```properties
DB_URL=jdbc:postgresql://localhost:5432/smart_residential
DB_USERNAME=postgres
DB_PASSWORD=12345


REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=


JWT_SECRET=dev_only_change_me_dev_only_change_me_123456


MAIL_USERNAME=
MAIL_PASSWORD=


OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=llama3.1
OLLAMA_CONNECT_TIMEOUT_MS=3000
OLLAMA_READ_TIMEOUT_MS=8000


ATTACHMENTS_STORAGE_DIR=uploads/attachments
```


If you use the provided Docker PostgreSQL service, use port `5433` instead:


```properties
DB_URL=jdbc:postgresql://localhost:5433/smart_residential
```


## Running Infrastructure With Docker


The included `docker-compose.yml` starts PostgreSQL and Redis:


```powershell
docker compose up -d
```


Docker ports:


- PostgreSQL: `localhost:5433`
- Redis: `localhost:6379`


The app default currently points to PostgreSQL on `localhost:5432`, so set `DB_URL` in `.env` if using Docker.


## Running The Backend


Run from the project root:


```powershell
.\mvnw.cmd spring-boot:run
```


Or run the `BackendApplication` class from IntelliJ.


Recommended IntelliJ setup:


- Project SDK: Java 17
- Working directory: project root
- Environment file: `.env`, or rely on `spring.config.import=optional:file:.env[.properties]`


## Testing


Run all tests:


```powershell
.\mvnw.cmd clean test
```


Use `clean` when migrations change, because old copied migration files can remain under `target/classes`.


## API Documentation


Swagger UI is available in local development:


```text
http://localhost:8080/swagger-ui.html
```


OpenAPI docs:


```text
http://localhost:8080/v3/api-docs
```


Swagger is disabled in the production profile through `application-prod.properties`.


## Authentication And Tenant Headers


Most protected endpoints require:


```http
Authorization: Bearer <jwt>
X-Tenant-Identifier: <tenant-identifier>
```


The JWT contains tenant information. The backend validates that `X-Tenant-Identifier` matches the tenant stored in the JWT.


## Multi-Tenancy


The project uses schema-based tenant isolation:


- Public schema stores tenant metadata.
- Each tenant has its own PostgreSQL schema.
- Hibernate resolves the active schema from `TenantContext`.
- Tenant migrations are stored in:


```text
src/main/resources/db/migration/tenant
```


Public migrations are stored in:


```text
src/main/resources/db/migration/public
```


Tenant migrations are applied by `TenantMigrationRunner` for active tenants on startup and by `TenantProvisioningService` when a tenant is created.


## Flyway Rules


Do not use:


```properties
spring.jpa.hibernate.ddl-auto=update
```


Schema changes must be added as Flyway migrations.


Important rules:


- Never edit a migration that has already been applied/shared.
- Add a new versioned migration instead.
- Keep migration versions unique.
- Run `.\mvnw.cmd clean test` after adding or renaming migrations.
- Tenant schema changes belong under `db/migration/tenant`.
- Public/shared metadata changes belong under `db/migration/public`.


## AI Issue Triage


AI categorization is backend-side only. The frontend does not call Ollama directly.


Default config:


```properties
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=llama3.1
```


If Ollama is unavailable or slow, issue creation should still complete safely. The issue remains open/unassigned unless categorization and assignment complete successfully.


## Attachments


Attachments support async processing fields such as:


- `fileSize`
- `fileType`
- `originalFilename`
- `storedFilename`
- `thumbnailPath`
- `processingStatus`
- `processingError`
- `processedAt`
- `uploadedById`


Attachment storage defaults to:


```text
uploads/attachments
```


Override with:


```properties
ATTACHMENTS_STORAGE_DIR=uploads/attachments
```


## Common Problems


### PostgreSQL connection refused


If the app tries port `5433` but your PostgreSQL is on `5432`, set:


```properties
DB_URL=jdbc:postgresql://localhost:5432/smart_residential
```


If using Docker Compose, set:


```properties
DB_URL=jdbc:postgresql://localhost:5433/smart_residential
```


### Flyway duplicate migration version


This means two files share the same version, for example two `V3__...sql` files.


Fix by renumbering the newer/unapplied migration. Do not rewrite migrations that teammates may already have applied.


### Missing column errors


Example:


```text
ERROR: column a1_0.file_size does not exist
```


This means the entity expects a column that the tenant schema does not have. Add a tenant Flyway migration to sync the schema, then restart the backend.


## Useful Commands


Start dependencies:


```powershell
docker compose up -d
```


Stop dependencies:


```powershell
docker compose down
```


Run app:


```powershell
.\mvnw.cmd spring-boot:run
```


Run tests:


```powershell
.\mvnw.cmd clean test
```


Build:


```powershell
.\mvnw.cmd clean package
```


## Production Notes


Before production:


- Set a strong `JWT_SECRET`.
- Disable Swagger using the production profile.
- Use real mail credentials from environment variables.
- Do not commit `.env`.
- Keep tenant migrations idempotent where they repair existing tenant drift.
- Confirm Redis and PostgreSQL are reachable from the backend environment.
- Keep Ollama configuration backend-only.





