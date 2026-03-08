# CLAUDE.md — Automation Framework AI Context

> This file is the single source of truth for Claude AI when working in this repository.
> It is committed to git so every team member gets identical Claude behaviour.
> When joining a new team, read this file before writing any code or tests.

---

## 1. Project Overview

**Framework**: Cucumber 7 + JUnit 4 + RestAssured 5 — Java 17 — Maven
**Purpose**: API test automation for teams across the organisation
**Report**: Allure (HTML) + Cucumber JSON
**CI**: Maven Surefire — run via `mvn test`

### Directory Map
```
src/
├── main/java/com/automation/
│   └── config/         ← ConfigManager (env-aware, singleton)
├── test/java/com/automation/
│   ├── clients/        ← One ApiClient subclass per API service
│   ├── context/        ← ScenarioContext (PicoContainer DI per scenario)
│   ├── hooks/          ← Before/After Cucumber hooks
│   ├── models/         ← Jackson POJOs matching API response schemas
│   ├── runners/        ← JUnit TestRunner
│   ├── steps/          ← Step definition classes (one per feature domain)
│   └── utils/          ← Shared helpers (JsonSchemaUtils, etc.)
└── test/resources/
    ├── features/       ← .feature files (Gherkin)
    ├── schemas/        ← JSON Schema files for response validation
    └── config/         ← config.properties, config-staging.properties, etc.
```

---

## 2. API Catalogue

> Add your team's APIs here. Each section follows the same template.
> Claude reads this to understand what each API does, how to test it, and what edge cases matter.

### 2.1 JSONPlaceholder (Reference / Sample API)

**Base URL**: `https://jsonplaceholder.typicode.com`
**Auth**: None (public API)
**Client class**: `PostsApiClient`, `UsersApiClient`
**Purpose**: Used as sample/reference API to demonstrate framework patterns

#### /posts
| Method | Path | Description | Success Code |
|--------|------|-------------|--------------|
| GET | /posts | List all posts (100 records) | 200 |
| GET | /posts/{id} | Get single post | 200 |
| GET | /posts?userId={n} | Filter posts by user | 200 |
| POST | /posts | Create post | 201 |
| PUT | /posts/{id} | Full replace | 200 |
| PATCH | /posts/{id} | Partial update | 200 |
| DELETE | /posts/{id} | Delete post | 200 |

**Post model**: `{ id: int, userId: int, title: string, body: string }`
**Error cases**: 404 for id > 100, no auth required
**Schema file**: `src/test/resources/schemas/post-schema.json`
**Feature file**: `src/test/resources/features/posts.feature`

#### /users
| Method | Path | Description | Success Code |
|--------|------|-------------|--------------|
| GET | /users | List all users (10 records) | 200 |
| GET | /users/{id} | Get single user | 200 |
| GET | /users/{id}/posts | Get posts authored by user | 200 |

**User model**: `{ id, name, username, email, phone, website, address, company }`
**Client class**: `UsersApiClient`

---

### 2.2 [YOUR TEAM'S API — Add Here]

> Copy this template for each new API your team owns:

```
**Service Name**: [e.g., Orders Service]
**Base URL**: https://api.yourcompany.com/v1
**Auth**: Bearer token — header: Authorization: Bearer {token}
**Token source**: Environment variable AUTH_TOKEN or config key api.auth.token
**Client class**: OrdersApiClient (to be created in com.automation.clients)

#### /orders
| Method | Path | Description | Success Code |
|--------|------|-------------|--------------|
| GET | /orders | List orders (paginated) | 200 |
| GET | /orders/{id} | Get order by ID | 200 |
| POST | /orders | Create order | 201 |
| PUT | /orders/{id}/cancel | Cancel order | 200 |

**Request model**: OrderRequest { customerId, items[], currency }
**Response model**: Order { id, status, total, createdAt }
**Pagination**: ?page=1&size=20 (header: X-Total-Count)
**Key test scenarios**:
  - Create order with valid items → 201 with id
  - Create order with out-of-stock item → 422 with error code ITEM_UNAVAILABLE
  - Get order with wrong customer auth → 403
  - Cancel already-cancelled order → 409
**Schema file**: src/test/resources/schemas/order-schema.json
**Feature file**: src/test/resources/features/orders.feature
```

---

## 3. Coding Standards & Best Practices

Claude MUST follow these rules when writing or reviewing any code in this repository.

### 3.1 Step Definitions

- **One step definition class per feature domain** (e.g., `PostsStepDefinitions`, `OrdersStepDefinitions`)
- Steps must be **stateless** — all state goes into `ScenarioContext`
- **Never** call `RestAssured` directly from step definitions — use the API client
- Step text should read like plain English, avoid technical terms in Gherkin
- Reuse existing steps before creating new ones

```java
// CORRECT: delegate to client, store in context
@When("I request post with id {int}")
public void iRequestPostWithId(int postId) {
    context.setResponse(postsApiClient.getPostById(postId));
    context.set("requestedPostId", postId);
}

// WRONG: RestAssured directly in step, no context
@When("I request post with id {int}")
public void iRequestPostWithId(int postId) {
    Response r = RestAssured.get("https://jsonplaceholder.typicode.com/posts/" + postId);
    Assert.assertEquals(200, r.statusCode());  // assertion mixed with action
}
```

### 3.2 API Clients

