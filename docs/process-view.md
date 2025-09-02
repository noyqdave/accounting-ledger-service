# Process View - Runtime Behavior

## Transaction Creation Flow

![Transaction Creation Flow](diagrams/process-view-creation.mmd)

## Transaction Retrieval Flow

![Transaction Retrieval Flow](diagrams/process-view-retrieval.mmd)

## Feature Flag Disabled Flow

![Feature Flag Disabled Flow](diagrams/process-view-feature-disabled.mmd)

## Key Runtime Behaviors

### Request Processing Pipeline
1. **Feature Flag Validation**: AOP aspect checks feature configuration
2. **Metrics Collection**: AOP aspect tracks operation metrics
3. **Input Validation**: Domain model validates business rules
4. **Business Logic**: Application services execute use cases
5. **Data Persistence**: Repository adapters handle database operations
6. **Response Mapping**: Domain objects returned to clients

### Error Handling
- **Global Exception Handler**: Centralized error processing
- **Feature Flag Exceptions**: Graceful degradation for disabled features
- **Validation Errors**: Input validation with meaningful error messages
- **Database Errors**: JPA exception translation
