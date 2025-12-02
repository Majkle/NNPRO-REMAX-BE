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
src/main/java/cz/upce/nnprop/remax/
├── address/            # Správa adres
├── images/             # Upload a serving obrázků (BLOB)
├── meetings/           # Logika schůzek (Meeting)
├── model/              # JPA Entity (sdílené modely)
│   ├── image/
│   ├── meeting/
│   ├── realestates/    # Dědičnost RealEstate (Apartment, House, Land)
│   ├── review/
│   └── users/          # Dědičnost RemaxUser (Admin, Realtor, Client)
├── personalInformation/# Osobní údaje uživatelů
├── profile/            # Správa profilu přihlášeného uživatele
├── realestates/        # Logika nemovitostí (Service, Controller, Spec)
├── review/             # Logika recenzí
├── security/           # Konfigurace bezpečnosti
│   ├── admin/          # Admin-only operace (blokování uživatelů)
│   ├── auth/           # Login, Register, UserDetails
│   ├── config/         # SecurityConfig, Cors
│   └── jwt/            # Generování a validace tokenů
└── RemaxApplication.java  # Main class
```

---

## 5. Datové Modely a Databáze

### 5.1 Polymorfismus Entit
Aplikace intenzivně využívá dědičnost v JPA.

#### Nemovitosti (`RealEstate`)
Používá strategii **`InheritanceType.JOINED`**. Společná data jsou v tabulce `real_estate`, specifická data v podtabulkách.
- **Parent:** `RealEstate` (cena, popis, adresa, plocha...)
- **Children:**
    - `Apartment` (patro, výtah, vlastnictví)
    - `House` (plocha pozemku, typ domu)
    - `Land` (určeno k bydlení?)

#### Uživatelé (`RemaxUser`)
Používá strategii **`InheritanceType.SINGLE_TABLE`**. Všechna data jsou v jedné tabulce `remax_user` s diskriminátorem `user_type`.
- **Parent:** `RemaxUser` (login, heslo, role)
- **Children:** `Admin`, `Realtor` (licence, info), `Client`

### 5.2 Obrázky
Obrázky nejsou ukládány na filesystém, ale **přímo do databáze** jako `byte[]` (typ `OID` v Postgres/`@Lob` v JPA) v entitě `Image`.
- *Poznámka:* Pro produkční škálování by bylo vhodné zvážit S3/Blob storage, ale pro tento projekt je DB řešení dostačující.

### 5.3 Vztahy
- **OneToOne:** User ↔ PersonalInformation ↔ Address
- **ManyToOne:** Meeting → Realtor/Client/RealEstate
- **OneToMany:** RealEstate → PriceHistory (historie vývoje cen)

---

## 6. REST API a Kontrolery

API je navrženo jako RESTful a komunikuje pomocí JSON.

### 6.1 Hlavní Endpointy
- **Auth:** `POST /api/auth/login`, `POST /api/auth/register`
- **RealEstates:**
    - `GET /api/real-estates` (s filtrováním)
    - `POST /api/real-estates` (vytvoření)
    - `PUT /api/real-estates/{id}` (editace)
- **Images:** `POST /api/images` (multipart upload), `GET /api/images/{id}` (stažení binárních dat)
- **Meetings:** CRUD operace pro schůzky.
- **Reviews:** Hodnocení makléřů.
- **Admin:** `POST /api/admin/block/{username}`, správa uživatelů.

### 6.2 Validace
Vstupy jsou validovány pomocí **Jakarta Validation** (`@Valid`, `@NotNull`, `@NotBlank`) přímo v DTO objektech. Chyby jsou zachytávány v `RestApiExceptionHandler` a vraceny jako `400 Bad Request`.

---

## 7. Security a Autentizace

Zabezpečení zajišťuje `SecurityConfig` a `JwtAuthenticationFilter`.

### 7.1 Flow Autentizace
1. Klient pošle credentials na `/api/auth/login`.
2. `AuthService` ověří údaje a zda není účet zablokován (`AccountStatus.BLOCKED`).
3. Při úspěchu vygeneruje **JWT Access Token** (platnost 1h defaultně).
4. Klient posílá token v hlavičce `Authorization: Bearer <token>`.
5. `JwtAuthenticationFilter` validuje token a nastaví `SecurityContext`.

### 7.2 Role a Oprávnění
Role jsou odvozeny od typu entity (`Admin` -> `ROLE_ADMIN`, `Realtor` -> `ROLE_REALTOR`, atd.).
- Endpointy jsou chráněny anotacemi `@PreAuthorize("hasRole('ADMIN')")`.
- CORS je povolen pro frontend (defaultně `http://localhost:3000`).

