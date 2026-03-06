# ACL (Anti-Corruption Layer, 부패 방지 계층)

Eric Evans의 DDD(도메인 주도 설계)에서 제안한 패턴으로,
**외부 시스템의 모델이 내부 도메인 모델을 오염시키지 않도록 보호하는 변환 계층**이다.

핵심 아이디어는 하나다.

> **외부 세계의 언어(모델, 필드명, 타입)를 내부 도메인의 언어로 번역하는 전담 번역기를 둔다.**

---

## 왜 ACL이 필요한가

외부 시스템(다른 서비스, 레거시 API, 써드파티 등)은 자신만의 모델과 용어를 가진다.
이를 그대로 내부에서 사용하면 외부 시스템의 변경이 도메인 로직까지 전파된다.

### ACL 없이 외부 시스템에 직접 의존하는 경우

```java
// Service가 외부 시스템의 응답 구조를 직접 안다
@Service
public class ProductApplicationService {

    @Override
    public Product create(CreateProductRequest request, UUID actorId) {
        // 외부 시스템의 필드명(sellerNo, sellerStatusCode)을 Service가 직접 다룸
        ExternalSellerPayload payload = externalClient.findSeller(request.sellerId());
        if (!"ACTIVE".equals(payload.sellerStatusCode())) {
            throw new RuntimeException("inactive seller");
        }
        UUID sellerId = UUID.fromString(payload.sellerNo()); // 타입 변환도 Service가 담당
        ...
    }
}
```

**문제**:
- 외부 API가 `sellerStatusCode` → `status`로 필드명을 바꾸면 Service 코드 수정이 필요하다
- 외부 시스템의 타입 변환(`String` → `UUID`) 로직이 비즈니스 코드에 섞인다
- `ExternalSellerPayload`라는 외부 모델이 Application 계층에 침투한다
- 외부 시스템 없이 Service를 단위 테스트하기 어렵다

---

## ACL 적용 후

외부 시스템과의 경계에 **번역 계층(ACL)** 을 두고, Application은 내부 언어만 사용한다.

```
Application Layer                  Infrastructure Layer
──────────────────────────────────────────────────────
ProductApplicationService          SellerAclAdapter
       │                                  │
       │  SellerAcl (인터페이스)           │  ExternalSellerClient
       └──────────────────────────────────┘
              내부 언어만 사용         외부 언어 번역 담당
         (SellerIdentity, UUID)   (ExternalSellerPayload, String)
```

---

## 구성 요소

### 1. SellerAcl — Application 계층의 인터페이스 (내부 계약)

```java
// application/acl/SellerAcl.java
public interface SellerAcl {
    SellerIdentity loadActiveSeller(UUID sellerId);
}
```

Application이 외부 시스템에게 요구하는 계약이다.
반환 타입이 `SellerIdentity`(내부 모델)이며, 외부 모델(`ExternalSellerPayload`)은 전혀 노출되지 않는다.

### 2. SellerIdentity — 내부 도메인 언어로 표현한 판매자

```java
// application/acl/SellerIdentity.java
public record SellerIdentity(UUID id) {}
```

외부 시스템이 `sellerNo(String)`로 표현하는 판매자 ID를 내부에서는 `UUID id`로 표현한다.
Application은 이 객체만 알고, 외부 시스템이 어떤 타입을 쓰는지 모른다.

### 3. SellerAclAdapter — 번역 담당 (Infrastructure 계층)

```java
// infrastructure/acl/SellerAclAdapter.java
@Component
public class SellerAclAdapter implements SellerAcl {

    private final ExternalSellerClient externalSellerClient;

    @Override
    public SellerIdentity loadActiveSeller(UUID sellerId) {
        // 1. 외부 시스템 호출 → 외부 모델(ExternalSellerPayload) 수신
        ExternalSellerPayload payload = externalSellerClient.findSeller(sellerId)
            .orElseThrow(() -> new SellerNotFoundException(sellerId));

        // 2. 외부 상태코드("ACTIVE") → 내부 비즈니스 규칙으로 검증
        if (!ACTIVE_STATUS.equalsIgnoreCase(payload.sellerStatusCode())) {
            throw new InactiveSellerException(sellerId);
        }

        // 3. 외부 타입(String sellerNo) → 내부 타입(UUID)으로 변환
        return new SellerIdentity(parseSellerId(payload.sellerNo()));
    }
}
```

