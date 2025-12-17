# Feature Flags Implementation and Testing Challenges

## Overview

This document explains how feature flags are implemented in the Accounting Ledger Service, how they use `application.yml` for configuration, and the challenges encountered when attempting to test disabled feature flag scenarios.

## Feature Flag Implementation

### Architecture Components

The feature flag system consists of four main components:

1. **FeatureFlagService** (Interface)
   - Defines the contract for checking feature flag status
   - Methods: `isEnabled(String)` and `requireEnabled(String)`

2. **FeatureFlagServiceImpl** (Implementation)
   - Reads feature flag values from `application.yml` using `@Value` annotations
   - Injects boolean values at bean creation time
   - Throws `FeatureFlagDisabledException` when a disabled feature is accessed

3. **FeatureFlagFilter** (Servlet Filter)
   - Extends `OncePerRequestFilter` to intercept HTTP requests
   - Checks feature flags before requests reach controllers
   - Returns HTTP 403 Forbidden when a feature is disabled
   - Uses `FeatureFlagProperties` (via `@ConfigurationProperties`) for endpoint mapping

4. **FeatureFlagProperties** (Configuration Properties)
   - Uses `@ConfigurationProperties(prefix = "feature")` to bind YAML configuration
   - Provides endpoint-to-feature-flag mapping from `application.yml`
   - Replaces previous SpEL-based approach for cleaner configuration binding

4. **FeatureFlagDisabledException** (Exception)
   - Custom runtime exception thrown when a disabled feature is accessed
   - Handled by `GlobalExceptionHandler` to return proper HTTP responses

### Configuration in application.yml

Feature flags are configured in `src/main/resources/application.yml`:

```yaml
feature:
  create-transaction:
    enabled: true
  get-all-transactions:
    enabled: true
  endpoints:
    "POST /transactions": "create-transaction"
    "GET /transactions": "get-all-transactions"
```

**Configuration Structure:**
- `feature.<feature-name>.enabled`: Boolean flag controlling feature availability
- `feature.endpoints`: Map of HTTP method + path to feature name
  - Format: `"[METHOD /path]": "feature-name"` (bracket notation preserves spaces in keys)
  - Used by `FeatureFlagFilter` to determine which feature to check for each endpoint
  - Bound to `FeatureFlagProperties` via `@ConfigurationProperties`

### How It Works

#### 1. Bean Creation and Property Injection

When Spring Boot starts, `FeatureFlagServiceImpl` is created as a `@Service` bean:

```java
@Service
public class FeatureFlagServiceImpl implements FeatureFlagService {
    @Value("${feature.create-transaction.enabled:true}")
    private boolean createTransactionEnabled;
    
    @Value("${feature.get-all-transactions.enabled:true}")
    private boolean getAllTransactionsEnabled;
    // ...
}
```

**Key Point:** The `@Value` annotation injects property values **at bean creation time**. The values are read from:
1. `application.yml` (or `application-test.yml` for tests)
2. Environment variables
3. System properties
4. Default values (the `:true` part provides a default)

Once injected, these boolean fields are **final for the bean's lifetime** - they cannot be changed without recreating the bean.

#### 2. Request Filtering

`FeatureFlagFilter` intercepts all HTTP requests:

```java
public FeatureFlagFilter(FeatureFlagService featureFlagService, 
                         ObjectMapper objectMapper,
                         FeatureFlagProperties featureFlagProperties) {
    this.featureFlagService = featureFlagService;
    this.objectMapper = objectMapper;
    this.endpointFeatureMap = featureFlagProperties.getEndpoints();
}

@Override
protected void doFilterInternal(HttpServletRequest request, 
                               HttpServletResponse response, 
                               FilterChain filterChain) {
    String method = request.getMethod();
    String path = request.getRequestURI();
    String endpointKey = method + " " + path;  // e.g., "GET /transactions"
    
    // Look up feature name from endpoint mapping (loaded via @ConfigurationProperties)
    String featureName = endpointFeatureMap.get(endpointKey);
    
    if (featureName != null) {
        try {
            featureFlagService.requireEnabled(featureName);
        } catch (FeatureFlagDisabledException e) {
            // Return 403 Forbidden
            handleFeatureDisabled(response, e);
            return;
        }
    }
    
    filterChain.doFilter(request, response);
}
```

