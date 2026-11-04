# DevConnect Backend

REST API for DevConnect, built with Java and Spring Boot.

**Live frontend:** https://devconnect-frontend-one.vercel.app
**Frontend repo:** https://github.com/Vivek-KA/devconnect-frontend

## Tech stack

Java 25, Spring Boot, Spring Security, Spring Data JPA (Hibernate), PostgreSQL, JWT (jjwt), Maven, Docker

## API endpoints

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/users` | Public (sign up) |
| POST | `/api/auth/login` | Public, returns a JWT |
| GET | `/api/posts`, `/api/posts/{id}` | Public |
| POST | `/api/posts` | Logged in |
| PUT, DELETE | `/api/posts/{id}` | Post author only |
| GET | `/api/posts/{id}/comments` | Public |
| POST | `/api/posts/{id}/comments` | Logged in |
| DELETE | `/api/posts/{id}/comments/{commentId}` | Comment author only |
| GET | `/api/posts/{id}/likes` | Public (like count) |
| POST | `/api/posts/{id}/likes` | Logged in (toggles like) |

## Design notes

- Passwords are hashed with BCrypt and never returned in responses
- Stateless authentication: a JWT filter validates the token on each request
- Ownership checks on edit and delete, enforced on the server
- A database unique constraint prevents duplicate likes
- Client-supplied ids are ignored to prevent overwriting existing records
- Secrets come from environment variables, never from the code

## Run locally

1. Install JDK 25 and create a PostgreSQL database (for example, a free Neon database).
2. Create `src/main/resources/application-local.properties` (it is git-ignored):

```properties
spring.datasource.url=jdbc:postgresql://HOST/DBNAME?sslmode=require
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
jwt.secret=A_LONG_RANDOM_STRING_OF_48_PLUS_CHARACTERS
```

3. Run with the `local` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Deployment

Deployed on Render using the included `Dockerfile`. Environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS`.
