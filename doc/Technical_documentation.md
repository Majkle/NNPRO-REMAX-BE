# Technická Příručka - Backend (NNPRO-REMAX-BE)

Tento dokument poskytuje kompletní technický přehled backendové části realitního portálu. Aplikace je postavena na **Java 21** a frameworku **Spring Boot**, využívá relační databázi **PostgreSQL** a komunikuje prostřednictvím **REST API**.

## Obsah
1. [Přehled Projektu](#1-přehled-projektu)
2. [Technologický Stack](#2-technologický-stack)
3. [Architektura Aplikace](#3-architektura-aplikace)
4. [Struktura Projektu](#4-struktura-projektu)
5. [Datové Modely a Databáze](#5-datové-modely-a-databáze)
6. [REST API a Kontrolery](#6-rest-api-a-kontrolery)
7. [Security a Autentizace](#7-security-a-autentizace)
8. [Pokročilé Funkcionality](#8-pokročilé-funkcionality)
9. [Vývojové Prostředí](#9-vývojové-prostředí)
10. [Docker a Deployment](#10-docker-a-deployment)
11. [Testování](#11-testování)
12. [Databázové Migrace](#12-databázové-migrace)

---

## 1. Přehled Projektu

Backend slouží jako centrální bod pro správu dat a logiky realitního portálu. Zajišťuje perzistenci dat o nemovitostech, uživatelích, schůzkách a recenzích. Poskytuje zabezpečené API pro frontendovou aplikaci a spravuje složitější byznys logiku, jako je filtrování nemovitostí nebo správa oprávnění.

---

## 2. Technologický Stack

### 2.1 Core
- **Java 21** - Programovací jazyk
- **Spring Boot 3.5.6** - Aplikační framework
- **Maven** - Build tool a dependency management

### 2.2 Data a Perzistence
- **PostgreSQL 17** - Relační databáze
- **Spring Data JPA (Hibernate)** - ORM vrstva
- **Liquibase 5.0.1** - Verzování databázového schématu (migrations)

### 2.3 Security
- **Spring Security** - Autentizace a autorizace
- **JWT (JSON Web Token)** - Stateless autentizace
- **BCrypt** - Hashování hesel

### 2.4 Dokumentace a Utility
- **SpringDoc OpenAPI (Swagger UI)** - Automatická dokumentace API
- **Lombok** - Redukce boilerplate kódu
- **ModelMapper (Custom)** - Mapování mezi Entity a DTO

---

## 3. Architektura Aplikace

Projekt dodržuje klasickou **vrstvenou architekturu** (Layered Architecture):

```
┌───────────────────────────────┐
│       Controller Layer        │ REST Endpointy, validace vstupu (DTO)
├───────────────────────────────┤
│        Service Layer          │ Byznys logika, transakce, mapování
├───────────────────────────────┤
│       Repository Layer        │ JPA Interfaces, DB queries, Specifications
├───────────────────────────────┤
│        Database Layer         │ PostgreSQL (Tables, Constraints)
└───────────────────────────────┘
```

### 3.1 Klíčové Principy
- **Dependency Injection**: Využití Spring IoC kontejneru (`@Service`, `@RestController`, `@RequiredArgsConstructor`).
- **DTO Pattern**: Oddělení interních entit od veřejného API (`Meeting` vs `MeetingDto`).
- **Exception Handling**: Centralizovaná správa chyb pomocí `@ControllerAdvice` (`RestApiExceptionHandler`).

---

## 4. Struktura Projektu

Zdrojový kód je organizován podle **doménových balíčků** (feature-based packaging), což usnadňuje orientaci.

```
src/main/java/cz/upce/nnpro/remax/
├── address/                # Správa adres (Entity, Service, Repository)
├── config/                 # Globální konfigurace (např. OpenAPI/Swagger)
├── images/                 # Upload a správa obrázků (ukládání do DB)
├── mail/                   # Služba pro odesílání e-mailů (šablony, SMTP)
├── meetings/               # Logika schůzek (Meeting, stavy schůzek)
├── profile/                # Správa uživatelů a profilů
│   ├── controller/
│   ├── dto/
│   ├── entity/             # RemaxUser, Admin, Realtor, Client, PersonalInformation
│   ├── repository/
│   └── service/
├── realestates/            # Logika nemovitostí
│   ├── controller/
│   ├── dto/                # Filtrovací DTO
│   ├── entity/             # Dědičnost RealEstate (Apartment, House, Land)
│   ├── repository/
│   └── service/            # Včetně RealEstateSpecification (filtrování)
├── review/                 # Logika recenzí a hodnocení makléřů
├── security/               # Konfigurace bezpečnosti a autentizace
│   ├── admin/              # Admin-only operace (blokování uživatelů, vytváření rolí)
│   ├── auth/               # Login, Register, Refresh token logic
│   ├── config/             # SecurityConfig, AdminInitializer
│   └── jwt/                # Generování a validace JWT tokenů
└── RemaxApplication.java   # Vstupní bod aplikace (Main class)
```

---

## 5. Datové Modely a Databáze

Datová vrstva je postavena na **JPA (Hibernate)** a využívá relační databázi PostgreSQL. Entity nejsou centralizovány v jednom balíčku, ale jsou umístěny v příslušných doménových modulech (např. `realestates/entity`, `profile/entity`).

### 5.1 Polymorfismus Entit
Aplikace využívá pokročilé mapování dědičnosti:

#### Nemovitosti (`RealEstate`)
Používá strategii **`InheritanceType.JOINED`**. To znamená, že existuje hlavní tabulka pro společná data a samostatné tabulky pro specifické atributy podtříd. Při dotazování Hibernate automaticky provádí `JOIN`.
- **Tabulka `real_estate`:** ID, název, popis, adresa, základní parametry.
    - **Cena:** Není uložena přímo v tabulce `real_estate`, ale dynamicky se načítá z relace `OneToMany` do tabulky `price_history`.
- **Podtabulky:**
    - `apartment` (patro, výtah, balkon...)
    - `house` (plocha pozemku, typ domu...)
    - `land` (určeno k bydlení)

#### Uživatelé (`RemaxUser`)
Používá strategii **`InheritanceType.SINGLE_TABLE`**. Všechna data jsou v jedné tabulce `remax_user`, což zajišťuje vysoký výkon při přihlašování (není třeba joinovat).
- **Diskriminátor:** Sloupec `user_type` (hodnoty: `ADMIN`, `REALTOR`, `CLIENT`).
- **Tabulka `remax_user`:** Login, heslo, email, ale i specifické sloupce jako `license_number` (pro makléře), které jsou u ostatních rolí `NULL`.

### 5.2 Správa Obrázků (`Image`)
Obrázky jsou řešeny jako samostatná entita `Image` (tabulka `image`).
- **Uložení:** Binární data (`byte[]`) jsou uložena ve sloupci typu `OID` (Postgres) / `@Lob` (JPA).
- **Vazby:**
    - `RealEstate` má vazbu `OneToMany` na `Image` (galerie nemovitosti).
    - `PersonalInformation` má vazbu `OneToOne` na `Image` (profilová fotka).

### 5.3 Klíčové Vztahy (ERD)
- **RealEstate ↔ PriceHistory:** Historie vývoje ceny v čase (`OneToMany`).
- **RemaxUser ↔ PersonalInformation:** Oddělení přihlašovacích údajů od osobních dat (`OneToOne`).
- **PersonalInformation ↔ Address:** Adresa bydliště uživatele.
- **RealEstate ↔ Address:** Adresa nemovitosti.
- **Meeting:** Vazební entita propojující `Client`, `Realtor` a `RealEstate`.

---

## 6. REST API a Kontrolery

API je navrženo jako RESTful a komunikuje pomocí formátu JSON. Všechny endpointy (kromě veřejných) vyžadují v hlavičce `Authorization: Bearer <token>`.

### 6.1 Hlavní Endpointy

#### **Autentizace (`AuthController`)**
- `POST /api/auth/login` – Přihlášení uživatele (vrací JWT token).
- `POST /api/auth/register` – Registrace nového klienta.
- `GET /api/auth/me` – Získání informací o aktuálně přihlášeném uživateli.
- `POST /api/auth/password-reset/request` – Žádost o reset hesla (odeslání e-mailu s kódem).
- `POST /api/auth/password-reset/confirm` – Nastavení nového hesla pomocí obdrženého kódu.

#### **Profil (`ProfileController`)**
- `GET /api/profile` – Získání detailu profilu přihlášeného uživatele.
- `PATCH /api/profile` – Aktualizace osobních údajů a adresy.
- `DELETE /api/profile` – Smazání vlastního účtu.

#### **Nemovitosti (`RealEstateController`)**
- `GET /api/real-estates` – Vyhledávání s filtrováním (cena, plocha, lokalita, typ).
- `GET /api/real-estates/{id}` – Detail nemovitosti.
- `POST /api/real-estates` – Vytvoření nemovitosti (pouze `ROLE_REALTOR`).
- `PUT /api/real-estates/{id}` – Editace nemovitosti (pouze `ROLE_REALTOR`).

#### **Obrázky (`ImageController`)**
- `POST /api/images` – Upload obrázku (Multipart File).
- `GET /api/images/{id}` – Stažení binárních dat obrázku (pro `<img>` tagy).
- `DELETE /api/images/{id}` – Smazání obrázku.

#### **Schůzky (`MeetingController`)**
- `POST /api/meetings` – Vytvoření žádosti o schůzku.
- `GET /api/meetings` – Seznam schůzek uživatele.
- `PUT /api/meetings/{id}` – Úprava stavu schůzky (potvrzení/zrušení).

#### **Recenze (`ReviewController`)**
- `POST /api/reviews` – Vytvoření recenze na makléře (pouze `ROLE_USER` - Client).
- `GET /api/reviews/realtor/{id}` – Seznam recenzí pro konkrétního makléře.
- `GET /api/reviews/stats/{id}` – Agregované statistiky makléře (průměrná hodnocení).

#### **Admin (`AdminController`)**
- `POST /api/admin/block/{username}` – Zablokování uživatele.
- `POST /api/admin/unblock/{username}` – Odblokování uživatele.
- `POST /api/admin/realtors` – Manuální vytvoření účtu makléře.

### 6.2 Validace a Chyby
Vstupy jsou validovány pomocí **Jakarta Validation** (`@Valid`, `@NotNull`, `@NotBlank`) v DTO objektech.
- Chyby validace vrací status `400 Bad Request` s mapou chybových polí.
- Nenalezené entity vrací `404 Not Found`.
- Neautorizovaný přístup vrací `401 Unauthorized` nebo `403 Forbidden`.

---

## 7. Security a Autentizace

Zabezpečení zajišťuje `SecurityConfig` a `JwtAuthenticationFilter`. Aplikace využívá bezstavovou (Stateless) architekturu.

### 7.1 Flow Autentizace
1. Klient pošle přihlašovací údaje na `/api/auth/login`.
2. `AuthService` ověří údaje a zkontroluje stav účtu (zda není `BLOCKED`).
3. Při úspěchu vygeneruje **JWT Access Token** (podepsaný pomocí `HMAC-SHA256`).
4. Klient posílá token v hlavičce `Authorization: Bearer <token>` při každém dalším requestu.
5. `JwtAuthenticationFilter` validuje token a nastaví `SecurityContext`.

### 7.2 Role a Oprávnění (`CustomUserDetailsService`)
Systém rolí je dynamický a odvozený od typu entity uživatele.
- **Všichni uživatelé** mají automaticky roli `ROLE_USER`.
- **Admin** (`Admin` entity) získává navíc `ROLE_ADMIN`.
- **Makléř** (`Realtor` entity) získává navíc `ROLE_REALTOR`.
- **Anonymní endpointy** (Swagger, Login) jsou explicitně povoleny v `SecurityConfig`.

### 7.3 Ochrana proti Brute-force
Systém počítá neúspěšné pokusy o přihlášení (`failedLoginAttempts`).
- Pokud počet pokusů překročí limit (nastaveno v `SecurityProperties`, defaultně 3), účet je automaticky zablokován (`blockedUntil`) na definovanou dobu (defaultně 24 hodin).
- Odblokování nastane automaticky po uplynutí doby, nebo manuálním zásahem admina.

### 7.4 Obnova Hesla
Aplikace implementuje bezpečný proces resetu hesla:
1. Uživatel požádá o reset zadáním e-mailu.
2. Vygeneruje se unikátní kód, který je **hashován** a uložen do databáze (`password_reset_code`).
3. Uživatel obdrží e-mail (via `MailService`) s nehashovaným kódem.
4. Endpoint pro potvrzení ověří shodu hashe a platnost časového razítka (`deadline`).

### 7.5 Inicializace (`AdminInitializer`)
Při startu aplikace se kontroluje existence administrátorského účtu. Pokud neexistuje, vytvoří se výchozí admin (credentials definovány v `application.properties`), což zajišťuje, že systém není nikdy bez správce.

---

## 8. Pokročilé Funkcionality

### 8.1 Dynamické Filtrování Nemovitostí
Třída `RealEstateSpecification` implementuje dynamické sestavování SQL dotazů pomocí **JPA Criteria API**. To umožňuje filtrovat podle libovolné kombinace parametrů:
- Cena (od-do)
- Plocha
- Typ nemovitosti (polymorfní dotaz)
- Vnořené atributy (Adresa -> Město)
- Kolekce (vybavení, inženýrské sítě - `isMember`)

### 8.2 Historie Cen
Při aktualizaci ceny nemovitosti (`RealEstateService.updateRealEstate`) systém automaticky detekuje změnu a vytvoří nový záznam v `PriceHistory` s časovým razítkem.

---

## 9. Vývojové Prostředí

### 9.1 Prerekvizity
- JDK 21
- Docker & Docker Compose (pro DB)
- Maven (volitelně, wrapper je součástí)

### 9.2 Spuštění Lokálně
1. **Databáze:** Spusťte PostgreSQL přes Docker:
   ```bash
   docker-compose up -d db
   ```
2. **Aplikace:** Spusťte Spring Boot:
   ```bash
   ./mvnw spring-boot:run
   ```
   Aplikace poběží na `http://localhost:8080`.

### 9.3 Konfigurace (`application.properties`)
Klíčové proměnné lze přepsat:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/remax_db
spring.datasource.username=remax_user
remax.security.jwt-secret=...
```

---

## 10. Docker a Deployment

Projekt je plně kontejnerizován pro zajištění konzistentního prostředí pro vývoj i produkci. Orchestrace je řízena pomocí **Docker Compose**, který spouští backend, frontend, databázi i mailový server.

### 10.1 Dockerfile (Backend)
Backend využívá **Multi-stage build** pro minimalizaci výsledné velikosti image:

1.  **Builder Stage (`maven:3.9.9-eclipse-temurin-21-alpine`)**:
    *   Kopíruje `pom.xml` a stahuje závislosti (využívá cache Docker vrstev).
    *   Kopíruje zdrojový kód (`src`) a kompiluje aplikaci (`mvn package -DskipTests`).
2.  **Runtime Stage (`eclipse-temurin:21-jre-alpine`)**:
    *   Vychází z lehkého Alpine Linux obrazu s JRE 21.
    *   Vytváří dedikovaného systémového uživatele `spring` (security best-practice, aplikace neběží pod rootem).
    *   Kopíruje zkompilovaný JAR soubor z první fáze.
    *   Exponuje port `8080`.

### 10.2 Struktura Docker Compose
Soubor `deployment/docker-compose.yml` definuje čtyři služby:

1.  **db (`postgres:17-alpine`)**:
    *   Hlavní databáze.
    *   Data jsou trvale uložena ve volume `postgres-data`.
    *   Obsahuje `healthcheck` (příkaz `pg_isready`), na který čeká backend před startem.
    *   Mapuje port `5432` na hosta pro přímý přístup (např. přes DBeaver).

2.  **backend**:
    *   Sestavuje se z `Dockerfile` umístěného v `deployment/`.
    *   Kontext sestavení je nastaven o úroveň výše (`context: ..`), aby měl Docker přístup k `pom.xml` a `src/`.
    *   Přepisuje konfiguraci pro připojení k DB (`SPRING_DATASOURCE_URL`) a MailHogu (`SPRING_MAIL_HOST`).

3.  **frontend**:
    *   Sestavuje se z adresáře `../../NNPRO-REMAX-FE` (očekává složku frontendu vedle složky backendu).
    *   Při sestavení předává argument `REACT_APP_API_URL` pro komunikaci s API.
    *   Běží na portu `3000`.

4.  **mailhog (`mailhog/mailhog:latest`)**:
    *   Fake SMTP server pro testování odesílání e-mailů (reset hesla).
    *   Zachytává e-maily, které backend odešle, a zobrazuje je ve webovém rozhraní.
    *   **SMTP Port:** `1025` (pro backend).
    *   **Web UI:** `8025` (pro vývojáře).

### 10.3 Příprava prostředí (Frontend skripty)
Protože `docker-compose.yml` odkazuje na relativní cestu k frontendu (`../../NNPRO-REMAX-FE`), je nutné zajistit, aby tato složka existovala. K tomu slouží pomocné skripty ve složce `deployment/`.

Skripty zkontrolují existenci složky a pokud chybí, automaticky naklonují repozitář frontendu.

**Postup spuštění:**

1.  Přejděte do složky deployment:
    ```bash
    cd deployment
    ```

2.  Spusťte inicializační skript dle vašeho OS:
    *   **Windows (PowerShell):**
        ```powershell
        .\init-frontend.ps1
        ```
    *   **Linux / macOS (Bash):**
        ```bash
        chmod +x init-frontend.sh
        ./init-frontend.sh
        ```

### 10.4 Spuštění celého stacku
Po inicializaci frontendu spusťte aplikaci příkazem (stále ve složce `deployment/`):

```bash
  docker-compose up --build
```

### 10.5 Dostupné služby
Po nastartování jsou služby dostupné na těchto adresách:

| Služba | URL / Port | Popis |
|--------|------------|-------|
| **Frontend** | [http://localhost:3000](http://localhost:3000) | Klientská aplikace (React) |
| **Backend API** | [http://localhost:8080/api](http://localhost:8080/api) | REST API |
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Dokumentace API |
| **MailHog** | [http://localhost:8025](http://localhost:8025) | Inbox pro testovací e-maily |
| **Databáze** | `localhost:5432` | PostgreSQL (user: `remax_user`, pass: `secret_password`) |
---

## 11. Testování

Projekt klade důraz na Unit a Integrační testování s využitím `spring-boot-starter-test`. Testy jsou konfigurovány tak, aby byly nezávislé na běžící instanci PostgreSQL.

### 11.1 Konfigurace Testů
Testy využívají separátní konfiguraci v `src/test/resources/application.properties`:
- **Databáze:** Používá se in-memory databáze **H2** (`jdbc:h2:mem:testdb`), která emuluje chování SQL databáze, ale je rychlejší a po skončení testů se smaže.
- **Liquibase:** Je pro testy vypnutý (`spring.liquibase.enabled=false`). Schéma se generuje automaticky pomocí Hibernate (`ddl-auto=create-drop`), což zrychluje start testů.

### 11.2 Unit Testy (Service Layer)
Testování byznys logiky probíhá v izolaci pomocí knihovny **Mockito**.
- Třídy jsou anotovány `@ExtendWith(MockitoExtension.class)`.
- Závislosti (Repository, Mappers) jsou mockovány (`@Mock`).
- Příklad: `RealEstateServiceTest`, `AddressServiceTest`.

### 11.3 Controller Testy (API Layer)
REST endpointy jsou testovány pomocí **MockMvc**.
- Většina testů používá `MockMvcBuilders.standaloneSetup()`, což umožňuje testovat kontroler izolovaně bez nastartování celého Spring kontextu.
- Ověřují se HTTP status kódy, JSON struktura odpovědi a mapování DTO.
- Příklad: `MeetingControllerTest`, `ImageControllerTest`.

### 11.4 Integrační Testy
Pro komplexní scénáře (např. Auth flow) se využívá `@SpringBootTest`, který nastartuje plný aplikační kontext s H2 databází.
- Příklad: `ProfileControllerTest` nebo `RemaxApplicationTests` (sanity check kontextu).
- **Mail:** Odesílání e-mailů je v testech mockováno pomocí `TestMailConfig`, aby se zabránilo pokusům o spojení s SMTP serverem.

---

## 12. Databázové Migrace

Správa databázového schématu je řešena nástrojem **Liquibase**. Veškeré změny struktury databáze jsou verzovány a aplikovány automaticky při startu aplikace.

### Struktura Changelogů
- **Master Changelog:** `src/main/resources/db/changelog/db.changelog-master.yaml`
    - Tento soubor slouží jako hlavní rozcestník a postupně načítá jednotlivé změnové sady (change sets).

- **Definice Změn (Changes):**
  Jednotlivé migrace jsou uloženy ve složce `src/main/resources/db/changelog/changes/`:
    1. **`001-initial-schema.yaml`**:
        - Vytvoření základního schématu (tabulky `address`, `remax_user`, `real_estate`, `meeting`, `review`, `image`).
        - Definice tabulek pro dědičnost (`apartment`, `house`, `land`).
        - Nastavení sekvencí, primárních klíčů a vazeb (Foreign Keys).
    2. **`002-password-reset.yaml`**:
        - Dodatečná migrace, která rozšiřuje tabulku `remax_user` o sloupce `password_reset_code` a `password_reset_code_deadline`.

### Proces Migrace
Při každém spuštění aplikace (lokálně i v Dockeru) Liquibase zkontroluje systémovou tabulku `DATABASECHANGELOG`. Pokud najde v YAML souborech nové change sety, které v databázi chybí, automaticky provede příslušné SQL příkazy (`CREATE TABLE`, `ALTER TABLE`, atd.), čímž udržuje schéma aktuální.