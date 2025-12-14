# Best Practices Guide: Use Case Specifications & BDD

## Overview
This guide captures best practices for writing use case specifications and BDD scenarios, derived from real project experience and industry standards.

---

## Use Case Specifications

### 1. Preconditions vs Runtime Validation

#### ✅ **Correct Approach**
- **Preconditions**: Only system operational requirements
  - "System is operational and database is accessible"
  - "Application server is running"
- **Runtime Validation**: Feature flags, input validation, business rules
  - Checked in basic flow steps
  - Trigger alternative flows when conditions fail

#### ❌ **Anti-Pattern**
```markdown
- **Preconditions**: 
  - Feature flag "create-transaction" is enabled  # WRONG - this is runtime validation
  - System is operational
```

#### ✅ **Correct Pattern**
```markdown
- **Preconditions**: 
  - System is operational and database is accessible  # CORRECT - system requirement
```

### 2. Clean Basic Flows

#### ✅ **Correct Approach**
- Describe only the happy path
- Linear progression without conditional logic
- No branching statements

#### ❌ **Anti-Pattern**
```markdown
2. **System validates feature flag**: The system checks if the "create-transaction" feature flag is enabled. If enabled, processing continues to step 3. If disabled, the system follows alternative flow A1.
```

#### ✅ **Correct Pattern**
```markdown
2. **System validates feature flag**: The system checks if the "create-transaction" feature flag is enabled.
```

### 3. Alternative Flow Triggers

#### ✅ **Correct Approach**
- Alternative flows contain their own trigger descriptions
- Specific step references
- Clear condition descriptions

#### ✅ **Correct Pattern**
```markdown
#### A1: Feature Flag Disabled
- **Trigger**: In step 3, the "create-transaction" feature flag is disabled
- **Steps**:
  1. System checks feature flag and determines it is disabled
  2. System throws FeatureFlagDisabledException
  3. Global exception handler catches the exception
  4. System returns HTTP 403 Forbidden with error message
```

### 4. Exception Flows vs Alternative Flows

#### ✅ **Correct Approach**
- **Alternative Flows**: Specific validation failures or business logic exceptions during execution
- **Exception Flows**: Only actual exceptions that occur during execution
- **Precondition Failures**: Not included in use case (system unavailable, etc.)

#### ❌ **Anti-Pattern**
```markdown
### Exception Flows
- **System Unavailable**: If the application server is down, the request will fail with connection error
```

#### ✅ **Correct Pattern**
```markdown
### Exception Flows
- **Database Connection Lost**: If database connection fails during transaction persistence
```

### 5. Logical Separation of Concerns

#### ✅ **Correct Structure**
- **Preconditions**: What must be true before use case starts
- **Basic Flow**: Clean, linear happy path
- **Alternative Flows**: Specific failure scenarios with clear triggers
- **Exception Flows**: Only execution exceptions

---

## BDD (Behavior-Driven Development)

### 1. Problem Space vs Solution Space

#### ✅ **Correct Approach (Problem Space)**
- Describe business intent and outcomes
- Use business language stakeholders understand
- Focus on what the business gets

#### ❌ **Anti-Pattern (Solution Space)**
```gherkin
Given the system is operational and the database is accessible
And the "create-transaction" feature flag is enabled
When I send a POST request to "/transactions" with the following data:
```json
{
  "amount": 100.00,
  "description": "Office supplies",
  "type": "EXPENSE"
}
```
Then the system should validate the request format
And the system should validate the feature flag
```

#### ✅ **Correct Pattern (Problem Space)**
```gherkin
Given I want to record a business expense
And the transaction amount is $100.00
And the transaction description is "Office supplies"
And the transaction type is "EXPENSE"
When I create the transaction
Then the transaction should be recorded in the ledger
And the transaction should show the correct amount of $100.00
```

### 2. Black Box vs White Box

#### ✅ **Correct Approach (Black Box)**
- Focus on outcomes and business value
- Describe what the system delivers
- Avoid internal implementation details

#### ❌ **Anti-Pattern (White Box)**
```gherkin
Then the system should validate the request format
And the system should track the "transactions.created" metric
And the system should create a domain object with:
- A generated UUID as identifier
- Current timestamp as creation date
And the system should persist the transaction to the database
And the system should return HTTP 200 with the created transaction object
```

