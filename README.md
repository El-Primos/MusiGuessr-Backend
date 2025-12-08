# MusiGuessr Backend

Backend API for MusiGuessr - A song guessing game where players guess songs from short previews and compete on leaderboards.

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.6**
- **PostgreSQL**
- **AWS S3** (for song storage)
- **Maven** (build tool)
- **Flyway** (for database migration)

---

## How to Run the Project

### Prerequisites
- Java 21 installed
- Maven installed (or use Maven wrapper included in project)
- PostgreSQL running locally or accessible remotely

### 1. Clone the Repository
```bash
git clone https://github.com/El-Primos/MusiGuessr.git
cd MusiGuessr/MusiGuessr-Backend
```

### 2. Configure Database
```bash
# Create image with dockerfile
docker build -t musiguessr-db:latest db

# Create container and run db
docker run -d --name musiguessr-db -e POSTGRES_USER=app_user -e POSTGRES_PASSWORD=app_pass -e POSTGRES_DB=musiguessr -p 5432:5432 musiguessr-db:latest

# Stop db
docker stop musiguessr-db

# Start db
docker start musiguessr-db
```

While running app, don't forget to init constants. Those will configure the flyway, check the
/MusiGuessr-Backend/src/main/resources/application.properties
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/musiguessr
export SPRING_DATASOURCE_USERNAME=app_user
export SPRING_DATASOURCE_PASSWORD=app_pass
export SPRING_FLYWAY_USER=app_user
export SPRING_FLYWAY_PASSWORD=app_pass

./mvnw spring-boot:run
```

An example query to connect:
```sql
SELECT * FROM musiguessr_schema.admins;
```

### 3. Create Migration SQL Files
Flyway migration scripts go in `src/main/resources/db/migration`:

```
V1__create_user_table.sql
V2__add_email_to_user.sql
```

Example content:

```sql
CREATE TABLE users (
    id PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100)
);
```

Flyway migrations run automatically when Spring Boot starts

### 4. Compile the Project
```bash
mvn clean install
```

Or using Maven wrapper (no Maven installation needed):
```bash
./mvnw clean install        # Linux/Mac
.\mvnw.cmd clean install    # Windows
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

Or using Maven wrapper:
```bash
./mvnw spring-boot:run       # Linux/Mac
.\mvnw.cmd spring-boot:run   # Windows
```

The API will start at: `http://localhost:8080`

### 6. Run Tests
```bash
mvn test
```

---

## Credentials & Environment Variables

⚠️ **Do NOT commit any credentials or sensitive information** (e.g., database passwords, API keys) to this repository. Instead, use environment variables to configure your application securely.

Spring Boot allows you to reference environment variables in your `application.properties` files:

~~~properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

spring.flyway.user=${SPRING_FLYWAY_USER}
spring.flyway.password=${SPRING_FLYWAY_PASSWORD}
~~~

### Environment Variables

Set the following variables in your system before running the application:

| Property | Environment Variable Name | Description |
|----------|--------------------------|-------------|
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | JDBC URL for application database |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | Database username for the application |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | Database password for the application |
| `spring.flyway.user` | `SPRING_FLYWAY_USER` | Database username used for Flyway migrations |
| `spring.flyway.password` | `SPRING_FLYWAY_PASSWORD` | Password for Flyway migrations |

### Setting Environment Variables

**Linux / macOS (bash/zsh):**
~~~bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/musiguessr
export SPRING_DATASOURCE_USERNAME=your_app_user
export SPRING_DATASOURCE_PASSWORD=your_app_password
export SPRING_FLYWAY_USER=flyway_user
export SPRING_FLYWAY_PASSWORD=flyway_password

./mvnw spring-boot:run
~~~

**Windows (PowerShell):**
~~~powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/musiguessr"
$env:SPRING_DATASOURCE_USERNAME="your_app_user"
$env:SPRING_DATASOURCE_PASSWORD="your_app_password"
$env:SPRING_FLYWAY_USER="flyway_user"
$env:SPRING_FLYWAY_PASSWORD="flyway_password"

.\mvnw spring-boot:run
~~~

This approach ensures that no credentials are stored in the repository, while Spring Boot can still read them through `application.properties` or other profile-specific property files.

---

## Git Workflow & Best Practices

### Branch Types

- **`main`** - Production-ready code (protected, no direct commits)
- **`develop`** - Integration branch for features
- **`feature/*`** - New features (e.g., `feature/user-authentication`)
- **`bugfix/*`** - Bug fixes (e.g., `bugfix/login-error`)
- **`hotfix/*`** - Urgent production fixes

### Basic Git Workflow

#### 1. Create a New Branch
Always create a branch from `develop`:
```bash
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name
```

#### 2. Branch Naming Convention
Use descriptive names with prefixes:
```bash
feature/jwt-authentication
feature/leaderboard-api
bugfix/scoring-calculation
bugfix/database-connection
hotfix/critical-security-issue
```

#### 3. Make Changes and Commit
```bash
# Stage your changes
git add .

# Commit with a clear message
git commit -m "Add user registration endpoint"
```
#### 4. Push Your Branch
```bash
git push origin feature/your-feature-name
```

#### 5. Create a Pull Request (PR)
1. Go to GitHub repository
2. Click "Pull Requests" → "New Pull Request"
3. Set base branch: `develop`, compare branch: `feature/your-feature-name`
4. Add a clear title and description
5. Request review from team members
6. Wait for approval before merging

#### 6. Switch Between Branches
```bash
# View all branches
git branch -a

# Switch to existing branch
git checkout develop
git checkout feature/another-feature

# Switch and pull latest changes
git checkout develop
git pull origin develop
```

#### 7. Keep Your Branch Updated
```bash
# While on your feature branch
git checkout feature/your-feature-name
git pull origin develop
```

### Quick Command Reference

```bash
# Check current branch and status
git status

# See commit history
git log --oneline

# Discard local changes (careful!)
git checkout -- .

# Delete local branch (after merge)
git branch -d feature/your-feature-name

# Delete remote branch
git push origin --delete feature/your-feature-name
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

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
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

