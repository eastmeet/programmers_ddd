# Event-Driven CQRS

기존 Command/Query 코드 분리 방식에서, **이벤트 발행(Event Publishing)** 을 통한 CQRS로 전환한 내용을 정리한다.

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

## @TransactionalEventListener 와 @EventListener의 차이

이벤트 핸들러에 `@TransactionalEventListener(phase = AFTER_COMMIT)`을 사용한 것이 핵심이다.

### @EventListener (일반 이벤트)

```
트랜잭션 시작
    │
    ├── productRepository.save(product)
    │
    ├── publishEvent() ──> handle() 즉시 실행  ← 트랜잭션 도중에 실행
    │
트랜잭션 커밋 or 롤백
```

**문제**: `save()`는 성공했지만 이후 코드에서 예외가 발생해 **롤백되어도 핸들러는 이미 실행됐다**.
핸들러가 외부 API 호출이나 알림을 보냈다면 취소할 수 없다.

### @TransactionalEventListener(phase = AFTER_COMMIT)

```
트랜잭션 시작
    │
    ├── productRepository.save(product)
    │
    ├── publishEvent() ──> 이벤트를 큐에 보류만 함 (실행 안 함)
    │
트랜잭션 커밋 ──> handle() 실행  ← DB 반영이 확정된 후에만 실행
    또는
트랜잭션 롤백 ──> handle() 실행 안 함  ← 롤백되면 이벤트도 취소
```

**DB에 실제로 저장이 확정된 이후에만** 핸들러가 실행된다.
롤백 시 이벤트 처리가 실행되지 않으므로 데이터 정합성이 보장된다.

| 속성 | @EventListener | @TransactionalEventListener(AFTER_COMMIT) |
|------|---------------|------------------------------------------|
| 실행 시점 | publishEvent() 즉시 | 트랜잭션 커밋 이후 |
| 롤백 시 | 핸들러 이미 실행됨 | 핸들러 실행 안 함 |
| 정합성 | 보장 어려움 | 보장됨 |
| 용도 | 트랜잭션 무관한 작업 | DB 작업 이후 후속 처리 |

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