#### ✅ **Correct Pattern (Black Box)**
```gherkin
Then the transaction should be recorded in the ledger
And the transaction should have a unique identifier
And the transaction should show the correct amount of $100.00
And the transaction should show the description "Office supplies"
And the transaction should be marked as an expense
And the transaction should have a timestamp showing when it was created
```

### 3. DRY (Don't Repeat Yourself)

#### ✅ **Correct Approach**
- Reference existing documentation
- Don't repeat business rules already defined
- Keep scenarios focused on behavior

#### ❌ **Anti-Pattern**
```markdown
## Business Rules
- All transactions must have a positive amount
- All transactions must have a description
- Each transaction gets a unique identifier
- Each transaction is timestamped when created
- Transactions are permanently recorded in the ledger
```

#### ✅ **Correct Pattern**
```markdown
## Notes
- These scenarios test the basic flow (happy path) of UC-001: Create Transaction
- Business rules and constraints are defined in the use case specifications
- Scenarios focus on different data variations to ensure system robustness
```

### 4. Proper Gherkin Structure

#### ✅ **Correct Pattern**
```gherkin
Feature: Create Transaction

Scenario: Successfully Create an Expense Transaction
Given I want to record a business expense
And the transaction amount is $100.00
And the transaction description is "Office supplies"
And the transaction type is "EXPENSE"
When I create the transaction
Then the transaction should be recorded in the ledger
And the transaction should have a unique identifier
And the transaction should show the correct amount of $100.00
And the transaction should show the description "Office supplies"
And the transaction should be marked as an expense
And the transaction should have a timestamp showing when it was created
```

### 5. Business Language Guidelines

#### ✅ **Use Business Terms**
- "record a business expense" (not "send POST request")
- "transaction should be recorded in the ledger" (not "persist to database")
- "unique identifier" (not "UUID")
- "timestamp showing when it was created" (not "current timestamp")

#### ✅ **Focus on Outcomes**
- What the business gets
- What the user experiences
- What value is delivered

### 6. TODO Comments in Step Definitions

#### ✅ **Correct Approach**
- TODO comments are acceptable during initial implementation (Red phase of TDD)
- TODO comments must be removed once the method is successfully implemented
- Replace TODO comments with regular explanatory comments if the explanation adds value
- Keep code clean and maintainable by removing outdated TODOs

#### ❌ **Anti-Pattern**
```java
// WRONG: Leaving TODO comments after implementation is complete
@Given("I provide an idempotency key {string}")
public void i_provide_an_idempotency_key(String key) {
    // TODO: Store the idempotency key in shared test context
    // This key will be used by TransactionStepDefinitions when creating transactions
    testContext.setIdempotencyKey(key);  // Implementation is done, TODO should be removed
}
```

#### ✅ **Correct Pattern**
```java
// CORRECT: Remove TODO once implementation is complete
@Given("I provide an idempotency key {string}")
public void i_provide_an_idempotency_key(String key) {
    // Store the idempotency key in shared test context
    // This key will be used by TransactionStepDefinitions when creating transactions
    testContext.setIdempotencyKey(key);
}
```

#### ✅ **When to Remove TODOs**
- After the method implementation is complete and tested
- After the test passes (Green phase of TDD)
- During code review or refactoring phase
- Before committing code to version control

#### ✅ **When to Keep Comments**
- Keep explanatory comments that add value for future maintainers
- Keep comments that explain non-obvious business logic
- Keep comments that document complex test setup or data flow
- Remove only the "TODO:" prefix, keep useful explanations

---

## Test-Driven Development (TDD)

### 1. The Red-Green-Refactor Cycle

#### ✅ **Correct Approach**
1. **Red**: Write a failing test first
   - Test should fail for the right reason (missing functionality, not compilation error)
   - Test should be specific and focused on one behavior
2. **Green**: Write minimal code to make the test pass
   - Implement only what's needed to satisfy the test
   - Don't add extra features or optimizations yet
3. **Refactor**: Improve code quality while keeping tests green
   - Clean up code, remove duplication, improve naming
   - Ensure all tests still pass

#### ❌ **Anti-Pattern**
```java
// WRONG: Writing implementation before tests
public class MyService {
    public void doSomething() {
        // Full implementation written first
        // Then tests written to match
    }
}
```

