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

```sh
curl -i http://localhost:8080/api/animals/dog/sound
curl -i http://localhost:8080/api/animals/cat/sound
```

`GET /api/animals/{animal}/sound` returns `200 OK` and
`text/plain;charset=UTF-8`. The body is exactly `멍멍` for `dog` or `야옹`
for `cat`, encoded as UTF-8 without quotes, JSON wrapping, extra spaces, or a
trailing newline. No request body is needed.

Only the lowercase identifiers `dog` and `cat` are supported. Other values,
including `Dog`, `CAT`, `강아지`, `고양이`, and values with surrounding whitespace,
return `404 Not Found`. A missing identifier (`/api/animals/sound`) also returns
404. POST returns `405 Method Not Allowed`, and requesting only
`Accept: application/json` returns `406 Not Acceptable`. See the error response contract below.

```sh
curl -i -X POST http://localhost:8080/api/animals/turtle/jump-sound
curl -i -X POST http://localhost:8080/api/animals/rabbit/jump-sound
```

`POST /api/animals/{animal}/jump-sound` returns `200 OK` and
`text/plain;charset=UTF-8`. This implementation uses text movement sounds:
exactly `엉금엉금` for `turtle` and `깡충깡충` for `rabbit`. These values were
chosen because the plan left the exact strings unspecified. Responses contain
UTF-8 bytes without quotes, JSON wrapping, extra spaces, or a trailing newline.
The endpoint immediately returns the sound without storing state or playing audio.

No request body or Content-Type is required. Any supplied body is ignored,
regardless of its Content-Type. Only lowercase `turtle` and `rabbit` are accepted;
other identifiers, Korean aliases, and surrounding whitespace return `404 Not Found`.
A missing identifier (`/api/animals/jump-sound`) also returns 404.
GET and other unsupported methods return `405 Method Not Allowed`.
`Accept: application/json` returns `406 Not Acceptable`.
See the error response contract below.

## Error responses

Invalid greetings and missing or empty required bodies return `400` with
`{"message":"잘못된 요청입니다."}`. Unsupported animal identifiers return `404`
with `{"message":"요청한 대상을 찾을 수 없습니다."}`. Unexpected application
exceptions return `500` with `{"message":"서버 오류가 발생했습니다."}`; their causes
are logged on the server and internal details are excluded from the response.

These responses use `application/json` and UTF-8 when `Accept` is absent or
allows JSON. Requests that exclude JSON retain the error status with an empty
body. Existing HTTP status codes and exception headers are preserved.
Framework errors such as 405, 406, 415, and unmapped paths retain Spring's
handling; their bodies are outside this JSON contract. In particular, the API's
plain-text mappings still reject JSON-only `Accept` requests with 406 before
invoking a controller. To accept both success and error bodies, use
`Accept: text/plain, application/json` or omit `Accept`. Success responses remain
plain text as described above. No additional dependencies or error codes are used.

## Test and package

```sh
./mvnw clean verify
java -jar target/hello-api-0.0.1-SNAPSHOT.jar
```

Tests check the HTTP status, exact response bytes, plain-text content type,
UTF-8 animal sounds, unsupported inputs, method and content negotiation errors,
and application context startup with all controllers registered.

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
(three requests per run). The development lead is direct and pragmatic, the
planning lead is curious and user-focused, and the operations lead is calm and
practical about failure scenarios. Feedback is scoped to this PR, with at most
two points and a target of 300 characters (hard limit: 400 body characters).
No findings means `OK`. An overlong draft is rewritten once; if still too long,
the job fails without publishing it. A result is discarded if the PR changes or
closes during generation.

Development PRs from `codex/implement-<issue number>` receive only a
`[개발 팀장]` code review comment, based on changed code diffs (including deletions).
The implementation workflow uploads a `development-pr` artifact after creating
the PR. The review workflow consumes it even if the implementation run ultimately
fails its test-result check. No artifact means no review. Human updates and manual
review runs also support these development branches. The workflow retains its
existing **AI Planning Review** name in Actions.

The script executes only from the default branch and reads plan contents at the
PR head commit through the GitHub API. It sends the PR title, description, and
plan contents with patches (or code diffs for development PRs) to the OpenAI
Responses API; it does not execute PR code. Reviews are limited to 20 files and
120 KB total. Code reviews exclude `docs/`, `.github/`, `.agents/`, and `.codex/`;
missing code patches fail the job rather than producing an unsupported review.
API failures or incomplete responses fail the job without posting a comment.

Run the review tests without credentials or external API calls:

```sh
node --test scripts/ai-review.test.mjs
```