번역 책임이 이 클래스 하나에 집중된다:
- 외부 필드명(`sellerNo`, `sellerStatusCode`) 해석
- 외부 상태값(`"ACTIVE"`) → 내부 예외(`InactiveSellerException`) 변환
- 외부 타입(`String`) → 내부 타입(`UUID`) 변환

### 4. ExternalSellerClient — 실제 외부 호출 인터페이스

```java
// infrastructure/acl/client/ExternalSellerClient.java
public interface ExternalSellerClient {
    Optional<ExternalSellerPayload> findSeller(UUID sellerId);
}

// 외부 시스템의 응답 구조 (외부 언어 그대로)
public record ExternalSellerPayload(String sellerNo, String sellerStatusCode) {}
```

외부 API의 실제 응답 구조(`ExternalSellerPayload`)는 이 계층에만 존재한다.

### 5. StubExternalSellerClient — 로컬 개발/테스트용 스텁

```java
// infrastructure/acl/client/StubExternalSellerClient.java
@Component
public class StubExternalSellerClient implements ExternalSellerClient {

    @Override
    public Optional<ExternalSellerPayload> findSeller(UUID sellerId) {
        // 실제 외부 HTTP 호출 대신 항상 ACTIVE 판매자를 반환
        // 추후 실제 HTTP/RPC 어댑터로 교체 예정
        return Optional.of(new ExternalSellerPayload(sellerId.toString(), "ACTIVE"));
    }
}
```

실제 외부 서버 없이 개발과 테스트가 가능하다.
실제 연동이 필요해지면 `StubExternalSellerClient`만 `HttpExternalSellerClient`로 교체하면 된다.

---

## Service 코드 변화

```java
// 이전: ApplicationEventPublisher 의존
@Service
public class ProductApplicationService implements ProductUseCase {
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public Product create(CreateProductRequest request, UUID actorId) {
        Product product = Product.create(request.sellerId(), ...);  // sellerId 검증 없음
        Product saved = productRepository.save(product);
        applicationEventPublisher.publishEvent(new ProductCreatedEvent(...));
        return saved;
    }
}
```

```java
// 현재: SellerAcl 의존 (이벤트 발행 제거, 판매자 검증 추가)
@Service
public class ProductApplicationService implements ProductUseCase {
    private final SellerAcl sellerAcl;
    private final ProductRepository productRepository;

    public Product create(CreateProductRequest request, UUID actorId) {
        SellerIdentity seller = sellerAcl.loadActiveSeller(request.sellerId()); // 판매자 검증
        Product product = Product.create(seller.id(), ...);  // 검증된 ID만 사용
        return productRepository.save(product);
    }
}
```

Service는 `SellerAcl` 인터페이스만 알고, 뒤에서 외부 API를 호출하는지, 스텁을 쓰는지 모른다.

---

## 패키지 구조

```
product/
├── application/
│   ├── acl/
│   │   ├── SellerAcl.java           # ACL 인터페이스 (내부 계약)
│   │   └── SellerIdentity.java      # 내부 도메인 언어로 표현한 판매자
│   ├── exception/
│   │   ├── SellerNotFoundException.java
│   │   └── InactiveSellerException.java
│   └── service/
│       └── ProductApplicationService.java
│
└── infrastructure/
    └── acl/
        ├── SellerAclAdapter.java    # 번역 구현체 (외부 → 내부 변환)
        └── client/
            ├── ExternalSellerClient.java      # 외부 호출 인터페이스
            ├── ExternalSellerPayload.java     # 외부 시스템 응답 구조 (외부 언어)
            └── StubExternalSellerClient.java  # 로컬 스텁 구현체
```

`ExternalSellerPayload`(외부 언어)는 `infrastructure/acl/client/` 안에만 존재한다.
`application/` 계층에는 절대 올라오지 않는다.

