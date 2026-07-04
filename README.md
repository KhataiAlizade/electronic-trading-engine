# Electronic Trading Platform - Matching Engine

A robust backend REST API built to simulate an electronic stock exchange. Features a price-time priority matching engine and real-time order book aggregation.

**Tech Stack:** Java 17, Spring Boot 3, Spring Data JPA, PostgreSQL, Maven

### Key Architecture Decisions
* **Thread Safety:** Used Hibernate Optimistic Locking (`@Version`) to prevent race conditions during concurrent order processing.
* **Data Isolation:** Implemented strict DTO mappings to prevent exposing the PostgreSQL database entities to the REST API layer.
* **Custom Queries:** Utilized JPA derived queries for efficient price-time priority sorting.

### How to Run
1. Update `application.properties` with your local PostgreSQL password.
2. Create a database named `trading` in your local PostgreSQL instance.
3. Run the Spring Boot application to automatically generate the database tables and start the server on port 8080.
