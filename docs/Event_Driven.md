# Event-Driven

기존 Command/Query 코드 분리 방식에서, **이벤트 발행(Event Publishing)** 으로 전환한 내용을 정리한다.

---

## 기존 CQRS와의 차이

이전 CQRS는 **코드 경로를 물리적으로 분리**하는 방식이었다.

```
[기존] 코드 분리 방식
Controller ──> ProductCommandUseCase ──> ProductCommandService ──> ProductCommandRepository
           └─> ProductQueryUseCase   ──> ProductQueryService   ──> ProductQueryRepository
```

이번 변경은 **단일 서비스가 Command를 처리하고, 결과를 이벤트로 알리는** 방식으로 전환했다.

```
[현재] 이벤트 발행 방식
Controller ──> ProductUseCase ──> ProductApplicationService ──> ProductRepository
                                           │
                                  publishEvent(이벤트 발행)
                                           │
                                           └──> ProductEventHandler (후속 처리)
```

**핵심 아이디어**: Command 실행 후 "어떤 일이 일어났다"는 사실을 이벤트로 발행하고,
후속 작업(로깅, 캐시 갱신, 알림 등)은 이벤트 핸들러가 독립적으로 처리한다.

---

## 이벤트란 무엇인가

이벤트는 **이미 일어난 사실(과거형)** 을 표현한다.

```java
// "Product가 생성되었다"는 사실
public record ProductCreatedEvent(UUID productId, UUID actorId) {}

// "Product가 수정되었다"는 사실
public record ProductUpdatedEvent(UUID productId, UUID actorId) {}

// "Product가 삭제되었다"는 사실
public record ProductDeletedEvent(UUID productId, UUID actorId) {}
```

이벤트 이름은 항상 **과거형**이다. "CreateProduct(명령)"가 아니라 "ProductCreated(사실)"이다.
이벤트는 최소한의 정보만 담는다. `productId`와 `actorId`만으로 핸들러가 필요한 작업을 식별할 수 있다.

---

## 이벤트 발행과 처리 흐름

### 1. Service에서 이벤트 발행

```java
// application/service/ProductApplicationService.java
@Service
@Transactional(readOnly = true)
public class ProductApplicationService implements ProductUseCase {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher applicationEventPublisher; // Spring 이벤트 발행자

    @Override
    @Transactional
    public Product create(CreateProductRequest request, UUID actorId) {
        Product product = Product.create(..., actorId);
        Product saved = productRepository.save(product);

        // 저장 후 이벤트 발행 - "Product가 생성됐다"는 사실을 알림
        applicationEventPublisher.publishEvent(new ProductCreatedEvent(saved.getId(), actorId));

        return saved;
    }

    @Override
    @Transactional
    public Product update(UUID productId, UpdateProductRequest request, UUID actorId) {
        Product product = findByIdOrThrow(productId);
        product.update(..., actorId);

        applicationEventPublisher.publishEvent(new ProductUpdatedEvent(product.getId(), actorId));

        return product;
    }

    @Override
    @Transactional
    public void delete(UUID productId) {
        Product product = findByIdOrThrow(productId);
        productRepository.delete(product);

        applicationEventPublisher.publishEvent(new ProductDeletedEvent(productId, product.getModifyId()));
    }
}
```

Service는 이벤트를 **발행만** 한다. 핸들러가 뭘 하는지 알 필요가 없다.

### 2. EventHandler에서 후속 처리

```java
// infrastructure/event/ProductEventHandler.java
@Component
public class ProductEventHandler {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductCreatedEvent event) {
        log.info("ProductCreatedEvent: productId={}, actorId={}", event.productId(), event.actorId());
        // 향후: 캐시 갱신, 알림 발송, 검색 인덱스 업데이트 등
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductUpdatedEvent event) {
        log.info("ProductUpdatedEvent: productId={}, actorId={}", event.productId(), event.actorId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductDeletedEvent event) {
        log.info("ProductDeletedEvent: productId={}, actorId={}", event.productId(), event.actorId());
    }
}
```

---

## @EventListener vs @TransactionalEventListener 실행 시점 비교

두 어노테이션을 동시에 등록해서 실제 실행 순서를 로그로 확인할 수 있다.

```java
@Async("eventExecutor")
@EventListener                                               // 즉시 실행
public void on(ProductCreatedEvent event) {
    log.info("ProductCreatedEvent: productId={}", event.productId());
}

@Async("eventExecutor")
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 커밋 후 실행
public void handle(ProductCreatedEvent event) {
    log.info("ProductCreatedEvent: productId={}", event.productId());
}
```

