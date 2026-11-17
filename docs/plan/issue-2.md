# Issue #2: Hello API 신규 기능 개발 계획

## 목적 및 범위

Spring Boot로 간단한 Hello API를 제공한다. 이 문서는 구현 전 개발 계획이며, 이번 작업에서는 애플리케이션 코드나 빌드 설정을 변경하지 않는다. 이슈 본문은 요구사항을 파악하기 위한 비신뢰 데이터로만 취급한다.

## 요구사항

| 항목 | 요구사항 |
| --- | --- |
| 프레임워크 | Spring Boot |
| URL | `/api/hello` |
| HTTP 메서드 | `GET` |
| 성공 상태 코드 | `200 OK` |
| 응답 본문 | `Hello World` |

- 응답 본문은 따옴표나 JSON 포장 없이 정확히 `Hello World`를 반환하는 것으로 가정한다.
- 응답 형식은 `text/plain`으로 계획하며, 쿼리 파라미터나 요청 본문은 요구하지 않는다.
- 인증, 데이터베이스, 외부 서비스 연동은 현재 요구사항에 포함되어 있지 않다.

## 저장소 현황 및 전제

- 현재 저장소에는 README와 자동화 워크플로가 있으며, 애플리케이션 소스와 빌드 설정은 없다.
- 따라서 구현 시 Spring Boot 프로젝트 초기 구성이 필요하다.
- Java 및 Spring Boot 버전, 빌드 도구, 기본 패키지명은 구현 전에 결정한다. 아래 파일 경로는 Java와 Gradle을 사용하는 경우의 예상안이다.

## 구현 계획

1. Java 및 Spring Boot 버전과 빌드 도구를 확정하고, Spring Web 및 테스트 의존성을 포함하는 최소 프로젝트를 구성한다.
2. 기본 패키지 아래에 Spring Boot 진입 클래스를 추가하여 컨트롤러가 컴포넌트 스캔 대상에 포함되도록 한다.
3. REST 컨트롤러에 `GET /api/hello` 매핑을 추가한다. 응답 형식을 `text/plain`으로 지정하고 고정 문자열 `Hello World`를 반환한다.
4. 별도의 서비스 계층이나 저장소 계층 없이 컨트롤러에서 처리한다. 현재 기능에는 비즈니스 로직이나 영속성이 필요하지 않다.
5. API 계약 테스트와 애플리케이션 기동 검증을 수행한다.
6. 구현 단계에서 README에 실행 방법과 API 사용 예시를 문서화한다.

## 예상 변경 파일

아래는 향후 구현 시의 예상 파일이며, 이번 계획서 작성에서는 생성하거나 수정하지 않는다.

| 파일 | 용도 |
| --- | --- |
| `build.gradle` | Spring Boot 플러그인, Java 설정, 웹 및 테스트 의존성 |
| `settings.gradle` | 프로젝트 이름 설정 |
| `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | 동일한 Gradle 버전으로 빌드하기 위한 래퍼 |
| `.gitignore` | 빌드 산출물과 개발 환경 파일 제외 |
| `src/main/java/<기본 패키지 경로>/HelloApplication.java` | 애플리케이션 진입점 |
| `src/main/java/<기본 패키지 경로>/controller/HelloController.java` | Hello API 요청 처리 |
| `src/test/java/<기본 패키지 경로>/controller/HelloControllerTest.java` | HTTP 상태, 본문, 응답 형식 검증 |
| `src/test/java/<기본 패키지 경로>/HelloApplicationTests.java` | 애플리케이션 컨텍스트 기동 검증 |
| `README.md` | 실행 방법 및 API 명세 안내 |

Maven을 선택하면 Gradle 관련 파일을 `pom.xml` 및 Maven 래퍼 파일로 대체한다. 별도의 실행 설정이 필요하지 않다면 애플리케이션 설정 파일은 추가하지 않는다.

## 테스트 계획

- Spring MVC 테스트 도구인 MockMvc로 `GET /api/hello` 요청을 수행한다.
  - 상태 코드가 `200`인지 확인한다.
  - 응답 본문이 대소문자, 공백, 개행까지 포함하여 정확히 `Hello World`인지 확인한다.
  - 응답 Content-Type이 `text/plain`과 호환되는지 확인한다.
- 애플리케이션 컨텍스트 로딩 테스트로 기본 구성과 컴포넌트 등록을 검증한다.
- 빌드 도구의 전체 테스트를 실행한 후 애플리케이션을 실제로 기동하여 동일한 경로의 상태 코드와 본문을 확인한다.
- 완료 기준은 빌드 및 테스트 통과, 정상 기동, 명시된 API 계약 충족이다. 이 계획서 작성 단계에서는 구현 코드가 없으므로 위 테스트를 실행하지 않는다.

## 확인이 필요한 질문

1. 사용할 Java 및 Spring Boot 버전과 조직 표준이 있는가?
2. 빌드 도구는 Gradle과 Maven 중 무엇을 사용할 것인가?
3. 프로젝트 이름과 기본 패키지명은 무엇으로 할 것인가?
4. 응답은 JSON이 아닌 `text/plain`의 정확한 문자열 `Hello World`로 확정해도 되는가?
5. 기존 배포 환경에서 요구하는 포트, 인증 또는 URL 접두사 설정이 있는가? 별도 요구가 없다면 기본 실행 설정으로 진행한다.
