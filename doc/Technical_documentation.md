# Technická Příručka - Backend (NNPRO-REMAX-BE)

Tento dokument poskytuje kompletní technický přehled backendové části realitního portálu. Aplikace je postavena na **Java 21** a frameworku **Spring Boot**, využívá relační databázi **PostgreSQL** a komunikuje prostřednictvím **REST API**.

## Obsah
1. [Úvod a Technologie](#1-úvod-a-technologie)
2. [Architektura a Struktura Kódu](#2-architektura-a-struktura-kódu)
3. [Datová Vrstva a Migrace](#3-datová-vrstva-a-migrace)
4. [Zabezpečení (Security)](#4-zabezpečení-security)
5. [REST API a Kontrolery](#5-rest-api-a-kontrolery)
6. [Klíčové Funkcionality](#6-klíčové-funkcionality)
7. [Testování a Kvalita Kódu](#7-testování-a-kvalita-kódu)
8. [Instalace, Docker a Spuštění](#8-instalace-docker-a-spuštění)

> 📄 **Externí dokumentace:** Detailní diagramy a technické graphy se nacházejí v souboru [Architecture_and_Design.md](./Architecture_and_Design.md).

9. [Strukturální pohled (Structural View)](./Architecture_and_Design.md#9-strukturální-pohled-structural-view)
10. [Behaviorální pohled (Behavioral View)](./Architecture_and_Design.md#10-behaviorální-pohled-behavioral-view)
11. [Infrastruktura a Nasazení (Deployment)](./Architecture_and_Design.md#11-infrastruktura-a-nasazení-deployment)
---

## 1. Úvod a Technologie

### 1.1 Přehled Projektu
Backend slouží jako centrální bod pro správu dat a logiky realitního portálu. Zajišťuje perzistenci dat o nemovitostech, uživatelích, schůzkách a recenzích. Poskytuje zabezpečené API pro frontendovou aplikaci a spravuje složitější byznys logiku, jako je filtrování nemovitostí nebo správa oprávnění.

### 1.2 Technologický Stack
**Core & Frameworks**
- **Java 21** - Programovací jazyk
- **Spring Boot 3.5.6** - Aplikační framework (Web, Data JPA, Security, Validation)
- **Maven** - Build tool a dependency management
- **Lombok** - Redukce boilerplate kódu

**Data & Storage**
- **PostgreSQL 17** - Relační databáze
- **Spring Data JPA (Hibernate)** - ORM vrstva
- **Liquibase 5.0.1** - Verzování databázového schématu

**Security**
- **Spring Security** - Autentizace a autorizace
- **JWT (JSON Web Token)** - Stateless autentizace
- **BCrypt** - Hashování hesel

**Dokumentace**
- **SpringDoc OpenAPI (Swagger UI)** - Automatická dokumentace API

---

## 2. Architektura a Struktura Kódu

### 2.1 Architektonický Vzor
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

**Klíčové Principy:**
- **Dependency Injection**: Využití Spring IoC kontejneru (`@Service`, `@RestController`, `@RequiredArgsConstructor`).
- **DTO Pattern**: Oddělení interních entit od veřejného API (`Meeting` vs `MeetingDto`).
- **Exception Handling**: Centralizovaná správa chyb pomocí `@ControllerAdvice` (`RestApiExceptionHandler`).

### 2.2 Struktura Adresářů
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

## 3. Datová Vrstva a Migrace

### 3.1 Datové Modely (Entity)
Datová vrstva je postavena na **JPA (Hibernate)**. Entity využívají pokročilé mapování dědičnosti:

#### Nemovitosti (`RealEstate`)
Používá strategii **`InheritanceType.JOINED`**.
- **Tabulka `real_estate`:** Společná data (název, popis, adresa).
- **Podtabulky:** `apartment`, `house`, `land` (specifické atributy).
- **Cena:** Uložena odděleně v `price_history` (OneToMany) pro sledování vývoje v čase.

#### Uživatelé (`RemaxUser`)
Používá strategii **`InheritanceType.SINGLE_TABLE`**.
- **Tabulka `remax_user`:** Všechna data v jedné tabulce pro rychlé přihlašování.
- **Diskriminátor:** Sloupec `user_type` (ADMIN, REALTOR, CLIENT).

#### Obrázky (`Image`)
- Binární data (`byte[]`) uložena v DB jako `OID` / `@Lob`.
- **Vazby:**
    - `RealEstate` má vazbu `OneToMany` na `Image` (galerie nemovitosti).
    - `PersonalInformation` má vazbu `OneToOne` na `Image` (profilová fotka).

#### Klíčové Vztahy (ERD)
- **RealEstate ↔ PriceHistory:** Historie vývoje ceny v čase (`OneToMany`).
- **RemaxUser ↔ PersonalInformation:** Oddělení přihlašovacích údajů od osobních dat (`OneToOne`).
- **PersonalInformation ↔ Address:** Adresa bydliště uživatele.
- **RealEstate ↔ Address:** Adresa nemovitosti.
- **Meeting:** Vazební entita propojující `Client`, `Realtor` a `RealEstate`.

### 3.2 Databázové Migrace (Liquibase)
Správa schématu je plně automatizovaná. Změny jsou definovány v YAML souborech v `src/main/resources/db/changelog/`.

- **Master Changelog:** `db.changelog-master.yaml`
- **Change Sets:**
    1. **`001-initial-schema.yaml`**:
        - Vytvoření základního schématu (tabulky `address`, `remax_user`, `real_estate`, `meeting`, `review`, `image`).
        - Definice tabulek pro dědičnost (`apartment`, `house`, `land`).
        - Nastavení sekvencí, primárních klíčů a vazeb (Foreign Keys).
    2. **`002-password-reset.yaml`**:
        - Dodatečná migrace, která rozšiřuje tabulku `remax_user` o sloupce `password_reset_code` a `password_reset_code_deadline`.

Při startu aplikace Liquibase automaticky porovná definice s aktuálním stavem databáze a provede chybějící SQL příkazy.

---

## 4. Zabezpečení (Security)

Zabezpečení zajišťuje `SecurityConfig` a `JwtAuthenticationFilter`. Aplikace využívá bezstavovou (Stateless) architekturu.

### 4.1 Autentizační Flow
1. Klient pošle credentials na `/api/auth/login`.
2. `AuthService` ověří údaje a stav účtu (blokace).
3. Server vrátí **JWT Access Token**.
4. Klient posílá token v hlavičce `Authorization: Bearer <token>` u každého requestu.

### 4.2 Role a Oprávnění
Systém rolí je odvozen od typu entity uživatele (`CustomUserDetailsService`):
- **ROLE_USER**: Základní role pro všechny přihlášené.
- **ROLE_ADMIN**: Pro entity typu `Admin`.
- **ROLE_REALTOR**: Pro entity typu `Realtor` (správa nemovitostí).

### 4.3 Ochrana účtu
- **Brute-force protection:** Po 3 neúspěšných pokusech se účet na 24 hodin zablokuje.
- **Reset hesla:** Bezpečný proces pomocí e-mailového kódu (hashovaného v DB).

### 4.4 Inicializace (`AdminInitializer`)
- Při startu aplikace se kontroluje existence administrátorského účtu. Pokud neexistuje, vytvoří se výchozí admin (credentials definovány v `application.properties`), což zajišťuje, že systém není nikdy bez správce.

---

## 5. REST API a Kontrolery

API je navrženo jako RESTful a komunikuje ve formátu JSON. Většina endpointů vyžaduje autentizaci.

### 5.1 Přehled hlavních modulů
| Modul | Base Path | Popis |
|-------|-----------|-------|
| **Auth** | `/api/auth` | Login, Register, Reset hesla |
| **Profile** | `/api/profile` | Správa vlastního profilu, změna údajů |
| **RealEstate**| `/api/real-estates`| CRUD nemovitostí, filtrování, detail |
| **Meeting** | `/api/meetings` | Žádosti o prohlídky, schvalování |
| **Review** | `/api/reviews` | Hodnocení makléřů |
| **Image** | `/api/images` | Upload a stahování obrázků |
| **Admin** | `/api/admin` | Blokování uživatelů, správa rolí |

### 5.2 Validace a Chyby
- Vstupy validovány pomocí **Jakarta Validation** (`@Valid`, `@NotNull`).
- **HTTP Status kódy:** `200 OK`, `201 Created`, `400 Bad Request` (validace), `401/403` (auth), `404 Not Found`.

---

## 6. Klíčové Funkcionality

### 6.1 Dynamické Filtrování
Třída `RealEstateSpecification` implementuje dynamické sestavování SQL dotazů pomocí **JPA Criteria API**. To umožňuje filtrovat podle libovolné kombinace parametrů:
- Cena (od-do)
- Plocha
- Typ nemovitosti (polymorfní dotaz)
- Vnořené atributy (Adresa -> Město)
- Kolekce (vybavení, inženýrské sítě - `isMember`)

### 6.2 Historie Cen
Metoda `updateRealEstate` automaticky detekuje změnu ceny. Stará cena zůstává v historii, nová se zapíše do tabulky `price_history` s aktuálním timestampem.

### 6.3 Inicializace Admina
Komponenta `AdminInitializer` při startu aplikace kontroluje existenci admina. Pokud chybí, vytvoří defaultního super-uživatele (credentials v `application.properties`).

---

## 7. Testování a Kvalita Kódu

Projekt využívá `spring-boot-starter-test` pro Unit a Integrační testy.

### 7.1 Konfigurace Testů
- **Databáze:** In-memory **H2 Database** (`jdbc:h2:mem:testdb`). Rychlá, izolovaná, po testech se smaže.
- **Liquibase:** V testech vypnuto, schéma generuje Hibernate (`ddl-auto=create-drop`).

### 7.2 Typy testů
- **Unit Testy (Service):** Izolované testy byznys logiky s využitím **Mockito**.
- **Controller Testy (API):** Testy endpointů pomocí **MockMvc** (ověření status kódů, JSON struktury).
- **Integrační Testy:** `@SpringBootTest` pro ověření celého kontextu (např. Auth flow).
- **Mail Mocking:** `TestMailConfig` zabraňuje odesílání skutečných e-mailů během testů.

### 7.3 End-to-End (E2E) Testování
Pro ověření funkčnosti celého systému (Frontend + Backend + Databáze) se využívají E2E testy spouštěné v Dockeru. Tyto testy simulují reálné chování uživatele v prohlížeči.

Testy jsou definovány ve frontendové části projektu, ale spouští se přes orchestraci v backend repozitáři pomocí Docker Compose.

**Postup spuštění:**
1. Přejděte do složky `deployment` v kořenovém adresáři.
2. Spusťte následující příkaz:

```bash
docker-compose -f docker-compose.yml -f docker-compose.e2e.yml up --build --exit-code-from e2e
```

**Vysvětlení příkazu:**
- `-f docker-compose.yml -f docker-compose.e2e.yml`: Sloučí standardní konfiguraci s konfigurací pro E2E testy.
- `up --build`: Sestaví a spustí kontejnery (Backend, Frontend, DB, MailHog a E2E runner).
- `--exit-code-from e2e`: Klíčový parametr pro CI/CD. Docker Compose se ukončí ve chvíli, kdy doběhne kontejner `e2e`, a vrátí jeho návratový kód (0 = úspěch, 1 = chyba).

**Výstupy:**
Reporty z testování (včetně screenshotů a videí z chyb) se ukládají do složky `../../NNPRO-REMAX-FE/e2e/playwright-report` (relativně k `docker-compose` souboru).

---

## 8. Instalace, Docker a Spuštění

Projekt je připraven pro lokální vývoj i kontejnerizované nasazení.

### 8.1 Prerekvizity (Lokální vývoj)
- JDK 21
- Docker (pro databázi)
- Maven

Pro lokální spuštění bez Dockeru:
1. Spusťte DB: `docker-compose up -d db`
2. Spusťte aplikaci: `./mvnw spring-boot:run`
3. Aplikace běží na: `http://localhost:8080`

### 8.2 Docker Deployment (Full Stack)
Orchestrace celého řešení je řízena přes **Docker Compose** ve složce `deployment/`.

**Služby v stacku:**
1.  **Backend:** Java 21 image (Multi-stage build: Maven build -> JRE Alpine runtime).
2.  **Frontend:** React aplikace (sestavena ze sousedního adresáře `../../NNPRO-REMAX-FE`).
3.  **Databáze:** PostgreSQL 17 (data uložena ve volume).
4.  **MailHog:** Fake SMTP server pro testování e-mailů.

#### 8.2.1. Dockerfile (Backend)
Backend využívá **Multi-stage build** pro minimalizaci výsledné velikosti image:
1.  **Builder Stage (`maven:3.9.9-eclipse-temurin-21-alpine`)**:
    *   Kopíruje `pom.xml` a stahuje závislosti (využívá cache Docker vrstev).
    *   Kopíruje zdrojový kód (`src`) a kompiluje aplikaci (`mvn package -DskipTests`).
2.  **Runtime Stage (`eclipse-temurin:21-jre-alpine`)**:
    *   Vychází z lehkého Alpine Linux obrazu s JRE 21.
    *   Vytváří dedikovaného systémového uživatele `spring` (security best-practice, aplikace neběží pod rootem).
    *   Kopíruje zkompilovaný JAR soubor z první fáze.
    *   Exponuje port `8080`.

#### 8.2.2. Struktura Docker Compose
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

### 8.3 Příprava prostředí a Spuštění
Protože `docker-compose.yml` odkazuje na relativní cestu k frontendu (`../../NNPRO-REMAX-FE`), je nutné zajistit, aby tato složka existovala. K tomu slouží pomocné skripty ve složce `deployment/`.

Skripty zkontrolují existenci složky a pokud chybí, automaticky naklonují repozitář frontendu.

**Postup spuštění:**
1.  Přejděte do složky: `cd deployment`
2.  Inicializujte frontend (stáhne repo, pokud chybí):
    - Windows: `.\init-frontend.ps1`
    - Linux/Mac: `./init-frontend.sh`
3.  Spusťte stack: `docker-compose up --build`

### 8.4 Dostupné URL
| Služba | URL / Port | Popis |
|--------|------------|-------|
| **Frontend** | [http://localhost:3000](http://localhost:3000) | Klientská aplikace (React) |
| **Backend API** | [http://localhost:8080/api](http://localhost:8080/api) | REST API |
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Dokumentace API |
| **MailHog** | [http://localhost:8025](http://localhost:8025) | Inbox pro testovací e-maily |
| **Databáze** | `localhost:5432` | PostgreSQL (user: `remax_user`, pass: `secret_password`) |