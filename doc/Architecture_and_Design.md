### 13. UML Diagrams (Architektura a Design)

Tato sekce vizualizuje klíčové struktury a toky v aplikaci pomocí UML diagramů.

#### 13.1 Class Diagram - Domain Model (Nemovitosti)
Diagram znázorňuje polymorfismus u entit nemovitostí (`RealEstate`) využívající strategii `JOINED` inheritance. Společná data jsou v abstraktní třídě, specifická v podtřídách. Cena je řešena historií vývoje (`PriceHistory`).
```mermaid
classDiagram
    class RealEstate {
        <<Abstract>>
        +Long id
        +String name
        +String description
        +Status status
        +Double usableArea
        +ContractType contractType
        +PriceDisclosure priceDisclosure
        +Commission commission
        +Taxes taxes
        +ZonedDateTime listedAt
        +Address address
        +BuildingProperties buildingProperties
        +Utilities utilities
        +List~PriceHistory~ priceHistory
        +List~Image~ image
    }

    class Apartment {
        +Integer floor
        +Integer totalFloors
        +Boolean elevator
        +Boolean balcony
        +Integer rooms
        +ApartmentOwnershipType ownershipType
    }

    class House {
        +Double plotArea
        +Integer stories
        +HouseType houseType
    }

    class Land {
        +Boolean isForHousing
    }

    class Address {
        +String city
        +String street
        +String postalCode
        +AddressRegion region
    }

    class PriceHistory {
        +Double price
        +ZonedDateTime timestamp
    }

    class Image {
        +String filename
        +byte[] data
    }

    RealEstate <|-- Apartment : extends
    RealEstate <|-- House : extends
    RealEstate <|-- Land : extends
    RealEstate *-- Address : OneToOne
    RealEstate *-- PriceHistory : OneToMany
    RealEstate *-- Image : OneToMany
```

#### 13.2 Class Diagram - User Hierarchy (Uživatelé)
Systém rolí je řešen dědičností `SINGLE_TABLE`. Osobní údaje jsou odděleny do entity `PersonalInformation` pro lepší modularitu.

```mermaid
classDiagram
    class RemaxUser {
        <<Abstract>>
        +String username
        +String email
        +String password
        +AccountStatus accountStatus
        +PersonalInformation personalInformation
    }

    class Admin {
        +createRealtor()
        +blockUser()
    }

    class Realtor {
        +int licenseNumber
        +String about
    }

    class Client {

    }

    class PersonalInformation {
        +String firstName
        +String lastName
        +String phoneNumber
        +ZonedDateTime birthDate
        +Address address
        +Image image
    }

    RemaxUser <|-- Admin
    RemaxUser <|-- Realtor
    RemaxUser <|-- Client
    RemaxUser *-- PersonalInformation : OneToOne
    PersonalInformation *-- Address : OneToOne
```

---

### 14. Entity-Relationship Diagram (ERD)

Schéma databáze odpovídající `Liquibase` changelogům.
- **Inheritance (RealEstate):** JOINED (tabulky `real_estate`, `apartment`, `house`, `land`).
- **Inheritance (User):** SINGLE_TABLE (pouze tabulka `remax_user` s diskriminátorem `user_type`).
- 
```mermaid
erDiagram
    remax_user ||--|| personal_information : "has details (FK)"
    personal_information ||--|| address : "lives at (FK)"
    personal_information ||--|{ image : "avatar (FK)"

    real_estate ||--|| address : "located at (FK)"
    real_estate ||--|{ image : "gallery (FK)"
    real_estate ||--|{ price_history : "tracks price (FK)"

    real_estate ||--o| apartment : "subtype (Joined)"
    real_estate ||--o| house : "subtype (Joined)"
    real_estate ||--o| land : "subtype (Joined)"

    meeting }|--|| remax_user : "realtor assigned"
    meeting }|--|| remax_user : "client requests"
    meeting }|--|| real_estate : "concerns"

    review }|--|| remax_user : "author (Client)"
    review }|--|| remax_user : "target (Realtor)"

    remax_user {
        bigint id PK
        string username
        string user_type "Discriminator (ADMIN, REALTOR, CLIENT)"
        string account_status
    }

    real_estate {
        bigint id PK
        string name
        string description
        string status
    }

    price_history {
        bigint id PK
        double price
        timestamp timestamp
    }

    meeting {
        bigint id PK
        timestamp meeting_time
        string meeting_status
        string meeting_type
    }
```

