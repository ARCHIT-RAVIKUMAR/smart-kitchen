# Smart Kitchen Inventory System
*Making Kitchen Inventory Smarter & Simpler*

**Live demo:** https://smart-kitchen-nhfa.onrender.com/
The demo runs on a free hosting tier. After a period of inactivity the server sleeps, so the first page can take 1 to 2 minutes to load. The demo has no login, so please do not enter real personal data.

A server-rendered Java web app to track kitchen items, quantities and expiry dates, with low-stock and expiry alerts, a shopping list and CSV export. College Java OOP project.

**Stack:** Java 21 · Spring Boot 3.3 · Thymeleaf + Bootstrap 5 · PostgreSQL (Supabase) · Spring Data JPA/Hibernate · Flyway · Maven · JUnit 5 + Mockito · Docker on Render

---
## Table of contents
1. Features · 2. Rules used by the app · 3. Pages · 4. Architecture · 5. OOP concepts · 6. Database · 7. Security · 8. Run locally · 9. Configuration · 10. Testing · 11. Deployment on Render · 12. Pre-demo checklist · 13. Viva Q&A · 14. Report outline · 15. Limitations and future work

## 1. Features
- **Dashboard:** summary banner, six stat cards, alerts, stock-health bar, items-by-category chart, recent activity.
- **Inventory:** add, view, edit, delete (with confirmation), search by name, filter by category / stock status / expiry status, sort by name / quantity / expiry date.
- **Quick quantity:** -1, +1 and "Mark as used up" buttons, plus an exact quantity box.
- **Duplicate-name warning** when adding an item whose name already exists.
- **Alerts:** Low stock and Expiry pages; one alert per item per condition.
- **Shopping list:** low and out-of-stock items with a suggested amount to buy (restock to twice the minimum); print or copy.
- **CSV export** of the current search / filter (opens in Excel; formulas are neutralised).
- **Help & FAQ** page, dark mode, mobile card layout, friendly 404 / error pages, activity log.
- Validation (no negative numbers, required fields, expiry not before purchase date); items without an expiry date are handled.

## 2. Rules used by the app
| Status | Rule |
|---|---|
| Out of Stock | quantity = 0 |
| Low Stock | 0 < quantity <= minimum stock |
| In Stock | quantity > minimum stock |
| No Expiry | no expiry date (never "expired") |
| Expired | expiry date is before today |
| Expiring Soon | today <= expiry date <= today + N days (N = 7, change with `EXPIRY_SOON_DAYS`) |
| Fresh | later than today + N days |

Statuses are calculated on every request from today's date, never stored.

## 3. Pages
Dashboard `/` · Inventory `/inventory` · Add `/inventory/new` · Details `/inventory/{id}` · Edit `/inventory/{id}/edit` · Low stock `/inventory/low-stock` · Expiry `/inventory/expiry` · Shopping list `/inventory/shopping-list` · CSV `/inventory/export.csv` · Help `/help` · Health check `/health` · 404 / error pages.

## 4. Architecture
Layered: controller -> service (interface + implementation) -> repository -> entity, with DTOs for forms.
```
controller/  InventoryController, DashboardController
service/     InventoryService (interface), InventoryServiceImpl, InventoryStatusService
repository/  InventoryRepository, ActivityLogRepository (Spring Data JPA)
entity/      InventoryItem, ActivityLog, Category, Unit, StockStatus, ExpiryStatus, ActivityType, StatusLabel
dto/         InventoryItemForm, ItemFilter, InventoryItemView, DashboardSummary, Alert, CategoryCount
validation/  ItemFormValidator
exception/   ItemNotFoundException, GlobalExceptionHandler
config/      AppConfig (Clock), DemoDataLoader
util/        CsvUtil
resources/   templates (Thymeleaf), static/css, static/js, db/migration (Flyway)
```

## 5. OOP concepts (for the viva)
- **Encapsulation:** `InventoryItem` has private fields; `setQuantity()` / `setMinimumStock()` throw on negative values, so an invalid item cannot exist.
- **Abstraction:** controllers use the `InventoryService` interface; repositories are interfaces (Spring writes the code).
- **Polymorphism:** `StockStatus` and `ExpiryStatus` both implement `StatusLabel`; pages show any status through the same `getLabel()` / `getBadgeClass()` calls.
- **Inheritance:** `ItemNotFoundException extends RuntimeException`; repositories extend `JpaRepository`. Not forced elsewhere.
- **Constructors / composition:** `new InventoryItem(...)`, services built through constructor injection.
- **Separation of concerns:** web in controllers, rules in services (stock and expiry rules only in `InventoryStatusService`), data in repositories.
- **Testability:** an injectable `Clock` lets tests freeze "today" for exact boundary tests.

## 6. Database
PostgreSQL on Supabase. Flyway migration `V1__create_tables.sql` creates `inventory_item` and `activity_log` (with CHECK constraints and indexes); Hibernate does not change the schema. Demo items are added only when the table is empty (no duplicates on restart). Never edit a migration that has already run; add a new one.

**ER spec:** `INVENTORY_ITEM(id PK, name, category, quantity, unit, minimum_stock, purchase_date, expiry_date, notes, created_at, updated_at)` and `ACTIVITY_LOG(id PK, type, item_name, details, created_at)`. The log stores the item name (not a foreign key) so history survives deletion.

