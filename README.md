# NHL Data Service

A Spring Boot REST API that fetches NHL game data with database caching, JWT authentication, and automated CI pipeline.

![CI Pipeline](https://github.com/sven-0414/nhl-data-service/workflows/CI%20Pipeline/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sven-0414_nhl-data-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sven-0414_nhl-data-service)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green)
![Maven](https://img.shields.io/badge/Maven-Build-blue)

## 🏒 Features

- **JWT Authentication**: Secure API access with JSON Web Tokens and Spring Security 6
- **Smart Caching Strategy**: Historical games cached in database, live games fetched fresh
- **Clean JSON API**: Simplified response format without betting/broadcast data
- **Automated CI**: GitHub Actions pipeline with testing and code quality analysis
- **Code Quality**: SonarCloud integration with Quality Gate monitoring
- **Database Optimization**: Efficient team caching to minimize database ca lls
- **Modern Security**: Stateless authentication with proper token validation

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.6+
- PostgreSQL 12+ (or H2 for development/testing)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/sven-0414/nhl-data-service.git
   cd nhl-data-service
   ```

2. **Configure environment variables**
   ```bash
   export DB_PASSWORD=your_password
   export ADMIN_PASSWORD=your_admin_password  # Optional, defaults to 'defaultPassword123'
   ```

3. **Configure database** (application.properties)
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/nhldb
   spring.datasource.username=your_username
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Login to get JWT token**
   ```bash
   curl -X POST http://localhost:8081/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"defaultPassword123"}'
   ```

6. **Use token for API calls**
   ```bash
   curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8081/api/v1/games/2024-01-15
   ```

## 🔐 Authentication

All API endpoints (except `/auth/login`) require JWT authentication using Spring Security 6.

### Default Credentials
- **Username**: `admin`
- **Password**: `defaultPassword123` (or value from `ADMIN_PASSWORD` environment variable)

### Getting a Token

**Request:**
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "defaultPassword123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY..."
}
```

### Using the Token

Include the JWT token in the Authorization header for all API requests:

```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY...
```

**Token Details:**
- **Expiration**: 24 hours
- **Algorithm**: HS512
- **Claims**: Subject (username), issued at, expiration

## 📋 API Documentation

### Authentication Endpoints

#### Login
```http
POST /auth/login
```

**Request Body:**
```json
{
  "username": "admin",
  "password": "defaultPassword123"
}
```

**Response:**
- `200 OK`: Login successful, returns JWT token
- `401 Unauthorized`: Invalid credentials

### Game Data Endpoints

#### Get Games by Date

```http
GET /api/v1/games/{date}
Authorization: Bearer {jwt_token}
```

**Parameters:**
- `date` (required): Date in YYYY-MM-DD format

**Example Request:**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8081/api/v1/games/2025-01-15
```

**Example Response:**
```json
[
  {
    "id": 2024020123,
    "season": 20242025,
    "gameType": 2,
    "gameDate": "2025-01-15",
    "venue": {
      "default": "Rogers Arena"
    },
    "gameState": "FINAL",
    "homeTeam": {
      "id": 23,
      "abbrev": "VAN",
      "name": {
        "default": "Canucks"
      },
      "placeName": {
        "default": "Vancouver"
      },
      "score": 4
    },
    "awayTeam": {
      "id": 6,
      "abbrev": "BOS",
      "name": {
        "default": "Bruins"
      },
      "placeName": {
        "default": "Boston"
      },
      "score": 2
    },
    "startTimeUTC": "2025-01-16T03:00:00Z"
  }
]
```

**Response Codes:**
- `200 OK`: Games found for the specified date
- `204 No Content`: No games scheduled for the date
- `401 Unauthorized`: Missing or invalid JWT token

## 🏗️ Architecture

### Security Architecture
- **Authentication**: JWT-based stateless authentication
- **Authorization**: Spring Security 6 filter chain
- **Token Management**: Custom JWT utility with HS512 signing
- **User Storage**: Database-backed user management

### Caching Strategy
- **Historical games** (before today): Cached in PostgreSQL database
- **Current/future games**: Always fetched fresh from NHL API
- **Team data**: Optimized with in-memory caching during batch operations

### Data Flow
```
Client Request → JWT Filter → Security Context → Controller → Service Layer → Database Check → NHL API (if needed) → Response
```

### Technologies Used
- **Spring Boot 3.4.4**: Main framework
- **Spring Security 6**: Authentication and authorization
- **Spring Data JPA**: Database operations
- **JWT (JJWT 0.12.6)**: Token generation and validation
- **PostgreSQL**: Production database
- **H2**: Testing database
- **Jackson**: JSON processing
- **Lombok**: Reduce boilerplate code
- **JUnit 5**: Testing framework

## 🔄 CI/CD Pipeline

The project includes a comprehensive CI/CD pipeline with:

- **Automated Testing**: JUnit tests run on every push
- **Security Testing**: JWT authentication and authorization tests
- **Code Quality Analysis**: SonarCloud integration with Quality Gate
- **Build Artifacts**: Automatic JAR packaging and upload
- **Multi-environment Support**: Separate configurations for development and testing

### Pipeline Status
All tests and quality checks must pass before code reaches main branch.

## 🧪 Testing

**Run all tests:**
```bash
mvn test
```

### Test Configuration
- **Production**: PostgreSQL database with JWT authentication
- **Testing**: H2 in-memory database with security disabled for easier testing
- **Authentication Tests**: Comprehensive JWT token validation and security filter testing

### Security Testing
The test suite includes:
- JWT token generation and validation
- Authentication filter behavior
- Protected endpoint access control
- User management functionality

## 🔧 Configuration

### Application Properties (Production)
```properties
# Server
server.port=8081

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/nhldb
spring.datasource.username=your_username
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update

# JWT Configuration
jwt.secret=${JWT_SECRET:mySecretKey123456789012345678901234567890}
jwt.expiration=${JWT_EXPIRATION:86400000}

# Logging
logging.level.se.sven.nhldataservice=INFO
logging.file.name=./logs/nhl-service.log
```

### Environment Variables
- `DB_PASSWORD`: Database password (required for production)
- `ADMIN_PASSWORD`: Admin user password (optional, defaults to 'defaultPassword123')
- `JWT_SECRET`: JWT signing secret (optional, has secure default)
- `JWT_EXPIRATION`: Token expiration in milliseconds (optional, defaults to 24 hours)

### Test Configuration
Tests automatically use H2 in-memory database with security disabled for easier testing.

## 📊 Code Quality

This project maintains high code quality standards:

- **SonarCloud Quality Gate**: ✅ Passed
- **Security Rating**: A (0 security hotspots)
- **Reliability Rating**: A (0 bugs)
- **Maintainability Rating**: A (minimal code smells)
- **Test Coverage**: 45%+ (including security tests)
- **Code Duplication**: 0.0%

[View detailed analysis on SonarCloud →](https://sonarcloud.io/project/overview?id=sven-0414_nhl-data-service)

## 🔒 Security Features

### JWT Implementation
- **Algorithm**: HMAC SHA-512 for token signing
- **Expiration**: Configurable token lifetime (default 24 hours)
- **Stateless**: No server-side session storage
- **Secure**: Proper token validation and user authentication

### Spring Security 6
- **Modern Configuration**: Bean-based security configuration
- **Custom Filters**: JWT authentication filter integration
- **Exception Handling**: Proper authentication and authorization error responses
- **CSRF Protection**: Disabled for stateless API design

### Password Security
- **BCrypt Encoding**: Secure password hashing
- **Environment Variables**: Sensitive configuration externalized
- **Default User**: Automatic admin user creation for development

## 🚀 Development

### Running Locally
1. Set up PostgreSQL database
2. Configure environment variables in your IDE or `.env` file
3. Run `NhlDataServiceApplication.java`
4. Use Postman or curl to test authentication and API endpoints

### API Testing with Postman
1. **POST** `/auth/login` with credentials to get JWT token
2. **Copy** the token from response
3. **Add** `Authorization: Bearer {token}` header to subsequent requests
4. **Test** protected endpoints like `/api/v1/games/{date}`

### Contributing
1. Create a feature branch
2. Make your changes with appropriate tests
3. Ensure all tests pass including security tests
4. Verify CI pipeline passes
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Built with modern Spring Boot practices, enterprise-grade security, and automated quality assurance**