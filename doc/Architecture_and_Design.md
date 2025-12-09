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
### 13.3 Use Case Diagrams (Případy užití)

Tato sekce definuje, jaké akce mohou jednotliví aktéři v systému provádět. Diagramy vycházejí z nastavení `SecurityConfig` a anotací `@PreAuthorize` v kontrolerech.

#### 13.3.1 Public & Client Actors (Veřejnost a Klienti)
Neregistrovaný uživatel (Guest) má přístup pouze k prohlížení a registraci. Po přihlášení získává Klient (`ROLE_USER`) možnost interagovat se systémem.

**Důležité:** Klient má přístup ke **statistikám makléře** (hodnocení), což je klíčové pro rozhodování, kterého makléře oslovit.

```mermaid
graph LR
    %% Actors
    Guest((Návštěvník))
    Client((Klient / User))

    %% Use Cases
    subgraph "Autentizace & Profil"
        UC_Reg(Registrace)
        UC_Log(Přihlášení)
        UC_Reset(Reset hesla)
        UC_Profile(Editace profilu)
    end

    subgraph "Nemovitosti"
        UC_Search(Vyhledávání & Filtrace)
        UC_Detail(Zobrazení detailu)
    end

    subgraph "Interakce a Důvěra"
        UC_ReqMeet(Žádost o prohlídku)
        UC_MyMeet(Správa mých schůzek)
        UC_Review(Napsat recenzi makléři)
        UC_EditReview(Editace vlastní recenze)
        UC_Stats(Zobrazení hodnocení makléře)
    end

    %% Relations Guest
    Guest --> UC_Search
    Guest --> UC_Detail
    Guest --> UC_Reg
    Guest --> UC_Log
    Guest --> UC_Reset

    %% Relations Client
    Client -.->|extends| Guest
    Client --> UC_Profile
    Client --> UC_ReqMeet
    Client --> UC_MyMeet
    Client --> UC_Review
    Client --> UC_EditReview
    Client --> UC_Stats

    style Guest fill:#f9f,stroke:#333,stroke-width:2px
    style Client fill:#bbf,stroke:#333,stroke-width:2px
```

#### 13.3.2 Management Actors (Makléři a Administrátoři)
Makléř (`ROLE_REALTOR`) spravuje své portfolio. Statistiky (UC_Stats) vidí také, ale v kontextu zpětné vazby na svou vlastní práci. Administrátor (`ROLE_ADMIN`) zajišťuje technickou správu.

```mermaid
graph LR
    %% Actors
    Realtor((Makléř))
    Admin((Admin))

    %% Use Cases Realtor
    subgraph "Správa Portfolia (Realtor)"
        UC_CreateRE(Vytvořit nemovitost)
        UC_EditRE(Editovat nemovitost)
        UC_Img(Upload/Delete fotek)
        UC_ManageMeet(Potvrzování schůzek)
        UC_Stats_Own(Zobrazení vlastních statistik)
    end

    %% Use Cases Admin
    subgraph "Administrace (Admin)"
        UC_Block(Blokace/Odblokování uživatele)
        UC_CreatePro(Manuální registrace Makléře/Admina)
        UC_DelUser(Smazání uživatele)
        UC_ModReview(Smazání nevhodné recenze)
        UC_ListUsers(Výpis všech uživatelů)
    end

    %% Relations Realtor
    Realtor --> UC_CreateRE
    Realtor --> UC_EditRE
    Realtor --> UC_Img
    Realtor --> UC_ManageMeet
    Realtor --> UC_Stats_Own

    %% Relations Admin
    Admin --> UC_Block
    Admin --> UC_CreatePro
    Admin --> UC_DelUser
    Admin --> UC_ModReview
    Admin --> UC_ListUsers

    style Realtor fill:#dfd,stroke:#333,stroke-width:2px
    style Admin fill:#faa,stroke:#333,stroke-width:2px
```

**Vysvětlení oprávnění:**
*   **Klient:** Může editovat pouze své vlastní údaje a recenze. Statistiky makléřů (`GET /api/reviews/stats/{id}`) jsou přístupné všem přihlášeným uživatelům.
*   **Makléř:** Má právo manipulovat s entitami `RealEstate`, `Image` a potvrzovat schůzky.
*   **Admin:** Má nejvyšší oprávnění pro správu účtů, včetně blokování uživatelů a mazání recenzí (moderace), ale standardně nezasahuje do obchodních dat nemovitostí.
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
