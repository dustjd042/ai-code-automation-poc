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

```sh
curl -i http://localhost:8080/api/hello \
  -H 'Content-Type: text/plain; charset=UTF-8' \
  --data-binary 'こんにちは'
```

`POST /api/hello` accepts exactly the Japanese greeting `こんにちは` as a
UTF-8 plain-text body. It returns `200 OK`, `text/plain;charset=UTF-8`, and
exactly `Hello` without quotes, JSON wrapping, spaces, or a trailing newline.
Missing or empty bodies and other strings return `400 Bad Request`, including
`고니찌와`, `안녕`, and input with surrounding whitespace or newlines. Input is
not trimmed or Unicode-normalized. Unsupported content types such as
`application/json` return `415 Unsupported Media Type`.

```sh
curl -i http://localhost:8080/api/healthcheck
```

`GET /api/healthcheck` returns status `200 OK`, a `text/plain` content type,
and exactly `healthcheck` without quotes, JSON wrapping, spaces, or a trailing
newline. No query parameters or request body are needed. This endpoint provides
a basic liveness check and does not check databases or external services.

## Test and package

```sh
./mvnw clean verify
java -jar target/hello-api-0.0.1-SNAPSHOT.jar
```

Tests check the HTTP status, exact response bytes, plain-text content type,
and application context startup with both controllers registered.
