# Java 5-Pin Bowling TDD Project

A Test-Driven Development (TDD) project for implementing 5-pin bowling scoring.

## Project Structure

```
src/
├── main/java/com/example/bowling/     # Production code
└── test/java/com/example/bowling/     # Test code
```

## TDD Approach

We'll follow the Red-Green-Refactor cycle:

1. **Red**: Write a failing test
2. **Green**: Write minimal code to make the test pass
3. **Refactor**: Improve the code while keeping tests green

## 5-Pin Bowling Rules

- Each pin has different values: 2, 3, 5, 3, 2 (left to right)
- Each frame has up to 3 rolls
- Strike: All 5 pins down in first roll (15 points + next 2 rolls)
- Spare: All 5 pins down in 2 rolls (15 points + next 1 roll)
- Perfect game: 12 strikes = 450 points

## Getting Started

```bash
# Compile the project
mvn compile

# Run tests
mvn test

# Run specific test
mvn test -Dtest=BowlingGameTest
```

## Development Workflow

1. Write a failing test
2. Run the test (should fail - Red)
3. Write minimal code to pass
4. Run the test (should pass - Green)
5. Refactor if needed
6. Repeat