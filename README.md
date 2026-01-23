# NHL Data Service

A Spring Boot REST API that fetches NHL game data with database caching and role-based JWT authentication.

![CI Pipeline](https://github.com/sven-0414/nhl-data-service/workflows/CI%20Pipeline/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sven-0414_nhl-data-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sven-0414_nhl-data-service)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green)

## Features

- **Role-Based Access Control**: USER and ADMIN roles with different permissions
- **JWT Authentication**: Secure stateless authentication
- **User Management**: Complete CRUD operations for user accounts
- **Smart Caching**: Historical games cached in database, live games fetched fresh from NHL API
- **Automated CI/CD**: GitHub Actions with SonarCloud integration

## Tech Stack

- Spring Boot 3.4.4
- Spring Security 6 (JWT + RBAC)
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
   ```

4. **Set database password** (in Run Configuration or export)
   ```bash
   export DB_PASSWORD=your_password
   ```

5. **Run**
   ```bash
   ./mvnw spring-boot:run
   ```

### Default Admin User

On first startup, a default admin user is created:
- **Username**: `admin`
- **Password**: `[FYLL I FRÅN DIN DATALOADER]`

## API Overview

### Authentication (Public)

**Register:**
```http
POST /auth/register
Content-Type: application/json

{
  "username": "john",
  "password": "password123",
  "email": "john@example.com"
}
```

**Login:**
```http
POST /auth/login
Content-Type: application/json

{
  "username": "john",
  "password": "password123"
}
```

Returns JWT token valid for 1 hour.

### Game Data (Requires Authentication)

**Get games by date:**
```http
GET /api/v1/games/2025-01-15
Authorization: Bearer {token}
```

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

### User Management (Admin Only)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/users` | GET | List all users |
| `/api/v1/users/{id}` | GET | Get user by ID |
| `/api/v1/users` | POST | Create new user |
| `/api/v1/users/{id}` | PUT | Update user |
| `/api/v1/users/{id}/password` | PUT | Change password |
| `/api/v1/users/{id}/admin` | PUT | Admin update (roles, enabled) |
| `/api/v1/users/{id}` | DELETE | Delete user |

**Role Permissions:**

- **USER**: Access game data, view/update own profile
- **ADMIN**: Full access including user management

## Architecture

### Security
- JWT-based stateless authentication with HS256
- Role-based authorization (USER/ADMIN)
- BCrypt password hashing
- Protection against self-deletion and removing last admin

### Caching Strategy
- Historical games (before today): Cached in PostgreSQL
- Current/future games: Always fetched fresh from NHL API

### Data Flow
```
Request → JWT Filter → Role Check → Controller → Service → Cache/API → Response
```

## Testing

```bash
mvn test
```

Tests include JWT authentication, role-based authorization, and user management.

## Configuration

**Environment Variables:**
- `JWT_SECRET` (required): JWT signing secret
- `JWT_EXPIRATION` (optional): Token lifetime in ms (default: 3600000)
- `DB_PASSWORD` (required): PostgreSQL password

**GitHub Secrets** (for CI/CD):
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `SONAR_TOKEN`

## License

MIT License - see [LICENSE](LICENSE) file for details.
