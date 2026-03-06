# Clean Architecture (클린 아키텍처)

Robert C. Martin(Uncle Bob)이 제안한 소프트웨어 아키텍처 패턴으로, **동심원(Concentric Circles) 구조**로 표현된다.

핵심 규칙은 하나다.

> **의존성 규칙(Dependency Rule): 안쪽 원은 바깥쪽 원을 절대 알아서는 안 된다.**

---

## 클린 아키텍처 계층 구조

```
         [ Infrastructure / Presentation ]   ← 가장 바깥 (기술 세부사항)
                       ↓ (의존)
              [ Application Layer ]           ← UseCase, 비즈니스 흐름 조율
                       ↓ (의존)
                [ Domain Layer ]              ← 핵심 비즈니스 규칙 (엔티티, Repository 인터페이스)
```

- **Domain**: 비즈니스의 본질. 어떤 프레임워크, DB에도 의존하지 않는다.
- **Application**: 도메인을 활용해 유스케이스를 실행하는 오케스트레이터.
- **Infrastructure**: 실제 DB, JPA 등 외부 기술 구현체.
- **Presentation**: HTTP 요청/응답 처리. Controller, DTO.

---

## 헥사고날 아키텍처와의 개념 비교

두 아키텍처 모두 **의존성 역전(DIP)** 을 통해 도메인을 외부로부터 보호한다는 목표는 같다.
하지만 구조를 바라보는 관점과 용어에서 차이가 있다.

### 관점의 차이

| 항목 | 헥사고날 아키텍처 | 클린 아키텍처 |
|------|----------------|-------------|
| 핵심 은유 | 육각형 + 포트와 어댑터 (방향성) | 동심원 (계층성) |
| 외부 연결 인터페이스 명칭 | Port (Inbound / Outbound) | Repository, Gateway, UseCase 인터페이스 |
| Repository 인터페이스 위치 | `application/port/out/` | `domain/repository/` ← **도메인 계층** |
| 웹 레이어 명칭 | Inbound Adapter | Presentation Layer |
| DB/외부 기술 레이어 명칭 | Outbound Adapter | Infrastructure Layer |
| UseCase 인터페이스 위치 | `application/port/in/` | `application/usecase/` |

### 가장 중요한 차이: Repository 인터페이스의 위치

**헥사고날 아키텍처**에서는 `ProductPersistencePort`가 `application/port/out/`에 있었다.
Application 계층이 "외부 DB에 무언가를 요청한다"는 포트 개념으로 바라본 것이다.

```
헥사고날:
application/
├── port/
│   ├── in/  ProductUseCase.java         ← Inbound Port
│   └── out/ ProductPersistencePort.java ← Outbound Port (application 계층 소속)
└── service/ ProductService.java
```

**클린 아키텍처**에서는 `ProductRepository`가 `domain/repository/`에 있다.
"Repository는 도메인의 저장소 개념이다. 도메인이 자신의 저장 규약을 정의한다"는 관점이다.

```
클린 아키텍처:
domain/
├── model/      Product.java
└── repository/ ProductRepository.java   ← 도메인 계층 소속 (핵심 차이)

application/
├── usecase/ ProductUseCase.java
└── service/ ProductApplicationService.java
```

의존성 방향은 동일하지만, Repository 인터페이스를 **도메인의 일부**로 보느냐, **애플리케이션의 포트**로 보느냐의 차이다.
클린 아키텍처에서는 도메인이 "어떻게 저장될지의 규약"을 직접 선언하므로 더 순수한 도메인 모델에 가깝다.

---

## 이번 커밋에서 변경된 내용

### 패키지 구조 변경

**헥사고날 아키텍처 (이전)**

```
product/
├── domain/
│   └── Product.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── ProductUseCase.java           # Inbound Port
│   │   └── out/
│   │       └── ProductPersistencePort.java   # Outbound Port
│   └── service/
│       └── ProductService.java
├── adapter/
│   ├── in/web/
│   │   ├── ProductController.java
│   │   └── ProductControllerImpl.java
│   └── out/persistence/
│       ├── ProductJpaRepository.java
│       └── ProductPersistenceAdapter.java
└── dto/
    ├── in/
    │   ├── ProductCreateRequest.java
    │   └── ProductUpdateRequest.java
    └── out/
        └── ProductResponse.java
```

**클린 아키텍처 (현재)**