**Key Point:** The endpoint mapping is loaded via `FeatureFlagProperties` using `@ConfigurationProperties`, which provides cleaner configuration binding than the previous SpEL-based approach.

**Flow:**
1. Request arrives (e.g., `GET /transactions`)
2. Filter constructs endpoint key: `"GET /transactions"`
3. Filter looks up feature name in `endpointFeatureMap`: `"get-all-transactions"`
4. Filter calls `featureFlagService.requireEnabled("get-all-transactions")`
5. If disabled, filter returns HTTP 403 with JSON error
6. If enabled, request continues to controller

#### 3. Exception Handling

If a feature is disabled, `FeatureFlagServiceImpl.requireEnabled()` throws:

```java
throw new FeatureFlagDisabledException("Feature 'get-all-transactions' is disabled");
```

The `GlobalExceptionHandler` catches this and returns:

```json
{
  "error": "Feature is disabled"
}
```

With HTTP status 403 Forbidden.

## Testing Challenges: Attempting to Test Disabled Feature Flags

### Problem Statement

Testing feature flag disabled scenarios requires setting `feature.get-all-transactions.enabled=false` in tests. However, this proved difficult because:

1. **Bean Creation Timing**: `@Value` annotations inject properties when beans are created
2. **YAML Precedence**: `application-test.yml` may load before `@TestPropertySource`
3. **Context Sharing**: Cucumber Spring shares Spring contexts across step definition classes
4. **Property Override Limitations**: Spring Boot's property resolution order can prevent overrides

### Approaches Attempted and Why They Failed

#### Approach 1: @TestPropertySource on Step Definition Class

**Attempt:**
```java
@TestPropertySource(properties = {
    "feature.get-all-transactions.enabled=false"
})
public class GetTransactionsFeatureDisabledStepDefinitions {
    // ...
}
```

**Why It Failed:**
- Cucumber Spring shares the Spring context across all step definition classes
- The context is created once with `@CucumberContextConfiguration`
- Different `@TestPropertySource` annotations on different step definition classes don't create separate contexts
- The first context created (with enabled flags) is reused for all scenarios

**Result:** Feature flag remained enabled (returned 200 instead of 403)

---

#### Approach 2: @TestPropertySource with override=true

**Attempt:**
```java
@TestPropertySource(properties = {
    "feature.get-all-transactions.enabled=false"
}, override = true)
public class GetTransactionsFeatureDisabledStepDefinitions {
    // ...
}
```

**Why It Failed:**
- `override` parameter doesn't exist in Spring Boot 3.2.8's `@TestPropertySource`
- Compilation error: `override()` method not found

**Result:** Compilation failure

---

#### Approach 3: @ActiveProfiles with Separate YAML File

**Attempt:**
Created `application-test-feature-disabled.yml`:
```yaml
feature:
  get-all-transactions:
    enabled: false
```

And used:
```java
@ActiveProfiles("test-feature-disabled")
public class GetTransactionsFeatureDisabledStepDefinitions {
    // ...
}
```

**Why It Failed:**
- Cucumber Spring's `@CucumberContextConfiguration` already specifies `@ActiveProfiles("test")`
- Profile conflicts or context sharing issues
- The separate profile didn't create a new context

**Result:** Feature flag remained enabled

---

#### Approach 4: @SpringBootTest(properties=...)

**Attempt:**
```java
@SpringBootTest(properties = {
    "feature.get-all-transactions.enabled=false"
})
public class GetTransactionsFeatureDisabledTest {
    // ...
}
```

**Why It Failed:**
- Properties in `@SpringBootTest` are applied, but `application-test.yml` may still load
- The `@Value` injection in `FeatureFlagServiceImpl` happens at bean creation
- YAML values can have higher precedence than programmatic properties in some cases
- The boolean field was already set to `true` when the bean was created