#### ✅ **Correct Pattern**
```java
// CORRECT: Test first, then implementation
@Test
public void shouldDoSomething() {
    // Test written first - will fail
    MyService service = new MyService();
    String result = service.doSomething();
    assertEquals("expected", result);
}

// Then implement just enough to pass:
public String doSomething() {
    return "expected";  // Minimal implementation
}
```

### 2. One Test at a Time

#### ✅ **Correct Approach**
- Write one failing test
- Make it pass
- Refactor if needed
- Move to the next test
- Don't write multiple tests before implementing

#### ❌ **Anti-Pattern**
```java
// WRONG: Writing all tests upfront
@Test public void test1() { /* ... */ }
@Test public void test2() { /* ... */ }
@Test public void test3() { /* ... */ }
// Then implementing all at once
```

#### ✅ **Correct Pattern**
```java
// CORRECT: One test at a time
@Test
public void shouldHandleFirstCase() {
    // Write test, implement, verify
}

// After first test passes:
@Test
public void shouldHandleSecondCase() {
    // Write next test, implement, verify
}
```

### 3. Communicating TDD Intent

#### ✅ **How to Request TDD Workflow**

When requesting features, explicitly state:
- "I want to follow TDD for this feature"
- "Write the first failing test for [specific behavior]. Don't implement the code yet."
- "Now make that test pass by implementing the minimal code needed."
- "One test at a time, please"

#### ✅ **Example Request Pattern**
```
"I want to add [feature] using TDD:

1. First, write a failing test for [specific behavior]
2. Then implement just enough code to make it pass
3. Then we'll move to the next test

Let's start with test #1: [describe the first test case]"
```

### 4. Test Quality in TDD

#### ✅ **Good TDD Tests**
- **Specific**: Test one behavior at a time
- **Focused**: Clear what is being tested
- **Fast**: Run quickly (unit tests, not integration)
- **Independent**: Don't depend on other tests
- **Readable**: Test name clearly describes the behavior

#### ✅ **Test Naming Convention**
```java
// Pattern: should[ExpectedBehavior]When[Condition]
@Test
public void shouldReturnCachedResponseWhenSameIdempotencyKeyAndRequestHash() {
    // Test implementation
}

@Test
public void shouldReturnEmptyOptionalWhenIdempotencyKeyNotFound() {
    // Test implementation
}
```

### 5. TDD Workflow Best Practices

#### ✅ **Incremental Development**
- Start with the simplest test case
- Build complexity gradually
- Each test should add one new behavior
- Refactor when you see duplication or design issues

#### ✅ **When to Refactor**
- After a test passes (green phase)
- When you see code duplication
- When design becomes unclear
- When tests become hard to read

#### ❌ **When NOT to Refactor**
- While writing the first implementation (green phase)
- If tests are failing (red phase)
- If you're unsure about the design direction

### 6. TDD and Integration Tests

#### ✅ **Correct Approach**
- Use TDD for unit tests (fast feedback)
- Integration tests can be written after unit tests pass
- BDD scenarios (Cucumber) can be written after core functionality works

#### ✅ **Layered Testing Strategy**
1. **Unit Tests (TDD)**: Fast, focused, test one class/component
2. **Integration Tests**: Test component interactions
3. **BDD Scenarios**: Test end-to-end behavior from user perspective

### 7. Testing Framework: JUnit 4

#### ✅ **Correct Approach**
- **Use JUnit 4** for all tests in this project
- Cucumber requires JUnit 4 (via `cucumber-junit` dependency)
- Maven Surefire is configured for JUnit 4 (`surefire-junit4`)
- All test classes should use:
  - `import org.junit.Test;` (not `org.junit.jupiter.api.Test`)
  - `@RunWith(SpringRunner.class)` for Spring Boot tests
  - `public void` test methods (not package-private)

#### ❌ **Anti-Pattern**
```java
// WRONG: Using JUnit 5
import org.junit.jupiter.api.Test;

@SpringBootTest
class MyTest {
    @Test
    void shouldDoSomething() { // WRONG - JUnit 5 syntax
    }
}
```

