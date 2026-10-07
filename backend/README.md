# ⚙️ MediPulse Backend

This directory contains all the **Backend** source code, database scripts, and Maven configuration for the MediPulse Online Pharmacy Platform.

---

## 📁 Directory Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/onlinepharmacy/
│   │   │   ├── config/          # Security, WebMVC & Data Initializer configs
│   │   │   ├── controller/      # REST API Controllers & View Navigation
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── model/           # JPA Entities (User, Medicine, Order, Cart, etc.)
│   │   │   ├── repository/     # Spring Data JPA Repositories
│   │   │   └── service/        # Business Services
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/          # Static Web Assets copy
│   │       └── templates/       # HTML View Templates copy
├── Database_schema.sql          # PostgreSQL / Supabase Schema definition
├── Database_Design.md           # Database Architecture & Entity Specifications
├── pom.xml                      # Maven dependencies and configuration
├── mvnw / mvnw.cmd              # Maven wrapper scripts
└── README.md                    # Backend documentation
```

---

## 🛠️ Technology Stack

- **Java 21**
- **Spring Boot 3.x**
  - **Spring Web**: RESTful APIs & Controller endpoints
  - **Spring Data JPA / Hibernate**: Object-Relational Mapping
  - **Spring Security**: Password encoding (BCrypt) & authentication controls
  - **Spring Mail**: SMTP email verification & notifications
- **Database**: PostgreSQL (Supabase Cloud / Local Instance)

---

## 🚀 Running the Backend Server

### 1. Configure Environment
Update `src/main/resources/application.properties` with your database credentials and configuration settings.

### 2. Compile & Run
Execute the Maven wrapper from the `backend/` directory:

```powershell
# Compile the application
.\mvnw.cmd compile

# Start the Spring Boot application
.\mvnw.cmd spring-boot:run
```

The server will start at **http://localhost:8081**.
