# Smart Campus Management System

A full-stack web application built to support campus management workflows through a Spring Boot REST API and a React-based frontend.

## 📸 Application Screenshots

### Login Page
![Login Page](screenshots/login-page.png)

### Dashboard
![Dashboard](screenshots/dashboard.png)

## Tech Stack

**Backend**

- Java 21

- Spring Boot

- Spring Security

- Spring Data JPA

- MySQL

- JWT-based authentication

- Maven

**Frontend**

- React 19

- Vite

- React Router

**Testing & API Documentation**

- JUnit / Spring Boot testing

- H2 in-memory database for tests

- Springdoc OpenAPI

## ✨ Key Features

- **Authentication:** Login interface with backend authentication support.
- **REST API:** Backend endpoints built using Spring Boot.
- **Security:** Spring Security and JWT-based authentication.
- **Frontend Routing:** React Router for navigating between application pages.
- **Database Integration:** MySQL persistence using Spring Data JPA.
- **Testing:** Backend tests using JUnit, Spring Boot testing support, and an H2 in-memory database.
- **Configuration:** Environment-specific application configuration.

*Note: Keep each feature listed above only if it is implemented and working in the current version of the project.*
## Project Structure

```text

smart-campus/

├── src/                 # Spring Boot backend and tests

├── frontend/            # React + Vite frontend

├── pom.xml              # Maven configuration

├── mvnw

├── mvnw.cmd

└── README.md

```

## Prerequisites

- Java 21

- Node.js and npm

- MySQL for local backend development

## Run Locally

### 1. Configure the backend

Create a local configuration file at:

`src/main/resources/application.properties`

This file is excluded from Git. Configure the database connection, a strong JWT signing secret, and any optional services using your local environment.

**Never commit real passwords, API keys, or production secrets.** Do not use example JWT secrets or default database passwords in production.

### 2. Start the backend

From the project root, run:

```powershell

.\\mvnw.cmd spring-boot:run

```

The backend uses the configuration supplied in `application.properties`.

### 3. Start the frontend

Open a second terminal:

```powershell

cd frontend

npm ci

npm run dev

```

Open the local URL printed by Vite. The development server is configured to use port `5173`.

### 4. Run backend tests

From the project root:

```powershell

.\\mvnw.cmd test

```

The test configuration uses an H2 in-memory database.

## Frontend Scripts

Run these commands from the `frontend/` directory.

| Command | Purpose |

|---|---|

| `npm run dev` | Start the development server |

| `npm run build` | Create a production frontend build |

| `npm run lint` | Run Oxlint |

| `npm run preview` | Preview the production build locally |

## Configuration and Security

- Keep `application.properties` and `.env` files out of version control.

- Supply production credentials through environment variables or a secrets manager.

- Use a securely generated JWT secret.

- Configure CORS for the intended frontend origin.

- Review database migration strategy, authentication settings, and API documentation exposure before deployment.

## Project Status

This project is under active development. Features and deployment instructions will be updated as implementation progresses.
