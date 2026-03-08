# Cucumber Java Automation Framework

Enterprise-grade API test automation framework for teams of 100+ engineers.
Built on **Cucumber 7 + JUnit 4 + RestAssured 5 + Java 17 + Maven**.

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Run Tests

```bash
# All tests (dev environment)
mvn test

# Smoke tests only
mvn test -Dcucumber.filter.tags="@smoke"

# Full regression suite
mvn test -Dcucumber.filter.tags="@regression"

# Specific domain
mvn test -Dcucumber.filter.tags="@posts"

# Staging environment
mvn test -Denv=staging

# Combined: staging smoke
mvn test -Denv=staging -Dcucumber.filter.tags="@smoke"
```

### View Reports

```bash
# Generate Allure HTML report
mvn allure:report

# Serve Allure report in browser
mvn allure:serve
```

Reports also written to `target/cucumber-reports/cucumber.html`.

---

## Project Structure

```
src/
├── main/java/com/automation/
│   └── config/          ← ConfigManager (env-aware singleton)
├── test/java/com/automation/
│   ├── clients/         ← API client classes (one per service)
│   ├── context/         ← ScenarioContext (PicoContainer DI)
│   ├── hooks/           ← Cucumber @Before/@After hooks
│   ├── models/          ← Jackson POJOs (request/response models)
│   ├── runners/         ← JUnit TestRunner
│   ├── steps/           ← Step definition classes
│   └── utils/           ← Shared helpers (JsonSchemaUtils)
└── test/resources/
    ├── features/        ← Gherkin feature files
    ├── schemas/         ← JSON Schema files
    └── config/          ← config.properties per environment
```

---

## Framework Architecture

```
Feature File (.feature)
        ↓
Step Definitions (com.automation.steps)
        ↓                    ↓
  ApiClient subclass    ScenarioContext
  (RestAssured)         (PicoContainer)
        ↓
  ConfigManager
  (env properties)
```

- **ApiClient** — base class wrapping RestAssured. All service clients extend this.
- **ScenarioContext** — injected per-scenario via PicoContainer. No static state.
- **ConfigManager** — singleton, loads `config-{env}.properties`. Supports system property override.

---

## Adding a New API Service

1. **Model** → `src/test/java/com/automation/models/MyEntity.java`
2. **Client** → `src/test/java/com/automation/clients/MyServiceApiClient.java` (extend `ApiClient`)
3. **Schema** → `src/test/resources/schemas/my-entity-schema.json`
4. **Feature** → `src/test/resources/features/my-entity.feature`
5. **Steps** → `src/test/java/com/automation/steps/MyEntityStepDefinitions.java`
6. **Register** → Add API to `CLAUDE.md` Section 2 (API Catalogue)

---

## Environment Configuration

| Environment | Command | Config File |
|-------------|---------|-------------|
| dev (default) | `mvn test` | `config.properties` |
| staging | `mvn test -Denv=staging` or `-P staging` | `config-staging.properties` |
| prod | `mvn test -Denv=prod` or `-P prod` | `config-prod.properties` |

**Override at runtime**: `-Dapi.base.url=https://custom.url`

---

## Tag Strategy

| Tag | Meaning |
|-----|---------|
| `@smoke` | Critical path, fast, run on every PR |
| `@regression` | Full functional coverage |
| `@negative` | Error and edge case scenarios |
| `@schema` | JSON schema contract tests |
| `@wip` | Not yet implemented — excluded from CI |
| `@domain` | e.g., `@posts`, `@users`, `@orders` — run per team |

---

## AI Assistance (Claude)

This repo includes two AI context files:

- **`CLAUDE.md`** — API catalogue, coding standards, and patterns. Claude reads this automatically when you use Claude Code in this directory. Every new engineer and every AI session shares the same context.
- **`SKILLS.md`** — Advanced patterns, CI integration, and review checklists for automation architects.

### Useful Claude prompts

```
"Add a client and feature file for the Orders API at https://api.example.com"
"Write a scenario outline for creating posts with different user IDs"
"Review my step definition against our coding standards"
"What test coverage am I missing for the /posts endpoint?"
```

---

## Sample API

The framework ships with tests against [JSONPlaceholder](https://jsonplaceholder.typicode.com) —
a public, free REST API. These serve as working examples you can run immediately and as
templates to copy for your own APIs.

```bash
# Run the sample posts tests
mvn test -Dcucumber.filter.tags="@posts and @smoke"
```

---

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Cucumber | 7.15.0 | BDD framework |
| JUnit | 4.13.2 | Test runner |
| RestAssured | 5.4.0 | HTTP client + assertions |
| Jackson | 2.16.1 | JSON serialisation |
| Lombok | 1.18.30 | Boilerplate reduction |
| AssertJ | 3.25.1 | Fluent assertions |
| Allure | 2.25.0 | HTML reporting |
| PicoContainer | via Cucumber BOM | Dependency injection |
| Logback | 1.4.14 | Logging |

---

## Contributing

1. Read `CLAUDE.md` before writing any code
2. Follow the naming conventions and tagging rules in `CLAUDE.md` Section 3
3. Every new API must be registered in `CLAUDE.md` Section 2
4. All PRs must pass `@smoke` tag on CI before merge
5. Use `@wip` tag for in-progress scenarios — never commit failing scenarios without it
