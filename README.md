# NHL Data Service

A Spring Boot REST API that fetches NHL game data with intelligent database caching and automated CI/CD pipeline.

![CI Pipeline](https://github.com/sven-0414/nhl-data-service/workflows/CI%20Pipeline/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sven-0414_nhl-data-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sven-0414_nhl-data-service)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green)
![Maven](https://img.shields.io/badge/Maven-Build-blue)

## 🏒 Features

- **Smart Caching Strategy**: Historical games cached in database, live games fetched fresh
- **Clean JSON API**: Simplified response format without betting/broadcast data
- **Automated CI/CD**: GitHub Actions pipeline with testing and code quality analysis
- **Code Quality**: SonarCloud integration with Quality Gate monitoring
- **Database Optimization**: Efficient team caching to minimize database calls

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

5. **Test the API**
   ```bash
   curl http://localhost:8081/api/v1/games/2024-01-15
   ```

## 📋 API Documentation

### Get Games by Date

```http
GET /api/v1/games/{date}
```

**Parameters:**
- `date` (required): Date in YYYY-MM-DD format

**Example Request:**
```bash
curl http://localhost:8081/api/v1/games/2025-01-15
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
- `500 Internal Server Error`: API or database error

## 🏗️ Architecture

### Caching Strategy
- **Historical games** (before today): Cached in PostgreSQL database
- **Current/future games**: Always fetched fresh from NHL API
- **Team data**: Optimized with in-memory caching during batch operations

### Data Flow
```
Client Request → Controller → Service Layer → Database Check → NHL API (if needed) → Response
```

### Technologies Used
- **Spring Boot 3.4.4**: Main framework
- **Spring Data JPA**: Database operations
- **PostgreSQL**: Production database
- **H2**: Testing database
- **Jackson**: JSON processing
- **Lombok**: Reduce boilerplate code
- **JUnit 5**: Testing framework

## 🔄 CI/CD Pipeline

The project includes a comprehensive CI/CD pipeline with:

- **Automated Testing**: JUnit tests run on every push
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
- **Production**: PostgreSQL database
- **Testing**: H2 in-memory database
- **Security**: Disabled for test environment

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

# Logging
logging.level.se.sven.nhldataservice=INFO
logging.file.name=./logs/nhl-service.log
```

### Environment Variables
- `DB_PASSWORD`: Database password (required for production)

### Test Configuration
Tests automatically use H2 in-memory database with separate application properties.

## 📊 Code Quality

This project maintains high code quality standards:

- **SonarCloud Quality Gate**: ✅ Passed
- **Security Rating**: A (0 issues)
- **Reliability Rating**: A (0 issues)
- **Maintainability Rating**: A (1 minor issue)
- **Test Coverage**: 48.4%
- **Code Duplication**: 0.0%

[View detailed analysis on SonarCloud →](https://sonarcloud.io/project/overview?id=sven-0414_nhl-data-service)

## 🚀 Development

### Running Locally
1. Set up PostgreSQL database
2. Configure environment variables in your IDE
3. Run `NhlDataServiceApplication.java`

### Contributing
1. Create a feature branch
2. Make your changes with tests
3. Ensure CI pipeline passes
4. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Built with modern Spring Boot practices and automated quality assurance**