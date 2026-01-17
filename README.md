

# Inflearn - 김영한 강사님 실전! 스프링 부트와 JPA 활용(1) - 웹 애플리케이션 개발 강의 정리

# JPA Shop

## 📋 프로젝트 개요

JPA Shop은 Spring Boot와 JPA를 활용하여 구축한 전자상거래 플랫폼입니다. 회원 관리, 상품 관리, 주문 처리 등 핵심적인 쇼핑몰 기능을 객체지향 설계 원칙에 따라 구현했습니다.

### 🎯 주요 기능
- **회원 관리**: 회원 가입, 조회, 주소 정보 관리
- **상품 관리**: 도서, 앨범, 영화 등 다양한 상품 타입 지원
- **주문 시스템**: 상품 주문, 주문 취소, 배송 상태 추적
- **관리자 기능**: 상품 재고 관리, 주문 내역 조회 및 검색

---

## 🛠️ 기술 스택

### 백엔드 기술
- **Java 17**: 최신 LTS 버전으로 안정성과 성능 보장
- **Spring Boot 3.5.10**: 최신 Spring Boot 프레임워크
- **Spring Data JPA**: 데이터베이스 ORM 처리
- **Spring MVC**: 웹 애플리케이션 프레임워크
- **Spring Validation**: 입력 데이터 검증
- **Thymeleaf**: 서버사이드 템플릿 엔진

### 데이터베이스
- **H2 Database**: 개발 및 테스트용 인메모리 데이터베이스
- **Hibernate**: JPA 구현체

### 개발 도구
- **Lombok**: 반복적인 코드 자동 생성
- **Spring Boot DevTools**: 개발 생산성 향상
- **Gradle**: 의존성 관리 및 빌드 도구

---

## 🏗️ 아키텍처 설계

### 레이어드 아키텍처 (Layered Architecture)
```
┌─────────────────────────────────────┐
│           Web Layer                │  ← Controller
├─────────────────────────────────────┤
│          Service Layer             │  ← Business Logic
├─────────────────────────────────────┤
│         Repository Layer           │  ← Data Access
├─────────────────────────────────────┤
│          Domain Layer              │  ← Entity, Value Object
└─────────────────────────────────────┘
```

### 패키지 구조
```
jpabook.jpashop/
├── domain/           # 도메인 모델 (엔티티, 값 객체)
├── repository/       # 데이터 접근 계층
├── service/          # 비즈니스 로직 계층
├── web/              # 웹 컨트롤러 계층
└── exception/        # 커스텀 예외 처리
```

---

## 📊 도메인 모델링

### 핵심 도메인 설계

#### 1. 회원 (Member)
```java
@Entity
public class Member {
    @Id @GeneratedValue
    private Long id;
    private String name;
    @Embedded
    private Address address;
    @OneToMany(mappedBy = "member")
    private List<Order> orders;
}
```

**설계 특징:**
- `@Embedded`를 사용한 주소 정보 캡슐화
- 일대다 관계를 통한 주문 이력 관리
- 값 객체(Value Object) 패턴 적용

#### 2. 주문 (Order)
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Delivery delivery;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
```

**설계 특징:**
- 복합적인 연관관계 설계 (다대일, 일대다, 일대일)
- `FetchType.LAZY`를 통한 성능 최적화
- Enum을 활용한 상태 관리
- 도메인 로직 포함 (주문 취소, 가격 계산)

#### 3. 상품 (Item) - 상속 전략
```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
public abstract class Item {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
}

@Entity
@DiscriminatorValue("B")
public class Book extends Item {
    private String author;
    private String isbn;
}
```

**설계 특징:**
- 단일 테이블 전략을 통한 상속 관계 구현
- 도서, 앨범, 영화 등 다양한 상품 타입 지원
- 추상 클래스를 통한 공통 속성 관리

---

## 🔧 핵심 기능 구현

### 1. 트랜잭션 관리
```java
@Service
@Transactional(readOnly = true)
public class OrderService {
    
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        // 주문 로직 구현
        // 트랜잭션 경계에서 데이터 일관성 보장
    }
}
```

**특징:**
- 선언적 트랜잭션 관리
- 읽기 전용 최적화 (`readOnly = true`)
- 메서드 레벨 트랜잭션 제어

### 2. 도메인 로직 캡슐화
```java
public class Order {
    /** 주문 취소 */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }
}
```

**특징:**
- 도메인 모델에 비즈니스 로직 위치
- 상태 기반의 비즈니스 규칙 구현
- 예외 처리를 통한 데이터 무결성 보장

### 3. Repository 패턴
```java
@Repository
public class OrderRepository {
    