**Result:** Feature flag remained enabled (returned 200 instead of 403)

---

#### Approach 5: @TestPropertySource with Empty Locations

**Attempt:**
```java
@TestPropertySource(properties = {
    "feature.get-all-transactions.enabled=false"
}, locations = {})
public class GetTransactionsFeatureDisabledTest {
    // ...
}
```

**Why It Failed:**
- Empty `locations` doesn't prevent YAML files from loading
- Spring Boot still loads `application-test.yml` automatically
- The property override didn't take effect before bean creation

**Result:** Feature flag remained enabled

---

#### Approach 6: @ActiveProfiles with inheritProfiles=false

**Attempt:**
```java
@ActiveProfiles(profiles = {}, inheritProfiles = false)
@TestPropertySource(properties = {
    "feature.get-all-transactions.enabled=false"
})
public class GetTransactionsFeatureDisabledTest {
    // ...
}
```

**Why It Failed:**
- Even with no profiles, Spring Boot still loads default configuration files
- The `@Value` injection timing issue persists
- Bean creation happens before property resolution completes

**Result:** Feature flag remained enabled

---

#### Approach 7: @MockBean for FeatureFlagService

**Attempt:**
```java
@MockBean
private FeatureFlagService featureFlagService;

@Test
public void shouldReturnForbiddenWhenFeatureFlagIsDisabled() {
    doThrow(new FeatureFlagDisabledException("..."))
        .when(featureFlagService)
        .requireEnabled("get-all-transactions");
    
    mockMvc.perform(get("/transactions"))
        .andExpect(status().isForbidden());
}
```

**Why It Failed:**
- `@MockBean` creates a mock, but `FeatureFlagFilter` was already created with the real `FeatureFlagServiceImpl`
- The filter holds a reference to the original service instance
- MockMvc may not properly replace the service in the filter's dependency
- The endpoint mapping (`feature.endpoints`) may not have been loaded correctly

**Result:** Mock was never called, feature flag check didn't happen (returned 200)

---

#### Approach 8: Combined @SpringBootTest and @TestPropertySource

**Attempt:**
```java
@SpringBootTest(properties = {
    "feature.get-all-transactions.enabled=false"
})
@TestPropertySource(properties = {
    "feature.get-all-transactions.enabled=false",
    "feature.endpoints.\"GET /transactions\"=get-all-transactions"
})
public class GetTransactionsFeatureDisabledTest {
    // ...
}
```

**Why It Failed:**
- Property resolution order issues
- The `@Value` fields in `FeatureFlagServiceImpl` are set during bean creation
- YAML files may be processed before property overrides take effect
- The boolean field value is "baked in" when the bean is instantiated

**Result:** Feature flag remained enabled

---

## Root Cause Analysis

### Why Property Overrides Don't Work

1. **Bean Creation Timing**
   - `FeatureFlagServiceImpl` is a `@Service` bean created during application context startup
   - `@Value("${feature.get-all-transactions.enabled:true}")` injects the value **when the bean is created**
   - This happens **before** test methods run
   - Once set, the private boolean field cannot be changed

2. **Property Resolution Order**
   Spring Boot resolves properties in this order (highest to lowest precedence):
   - Command line arguments
   - `@TestPropertySource` properties
   - `@SpringBootTest(properties=...)`
   - `application-{profile}.yml`
   - `application.yml`
   - Default values in `@Value` annotations
   
   However, **YAML files are loaded during context initialization**, and the timing of when `@Value` injection occurs can cause YAML values to "win" even when `@TestPropertySource` should override them.

3. **Context Sharing in Cucumber**
   - Cucumber Spring uses `@CucumberContextConfiguration` to define a shared Spring context
   - All step definition classes share this context
   - Different `@TestPropertySource` annotations don't create separate contexts
   - The first context created is reused for all scenarios