---

## 이번 커밋에서 함께 제거된 것

ACL 도입과 함께 이전의 이벤트 발행 코드가 제거됐다.

| 제거된 것 | 이유 |
|-----------|------|
| `ProductCreatedEvent` / `ProductUpdatedEvent` / `ProductDeletedEvent` | 이벤트 기반 후속 처리 단순화 |
| `ProductEventHandler` | 핸들러 제거 |
| `ApplicationEventPublisher` 의존 | Service에서 제거 |

현재 단계에서는 이벤트 없이 **ACL을 통한 외부 경계 보호**에 집중한 구조다.

---

## 의존성 방향 정리

```
ProductControllerImpl
       │
       ▼
ProductApplicationService
       │                    │
       ▼                    ▼
ProductRepository       SellerAcl (인터페이스)
       │                    │
       ▼                    ▼
ProductRepositoryAdapter  SellerAclAdapter
       │                    │
       ▼                    ▼
  ProductJpaRepository   ExternalSellerClient (인터페이스)
                              │
                              ▼
                       StubExternalSellerClient
                       (→ 추후 실제 HTTP 클라이언트로 교체)
```

Application 계층은 항상 **인터페이스**(`SellerAcl`)에만 의존한다.
외부 시스템의 구체적인 호출 방식은 Infrastructure 계층에 완전히 캡슐화된다.

---

## 테스트 전략

ACL 구조는 각 계층을 독립적으로 테스트할 수 있게 만든다.

### Service 테스트 — 외부 시스템 없이 SellerAcl Mock으로

```java
@ExtendWith(MockitoExtension.class)
class ProductApplicationServiceTest {

    @Mock
    private SellerAcl sellerAcl;  // 외부 시스템 대신 Mock

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductApplicationService service;

    @Test
    void createUsesVerifiedSellerId() {
        UUID sellerId = UUID.randomUUID();
        when(sellerAcl.loadActiveSeller(sellerId)).thenReturn(new SellerIdentity(sellerId));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(request, actorId);

        verify(sellerAcl).loadActiveSeller(sellerId);  // ACL 호출 여부 검증
    }
}
```

### ACL Adapter 테스트 — ExternalSellerClient Mock으로

```java
@ExtendWith(MockitoExtension.class)
class SellerAclAdapterTest {

    @Mock
    private ExternalSellerClient externalSellerClient;  // 실제 HTTP 호출 대신 Mock

    @InjectMocks
    private SellerAclAdapter sellerAclAdapter;

    @Test
    void loadActiveSellerReturnsIdentity() {
        UUID sellerId = UUID.randomUUID();
        when(externalSellerClient.findSeller(sellerId))
            .thenReturn(Optional.of(new ExternalSellerPayload(sellerId.toString(), "ACTIVE")));

        SellerIdentity identity = sellerAclAdapter.loadActiveSeller(sellerId);

        assertThat(identity.id()).isEqualTo(sellerId);
    }

    @Test
    void loadActiveSellerThrowsWhenSellerInactive() {
        when(externalSellerClient.findSeller(sellerId))
            .thenReturn(Optional.of(new ExternalSellerPayload(sellerId.toString(), "INACTIVE")));

        assertThatThrownBy(() -> sellerAclAdapter.loadActiveSeller(sellerId))
            .isInstanceOf(InactiveSellerException.class);
    }
}
```

번역 로직(상태 검증, 타입 변환)을 외부 시스템 없이 단독으로 검증할 수 있다.

---

## 핵심 정리

| 항목 | ACL 없이 | ACL 적용 후 |
|------|---------|------------|
| 외부 모델 위치 | Application까지 침투 | Infrastructure에만 존재 |
| 외부 API 변경 시 | Service 코드 수정 필요 | Adapter만 수정 |
| 타입/필드명 변환 위치 | Service 내부 | SellerAclAdapter 집중 |
| Service 단위 테스트 | 외부 시스템 Mock 필요 | SellerAcl Mock으로 충분 |
| 외부 시스템 교체 | 전체 영향 | StubClient → 실제 Client 교체만 |