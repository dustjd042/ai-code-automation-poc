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

## AI planning review

`AI Planning Review` runs after a successful `Codex Issue Planning` run on
the default branch. It downloads that run's `planning-pr` artifact, reads
the PR number, and reviews changed Markdown files under `docs/plan/`.
Same-repository planning PRs opened or updated by a person also trigger reviews.
Fork PRs and closed PRs are skipped. A run without a planning artifact is skipped.

Before using it:

- Merge the workflow and `scripts/ai-review.mjs` into the default branch.
- Set `OPENAI_API_KEY` in the `test` environment (or as a repository secret).
  Environment approval rules still apply.
- Optionally set `OPENAI_REVIEW_MODEL` as an Actions variable; the default is
  `gpt-5-mini`. The key must have access to the selected model.

For an existing PR, use **Actions → AI Planning Review → Run workflow** on the
default branch and enter `pr_number`. Repeated reviews update the bot's existing
comments, one each beginning with `[개발 팀장]`, `[기획 팀장]`, and
`[운영 팀장]`. Each role uses its own review criteria and one model request
(three requests per run). A result is discarded if the PR changes or closes during generation.

The script executes only from the default branch and reads plan contents at the
PR head commit through the GitHub API. It sends the PR title and changed plan
contents to the OpenAI Responses API; it does not execute PR code. Reviews are
limited to 20 documents and 120 KB total and do not inspect application code.
API failures or incomplete responses fail the job without posting a comment.

Run the review tests without credentials or external API calls:

```sh
node --test scripts/ai-review.test.mjs
```