```
product/
├── domain/
│   ├── model/
│   │   └── Product.java                      # 도메인 모델
│   └── repository/
│       └── ProductRepository.java            # Repository 인터페이스 (도메인 계층)
├── application/
│   ├── usecase/
│   │   └── ProductUseCase.java               # UseCase 인터페이스
│   ├── service/
│   │   └── ProductApplicationService.java    # 비즈니스 로직 구현체
│   └── exception/
│       └── ProductNotFoundException.java     # 애플리케이션 예외
├── infrastructure/
│   └── persistence/
│       ├── ProductJpaRepository.java         # Spring Data JPA
│       └── ProductRepositoryAdapter.java     # Repository 구현체 (JPA 연결)
└── presentation/
    ├── controller/
    │   ├── ProductController.java            # Swagger 인터페이스
    │   └── ProductControllerImpl.java        # HTTP 요청 처리
    └── dto/
        ├── request/
        │   ├── ProductCreateRequest.java
        │   └── ProductUpdateRequest.java
        └── response/
            └── ProductResponse.java
```

### 변경 항목 정리

| 헥사고날 (이전) | 클린 아키텍처 (현재) | 변경 이유 |
|---------------|-------------------|---------|
| `domain/Product.java` | `domain/model/Product.java` | 모델 패키지 명시 |
| `application/port/out/ProductPersistencePort.java` | `domain/repository/ProductRepository.java` | Repository 인터페이스를 도메인 계층으로 이동 |
| `application/port/in/ProductUseCase.java` | `application/usecase/ProductUseCase.java` | Port 용어 제거, 역할 명칭으로 변경 |
| `application/service/ProductService.java` | `application/service/ProductApplicationService.java` | Application 계층임을 이름에 명시 |
| `adapter/out/persistence/ProductPersistenceAdapter.java` | `infrastructure/persistence/ProductRepositoryAdapter.java` | Adapter → Infrastructure 계층 재분류 |
| `adapter/in/web/ProductControllerImpl.java` | `presentation/controller/ProductControllerImpl.java` | Adapter → Presentation 계층 재분류 |
| `dto/in/`, `dto/out/` | `presentation/dto/request/`, `presentation/dto/response/` | DTO를 Presentation 계층 하위로 이동 |
| (없음) | `common/exception/GlobalExceptionHandler.java` | 전역 예외 처리 추가 |
| (없음) | `common/exception/ErrorResponse.java` | 표준화된 에러 응답 추가 |

---

## 의존성 흐름

```
HTTP 요청
    |
    v
ProductControllerImpl (presentation)
    |  ProductUseCase 호출
    v
ProductApplicationService (application)
    |  ProductRepository 호출 (domain 인터페이스)
    v
ProductRepositoryAdapter (infrastructure)  -- ProductRepository 구현
    |
    v
ProductJpaRepository -> PostgreSQL
```

각 계층의 의존 방향:
- `presentation` -> `application` (UseCase 인터페이스)
- `application` -> `domain` (Repository 인터페이스, 도메인 모델)
- `infrastructure` -> `domain` (Repository 인터페이스 구현)

`infrastructure`와 `presentation`은 서로를 모른다. 둘 다 `domain`/`application`에만 의존한다.

---

## 예외 처리 추가

클린 아키텍처 전환과 함께 계층별 예외 처리 구조도 도입했다.

```java
// application/exception/ProductNotFoundException.java
// - 도메인 개념의 예외: "Product를 찾을 수 없다"는 비즈니스 규칙 위반
public class ProductNotFoundException extends RuntimeException { ... }

// common/exception/GlobalExceptionHandler.java
// - Infrastructure/Presentation 경계에서 예외를 HTTP 응답으로 변환
@RestControllerAdvice
public class GlobalExceptionHandler { ... }
```

**이전 방식(헥사고날)**: Service 내부에서 `ResponseStatusException`을 직접 던짐
→ Application 계층이 HTTP 상태코드(Spring Web)를 직접 알아야 하므로 계층 위반

**현재 방식(클린)**: Application은 도메인 예외(`ProductNotFoundException`)를 던지고,
Presentation 경계의 `GlobalExceptionHandler`가 HTTP 응답으로 변환
→ Application이 HTTP를 모른다. 계층 경계가 명확해진다.

---

## 한눈에 비교

| 항목 | 헥사고날 | 클린 아키텍처 |
|------|---------|-------------|
| 구조 은유 | 포트와 어댑터 (방향) | 동심원 (계층) |
| Repository 위치 | `application/port/out/` | `domain/repository/` |
| 외부 레이어 명칭 | adapter/in, adapter/out | presentation, infrastructure |
| UseCase 위치 | `application/port/in/` | `application/usecase/` |
| 예외 처리 방식 | Service에서 HTTP 예외 직접 | 도메인 예외 + 전역 핸들러 |
| 개념적 초점 | 포트를 통한 격리 | 계층 간 의존성 방향 |

> 두 아키텍처는 상충하는 개념이 아니다. 클린 아키텍처의 구현 방법 중 하나가 헥사고날이라고 볼 수도 있다.
> 이번 전환은 포트/어댑터 용어 대신 도메인 중심의 계층 명칭을 사용하고,
> Repository 인터페이스를 도메인의 일부로 명시적으로 귀속시키는 방향으로의 변화다.