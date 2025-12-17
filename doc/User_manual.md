# Provozní Příručka - Backend Realitního Portálu

Tento dokument slouží jako manuál pro nasazení, konfiguraci a správu backendové části realitního portálu (NNPRO-REMAX-BE).
Je koncipována pro DevOps inženýry, administrátory serverů nebo vývojáře, kteří potřebují aplikaci nasadit a spravovat.

Backend je REST API aplikace postavená na **Java 21** a **Spring Boot 3.5.6**, využívající databázi **PostgreSQL 17**.

## Obsah
1. [Požadavky na Systém](#1-požadavky-na-systém)
2. [Rychlý Start (Docker)](#2-rychlý-start-docker)
3. [Manuální Instalace (Bez Dockeru)](#3-manuální-instalace-bez-dockeru)
4. [Konfigurace](#4-konfigurace)
5. [Správa Databáze a Migrace](#5-správa-databáze-a-migrace)
6. [První Přihlášení a Administrace](#6-první-přihlášení-a-administrace)
7. [API Dokumentace (Swagger)](#7-api-dokumentace-swagger)
8. [Řešení Problémů](#8-řešení-problémů)

---

## 1. Požadavky na Systém

Pro běh aplikace jsou vyžadovány následující technologie:

- **Java Runtime Environment (JRE):** Verze 21 (doporučeno Eclipse Temurin).
- **Databáze:** PostgreSQL verze 17.
- **Docker & Docker Compose:** (Doporučeno pro snadné nasazení celého stacku).
- **RAM:** Minimálně 512MB pro backend kontejner.
- **Disk:** Alespoň 1GB pro databázová data a logy.

---

## 2. Rychlý Start (Docker)

Nejjednodušší způsob nasazení je pomocí připraveného Docker Compose stacku, který automaticky nastaví databázi, backend aplikaci a lokální SMTP server pro testování.

### 2.1 Příprava prostředí
Aplikace obsahuje pomocné skripty pro přípravu prostředí (zejména pokud chcete spustit stack společně s frontendem).

1. Přejděte do složky `deployment` v kořenovém adresáři projektu:
   ```bash
   cd deployment
   ```

2. (Volitelné) Pokud chcete spustit stack včetně frontendu, spusťte inicializační skript, který stáhne potřebný repozitář:
    - **Linux/macOS:** `chmod +x init-frontend.sh && ./init-frontend.sh`
    - **Windows (PowerShell):** `.\init-frontend.ps1`

### 2.2 Spuštění
Spusťte kontejnery v režimu na pozadí:

```bash
docker-compose up -d --build
```

### 2.3 Ověření běhu
Po nastartování (cca 30-60 sekund) budou dostupné následující služby:

- **Backend API:** `http://localhost:8080/api`
- **Swagger UI (Dokumentace):** `http://localhost:8080/swagger-ui.html`
- **MailHog (Testovací e-maily):** `http://localhost:8025` (Zde uvidíte e-maily odeslané aplikací, např. reset hesla)
- **Frontend (pokud byl spuštěn):** `http://localhost:3000`

---

## 3. Manuální Instalace (Bez Dockeru)

Pokud preferujete běh přímo na serveru (bare metal) bez kontejnerizace:

### 3.1 Nastavení Databáze
1. Nainstalujte PostgreSQL 17.
2. Vytvořte databázi a uživatele (hodnoty by měly odpovídat konfiguraci v `application.properties`):
   ```sql
   CREATE DATABASE remax_db;
   CREATE USER remax_user WITH PASSWORD 'secret_password';
   GRANT ALL PRIVILEGES ON DATABASE remax_db TO remax_user;
   GRANT ALL ON SCHEMA public TO remax_user;
   ```

### 3.2 Sestavení Aplikace
V kořenovém adresáři projektu (kde je `pom.xml`) spusťte Maven wrapper:

```bash
# Linux / macOS
./mvnw clean package -DskipTests

# Windows
.\mvnw.cmd clean package -DskipTests
```
Výsledný soubor `.jar` se vytvoří ve složce `target/` (např. `target/remax-0.0.1-SNAPSHOT.jar`).

### 3.3 Spuštění
Nastavte proměnné prostředí (volitelné, jinak se použijí výchozí) a spusťte aplikaci:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/remax_db
export SPRING_DATASOURCE_USERNAME=remax_user
export SPRING_DATASOURCE_PASSWORD=secret_password

java -jar target/remax-0.0.1-SNAPSHOT.jar
```

---

## 4. Konfigurace

Aplikaci lze konfigurovat pomocí souboru `src/main/resources/application.properties` nebo přepsáním pomocí **Environment Variables** (doporučeno pro produkci).

### 4.1 Klíčové Proměnné

| Proměnná Prostředí | Property (application.properties) | Popis | Výchozí Hodnota |
|-------------------|-----------------------------------|-------|-----------------|
| `SPRING_DATASOURCE_URL` | `spring.datasource.url` | JDBC URL databáze | `jdbc:postgresql://localhost:5432/remax_db` |
| `SPRING_DATASOURCE_USERNAME` | `spring.datasource.username` | Uživatel DB | `remax_user` |
| `SPRING_DATASOURCE_PASSWORD` | `spring.datasource.password` | Heslo DB | `secret_password` |
| `REMAX_SECURITY_JWT_SECRET` | `remax.security.jwt-secret` | **KRITICKÉ:** Klíč pro podpis tokenů (min 64 znaků) | *(hardcoded dev secret)* |
| `REMAX_SECURITY_JWT_EXPIRATION_MS` | `remax.security.jwt-expiration-ms` | Platnost tokenu v ms | `3600000` (1 hodina) |
| `REMAX_SECURITY_CORS_ALLOWED_ORIGINS` | `remax.security.cors-allowed-origins` | Povolené frontend URL (CORS) | `http://localhost:3000` |
| `SPRING_MAIL_HOST` | `spring.mail.host` | SMTP Server | `localhost` (nebo `mailhog` v Dockeru) |
| `SPRING_MAIL_PORT` | `spring.mail.port` | SMTP Port | `1025` |

### 4.2 Produkční Nastavení (Důležité)
Pro nasazení mimo lokální vývoj **musíte** změnit následující:

1. **JWT Secret:** Vygenerujte dlouhý náhodný řetězec a nastavte ho do `REMAX_SECURITY_JWT_SECRET`. Pokud zůstane výchozí, aplikace je zranitelná.
2. **CORS:** Nastavte `REMAX_SECURITY_CORS_ALLOWED_ORIGINS` na přesnou doménu frontendu (např. `https://moje-realitka.cz`).
3. **Databáze:** Ujistěte se, že `SPRING_JPA_HIBERNATE_DDL_AUTO` je nastaveno na `validate` (výchozí pro Postgres v produkčním profilu), aby Hibernate nezasahoval do schématu spravovaného Liquibase.

---

## 5. Správa Databáze a Migrace

### 5.1 Verzování (Liquibase)
Backend používá nástroj **Liquibase** pro automatickou správu databázového schématu.
- Definice změn jsou v `src/main/resources/db/changelog`.
- **Není potřeba ručně spouštět SQL skripty.** Aplikace si při startu sama zaktualizuje tabulky.

### 5.2 Ukládání Obrázků
Obrázky nemovitostí a profilové fotky se ukládají **přímo do databáze** (tabulka `image`, sloupec `data` typu `OID`/`byte[]`).
- *Upozornění pro provoz:* Sledujte velikost databáze. Pro velké produkční nasazení může být nutné v budoucnu refaktorovat na S3 storage.

---

## 6. První Přihlášení a Administrace

Aplikace obsahuje `AdminInitializer`, který při startu automaticky vytvoří výchozího administrátora, pokud v databázi žádný neexistuje.

### 6.1 Výchozí Účty
#### 6.1.1. Admin Účet
- **Email:** `admin@remax.cz`
- **Username:** `admin` (lze změnit přes `remax.default-admin.username`)
- **Heslo:** `changeme1234` (lze změnit přes `remax.default-admin.password`)
**Bezpečnostní varování:** Po prvním nasazení se ihned přihlaste a změňte heslo tomuto účtu, nebo vytvořte nového admina a tohoto smažte.

#### 6.1.2. Realtor Účet
- **Email:** `realtor@remax.cz`
- **Username:** `realtor`
- **Heslo:** `password`

#### 6.1.2. Client Účet
- **Email:** `client@remax.cz`
- **Username:** `client`
- **Heslo:** `password`
- 
### 6.2 Blokování Uživatelů
Admin má právo zablokovat uživatele (např. při podezřelém chování) přes API:
- **Endpoint:** `POST /api/admin/block/{username}`
- Systém také **automaticky blokuje účty** po překročení limitu neúspěšných pokusů o přihlášení (výchozí: 3 pokusy, blokace na 24 hodin). Tyto parametry lze upravit v `application.properties`.

---

## 7. API Dokumentace (Swagger)

Backend generuje interaktivní dokumentaci podle standardu OpenAPI v3.

Po spuštění aplikace je dokumentace dostupná na:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

Tato dokumentace umožňuje zkoušet endpointy přímo z prohlížeče (tlačítko "Try it out"). Pro autentizované endpointy je nutné se nejprve přihlásit (`/api/auth/login`), získat token a ten vložit do tlačítka **Authorize** ve formátu `Bearer <token>`.

---

## 8. Řešení Problémů

### 8.1 Aplikace nenastartuje (Connection refused)
- **Příčina:** Backend se nemůže připojit k databázi.
- **Řešení:**
    1. Ověřte, že kontejner `db` běží (`docker ps`).
    2. Zkontrolujte logy backendu: `docker logs remax-backend`.
    3. Zkontrolujte, zda URL databáze v `docker-compose.yml` (`jdbc:postgresql://db:5432/...`) odpovídá názvu služby.

### 8.2 Odesílání e-mailů selhává
- **Příčina:** SMTP server není dostupný.
- **Řešení:**
    - V Dockeru: Zkontrolujte, zda běží kontejner `mailhog`.
    - Manuálně: Ujistěte se, že na `localhost:1025` něco naslouchá, nebo upravte `spring.mail.host` na reálný SMTP server.

### 8.3 Upload obrázku selže (413 Payload Too Large)
- **Příčina:** Obrázek překračuje maximální povolenou velikost (výchozí 1MB ve Spring Boot, ale upraveno na 10MB).
- **Řešení:** Zkontrolujte nastavení `spring.servlet.multipart.max-file-size` v `application.properties`.

---