#  Distributed URL Shortener

A scalable backend service for shortening long URLs, handling high-frequency redirects, enforcing API rate limits, and generating real-time click analytics.  
Designed to demonstrate core backend engineering concepts such as caching, concurrency handling, scheduled jobs, and distributed system patterns.

---

##  Features

- URL shortening with Base62 encoding
- Redis-based caching for fast redirects
- Click analytics (total, daily, hourly)
- Rate limiting using Redis
- Scheduled cleanup for expired URLs
- Background sync of click counts to database
- Fully containerized using Docker
- Interactive API documentation with Swagger

---

##  Architecture Overview

- **Spring Boot** handles REST APIs and business logic  
- **Redis** is used for caching, rate limiting, and temporary analytics storage  
- **PostgreSQL** stores persistent URL and analytics data  
- **Schedulers** periodically sync analytics data and clean expired URLs  
- **Lua scripts** ensure atomic rate-limiting operations in Redis  

The design separates read-heavy and write-heavy operations to achieve low latency and scalability.

---

##  Tech Stack

| Layer | Technology |
|-----|-----------|
| Language | Java |
| Framework | Spring Boot |
| Database | PostgreSQL |
| Cache | Redis |
| Rate Limiting | Redis + Lua |
| ORM | JPA / Hibernate |
| API Documentation | Swagger (OpenAPI) |
| Containerization | Docker, Docker Compose |

---

##  Project Structure
```

src/main/java/com/urlshortener
├── controller 
├── service
├── repository 
├── scheduler 
├── ratelimit 
├── interceptor 
├── config 
├── model 
└── util 

```

---

##  Application Flow

### URL Shortening
1. Generate a unique ID using Redis
2. Encode ID using Base62
3. Store metadata in PostgreSQL
4. Cache short URL mapping in Redis

### Redirection
1. Fetch original URL from Redis (fallback to DB)
2. Increment click counters in Redis
3. Redirect user with minimal latency

### Analytics
- Clicks tracked in Redis (hourly & daily)
- Scheduled jobs sync counts to PostgreSQL
- APIs expose aggregated analytics

### Rate Limiting
- Implemented using Redis Lua scripts
- Fixed-window strategy
- Rate limit details returned in response headers

---

##  API Endpoints

### Shorten URL
```http
POST /api/shorten
Content-Type: application/json

{
  "originalUrl": "https://example.com"
}

```

Redirect
```
GET /{shortCode}
```

Analytics Summary
```
GET /api/analytics/{shortCode}/summary
```

Hourly Analytics
```
GET /api/analytics/{shortCode}/hourly
```

Top URL's
```
GET /api/analytics/{shortCode}/hourly
```


---

## Swagger API Documentation

http://localhost:8080/swagger-ui.html

---

##  Running Locally with Docker

### Prerequisites
- Docker
- Docker Compose

### Clone Repository
```
git clone https://github.com/NikhilaManogna/Distributed-Url-Shortener.git
```

### Start Application
```
cd Distributed-Url-Shortener
docker compose up --build
```

### Running Locally without Docker

1. Configure `application-dev.yml`
2. Start Redis locally
3. Start PostgreSQL locally
4. Run the Spring Boot application via:

```
./mvnw spring-boot:run
```

---

## Services

| Service | URL |
|--------|-----|
| Application | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Redis | localhost:6379 |
| PostgreSQL | localhost:5432 |

---

## Sample Curl Request
```
curl -i -X POST http://localhost:8080/api/shorten
-H "Content-Type: application/json"
-d "{\"originalUrl\":\"http://google.com\"}"
```

---

## Deployment Ready

The application is fully containerized and can be deployed on:

- AWS (EC2 / ECS)
- GCP (Cloud Run)
- Azure Container Apps
- Render / Fly.io

## Contributing

Contributions are welcome!  
To contribute:
1. Fork the repo
2. Create a feature branch
3. Commit your changes
4. Open a pull request