### 7.3 Ochrana proti Brute-force
Systém počítá neúspěšné pokusy o přihlášení. Po překročení limitu (default 3) dojde k dočasnému zablokování účtu (`blocked_until`).

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

Projekt obsahuje plnou Docker podporu pro snadné nasazení backendu i frontendu v jednom kroku.

### 10.1 Dockerfile
Backend využívá **Multi-stage build** pro optimalizaci velikosti image:
1. **Builder stage:** Obraz `maven:3.9.9-eclipse-temurin-21-alpine` zkompiluje aplikaci a vytvoří spustitelný JAR.
2. **Runtime stage:** Lehký obraz `eclipse-temurin:21-jre-alpine` spustí aplikaci.

### 10.2 Docker Compose
Soubor `deployment/docker-compose.yml` orchestruje celou architekturu:
- **db:** PostgreSQL 17 (data perzistována ve volume `postgres-data`).
- **backend:** Spring Boot aplikace (čeká na healthcheck databáze).
- **frontend:** React aplikace (sestavena z vedlejšího repozitáře).

### 10.3 Inicializační skripty pro Frontend
Protože `docker-compose.yml` očekává zdrojový kód frontendu ve složce sousedící s backendem (relativní cesta `../../NNPRO-REMAX-FE`), jsou připraveny automatizační skripty. Tyto skripty zkontrolují přítomnost složky a případně naklonují frontendový repozitář.

Před spuštěním `docker-compose up` je doporučeno spustit příslušný skript pro váš operační systém:

**Windows (PowerShell):**
```powershell
cd deployment
.\init-frontend.ps1
```

**Linux / macOS (Bash):**
```bash
cd deployment
chmod +x init-frontend.sh
./init-frontend.sh
```

Skript provede:
1. Kontrolu existence složky `../NNPRO-REMAX-FE`.
2. Pokud neexistuje, provede `git clone https://github.com/Majkle/NNPRO-REMAX-FE.git`.
3. Pokud existuje, přeskočí stahování (nepřepisuje existující práci).

### 10.4 Spuštění celého stacku
Po inicializaci frontendu (viz bod 10.3) lze spustit celý systém příkazem:

```bash
cd deployment
docker-compose up --build
```

Aplikace budou dostupné na:
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080
- **Databáze:** port 5432

---

## 11. Testování

Projekt využívá `spring-boot-starter-test`.

### 11.1 Unit Testy
Testování jednotlivých servis (např. `AddressServiceTest`, `MeetingServiceTest`) pomocí **Mockito**.
- Mockování Repository vrstvy.
- Ověření logiky bez nutnosti DB.

### 11.2 Integrační Testy
Ačkoliv v ukázce převažují Unit testy, infrastruktura je připravena i na integrační testy (`@SpringBootTest`), které zvednou celý Spring kontext.

### 11.3 Spuštění Testů
```bash
./mvnw test
```

---

## 12. Databázové Migrace

Správa databázového schématu je řešena nástrojem **Liquibase**.

- **Master Changelog:** `src/main/resources/db/changelog/db.changelog-master.yaml`
- **ChangeSets:** Definice změn jsou v YAML souborech.
- Při startu aplikace Liquibase automaticky porovná aktuální stav DB s changelogy a provede potřebné `CREATE TABLE` nebo `ALTER TABLE` příkazy.
- Inicializační skript `001-initial-schema.yaml` obsahuje kompletní definici tabulek, sekvencí a cizích klíčů.