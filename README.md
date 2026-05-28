# NHL Data Service

A Spring Boot REST API that fetches NHL game data with database caching and role-based JWT authentication.

![CI Pipeline](https://github.com/sven-0414/nhl-data-service/workflows/CI%20Pipeline/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sven-0414_nhl-data-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sven-0414_nhl-data-service)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green)

## Features

- **Role-Based Access Control**: USER and ADMIN roles with different permissions
- **JWT Authentication**: Secure stateless authentication
- **User Management**: Complete CRUD operations for user accounts
- **Smart Caching**: Historical games cached in database, live games fetched fresh from NHL API
- **Automated CI/CD**: GitHub Actions with SonarCloud integration

## Tech Stack

- Spring Boot 4.0.6
- Spring Security 7 (JWT + RBAC)
- Spring Data JPA
- PostgreSQL
- Maven
- JUnit 5
- GitHub Actions + SonarCloud

## Quick Start

### Prerequisites
- Java 21
- PostgreSQL 12+
- Maven 3.6+

### Setup

1. **Clone and configure**
   ```bash
   git clone https://github.com/sven-0414/nhl-data-service.git
   cd nhl-data-service
   cp .env.example .env
   ```

2. **Generate JWT secret** (minimum 32 characters)
   ```bash
   openssl rand -base64 48
   ```

3. **Update `.env`**
   ```
   JWT_SECRET=your-generated-secret-here
   JWT_EXPIRATION=3600000
   ADMIN_PASSWORD=your-secure-admin-password
   DB_PASSWORD=your-database-password
   ```

4. **Run**
   ```bash
   ./mvnw spring-boot:run
   ```

### Default Admin User

On first startup, a default admin user is created:
- **Username**: `admin`
- **Password**: `defaultPassword123`

To use a custom password, set `ADMIN_PASSWORD` before starting:
```bash
export ADMIN_PASSWORD=your-secure-password
```

## API Overview

### Authentication (Public)

**Register:**
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "john",
  "password": "password123",
  "email": "john@example.com"
}
```

**Login:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "john",
  "password": "password123"
}
```

Returns JWT token valid for 1 hour (configurable via `JWT_EXPIRATION`).

### Game Data (Requires Authentication)

**Get games by date:**
```http
GET /api/v1/games/{date}
Authorization: Bearer {token}
```

- `{date}` must be in `yyyy-MM-dd` format (e.g. `2025-01-15`)
- Returns `200 OK` with a list of games, or `204 No Content` if no games were played that date
- Returns `400 Bad Request` if the date format is invalid
- Historical dates are served from the database cache; current and future dates are always fetched fresh from the NHL API

**Example response:**
```json
[
  {
    "id": 2024020123,
    "gameDate": "2025-01-15",
    "homeTeam": {
      "abbrev": "VAN",
      "score": 4
    },
    "awayTeam": {
      "abbrev": "BOS",
      "score": 2
    },
    "gameState": "FINAL"
  }
]
```

### User Management

| Endpoint                     | Method | Access     | Description              |
|------------------------------|--------|------------|--------------------------|
| `/api/v1/users/me`           | GET    | Any        | Get own profile          |
| `/api/v1/users/me`           | PUT    | Any        | Update own profile       |
| `/api/v1/users/me/password`  | PUT    | Any        | Change own password      |
| `/api/v1/users`              | GET    | Any        | List all users           |
| `/api/v1/users/{id}`         | GET    | Any        | Get user by ID           |
| `/api/v1/users`              | POST   | Admin      | Create new user          |
| `/api/v1/users/{id}`         | PUT    | Admin/Own  | Update user              |
| `/api/v1/users/{id}`         | DELETE | Admin      | Delete user              |
| `/api/v1/users/{id}/admin`   | PUT    | Admin      | Update roles and status  |

**Role Permissions:**

- **USER**: Access game data, view all user profiles, update own profile and password
- **ADMIN**: Full access including user creation, deletion, and role management

## Architecture

### Security
- JWT-based stateless authentication with HS256
- Role-based authorization (USER/ADMIN)
- BCrypt password hashing
- Protection against self-deletion and removing last admin
- Disabled users are rejected even with a valid token

### Caching Strategy
- Historical games (before today): Cached in PostgreSQL, served directly from the database
- Current and future games: Always fetched fresh from the NHL API

### Data Flow
```
Request → JWT Filter → Role Check → Controller → Service → Cache/API → Response
```

## Testing

```bash
mvn test
```

Tests include JWT authentication, role-based authorization, and user management across unit and integration test suites.

## Configuration

**Environment Variables:**

| Variable         | Required | Default            | Description                            |
|------------------|----------|--------------------|----------------------------------------|
| `JWT_SECRET`     | Yes      | –                  | JWT signing secret (min 32 characters) |
| `JWT_EXPIRATION` | No       | `3600000`          | Token lifetime in milliseconds         |
| `DB_PASSWORD`    | Yes      | –                  | PostgreSQL password                    |
| `ADMIN_PASSWORD` | No       | `defaultPassword123` | Initial admin user password          |

**GitHub Secrets** (for CI/CD):
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `SONAR_TOKEN`

## Known Limitations

- **No rate limiting**: The `/auth/login` and `/auth/register` endpoints have no rate limiting. Brute force protection should be added before any production use.
- **No token blacklisting**: Issued JWT tokens remain valid until expiry. Logging out does not invalidate the token server-side.

## License

MIT License - see [LICENSE](LICENSE) file for details.
