# Process View - Runtime Behavior

## Transaction Creation Flow

![Transaction Creation Flow](diagrams/process-view-creation.mmd)

## Transaction Retrieval Flow

![Transaction Retrieval Flow](diagrams/process-view-retrieval.mmd)

## Feature Flag Disabled Flow

When a feature flag is disabled, the FeatureFlagFilter intercepts the request before it reaches the controller and returns a 403 Forbidden response with a JSON error message.

## Key Runtime Behaviors

### Request Processing Pipeline
1. **Feature Flag Validation**: HTTP filter (FeatureFlagFilter) checks feature configuration via FeatureFlagService
2. **Request Routing**: If enabled, request proceeds to controller; if disabled, returns 403
3. **Metrics Collection**: AOP aspect tracks operation metrics
4. **Input Validation**: Domain model validates business rules
5. **Business Logic**: Application services execute use cases
6. **Data Persistence**: Repository adapters handle database operations
7. **Response Mapping**: Domain objects returned to clients

### Error Handling
- **Global Exception Handler**: Centralized error processing
- **Feature Flag Exceptions**: FeatureFlagFilter handles disabled features at HTTP layer, returns 403 with JSON error
- **Validation Errors**: Input validation with meaningful error messages
- **Database Errors**: JPA exception translation
