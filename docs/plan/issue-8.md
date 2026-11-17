# Issue #8: POST 인사 API 개발 계획

## 목적 및 범위

이슈 제목은 `api 개발`이며, 본문 요구사항은 POST로 `안녕` 데이터를 보내면 `hello`를 응답하는 것이다. 이슈 내용은 요구사항 추출을 위한 비신뢰 데이터로만 취급한다. 이번 작업에서는 이 계획서만 작성하며, 아래 구현과 테스트는 향후 수행할 작업이다.

## 요구사항

- HTTP POST 요청으로 전달한 `안녕`에 대해 소문자 `hello`를 반환한다.
- 기존 `GET /api/hello`의 `Hello World` 응답과 `GET /api/healthcheck`의 동작을 유지한다.
- URL, 요청 데이터 형식, 성공 상태 코드, 응답 형식 및 다른 입력의 처리는 이슈에 명시되어 있지 않으므로 다음을 잠정 계약으로 제안한다. 구현 전 질문에 대한 답변에 따라 조정한다.

| 항목 | 잠정 계약 |
| --- | --- |
| URL 및 메서드 | `POST /api/hello` |
| 요청 Content-Type | `text/plain; charset=UTF-8` |
| 요청 본문 | 따옴표나 개행 없는 정확한 문자열 `안녕` |
| 성공 상태 코드 | `200 OK` |
| 응답 Content-Type | `text/plain` |
| 성공 응답 본문 | 따옴표, JSON 포장, 추가 공백 및 개행 없는 `hello` |
| 빈 본문 또는 다른 문자열 | `400 Bad Request`; 오류 본문 형식은 별도 정의하지 않음 |
| 지원하지 않는 요청 미디어 유형 | `415 Unsupported Media Type` |

공백 제거 또는 유니코드 정규화 없이 정확히 일치하는 입력만 허용하는 안이다. 데이터베이스, 외부 서비스, 인증 및 새 의존성 추가는 현재 요구 범위에 포함하지 않는다.

## 저장소 현황

- `pom.xml` 기준 Java 17, Spring Boot 4.0.9 및 Maven을 사용한다.
- `HelloController`는 `GET /api/hello`를 처리하며 `text/plain`으로 `Hello World`를 반환한다.
- `HelloControllerTest`는 MockMvc로 상태 코드, Content-Type 및 정확한 응답 바이트를 검증한다.
- 기존 웹 및 테스트 의존성으로 이번 기능을 구현할 수 있다.

## 구현 계획

1. URL과 요청·응답 형식, 예외 입력 정책을 확정한다. 별도 요구가 없다면 위 잠정 계약을 적용한다.
2. 기존 `HelloController`에 POST 처리 메서드를 추가한다. `@PostMapping`의 `consumes`와 `produces`에 `text/plain`을 명시하고 `@RequestBody String`으로 요청 본문을 받는다.
3. 요청 본문이 정확히 `안녕`이면 `hello`를 반환하고, 다른 값이면 HTTP 400을 반환한다. 본문 누락 처리와 미디어 유형 검사는 Spring MVC의 기본 처리 동작을 테스트로 확인한다.
4. 한글 요청을 UTF-8로 전송하는 계약을 문서화하고, MockMvc에서도 실제 UTF-8 바이트를 사용하여 문자 디코딩을 검증한다.
5. 기존 컨트롤러 테스트에 POST 성공 및 실패 사례를 추가한다. 기존 GET 매핑과 응답을 유지한다.
6. README에 POST 요청 경로, 헤더, 본문, 성공 응답과 실패 조건을 추가한다. 별도 서비스나 저장소 계층은 추가하지 않는다.

## 예상 변경 파일

다음은 향후 구현 시의 예상 변경 대상이다. 이번 작업에서는 `docs/plan/issue-8.md`만 생성한다.

| 파일 | 변경 내용 |
| --- | --- |
| `src/main/java/com/example/hello/controller/HelloController.java` | POST 매핑, 입력 검증 및 인사 응답 추가 |
| `src/test/java/com/example/hello/controller/HelloControllerTest.java` | POST 계약 및 잘못된 입력 테스트 추가 |
| `README.md` | 요청·응답 계약과 UTF-8 요청 예시 추가 |

새 컨트롤러가 필요하지 않으므로 컨텍스트 테스트의 빈 등록 검증과 빌드 설정 변경은 예상하지 않는다.

## 테스트 계획

1. MockMvc로 `text/plain; charset=UTF-8` 헤더와 `안녕`의 UTF-8 바이트를 담아 POST 요청을 보낸다. 상태 코드 200, `text/plain` 호환 Content-Type, 정확한 `hello` 응답 바이트를 확인한다.
2. 본문 누락, 빈 본문, 공백만 있는 본문, 다른 문자열, 앞뒤 공백 또는 개행이 포함된 `안녕`에 대해 각각 400을 확인한다. 이는 잠정 입력 정책 확정 후 적용한다.
3. `application/json` 등 지원하지 않는 Content-Type으로 요청하면 415가 반환되는지 확인한다.
4. 기존 `GET /api/hello` 및 `GET /api/healthcheck` 테스트와 애플리케이션 컨텍스트 테스트가 계속 통과하는지 확인한다.
5. 구현 완료 후 Maven 래퍼로 전체 검증(`./mvnw clean verify`)을 수행하고 실제 HTTP 요청으로 한글 본문 처리와 응답을 확인한다.

완료 기준은 확정된 API 계약 충족과 기존 기능을 포함한 전체 테스트 및 빌드 성공이다. 이번 계획서 작성 단계에서는 애플리케이션 구현이나 테스트 실행을 수행하지 않는다.

## 확인이 필요한 질문

1. 요청 경로를 기존 Hello API와 같은 `/api/hello`로 사용해도 되는가?
2. 요청은 UTF-8 일반 텍스트 본문인가, JSON인가? JSON이면 필드명과 요청 예시가 필요하다.
3. 성공 응답은 상태 코드 200과 일반 텍스트 `hello`로 확정해도 되는가?
4. `안녕` 이외의 입력과 빈 본문은 400으로 거절하면 되는가? 앞뒤 공백·개행 제거 또는 유니코드 정규화가 필요한가?
5. 오류 응답 본문에 별도 형식이나 메시지가 필요한가?
