# MusiGuessr Backend

Backend API for MusiGuessr - A song guessing game where players guess songs from short previews and compete on leaderboards.

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.6**
- **PostgreSQL**
- **AWS S3** (for song storage)
- **Maven** (build tool)
- **Flyway** (for database migration)
- **Docker & Docker Compose** (containerization)

---

## Prerequisites

Before running the project, ensure you have the following installed:

- **Docker Desktop** (includes Docker Compose)
- **Java 21** (Required for local development)
- **Maven** (Optional, wrapper included in the project)

---

## How to Run the Project

You can run the project in two ways:
1. **Full Docker Setup** (Recommended for quick start - Runs both App & DB)
2. **Local Development** (Runs DB in Docker, App locally for debugging)

### 1. Clone the Repository
```bash
git clone [https://github.com/El-Primos/MusiGuessr-Backend.git](https://github.com/El-Primos/MusiGuessr-Backend.git)
cd MusiGuessr/MusiGuessr-Backend
```

### 2. Configure Environment Variables
Create a `.env` file in the project root (`/MusiGuessr-Backend/.env`). Docker Compose will automatically read this file.

**`.env` Example:**
```properties
# Database Configuration (Required)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/musiguessr
SPRING_DATASOURCE_USERNAME=app_user
SPRING_DATASOURCE_PASSWORD=app_pass

# Flyway Migration Configuration (Required)
SPRING_FLYWAY_USER=app_user
SPRING_FLYWAY_PASSWORD=app_pass

# JWT Authentication (Required)
# Generate a strong secret key (minimum 256 bits recommended)
# You can generate one using: openssl rand -base64 32
JWT_SECRET_KEY=your-secret-key-here-change-this-in-production

# AWS S3 Configuration (Required for file uploads)
AWS_ACCESS_KEY_ID=your-aws-access-key-id
AWS_SECRET_ACCESS_KEY=your-aws-secret-access-key

# Optional Configuration (has defaults if not specified)
# SPRING_APPLICATION_NAME=MusiGuessr Backend
# JWT_EXPIRATION=3600000           # 1 hour in milliseconds
# JWT_REFRESH_EXPIRATION=604800000 # 7 days in milliseconds
```

---

### Option 1: Run with Docker Compose (Backend + Database)

This method builds the application and starts both the PostgreSQL database and the Spring Boot API in isolated containers.

```bash
# Build and start services
docker-compose up --build -d

# View logs (to ensure everything started correctly)
docker-compose logs -f

# Stop services
docker-compose down
```

- **API URL:** `http://localhost:8080`
- **Database:** Running on port `5432`

---

### Option 2: Local Development (Java Locally + DB in Docker)

Use this method if you are developing features and want to debug the Java code in your IDE.

#### Step 1: Start Database Only
Use Docker Compose to start only the database service.

```bash
docker-compose up -d db
```

#### Step 2: Run Spring Boot Application

**Method A: Using .env (Recommended)**
If you created the `.env` file, simply run:
```bash
./mvnw spring-boot:run       # Linux/Mac
.\mvnw.cmd spring-boot:run   # Windows
```

**Method B: Manual Export (If not using .env)**
If you prefer not to use a `.env` file, export the variables manually before running:

**Linux / macOS (bash/zsh):**
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/musiguessr
export SPRING_DATASOURCE_USERNAME=app_user
export SPRING_DATASOURCE_PASSWORD=app_pass
export SPRING_FLYWAY_USER=app_user
export SPRING_FLYWAY_PASSWORD=app_pass
export JWT_SECRET_KEY=your-secret-key-here
export AWS_ACCESS_KEY_ID=your-aws-access-key-id
export AWS_SECRET_ACCESS_KEY=your-aws-secret-access-key

./mvnw spring-boot:run
```

**Windows (PowerShell):**
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/musiguessr"
$env:SPRING_DATASOURCE_USERNAME="app_user"
$env:SPRING_DATASOURCE_PASSWORD="app_pass"
$env:SPRING_FLYWAY_USER="app_user"
$env:SPRING_FLYWAY_PASSWORD="app_pass"
$env:JWT_SECRET_KEY="your-secret-key-here"
$env:AWS_ACCESS_KEY_ID="your-aws-access-key-id"
$env:AWS_SECRET_ACCESS_KEY="your-aws-secret-access-key"

.\mvnw spring-boot:run
```

---

## Database Migrations (Flyway)

Flyway migration scripts are located in `src/main/resources/db/migration`.
Migrations run automatically when the application starts.

**Example Migration File:** `V1__create_user_table.sql`
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100)
);
```

To verify the database connection manually
```sql
SELECT * FROM musiguessr_schema.users;
```

---

## Testing & Compilation

To compile the project and run unit tests without starting the server:

```bash
mvn clean install
mvn test
```

Or using Maven wrapper (no Maven installation needed):
```bash
./mvnw clean install    # Linux/Mac
./mvnw test

.\mvnw clean install    # Windows
.\mvnw test
```

---

## Git Workflow & Best Practices

### Branch Types
- **`main`** - Production-ready code (protected, no direct commits)
- **`develop`** - Integration branch for features
- **`feat/*`** - New features (e.g., `feat/user-authentication`)
- **`fix/*`** - Bug fixes (e.g., `fix/login-error`)
- **`hotfix/*`** - Urgent production fixes

### Basic Workflow
1.  **Create Branch:** `git checkout -b feat/my-feature`
2.  **Commit:** `git commit -m "Add login endpoint"`
3.  **Push:** `git push origin feat/my-feature`
4.  **Pull Request:** Open a PR to `develop`.

### Quick Reference
```bash
# Check status
git status

# Clean uncommitted changes
git checkout -- .

# Delete local branch
git branch -d feature/my-feature
```

### PR Review Checklist

Before creating a PR, make sure:
- [ ] Code compiles without errors
- [ ] Tests pass (`mvn test`)
- [ ] Code follows project structure
- [ ] Commit messages are clear
- [ ] No sensitive data (passwords, API keys) committed

---

## Project Structure

```
src/main/java/com/musiguessr/backend/
├── controller/      # REST API endpoints
├── service/         # Business logic
├── repository/      # Database access
├── model/           # Entity classes
├── config/          # Configuration classes
├── security/        # JWT & Security
└── dto/             # Data Transfer Objects

src/main/resources/
├── application.properties    # Configuration
├── application-dev.properties
└── db/
    └── migration/            # Flyway migration SQL scripts
        ├── Vx__your_migration_file.sql
        └── ...               # Additional migration files
```

---

## API Documentation

This project uses **Swagger (OpenAPI 3)** for API documentation and testing.

Once the application is running, access the interactive API documentation at:

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

**Swagger UI** lets you:
- View all available API endpoints
- See request/response schemas
- Test endpoints directly from your browser
- Understand required parameters and authentication

No additional configuration needed - Swagger is automatically enabled when you run the application!

---

## Troubleshooting

**Port 8080 already in use:**
```properties
# Change port in application.properties
server.port=8081
```

**Database connection error:**
- Check PostgreSQL is running
- Verify credentials in `application.properties`
- Ensure database `musiguessr` exists

**Build errors:**
- Run `mvn clean install -U` to update dependencies
- Check Java version: `java -version` (should be 21)