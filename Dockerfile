
# ---- Build frontend ----
FROM node:22-alpine AS frontend-build
WORKDIR /frontend

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

# ---- Build backend ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src

# Copy React build into Spring Boot static resources
COPY --from=frontend-build /frontend/dist/ /app/src/main/resources/static/

# Use environment-driven configuration; do not package local secrets.
RUN rm -f src/main/resources/application.properties \
 && cp src/main/resources/application.properties.example src/main/resources/application.properties

RUN mvn -B -q package -Dmaven.test.skip=true

# ---- Run application ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --uid 1001 appuser
COPY --from=build /app/target/*.jar app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-XX:MaxRAMPercentage=60", "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1", "-Xss512k", "-jar", "app.jar"]
