Zde je **Provozní Příručka (User/Operator Manual)** pro backendovou část projektu. Je koncipována pro DevOps inženýry, administrátory serverů nebo vývojáře, kteří potřebují aplikaci nasadit a spravovat.

---

# Provozní Příručka - Backend Realitního Portálu

Tento dokument slouží jako manuál pro nasazení, konfiguraci a správu backendové části realitního portálu (NNPRO-REMAX-BE).

Backend je REST API aplikace postavená na **Java Spring Boot**, využívající databázi **PostgreSQL**.

## Obsah
1. [Požadavky na Systém](#1-požadavky-na-systém)
2. [Rychlý Start (Docker)](#2-rychlý-start-docker)
3. [Manuální Instalace (Bez Dockeru)](#3-manuální-instalace-bez-dockeru)
4. [Konfigurace](#4-konfigurace)
5. [Správa Databáze](#5-správa-databáze)
6. [První Přihlášení a Správa](#6-první-přihlášení-a-správa)
7. [API Dokumentace (Swagger)](#7-api-dokumentace-swagger)
8. [Řešení Problémů](#8-řešení-problémů)

---

## 1. Požadavky na Systém

Pro běh aplikace jsou vyžadovány následující technologie:

- **Java Runtime Environment (JRE):** Verze 21 (doporučeno Eclipse Temurin).
- **Databáze:** PostgreSQL verze 17.
- **Docker & Docker Compose:** (Volitelné, ale doporučené pro snadné nasazení).
- **RAM:** Minimálně 512MB pro backend kontejner.
- **Disk:** Alespoň 1GB pro databázová data a logy.

---

## 2. Rychlý Start (Docker)

Nejjednodušší způsob nasazení je pomocí připraveného Docker Compose stacku, který automaticky nastavit databázi i aplikaci.

### 2.1 Příprava prostředí
Aplikace obsahuje pomocné skripty pro přípravu prostředí (zejména pokud chcete spustit i frontend).

1. Přejděte do složky `deployment`:
   ```bash
   cd deployment
   ```

2. (Volitelné) Pokud chcete spustit stack včetně frontendu, spusťte inicializační skript, který stáhne potřebné repozitáře:
    - **Linux/macOS:** `chmod +x init-frontend.sh && ./init-frontend.sh`
    - **Windows:** `.\init-frontend.ps1`

### 2.2 Spuštění
Spusťte kontejnery v režimu na pozadí:

```bash
docker-compose up -d --build
```

### 2.3 Ověření běhu
- **Backend API:** `http://localhost:8080/api`
- **Zdravotní stav DB:** Kontejner `remax-db` by měl být ve stavu `healthy`.

### 2.4 Zastavení
```bash
docker-compose down
```
*Poznámka: Data v databázi zůstanou zachována ve volume `postgres-data`.*

---

## 3. Manuální Instalace (Bez Dockeru)

Pokud preferujete běh přímo na serveru bez kontejnerizace:

### 3.1 Nastavení Databáze
1. Nainstalujte PostgreSQL 17.
2. Vytvořte databázi a uživatele:
   ```sql
   CREATE DATABASE remax_db;
   CREATE USER remax_user WITH PASSWORD 'secret_password';
   GRANT ALL PRIVILEGES ON DATABASE remax_db TO remax_user;
   -- Ujistěte se, že uživatel má práva na public schema
   GRANT ALL ON SCHEMA public TO remax_user;
   ```

### 3.2 Sestavení Aplikace
V kořenovém adresáři projektu spusťte Maven wrapper:

```bash
# Linux / macOS
./mvnw clean package -DskipTests

# Windows
.\mvnw.cmd clean package -DskipTests
```
Výsledný soubor `.jar` se vytvoří ve složce `target/`.

### 3.3 Spuštění
```bash
java -jar target/remax-0.0.1-SNAPSHOT.jar
```
Aplikace se pokusí připojit k databázi na `localhost:5432`. Pokud máte DB jinde, upravte konfiguraci (viz sekce 4).

---

## 4. Konfigurace

Aplikaci lze konfigurovat pomocí souboru `application.properties` nebo **environment variables** (doporučeno pro produkci).

### 4.1 Klíčové Proměnné

| Proměnná Prostředí | Property (application.properties) | Popis | Výchozí Hodnota |
|-------------------|-----------------------------------|-------|-----------------|
| `SPRING_DATASOURCE_URL` | `spring.datasource.url` | JDBC URL databáze | `jdbc:postgresql://localhost:5432/remax_db` |
| `SPRING_DATASOURCE_USERNAME` | `spring.datasource.username` | Uživatel DB | `remax_user` |
| `SPRING_DATASOURCE_PASSWORD` | `spring.datasource.password` | Heslo DB | `secret_password` |
| `REMAX_SECURITY_JWT_SECRET` | `remax.security.jwt-secret` | Tajný klíč pro podpis tokenů (min 64 znaků) | *(hardcoded dev secret)* |
| `REMAX_SECURITY_JWT_EXPIRATION_MS` | `remax.security.jwt-expiration-ms` | Platnost tokenu v ms | `3600000` (1 hodina) |
| `REMAX_SECURITY_CORS_ALLOWED_ORIGINS` | `remax.security.cors-allowed-origins` | Povolené frontend URL (CORS) | `http://localhost:3000` |

### 4.2 Produkční Nastavení (Důležité)
Pro produkční nasazení **musíte** změnit následující:

1. **JWT Secret:** Vygenerujte dlouhý náhodný řetězec. Pokud se tento klíč prozradí, útočník může falšovat identitu uživatelů.
2. **DDL Auto:** V produkci nastavte `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` (místo `update`), aby Hibernate nezasahoval do schématu, které spravuje Liquibase.
3. **CORS:** Nastavte přesnou doménu, na které běží frontend (např. `https://moje-realitka.cz`), místo `localhost`.

---

## 5. Správa Databáze

### 5.1 Verzování (Liquibase)
Aplikace používá **Liquibase** pro automatickou správu struktury databáze.
- Při každém startu aplikace se zkontroluje soubor `db.changelog-master.yaml`.
- Pokud existují nové změny (ChangeSets), automaticky se aplikují.
- **Není potřeba ručně spouštět SQL skripty.**

### 5.2 Obrázky
Obrázky nemovitostí a uživatelů se ukládají **přímo do databáze** (tabulka `image`, sloupec `data` typu OID/LOB).
- *Upozornění:* Pravidelně monitorujte velikost databáze. Pro velké objemy dat doporučujeme v budoucnu refactoring na S3 storage.

---

## 6. První Přihlášení a Správa

Aplikace obsahuje mechanismus, který při startu vytvoří výchozího administrátora, pokud v databázi žádný neexistuje.

### 6.1 Výchozí Admin Účet
Pokud není nastaveno jinak v proměnných prostředí:
- **Email:** `admin@remax.cz`
- **Username:** `admin`
- **Heslo:** `changeme1234`

**Důležité:** Po prvním nasazení se okamžitě přihlaste a změňte heslo, nebo vytvořte nového admina a tohoto smažte.

### 6.2 Blokování Uživatelů
Admin má právo zablokovat uživatele při podezřelém chování.
- **Endpoint:** `POST /api/admin/block/{username}?until=2025-12-31T23:59:59Z`
- Systém také automaticky blokuje účty po **3 neúspěšných pokusech** o přihlášení (na 24 hodin). Tyto parametry lze upravit v konfiguraci.

---

## 7. API Dokumentace (Swagger)

Backend generuje automatickou dokumentaci všech REST endpointů. To je užitečné pro testování konektivity a pro vývojáře frontendu.

Po spuštění aplikace je dokumentace dostupná na:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## 8. Řešení Problémů

### 8.1 Aplikace nenastartuje (Connection refused)
- **Příčina:** Backend se nemůže připojit k databázi.
- **Řešení:**
    1. Ověřte, že kontejner `db` běží (`docker ps`).
    2. Zkontrolujte logy backendu: `docker logs remax-backend`.
    3. Pokud běžíte lokálně, ujistěte se, že `SPRING_DATASOURCE_URL` míří na správný port (zpravidla `localhost:5432`).

### 8.2 CORS Error v prohlížeči
- **Příčina:** Frontend běží na jiné adrese/portu, než který backend očekává.
- **Řešení:** Upravte proměnnou `REMAX_SECURITY_CORS_ALLOWED_ORIGINS`. Povolte např. `http://localhost:3000` nebo `*` (pouze pro vývoj!).

### 8.3 "FATAL: password authentication failed"
- **Příčina:** Heslo v konfiguraci backendu nesouhlasí s heslem nastaveným v databázi.
- **Řešení:** Sjednoťte proměnné `POSTGRES_PASSWORD` (v sekci db) a `SPRING_DATASOURCE_PASSWORD` (v sekci backend) v `docker-compose.yml`.

### 8.4 Upload obrázku selže (413 Payload Too Large)
- **Příčina:** Výchozí nastavení Spring Boot limituje velikost uploadu.
- **Řešení:** Přidejte do konfigurace:
  ```properties
  spring.servlet.multipart.max-file-size=10MB
  spring.servlet.multipart.max-request-size=10MB
  ```

---
**© 2025 Backend Operations Team**