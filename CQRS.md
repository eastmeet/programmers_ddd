# CQRS (Command Query Responsibility Segregation)

Greg Young이 제안한 패턴으로, **명령(Command)과 조회(Query)의 책임을 분리**하는 아키텍처 원칙이다.

핵심 아이디어는 하나다.

> **상태를 변경하는 작업(Command)과 상태를 읽는 작업(Query)은 서로 다른 모델로 처리한다.**

---

## CQS 원칙에서 출발

CQRS의 뿌리는 Bertrand Meyer의 **CQS(Command Query Separation)** 원칙이다.

```
Command: 상태를 변경한다. 반환값이 없다(void).
Query:   상태를 읽는다. 상태를 변경하지 않는다.
```

CQS는 메서드 수준의 원칙이고, CQRS는 이를 **아키텍처 수준**으로 확장한 것이다.

---

## 기존 클린 아키텍처의 문제

클린 아키텍처를 도입했지만 단일 서비스가 읽기/쓰기를 모두 담당하는 구조였다.

```
ProductUseCase (인터페이스)
├── create()   ← 쓰기
├── update()   ← 쓰기
├── delete()   ← 쓰기
├── getById()  ← 읽기
└── getAll()   ← 읽기

ProductApplicationService (구현체)
└── 위 5개 메서드를 모두 구현

ProductRepository (도메인 인터페이스)
├── save()
├── delete()
├── findById()
└── findAll()
```

**문제점**:
- 읽기 요청이 폭증해도 쓰기 서비스와 함께 스케일링해야 한다
- 읽기 최적화(캐시, 조회 전용 쿼리)와 쓰기 최적화(트랜잭션, 정합성)가 같은 코드에 섞인다
- 서비스가 커질수록 단일 Service 클래스의 책임이 비대해진다

---

## CQRS 적용 후 구조

Command와 Query를 완전히 분리된 흐름으로 처리한다.

```
[ HTTP 요청 ]
      |
      v
ProductControllerImpl
      |
      |── (쓰기 요청) ──> ProductCommandUseCase ──> ProductCommandService
      |                                                     |
      |                                          ProductCommandRepository
      |                                                     |
      |                                       ProductCommandRepositoryAdapter
      |
      └── (읽기 요청) ──> ProductQueryUseCase ──> ProductQueryService
                                                        |
                                               ProductQueryRepository
                                                        |
                                          ProductQueryRepositoryAdapter
```

같은 JPA(`ProductJpaRepository`)를 사용하지만, **접근 경로가 완전히 분리**되어 있다.

---

## 계층별 변경사항

### Application 계층

**이전 (단일 UseCase)**

```java
// application/usecase/ProductUseCase.java
public interface ProductUseCase {
    ProductResponse create(ProductCreateRequest req);   // 쓰기
    ProductResponse getById(UUID productId);            // 읽기
    List<ProductResponse> getAll();                     // 읽기
    ProductResponse update(UUID productId, ...);        // 쓰기
    void delete(UUID productId);                        // 쓰기
}
```

**이후 (Command/Query 분리)**

```java
// application/command/usecase/ProductCommandUseCase.java
public interface ProductCommandUseCase {
    ProductResponse create(ProductCreateRequest req);
    ProductResponse update(UUID productId, ProductUpdateRequest req);
    void delete(UUID productId);
}

// application/query/usecase/ProductQueryUseCase.java
public interface ProductQueryUseCase {
    ProductResponse getById(UUID productId);
    List<ProductResponse> getAll();
}
```

**이전 (단일 Service)**

```java
// application/service/ProductApplicationService.java
@Service
public class ProductApplicationService implements ProductUseCase {
    private final ProductRepository productRepository; // 읽기/쓰기 모두 처리
    // create, update, delete, getById, getAll 모두 구현
}
```

**이후 (Command/Query Service 분리)**

```java
// application/command/service/ProductCommandService.java
@Service
@Transactional(readOnly = true)
public class ProductCommandService implements ProductCommandUseCase {
    private final ProductCommandRepository productCommandRepository; // 쓰기 전용
    // create, update, delete만 구현
}

// application/query/service/ProductQueryService.java
@Service
@Transactional(readOnly = true)
public class ProductQueryService implements ProductQueryUseCase {
    private final ProductQueryRepository productQueryRepository; // 읽기 전용
    // getById, getAll만 구현
}
```

---

### Domain 계층 (Repository 인터페이스 분리)

**이전 (단일 Repository)**

```java
// domain/repository/ProductRepository.java
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID productId);
    List<Product> findAll();
    void delete(Product product);
}
```

**이후 (Command/Query Repository 분리)**

```java
// domain/repository/command/ProductCommandRepository.java
public interface ProductCommandRepository {
    Product save(Product product);
    Optional<Product> findById(UUID productId); // update/delete 시 조회 목적
    void delete(Product product);
}

// domain/repository/query/ProductQueryRepository.java
public interface ProductQueryRepository {
    Optional<Product> findById(UUID productId);
    List<Product> findAll();
}
```

도메인 계층에서부터 읽기/쓰기의 계약이 분리된다.
`ProductCommandRepository`의 `findById`는 수정/삭제를 위한 엔티티 조회 목적이고,
`ProductQueryRepository`의 `findById`는 순수 조회 목적이다.

---

### Infrastructure 계층 (Adapter 분리)

**이전 (단일 Adapter)**

```java
// infrastructure/persistence/ProductRepositoryAdapter.java
@Repository
public class ProductRepositoryAdapter implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;
    // save, findById, findAll, delete 모두 구현
}
```

