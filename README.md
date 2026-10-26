# Smart Kitchen Inventory System
*Making Kitchen Inventory Smarter & Simpler*

A server-rendered Java web app to track kitchen items, quantities and expiry dates, with low-stock and expiry alerts.

**Stack:** Java 21 · Spring Boot 3.3 · Thymeleaf + Bootstrap 5 · PostgreSQL · Spring Data JPA/Hibernate · Flyway · Maven · JUnit 5 + Mockito

---
## 1. One-time setup (Windows)

Check tools (PowerShell, any folder):
```powershell
java -version      # must say 21
mvn -version       # Maven 3.9+
git --version
psql --version     # PostgreSQL 14+
```
Missing something? Install: **JDK 21** (Temurin: adoptium.net), **Maven** (maven.apache.org – unzip and add `bin` to PATH, set `JAVA_HOME`), **Git**, **PostgreSQL** (postgresql.org – remember the password you set for user `postgres`).
VS Code extensions: *Extension Pack for Java* and *Spring Boot Extension Pack*.

## 2. Create the database
```powershell
psql -U postgres -c "CREATE DATABASE smart_kitchen;"
```
(If `psql` is not found, open **SQL Shell (psql)** from the Start menu and run `CREATE DATABASE smart_kitchen;`.)

## 3. Configure the connection (secrets stay out of Git)
From the project folder:
```powershell
Copy-Item config\application.properties.example config\application.properties
notepad config\application.properties     # put your real postgres password
```
`config/application.properties` is in `.gitignore`. Spring Boot reads it automatically.

## 4. Run
```powershell
mvn spring-boot:run
```
Open http://localhost:8080 . Flyway creates the tables on first start and 11 demo items are added **only if the table is empty** (no duplicates on restart). Data survives restarts.

## 5. Test
```powershell
mvn test
```
Tests use an in-memory H2 database (tests only). The real app always uses PostgreSQL.

---
## Rules used by the app
| Item | Rule |
|---|---|
| Out of Stock | quantity = 0 |
| Low Stock | 0 < quantity <= minimum stock |
| In Stock | quantity > minimum stock |
| No Expiry | no expiry date (never "expired") |
| Expired | expiry date is before today |
| Expiring Soon | today <= expiry date <= today + N days (N = 7, change with `EXPIRY_SOON_DAYS`) |
| Fresh | later than today + N |

Statuses are calculated on every request from today's date, never stored.

## Features
Dashboard (hero, stat cards, alerts, stock-health bar, items-by-category chart, recent activity) · add / view / edit / delete · search, filter, sort · quick -1 / +1 / "Mark as used up" · duplicate-name warning · Low-stock and Expiry pages · **Shopping list** (print / copy) · **CSV export** (respects current filters) · Help & FAQ · dark mode · mobile card layout · friendly error pages · activity log.

## Pages
Dashboard `/` · Inventory list (search, filter, sort) `/inventory` · Add `/inventory/new` · Details `/inventory/{id}` · Edit `/inventory/{id}/edit` · Low stock `/inventory/low-stock` · Expiry `/inventory/expiry` · 404/error pages.

## Project structure
```
controller/  InventoryController, DashboardController   (web requests only)
service/     InventoryService (interface), InventoryServiceImpl, InventoryStatusService
repository/  InventoryRepository, ActivityLogRepository (Spring Data JPA)
entity/      InventoryItem, ActivityLog, Category, Unit, StockStatus, ExpiryStatus, ActivityType, StatusLabel
dto/         InventoryItemForm, ItemFilter, InventoryItemView, DashboardSummary, Alert
validation/  ItemFormValidator
exception/   ItemNotFoundException, GlobalExceptionHandler
config/      AppConfig (Clock), DemoDataLoader
resources/   templates (Thymeleaf), static/css, static/js, db/migration (Flyway)
```

---
## 6. Deploy on Render
1. Push to GitHub (`git init`, `git add .`, `git commit -m "Smart Kitchen"`, create repo, `git push`). `.env` and `config/application.properties` are ignored.
2. Render → **New → PostgreSQL** (free). Copy the *Internal* host, database, user, password.
3. Render → **New → Web Service** → pick your repo → **Runtime: Docker** (the `Dockerfile` is included).
4. Environment variables:
   - `DB_URL` = `jdbc:postgresql://<internal-host>:5432/<database>`
   - `DB_USERNAME`, `DB_PASSWORD` = from the database page
   - `THYMELEAF_CACHE` = `true`