### 실제 로그 출력 결과

```
[event-1] ProductCreatedEvent: productId=4329c0cb...   ← @EventListener (on)
Hibernate: insert into public.product (...)            ← 실제 DB INSERT
[event-2] ProductCreatedEvent: productId=4329c0cb...   ← @TransactionalEventListener (handle)
```

`event-1`이 Hibernate INSERT보다 **먼저** 출력됐다.
`event-2`는 INSERT가 완료된 **이후** 출력됐다.

### 실행 흐름 비교

```
@Transactional create() 시작
       │
       ├── Product.create()
       │
       ├── productRepository.save()       ← 아직 INSERT 안 됨 (flush 전)
       │
       ├── publishEvent()
       │      ├── @EventListener     → 즉시 별도 스레드(event-1)로 실행  ← INSERT보다 먼저!
       │      └── @TransactionalEventListener → 커밋 후로 보류
       │
       ├── 트랜잭션 flush → Hibernate: INSERT 실행
       │
       └── 커밋 완료
              └── @TransactionalEventListener → 별도 스레드(event-2)로 실행
```

### @EventListener에서 DB 조회하면?

`@EventListener`는 아직 INSERT가 실행되기 전에 실행되므로, 핸들러에서 DB를 조회하면 방금 저장한 데이터가 보이지 않는다.

```java
@Async("eventExecutor")
@EventListener
public void on(ProductCreatedEvent event) {
    // ⚠️ INSERT 전이므로 데이터 없음
    productRepository.findById(event.productId()); // Optional.empty()
}

@Async("eventExecutor")
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handle(ProductCreatedEvent event) {
    // ✅ 커밋 완료 후이므로 데이터 있음
    productRepository.findById(event.productId()); // Optional[Product]
}
```

| 항목 | @EventListener | @TransactionalEventListener(AFTER_COMMIT) |
|------|---------------|------------------------------------------|
| 실행 시점 | publishEvent() 즉시 (INSERT 전) | 트랜잭션 커밋 후 (INSERT 후) |
| 롤백 시 | 핸들러 이미 실행됨 | 핸들러 실행 안 함 |
| 핸들러에서 DB 조회 | 저장한 데이터 안 보임 | 저장한 데이터 보임 |
| 정합성 보장 | 어려움 | 보장됨 |
| 용도 | 트랜잭션과 무관한 즉시 작업 | DB 확정 후 후속 처리 |

DB 데이터를 기반으로 하는 후속 처리(캐시 갱신, 알림, 외부 연동)는 반드시 `@TransactionalEventListener(AFTER_COMMIT)`을 사용해야 한다.

### phase 옵션 4가지

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)    // 커밋 후 (가장 많이 사용)
@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)  // 롤백 후
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION) // 커밋/롤백 관계없이 완료 후
@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)   // 커밋 직전
```

---

## @Async 비동기 처리 설정

### 왜 @Async가 필요한가?

`@Async` 없이 `@TransactionalEventListener`만 사용하면 핸들러가 **요청 스레드에서 동기로** 실행된다.

```
[요청 스레드]
    ├── create() 실행
    ├── 커밋 완료
    ├── handle() 실행  ← 요청 스레드가 여기서 블로킹됨
    └── HTTP 응답 반환  ← handle()이 끝난 후에야 응답
```

핸들러가 느린 작업(외부 API, 이메일 발송 등)을 수행하면 응답 시간이 그만큼 늘어난다.

`@Async`를 추가하면 핸들러가 **별도 스레드 풀**에서 실행되어, 요청 스레드는 즉시 응답을 반환한다.

```
[요청 스레드]                        [event- 스레드 풀]
    ├── create() 실행
    ├── 커밋 완료
    └── HTTP 응답 반환  ──────────────→  handle() 비동기 실행
                                              └── 느린 작업도 OK
```

### AsyncConfig 설정

```java
// config/AsyncConfig.java
@EnableAsync       // Spring의 @Async 기능 활성화 (없으면 @Async가 동작하지 않음)
@Configuration
public class AsyncConfig {

    @Bean(name = "eventExecutor")
    public Executor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event-");
        executor.initialize();
        return executor;
    }
}
```

### ThreadPoolTaskExecutor 설정값 상세

스레드 풀은 요청이 들어올 때 스레드를 어떻게 생성하고 관리할지를 결정한다.

```
[작업 요청] ──> [Core 스레드] ──(Core 꽉 참)──> [Queue] ──(Queue 꽉 참)──> [Max 스레드]
                                                                                  │
                                                                         (Max도 꽉 참) ──> 거부 정책
