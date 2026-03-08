# SKILLS.md — Automation Architect Reference

> This file documents the skills, patterns, and workflows expected of a senior/lead
> automation engineer working with this framework. It is also a reference for Claude
> when helping architects with advanced tasks.

---

## 1. Framework Design Patterns Used

### Pattern: Client–Context–Steps Separation

```
Feature File (Gherkin)
      ↓  calls
Step Definitions          ← thin orchestration layer
      ↓  calls
ApiClient subclass        ← HTTP abstraction (RestAssured wrapped)
      ↓  reads/writes
ScenarioContext           ← scenario-scoped state (PicoContainer injected)
      ↓  reads
ConfigManager             ← environment config (singleton)
```

**Why**: Prevents RestAssured leaking into Gherkin layer. Steps stay readable. Clients stay reusable.

### Pattern: Builder + Lombok for Test Data

```java
Post post = Post.builder()
    .userId(1)
    .title("My test post")
    .body("body content")
    .build();
```

Use `.toBuilder()` to derive variants: `basePost.toBuilder().title("different").build()`

### Pattern: Soft Assertions for Multi-field Validation

```java
SoftAssertions soft = new SoftAssertions();
soft.assertThat(post.getId()).isPositive();
soft.assertThat(post.getTitle()).isNotEmpty();
soft.assertThat(post.getUserId()).isEqualTo(1);
soft.assertAll();  // reports all failures at once
```

### Pattern: JSON Schema Validation as Contract Test

Every API endpoint should have a schema file. Schema tests act as contract tests:
they catch breaking API changes even when values are acceptable.

```java
response.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/post-schema.json"));
```

---

## 2. Advanced Testing Techniques

### 2.1 Data-Driven Testing with Scenario Outline

```gherkin
Scenario Outline: Create post with various user IDs
  When I create a post with userId <userId> and title "<title>"
  Then the response status code should be 201
  And the created post userId should be <userId>

  Examples:
    | userId | title             |
    | 1      | First user post   |
    | 5      | Fifth user post   |
    | 10     | Tenth user post   |
```

### 2.2 Chained API Calls (Dependent Resources)

When Test B depends on data from Test A:

```java
// In step definition: create user, then use the returned ID
@When("I create a user and retrieve their posts")
public void createUserAndGetPosts() {
    Response userResponse = usersApiClient.createUser(testUser);
    int userId = userResponse.jsonPath().getInt("id");
    context.set("createdUserId", userId);
    context.setResponse(postsApiClient.getPostsByUserId(userId));
}
```

### 2.3 Request Specification with Auth

Extend `buildRequestSpec()` in your client for auth:

```java
@Override
protected RequestSpecification buildRequestSpec() {
    String token = config.get("api.auth.token", System.getenv("AUTH_TOKEN"));
    return new RequestSpecBuilder()
            .setBaseUri(config.getBaseUrl())
            .addHeader("Authorization", "Bearer " + token)
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();
}
```

### 2.4 Response Time Assertions

```java
// Assert response time is under 2 seconds
response.then().time(lessThan(2000L));

// In step definition
assertThat(context.getResponse().getTime())
    .as("Response should complete within 2s")
    .isLessThan(2000);
```

### 2.5 Parallel Execution Strategy

For 100-engineer teams, parallelisation is critical:

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <useUnlimitedThreads>false</useUnlimitedThreads>
        <threadCount>4</threadCount>
    </configuration>
</plugin>
```

**Critical rule**: Never share mutable state between scenarios. ScenarioContext is
PicoContainer-scoped (new instance per scenario) — this is already thread-safe.

---

## 3. CI/CD Integration

### GitHub Actions Example

```yaml
name: API Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        env: [dev, staging]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run smoke tests
        run: mvn test -Denv=${{ matrix.env }} -Dcucumber.filter.tags="@smoke"
        env:
          AUTH_TOKEN: ${{ secrets.AUTH_TOKEN }}
      - name: Upload Allure results
        uses: actions/upload-artifact@v4
        with:
          name: allure-results-${{ matrix.env }}
          path: target/allure-results
