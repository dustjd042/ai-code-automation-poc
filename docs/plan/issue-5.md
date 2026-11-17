# Issue #5: 헬스 체크 API 개발 계획

## 목적 및 범위

애플리케이션의 HTTP 응답 여부를 확인할 수 있는 헬스 체크 API를 추가한다. 이슈 제목은 `Hello APIhealthcheck URL 개발`이며, 이슈 본문은 요구사항을 추출하기 위한 비신뢰 데이터로만 취급한다. 이번 작업의 산출물은 이 계획서이며, 아래 구현과 테스트는 향후 수행할 작업이다.

## 요구사항

| 항목 | 요구사항 |
| --- | --- |
| URL | `/api/healthcheck` |
| HTTP 메서드 | `GET` |
| 성공 상태 코드 | `200 OK` |
| 응답 본문 | `healthcheck` |

- 응답은 소문자 `healthcheck`를 따옴표, JSON 포장, 추가 공백 및 개행 없이 반환하는 것으로 계획한다.
- 이슈에 응답 형식은 명시되어 있지 않으므로 기존 Hello API와 동일하게 `text/plain`을 사용한다는 전제를 둔다.
- 쿼리 파라미터와 요청 본문 없이 호출할 수 있도록 한다.
- 기존 `GET /api/hello`의 동작을 유지한다.
- 데이터베이스나 외부 서비스의 상태 검사, 인증 추가, Actuator 도입은 현재 요구사항 범위에 포함하지 않는다. 이 API는 고정 문자열 응답을 통한 기본적인 생존 확인 용도다.

## 저장소 현황

- `pom.xml` 기준 Java 17, Spring Boot 4.0.9 및 Maven을 사용한다.
- `HelloController`가 `GET /api/hello`에 대해 `text/plain` 응답을 제공한다.
- `HelloControllerTest`는 MockMvc로 상태 코드, 응답 형식 및 정확한 응답 바이트를 검증한다.
- `HelloApplicationTests`는 애플리케이션 컨텍스트 로딩과 기존 컨트롤러 등록을 검증한다.
- 기존 웹 및 테스트 의존성으로 구현할 수 있으므로 빌드 설정 변경은 예상하지 않는다.

## 구현 계획

1. 기존 컨트롤러와 같은 패키지에 `HealthcheckController`를 추가하여 헬스 체크 요청을 처리한다.
2. `@RestController`와 `@GetMapping`으로 `/api/healthcheck`의 GET 요청을 매핑하고, 응답 형식을 `MediaType.TEXT_PLAIN_VALUE`로 명시한다.
3. 메서드에서 고정 문자열 `healthcheck`를 반환한다. 별도 서비스 계층이나 외부 상태 조회는 추가하지 않는다.
4. 기존 테스트 패턴에 맞춰 `HealthcheckControllerTest`를 추가하고 API 계약을 검증한다.
5. 컨텍스트 테스트에 새 컨트롤러 등록 검증을 추가하여 실제 컴포넌트 스캔 대상인지 확인한다.
6. README에 새 엔드포인트와 성공 응답 예시를 추가한다.

## 예상 변경 파일

아래 목록은 향후 구현 시의 예상 변경 대상이다. 이번 작업에서는 이 계획서만 생성한다.

| 파일 | 변경 내용 |
| --- | --- |
| `src/main/java/com/example/hello/controller/HealthcheckController.java` | 헬스 체크 컨트롤러 신규 작성 |
| `src/test/java/com/example/hello/controller/HealthcheckControllerTest.java` | MockMvc 기반 API 계약 테스트 신규 작성 |
| `src/test/java/com/example/hello/HelloApplicationTests.java` | 새 컨트롤러 빈 등록 검증 추가 |
| `README.md` | 헬스 체크 URL, 메서드, 상태 코드 및 응답 본문 안내 |

## 테스트 계획

1. `@WebMvcTest(HealthcheckController.class)`와 MockMvc로 파라미터 및 본문 없이 `GET /api/healthcheck`를 호출한다.
   - HTTP 상태 코드가 `200`인지 확인한다.
   - 응답 바이트가 UTF-8 문자열 `healthcheck`와 정확히 일치하여 공백이나 개행이 없는지 확인한다.
   - Content-Type이 `text/plain`과 호환되는지 확인한다.
2. 애플리케이션 컨텍스트 테스트에서 기존 컨트롤러와 새 컨트롤러가 모두 등록되는지 확인한다.
3. 구현 후 Maven 래퍼의 `./mvnw clean verify`로 전체 테스트와 빌드를 검증한다. 기존 Hello API 테스트도 통과해야 한다.
4. 애플리케이션을 기동한 후 실제 HTTP 요청으로 `/api/healthcheck`의 상태 코드와 본문을 확인하고, `/api/hello`도 기존 응답을 유지하는지 확인한다.

완료 기준은 새 API 계약 충족, 기존 테스트를 포함한 전체 빌드 성공 및 정상 기동이다. 이번 계획서 작성 단계에서는 구현이나 테스트 실행을 수행하지 않는다.

## 확인이 필요한 질문

1. 응답 형식을 기존 API와 동일한 `text/plain`으로 확정해도 되는가? 별도 요구가 없다면 위 전제를 적용한다.
2. 고정 문자열 응답을 통한 생존 확인만 필요한가, 아니면 향후 데이터베이스나 외부 서비스 상태를 반영하는 준비 상태 검사도 필요한가? 현재 계획은 생존 확인으로 한정한다.
3. 배포 환경에서 이 경로에 요구하는 접근 제어나 상태 검사 설정이 있는가? 해당 요구가 있다면 구현 및 배포 설정 범위를 추가로 정한다.
