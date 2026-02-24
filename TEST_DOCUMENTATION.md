# Battle Service Test Suite Documentation

## Overview

Comprehensive test suite for the Battle Service and Battle Controller in the CodeClash application. The tests ensure that all battle-related functionality works correctly, including battle creation, matchmaking, real-time state management, and WebSocket communication.

## Test Structure

### 1. **BattleServiceTest** - Unit Tests

Location: `src/test/java/com/gourav/CodyWar/Service/BattleServiceTest.java`

**Test Coverage:**

- ✅ Battle Creation (public and private)
- ✅ Join Battle (by room code and ID)
- ✅ Matchmaking (queue, find match, cancel)
- ✅ Player Ready Status
- ✅ Start Battle
- ✅ Leave Battle
- ✅ End Battle (with/without winner)
- ✅ Get Battle Details
- ✅ Get Battle State (Redis)
- ✅ Cancel Battle
- ✅ Error Handling

**Total Test Cases:** 30+

### 2. **BattleControllerTest** - Integration Tests

Location: `src/test/java/com/gourav/CodyWar/Controller/BattleControllerTest.java`

**Test Coverage:**

- ✅ POST /api/battles - Create battle
- ✅ POST /api/battles/join - Join by room code
- ✅ POST /api/battles/{id}/join - Join by ID
- ✅ POST /api/battles/matchmaking - Find match
- ✅ DELETE /api/battles/matchmaking - Cancel matchmaking
- ✅ POST /api/battles/{id}/ready - Set ready status
- ✅ POST /api/battles/{id}/leave - Leave battle
- ✅ GET /api/battles/{id} - Get battle details
- ✅ GET /api/battles/{id}/state - Get battle state
- ✅ GET /api/battles/my-battles - Get user's battles
- ✅ GET /api/battles/active - Get active battles
- ✅ Request Validation
- ✅ Error Handling

**Total Test Cases:** 20+

## Running the Tests

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Class

```bash
# Run BattleService tests only
./mvnw test -Dtest=BattleServiceTest

# Run BattleController tests only
./mvnw test -Dtest=BattleControllerTest
```

### Run Specific Test Method

```bash
./mvnw test -Dtest=BattleServiceTest#createBattle_PublicBattle_Success
```

### Run Tests with Coverage

```bash
./mvnw test jacoco:report
```

Coverage report will be available at: `target/site/jacoco/index.html`

### Run Tests in Continuous Mode (Watch)

```bash
./mvnw test -Dspring-boot.run.arguments=--spring.devtools.restart.enabled=true
```

## Test Scenarios Covered

### 1. Battle Creation Tests

- ✅ Create public battle successfully
- ✅ Create private battle with room code
- ✅ Assign random problem when not specified
- ✅ Validate user exists
- ✅ Prevent user from creating multiple active battles
- ✅ Store battle state in Redis
- ✅ Track user's active battle

### 2. Join Battle Tests

- ✅ Join by valid room code
- ✅ Join by valid battle ID
- ✅ Prevent joining battle in progress
- ✅ Prevent joining full battle
- ✅ Prevent joining same battle twice
- ✅ Prevent joining when already in another battle
- ✅ Handle invalid room code
- ✅ Update Redis state after join
- ✅ Broadcast PLAYER_JOINED event via WebSocket

### 3. Matchmaking Tests

- ✅ Queue user when no match available
- ✅ Match users with similar ratings
- ✅ Create battle when match found
- ✅ Remove from queue after match
- ✅ Cancel matchmaking successfully
- ✅ Handle queue timeout

### 4. Player Ready Tests

- ✅ Set player ready status
- ✅ Update Redis state
- ✅ Broadcast PLAYER_READY event
- ✅ Auto-start when all players ready
- ✅ Prevent ready on non-waiting battles

### 5. Battle Lifecycle Tests

- ✅ Start battle with minimum participants
- ✅ Prevent starting without enough players
- ✅ Update battle status to IN_PROGRESS
- ✅ Set start time
- ✅ Broadcast BATTLE_STARTING and TIMER_START events

### 6. Leave/Cancel Tests

- ✅ Leave battle before start
- ✅ Prevent leaving during battle
- ✅ Remove participant from battle
- ✅ Update Redis state
- ✅ Cancel battle when last player leaves
- ✅ Broadcast PLAYER_LEFT event

### 7. End Battle Tests