```

### Tagging Strategy for CI Gates

| Pipeline Stage | Tag Filter | Purpose |
|----------------|------------|---------|
| PR check | `@smoke` | Fast feedback (< 5 min) |
| Merge to main | `@regression` | Full suite |
| Nightly | `@regression and not @wip` | All stable tests |
| Release gate | `@smoke and @critical` | Minimal risk gate |

---

## 4. Framework Extension Cookbook

### Add a New API Service (Checklist)

```
[ ] Create model POJO:      src/main/java/com/automation/models/MyEntity.java
[ ] Create API client:      src/main/java/com/automation/clients/MyApiClient.java
[ ] Create JSON schema:     src/test/resources/schemas/my-entity-schema.json
[ ] Create feature file:    src/test/resources/features/my-entity.feature
[ ] Create step defs:       src/test/java/com/automation/steps/MyEntityStepDefinitions.java
[ ] Register in CLAUDE.md:  Section 2 — API Catalogue
[ ] Update config:          config-staging.properties with new base URL if different service
[ ] Add @domain tag:        All scenarios in the feature file
```

### Add a New Environment

```
[ ] Create config-{env}.properties in src/test/resources/config/
[ ] Add Maven profile in pom.xml
[ ] Add CI job matrix entry
[ ] Document base URL in CLAUDE.md Section 2
```

### Add Authentication

```
[ ] Override buildRequestSpec() in the client
[ ] Read token from config.get("api.auth.token") or System.getenv()
[ ] Add @Before hook to refresh tokens if they expire
[ ] Document auth method in CLAUDE.md Section 2 for your API
[ ] Never commit tokens — use CI secrets or .env (gitignored)
```

---

## 5. Code Review Checklist

Use this when reviewing PRs. Claude uses this list when asked to review code.

### Feature Files
- [ ] Has `@regression` and a domain tag
- [ ] Has at least one `@smoke` scenario
- [ ] `@wip` on any unimplemented scenario
- [ ] No assertion logic in `Given` steps
- [ ] `Background:` used for repeated `Given` steps

### Step Definitions
- [ ] Constructor injection of `ScenarioContext` (no static fields)
- [ ] All HTTP calls via API client (not raw RestAssured)
- [ ] AssertJ used (not JUnit Assert)
- [ ] Every assertion has `.as("description")`
- [ ] State stored in context, not instance variables

### API Clients
- [ ] Extends `ApiClient`
- [ ] Returns `Response` — no assertions in client
- [ ] Javadoc with endpoint table
- [ ] No hardcoded URLs

### Models
- [ ] Lombok annotations present
- [ ] `@JsonIgnoreProperties(ignoreUnknown = true)`
- [ ] Boxed types for nullable fields

### Config
- [ ] No hardcoded values in source code
- [ ] New keys added to `config.properties`
- [ ] Secrets referenced via env vars

---

## 6. Claude AI Usage Patterns for Architects

### Prompts that work well

```
"Add a new API client for the Orders service. Base URL: https://api.example.com/v1.
 It uses Bearer token auth. Endpoints: GET /orders, POST /orders, GET /orders/{id}"

"Write a feature file for the Orders API covering happy path, validation errors, and schema."

"Review the steps in PostsStepDefinitions.java against our coding standards."

"Add a scenario outline for creating posts with different user IDs."

"What scenarios should I write to get good coverage of the /posts endpoint?"

"Generate the JSON schema for this API response: { userId: 1, id: 1, title: '...', body: '...' }"
```

### Context Claude already has (from CLAUDE.md)
- All API endpoints, models, clients, step definitions
- Framework patterns and conventions
- Naming rules, tag rules, and DI patterns
- How to extend the framework

### Things to always specify
- API base URL and auth method (if different from default)
- Target environment
- Which tags the new scenarios should carry

---

## 7. Troubleshooting Reference

| Problem | Likely Cause | Fix |
|---------|-------------|-----|
| `NullPointerException` in steps | Missing constructor injection | Add `ScenarioContext` param to constructor |
| Same step used in two classes fails | Duplicate step definition | Extract to shared steps class or rename |
| Test passes locally, fails in CI | Hardcoded URL or missing env var | Use `ConfigManager`, check CI secrets |
| Allure report empty | AspectJ weaver not on agent path | Check `argLine` in surefire plugin config |
| JSON schema assertion fails | Schema file not on classpath | Check file is in `src/test/resources/schemas/` |
| PicoContainer injection error | Missing no-arg constructor on context class | Add `@NoArgsConstructor` or default constructor |
| 100 engineers, slow suite | No parallelism configured | Add surefire parallel config, ensure no static state |