    public List<Order> findAllByString(OrderSearch orderSearch) {
        // 동적 쿼리를 통한 주문 검색
        // JPQL을 활용한 복잡한 조회 처리
    }
}
```

**특징:**
- 데이터 접근 로직 분리
- JPQL을 통한 복잡한 쿼리 처리
- 검색 조건에 따른 동적 쿼리 지원

---

## 🎨 프론트엔드 구현

### Thymeleaf 템플릿 엔진
- **서버사이드 렌더링**: SEO 최적화 및 초기 로딩 속도 향상
- **자연스러운 템플릿**: HTML 구조 유지하면서 동적 콘텐츠 삽입
- **레이아웃 조각**: 헤더, 푸터 등 공통 요소 재사용

### 반응형 웹 디자인
- **Bootstrap 프레임워크**: 모바일 친화적인 UI/UX
- **커스텀 CSS**: 현대적인 다크 테마 디자인
- **그라데이션 및 애니메이션**: 시각적 효과 강화

### 주요 페이지 구성
1. **홈페이지**: 메인 네비게이션 및 기능 소개
2. **회원 관리**: 가입 폼, 회원 목록 조회
3. **상품 관리**: 상품 등록, 수정, 목록 조회
4. **주문 시스템**: 주문 폼, 주문 내역, 검색 기능

---

## 🚀 성능 최적화

### 1. 지연 로딩 (Lazy Loading)
```java
@ManyToOne(fetch = FetchType.LAZY)
private Member member;

@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<OrderItem> orderItems;
```

**효과:**
- 불필요한 데이터 로딩 방지
- 메모리 사용량 최적화
- 쿼리 성능 향상

### 2. 영속성 전이 (Cascade)
```java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
private List<OrderItem> orderItems;
```

**효과:**
- 연관된 엔티티 자동 저장/삭제
- 코드 간소화
- 데이터 일관성 보장

### 3. 데이터베이스 설정 최적화
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
        format_sql: true
        show_sql: true

logging:
  level:
    org.hibernate.SQL: debug
```

**효과:**
- SQL 로깅을 통한 성능 모니터링
- 자동 스키마 생성
- 개발 생산성 향상

---

## 🔒 데이터 검증 및 예외 처리

### 입력 데이터 검증
```java
public class MemberForm {
    @NotEmpty(message = "회원 이름은 필수입니다.")
    private String name;
    private String city;
    private String street;
    private String zipcode;
}
```

**특징:**
- Bean Validation을 활용한 입력 검증
- 커스텀 에러 메시지 지원
- 클라이언트 및 서버 사이드 검증

### 커스텀 예외 처리
```java
public class NotEnoughStockException extends RuntimeException {
    public NotEnoughStockException() {
        super("재고 수량이 부족합니다.");
    }
}
```

**특징:**
- 비즈니스 예외 정의
- 의미 있는 에러 메시지 제공
- 글로벌 예외 처리 지원

---

## 📈 확장성 및 유지보수

### 1. 개방-폐쇄 원칙 (OCP)
- 새로운 상품 타입 추가 시 기존 코드 수정 없이 확장
- 인터페이스를 통한 유연한 설계

### 2. 의존성 주입 (DI)
- `@RequiredArgsConstructor`를 통한 생성자 주입
- 테스트 용이성 향상
- 결합도 감소

### 3. 설정 외부화
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:h2:tcp://localhost/~/jpashop
```

**효과:**
- 환경별 설정 관리 용이
- 배포 및 운영 편의성 향상

---

## 🧪 테스트 전략

### 단위 테스트
- Service Layer 비즈니스 로직 검증
- Repository Layer 데이터 접근 테스트
- 도메인 모델 단위 테스트

### 통합 테스트
- Controller Layer API 테스트
- 전체 주문 프로세스 테스트
- 데이터베이스 연동 테스트

---

## 🚀 배포 및 운영

### 개발 환경
- **H2 Database**: 인메모리 데이터베이스로 빠른 개발
- **DevTools**: 자동 리로딩으로 개발 생산성 향상
- **포트 8081**: 기본 포트와의 충돌 방지

### 프로덕션 환경 고려사항
- 데이터베이스 전환 (MySQL, PostgreSQL)
- 보안 설정 강화
- 로깅 및 모니터링 구축
- 성능 튜닝 및 스케일링

---

## 💡 학습 포인트 및 성과

### 기술적 성취
1. **JPA 심화 활용**: 복잡한 연관관계 매핑, 상속 전략 구현
2. **도메인 주도 설계**: 비즈니스 로직 캡슐화, 객체지향 원칙 적용
3. **스프링 부트 심화**: 트랜잭션 관리, 설정 최적화
4. **성능 최적화**: 지연 로딩, 쿼리 튜닝, 메모리 관리

### 설계 원칙 적용
- **단일 책임 원칙**: 각 클래스의 명확한 책임 분리
- **개방-폐쇄 원칙**: 확장 가능한 아키텍처 설계
- **의존성 역전 원칙**: 추상화에 의존하는 구조
- **인터페이스 분리 원칙**: 클라이언트 중심의 인터페이스 설계

---

## 🔮 향후 개선 방안

### 기능 확장
- 결제 시스템 연동 (PG사 API)
- 장바구니 기능 구현
- 상품 리뷰 및 평점 시스템
- 회원 등급 및 할인 정책

### 기술적 개선
- Redis를 활용한 캐싱 전략
- Querydsl을 통한 동적 쿼리 개선
- API 문서화 (Swagger/OpenAPI)
- 마이크로서비스 아키텍처로의 전환

### 운영적 개선
- CI/CD 파이프라인 구축
- 컨테이너화 (Docker)
- 모니터링 및 로깅 시스템 강화
- 보안 취약점 점검 및 개선(XSS, SQLi, 주통기 웹 취약점 진단 근거)

---