- ✅ End with winner
- ✅ End without winner (draw)
- ✅ Update battle status to COMPLETED
- ✅ Update user statistics (battles played/won)
- ✅ Clear active battle tracking
- ✅ Broadcast WINNER_ANNOUNCEMENT event

### 8. Query Tests

- ✅ Get battle by ID
- ✅ Get battle state from Redis
- ✅ Get user's battle history
- ✅ Get active public battles
- ✅ Handle non-existent battles

## Test Data Setup

### Mock Objects

Each test uses mock implementations of:

- `BattleRepository` - Database operations
- `BattleParticipantRepository` - Participant operations
- `ProblemRepository` - Problem retrieval
- `UserRepository` - User operations
- `RedisTemplate` - Redis state management
- `SimpMessagingTemplate` - WebSocket messaging

### Test Users

- **testUser1**: Rating 1500, 10 battles played, 5 won
- **testUser2**: Rating 1550, 8 battles played, 4 won

### Test Data

- **Test Problem**: "Two Sum" (Medium difficulty)
- **Test Battle**: 2 players, 30 minutes duration
- **Test Room Code**: "TEST1234"

## Assertions Used

### Service Tests

- Method invocations verified using `verify()`
- Return values validated using `assertEquals()`, `assertNotNull()`
- Exceptions validated using `assertThrows()`
- Object state verified using `argThat()` matchers

### Controller Tests

- HTTP status codes validated
- JSON response structure verified
- Success/error messages checked
- Data payloads validated
- Request validation tested

## Mock Verification

### Redis Operations

```java
verify(valueOperations).set(eq("battle:state:" + battleId), any(BattleState.class), anyLong(), any());
verify(valueOperations).get("battle:state:" + battleId);
verify(valueOperations).delete("user:battle:" + userId);
```

### WebSocket Broadcasting

```java
verify(messagingTemplate).convertAndSend(
    eq("/topic/battle/" + battleId),
    any(BattleEvent.class)
);
```

### Database Operations

```java
verify(battleRepository).save(argThat(battle ->
    battle.getStatus() == BattleStatus.IN_PROGRESS
));
verify(participantRepository).delete(participant);
```

## Common Test Patterns

### Arrange-Act-Assert (AAA)

```java
@Test
void testMethod_Scenario_ExpectedResult() {
    // Arrange - Set up test data and mocks
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    // Act - Execute the method under test
    Result result = service.methodUnderTest(params);

    // Assert - Verify the results
    assertNotNull(result);
    verify(repository).save(any());
}
```

### Exception Testing

```java
@Test
void testMethod_InvalidInput_ThrowsException() {
    // Arrange
    when(repository.findById(id)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(IllegalArgumentException.class,
        () -> service.methodUnderTest(id));
}
```

## Continuous Integration

### GitHub Actions (Example)

```yaml
name: Run Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: "17"
      - name: Run tests
        run: ./mvnw test
      - name: Generate coverage report
        run: ./mvnw jacoco:report
```

## Troubleshooting

### Redis Connection Errors

If tests fail due to Redis connection:

1. Mock Redis operations are used in unit tests (no real Redis needed)
2. For integration tests, ensure Redis is running or use embedded Redis:

```xml
<dependency>
    <groupId>it.ozimov</groupId>
    <artifactId>embedded-redis</artifactId>
    <version>0.7.3</version>
    <scope>test</scope>
</dependency>
```

### Test Isolation

Each test is isolated using:

- `@BeforeEach` to reset state
- Mock reset between tests
- Independent test data for each test

### Debugging Tests

```bash
# Run with debug output
./mvnw test -X

# Run single test with debug
./mvnw test -Dtest=BattleServiceTest#testName -X
```

## Best Practices Demonstrated

1. ✅ **Descriptive Test Names** - Clear scenario and expected outcome
2. ✅ **Test Isolation** - No dependencies between tests
3. ✅ **Comprehensive Coverage** - Happy path and error cases
4. ✅ **Mock Verification** - Ensure correct interactions
5. ✅ **Readable Assertions** - Clear failure messages
6. ✅ **DRY Principle** - Reusable setup in @BeforeEach
7. ✅ **Fast Tests** - No real database/Redis needed
8. ✅ **Maintainable** - Easy to update when code changes

## Future Enhancements

- [ ] Add performance tests for high concurrency
- [ ] Add WebSocket integration tests
- [ ] Add end-to-end tests with TestContainers
- [ ] Add mutation testing with PIT
- [ ] Add contract tests with Pact
- [ ] Add load tests with JMeter/Gatling

## References

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ Documentation](https://assertj.github.io/doc/)