```

**setCorePoolSize(2)** - 항상 유지하는 기본 스레드 수

```
평소 이벤트가 적을 때: 스레드 2개만 유지
요청이 없어도 이 2개는 대기 상태로 살아있음 → 빠른 응답 가능
```

**setMaxPoolSize(10)** - 순간 폭증 시 최대로 늘어날 수 있는 스레드 수

```
Queue가 꽉 찼을 때만 Core를 초과해서 스레드 생성
초과 스레드는 작업이 없으면 일정 시간 후 자동 제거 (기본 60초)
```

**setQueueCapacity(100)** - Core 스레드가 모두 바쁠 때 작업을 대기시키는 큐 크기

```
Core(2개) 모두 바쁨 → 큐에 최대 100개 대기 가능
큐가 100개 꽉 참 → 그때 Max까지 추가 스레드 생성
```

> 주의: Queue가 꽉 찬 후에 Max 스레드가 생성된다. 즉, Core → Queue → Max 순서다.
> Queue를 크게 잡으면 Max 스레드가 거의 생성되지 않는다.

**setThreadNamePrefix("event-")** - 생성되는 스레드 이름의 접두어

```
로그에서 어느 스레드가 실행했는지 식별 가능
[event-1] ProductCreatedEvent: ...   ← event- 접두어로 이벤트 처리 스레드임을 바로 파악
[http-nio-1] ...                     ← 요청 스레드와 구분
```

**전체 동작 시나리오**

```
상황 1. 동시 이벤트 2개 이하
  → Core 스레드(event-1, event-2)가 처리

상황 2. 동시 이벤트 3~102개
  → Core 2개 처리 중, 나머지 100개는 Queue 대기

상황 3. 동시 이벤트 103~112개
  → Core 2개 + Queue 100개 꽉 참 → Max까지 추가 스레드(event-3 ~ event-10) 생성

상황 4. 동시 이벤트 113개 이상
  → 거부 정책 실행 (기본: CallerRunsPolicy → 요청 스레드가 직접 처리)
```

### @Async에 Executor 이름을 지정하는 이유

```java
@Async("eventExecutor")   // 특정 Executor 지정
@Async                    // 지정 없으면 Spring 기본 Executor 사용
```

이름을 지정하지 않으면 Spring 기본 `SimpleAsyncTaskExecutor`가 사용되는데,
이는 **요청마다 새 스레드를 생성**하고 재사용하지 않아 성능 문제가 발생할 수 있다.
`ThreadPoolTaskExecutor`를 명시적으로 지정해서 스레드를 재사용하고 수를 제한한다.

여러 용도별로 Executor를 분리하는 것도 가능하다.

```java
@Bean(name = "eventExecutor")
public Executor eventExecutor() { ... }   // 이벤트 처리용

@Bean(name = "batchExecutor")
public Executor batchExecutor() { ... }  // 배치 처리용 (더 큰 풀)

