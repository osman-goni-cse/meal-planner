# DSi Meal Planner

A Spring Boot web application for meal planning with Google OAuth2 authentication.

## Features

- Google OAuth2 authentication
- Meal planning and management
- Dish management
- Weekly meal planning
- Responsive UI with Tailwind CSS

## Prerequisites

- Java 17 or higher
- Maven
- Google OAuth2 credentials

## Setup

### 1. Google OAuth2 Configuration

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google+ API
4. Go to "Credentials" and create OAuth 2.0 Client ID
5. Set authorized redirect URIs to: `http://localhost:8080/login/oauth2/code/google`

### 2. Environment Variables

**Option A: Using the provided script**
```bash
./set-env-vars.sh
```

**Option B: Manual setup**
```bash
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
```

**Option C: Permanent setup**
Add to your `~/.bashrc` or `~/.zshrc`:
```bash
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
```

### 3. Database Setup

The application uses H2 in-memory database by default. For production, configure a persistent database in `application.properties`.

### 4. Run the Application

```bash
./mvnw spring-boot:run
```

The application will be available at `http://localhost:8080`

## Security

- Google OAuth2 credentials are now stored as environment variables
- Never commit credentials to version control
- Use the provided `set-env-vars.sh` script or set environment variables manually

## Development

- Frontend: Thymeleaf templates with Tailwind CSS
- Backend: Spring Boot with Spring Security
- Database: H2 (in-memory) with JPA/Hibernate

## License

This project is for internal use at DSi. 