4. **Filter Dependency Injection**
   - `FeatureFlagFilter` receives `FeatureFlagService` via constructor injection
   - This happens during context initialization
   - `@MockBean` creates a new mock, but the filter already has a reference to the real service
   - The filter's dependency is "frozen" at creation time

## Potential Solutions (Not Implemented)

### Solution 1: Use @DirtiesContext
Force Spring to recreate the context for each test class:
```java
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {"feature.get-all-transactions.enabled=false"})
public class GetTransactionsFeatureDisabledTest {
    // ...
}
```
**Trade-off:** Slower test execution due to context recreation

### Solution 2: Test FeatureFlagFilter Directly
Create a unit test for `FeatureFlagFilter` with a mocked `FeatureFlagService`:
```java
@ExtendWith(MockitoExtension.class)
class FeatureFlagFilterTest {
    @Mock
    private FeatureFlagService featureFlagService;
    
    private FeatureFlagFilter filter;
    
    @Test
    void shouldReturn403WhenFeatureDisabled() {
        // Test filter behavior directly
    }
}
```
**Trade-off:** Tests the filter in isolation, not the full integration

### Solution 3: Use @TestConfiguration
Provide a test-specific `FeatureFlagService` implementation:
```java
@TestConfiguration
static class TestConfig {
    @Bean
    @Primary
    public FeatureFlagService testFeatureFlagService() {
        return new FeatureFlagService() {
            public boolean isEnabled(String name) {
                return !"get-all-transactions".equals(name);
            }
            // ...
        };
    }
}
```
**Trade-off:** More complex setup, but allows per-test configuration

### Solution 4: Environment Variables
Set environment variables before running tests:
```bash
export FEATURE_GET_ALL_TRANSACTIONS_ENABLED=false
mvn test
```
**Trade-off:** Not suitable for automated test suites, requires external setup

### Solution 5: Refactor to Use ConfigurationProperties ✅ **IMPLEMENTED**

**Solution:** Refactored to use `@ConfigurationProperties` for endpoint mapping:
```java
@Component
@ConfigurationProperties(prefix = "feature")
public class FeatureFlagProperties {
    private Map<String, String> endpoints = new HashMap<>();
    // ...
}
```

**Implementation:**
- Created `FeatureFlagProperties` class with `@ConfigurationProperties(prefix = "feature")`
- Updated `FeatureFlagFilter` to inject `FeatureFlagProperties` instead of using SpEL
- Used bracket notation `"[GET /transactions]"` in YAML to preserve keys with spaces
- Created separate Cucumber test runners for enabled/disabled scenarios
- Used profile-specific YAML files (`application-ff-disabled.yml`) to override feature flags

**Result:** ✅ **Successfully implemented** - Disabled feature flag tests now work correctly

## Current State

**✅ Feature flag testing is now fully implemented and working.**

The feature flag system works correctly in both production and testing:
- **Production:** Feature flags can be disabled via `application.yml`
- **Testing:** Disabled feature flags are tested using:
  - Separate Cucumber test runners (`CucumberTestRunnerEnabled`, `CucumberTestRunnerFeatureDisabled`)
  - Profile-specific YAML files (`application-ff-disabled.yml`)
  - `@ConfigurationProperties` for clean configuration binding
  - Separate Spring contexts for enabled/disabled scenarios

**Testing Infrastructure:**
- `CucumberTestRunnerEnabled`: Runs scenarios with feature flags enabled (excludes `@ff_disabled` tag)
- `CucumberTestRunnerFeatureDisabled`: Runs scenarios with feature flags disabled (only `@ff_disabled` tag)
- `application-ff-disabled.yml`: Profile configuration that disables specific feature flags
- Separate `@CucumberContextConfiguration` classes in isolated packages to prevent context conflicts

**Test Coverage:**
- ✅ Unit tests of `FeatureFlagService` logic
- ✅ Integration tests with enabled flags (verifying the filter allows requests)
- ✅ BDD tests with disabled flags (verifying the filter returns 403 Forbidden)
- ✅ Endpoint mapping correctly loaded via `@ConfigurationProperties`
