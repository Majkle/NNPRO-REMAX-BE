### 13. UML Diagrams (Architektura a Design)

Tato sekce vizualizuje klíčové struktury a toky v aplikaci pomocí UML diagramů.

#### 13.1 Class Diagram - Domain Model (Nemovitosti)
Diagram znázorňuje polymorfismus u entit nemovitostí (`RealEstate`) využívající strategii `JOINED` inheritance.

```mermaid
classDiagram
    class RealEstate {
        +Long id
        +String name
        +Double price
        +Double usableArea
        +Status status
        +ContractType contractType
        +Address address
        +List~Image~ images
        +List~PriceHistory~ history
    }

    class Apartment {
        +Integer floor
        +Boolean elevator
        +Boolean balcony
        +ApartmentOwnershipType ownership
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

    RealEstate <|-- Apartment : extends
    RealEstate <|-- House : extends
    RealEstate <|-- Land : extends
    RealEstate *-- Address : composition (OneToOne)
```

#### 13.2 Class Diagram - User Hierarchy (Uživatelé)
Systém rolí je řešen dědičností `SINGLE_TABLE`.

```mermaid
classDiagram
    class RemaxUser {
        <<Abstract>>
        +String username
        +String email
        +String password
        +AccountStatus accountStatus
        +PersonalInformation personalInfo
    }

    class Admin {
        +createRealtor()
        +blockUser()
    }

    class Realtor {
        +int licenseNumber
        +String about
        +List~Meeting~ meetings
    }

    class Client {
        +List~Review~ reviewsWritten
    }

    class PersonalInformation {
        +String firstName
        +String lastName
        +String phoneNumber
        +Address address
    }

    RemaxUser <|-- Admin
    RemaxUser <|-- Realtor
    RemaxUser <|-- Client
    RemaxUser *-- PersonalInformation
```

---

### New Section: 14. Entity-Relationship Diagram (ERD)

Schéma databáze generované na základě JPA entit. Zvýrazňuje vztahy mezi tabulkami a cizí klíče.

```mermaid
erDiagram
    remax_user ||--|| personal_information : "has details"
    personal_information ||--|| address : "lives at"
    
    real_estate ||--|| address : "located at"
    real_estate ||--|{ image : "contains"
    real_estate ||--|{ price_history : "tracks"
    real_estate ||--|{ meeting : "has appointments"
    
    meeting }|--|| remax_user : "realtor assigned"
    meeting }|--|| remax_user : "client requests"
    
    review }|--|| remax_user : "author (client)"
    review }|--|| remax_user : "target (realtor)"

    remax_user {
        bigint id PK
        string username
        string user_type "Discriminator"
    }

    real_estate {
        bigint id PK
        string name
        double price
    }

    meeting {
        bigint id PK
        timestamp time
        string status
    }
```

---

### New Section: 15. Sequence Diagrams (Toky Aplikace)

#### 15.1 Authentication Flow (Login)
Ukázka procesu přihlášení, validace a vydání JWT tokenu.

```mermaid
sequenceDiagram
    participant Client as Frontend/Client
    participant AC as AuthController
    participant AS as AuthService
    participant UR as UserRepository
    participant JWT as JwtUtil
    
    Client->>AC: POST /api/auth/login (username, pass)
    AC->>AS: login(username, pass)
    AS->>UR: findByUsername(username)
    UR-->>AS: RemaxUser entity
    
    alt Invalid Password or Blocked
        AS-->>AC: Throw BadCredentialsException
        AC-->>Client: 401 Unauthorized
    else Valid Credentials
        AS->>JWT: generateToken(username)
        JWT-->>AS: String (JWT)
        AS-->>AC: AuthResponse (token, role)
        AC-->>Client: 200 OK + JSON
    end
```

#### 15.2 Real Estate Filtering Flow
Ukázka dynamického filtrování pomocí JPA Specification.

```mermaid
sequenceDiagram
    participant Client
    participant Controller as RealEstateController
    participant Service as RealEstateService
    participant Spec as RealEstateSpecification
    participant Repo as RealEstateRepository
    participant DB as PostgreSQL

    Client->>Controller: GET /api/real-estates?city=Prague&minPrice=2M
    Controller->>Service: searchRealEstates(filterDto)
    Service->>Spec: filterBy(filterDto)
    Spec-->>Service: Specification<RealEstate>
    Service->>Repo: findAll(Specification, Pageable)
    Repo->>DB: SQL Select with Where Clauses
    DB-->>Repo: Result Set
    Repo-->>Service: Page<RealEstate>
    Service-->>Controller: Page<RealEstate>
    Controller->>Client: JSON List
```

---

### New Section: 16. State Machine Diagrams (Stavové diagramy)

#### 16.1 Meeting Lifecycle
Stavový diagram pro entitu `Meeting` (`PENDING`, `CONFIRMED`, `CANCELED`).

```mermaid
stateDiagram-v2
    [*] --> PENDING: Client creates request
    PENDING --> CONFIRMED: Realtor accepts
    PENDING --> CANCELED: Realtor/Client declines
    CONFIRMED --> CANCELED: Unexpected event
    CONFIRMED --> [*]: Meeting happened
    CANCELED --> [*]
```

---

### New Section: 17. Deployment Architecture

Vizualizace nasazení pomocí Docker Compose.

```mermaid
graph TD
    subgraph Host Machine
        subgraph Docker Network [remax-network]
            FE[Frontend Container<br/>React + Nginx<br/>Port: 3000]
            BE[Backend Container<br/>Spring Boot<br/>Port: 8080]
            DB[Database Container<br/>PostgreSQL 17<br/>Port: 5432]
        end
        
        Browser[User Browser]
    end

    Browser -->|HTTP/80| FE
    Browser -->|REST API/8080| BE
    BE -->|JDBC/5432| DB
    FE -.->|AJAX Calls| BE
```

---