- **Extend `ApiClient`** for every new service — never use raw RestAssured in tests
- Client methods return `Response` — assertions belong in step definitions, NOT clients
- Method names must match HTTP semantics: `getPostById()`, `createPost()`, `deletePost()`
- Add Javadoc on the class listing all endpoints, models, and auth method
- One class per service (not one class for all services)

### 3.3 Models (POJOs)

- Use `@Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonIgnoreProperties(ignoreUnknown=true)`
- Use `Integer`/`Long` (boxed) not `int`/`long` — null safety for optional fields
- Add a Javadoc comment describing each field's meaning and API source

### 3.4 Feature Files (Gherkin)

- **Every feature** must have at least one `@smoke` scenario
- **Tags required**: `@regression` on all features, plus domain tag (`@posts`, `@orders`)
- `@wip` on any scenario that is not yet implemented (excluded from CI runs)
- Use `Background:` for shared Given steps — avoid duplicating setup across scenarios
- Use `Scenario Outline:` with `Examples:` for data-driven tests
- **No assertions in Given steps** — Given is for state setup only

```gherkin
# CORRECT tag pattern
@orders @regression
Feature: Orders API

  Background:
    Given the orders API is available

  @smoke
  Scenario: Create order successfully

  @negative
  Scenario: Create order with invalid data

# WRONG — missing tags, assertions in Given
Feature: Orders
  Given I create an order and it returns 201
```

### 3.5 Assertions

- Use **AssertJ** (`assertThat()`) — never JUnit `Assert.assertEquals()`
- Always provide a human-readable `.as("message")` on every assertion
- Assert **one logical concern per step** — split into multiple steps if needed
- Validate response schema with `JsonSchemaUtils.matchesSchema()` in `@schema`-tagged scenarios

### 3.6 Configuration

- **No hardcoded URLs, credentials, or timeouts** in source code
- All values in `config.properties` / `config-{env}.properties`
- Sensitive values via environment variables (`System.getenv()`) or CI secrets
- Use `ConfigManager.getInstance().get("key")` — never `System.getProperty()` directly in tests

### 3.7 Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Test class | PascalCase + domain | `PostsStepDefinitions` |
| API Client | PascalCase + ApiClient | `OrdersApiClient` |
| Model | PascalCase noun | `Order`, `Post` |
| Feature file | kebab-case | `create-order.feature` |
| Config key | dot.notation | `api.base.url` |
| Step method | camelCase verb-noun | `iCreateAPost()` |
| Schema file | kebab-schema.json | `order-schema.json` |

### 3.8 Dependency Injection

- Use **PicoContainer** for sharing state between step classes
- Inject `ScenarioContext` via constructor in every step class and hooks class
- **Never** use static fields or ThreadLocal for scenario state
- Hooks must also receive `ScenarioContext` via constructor to clear it in `@After`

### 3.9 Logging

- Use SLF4J + Logback: `private static final Logger log = LoggerFactory.getLogger(getClass())`
- Log at `INFO` for scenario milestones, `DEBUG` for request/response details
- On failure in `@After`, log the last response body at `ERROR` level
- Never use `System.out.println()` in test code

---

## 4. How Claude Should Help

### When asked to add a new API:
1. Create a model POJO in `com.automation.models`
2. Create an API client extending `ApiClient` in `com.automation.clients`
3. Create a JSON schema in `src/test/resources/schemas/`
4. Create a feature file in `src/test/resources/features/`
5. Create step definitions in `com.automation.steps`
6. Register the API in Section 2 of this CLAUDE.md

### When asked to write a scenario:
- Read the API catalogue in Section 2 first
- Check for existing reusable steps in the step definitions before creating new ones
- Follow the tag and Gherkin conventions in Section 3.4
- Include at least: happy path, negative/error, and schema validation scenarios

### When reviewing code:
- Check against every rule in Section 3
- Flag any hardcoded values, direct RestAssured usage in steps, or missing tags
- Suggest AssertJ replacements for JUnit assertions

### When a new team member asks about an API:
- Read Section 2 and explain the endpoints, models, auth, and test approach
- Point them to the relevant feature file, client class, and step definitions

---

## 5. Running Tests

```bash
# All tests (default: dev env)
mvn test

# Specific environment
mvn test -Denv=staging
mvn test -P staging

# Specific tag
mvn test -Dcucumber.filter.tags="@smoke"
mvn test -Dcucumber.filter.tags="@posts and @regression"
mvn test -Dcucumber.filter.tags="@regression and not @wip"

# Generate Allure report
mvn allure:report
mvn allure:serve      # opens in browser

# Parallel execution (future)
mvn test -Dsurefire.forkCount=4
```

---

## 6. Adding a New Team / Service

1. Create a subdirectory under `features/` for your team: `features/payments/`
2. Add your API to Section 2 of this CLAUDE.md (raise a PR for review)
3. Use the team tag on all your scenarios: `@payments`
4. Create a dedicated `TestRunner` if you need isolated CI runs
5. Add environment config for your service base URL in `config-staging.properties`

---

## 7. Memory — Do Not Overwrite

This section is written by Claude across sessions. Architects should not edit it manually.

- Framework uses PicoContainer for DI — ScenarioContext injected per scenario, not static
- TestRunner excludes `@wip` by default
- All API responses stored in ScenarioContext.lastResponse — cleared in @After hook
- Config priority: System property > env-file > default config.properties
