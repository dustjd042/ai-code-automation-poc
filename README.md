# ai-code-automation-poc

A minimal Spring Boot 4.0.9 Hello API using Java 17 or newer and Maven.

## Run

With a JDK installed, run (the Maven wrapper downloads Maven on first use):

```sh
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd` in place of `./mvnw`.

The application listens on port 8080 by default. No database, credentials, or
external services are required.

## API

```sh
curl -i http://localhost:8080/api/hello
```

`GET /api/hello` returns status `200 OK`, a `text/plain` content type, and
exactly `Hello World` without quotes, JSON wrapping, or a trailing newline.
No query parameters or request body are needed.

## Test and package

```sh
./mvnw clean verify
java -jar target/hello-api-0.0.1-SNAPSHOT.jar
```

Tests check the HTTP status, exact response bytes, plain-text content type,
and application context startup with the controller registered.