5. Deploy, open the URL, add an item, then use *Manual Deploy → Restart* and confirm the item is still there.

Note: the Render free web service sleeps when idle (first load is slow) and the free database expires after about 30 days. Open the site once before your demo.

---
### Pre-hosting checklist
- [ ] `mvn test` (or IntelliJ "Run All Tests") passes
- [ ] App runs against Supabase and every page opens (Dashboard, Inventory, Add, Edit, Details, Low stock, Expiry, Shopping list, Help, 404)
- [ ] No real passwords in Git (`.env`, `config/application.properties` are ignored)
- [ ] Render env vars set: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `THYMELEAF_CACHE=true`; Render health check path = `/health`
- [ ] Open the live URL once before the demo (free tier sleeps)
- [ ] Take screenshots

Hosting settings already built in: proxy-aware https redirects, small DB connection pool, timezone set in code (`APP_TIMEZONE`, default Asia/Kolkata), no stack traces shown to users, non-root Docker user.

## 7. Where OOP appears (for the viva)
- **Encapsulation** – `InventoryItem` has private fields; `setQuantity()` / `setMinimumStock()` throw an exception on negative values, so an invalid item cannot exist.
- **Abstraction** – controllers use the `InventoryService` *interface*; `InventoryRepository` is only an interface (Spring writes the code).
- **Polymorphism** – `StockStatus` and `ExpiryStatus` both implement `StatusLabel`; the page shows any status through the same `getLabel()/getBadgeClass()` calls, each enum answering differently. (Also: `InventoryServiceImpl` can be swapped for another implementation.)
- **Inheritance** – `ItemNotFoundException extends RuntimeException`; repositories extend `JpaRepository`. We deliberately did not force more inheritance.
- **Constructors / objects** – `new InventoryItem(...)`, `InventoryItemView(item, statuses)`, constructor injection of services.
- **Separation of concerns** – controller (web) → service (rules) → repository (database). Rules live only in `InventoryStatusService`.
- **Testability** – an injectable `Clock` lets tests freeze "today" for exact boundary tests.

### Viva Q&A
1. **What is encapsulation here?** Private fields with setters that validate; e.g. quantity can never be negative.
2. **Why an interface for the service?** Controllers depend on the abstraction, so the implementation can change without touching them.
3. **Where is polymorphism?** `StatusLabel` implemented by two enums; the template treats both the same.
4. **Why enums for Category/Unit?** Fixed allowed values, so no typos and easy dropdowns.
5. **Why a DTO (`InventoryItemForm`)?** Keeps form input and validation separate from the database entity.
6. **Why is status not stored in the DB?** It depends on today's date; storing it would go stale. We calculate it.
7. **Why a `Clock`?** So tests can fix the date and test boundaries (today, +7, +8 days).
8. **What does Flyway do?** Runs versioned SQL files (`V1__...sql`) so any fresh database gets the same tables.
9. **How do you prevent SQL injection?** Spring Data JPA uses parameterized queries; we never build SQL from user text.
10. **How is XSS prevented?** Thymeleaf `th:text` escapes user text automatically.
11. **What is MVC here?** Model = entities/DTOs, View = Thymeleaf templates, Controller = `InventoryController`.
12. **How are errors handled?** `GlobalExceptionHandler` shows friendly 404/400/500 pages; stack traces go only to the server log.

## 8. Report outline
1. Introduction & problem statement · 2. Objectives · 3. Scope (in/out) · 4. Technology stack · 5. System requirements · 6. Use cases (Add, View, Edit, Delete, Update quantity, Search/Filter/Sort, View alerts) · 7. Architecture (layers diagram) · 8. Class diagram · 9. ER diagram (below) · 10. UI screenshots · 11. Testing (test list + results) · 12. Deployment · 13. Limitations & future work (login, email alerts, barcode scanning) · 14. Conclusion.

**ER spec:** `INVENTORY_ITEM(id PK, name, category, quantity, unit, minimum_stock, purchase_date, expiry_date, notes, created_at, updated_at)` and `ACTIVITY_LOG(id PK, type, item_name, details, created_at)`. The log stores the item *name* (not a foreign key) so history survives deletion; the two tables are intentionally not linked.

**Screenshots to capture:** dashboard, inventory list with filters, add form with validation errors, details page, low-stock page, expiry page, 404 page, mobile view, live Render URL.
