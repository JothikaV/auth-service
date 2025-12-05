# Auth Service

## Features

* User Registration and Login with JWT Authentication
* Role-Based Access Control (RBAC) for authorization
* User and Role Management APIs
* Audit Logging for entity changes (createdBy, lastModifiedBy, createdDate, lastModifiedDate)
* Caching of current user using Spring Cache
* Kafka producer user events (`user-login`, `user-register`)
* Database integration (MySQL)
* Swagger/OpenAPI documentation for APIs
* Spring Security for endpoint protection

## Setup Instructions

### Local Setup (Gradle)

1. Configure application properties:

```properties
# src/main/resources/application.properties

# --- Database Configuration ---
spring.datasource.url=jdbc:mysql://localhost:3306/authdb     # MySQL database URL
spring.datasource.username=root                              # Database username
spring.datasource.password=password                          # Database password

# --- JPA / Hibernate ---
spring.jpa.hibernate.ddl-auto=update                         # Auto-update database schema
spring.jpa.show-sql=true                                     # Show SQL queries in logs

# --- Kafka Configuration ---
spring.kafka.bootstrap-servers=localhost:9092                # Kafka broker address

# --- JWT Configuration ---
auth.jwt.secret=your_jwt_secret_here                         # Secret key for JWT signing
```

2. Build the project:

```bash
./gradlew clean build
```

3. Run the project:

```bash
./gradlew bootRun
```

### Docker Setup

1. Ensure Docker and Docker Compose are installed.
2. Update `docker-compose.yml` with DB, Kafka, and Zookeeper configurations.
3. Run:

```bash
docker-compose up --build
```
You can also open the project in an IDE and run it directly while building Docker for Kafka services.

## Database Schema & Migrations

### Tables

**users**

| Column     | Type     | Description            |
| ---------- | -------- | ---------------------- |
| id         | BIGINT   | Primary Key            |
| username   | VARCHAR  | Username               |
| email      | VARCHAR  | Email, unique          |
| password   | VARCHAR  | Encrypted password     |
| created_at | DATETIME | Created timestamp      |
| updated_at | DATETIME | Last updated timestamp |
| created_by | VARCHAR  | Auditor username       |
| updated_by | VARCHAR  | Auditor username       |

**roles**

| Column     | Type     | Description                             |
| ---------- | -------- | --------------------------------------- |
| id         | BIGINT   | Primary Key                             |
| name       | VARCHAR  | Role name (ROLE_ADMIN, ROLE_USER, etc.) |
| created_at | DATETIME | Created timestamp                       |
| updated_at | DATETIME | Last updated timestamp                  |
| created_by | VARCHAR  | Auditor username                        |
| updated_by | VARCHAR  | Auditor username                        |

**user_roles** (join table)

| Column  | Type   |
| ------- | ------ |
| user_id | BIGINT |
| role_id | BIGINT |

### Auditing

* `@CreatedBy`, `@LastModifiedBy`, `@CreatedDate`, `@LastModifiedDate` are used.
* `AuditorAware` implementation fetches current authenticated username.

## API Endpoints

* **POST /users/register** - Register a new user
* **POST /users/login** - Login and retrieve JWT
* **GET /users/me** - Fetch current logged-in user
* **POST /roles** - Create one or more roles (Admin only)
* **POST /users/{userId}/roles** - Assign roles (Admin only)
* **GET /roles/admin/stats** - Fetch admin statistics (Admin only)

### Swagger UI

Access Swagger documentation at:

```
http://localhost:8082/auth/swagger-ui.html
```

## Design Decisions

* Spring Boot with Spring Security for RBAC
* JWT used for stateless authentication
* Roles prefixed with `ROLE_` for Spring Security compatibility
* Caching `currentUser` reduces DB calls
* Kafka used for event-driven notifications (login/register events)
* Audit fields maintained automatically via Spring Data JPA
* MySQL database with auto schema generation via Hibernate
* Docstrings added for all major classes for clarity

## Caching

* Current user is cached using `@Cacheable(value = "currentUser", key = "#email")`
* Reduces repeated database calls for frequently accessed user info

## Testing

* Integration tests using `@SpringBootTest` and `MockMvc`
* Tests cover:

    * Unauthorized access
    * JWT authentication
    * Role-based access
    * Admin statistics
    * Role assignment

## Kafka

* Topics: `user-login`, `user-register`
* `KafkaProducerService` sends messages

## Build & Run Commands

### Gradle Local Build & Run

```bash
./gradlew clean build
./gradlew bootRun
```

### Docker

```bash
docker-compose up --build
```