@Bean(name = "emailExecutor")
public Executor emailExecutor() { ... }  // 이메일 발송용 (더 작은 풀)
```

---

## 함께 변경된 사항들

이벤트 방식 전환과 함께 계층 책임도 정리됐다.

### UseCase 반환 타입: ProductResponse → Product

**이전**
```java
// UseCase가 ProductResponse(DTO)를 반환
public interface ProductUseCase {
    ProductResponse create(ProductCreateRequest req);
}
```

**현재**
```java
// UseCase가 Product(도메인 객체)를 반환
public interface ProductUseCase {
    Product create(CreateProductRequest request, UUID actorId);
}
```

Application 계층이 `ProductResponse`(Presentation 관심사)를 알 필요가 없다.
DTO 변환은 Controller가 담당한다.

```java
// ProductControllerImpl - DTO 변환을 Controller에서 처리
public ResponseEntity<ProductResponse> create(CreateProductRequest req, UUID actorId) {
    Product product = productUseCase.create(req, actorId);
    ProductResponse response = ProductResponse.from(product);  // Controller에서 변환
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### actorId 처리 위치: Service → Controller

**이전**: Service가 String을 받아 UUID로 파싱
```java
// Service 내부에서 UUID 변환 (Presentation 관심사가 Application에 침투)
private UUID toUuid(String value, String fieldName) {
    try { return UUID.fromString(value); }
    catch (...) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ...); }
}
```

**현재**: Controller에서 UUID로 받아 Service에 전달
```java
// Controller - @RequestHeader에서 Spring이 자동으로 UUID 변환
ResponseEntity<ProductResponse> create(
    @RequestBody CreateProductRequest request,
    @RequestHeader("X-Actor-Id") UUID actorId  // Spring이 String → UUID 변환
);

// Service - 이미 UUID인 actorId를 그대로 사용
public Product create(CreateProductRequest request, UUID actorId) {
    Product product = Product.create(..., actorId); // UUID 변환 로직 없음
}
```

HTTP 헤더 파싱은 Presentation 계층의 책임이다. Application은 UUID만 받는다.

### DTO 이름 변경

| 이전 | 현재 | 이유 |
|------|------|------|
| `ProductCreateRequest` | `CreateProductRequest` | 동사 우선 네이밍 (Create + 대상) |
| `ProductUpdateRequest` | `UpdateProductRequest` | 동사 우선 네이밍 |

---

## 패키지 구조 변화

**이전 (Command/Query 코드 분리)**

```
product/
├── application/
│   ├── command/
│   │   ├── usecase/ProductCommandUseCase.java
│   │   └── service/ProductCommandService.java
│   └── query/
│       ├── usecase/ProductQueryUseCase.java
│       └── service/ProductQueryService.java
├── domain/repository/
│   ├── command/ProductCommandRepository.java
│   └── query/ProductQueryRepository.java
└── infrastructure/persistence/
    ├── command/ProductCommandRepositoryAdapter.java
    └── query/ProductQueryRepositoryAdapter.java
```

**현재 (이벤트 기반)**

```
product/
├── application/
│   ├── event/
│   │   ├── ProductCreatedEvent.java      ← 새로 추가
│   │   ├── ProductUpdatedEvent.java      ← 새로 추가
│   │   └── ProductDeletedEvent.java      ← 새로 추가
│   ├── usecase/ProductUseCase.java       ← 단일 UseCase 복귀
│   └── service/ProductApplicationService.java
├── domain/repository/
│   └── ProductRepository.java            ← 단일 Repository 복귀
└── infrastructure/
    ├── event/ProductEventHandler.java    ← 새로 추가
    └── persistence/ProductRepositoryAdapter.java
```

Command/Query Repository 분리를 제거하고, 이벤트 패키지가 추가됐다.

---

## 이벤트 위치: application vs domain

이벤트 클래스가 `application/event/`에 위치한 것에 주목할 만하다.

```
application/event/ProductCreatedEvent.java  ← 현재 위치
domain/event/ProductCreatedEvent.java       ← DDD 순수주의 관점
```

- **`domain/event/`**: 이벤트가 비즈니스 규칙의 일부일 때. 예) `재고 소진됨`, `주문 취소됨`
- **`application/event/`**: 유스케이스 실행 결과를 알릴 때. 예) `상품 등록됨(로그용)`, `수정됨(캐시용)`

현재 이벤트는 비즈니스 규칙이 아니라 **유스케이스 완료 후 후속 작업 트리거** 목적이므로
`application/event/`가 적절한 위치다.

---

## 현재 이벤트 핸들러의 역할과 확장 가능성

현재 핸들러는 로그 출력만 한다. 이 구조의 진가는 확장할 때 드러난다.

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handle(ProductCreatedEvent event) {
    // 현재: 로그만
    log.info("ProductCreatedEvent: productId={}", event.productId());

    // 향후 추가 가능한 것들 (Service 코드 수정 없이):
    // - 캐시 갱신: cacheManager.evict("product", event.productId())
    // - 검색 인덱스 업데이트: searchIndexer.index(event.productId())
    // - 알림 발송: notificationService.notify(event.actorId(), "상품 등록 완료")
    // - 감사 로그: auditLogger.log("PRODUCT_CREATED", event)
    // - 외부 시스템 연동: externalApi.sync(event.productId())
}
```

핸들러에 기능을 추가해도 `ProductApplicationService`는 전혀 수정하지 않아도 된다.
이것이 이벤트 기반의 핵심 이점인 **Service 코드의 안정성**이다.

---

## 테스트 전략 변화

이벤트 방식은 테스트에서 이벤트 발행 여부를 검증한다.

```java
// ProductApplicationServiceTest.java
@Mock
private ApplicationEventPublisher applicationEventPublisher;

@Test
void createSetsActorIdToRegIdAndModifyId() {
    // ...
    productApplicationService.create(request, actorId);

    // 이벤트가 발행되었는지 검증
    verify(applicationEventPublisher).publishEvent(any(ProductCreatedEvent.class));
}
```

Service 테스트에서 `ApplicationEventPublisher`를 Mock으로 주입하고,
`publishEvent()`가 올바른 이벤트 타입으로 호출됐는지 검증한다.
이벤트 핸들러의 실제 동작은 별도 단위 테스트로 분리할 수 있다.