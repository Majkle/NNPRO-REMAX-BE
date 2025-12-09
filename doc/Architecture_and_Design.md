# Architektura a Design Systému (NNPRO-REMAX)

Tento dokument slouží jako vizuální a architektonická příloha k technické dokumentaci backendu. Zaměřuje se na strukturální rozvržení (UML Class a ERD diagramy), behaviorální modely (sekvenční diagramy, toky aktivit) a fyzickou infrastrukturu nasazení.

> 📘 **Hlavní dokumentace:** Textový popis technologií, instalace a API naleznete v souboru [Technical_documentation.md](./Technical_documentation.md).

## Obsah

**Technická Příručka (Externí):**
1. [Úvod a Technologie](./Technical_documentation.md#1-úvod-a-technologie)
2. [Architektura a Struktura Kódu](./Technical_documentation.md#2-architektura-a-struktura-kódu)
3. [Datová Vrstva a Migrace](./Technical_documentation.md#3-datová-vrstva-a-migrace)
4. [Zabezpečení (Security)](./Technical_documentation.md#4-zabezpečení-security)
5. [REST API a Kontrolery](./Technical_documentation.md#5-rest-api-a-kontrolery)
6. [Klíčové Funkcionality](./Technical_documentation.md#6-klíčové-funkcionality)
7. [Testování a Kvalita Kódu](./Technical_documentation.md#7-testování-a-kvalita-kódu)
8. [Instalace, Docker a Spuštění](./Technical_documentation.md#8-instalace-docker-a-spuštění)

**Architektura a Design (Tento dokument):**
9. [Strukturální pohled (Structural View)](#9-strukturální-pohled-structural-view)
10. [Behaviorální pohled (Behavioral View)](#10-behaviorální-pohled-behavioral-view)
11. [Infrastruktura a Nasazení (Deployment)](#11-infrastruktura-a-nasazení-deployment)

---
---

## 9. Strukturální pohled (Structural View)

Tato kapitola definuje statickou stavbu systému – od databázového schématu přes objektový model až po rozdělení do logických komponent.

### 9.1 Entity-Relationship Diagram (ERD)
Schéma databáze odpovídající `Liquibase` changelogům. Znázorňuje fyzické uložení dat.
*   **Inheritance (RealEstate):** Strategie `JOINED` (tabulky `real_estate`, `apartment`, `house`, `land`).
*   **Inheritance (User):** Strategie `SINGLE_TABLE` (tabulka `remax_user` s diskriminátorem).

```mermaid
erDiagram
    qremax_user ||--|| personal_information : "has details (FK)"
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

### 9.2 Class Diagram - Domain Model (Nemovitosti)
Diagram znázorňuje ORM mapování a polymorfismus u entit nemovitostí (`RealEstate`)  využívající strategii `JOINED` inheritance. Společná data jsou v abstraktní třídě, specifická v podtřídách. Cena je řešena historií vývoje (`PriceHistory`).

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

### 9.3 Class Diagram - User Hierarchy (Uživatelé)
Systém rolí je řešen dědičností `SINGLE_TABLE`. Osobní údaje jsou odděleny do entity `PersonalInformation` pro lepší modularitu a dodržování principu Separation of Concerns.

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

### 9.4 Component Diagram (Logická Architektura)
Aplikace je navržena jako **modulární monolit**. Diagram ukazuje závislosti mezi jednotlivými balíčky (features), které komunikují primárně skrze Service vrstvy.

```mermaid
classDiagram
    direction TB

    namespace Shared_Utilities {
        class Address_Module {
            AddressService
            AddressRepository
        }
        class Image_Module {
            ImageService
            ImageRepository
        }
        class Mail_Module {
            MailService
        }
    }

    namespace Core_Domain {
        class Profile_Module {
            ProfileService
            PersonalInformationService
            RemaxUserRepository
        }
        class Real_Estate_Module {
            RealEstateService
            RealEstateRepository
            RealEstateSpecification
        }
    }

    namespace Feature_Modules {
        class Security_Module {
            AuthService
            AdminService
            JwtUtil
        }
        class Meeting_Module {
            MeetingService
            MeetingRepository
        }
        class Review_Module {
            ReviewService
            ReviewRepository
        }
    }

    Security_Module ..> Mail_Module : Reset Password
    Security_Module ..> Profile_Module : UserDetails/Registration
    Security_Module ..> Address_Module : Create User Address

    Profile_Module ..> Address_Module : Personal Address
    Profile_Module ..> Image_Module : Avatar

    Real_Estate_Module ..> Address_Module : Property Address
    Real_Estate_Module ..> Image_Module : Gallery

    Meeting_Module ..> Profile_Module : Client & Realtor
    Meeting_Module ..> Real_Estate_Module : Property

    Review_Module ..> Profile_Module : Author & Target
```

---

## 10. Behaviorální pohled (Behavioral View)

Tato sekce popisuje dynamické chování systému, interakce uživatelů a toky dat.

### 10.1 Use Case Diagrams (Případy užití)
Diagramy vycházejí z nastavení `SecurityConfig` a anotací `@PreAuthorize`.

#### 10.1.1 Veřejnost a Klienti
Neregistrovaný uživatel (Guest) má přístup pouze k prohlížení a registraci.
Klient (`ROLE_USER`) má po přihlášení přístup k interakcím s makléři.
*   **Klíčová funkce:** Klient vidí statistiky makléřů (hodnocení), což mu pomáhá při výběru.

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

#### 10.1.2 Makléři a Administrátoři
Makléř (`ROLE_REALTOR`) spravuje portfolio a schůzky. Admin (`ROLE_ADMIN`) zajišťuje technickou správu a moderaci.

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

### 10.2 Sequence Diagrams (Sekvenční diagramy)
Zachycují časový průběh komunikace mezi komponentami pro "Happy Path" scénáře.

#### 10.2.1 Authentication Flow
Přihlášení uživatele, ověření blokace a vydání JWT tokenu.
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

#### 10.2.2 Real Estate Filtering Flow
Dynamické filtrování pomocí JPA Specification a Criteria API.
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

### 10.3 Activity Diagrams (Diagramy Aktivit)
Detailní popis algoritmů s rozhodovací logikou.

#### 10.3.1 Login & Brute-force Protection
Logika v `AuthService` chránící účet před hádáním hesel.
```mermaid
flowchart TD
    Start([Start Login Request]) --> FindUser{User exists?}
    FindUser -- No --> ErrorBadCreds[Throw BadCredentialsException]
    FindUser -- Yes --> CheckBlocked{Is Account Blocked?}

    CheckBlocked -- Yes --> CheckTime{BlockedUntil > Now?}
    CheckTime -- Yes --> ErrorBlocked[Throw 'Account is blocked']
    CheckTime -- No --> AuthAttempt[Attempt Authentication]
    CheckBlocked -- No --> AuthAttempt

    AuthAttempt --> ValidCreds{Password Valid?}

%% Success Path
    ValidCreds -- Yes --> ResetAttempts[Reset failedLoginAttempts = 0]
    ResetAttempts --> GenToken[Generate JWT Token]
    GenToken --> ReturnSuccess([Return AuthResponse])

%% Failure Path
    ValidCreds -- No --> IncAttempts[Increment failedLoginAttempts]
    IncAttempts --> CheckThreshold{Attempts >= Threshold?}

    CheckThreshold -- No --> SaveFail1[Save User State]
    SaveFail1 --> ErrorBadCreds

    CheckThreshold -- Yes --> BlockUser[Set Status = BLOCKED]
    BlockUser --> SetTimer[Set BlockedUntil = Now + 24h]
    SetTimer --> SaveFail2[Save User State]
    SaveFail2 --> ErrorBadCreds
```

#### 10.3.2 Real Estate Update & Price History
Logika v `RealEstateService` zajišťující verzování ceny a polymorfní update.
```mermaid
flowchart TD
    Start([Start Update Request]) --> Fetch[Fetch RealEstate by ID]
    Fetch --> Exists{Exists?}
    Exists -- No --> ErrorNotFound[Throw EntityNotFoundException]

    Exists -- Yes --> UpdateAddr{DTO has Address?}
    UpdateAddr -- Yes --> AddrService[Call AddressService.update]
    UpdateAddr -- No --> CheckPrice
    AddrService --> CheckPrice

    CheckPrice{DTO Price != Current Price?}
    CheckPrice -- Yes --> NewHistory[Create new PriceHistory entry]
    NewHistory --> AddHistory[Add to priceHistory list]
    AddHistory --> UpdateCommon
    CheckPrice -- No --> UpdateCommon[Update Common Fields\nName, Desc, Status...]

    UpdateCommon --> PolyCheck{Check Instance Type}

    PolyCheck -- Apartment --> UpApt[Update Floor, Elevator, Balcony...]
    PolyCheck -- House --> UpHouse[Update PlotArea, HouseType...]
    PolyCheck -- Land --> UpLand[Update IsForHousing]

    UpApt --> Save[Save Entity to DB]
    UpHouse --> Save
    UpLand --> Save

    Save --> End([Return Updated DTO])
```

#### 10.3.3 Password Reset Lifecycle
Tento diagram zachycuje dvoufázový proces obnovy hesla.
1.  **Žádost:** Generování unikátního kódu, jeho hashování pro databázi a odeslání v čitelné podobě e-mailem.
2.  **Potvrzení:** Validace přijatého kódu, kontrola expirační lhůty a samotná změna hesla.
```mermaid
flowchart TD
    subgraph Request [Fáze 1: Žádost o Reset]
        StartReq([Start: Request Reset]) --> CheckEmail{Email Exists?}
        CheckEmail -- No --> EndReq([Silent End])
        CheckEmail -- Yes --> GenCode[Generate UUID Code]
        GenCode --> Encode[Hash Code via BCrypt]
        Encode --> SetDead[Set Deadline +10 min]
        SetDead --> SaveUser1[Save User to DB]
        SaveUser1 --> SendMail[MailService: Send RAW Code]
    end

    subgraph Confirm [Fáze 2: Potvrzení kódem]
        StartConf([Start: Confirm Reset]) --> FindUser{Username Exists?}
        FindUser -- No --> ErrUser[Error: Invalid User]

        FindUser -- Yes --> MatchCode{Code Matches Hash?}
        MatchCode -- No --> ErrCode[Error: Invalid Code]

        MatchCode -- Yes --> CheckTime{Is Deadline Expired?}
        CheckTime -- Yes --> ErrTime[Error: Code Expired]

        CheckTime -- No --> HashPass[Hash New Password]
        HashPass --> ClearToken[Clear Code & Deadline]
        ClearToken --> SaveUser2[Save User to DB]
        SaveUser2 --> Success([Success: Password Changed])
    end
```

#### 10.3.4 Meeting Creation Validation
Diagram vizualizuje logiku v `MeetingService.createMeeting`. Důraz je kladen na **validaci cizích klíčů** a **kontrolu typů uživatelů** (instanceof check), aby bylo zajištěno, že schůzka propojuje skutečnou nemovitost, makléře a klienta.

```mermaid
flowchart TD
    Start([Start: Create Meeting]) --> MapDto[Map DTO to Entity]

    MapDto --> CheckRE{RealEstate Exists?}
    CheckRE -- No --> ErrRE[Throw EntityNotFound]

    CheckRE -- Yes --> CheckRealtor{Realtor Exists?}
    CheckRealtor -- No --> ErrRealtor[Throw EntityNotFound]

    CheckRealtor -- Yes --> TypeRealtor{Is instance of Realtor?}
    TypeRealtor -- No --> ErrRealtorType[Throw EntityNotFound\n 'User is not Realtor']

    TypeRealtor -- Yes --> CheckClient{Client Exists?}
    CheckClient -- No --> ErrClient[Throw EntityNotFound]

    CheckClient -- Yes --> TypeClient{Is instance of Client?}
    TypeClient -- No --> ErrClientType[Throw EntityNotFound\n 'User is not Client']

    TypeClient -- Yes --> Link[Link Entities to Meeting]
    Link --> Save[Save Meeting to DB]
    Save --> End([Return DTO])
```

#### 10.3.5 Review Creation Logic
Byznys pravidlo: Pouze Klient může hodnotit Makléře.
```mermaid
flowchart TD
    Start([Start: Create Review]) --> FetchAuthor[Fetch Current User\n 'from JWT/SecurityContext']

    FetchAuthor --> IsClient{Is User a Client?}
    IsClient -- No --> ErrRole[Throw IllegalArgument\n'Only Clients can write reviews']

    IsClient -- Yes --> FetchTarget[Fetch Target User by ID]
    FetchTarget --> IsRealtor{Is Target a Realtor?}
    IsRealtor -- No --> ErrTarget[Throw EntityNotFound\n'Target is not a Realtor']

    IsRealtor -- Yes --> Map[Map DTO to Entity]
    Map --> SetName[Set DisplayName\nfrom PersonalInfo]
    SetName --> Save[Save Review]
    Save --> End([Return Created Review])
```

### 10.4 State Machine Diagram (Stavové diagramy)
Životní cyklus schůzky (`Meeting`) v systému.

```mermaid
stateDiagram-v2
    [*] --> PENDING: Client creates Meeting
    PENDING --> CONFIRMED: Realtor accepts
    PENDING --> CANCELED: Realtor/Client cancels
    CONFIRMED --> CANCELED: Unexpected event
    CONFIRMED --> [*]: Meeting date passes
    CANCELED --> [*]
```

---

## 11. Infrastruktura a Nasazení (Deployment)

### 11.1 Deployment Architecture
Vizualizace kontejnerizace aplikace definované v `docker-compose.yml`. Ukazuje izolaci sítě a mapování portů.

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