#### ✅ **Correct Pattern**
```java
// CORRECT: Using JUnit 4
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MyTest {
    @Test
    public void shouldDoSomething() { // CORRECT - JUnit 4 syntax
    }
}
```

#### ✅ **Why JUnit 4?**
- **Cucumber Compatibility**: The project uses Cucumber for BDD testing, which requires JUnit 4
- **Maven Configuration**: Surefire plugin is configured with `surefire-junit4` provider
- **Consistency**: All existing tests use JUnit 4, maintaining consistency across the codebase
- **No JUnit 5**: Do not create JUnit 5 tests - they will not run with the current configuration

---

## Docker and Testcontainers Integration

### 1. Why Use Testcontainers

#### ✅ **Correct Approach**
- Use Testcontainers for integration tests that require real databases
- Provides production-like testing environment
- Ensures compatibility with actual database (PostgreSQL, MySQL, etc.)
- Automatically manages container lifecycle (start/stop)

#### ❌ **Anti-Pattern**
```java
// WRONG: Using in-memory H2 for all tests when production uses PostgreSQL
@SpringBootTest
public class MyTest {
    // H2 behaves differently than PostgreSQL - tests may pass but production fails
}
```

#### ✅ **Correct Pattern**
```java
// CORRECT: Use Testcontainers for integration tests
@SpringBootTest
@ActiveProfiles("test")
public class PostgreSQLIntegrationTest {
    @ClassRule
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    // Tests run against real PostgreSQL
}
```

### 2. Docker Configuration

#### ✅ **macOS with Docker Desktop**

**Option 1: Environment Variable (Recommended)**
```bash
# Add to ~/.zshrc or ~/.bash_profile
export DOCKER_HOST=unix://$HOME/.docker/run/docker.sock
```

**Option 2: Maven Surefire Configuration**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <environmentVariables>
            <DOCKER_HOST>unix://${user.home}/.docker/run/docker.sock</DOCKER_HOST>
        </environmentVariables>
    </configuration>
</plugin>
```

**Option 3: Testcontainers Properties File**
Create `~/.testcontainers.properties`:
```
docker.host=unix:///Users/your-username/.docker/run/docker.sock
```

#### ✅ **Linux**
- Usually works out of the box: `/var/run/docker.sock`
- If not, set: `export DOCKER_HOST=unix:///var/run/docker.sock`

#### ✅ **Windows**
- Docker Desktop usually works automatically
- If issues: Use named pipe: `npipe:////./pipe/docker_engine`

### 3. Testcontainers Best Practices

#### ✅ **Container Lifecycle Management**
```java
// CORRECT: Use @ClassRule for JUnit 4 (container shared across all tests in class)
@ClassRule
public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

// CORRECT: Use @Container for JUnit 5 (per-test or shared)
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
```

#### ✅ **Spring Boot Integration**
```java
// CORRECT: Use @DynamicPropertySource for JUnit 5
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
}

// CORRECT: Use system properties for JUnit 4
static {
    postgres.start();
    System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
    System.setProperty("spring.datasource.username", postgres.getUsername());
    System.setProperty("spring.datasource.password", postgres.getPassword());
}
```

#### ✅ **Container Reuse (Performance)**
```java
// CORRECT: Enable reuse for faster test execution (containers persist between runs)
@ClassRule
public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withReuse(true);  // Container stays running between test runs
```

**Note:** Requires `testcontainers.reuse.enable=true` in `~/.testcontainers.properties`

#### ❌ **Anti-Patterns**
```java
// WRONG: Starting container in @Before (creates new container per test - slow!)
@Before
public void setUp() {
    postgres.start();  // Don't do this with @ClassRule
}

// WRONG: Not cleaning up test data
@Test
public void testSomething() {
    // Creates data but doesn't clean up - affects other tests
}

// WRONG: Hardcoding container configuration
@ClassRule
public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("mydb")  // Hardcoded - not flexible
```

### 4. Test Organization

#### ✅ **Correct Approach**
- **Unit Tests**: Use H2 or mocks (fast, no Docker needed)
- **Integration Tests**: Use Testcontainers (real database, slower)
- **BDD Tests**: Can use either, depending on what you're testing

#### ✅ **Test Naming Convention**
```java
// Unit test (no Docker)
@Test
public void shouldSaveTransaction() { ... }

// Integration test (with Docker)
@Test
public void shouldSaveAndRetrieveTransactionFromPostgreSQL() { ... }
```

