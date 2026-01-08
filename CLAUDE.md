# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Service Overview
The **statistics-service** is part of the ECMSP e-commerce microservices platform. It is responsible for storing and analyzing sales and delivery statistics, consuming events from Kafka to track product performance, margins, and inventory movements.

## Technology Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.7, Spring Cloud 2025.0.0
- **Database**: PostgreSQL 15 with Spring Data JPA
- **Messaging**: Spring Kafka 3.3.4 for event consumption
- **Build Tool**: Maven with Spring Boot Maven plugin
- **Development**: Lombok, Spring Boot DevTools
- **API Documentation**: SpringDoc OpenAPI 2.3.0
- **Testing**: JUnit 5, H2 for in-memory tests
- **Shared Schemas**: Uses `com.ecmsp:protos:1.0.0-SNAPSHOT` from Nexus repository

## Port Configuration
- **HTTP API**: 8700
- **gRPC Server**: 7400
- **Database (dev)**: 9700
- **Kafka (dev)**: 9094 (external), 8088 (Kafka UI)

## Common Development Commands

### Building and Running
```bash
# Build the service
./mvnw clean compile

# Run the service (default profile: dev)
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=compose

# Package the service
./mvnw clean package

# Run tests
./mvnw test

# Run specific test
./mvnw test -Dtest=ClassName#methodName
```

### Database Management
```bash
# Start PostgreSQL and Kafka for local development
cd docker
docker-compose up -d

# Stop database and Kafka
docker-compose down

# View database logs
docker logs statistics-service-db-dev

# Connect to database
docker exec -it statistics-service-db-dev psql -U admin -d statistics-service-db
```

### Docker Build
```bash
# Build Docker image
docker build -t statistics-service .

# Run in Docker (requires external database)
docker run -p 8700:8080 -p 7400:7400 statistics-service
```

## Database Schema

### SOLD Table
Tracks individual product sales with pricing, margin, and inventory data:
- `ID` (UUID): Primary key
- `VARIANT_ID` (UUID): Product variant identifier
- `PRODUCT_ID` (UUID): Product identifier
- `PRODUCT_NAME` (VARCHAR): Product name for reporting
- `PRICE` (DECIMAL): Sale price
- `QUANTITY` (INT): Quantity sold
- `MARGIN` (DECIMAL): Profit margin
- `STOCK_REMAINING` (INT): Inventory after sale
- `DATE` (TIMESTAMP): Sale timestamp

Indexes: variant_id, product_id, product_name, date

### DELIVERY Table
Tracks product deliveries and restocking events:
- `ID` (UUID): Primary key
- `EVENT_ID` (UUID): Delivery event identifier
- `VARIANT_ID` (UUID): Product variant delivered
- `DELIVERED_QUANTITY` (INT): Quantity received
- `DELIVERED_AT` (TIMESTAMP): Delivery timestamp

Indexes: variant_id, event_id, delivered_at

## Package Structure
- `controller/` - REST API controllers for statistics queries
- `service/` - Business logic for aggregations and analytics
- `repository/` - Spring Data JPA repositories
- `model/` - JPA entities (Sold, Delivery)
- `kafka/` - Kafka event consumers (likely for order/delivery events)

## Spring Profiles

### dev (default)
- Local development profile
- Database: `localhost:9700`
- Kafka: `localhost:9094`
- Credentials: admin/admin
- SQL logging enabled

### compose
- Docker Compose deployment
- Database: `product-service-db:5432` (likely should be statistics-service-db)
- Kafka: `kafka:9092`
- Consumer group: `product-service` (likely should be statistics-service)

### prod
- Production configuration
- External database and Kafka configuration required

## Architecture Notes

### Event Consumption
The service acts as a Kafka consumer, likely listening to:
- Order completion events (to populate SOLD table)
- Delivery events (to populate DELIVERY table)
- Product events (for price/margin updates)

### Integration Points
- Consumes Kafka events from other services (order-service, product-service)
- Provides REST API for querying statistics and analytics
- May provide gRPC endpoints for real-time statistics queries

### Configuration Note
The `application-compose.properties` file has inconsistencies:
- References `product-service-db` instead of `statistics-service-db`
- Consumer group is `product-service` instead of `statistics-service`
These should be corrected if deploying with Docker Compose.

## Development Notes

### Current Implementation Status
- Database schema defined and initialized via `docker/init.sql`
- Package structure created (controller, service, repository, model)
- Currently appears to be in early setup phase (basic-crud-setup branch)
- No JPA entities or Kafka consumers implemented yet

### Testing Strategy
- H2 in-memory database for unit/integration tests
- JUnit 5 test framework
- Consider adding Testcontainers for PostgreSQL integration tests
- Test Kafka consumers with embedded Kafka or Testcontainers

### Maven Repository Configuration
The service uses Nexus repositories for artifact resolution:
- `nexus.ecmsp.pl/repository/maven-releases/`
- `nexus.ecmsp.pl/repository/maven-snapshots/`
- Required for accessing shared `protos` dependency
