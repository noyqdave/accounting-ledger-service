# Idempotency Implementation Plan

## Overview
This plan implements idempotency for transaction creation in small, vertically integrated increments. Each increment is either a **working solution** (delivers end-to-end value) or **foundational** (enables future solutions).

---

## Definition of Done

All increments must meet these criteria before being considered complete:

- [ ] All acceptance criteria are met and verified
- [ ] Code is written following project coding standards
- [ ] Unit tests are written and passing
- [ ] Integration tests are written and passing (if applicable)
- [ ] BDD scenarios are written and passing (if applicable)
- [ ] Code is reviewed and approved
- [ ] No linter errors or warnings
- [ ] API documentation is updated (Swagger/OpenAPI)
- [ ] Feature works end-to-end (vertically integrated)
- [ ] Existing tests continue to pass (no regressions)
- [ ] Changes are committed with clear commit messages

---

## Increment 1: Basic Idempotency with In-Memory Storage

**Type:** ✅ **Working Solution** (limited scope: in-memory only, doesn't survive restarts)

**Intent:** Enable clients to safely retry transaction creation requests without creating duplicates. This delivers immediate value for retry scenarios within a single application session.

**What it delivers:**
- Clients can include `Idempotency-Key` header in POST `/transactions` requests
- Duplicate requests (same key + same body) return the same response without creating duplicate transactions
- Request conflicts (same key + different body) return 409 Conflict error
- Full end-to-end functionality working (API → service → storage → response)

**Acceptance Criteria:**
- ✅ Header `Idempotency-Key` is accepted and documented in API
- ✅ First request with idempotency key creates transaction normally
- ✅ Second request with same key and same body returns cached response (identical transaction ID)
- ✅ Second request with same key but different body returns 409 Conflict
- ✅ No duplicate transactions created in database
- ✅ Idempotency works end-to-end via HTTP API

**Technical Implementation:**
- Create `IdempotencyService` interface and in-memory implementation (ConcurrentHashMap)
- Create `IdempotencyFilter` to intercept requests with idempotency key
- Hash request body (amount, description, type) for request matching
- Store response (status code + body) in memory with idempotency key
- Return cached response for duplicate requests
- Return 409 Conflict for request conflicts

**Limitations:**
- Idempotency keys only persist in memory (lost on restart)
- Not suitable for production deployments or long-running scenarios

---

## Increment 2: Persistent Idempotency with Database Storage

**Type:** ✅ **Working Solution** (production-ready for idempotency feature)

**Intent:** Make idempotency work across application restarts and deployments, enabling production use where clients may retry requests after service restarts.

**What it delivers:**
- All functionality from Increment 1
- Idempotency keys persist in database
- Idempotent behavior works after application restart
- Production-ready idempotency feature

**Acceptance Criteria:**
- ✅ All acceptance criteria from Increment 1
- ✅ Idempotency keys stored in database table
- ✅ Keys persist after application restart
- ✅ Same idempotency key returns cached response after restart
- ✅ In-memory implementation replaced with database storage

**Technical Implementation:**
- Create `IdempotencyKeyEntity` with fields:
  - `idempotencyKey` (primary key, String)
  - `requestHash` (String)
  - `responseBody` (TEXT/JSON)
  - `statusCode` (Integer)
  - `createdAt` (Timestamp)
  - `expiresAt` (Timestamp)
- Create `IdempotencyKeyRepository` (JPA repository)
- Replace in-memory storage with database repository
- Create database migration/DDL script

**Replaces:** Increment 1's in-memory storage

---

## Increment 3: Key Expiration and Validation

**Type:** ✅ **Working Solution** (enhances production solution)

**Intent:** Prevent unbounded database growth from idempotency keys and ensure clients use valid key formats.

**What it delivers:**
- Idempotency keys expire after configured TTL (default: 24 hours)
- Expired keys are ignored (treated as new requests)
- Invalid key formats are rejected with clear error messages
- Production-safe idempotency with resource management

**Acceptance Criteria:**
- ✅ New idempotency keys have expiration time (configurable, default 24 hours)
- ✅ Expired keys are not used for idempotency checks
- ✅ Requests with expired keys create new transactions
- ✅ Idempotency key must be valid UUID format
- ✅ Invalid format returns 400 Bad Request with clear error
- ✅ TTL configurable via `application.yml`

**Technical Implementation:**
- Set `expiresAt` when storing keys (createdAt + TTL)
- Check expiration during key lookup
- Validate UUID format before processing
- Add configuration property: `idempotency.ttl-hours`
- Update API documentation with format requirements

**Enhances:** Increment 2

---

## Increment 4: Automatic Cleanup of Expired Keys

**Type:** ✅ **Working Solution** (operational enhancement)

**Intent:** Automatically maintain database by removing expired idempotency keys without manual intervention.

**What it delivers:**
- Scheduled task automatically deletes expired keys
- Database size remains bounded without manual maintenance
- Cleanup runs without impacting normal request processing

**Acceptance Criteria:**
- ✅ Scheduled task runs periodically (default: every hour)
- ✅ Expired keys are deleted automatically
- ✅ Task execution is logged
- ✅ Active (non-expired) keys are preserved
- ✅ Cleanup doesn't block or slow down normal requests

**Technical Implementation:**
- Use Spring `@Scheduled` annotation
- Create cleanup method in repository/service
- Configure schedule (e.g., `@Scheduled(fixedRate = 3600000)`)
- Add logging for cleanup operations

**Enhances:** Increment 3

---

## Increment 5: Idempotency Metrics and Monitoring

**Type:** ✅ **Working Solution** (observability enhancement)

**Intent:** Enable monitoring of idempotency usage and effectiveness for operational visibility.

**What it delivers:**
- Metrics track idempotency request volume
- Metrics track cache hit rate (retry effectiveness)
- Metrics track conflict rate (client errors)
- Operational visibility into idempotency feature usage

**Acceptance Criteria:**
- ✅ Metric: `idempotency.requests.total` (counter)
- ✅ Metric: `idempotency.cache.hits` (counter)
- ✅ Metric: `idempotency.conflicts` (counter)
- ✅ Metrics exposed via actuator endpoint

**Technical Implementation:**
- Add Micrometer metrics to `IdempotencyService`
- Use `@TrackMetric` or similar pattern (consistent with existing codebase)
- Increment counters for requests, cache hits, and conflicts

**Enhances:** Increment 4

---

## Implementation Order

### Recommended Sequence (Working Solutions First)

1. **Increment 1** - Get basic idempotency working end-to-end (in-memory)
   - Delivers immediate value
   - Validates approach
   - Enables testing

2. **Increment 2** - Make it production-ready (database persistence)
   - Replaces in-memory with database
   - Production-ready solution

3. **Increment 3** - Add safety and resource management (expiration + validation)
   - Prevents unbounded growth
   - Validates input

4. **Increment 4** - Automated maintenance (scheduled cleanup)
   - Operational enhancement

5. **Increment 5** - Observability (metrics)
   - Monitoring and insights

### Alternative: Skip In-Memory Step

If database implementation is straightforward, you could combine Increments 1 and 2:
- Start directly with database storage
- Skip in-memory implementation
- Faster path to production-ready solution

**Trade-off:** Larger first increment, but one less step to production

---

## Key Design Decisions

- **Storage Approach:** Start in-memory (simple), then move to database (production)
- **Key Format:** UUID (standard, well-supported)
- **Expiration:** 24 hours default (aligns with typical retry windows)
- **Conflict Handling:** 409 Conflict (standard HTTP status for resource conflicts)
- **Filter vs Interceptor:** HTTP Filter (consistent with existing `FeatureFlagFilter` pattern)

---

## Notes

- Each increment is vertically integrated (API → service → storage → response)
- Each increment has BDD tests demonstrating the behavior
- Increment 1 can be used for development/testing, but Increment 2 is needed for production
- Consider feature flag for idempotency if you want gradual rollout (can be added later)
