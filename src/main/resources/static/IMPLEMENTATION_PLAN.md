# CodeClash — Comprehensive Step-by-Step Implementation Plan

> **Document Purpose:** This is a detailed, phased implementation roadmap for the CodeClash competitive coding platform. Each step is written in precise, imperative language so that any developer (or LLM assistant) can execute it reliably without ambiguity.

---

## Table of Contents

1. [Current State Audit](#1-current-state-audit)
2. [Phase 0 — Prerequisites & Environment](#2-phase-0--prerequisites--environment)
3. [Phase 1 — Test Case Management (Backend)](#3-phase-1--test-case-management-backend)
4. [Phase 2 — Submission Data Layer (Backend)](#4-phase-2--submission-data-layer-backend)
5. [Phase 3 — Docker Code Execution Sandbox](#5-phase-3--docker-code-execution-sandbox)
6. [Phase 4 — Code Execution Service (Backend)](#6-phase-4--code-execution-service-backend)
7. [Phase 5 — Submission Service (Backend)](#7-phase-5--submission-service-backend)
8. [Phase 6 — Submission Controller & API (Backend)](#8-phase-6--submission-controller--api-backend)
9. [Phase 7 — Battle ↔ Submission Integration (Backend)](#9-phase-7--battle--submission-integration-backend)
10. [Phase 8 — Rating / ELO System (Backend)](#10-phase-8--rating--elo-system-backend)
11. [Phase 9 — Backend Hardening & Tests](#11-phase-9--backend-hardening--tests)
12. [Phase 10 — Frontend Scaffolding (React)](#12-phase-10--frontend-scaffolding-react)
13. [Phase 11 — Frontend Auth Pages](#13-phase-11--frontend-auth-pages)
14. [Phase 12 — Frontend Dashboard & Problem Pages](#14-phase-12--frontend-dashboard--problem-pages)
15. [Phase 13 — Frontend Battle Lobby & Room](#15-phase-13--frontend-battle-lobby--room)
16. [Phase 14 — Frontend Code Editor & Submission](#16-phase-14--frontend-code-editor--submission)
17. [Phase 15 — Frontend Real-Time WebSocket Integration](#17-phase-15--frontend-real-time-websocket-integration)
18. [Phase 16 — End-to-End Testing & Polish](#18-phase-16--end-to-end-testing--polish)
19. [Phase 17 — Deployment](#19-phase-17--deployment)
20. [Appendix A — File Inventory (What Exists)](#appendix-a--file-inventory-what-exists)
21. [Appendix B — API Contract Summary](#appendix-b--api-contract-summary)

---

## 1. Current State Audit

### What Is Already Built ✅

| Layer          | Component                                                                           | Location                               | Status                                                                                                                                                                                                                                                                   |
| -------------- | ----------------------------------------------------------------------------------- | -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Entity**     | `User`                                                                              | `Domain/Entity/User.java`              | Complete — UUID, username, email, passwordHash, ratingScore, battlesPlayed, battlesWon, languagesUsed, role, status, timestamps                                                                                                                                          |
| **Entity**     | `Battle`                                                                            | `Domain/Entity/Battle.java`            | Complete — roomCode, problem, status, maxParticipants, durationSeconds, isPrivate, participants, winner, submissions, timestamps                                                                                                                                         |
| **Entity**     | `BattleParticipant`                                                                 | `Domain/Entity/BattleParticipant.java` | Complete — battle, user, isReady, hasSubmitted, score, joinedAt. Unique constraint on (battle_id, user_id)                                                                                                                                                               |
| **Entity**     | `Problem`                                                                           | `Domain/Entity/Problem.java`           | Complete — title, description, constraints, exampleInput, exampleOutput, difficulty, timeLimitSeconds, memoryLimitMb, testCases, battles                                                                                                                                 |
| **Entity**     | `TestCase`                                                                          | `Domain/Entity/TestCase.java`          | Complete — problem (ManyToOne), input (TEXT), expectedOutput (TEXT), isHidden (default true), orderIndex                                                                                                                                                                 |
| **Entity**     | `Submission`                                                                        | `Domain/Entity/Submission.java`        | Complete — battle, user, language, code (TEXT), status, executionTimeMs, memoryUsedKb, testCasesPassed, totalTestCases, errorMessage, timestamps                                                                                                                         |
| **Enum**       | `BattleStatus`                                                                      | `Domain/Entity/BattleStatus.java`      | WAITING, READY, IN_PROGRESS, COMPLETED, CANCELLED                                                                                                                                                                                                                        |
| **Enum**       | `SubmissionStatus`                                                                  | `Domain/Entity/SubmissionStatus.java`  | PENDING, RUNNING, ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED, MEMORY_LIMIT_EXCEEDED, RUNTIME_ERROR, COMPILATION_ERROR                                                                                                                                                   |
| **Enum**       | `Language`                                                                          | `Domain/Entity/Language.java`          | JAVA, PYTHON, JAVASCRIPT, TYPESCRIPT, CPP, C, GO, RUST, KOTLIN, RUBY, CSHARP                                                                                                                                                                                             |
| **Enum**       | `Difficulty`                                                                        | `Domain/Entity/Difficulty.java`        | EASY, MEDIUM, HARD                                                                                                                                                                                                                                                       |
| **Enum**       | `Role`, `Status`                                                                    | `Domain/Entity/`                       | PLAYER/ADMIN, ACTIVE/etc.                                                                                                                                                                                                                                                |
| **DTO**        | `ApiResponse<T>`                                                                    | `Domain/Dto/ApiResponse.java`          | Generic wrapper — success, message, data, error, timestamp                                                                                                                                                                                                               |
| **DTO**        | `AuthResponse`, `LoginRequest`, `RegisterRequest`, `RefreshTokenRequest`, `UserDto` | `Domain/Dto/`                          | Complete                                                                                                                                                                                                                                                                 |
| **DTO**        | `ProblemRequestDto`, `ProblemResponseDto`                                           | `Domain/Dto/`                          | Complete — includes exampleInput/Output, timeLimitSeconds, memoryLimitMb, Difficulty                                                                                                                                                                                     |
| **DTO**        | `CreateBattleRequest`, `JoinBattleRequest`                                          | `Domain/Dto/`                          | Complete                                                                                                                                                                                                                                                                 |
| **DTO**        | `BattleResponseDto` (with `ParticipantDto`)                                         | `Domain/Dto/`                          | Complete                                                                                                                                                                                                                                                                 |
| **DTO**        | `BattleState` (with `ParticipantState`)                                             | `Domain/Dto/`                          | Complete — Redis-serializable, Serializable, isFull(), allParticipantsReady(), getRemainingTimeSeconds()                                                                                                                                                                 |
| **DTO**        | `BattleEvent` (with `EventType` enum)                                               | `Domain/Dto/`                          | Complete — 14 event types                                                                                                                                                                                                                                                |
| **DTO**        | `MatchmakingEntry`                                                                  | `Domain/Dto/`                          | Complete                                                                                                                                                                                                                                                                 |
| **Repo**       | `UserRepository`                                                                    | `Repository/`                          | findByEmail, existsByEmail, existsByUsername                                                                                                                                                                                                                             |
| **Repo**       | `ProblemRepository`                                                                 | `Repository/`                          | Standard JPA                                                                                                                                                                                                                                                             |
| **Repo**       | `BattleRepository`                                                                  | `Repository/`                          | findByRoomCode, existsByRoomCode, findPublicBattlesByStatus, findByStatusIn, findByUserIdAndStatusIn, findByUserIdOrderByCreatedAtDesc, countByUserIdAndStatus, findAvailablePublicBattles                                                                               |
| **Repo**       | `BattleParticipantRepository`                                                       | `Repository/`                          | findByBattleIdAndUserId, findByBattleId, existsByBattleIdAndUserId                                                                                                                                                                                                       |
| **Service**    | `AuthService`                                                                       | `Service/AuthService.java`             | register, login, refreshToken, getCurrentUser                                                                                                                                                                                                                            |
| **Service**    | `ProblemService`                                                                    | `Service/ProblemService.java`          | createProblem, getProblemById, getAllProblems, updateProblem, deleteProblem                                                                                                                                                                                              |
| **Service**    | `BattleService`                                                                     | `Service/BattleService.java`           | Full implementation — create, join (by room code and by ID), matchmaking (queue/dequeue/match), setPlayerReady, startBattle, leaveBattle, cancelBattle, endBattle, getBattle, getBattleState, getUserBattles, getActiveBattles, updateBattleTimers (@Scheduled every 1s) |
| **Service**    | `BattleEventPublisher`                                                              | `Service/BattleEventPublisher.java`    | Redis Pub/Sub for cross-instance WebSocket event broadcasting                                                                                                                                                                                                            |
| **Controller** | `AuthController`                                                                    | `Controller/`                          | POST /api/auth/register, /login, /refresh; GET /api/auth/me                                                                                                                                                                                                              |
| **Controller** | `ProblemController`                                                                 | `Controller/`                          | CRUD at /api/problems; admin-only for create/update/delete                                                                                                                                                                                                               |
| **Controller** | `BattleController`                                                                  | `Controller/`                          | POST /api/battles, /join, /{id}/join, /matchmaking, /{id}/ready, /{id}/leave; DELETE /matchmaking; GET /{id}, /{id}/state, /my-battles, /active                                                                                                                          |
| **Controller** | `BattleWebSocketController`                                                         | `Controller/`                          | STOMP handlers: /battle/{id}/join, /ready, /leave, /heartbeat, /state                                                                                                                                                                                                    |
| **Security**   | `JwtUtil`                                                                           | `Security/`                            | extractEmail, generateToken, generateRefreshToken, isTokenValid (HS256)                                                                                                                                                                                                  |
| **Security**   | `JwtAuthenticationFilter`                                                           | `Security/`                            | OncePerRequestFilter — reads Bearer token from Authorization header                                                                                                                                                                                                      |
| **Security**   | `CustomUserDetails`, `CustomUserDetailsService`                                     | `Security/`                            | Spring Security UserDetails adapter                                                                                                                                                                                                                                      |
| **Config**     | `SecurityConfig`                                                                    | `Configuration/`                       | JWT stateless, /api/auth/** and /ws/** permitted, /api/admin/\*\* requires ADMIN                                                                                                                                                                                         |
| **Config**     | `RedisConfig`                                                                       | `Configuration/`                       | RedisTemplate with JSON serialization, RedisMessageListenerContainer                                                                                                                                                                                                     |
| **Config**     | `WebSocketConfig`                                                                   | `Configuration/`                       | STOMP endpoint /ws/battle, broker prefixes /topic and /queue, app prefix /app, JWT auth interceptor                                                                                                                                                                      |
| **Config**     | `GlobalExceptionHandler`                                                            | `Configuration/`                       | Handles MethodArgumentNotValidException, BadCredentialsException, UsernameNotFoundException, IllegalArgumentException, generic Exception                                                                                                                                 |
| **App**        | `CodyWarApplication`                                                                | Root                                   | @SpringBootApplication, @EnableScheduling                                                                                                                                                                                                                                |
| **Tests**      | `BattleServiceTest`                                                                 | `test/.../Service/`                    | 31 unit tests (may have residual issues)                                                                                                                                                                                                                                 |
| **Tests**      | `BattleControllerTest`                                                              | `test/.../Controller/`                 | 18 integration tests with MockMvc                                                                                                                                                                                                                                        |

### What Is NOT Built ❌

| #   | Component                   | Type                | Why It's Needed                                                                                              |
| --- | --------------------------- | ------------------- | ------------------------------------------------------------------------------------------------------------ |
| 1   | `TestCaseRepository`        | Repository          | Required to fetch test cases for a problem during code judging                                               |
| 2   | `SubmissionRepository`      | Repository          | Required to persist and query submission records                                                             |
| 3   | `SubmitCodeRequest` DTO     | DTO                 | Frontend needs a request contract to submit code                                                             |
| 4   | `SubmissionResponseDto` DTO | DTO                 | Backend needs a response contract for submission results                                                     |
| 5   | `CodeExecutionService`      | Service             | The core sandbox — writes code to temp file, launches Docker container, captures output, enforces limits     |
| 6   | `SubmissionService`         | Service             | Orchestrates the full flow: save submission → execute code → judge results → update battle state → broadcast |
| 7   | `SubmissionController`      | Controller          | REST endpoint for code submission during battles                                                             |
| 8   | Docker execution images     | Infrastructure      | Pre-built Docker images for each supported language (Java, Python, JS, etc.)                                 |
| 9   | Docker execution scripts    | Infrastructure      | Shell scripts inside containers: compile, run, timeout, capture output                                       |
| 10  | Rating/ELO system           | Service logic       | Adjust player ratingScore after battles                                                                      |
| 11  | Test case management        | Service enhancement | ProblemService should support creating/updating problems with embedded test cases                            |
| 12  | CORS configuration          | Config              | Required for frontend on a different port to call the API                                                    |
| 13  | Frontend application        | Full app            | React + Monaco Editor + Tailwind — login, dashboard, battle room, editor, results                            |
| 14  | End-to-end tests            | Tests               | Full flow testing from auth → battle → submission → result                                                   |
| 15  | Deployment configuration    | DevOps              | docker-compose for backend + PostgreSQL + Redis; production deployment                                       |

---

## 2. Phase 0 — Prerequisites & Environment

> **Goal:** Ensure the development environment is fully working before writing any new code.

### Step 0.1 — Verify Prerequisites Are Installed

Ensure the following are installed and accessible from the terminal:

- **Java 17** — run `java -version` and confirm output shows 17.x
- **Maven** — run `mvn -version` (or use the included `mvnw.cmd` wrapper)
- **PostgreSQL** — a running instance (local or Docker)
- **Redis** — a running instance (local or Docker)
- **Docker** — run `docker --version`; Docker Desktop on Windows must be running
- **Node.js 18+** — run `node -v` (needed for the frontend in later phases)

### Step 0.2 — Set Environment Variables

The application reads configuration from environment variables. Set these before running:

```
DB_URL=jdbc:postgresql://localhost:5432/codywar
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=<base64-encoded-string-at-least-256-bits>
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

**How to generate a JWT secret (PowerShell):**

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

### Step 0.3 — Create the PostgreSQL Database

```sql
CREATE DATABASE codywar;
```

### Step 0.4 — Verify Backend Compiles

Run from `Backend/CodyWar/`:

```
.\mvnw.cmd clean compile
```

This must succeed with no errors. If Redis or DB connection errors appear at runtime that's OK — we only need compilation to pass here.

### Step 0.5 — Verify Backend Starts

Run from `Backend/CodyWar/`:

```
.\mvnw.cmd spring-boot:run
```

Confirm the application starts on port 8000 and Hibernate creates the tables. Stop the app after verification.

---

## 3. Phase 1 — Test Case Management (Backend)

> **Goal:** Create the repository for test cases and enhance ProblemService so that problems can be created/updated with embedded test cases. This is a prerequisite for code judging.

### Step 1.1 — Create `TestCaseRepository`

**File:** `src/main/java/com/gourav/CodyWar/Repository/TestCaseRepository.java`

**What to do:**

- Create a Spring Data JPA interface extending `JpaRepository<TestCase, UUID>`
- Add a method `List<TestCase> findByProblemIdOrderByOrderIndexAsc(UUID problemId)` — this returns all test cases for a given problem, ordered by their `orderIndex` field
- Add a method `List<TestCase> findByProblemIdAndIsHiddenFalseOrderByOrderIndexAsc(UUID problemId)` — this returns only the non-hidden (sample) test cases, used when showing examples to users
- Add a method `long countByProblemId(UUID problemId)` — used to get total test case count

**Why:** The `TestCase` entity already exists with fields `input`, `expectedOutput`, `isHidden`, and `orderIndex`. The `CodeExecutionService` (Phase 4) will need to fetch test cases by problem ID to compare execution output against expected output.

### Step 1.2 — Create `TestCaseDto`

**File:** `src/main/java/com/gourav/CodyWar/Domain/Dto/TestCaseDto.java`

**What to do:**

- Create a DTO class with Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Fields: `UUID id`, `String input`, `String expectedOutput`, `boolean isHidden`, `int orderIndex`

**Why:** Needed for the request/response when creating problems with test cases.

### Step 1.3 — Update `ProblemRequestDto` to Accept Test Cases

**File:** `src/main/java/com/gourav/CodyWar/Domain/Dto/ProblemRequestDto.java`

**What to do:**

- Add a field `List<TestCaseDto> testCases` to the existing `ProblemRequestDto`
- Add `@Valid` annotation on the field to cascade validation
- Each `TestCaseDto` in the list represents a test case to be created with the problem

**Why:** Currently, problems are created without test cases. The PRD requires test cases to be associated with problems for judging submissions.

### Step 1.4 — Update `ProblemResponseDto` to Include Test Cases

**File:** `src/main/java/com/gourav/CodyWar/Domain/Dto/ProblemResponseDto.java`

**What to do:**

- Add a field `List<TestCaseDto> testCases` to the existing `ProblemResponseDto`
- When building the response, only include non-hidden test cases (where `isHidden == false`) for regular users
- Admin endpoints can include all test cases

**Why:** Users need to see sample test cases in the battle room. Hidden test cases are only used internally for judging.

### Step 1.5 — Update `ProblemService` to Handle Test Cases

**File:** `src/main/java/com/gourav/CodyWar/Service/ProblemService.java`

**What to do:**

- Inject `TestCaseRepository` into ProblemService
- In `createProblem()`: after saving the Problem entity, iterate over `request.getTestCases()` (if not null/empty), create `TestCase` entities for each, set the `problem` field to the saved problem, set `orderIndex` incrementally starting from 0, then save all test cases using `testCaseRepository.saveAll()`
- In `updateProblem()`: delete existing test cases for the problem using `testCaseRepository.deleteAll(problem.getTestCases())`, then re-create them from the request (same as create logic)
- In `mapToResponseDto()`: fetch non-hidden test cases using `testCaseRepository.findByProblemIdAndIsHiddenFalseOrderByOrderIndexAsc(problemId)`, map each to `TestCaseDto`, and set on the response DTO

**Why:** Problems are useless without test cases for the code judging pipeline.

### Step 1.6 — Write Unit Tests for Test Case Management

**File:** `src/test/java/com/gourav/CodyWar/Service/ProblemServiceTest.java`

**What to do:**

- Create a test class with `@ExtendWith(MockitoExtension.class)`
- Mock `ProblemRepository`, `TestCaseRepository`
- Write tests for:
  - `createProblem_withTestCases_savesAllTestCases()` — verify `testCaseRepository.saveAll()` is called with correct data
  - `createProblem_withoutTestCases_savesOnlyProblem()` — verify no test case save occurs
  - `updateProblem_replacesTestCases()` — verify old test cases are deleted and new ones saved
  - `getProblemById_returnsOnlyVisibleTestCases()` — verify hidden test cases are excluded from response

**Why:** Ensures test case CRUD works before building the execution pipeline on top of it.

---

## 4. Phase 2 — Submission Data Layer (Backend)

> **Goal:** Create the repository and DTOs for submissions. This is pure data access — no business logic yet.

### Step 2.1 — Create `SubmissionRepository`

**File:** `src/main/java/com/gourav/CodyWar/Repository/SubmissionRepository.java`

**What to do:**

- Create a Spring Data JPA interface extending `JpaRepository<Submission, UUID>`
- Add method: `List<Submission> findByBattleIdAndUserId(UUID battleId, UUID userId)` — fetches all submissions by a user for a specific battle
- Add method: `List<Submission> findByBattleIdOrderBySubmittedAtDesc(UUID battleId)` — all submissions in a battle, newest first
- Add method: `Optional<Submission> findFirstByBattleIdAndStatusOrderBySubmittedAtAsc(UUID battleId, SubmissionStatus status)` — finds the first ACCEPTED submission in a battle (for winner determination)
- Add method: `long countByBattleIdAndUserId(UUID battleId, UUID userId)` — counts submissions to potentially enforce a max submission limit
- Add method: `List<Submission> findByUserIdOrderBySubmittedAtDesc(UUID userId)` — a user's submission history

**Why:** The `Submission` entity exists already. The repository is needed by `SubmissionService` to persist submissions before and after code execution.

### Step 2.2 — Create `SubmitCodeRequest` DTO

**File:** `src/main/java/com/gourav/CodyWar/Domain/Dto/SubmitCodeRequest.java`

**What to do:**

- Create a DTO class with Lombok annotations (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- Fields:
  - `@NotNull UUID battleId` — the battle this submission is for
  - `@NotNull Language language` — the programming language (from the existing `Language` enum)
  - `@NotBlank String code` — the source code submitted by the user

**Why:** This is the contract for the POST endpoint that users hit when they submit code during a battle.

### Step 2.3 — Create `SubmissionResponseDto` DTO

**File:** `src/main/java/com/gourav/CodyWar/Domain/Dto/SubmissionResponseDto.java`

**What to do:**

- Create a DTO class with Lombok annotations
- Fields:
  - `UUID id` — submission ID
  - `UUID battleId`
  - `UUID userId`
  - `String username`
  - `Language language`
  - `SubmissionStatus status`
  - `int testCasesPassed`
  - `int totalTestCases`
  - `Long executionTimeMs`
  - `Long memoryUsedKb`
  - `String errorMessage` — compilation or runtime error text
  - `Instant submittedAt`
  - `Instant judgedAt`

**Why:** This DTO is returned to the frontend after submission and also broadcast via WebSocket to all battle participants.

### Step 2.4 — Create `ExecutionResult` Internal DTO

**File:** `src/main/java/com/gourav/CodyWar/Domain/Dto/ExecutionResult.java`

**What to do:**

- Create an internal DTO (not exposed via API) with Lombok annotations
- Fields:
  - `boolean success` — whether compilation and execution succeeded
  - `String output` — standard output from the program
  - `String errorOutput` — standard error from the program
  - `int exitCode` — process exit code (0 = success)
  - `long executionTimeMs` — measured execution time
  - `long memoryUsedKb` — measured memory usage
  - `boolean timedOut` — true if execution exceeded time limit
  - `boolean memoryExceeded` — true if execution exceeded memory limit

**Why:** This is the internal contract between `CodeExecutionService` (which runs Docker) and `SubmissionService` (which judges the results).

---

## 5. Phase 3 — Docker Code Execution Sandbox

> **Goal:** Create the Docker infrastructure that safely executes user code inside isolated containers. This is the most security-critical component.

### Step 3.1 — Create the Directory Structure

Create the following directory tree at the project root:

```
docker/
├── images/
│   ├── java/
│   │   ├── Dockerfile
│   │   └── run.sh
│   ├── python/
│   │   ├── Dockerfile
│   │   └── run.sh
│   └── javascript/
│       ├── Dockerfile
│       └── run.sh
```

**Why:** Each supported language needs its own Docker image with the appropriate compiler/runtime and a standardized execution script.

### Step 3.2 — Create the Java Execution Image

**File:** `docker/images/java/Dockerfile`

**What to do:**

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
RUN adduser -D -s /bin/sh coderunner
WORKDIR /code
COPY run.sh /run.sh
RUN chmod +x /run.sh
USER coderunner
ENTRYPOINT ["/run.sh"]
```

**Key properties of this image:**

- Uses Alpine for small size
- Creates a non-root user `coderunner` for security
- Working directory is `/code` where user code will be mounted
- Runs the `run.sh` script as entry point

**File:** `docker/images/java/run.sh`

**What to do:**

```bash
#!/bin/sh
set -e

# Compile
javac /code/Solution.java 2>/code/compile_error.txt
if [ $? -ne 0 ]; then
    echo "COMPILATION_ERROR"
    cat /code/compile_error.txt
    exit 1
fi

# Run with timeout
timeout ${TIME_LIMIT:-5}s java -cp /code -Xmx${MEMORY_LIMIT:-256}m Solution < /code/input.txt > /code/output.txt 2>/code/runtime_error.txt
EXIT_CODE=$?

if [ $EXIT_CODE -eq 124 ]; then
    echo "TIME_LIMIT_EXCEEDED"
    exit 124
elif [ $EXIT_CODE -ne 0 ]; then
    echo "RUNTIME_ERROR"
    cat /code/runtime_error.txt
    exit $EXIT_CODE
fi

cat /code/output.txt
exit 0
```

**Why:** This script follows the compile → run → capture output flow described in the PRD. The `timeout` command enforces time limits. The `-Xmx` flag enforces Java heap memory limits. Exit codes are used to distinguish between different failure modes.

### Step 3.3 — Create the Python Execution Image

**File:** `docker/images/python/Dockerfile`

```dockerfile
FROM python:3.11-alpine
RUN adduser -D -s /bin/sh coderunner
WORKDIR /code
COPY run.sh /run.sh
RUN chmod +x /run.sh
USER coderunner
ENTRYPOINT ["/run.sh"]
```

**File:** `docker/images/python/run.sh`

```bash
#!/bin/sh
set -e

timeout ${TIME_LIMIT:-5}s python3 /code/Solution.py < /code/input.txt > /code/output.txt 2>/code/runtime_error.txt
EXIT_CODE=$?

if [ $EXIT_CODE -eq 124 ]; then
    echo "TIME_LIMIT_EXCEEDED"
    exit 124
elif [ $EXIT_CODE -ne 0 ]; then
    echo "RUNTIME_ERROR"
    cat /code/runtime_error.txt
    exit $EXIT_CODE
fi

cat /code/output.txt
exit 0
```

### Step 3.4 — Create the JavaScript Execution Image

**File:** `docker/images/javascript/Dockerfile`

```dockerfile
FROM node:18-alpine
RUN adduser -D -s /bin/sh coderunner
WORKDIR /code
COPY run.sh /run.sh
RUN chmod +x /run.sh
USER coderunner
ENTRYPOINT ["/run.sh"]
```

**File:** `docker/images/javascript/run.sh`

```bash
#!/bin/sh
set -e

timeout ${TIME_LIMIT:-5}s node /code/Solution.js < /code/input.txt > /code/output.txt 2>/code/runtime_error.txt
EXIT_CODE=$?

if [ $EXIT_CODE -eq 124 ]; then
    echo "TIME_LIMIT_EXCEEDED"
    exit 124
elif [ $EXIT_CODE -ne 0 ]; then
    echo "RUNTIME_ERROR"
    cat /code/runtime_error.txt
    exit $EXIT_CODE
fi

cat /code/output.txt
exit 0
```

### Step 3.5 — Build All Docker Images

Run the following commands from the project root to build the images:

```powershell
docker build -t codywar-java:latest docker/images/java/
docker build -t codywar-python:latest docker/images/python/
docker build -t codywar-javascript:latest docker/images/javascript/
```

Verify each image exists:

```powershell
docker images | findstr codywar
```

**Why:** These images must be pre-built on the host machine before the CodeExecutionService can use them. They are not pulled from a registry — they are local builds.

### Step 3.6 — Test a Docker Image Manually

To verify the Java image works:

1. Create a temp directory with a `Solution.java` file containing a simple `Hello World` and an `input.txt` file
2. Run: `docker run --rm -v <temp-dir>:/code --network none --memory 256m --cpus 0.5 codywar-java:latest`
3. Verify the output is `Hello World`

**Why:** Manual verification catches Dockerfile issues before they become hard-to-debug service errors.

---

## 6. Phase 4 — Code Execution Service (Backend)

> **Goal:** Create the Java service that orchestrates Docker container execution. This service writes user code to a temp directory, runs a Docker container, captures output, and returns structured results.

### Step 4.1 — Add Docker Client Dependency to `pom.xml`

**File:** `Backend/CodyWar/pom.xml`

**What to do:**

- Add the `com.github.docker-java:docker-java-core` and `com.github.docker-java:docker-java-transport-httpclient5` dependencies (version `3.3.6` or latest stable)
- These provide a Java API for interacting with the Docker daemon

```xml
<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java-core</artifactId>
    <version>3.3.6</version>
</dependency>
<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java-transport-httpclient5</artifactId>
    <version>3.3.6</version>
</dependency>
```

**Alternative approach (simpler, recommended for MVP):** Instead of the Docker Java client library, use `ProcessBuilder` to run `docker` CLI commands directly. This is simpler to implement and debug. The steps below assume the `ProcessBuilder` approach.

### Step 4.2 — Add Execution Configuration Properties

**File:** `src/main/resources/application.properties`

**What to do — append these lines:**

```properties
# Code Execution Configuration
execution.temp-dir=${java.io.tmpdir}/codywar-executions
execution.default-time-limit-seconds=5
execution.default-memory-limit-mb=256
execution.container-timeout-seconds=30
```

**Why:** Externalizes execution settings so they can be tuned without code changes.

### Step 4.3 — Create `CodeExecutionService`

**File:** `src/main/java/com/gourav/CodyWar/Service/CodeExecutionService.java`

**What to do — implement the following class with these exact methods:**

**Class-level:**

- Annotate with `@Service` and `@Slf4j`
- Inject configuration values using `@Value` for the properties added in Step 4.2

**Method: `getDockerImageName(Language language)`**

- Visibility: `private`
- Takes a `Language` enum value and returns the Docker image name string
- Mapping: `JAVA` → `"codywar-java:latest"`, `PYTHON` → `"codywar-python:latest"`, `JAVASCRIPT` → `"codywar-javascript:latest"`
- For unsupported languages, throw `IllegalArgumentException("Unsupported language: " + language)`

**Method: `getSourceFileName(Language language)`**

- Visibility: `private`
- Returns the expected filename for each language: `JAVA` → `"Solution.java"`, `PYTHON` → `"Solution.py"`, `JAVASCRIPT` → `"Solution.js"`

**Method: `executeCode(String code, String input, Language language, int timeLimitSeconds, int memoryLimitMb)`**

- Visibility: `public`
- Return type: `ExecutionResult`
- Steps:
  1. Create a unique temp directory: `Files.createTempDirectory("codywar-exec-")` inside the configured temp dir
  2. Write the user's code to `<tempDir>/<sourceFileName>` (e.g., `Solution.java`)
  3. Write the test case input to `<tempDir>/input.txt`
  4. Build a `docker run` command using `ProcessBuilder`:
     ```
     docker run --rm
       --network none
       --memory <memoryLimitMb>m
       --cpus 0.5
       --pids-limit 50
       -e TIME_LIMIT=<timeLimitSeconds>
       -e MEMORY_LIMIT=<memoryLimitMb>
       -v <tempDir>:/code
       <dockerImageName>
     ```
  5. Start the process and wait for completion with a timeout of `containerTimeoutSeconds`
  6. Read stdout and stderr from the process
  7. Determine the `ExecutionResult` based on exit code:
     - Exit 0 → `success=true`, `output=stdout`
     - Exit 124 → `timedOut=true`
     - Exit 1 with stderr containing "COMPILATION_ERROR" → compilation error
     - Other non-zero → runtime error
  8. Measure execution time using `System.nanoTime()` before and after process execution
  9. Clean up: delete the temp directory in a `finally` block
  10. Return the `ExecutionResult`

**Method: `executeAgainstTestCases(String code, Language language, List<TestCase> testCases, int timeLimitSeconds, int memoryLimitMb)`**

- Visibility: `public`
- Return type: A new inner class or record called `JudgingResult` containing: `SubmissionStatus status`, `int testCasesPassed`, `int totalTestCases`, `long executionTimeMs`, `String errorMessage`
- Steps:
  1. Set `totalTestCases = testCases.size()` and `testCasesPassed = 0`
  2. For each `TestCase` in order:
     a. Call `executeCode(code, testCase.getInput(), language, timeLimitSeconds, memoryLimitMb)`
     b. If `executionResult.isTimedOut()` → return immediately with status `TIME_LIMIT_EXCEEDED`
     c. If `!executionResult.isSuccess()` → return immediately with status `RUNTIME_ERROR` or `COMPILATION_ERROR` (check `executionResult.getErrorOutput()`)
     d. Compare `executionResult.getOutput().trim()` with `testCase.getExpectedOutput().trim()` — if they match, increment `testCasesPassed`
     e. If they don't match, continue to next test case (don't short-circuit on wrong answer — we want to report partial scores)
  3. After all test cases: if `testCasesPassed == totalTestCases`, status = `ACCEPTED`; otherwise status = `WRONG_ANSWER`
  4. Return the `JudgingResult`

**Why:** This is the heart of CodeClash. The PRD specifies Docker sandboxing with `--network none`, memory limits, and timeout enforcement. The `ProcessBuilder` approach is simplest for MVP and avoids the complexity of the Docker Java client library.

### Step 4.4 — Write Unit Tests for CodeExecutionService

**File:** `src/test/java/com/gourav/CodyWar/Service/CodeExecutionServiceTest.java`

**What to do:**

- These tests require Docker to be running. Annotate the class with `@SpringBootTest` or use `@Testcontainers` if preferred
- Tests:
  - `executeCode_javaHelloWorld_returnsSuccessOutput()` — submit `public class Solution { public static void main(String[] args) { System.out.println("Hello"); } }`, verify output is `"Hello\n"`
  - `executeCode_pythonPrint_returnsSuccessOutput()` — submit `print("Hello")`, verify output
  - `executeCode_infiniteLoop_returnsTimedOut()` — submit `while(true){}`, verify `timedOut == true`
  - `executeCode_compilationError_returnsError()` — submit invalid Java code, verify appropriate error status
  - `executeAgainstTestCases_allPass_returnsAccepted()` — submit correct solution, verify ACCEPTED
  - `executeAgainstTestCases_someFail_returnsWrongAnswer()` — submit partially correct solution, verify WRONG_ANSWER with correct `testCasesPassed` count

**Why:** Docker-dependent tests validate the full execution pipeline end-to-end.

---

## 7. Phase 5 — Submission Service (Backend)

> **Goal:** Create the service that orchestrates the full submission lifecycle: receive code → save to DB → execute in sandbox → judge → update DB → update battle state → broadcast result.

### Step 5.1 — Create `SubmissionService`

**File:** `src/main/java/com/gourav/CodyWar/Service/SubmissionService.java`

**What to do — implement the following class:**

**Class-level:**

- Annotate with `@Service`, `@RequiredArgsConstructor`, `@Slf4j`
- Inject: `SubmissionRepository`, `BattleRepository`, `BattleParticipantRepository`, `TestCaseRepository`, `UserRepository`, `CodeExecutionService`, `BattleService`, `RedisTemplate<String, Object>`, `SimpMessagingTemplate`

**Method: `submitCode(UUID userId, SubmitCodeRequest request)`**

- Visibility: `public`
- Return type: `SubmissionResponseDto`
- Annotate with `@Transactional`
- Steps:
  1. **Validate user exists:** `userRepository.findById(userId).orElseThrow()`
  2. **Validate battle exists and is IN_PROGRESS:** `battleRepository.findById(request.getBattleId()).orElseThrow()`. If `battle.getStatus() != BattleStatus.IN_PROGRESS`, throw `IllegalStateException("Battle is not in progress")`
  3. **Validate user is a participant:** `participantRepository.findByBattleIdAndUserId(battleId, userId).orElseThrow()`
  4. **Create and save the Submission entity** with status `PENDING`:
     ```
     Submission submission = Submission.builder()
         .battle(battle)
         .user(user)
         .language(request.getLanguage())
         .code(request.getCode())
         .status(SubmissionStatus.PENDING)
         .build();
     submissionRepository.save(submission);
     ```
  5. **Broadcast SUBMISSION_RECEIVED event** via WebSocket so other players see that a submission was made:
     ```
     broadcastBattleEvent(battleId, SUBMISSION_RECEIVED, { userId, username, submissionId })
     ```
  6. **Execute code asynchronously** — call `processSubmission(submission.getId())` in a new thread using `@Async` or `CompletableFuture.runAsync()`
  7. **Return** a `SubmissionResponseDto` with status PENDING (the final result will come via WebSocket)

**Method: `processSubmission(UUID submissionId)`**

- Visibility: `private` (or package-private for testing)
- Annotate with `@Transactional`
- Steps:
  1. Reload the submission from DB: `submissionRepository.findById(submissionId).orElseThrow()`
  2. Update status to `RUNNING` and save
  3. Fetch test cases: `testCaseRepository.findByProblemIdOrderByOrderIndexAsc(submission.getBattle().getProblem().getId())`
  4. Get problem limits: `timeLimitSeconds` and `memoryLimitMb` from the Problem entity
  5. Call `codeExecutionService.executeAgainstTestCases(code, language, testCases, timeLimit, memoryLimit)`
  6. Update the submission with results:
     ```
     submission.setStatus(judgingResult.getStatus());
     submission.setTestCasesPassed(judgingResult.getTestCasesPassed());
     submission.setTotalTestCases(judgingResult.getTotalTestCases());
     submission.setExecutionTimeMs(judgingResult.getExecutionTimeMs());
     submission.setErrorMessage(judgingResult.getErrorMessage());
     submission.setJudgedAt(Instant.now());
     submissionRepository.save(submission);
     ```
  7. **Update BattleParticipant:** set `hasSubmitted = true`, update `score = testCasesPassed`
  8. **Update Redis BattleState:** update the participant's `hasSubmitted`, `score`, and `lastSubmissionId` fields in the `BattleState` stored in Redis
  9. **Broadcast SUBMISSION_JUDGED event** via WebSocket:
     ```
     broadcastBattleEvent(battleId, SUBMISSION_JUDGED, { submissionId, userId, status, testCasesPassed, totalTestCases, executionTimeMs })
     ```
  10. **Check for winner:** If status is `ACCEPTED`, this player solved the problem. Call `battleService.endBattle(battleId, userId)` to end the battle with this player as the winner

**Method: `getSubmission(UUID submissionId)`**

- Return the submission as `SubmissionResponseDto`

**Method: `getBattleSubmissions(UUID battleId)`**

- Return `List<SubmissionResponseDto>` for all submissions in the battle

**Method: `getUserSubmissions(UUID userId)`**

- Return `List<SubmissionResponseDto>` for a user's submission history

**Private helper: `mapToSubmissionResponseDto(Submission submission)`**

- Maps the entity to the DTO, including the username from the User relation

**Why:** This service is the glue between the user-facing API and the Docker execution engine. The asynchronous pattern prevents HTTP timeouts during code execution (which can take up to 30 seconds).

### Step 5.2 — Enable Async Processing

**File:** `src/main/java/com/gourav/CodyWar/CodyWarApplication.java`

**What to do:**

- Add `@EnableAsync` annotation to the main application class (it already has `@EnableScheduling`)

**Why:** The `@Async` annotation on `processSubmission` won't work without `@EnableAsync` on the application.

### Step 5.3 — Create AsyncConfig (Optional but Recommended)

**File:** `src/main/java/com/gourav/CodyWar/Configuration/AsyncConfig.java`

**What to do:**

- Create a `@Configuration` class
- Define a `@Bean` of type `Executor` named `taskExecutor`
- Use `ThreadPoolTaskExecutor` with: corePoolSize=5, maxPoolSize=20, queueCapacity=100, threadNamePrefix="CodeExec-"

**Why:** Without explicit configuration, Spring uses a single-thread executor for `@Async`. A thread pool prevents bottlenecks when multiple submissions arrive simultaneously.

### Step 5.4 — Write Unit Tests for SubmissionService

**File:** `src/test/java/com/gourav/CodyWar/Service/SubmissionServiceTest.java`

**What to do:**

- Use `@ExtendWith(MockitoExtension.class)`
- Mock all injected dependencies
- Tests:
  - `submitCode_validRequest_createsSubmissionAndReturnsDto()` — verify submission is saved with status PENDING
  - `submitCode_battleNotInProgress_throwsException()` — verify ISE when battle status is WAITING
  - `submitCode_userNotParticipant_throwsException()` — verify IAE when user not in battle
  - `processSubmission_allTestCasesPass_setsAccepted()` — mock `CodeExecutionService` to return all-pass result, verify submission updated to ACCEPTED
  - `processSubmission_someTestCasesFail_setsWrongAnswer()` — mock partial failure, verify WRONG_ANSWER
  - `processSubmission_timeLimit_setsTimeLimitExceeded()` — mock timeout, verify TLE status
  - `processSubmission_accepted_endsBattle()` — verify `battleService.endBattle()` is called when ACCEPTED

---

## 8. Phase 6 — Submission Controller & API (Backend)

> **Goal:** Create the REST controller that exposes the submission endpoint to the frontend.

### Step 6.1 — Create `SubmissionController`

**File:** `src/main/java/com/gourav/CodyWar/Controller/SubmissionController.java`

**What to do:**

```
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Slf4j
```

**Endpoints:**

| Method | Path                                 | Auth     | Description                                                                                               |
| ------ | ------------------------------------ | -------- | --------------------------------------------------------------------------------------------------------- |
| `POST` | `/api/submissions`                   | Required | Submit code for a battle. Request body: `SubmitCodeRequest`. Returns `ApiResponse<SubmissionResponseDto>` |
| `GET`  | `/api/submissions/{id}`              | Required | Get a specific submission by ID. Returns `ApiResponse<SubmissionResponseDto>`                             |
| `GET`  | `/api/submissions/battle/{battleId}` | Required | Get all submissions for a battle. Returns `ApiResponse<List<SubmissionResponseDto>>`                      |
| `GET`  | `/api/submissions/my-submissions`    | Required | Get current user's submission history. Returns `ApiResponse<List<SubmissionResponseDto>>`                 |

Each endpoint should:

- Extract user from `@AuthenticationPrincipal CustomUserDetails`
- Delegate to `SubmissionService`
- Wrap response in `ApiResponse.success()`

**Why:** This is the standard REST layer. The POST endpoint is what the frontend calls when the user clicks "Submit" in the code editor.

### Step 6.2 — Write Controller Tests

**File:** `src/test/java/com/gourav/CodyWar/Controller/SubmissionControllerTest.java`

**What to do:**

- Use `@WebMvcTest(SubmissionController.class)` with `@AutoConfigureMockMvc(addFilters = false)` to disable security filters
- Mock `SubmissionService`
- Tests:
  - `submitCode_returnsCreated()` — POST with valid body, verify 200 and response shape
  - `getSubmission_returnsDto()` — GET by ID, verify 200
  - `getBattleSubmissions_returnsList()` — GET by battle ID, verify 200

---

## 9. Phase 7 — Battle ↔ Submission Integration (Backend)

> **Goal:** Wire the submission flow into the existing battle lifecycle so that submissions trigger winner determination and battle completion.

### Step 7.1 — Add WebSocket Submission Handler

**File:** `src/main/java/com/gourav/CodyWar/Controller/BattleWebSocketController.java`

**What to do:**

- Add a new STOMP message handler for code submission via WebSocket (as an alternative to the REST endpoint):

```java
@MessageMapping("/battle/{battleId}/submit")
public void handleCodeSubmission(
        @DestinationVariable UUID battleId,
        Map<String, Object> payload,
        SimpMessageHeaderAccessor headerAccessor) {
    // Extract user, validate, delegate to submissionService.submitCode()
    // The service will broadcast results via WebSocket
}
```

**Why:** Some users may prefer submitting via WebSocket for lower latency. The REST endpoint remains as the primary method.

### Step 7.2 — Update `BattleService.endBattle()` to Include Score Details

**File:** `src/main/java/com/gourav/CodyWar/Service/BattleService.java`

**What to do:**

- In the `endBattle()` method, after determining the winner, include participant scores and submission details in the `WINNER_ANNOUNCEMENT` WebSocket event payload
- Add participant scores, test cases passed, and execution times to the broadcast

**Why:** The frontend needs detailed results to display the battle outcome screen.

### Step 7.3 — Handle Battle Timeout with Submissions

**File:** `src/main/java/com/gourav/CodyWar/Service/BattleService.java`

**What to do:**

- Update `determineWinnerAndEndBattle()` (called by `updateBattleTimers()` when time runs out):
  - Instead of only checking `ParticipantState.score`, also query `SubmissionRepository` for the highest-scoring ACCEPTED or WRONG_ANSWER submission
  - Winner is determined by: (1) most test cases passed, then (2) earliest submission time as tiebreaker
  - If no participants submitted, end with no winner (draw)

**Why:** When the timer expires, the current code only looks at Redis state scores. It should also consider the actual submission records for accuracy.

---

## 10. Phase 8 — Rating / ELO System (Backend)

> **Goal:** Implement an ELO-based rating system that adjusts player ratings after each battle.

### Step 8.1 — Create `RatingService`

**File:** `src/main/java/com/gourav/CodyWar/Service/RatingService.java`

**What to do:**

- Annotate with `@Service`, `@RequiredArgsConstructor`, `@Slf4j`
- Inject `UserRepository`

**Method: `updateRatings(UUID winnerId, UUID loserId)`**

- Implement ELO rating calculation:
  - K-factor: 32 (standard for new players)
  - Expected score: `E_a = 1 / (1 + 10^((R_b - R_a) / 400))`
  - New rating: `R_a' = R_a + K * (S_a - E_a)` where `S_a = 1` for win, `0` for loss
- Fetch both users from DB
- Calculate new ratings for both
- Update and save both users
- Return a Map or DTO with old and new ratings for both players

**Method: `updateRatingsForDraw(UUID userId1, UUID userId2)`**

- Same formula but `S_a = 0.5` for both players

**Why:** The PRD mentions `ratingScore` on the User entity (defaults to 1000). This service calculates proper ELO adjustments.

### Step 8.2 — Integrate Rating Updates into `BattleService.endBattle()`

**File:** `src/main/java/com/gourav/CodyWar/Service/BattleService.java`

**What to do:**

- Inject `RatingService` into BattleService
- In `endBattle()`, after setting the winner:
  - If there's a winner and exactly 2 participants: call `ratingService.updateRatings(winnerId, loserId)`
  - If no winner (draw) and 2 participants: call `ratingService.updateRatingsForDraw()`
  - Include the rating changes in the `WINNER_ANNOUNCEMENT` WebSocket event

**Why:** Ratings should update automatically when battles end.

### Step 8.3 — Add Leaderboard Endpoint

**File:** `src/main/java/com/gourav/CodyWar/Repository/UserRepository.java`

**What to do:**

- Add method: `List<User> findTop50ByOrderByRatingScoreDesc()` — returns top 50 players by rating

**File:** `src/main/java/com/gourav/CodyWar/Controller/AuthController.java` (or create a new `LeaderboardController`)

**What to do:**

- Add endpoint: `GET /api/leaderboard` — returns top players
- This can be a public endpoint (no auth required) or authenticated

**Why:** Leaderboards are a core gamification feature.

---

## 11. Phase 9 — Backend Hardening & Tests

> **Goal:** Fix existing test issues, add comprehensive tests, add CORS, and prepare the backend for frontend integration.

### Step 9.1 — Add CORS Configuration

**File:** `src/main/java/com/gourav/CodyWar/Configuration/SecurityConfig.java`

**What to do:**

- Add a `CorsConfigurationSource` bean
- Allow origins: `http://localhost:3000`, `http://localhost:5173` (Vite default)
- Allow methods: GET, POST, PUT, DELETE, OPTIONS
- Allow headers: Authorization, Content-Type
- Allow credentials: true
- Apply CORS to the security filter chain with `.cors(cors -> cors.configurationSource(corsConfigurationSource()))`

**Why:** Without CORS, the React frontend running on a different port will get CORS errors when calling the API.

### Step 9.2 — Add WebSocket CORS

**File:** `src/main/java/com/gourav/CodyWar/Configuration/WebSocketConfig.java`

**What to do:**

- In `registerStompEndpoints()`, update `.setAllowedOrigins("*")` to `.setAllowedOriginPatterns("http://localhost:*")` for development

**Why:** WebSocket connections also need CORS allowance.

### Step 9.3 — Fix Existing Test Issues

**File:** `src/test/java/com/gourav/CodyWar/Service/BattleServiceTest.java`

**What to do:**

- Review all 31 tests and fix any failures
- Known issue: Redis mock operations — ensure `redisTemplate.delete(key)` is properly mocked (not `valueOperations.delete()`)
- Known issue: `@Value` fields need to be set via `ReflectionTestUtils.setField()` in `@BeforeEach`
- Run tests with `.\mvnw.cmd test -pl . -Dtest=BattleServiceTest` and fix until all pass

### Step 9.4 — Add Integration Test Configuration

**File:** `src/test/resources/application-test.properties`

**What to do:**

- Create test-specific properties:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3Rpbmc=
jwt.expiration=900000
jwt.refresh-expiration=604800000
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

**File:** `pom.xml`

- Add H2 test dependency:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

**Why:** Unit tests should not depend on a running PostgreSQL instance.

### Step 9.5 — Add `GlobalExceptionHandler` for New Exception Types

**File:** `src/main/java/com/gourav/CodyWar/Configuration/GlobalExceptionHandler.java`

**What to do:**

- Add handler for `IllegalStateException` (used by BattleService for state violations):
  ```java
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(ApiResponse.error(ex.getMessage()));
  }
  ```

**Why:** Currently, `IllegalStateException` falls through to the generic handler which returns 500. It should return 409 Conflict.

---

## 12. Phase 10 — Frontend Scaffolding (React)

> **Goal:** Create the React frontend project with all dependencies, routing, and project structure.

### Step 10.1 — Create the React App

Run from the workspace root (`c:\Users\ssr\Desktop\Codywar\`):

```powershell
npm create vite@latest Frontend -- --template react
cd Frontend
npm install
```

### Step 10.2 — Install Dependencies

Run from `Frontend/`:

```powershell
npm install react-router-dom axios @monaco-editor/react @stomp/stompjs sockjs-client
npm install -D tailwindcss @tailwindcss/vite
```

| Package                | Purpose                                             |
| ---------------------- | --------------------------------------------------- |
| `react-router-dom`     | Client-side routing (login, dashboard, battle room) |
| `axios`                | HTTP client for REST API calls                      |
| `@monaco-editor/react` | Monaco Editor component (the VS Code editor)        |
| `@stomp/stompjs`       | STOMP WebSocket client for real-time battle events  |
| `sockjs-client`        | WebSocket fallback transport                        |
| `tailwindcss`          | Utility-first CSS framework                         |

### Step 10.3 — Configure Tailwind CSS

**File:** `Frontend/src/index.css`

Replace contents with:

```css
@import "tailwindcss";
```

**File:** `Frontend/vite.config.js`

Add tailwind plugin:

```javascript
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 3000,
    proxy: {
      "/api": "http://localhost:8000",
      "/ws": {
        target: "http://localhost:8000",
        ws: true,
      },
    },
  },
});
```

**Why:** The proxy configuration forwards API and WebSocket requests to the Spring Boot backend during development.

### Step 10.4 — Create Directory Structure

```
Frontend/src/
├── api/
│   └── axios.js          # Axios instance with interceptors
├── components/
│   ├── common/            # Shared components (Button, Input, Loading, etc.)
│   ├── auth/              # Login, Register forms
│   ├── battle/            # Battle lobby, room, timer, scoreboard
│   └── editor/            # Monaco editor wrapper
├── context/
│   └── AuthContext.jsx    # Auth state management
├── hooks/
│   ├── useAuth.js         # Auth hook
│   ├── useBattle.js       # Battle state hook
│   └── useWebSocket.js    # WebSocket connection hook
├── pages/
│   ├── LoginPage.jsx
│   ├── RegisterPage.jsx
│   ├── DashboardPage.jsx
│   ├── BattleLobbyPage.jsx
│   ├── BattleRoomPage.jsx
│   └── LeaderboardPage.jsx
├── utils/
│   └── constants.js       # API URLs, event types
├── App.jsx
├── main.jsx
└── index.css
```

### Step 10.5 — Create Axios Instance with JWT Interceptor

**File:** `Frontend/src/api/axios.js`

**What to do:**

- Create an Axios instance with `baseURL: '/api'`
- Add a request interceptor that reads the JWT token from `localStorage.getItem('accessToken')` and adds it to the `Authorization: Bearer <token>` header
- Add a response interceptor that catches 401 errors, attempts to refresh the token using `POST /api/auth/refresh` with the refresh token from `localStorage.getItem('refreshToken')`, and retries the original request
- If refresh also fails, clear localStorage and redirect to `/login`

**Why:** Every authenticated API call needs the JWT token. The interceptor automates this.

### Step 10.6 — Create Auth Context

**File:** `Frontend/src/context/AuthContext.jsx`

**What to do:**

- Create a React Context with state: `user`, `isAuthenticated`, `loading`
- On mount, check for existing token in localStorage and call `GET /api/auth/me` to restore session
- Provide methods: `login(email, password)`, `register(username, email, password)`, `logout()`
- `login` calls `POST /api/auth/login`, stores tokens, sets user
- `logout` clears localStorage and resets state

### Step 10.7 — Set Up Routing

**File:** `Frontend/src/App.jsx`

**What to do:**

- Set up `BrowserRouter` with routes:
  - `/login` → `LoginPage` (public)
  - `/register` → `RegisterPage` (public)
  - `/dashboard` → `DashboardPage` (protected)
  - `/battle/lobby` → `BattleLobbyPage` (protected)
  - `/battle/:id` → `BattleRoomPage` (protected)
  - `/leaderboard` → `LeaderboardPage` (public)
  - `/` → redirect to `/dashboard` or `/login`
- Create a `ProtectedRoute` component that checks `isAuthenticated` and redirects to `/login` if not

---

## 13. Phase 11 — Frontend Auth Pages

> **Goal:** Build login and registration pages that connect to the existing auth API.

### Step 11.1 — Create Login Page

**File:** `Frontend/src/pages/LoginPage.jsx`

**What to do:**

- Form with email and password fields
- Submit button calls `AuthContext.login()`
- On success, redirect to `/dashboard`
- On error, show error message
- Link to register page

### Step 11.2 — Create Register Page

**File:** `Frontend/src/pages/RegisterPage.jsx`

**What to do:**

- Form with username, email, password, and confirm password fields
- Client-side validation: password match, email format, username min length
- Submit calls `POST /api/auth/register`
- On success, redirect to `/login` with success message

### Step 11.3 — Style Auth Pages

- Use Tailwind classes for a clean, centered card layout
- Dark theme preferred (coding platform aesthetic)
- Responsive design

---

## 14. Phase 12 — Frontend Dashboard & Problem Pages

> **Goal:** Build the main dashboard where users see their stats, active battles, and battle history.

### Step 12.1 — Create Dashboard Page

**File:** `Frontend/src/pages/DashboardPage.jsx`

**What to do:**

- Display user info: username, rating, battles played, battles won, win rate
- Section: "Active Battles" — fetch from `GET /api/battles/active`, show list with Join buttons
- Section: "My Battles" — fetch from `GET /api/battles/my-battles`, show recent battle history
- Button: "Create Battle" → opens a modal or navigates to lobby
- Button: "Find Match" → calls `POST /api/battles/matchmaking`
- Navigation bar with links to Dashboard, Leaderboard, Logout

### Step 12.2 — Create Battle Lobby Page

**File:** `Frontend/src/pages/BattleLobbyPage.jsx`

**What to do:**

- "Create Battle" form: toggle private/public, select duration (5/10/15/30 minutes), max participants
- "Join by Room Code" input field
- "Quick Match" button that calls `POST /api/battles/matchmaking`
- Display waiting status when queued for matchmaking
- On battle creation/join, redirect to `/battle/{battleId}`

### Step 12.3 — Create Leaderboard Page

**File:** `Frontend/src/pages/LeaderboardPage.jsx`

**What to do:**

- Fetch from `GET /api/leaderboard`
- Display ranked table: position, username, rating, battles played, win rate
- Highlight the current user's row

---

## 15. Phase 13 — Frontend Battle Lobby & Room

> **Goal:** Build the pre-battle waiting room where players see each other, mark as ready, and wait for the battle to start.

### Step 13.1 — Create WebSocket Hook

**File:** `Frontend/src/hooks/useWebSocket.js`

**What to do:**

- Create a custom hook `useWebSocket(battleId)` that:
  1. Creates a STOMP client connection to `ws://localhost:8000/ws/battle` (or via the proxy at `/ws/battle`)
  2. Adds the JWT token to the connection headers: `{ Authorization: 'Bearer ' + token }`
  3. On connect, subscribes to `/topic/battle/{battleId}` for broadcast events
  4. Subscribes to `/user/queue/errors` for personal error messages
  5. Subscribes to `/user/queue/battle-state` for personal state updates
  6. Returns: `{ connected, battleEvents, sendMessage, disconnect }`
  7. Sends `/app/battle/{battleId}/join` on connection
  8. Cleans up (disconnect) on unmount

**Why:** WebSocket events drive the entire real-time experience. This hook encapsulates connection management.

### Step 13.2 — Create Battle Room Page (Waiting State)

**File:** `Frontend/src/pages/BattleRoomPage.jsx`

**What to do:**

- Fetch battle info on mount: `GET /api/battles/{id}`
- Connect WebSocket using `useWebSocket(battleId)`
- **Waiting state UI:**
  - Show room code (if private) with "Copy" button
  - Show participant list with ready status (green checkmark or gray circle)
  - "Ready" toggle button — calls `POST /api/battles/{id}/ready?ready=true`
  - "Leave" button — calls `POST /api/battles/{id}/leave`, redirects to lobby
  - Listen for `PLAYER_JOINED`, `PLAYER_LEFT`, `PLAYER_READY` events to update the participant list in real-time
  - Listen for `BATTLE_STARTING` event — show countdown overlay (5, 4, 3, 2, 1)
  - On `TIMER_START` event, transition to the coding state (Phase 14)

**Why:** The waiting room is where players gather before the battle starts. All state changes are driven by WebSocket events.

---

## 16. Phase 14 — Frontend Code Editor & Submission

> **Goal:** Build the in-battle code editor using Monaco Editor, with language selection, submission, and real-time result display.

### Step 14.1 — Create Monaco Editor Component

**File:** `Frontend/src/components/editor/CodeEditor.jsx`

**What to do:**

- Use `@monaco-editor/react`'s `Editor` component
- Props: `language`, `value`, `onChange`
- Configuration:
  - Theme: `"vs-dark"`
  - Options: `minimap: { enabled: false }`, `fontSize: 14`, `wordWrap: "on"`, `automaticLayout: true`
- Language dropdown: map `Language` enum to Monaco language IDs: `JAVA` → `"java"`, `PYTHON` → `"python"`, `JAVASCRIPT` → `"javascript"`, etc.

### Step 14.2 — Create Battle Room Page (Coding State)

**File:** `Frontend/src/pages/BattleRoomPage.jsx` (extend the existing page)

**What to do — the coding state layout (split view):**

- **Left panel (40% width):** Problem description
  - Title, difficulty badge (green/yellow/red), description, constraints
  - Example input/output with formatted code blocks
  - Tab: "Problem" | "Submissions" (shows user's past submissions for this battle)
- **Right panel (60% width):** Code editor
  - Language selector dropdown at the top
  - Monaco Editor taking most of the space
  - "Submit" button at the bottom (calls `POST /api/submissions`)
  - "Run" button (optional — for running against sample test cases only, not full judging)
- **Top bar:** Timer countdown (driven by `TIMER_UPDATE` WebSocket events), opponent status indicators
- **Bottom bar / Toast:** Submission results when `SUBMISSION_JUDGED` events arrive

### Step 14.3 — Handle Submission Flow

**What to do:**

1. When user clicks "Submit":
   - Disable the button and show "Submitting..."
   - Call `POST /api/submissions` with `{ battleId, language, code }`
   - Response returns with status PENDING
   - Show "Judging your submission..." indicator
2. When `SUBMISSION_JUDGED` WebSocket event arrives for the current user:
   - Show result: ACCEPTED (green), WRONG_ANSWER (red), TLE (yellow), etc.
   - Show test cases passed: "5/5 test cases passed"
   - Show execution time
   - If ACCEPTED, the `WINNER_ANNOUNCEMENT` event will follow shortly
3. When `SUBMISSION_JUDGED` event arrives for the opponent:
   - Show notification: "Opponent submitted — 3/5 test cases passed"
   - Do NOT reveal the opponent's code

### Step 14.4 — Handle Battle End

**What to do:**

- Listen for `WINNER_ANNOUNCEMENT` event
- Show a full-screen modal or overlay:
  - "You Won!" / "You Lost!" / "Draw!"
  - Show final scores for all participants
  - Show rating changes (if implemented)
  - "Back to Dashboard" button
- Listen for `BATTLE_CANCELLED` event — show "Battle was cancelled" and redirect

---

## 17. Phase 15 — Frontend Real-Time WebSocket Integration

> **Goal:** Polish the WebSocket integration for reliable reconnection, error handling, and state synchronization.

### Step 15.1 — Implement Reconnection Logic

**File:** `Frontend/src/hooks/useWebSocket.js`

**What to do:**

- On disconnect, attempt to reconnect with exponential backoff (1s, 2s, 4s, 8s, max 30s)
- After reconnect, re-subscribe to all topics and request current state via `/app/battle/{battleId}/state`
- Show "Reconnecting..." indicator in the UI

### Step 15.2 — Implement Heartbeat

**What to do:**

- Send a heartbeat message every 30 seconds to `/app/battle/{battleId}/heartbeat`
- The server responds with current battle state and remaining time
- Use this to sync the client timer with the server timer (prevents drift)

### Step 15.3 — Handle Edge Cases

- What if the user refreshes the page mid-battle? → Restore state from `GET /api/battles/{id}` and reconnect WebSocket
- What if the opponent disconnects? → Show "Opponent disconnected" but don't end the battle immediately (they may reconnect)
- What if both users submit ACCEPTED at nearly the same time? → The server determines the winner by `submittedAt` timestamp

---

## 18. Phase 16 — End-to-End Testing & Polish

> **Goal:** Test the complete flow from user registration through battle completion and verify all integrations work.

### Step 16.1 — Create Seed Data Script

**File:** `Backend/CodyWar/src/main/resources/data.sql` (or a `DataSeeder` component)

**What to do:**

- Create a `@Component` class that runs on startup (`@PostConstruct` or `ApplicationRunner`)
- Only seed if the database is empty (check `problemRepository.count() == 0`)
- Create 5-10 sample problems with 3-5 test cases each
- Create 2 test users (if they don't exist)

**Why:** Manual testing requires problems to exist. Seed data provides a consistent starting state.

### Step 16.2 — Manual End-to-End Test Script

Document and execute this manual test flow:

1. Start PostgreSQL, Redis, Docker
2. Start the backend: `.\mvnw.cmd spring-boot:run`
3. Start the frontend: `cd Frontend && npm run dev`
4. **Test Auth:** Register two users in two different browser tabs
5. **Test Battle Creation:** User 1 creates a private battle, copies room code
6. **Test Battle Join:** User 2 joins using room code — verify both see each other in WebSocket
7. **Test Ready:** Both users mark ready — verify battle starts with countdown
8. **Test Code Submission:** User 1 submits correct solution — verify:
   - Submission appears as PENDING then JUDGED
   - User 2 sees opponent submission notification
   - Winner announcement appears
   - Battle ends and ratings update
9. **Test Matchmaking:** Both users use Quick Match — verify they are matched
10. **Test Edge Cases:** Submit wrong answer, TLE solution, compilation error

### Step 16.3 — Run All Backend Tests

```powershell
cd Backend\CodyWar
.\mvnw.cmd clean test
```

Fix any remaining failures.

---

## 19. Phase 17 — Deployment

> **Goal:** Containerize the entire application and prepare for production deployment.

### Step 17.1 — Create Backend Dockerfile

**File:** `Backend/CodyWar/Dockerfile`

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Step 17.2 — Create Frontend Dockerfile

**File:** `Frontend/Dockerfile`

```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### Step 17.3 — Create `docker-compose.yml`

**File:** `docker-compose.yml` (workspace root)

**What to do:**

- Define services: `backend`, `frontend`, `postgres`, `redis`
- Backend depends on postgres and redis
- Frontend depends on backend
- Map environment variables
- Persist postgres data with a volume
- Backend needs Docker socket mounted (`/var/run/docker.sock`) for code execution

### Step 17.4 — Create Nginx Configuration

**File:** `Frontend/nginx.conf`

**What to do:**

- Serve static files from `/usr/share/nginx/html`
- Proxy `/api/` to `http://backend:8000/api/`
- Proxy `/ws/` to `http://backend:8000/ws/` with WebSocket upgrade headers
- Fallback all other routes to `index.html` (SPA routing)

---

## Appendix A — File Inventory (What Exists)

### Source Files — `src/main/java/com/gourav/CodyWar/`

```
CodyWarApplication.java                    ✅ @SpringBootApplication, @EnableScheduling

Configuration/
├── GlobalExceptionHandler.java            ✅ Handles validation, auth, generic exceptions
├── RedisConfig.java                       ✅ RedisTemplate + JSON serializer
├── SecurityConfig.java                    ✅ JWT stateless, CORS needed
└── WebSocketConfig.java                   ✅ STOMP /ws/battle, JWT interceptor

Controller/
├── AuthController.java                    ✅ register, login, refresh, me
├── BattleController.java                  ✅ create, join, matchmaking, ready, leave, get
├── BattleWebSocketController.java         ✅ STOMP join, ready, leave, heartbeat, state
└── ProblemController.java                 ✅ CRUD (admin-only writes)

Domain/
├── Dto/
│   ├── ApiResponse.java                   ✅ Generic wrapper
│   ├── AuthResponse.java                  ✅
│   ├── BattleEvent.java                   ✅ 14 event types
│   ├── BattleResponseDto.java             ✅ with ParticipantDto
│   ├── BattleState.java                   ✅ Redis state, Serializable
│   ├── CreateBattleRequest.java           ✅
│   ├── JoinBattleRequest.java             ✅
│   ├── LoginRequest.java                  ✅
│   ├── MatchmakingEntry.java              ✅
│   ├── ProblemRequestDto.java             ✅ (needs test case list)
│   ├── ProblemResponseDto.java            ✅ (needs test case list)
│   ├── RefreshTokenRequest.java           ✅
│   ├── RegisterRequest.java              ✅
│   └── UserDto.java                       ✅
└── Entity/
    ├── Battle.java                        ✅
    ├── BattleParticipant.java             ✅
    ├── BattleStatus.java                  ✅ WAITING, READY, IN_PROGRESS, COMPLETED, CANCELLED
    ├── Difficulty.java                    ✅ EASY, MEDIUM, HARD
    ├── Language.java                      ✅ 11 languages
    ├── Problem.java                       ✅
    ├── Role.java                          ✅ PLAYER, ADMIN
    ├── Status.java                        ✅
    ├── Submission.java                    ✅
    ├── SubmissionStatus.java              ✅ 8 statuses
    ├── TestCase.java                      ✅
    └── User.java                          ✅

Repository/
├── BattleParticipantRepository.java       ✅
├── BattleRepository.java                  ✅
├── ProblemRepository.java                 ✅
└── UserRepository.java                    ✅

Security/
├── CustomUserDetails.java                 ✅
├── CustomUserDetailsService.java          ✅
├── JwtAuthenticationFilter.java           ✅
└── JwtUtil.java                           ✅

Service/
├── AuthService.java                       ✅
├── BattleEventPublisher.java              ✅ Redis Pub/Sub
├── BattleService.java                     ✅ Full battle lifecycle
└── ProblemService.java                    ✅ CRUD
```

### Files That Need To Be Created

```
Repository/
├── TestCaseRepository.java                ❌ Phase 1
└── SubmissionRepository.java              ❌ Phase 2

Domain/Dto/
├── TestCaseDto.java                       ❌ Phase 1
├── SubmitCodeRequest.java                 ❌ Phase 2
├── SubmissionResponseDto.java             ❌ Phase 2
└── ExecutionResult.java                   ❌ Phase 2

Service/
├── CodeExecutionService.java              ❌ Phase 4
├── SubmissionService.java                 ❌ Phase 5
└── RatingService.java                     ❌ Phase 8

Controller/
└── SubmissionController.java              ❌ Phase 6

Configuration/
└── AsyncConfig.java                       ❌ Phase 5

docker/images/                             ❌ Phase 3
Frontend/                                  ❌ Phase 10-15
```

---

## Appendix B — API Contract Summary

### Existing Endpoints

| Method | Path                       | Auth  | Description               |
| ------ | -------------------------- | ----- | ------------------------- |
| POST   | `/api/auth/register`       | No    | Register new user         |
| POST   | `/api/auth/login`          | No    | Login, returns JWT        |
| POST   | `/api/auth/refresh`        | No    | Refresh access token      |
| GET    | `/api/auth/me`             | Yes   | Get current user info     |
| POST   | `/api/problems`            | Admin | Create problem            |
| GET    | `/api/problems`            | Yes   | List all problems         |
| GET    | `/api/problems/{id}`       | Yes   | Get problem by ID         |
| PUT    | `/api/problems/{id}`       | Admin | Update problem            |
| DELETE | `/api/problems/{id}`       | Admin | Delete problem            |
| POST   | `/api/battles`             | Yes   | Create battle             |
| POST   | `/api/battles/join`        | Yes   | Join by room code         |
| POST   | `/api/battles/{id}/join`   | Yes   | Join by battle ID         |
| POST   | `/api/battles/matchmaking` | Yes   | Find or queue match       |
| DELETE | `/api/battles/matchmaking` | Yes   | Cancel matchmaking        |
| POST   | `/api/battles/{id}/ready`  | Yes   | Set ready status          |
| POST   | `/api/battles/{id}/leave`  | Yes   | Leave battle              |
| GET    | `/api/battles/{id}`        | Yes   | Get battle details        |
| GET    | `/api/battles/{id}/state`  | Yes   | Get Redis battle state    |
| GET    | `/api/battles/my-battles`  | Yes   | Get user's battles        |
| GET    | `/api/battles/active`      | Yes   | Get active public battles |

### New Endpoints (To Be Created)

| Method | Path                                 | Auth     | Phase | Description               |
| ------ | ------------------------------------ | -------- | ----- | ------------------------- |
| POST   | `/api/submissions`                   | Yes      | 6     | Submit code for a battle  |
| GET    | `/api/submissions/{id}`              | Yes      | 6     | Get submission details    |
| GET    | `/api/submissions/battle/{battleId}` | Yes      | 6     | Get battle submissions    |
| GET    | `/api/submissions/my-submissions`    | Yes      | 6     | Get user's submissions    |
| GET    | `/api/leaderboard`                   | Optional | 8     | Get top players by rating |

### WebSocket Topics

| Destination                  | Direction        | Description              |
| ---------------------------- | ---------------- | ------------------------ |
| `/app/battle/{id}/join`      | Client → Server  | Join battle room         |
| `/app/battle/{id}/ready`     | Client → Server  | Set ready status         |
| `/app/battle/{id}/leave`     | Client → Server  | Leave battle room        |
| `/app/battle/{id}/heartbeat` | Client → Server  | Keep-alive ping          |
| `/app/battle/{id}/state`     | Client → Server  | Request current state    |
| `/app/battle/{id}/submit`    | Client → Server  | Submit code (Phase 7)    |
| `/topic/battle/{id}`         | Server → Clients | Broadcast battle events  |
| `/user/queue/errors`         | Server → Client  | Personal error messages  |
| `/user/queue/battle-state`   | Server → Client  | Personal state responses |
| `/user/queue/heartbeat`      | Server → Client  | Heartbeat response       |

---

## Execution Order Summary

| Phase | Dependency  | Estimated Effort | Description                     |
| ----- | ----------- | ---------------- | ------------------------------- |
| 0     | None        | 1 hour           | Environment setup               |
| 1     | Phase 0     | 2-3 hours        | Test case management            |
| 2     | Phase 0     | 2 hours          | Submission data layer           |
| 3     | Phase 0     | 3-4 hours        | Docker images                   |
| 4     | Phases 2, 3 | 4-6 hours        | Code execution service          |
| 5     | Phases 1, 4 | 4-6 hours        | Submission service              |
| 6     | Phase 5     | 2 hours          | Submission controller           |
| 7     | Phase 6     | 2-3 hours        | Battle ↔ submission integration |
| 8     | Phase 7     | 2-3 hours        | Rating system                   |
| 9     | Phase 8     | 3-4 hours        | Backend hardening & tests       |
| 10    | Phase 0     | 2-3 hours        | Frontend scaffolding            |
| 11    | Phase 10    | 3-4 hours        | Frontend auth                   |
| 12    | Phase 11    | 4-5 hours        | Frontend dashboard              |
| 13    | Phase 12    | 4-5 hours        | Frontend battle lobby           |
| 14    | Phase 13    | 6-8 hours        | Frontend code editor            |
| 15    | Phase 14    | 3-4 hours        | Frontend WebSocket polish       |
| 16    | All         | 4-6 hours        | End-to-end testing              |
| 17    | Phase 16    | 3-4 hours        | Deployment                      |

**Total Estimated Effort:** ~55-75 hours

> **Note:** Phases 1-3 can be worked on in parallel. Phases 10-12 can begin as soon as Phase 0 is complete (they only need the existing API). The critical path is: Phase 0 → 3 → 4 → 5 → 6 → 7 → 16.

---

_Document generated based on full codebase audit of the CodeClash project. All file paths are relative to `Backend/CodyWar/src/main/java/com/gourav/CodyWar/` unless otherwise specified._
