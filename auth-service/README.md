# Spring Boot Authentication Service

This repository contains a secure, production-ready **Authentication Service** built using Spring Boot, Spring Data JPA, Spring Security, JSON Web Token (JWT), and PostgreSQL. 

The service features a layered architecture, inputs validation, global exception handling, and robust **Role-Based Access Control (RBAC)**.

---

## Features
- **User Registration**: Creates users with unique emails and encrypted passwords.
- **User Login**: Authenticates credentials and returns a secure JWT token containing role claims.
- **Role-Based Access Control (RBAC)**: Support for roles: `USER`, `THEATRE_OWNER`, and `ADMIN`.
- **JWT-Based Authentication**: Stateless request authentication using Bearer tokens.
- **Input Validation**: Strict validations for email formatting and password length.
- **Unified Error Handling**: Clear and structured error responses.
- **Dual DB Config**: Default PostgreSQL configuration with an active H2 in-memory profile fallback for zero-setup local testing.

---

## Project Structure
The project follows standard Spring Boot layered architecture:
```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/assignment/authservice/
│   │   │   ├── controller/         # REST Controllers for routing
│   │   │   │   └── AuthController.java
│   │   │   ├── service/            # Business logic (User & JWT services)
│   │   │   │   ├── UserService.java
│   │   │   │   ├── UserServiceImpl.java
│   │   │   │   └── JwtService.java
│   │   │   ├── repository/         # Data access layers (JPA)
│   │   │   │   └── UserRepository.java
│   │   │   ├── entity/             # Database JPA Entities & Enums
│   │   │   │   ├── User.java
│   │   │   │   └── Role.java
│   │   │   ├── dto/                # Request/Response data transfer objects
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── security/           # Spring Security filter chain configurations
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtAuthenticationEntryPoint.java
│   │   │   └── exception/          # Global REST Exception Handlers & custom exceptions
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       └── UserAlreadyExistsException.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties      # Main properties (defaults to PostgreSQL)
│   │       └── application-h2.properties   # Local Dev H2 In-Memory DB configuration
```

---

## Database Configuration

The application is configured to run on PostgreSQL by default. To make it extremely easy to run and test locally out-of-the-box, it also contains a profile for H2 database.

### 1. PostgreSQL (Default / Production)
In `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 2. H2 In-Memory Database (Development Fallback)
In `src/main/resources/application-h2.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:authdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=password
spring.datasource.driver-class-name=org.h2.Driver
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## Steps to Run the Project

Ensure you have **Java 17** installed.

### 1. Run using H2 Database (No setup required)
The H2 profile is configured as the active profile by default in `application.properties`. You can launch it directly:
```bash
./mvnw spring-boot:run
```
Once started:
- The server will run at: `http://localhost:8080`
- The H2 console is accessible at: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:authdb`
  - Username: `sa`
  - Password: `password`

### 2. Run using PostgreSQL Database
Ensure you have PostgreSQL running and a database named `auth_db` created. Change or remove the active profile setting in `application.properties`:
```properties
spring.profiles.active=prod
```
Then start the application:
```bash
./mvnw spring-boot:run
```

### 3. Run Automated Tests
Execute the JUnit integration tests:
```bash
./mvnw clean test
```

---

## API Endpoints

### 1. Register User
- **Method**: `POST`
- **URL**: `/auth/register`
- **Request Body**:
```json
{
  "name": "Bruce Wayne",
  "email": "bruce@gotham.com",
  "password": "gothamKnight",
  "role": "ADMIN"
}
```
*Note: Available roles are `USER`, `THEATRE_OWNER`, and `ADMIN`.*

- **Response (201 Created)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 86400000,
  "id": 1,
  "name": "Bruce Wayne",
  "email": "bruce@gotham.com",
  "role": "ADMIN"
}
```

### 2. Login User
- **Method**: `POST`
- **URL**: `/auth/login`
- **Request Body**:
```json
{
  "email": "bruce@gotham.com",
  "password": "gothamKnight"
}
```

- **Response (200 OK)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 86400000,
  "id": 1,
  "name": "Bruce Wayne",
  "email": "bruce@gotham.com",
  "role": "ADMIN"
}
```

### 3. Role-Based Verification Endpoints
To test Role-Based Access Control, pass the JWT token in the `Authorization` header as `Bearer <token>`.

#### Test User Access
- **Method**: `GET`
- **URL**: `/auth/test/user`
- **Allowed Roles**: `USER`, `THEATRE_OWNER`, `ADMIN`
- **Header**: `Authorization: Bearer <token>`

#### Test Theatre Owner Access
- **Method**: `GET`
- **URL**: `/auth/test/theatre`
- **Allowed Roles**: `THEATRE_OWNER`, `ADMIN`
- **Header**: `Authorization: Bearer <token>`

#### Test Admin Access
- **Method**: `GET`
- **URL**: `/auth/test/admin`
- **Allowed Roles**: `ADMIN`
- **Header**: `Authorization: Bearer <token>`

---

## Error Handling Specifications

All exceptions (validation, bad credentials, resource conflicts) return a structured JSON response:

### Validation Error (400 Bad Request)
```json
{
  "timestamp": "2026-06-16T10:23:08.543",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": {
    "email": "Invalid email format",
    "password": "Password must be at least 6 characters"
  }
}
```

### Bad Credentials (401 Unauthorized)
```json
{
  "timestamp": "2026-06-16T10:23:08.672",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

### Email Conflict (409 Conflict)
```json
{
  "timestamp": "2026-06-16T10:23:08.701",
  "status": 409,
  "error": "Conflict",
  "message": "Email 'bruce@gotham.com' is already registered"
}
```