---

### 15. Sequence Diagrams (Toky Aplikace)

#### 15.1 Authentication Flow (Login)
Proces přihlášení, validace účtu (zda není blokován) a vydání JWT tokenu.

```mermaid
sequenceDiagram
    participant Client as Frontend/Client
    participant AC as AuthController
    participant AS as AuthService
    participant UR as RemaxUserRepository
    participant AM as AuthManager
    participant JWT as JwtUtil

    Client->>AC: POST /api/auth/login (username, pass)
    AC->>AS: login(username, pass)
    AS->>UR: findByUsername(username)
    UR-->>AS: RemaxUser entity

    alt User Blocked
        AS-->>AC: Throw BadCredentialsException
    else Valid User
        AS->>AM: authenticate(token)
        AM-->>AS: Authentication Success
        AS->>UR: resetFailedLoginAttempts()
        AS->>JWT: generateToken(username)
        JWT-->>AS: String (JWT)
        AS-->>AC: AuthResponse (token, role, expiresAt)
        AC-->>Client: 200 OK + JSON
    end
```

#### 15.2 Real Estate Filtering Flow
Ukázka dynamického filtrování pomocí `RealEstateSpecification` a JPA Criteria API.

```mermaid
sequenceDiagram
    participant Client
    participant Controller as RealEstateController
    participant Service as RealEstateService
    participant Spec as RealEstateSpecification
    participant Repo as RealEstateRepository
    participant DB as PostgreSQL

    Client->>Controller: GET /api/real-estates?city=Prague&minPrice=5M&rooms=3
    Controller->>Service: searchRealEstates(filterDto, pageable)
    Service->>Spec: filterBy(filterDto)
    Note right of Spec: Builds predicates for<br/>Type, PriceHistory, Address, etc.
    Spec-->>Service: Specification<RealEstate>
    Service->>Repo: findAll(Specification, Pageable)
    Repo->>DB: SQL Select with JOINs & WHERE
    DB-->>Repo: Result Set
    Repo-->>Service: Page<RealEstate>
    Service->>Controller: Page<RealEstate> (Mapped to DTOs)
    Controller->>Client: JSON Page<RealEstateDto>
```

---

### 16. State Machine Diagrams (Stavové diagramy)

#### 16.1 Meeting Lifecycle
Životní cyklus schůzky řízený přes `MeetingService`.

```mermaid
stateDiagram-v2
    [*] --> PENDING: Client creates Meeting
    PENDING --> CONFIRMED: Realtor/Admin accepts
    PENDING --> CANCELED: Realtor/Client cancels
    CONFIRMED --> CANCELED: Unexpected event
    CONFIRMED --> [*]: Meeting date passes
    CANCELED --> [*]
```

---

### 17. Deployment Architecture

Vizualizace nasazení definovaná v `docker-compose.yml`.

```mermaid
graph TD
    subgraph Host Machine
        subgraph Docker Network [remax-network]
            FE[Frontend Container<br/>React + Nginx<br/>Port: 3000]
            BE[Backend Container<br/>Spring Boot<br/>Port: 8080]
            DB[Database Container<br/>PostgreSQL 17<br/>Port: 5432]
            MH[MailHog Container<br/>SMTP Mock<br/>Ports: 1025/8025]
        end

        Browser[User Browser]
    end

    Browser -->|HTTP/80| FE
    Browser -->|REST API/8080| BE
    Browser -->|Web UI/8025| MH
    BE -->|JDBC/5432| DB
    BE -->|SMTP/1025| MH
    FE -.->|AJAX Calls| BE
```

---
