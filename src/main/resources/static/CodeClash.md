# Project Synopsis and Implementation Strategy: CodeClash

## Project Synopsis: CodeClash

CodeClash is a real-time, competitive coding platform designed to pit two or more users against each other in a synchronous battle environment. Users join a room, receive the same algorithmic problem, and race to submit a correct and efficient solution using an in-browser code editor.

The winner is determined by a combination of:

* Solution correctness (passing all test cases)
* Speed (time taken for the first correct submission)

The project's core challenge and innovation lie in its secure, scalable **Code Execution System**, which utilizes isolated Docker containers to safely run user-submitted code against predefined test cases. This mitigates the significant security risks associated with executing arbitrary third-party code directly on the server.

The platform leverages a modern, Java-focused tech stack:

* Spring Boot for the backend
* React with Monaco Editor for the frontend
* Redis for real-time battle state and synchronization via WebSockets

---

## Key Metrics

| Metric             | Description                                                                  |
| ------------------ | ---------------------------------------------------------------------------- |
| Core Goal          | Real-time competitive coding platform (1v1 or multi-player)                  |
| Key Differentiator | Secure, isolated code execution using Docker sandboxing                      |
| Success Criteria   | Fast, reliable submission judging; real-time synchronization of battle state |
| MVP Timeline       | ~4 weeks                                                                     |

---

# Research and Implementation Strategy

## 1. High-Level Architecture and Flow

The implementation follows a high-level architecture focused on service decoupling and secure boundaries.

### A. Battle Lifecycle

1. **Matchmaking**

    * User requests a battle.
    * Spring Boot backend checks Redis for available public rooms or private room codes.
    * Auto-match uses a basic queue/priority system managed in memory or Redis.

2. **Room Setup**

    * When a match is found or a private room is created, a new battle state is initialized in Redis.
    * Stored data includes:

        * battleId
        * problemId
        * playerIds
        * startTime
        * current status

3. **Real-Time Sync (WebSockets)**

    * Backend establishes WebSocket connections with all players in the room.
    * Broadcasts `TIMER_START` event and initial battle data.
    * Frontend listens continuously for opponent status and timer updates.

4. **Submission**

    * User writes code and submits.
    * Submission is sent via REST endpoint to the Spring Boot backend.

5. **Code Execution (Sandbox)**

    * Backend coordinates execution:

        * Saves code to a temporary file
        * Communicates with Code Execution Service
        * Launches pre-configured Docker container (e.g., `java-execution-image:latest`)
        * Injects and executes code against test cases inside container

6. **Judging**

    * Resource limits enforced (CPU, memory, time).
    * Captures:

        * exit code
        * standard output
        * standard error

7. **Result Update**

    * Result returned to Spring Boot backend.
    * Battle state updated in Redis.
    * Submission stored in database.
    * `SUBMISSION_UPDATE` WebSocket event broadcast to players.

8. **Winner Determination**

    * First correct submission wins.
    * `WINNER_ANNOUNCEMENT` event broadcast.

---

## 2. Research Focus and Key Technical Challenges

| Component                 | Research / Challenge                                             | Solution Approach                                                                                      |
| ------------------------- | ---------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| Code Sandbox              | Prevent malicious code, infinite loops, filesystem/network abuse | Docker containers with CPU, memory, swap limits; network disabled; timeout utility in execution script |
| WebSockets Scalability    | Manage thousands of connections and fast state updates           | Spring Boot WebSockets + Redis Pub/Sub for scaling across instances                                    |
| Monaco Editor Integration | Multi-language syntax highlighting and autocomplete              | Use `react-monaco-editor`; configure language model on component mount                                 |
| Resource Limits           | Fair time and memory enforcement                                 | Docker limits + in-container tools like `time` and `ulimit`                                            |

---

## 3. Required Tools and Integration Plan

### A. Core Technologies

| Tool / Technology      | Role                                       | Integration Point                                              |
| ---------------------- | ------------------------------------------ | -------------------------------------------------------------- |
| Java 17+ / Spring Boot | Backend API, core logic, WebSocket hub     | Handles authentication, battle logic, submission orchestration |
| React / Tailwind       | Frontend UI                                | Consumes REST APIs and WebSocket updates                       |
| Monaco Editor          | Live coding editor                         | React component inside battle room                             |
| Redis                  | Real-time state, timers, matchmaking queue | Used via Spring Data Redis                                     |
| MySQL / PostgreSQL     | Persistent storage                         | Used with Spring Data JPA                                      |

---

### B. Code Execution Sandbox Tools

| Tool / Technology | Role                      | Integration Point                                       |
| ----------------- | ------------------------- | ------------------------------------------------------- |
| Docker            | Secure isolated execution | Backend uses Docker client library to manage containers |
| Bash Scripts      | In-container orchestrator | Handles compile, run, timeout, output capture           |
| Test Case Runner  | Output comparison logic   | Implemented in backend or execution service             |

Example container entry flow:

```bash
javac Code.java
timeout 5s java Code
```

---

## 4. Integration Strategy: Decoupling the Execution Service

The Code Execution System is the most complex component and should be isolated.

### Dedicated Execution Service

* MVP: Docker calls can be made from main Spring Boot app.
* Long-term: Separate microservice (ExecutorService) dedicated to execution.

### API Contract

**Request:**

* problemId
* userId
* language
* codeString

**Response:**

* submissionId
* status (ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED)
* executionTime
* memoryUsed

### Asynchronous Handling

* Submission API returns immediately.
* Execution runs asynchronously.
* Final result pushed back to main backend.
* Backend broadcasts result via WebSocket.
* Prevents HTTP timeout and improves reliability.
