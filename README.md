# BFHL API — D.Y. Patil Campus Hiring (June 2026)

A production-ready Spring Boot REST API that processes mixed arrays of input data and returns categorized, enriched responses.

---

## 🚀 Quick Start

### Prerequisites
- Java 21+

### Run Locally

```bash
# Windows
./mvnw.cmd spring-boot:run

# Mac/Linux
./mvnw spring-boot:run
```

The API will be available at: `http://localhost:8080`

---

## 📌 Endpoint

### `POST /bfhl`

**Headers:**
| Header | Required | Description |
|--------|----------|-------------|
| `X-Request-Id` | Optional | Echo'd back in the response |
| `Content-Type` | Required | `application/json` |

**Request Body:**
```json
{
  "data": ["A", "1", "22", "$", "B", "7"]
}
```

**Response:**
```json
{
  "is_success": true,
  "request_id": "REQ-1001",
  "odd_numbers": ["1", "7"],
  "even_numbers": ["22"],
  "alphabets": ["A", "B"],
  "special_characters": ["$"],
  "sum": "30",
  "largest_number": "22",
  "smallest_number": "1",
  "alphabet_count": 2,
  "number_count": 3,
  "special_character_count": 1,
  "contains_duplicates": false,
  "unique_element_count": 6,
  "processing_time_ms": 5,
  "alphabet_frequency": { "A": 1, "B": 1 },
  "sorted_numbers": ["1", "7", "22"],
  "vowel_count": 1,
  "longest_alphabetic_value": "A",
  "shortest_alphabetic_value": "A",
  "summary": {
    "total_elements_received": 6,
    "valid_elements_processed": 6,
    "invalid_elements_ignored": 0
  }
}
```

---

## 🏗 Architecture

```
com.dypatil.bfhl
├── BfhlApiApplication.java         # Entry point
├── config/
│   └── AsyncConfig.java            # Thread pool for large payloads
├── controller/
│   └── BfhlController.java         # REST controller (POST /bfhl)
├── dto/
│   ├── BfhlRequest.java            # Input DTO with Bean Validation
│   ├── BfhlResponse.java           # Output DTO with all fields
│   └── ErrorResponse.java          # Error response DTO
├── exception/
│   ├── BfhlException.java          # Custom application exception
│   └── GlobalExceptionHandler.java # @ControllerAdvice error handler
└── service/
    ├── BfhlService.java            # Service interface
    └── impl/
        └── BfhlServiceImpl.java    # Business logic implementation
```

---

## ⚡ Features

| Feature | Supported |
|---------|-----------|
| Numbers (odd/even classification) | ✅ |
| Alphabets extraction | ✅ |
| Special characters | ✅ |
| Alphanumeric string decomposition | ✅ |
| Negative & decimal numbers | ✅ |
| Duplicate detection & removal | ✅ |
| Null / empty string filtering | ✅ |
| X-Request-Id header echo | ✅ |
| Processing time (ms) | ✅ |
| Alphabet frequency map | ✅ |
| Sorted numbers (ascending) | ✅ |
| Vowel count | ✅ |
| Longest/shortest alphabetic value | ✅ |
| Unique element count | ✅ |
| Summary object | ✅ |
| Async processing for >10,000 elements | ✅ |
| Correlation ID for async requests | ✅ |
| Global exception handling | ✅ |
| Bean Validation | ✅ |
| Structured logging | ✅ |
| JUnit 5 + Mockito unit tests | ✅ |
| MockMvc integration tests | ✅ |
| JaCoCo coverage reporting | ✅ |

---

## 🧪 Running Tests

```bash
./mvnw.cmd test
```

Coverage report: `target/site/jacoco/index.html`

---

## 🏥 Health Check

```
GET /actuator/health
```

---

## ☁️ Deployment (Render)

1. Push this repo to GitHub
2. Go to [render.com](https://render.com) → New Web Service
3. Connect your GitHub repo
4. Render auto-detects `render.yaml` and deploys

Or manually set:
- **Build Command:** `./mvnw clean package -DskipTests`
- **Start Command:** `java -jar target/bfhl-api-1.0.0.jar`
- **Environment:** Java 21

---

## 📋 Sample cURL Commands

```bash
# Example 1
curl -X POST http://localhost:8080/bfhl \
  -H "Content-Type: application/json" \
  -H "X-Request-Id: REQ-1001" \
  -d '{"data": ["A", "1", "22", "$", "B", "7"]}'

# Example 4: Negative & decimal
curl -X POST http://localhost:8080/bfhl \
  -H "Content-Type: application/json" \
  -H "X-Request-Id: REQ-1004" \
  -d '{"data": ["-10", "25.5", "-100.75", "B", "@", "5", "A9"]}'

# Example 3: Duplicates + nulls
curl -X POST http://localhost:8080/bfhl \
  -H "Content-Type: application/json" \
  -H "X-Request-Id: REQ-1003" \
  -d '{"data": ["10", "10", "A", "A", "", null, "&", "5"]}'
```