**이후 (Command/Query Adapter 분리)**

```java
// infrastructure/persistence/command/ProductCommandRepositoryAdapter.java
@Repository
public class ProductCommandRepositoryAdapter implements ProductCommandRepository {
    private final ProductJpaRepository productJpaRepository;
    // save, findById, delete 구현
}

// infrastructure/persistence/query/ProductQueryRepositoryAdapter.java
@Repository
public class ProductQueryRepositoryAdapter implements ProductQueryRepository {
    private final ProductJpaRepository productJpaRepository;
    // findById, findAll 구현
}
```

현재는 두 Adapter가 같은 `ProductJpaRepository`를 공유하지만,
향후 Query Adapter만 별도 읽기 DB나 QueryDSL 등으로 교체하는 것이 용이해진다.

---

### Presentation 계층 (Controller 주입 변경)

**이전**

```java
@RestController
public class ProductControllerImpl implements ProductController {
    private final ProductUseCase productUseCase; // 단일 UseCase
}
```

**이후**

```java
@RestController
public class ProductControllerImpl implements ProductController {
    private final ProductCommandUseCase productCommandUseCase; // 쓰기
    private final ProductQueryUseCase productQueryUseCase;     // 읽기

    public ResponseEntity<ProductResponse> create(...)  { return productCommandUseCase.create(...); }
    public ProductResponse getById(...)                  { return productQueryUseCase.getById(...); }
    public List<ProductResponse> getAll()                { return productQueryUseCase.getAll(); }
    public ProductResponse update(...)                   { return productCommandUseCase.update(...); }
    public ResponseEntity<Void> delete(...)              { productCommandUseCase.delete(...); ... }
}
```

Controller가 요청의 성격에 따라 Command/Query UseCase를 명시적으로 선택해서 호출한다.

---

## 패키지 구조 전체 비교

**이전 (클린 아키텍처)**

```
product/
├── domain/
│   ├── model/Product.java
│   └── repository/
│       └── ProductRepository.java
├── application/
│   ├── usecase/ProductUseCase.java
│   └── service/ProductApplicationService.java
├── infrastructure/
│   └── persistence/
│       ├── ProductJpaRepository.java
│       └── ProductRepositoryAdapter.java
└── presentation/
    ├── controller/
    └── dto/
```

**이후 (CQRS)**

```
product/
├── domain/
│   ├── model/Product.java
│   └── repository/
│       ├── command/ProductCommandRepository.java   ← 쓰기 계약
│       └── query/ProductQueryRepository.java       ← 읽기 계약
├── application/
│   ├── command/
│   │   ├── usecase/ProductCommandUseCase.java      ← 쓰기 UseCase
│   │   └── service/ProductCommandService.java
│   └── query/
│       ├── usecase/ProductQueryUseCase.java         ← 읽기 UseCase
│       └── service/ProductQueryService.java
├── infrastructure/
│   └── persistence/
│       ├── ProductJpaRepository.java               ← JPA (공유)
│       ├── command/ProductCommandRepositoryAdapter.java
│       └── query/ProductQueryRepositoryAdapter.java
└── presentation/
    ├── controller/
    └── dto/
```

---

## 이 프로젝트에서 적용한 CQRS 수준

CQRS는 적용 깊이에 따라 세 단계로 구분할 수 있다.

| 단계 | 설명 | 이 프로젝트 |
|------|------|:---------:|
| **논리적 분리** | 같은 DB, 같은 모델 / 코드 구조만 분리 | **현재 단계** |
| **모델 분리** | 쓰기는 도메인 모델, 읽기는 별도 Read Model(DTO 직접 조회) | - |
| **저장소 분리** | 쓰기 DB와 읽기 DB를 물리적으로 분리 + 이벤트 동기화 | - |

현재는 **논리적 분리** 단계다. 같은 JPA와 같은 DB를 사용하지만,
코드 경로가 Command/Query로 완전히 나뉘어 있어 이후 단계로의 확장 기반이 마련된 상태다.

---

## 장단점

### 장점

| 장점 | 설명 |
|------|------|
| **단일 책임** | Command Service는 쓰기만, Query Service는 읽기만 담당 |
| **독립적 최적화** | 읽기에 캐시/ReadOnly 트랜잭션, 쓰기에 트랜잭션/락을 독립적으로 적용 가능 |
| **독립적 스케일링** | 조회 트래픽이 폭증하면 Query 쪽만 스케일 아웃 가능 (저장소 분리 시) |
| **확장성** | Query Adapter만 QueryDSL, 읽기 전용 DB, Elasticsearch로 교체 가능 |
| **코드 명확성** | 메서드를 보면 읽기인지 쓰기인지 즉시 파악 가능 |

### 단점

| 단점 | 설명 |
|------|------|
| **파일 수 증가** | 같은 도메인에 Command/Query 쌍으로 파일이 2배로 늘어난다 |
| **단순 CRUD에는 과함** | 조회/쓰기 요구사항이 동일하다면 분리 이득이 없다 |
| **저장소 분리 시 정합성** | 읽기 DB와 쓰기 DB를 분리하면 최종 일관성(Eventual Consistency) 처리가 복잡해진다 |

> CQRS는 읽기/쓰기의 요구사항이 다르고, 트래픽 패턴이 비대칭일 때 진가를 발휘한다.
> 단순 CRUD 서비스에 적용하면 복잡도만 높아질 수 있다.