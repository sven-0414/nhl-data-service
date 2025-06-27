# NHL Data Service

A Spring Boot REST API that fetches NHL game data with database caching and real-time updates.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green)
![Maven](https://img.shields.io/badge/Maven-Build-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 🏒 Features

- **Caching Strategy**: Historical games cached in database, live games fetched fresh
- **Real-time Data**: Always current scores and game status for today's matches
- **Clean JSON API**: Simplified response format without betting/broadcast data

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.6+
- PostgreSQL 12+ (or H2 for development)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/sven-0414/nhl-data-service.git
   cd nhl-data-service
   ```

2. **Configure database** (application.properties)
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/nhldb
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Test the API**
   ```bash
   curl http://localhost:8081/api/games/2024-01-15
   ```

## 📋 API Documentation

### Get Games by Date

```http
GET /api/games/{date}
```

**Parameters:**
- `date` (required): Date in YYYY-MM-DD format

**Example Request:**
```bash
curl http://localhost:8081/api/games/2025-01-15
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
- **Jackson**: JSON processing
- **Lombok**: Reduce boilerplate code
- **JUnit 5**: Testing framework

## 🧪 Testing

Run all tests:
```bash
mvn test
```

Run with coverage report:
```bash
mvn test jacoco:report
```

View coverage: `target/site/jacoco/index.html`

## 🔧 Configuration

### Application Properties
```properties
# Server
server.port=8081

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/nhldb
spring.jpa.hibernate.ddl-auto=update

# Logging
logging.level.se.sven.nhldataservice=INFO
logging.file.name=./logs/nhl-service.log
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
**Built with ❤️ for hockey fans and Spring Boot enthusiasts**