### 5. Troubleshooting

#### ✅ **Common Issues and Solutions**

**"Could not find a valid Docker environment"**
1. Verify Docker Desktop is running: `docker ps`
2. Check Docker socket path: `ls -la ~/.docker/run/docker.sock`
3. Set `DOCKER_HOST` environment variable
4. Verify Docker context: `docker context ls`

**"Container startup timeout"**
- Increase timeout: `.withStartupTimeout(Duration.ofMinutes(2))`
- Check Docker has enough resources (CPU/memory)

**"Port already in use"**
- Use random ports: Testcontainers does this automatically
- Or specify: `.withExposedPorts(5432)`

**"Image pull failures"**
- Pre-pull images: `docker pull postgres:15-alpine`
- Check network connectivity
- Verify Docker registry access

### 6. CI/CD Integration

#### ✅ **GitHub Actions**
```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run tests
        run: mvn test
        # Docker is available by default in GitHub Actions
```

#### ✅ **Local Development**
- Use helper script: `./run-integration-tests.sh`
- Or set environment variable in shell profile
- Document in README for team members

---

## Common Anti-Patterns to Avoid

### Use Case Specifications
1. **Feature flags in preconditions** → Move to runtime validation
2. **Conditional logic in basic flows** → Keep basic flows clean
3. **System unavailable in exception flows** → This is a precondition failure
4. **Missing step references in alternative flows** → Always specify which step triggers the alternative

### BDD Scenarios
1. **Technical implementation details** → Focus on business intent
2. **JSON/HTTP details** → Use business language
3. **Internal system behavior** → Describe outcomes
4. **Repeating existing documentation** → Reference instead of repeat
5. **White box testing language** → Use black box approach

### TDD
1. **Writing implementation before tests** → Always write failing test first
2. **Writing multiple tests before implementing** → One test at a time
3. **Skipping the refactor phase** → Clean up code after tests pass
4. **Writing tests that don't fail first** → Tests should fail for the right reason
5. **Implementing more than needed** → Write minimal code to pass the test
6. **Using JUnit 5** → Use JUnit 4 (required for Cucumber compatibility)
7. **Missing @RunWith annotation** → Always use `@RunWith(SpringRunner.class)` for Spring Boot tests

---

## Quality Checklist

### Use Case Specifications
- [ ] Preconditions contain only system operational requirements
- [ ] Basic flow is clean and linear without conditional logic
- [ ] Alternative flows have clear triggers with step references
- [ ] Exception flows contain only execution exceptions
- [ ] No redundant information with other documentation

### BDD Scenarios
- [ ] Written in problem space (business intent)
- [ ] Use black box language (outcomes, not implementation)
- [ ] Follow DRY principle (reference, don't repeat)
- [ ] Use business language stakeholders understand
- [ ] Focus on what the business gets, not how it's delivered
- [ ] TODO comments removed once step definition methods are implemented

### TDD
- [ ] Write failing test first (Red phase)
- [ ] Implement minimal code to pass (Green phase)
- [ ] Refactor after test passes (Refactor phase)
- [ ] One test at a time workflow
- [ ] Tests are specific, focused, and readable
- [ ] Test names clearly describe expected behavior
- [ ] Use JUnit 4 (not JUnit 5) for Cucumber compatibility
- [ ] Test classes use `@RunWith(SpringRunner.class)` for Spring Boot tests
- [ ] Test methods are `public void` (JUnit 4 syntax)

### Docker/Testcontainers
- [ ] Docker Desktop installed and running
- [ ] DOCKER_HOST environment variable configured (macOS)
- [ ] Integration tests use Testcontainers for real database testing
- [ ] Unit tests use H2 or mocks (no Docker required)
- [ ] Container lifecycle properly managed (@ClassRule for JUnit 4)
- [ ] Test data cleaned up between tests
- [ ] Helper scripts or documentation for running integration tests

---

## References
- Use case specifications should reference: [Use Case Specifications](use-case-specifications.md)
- BDD scenarios should reference: [BDD Scenarios](bdd-scenarios.md)
- Architecture documentation: [Architecture Overview](ARCHITECTURE.md)