## 7. Security
- Server-side validation, JPA parameterised queries (no SQL injection), Thymeleaf output escaping (no XSS), CSV formula neutralisation.
- Error pages never show stack traces or internal details.
- Secrets live only in environment variables; `.env` and `config/application.properties` are git-ignored.
- The demo has **no login** (not required by the project brief), so anyone with the link can edit data. Login is listed under future work.

## 8. Run locally (IntelliJ)
1. Open the folder in IntelliJ (select `pom.xml`, JDK 21).
2. Create a free Supabase project. Under **Connect** use the **Session pooler** details (port 5432).
3. **Run -> Edit Configurations -> SmartKitchenApplication -> Modify options -> Environment variables**, paste one line:
```
DB_URL=jdbc:postgresql://<host>:5432/postgres?sslmode=require;DB_USERNAME=<user>;DB_PASSWORD=<db password>
```
Use letters and numbers in the password (no `;`).
4. Run `SmartKitchenApplication` and open http://localhost:8080 .

Alternative with a local PostgreSQL: copy `config/application.properties.example` to `config/application.properties` and fill it in.

## 9. Configuration (environment variables)
| Variable | Required | Purpose |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Yes | PostgreSQL connection |
| `THYMELEAF_CACHE` | Production: `true` | Template caching |
| `EXPIRY_SOON_DAYS` | No (7) | "Expiring soon" window |
| `APP_TIMEZONE` | No (Asia/Kolkata) | Timezone for "today" and activity times |
| `SEED_DEMO_DATA` | No (true) | Add demo items when the table is empty |
| `PORT` | Set by host | HTTP port |

Never commit real credentials.

## 10. Testing
```
mvn test            # or IntelliJ: right-click src/test/java -> Run 'All Tests'
```
Tests use an in-memory H2 database, so no Supabase connection is needed.

| Test class | Covers |
|---|---|
| `InventoryStatusServiceTest` | Stock and expiry rules, boundary dates (today, +7, +8), no-expiry items |
| `InventoryServiceImplTest` / `InventoryServiceExtrasTest` | Create, update, delete, quantity rules, search / sort, dashboard counts, alerts, shopping list, category breakdown, duplicate name |
| `InventoryItemFormValidationTest` | Input validation, date order, entity rules |
| `InventoryRepositoryTest` | Database save / find / delete, activity log |
| `InventoryControllerTest` / `InventoryControllerExtrasTest` | Pages, 404 / 400 handling, form errors, redirects, CSV, adjust, duplicate warning |
| `InventoryIntegrationTest` / `PagesIntegrationTest` | Full flow web -> service -> database |
| `CsvUtilTest` | CSV escaping and formula neutralisation |

## 11. Deployment on Render
1. Push the repository to GitHub.
2. Render -> **New -> Web Service** -> select the repo -> Language **Docker**, Instance type **Free**.
3. Environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `THYMELEAF_CACHE=true`.
4. Advanced -> Health Check Path: `/health`.
5. Every push to `main` redeploys automatically. First start is slow (about 90 seconds) on the free tier.

## 12. Pre-demo checklist
- [ ] Open https://smart-kitchen-nhfa.onrender.com/ a few minutes early to wake the server
- [ ] Keep the local version ready as a backup
- [ ] All tests pass (`mvn test`)
- [ ] Demo path: dashboard alerts -> add item (show validation errors) -> edit -> -1 button -> search / filter / sort -> shopping list -> CSV export -> dark mode
- [ ] Screenshots: dashboard, inventory with filters, add form with errors, details, low-stock, expiry, shopping list, 404 page, mobile view

## 13. Viva Q&A
1. **What is encapsulation here?** Private fields with setters that validate; quantity can never be negative.
2. **Why an interface for the service?** Controllers depend on the abstraction, so the implementation can change without touching them.
3. **Where is polymorphism?** `StatusLabel` is implemented by two enums; the template treats both the same.
4. **Why enums for Category / Unit?** Fixed allowed values: no typos, easy dropdowns.
5. **Why a DTO (`InventoryItemForm`)?** Keeps form input and validation separate from the database entity.
6. **Why is status not stored in the database?** It depends on today's date and would go stale; we calculate it.
7. **Why a `Clock`?** Tests can fix the date and check boundaries exactly.
8. **What does Flyway do?** Runs versioned SQL files so any fresh database gets the same tables.
9. **SQL injection?** JPA uses parameterised queries; no SQL is built from user text.
10. **XSS?** Thymeleaf `th:text` escapes user text.
11. **How are errors handled?** `GlobalExceptionHandler` shows friendly 404 / 400 / 500 pages; details go only to the server log.
12. **Why no login?** The brief says not to add login unless needed; it is listed as future work.

## 14. Report outline
1. Introduction and problem statement · 2. Objectives · 3. Scope (in / out) · 4. Technology stack · 5. Requirements · 6. Use cases (add, view, edit, delete, update quantity, search / filter / sort, alerts, shopping list, export) · 7. Architecture diagram · 8. Class diagram · 9. ER diagram · 10. Screenshots · 11. Testing results · 12. Deployment · 13. Limitations and future work · 14. Conclusion.

## 15. Limitations and future work
No login (anyone with the link can edit data); no email or push alerts; free hosting sleeps when idle; no barcode scanning. Possible future work: password login or multi-user accounts, email reminders, recipe suggestions.
