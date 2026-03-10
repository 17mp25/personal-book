# Personal Book API

A Spring Boot REST API that integrates with the Google Books API to search for books
and persist them to a local database.

## Tech Stack
- Java 22
- Spring Boot 4.0.1
- Spring Data JPA (H2 in-memory DB)
- MapStruct
- Lombok
- Mockito / MockWebServer

## Prerequisites
- Java 17+
- Maven 3.6+
- A valid Google Books API key

## API Key Setup
1. Open `src/main/resources/application.properties`
2. Replace the placeholder with your real key:
```
google.books.api-key=YOUR_ACTUAL_KEY
google.books.base-url=https://www.googleapis.com/books/v1
```

## Build
```bash
mvn clean install -DskipTests
```

## Run
```bash
mvn spring-boot:run
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/books` | Get all saved books |
| GET | `/google?q={query}` | Search Google Books API |
| POST | `/books/{googleBookId}` | Fetch from Google Books and save |

### Example
```bash
curl -X POST http://localhost:8080/books/ka2VUBqHiWkC
curl -X GET http://localhost:8080/books
```

## Running Tests
```bash
mvn test
```
> Smoke tests require a valid API key. Run `GoogleBookServiceSmokeTests`
> manually in IntelliJ with a valid key configured.

## Assumptions
1. H2 in-memory database is used — data resets on restart
2. A valid Google Books API key must be configured before running the app or integration tests
3. Duplicate books (same Google Book ID) are rejected with HTTP 409
4. Book data is validated before saving — title and authors must be present
5. Only the first author is stored when a book has multiple authors
6. `application.properties` is excluded from version control to protect the API key
7. Flyway/Liquibase not used as H2 auto-generates schema on startup
8. Smoke tests require network access and a valid API key — excluded from `mvn test`