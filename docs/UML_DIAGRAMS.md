# Unified Modeling Language (UML) Specification
## Project: Kwizz — Real-Time Interactive Quiz System

This document contains the complete visual and structural architecture models for the **Kwizz** platform, specified using standard UML notation rendered via Mermaid.

---

## Table of Contents
1. [Use Case Diagram](#1-use-case-diagram)
2. [Domain Class Diagram](#2-domain-class-diagram)
3. [Component & Architecture Diagram](#3-component--architecture-diagram)
4. [Sequence Diagrams](#4-sequence-diagrams)
   - 4.1 Participant Join & Lobby Synchronization
   - 4.2 Question Dispatch, Answer Submission & Scoring
   - 4.3 Automated Timeout & Leaderboard Reveal
5. [State Machine Diagram (Session Lifecycle)](#5-state-machine-diagram-session-lifecycle)
6. [Activity Diagram (Gameplay Loop)](#6-activity-diagram-gameplay-loop)
7. [Entity-Relationship (ER) Diagram](#7-entity-relationship-er-diagram)
8. [Deployment Diagram (Docker Architecture)](#8-deployment-diagram-docker-architecture)

---

## 1. Use Case Diagram

The Use Case model defines the functional interactions between primary actors (**Quiz Host**, **Participant**, and **System Timer**) and the application.

```mermaid
flowchart LR
    subgraph Actors
        Host["Host (Educator)"]
        Participant["Participant (Student)"]
        Timer["System Scheduler (Timer)"]
    end

    subgraph "Kwizz System Boundary"
        UC1(["Register & Authenticate"])
        UC2(["Create / Edit / Delete Quiz"])
        UC3(["Add / Remove Questions"])
        UC4(["Launch Live Session"])
        UC5(["Display QR Code & Join Code"])
        UC6(["Join Session (Nickname & Code)"])
        UC7(["Broadcast Roster Over WebSocket"])
        UC8(["Advance Question / Start Quiz"])
        UC9(["Synchronize Real-time Countdown"])
        UC10(["Submit Answer Option"])
        UC11(["Compute Score & Time Bonus"])
        UC12(["Broadcast Leaderboard"])
        UC13(["Auto-trigger Timeout Reveal"])
        UC14(["Stop / Finish Session"])
    end

    Host --> UC1
    Host --> UC2
    Host --> UC3
    Host --> UC4
    Host --> UC5
    Host --> UC8
    Host --> UC14

    Participant --> UC6
    Participant --> UC10

    Timer --> UC13
    Timer --> UC9

    UC4 ..> UC5 : <<include>>
    UC6 ..> UC7 : <<include>>
    UC8 ..> UC9 : <<include>>
    UC10 ..> UC11 : <<include>>
    UC13 ..> UC12 : <<include>>
    UC8 ..> UC12 : <<include>>
```

---

## 2. Domain Class Diagram

This diagram captures the core entities, business services, controllers, data transfer objects, and their relationships within the Spring Boot application.

```mermaid
classDiagram
    direction TB

    %% Entities
    class Host {
        -Long id
        -String username
        -String password
        -List~Quiz~ quizzes
        +getId() Long
        +getUsername() String
        +getPassword() String
        +getQuizzes() List~Quiz~
    }

    class Quiz {
        -Long id
        -String title
        -String description
        -Host host
        -List~Question~ questions
        +getId() Long
        +getTitle() String
        +getDescription() String
        +getHost() Host
        +getQuestions() List~Question~
    }

    class Question {
        -Long id
        -String text
        -String optionA
        -String optionB
        -String optionC
        -String optionD
        -int correctOption
        -int timeLimitSeconds
        -Quiz quiz
        -List~Score~ scores
        +getId() Long
        +getText() String
        +getCorrectOption() int
        +getTimeLimitSeconds() int
    }

    class SessionStatus {
        <<enumeration>>
        WAITING
        IN_PROGRESS
        FINISHED
    }

    class QuizSession {
        -Long id
        -Quiz quiz
        -String joinCode
        -SessionStatus status
        -int currentQuestionIndex
        -LocalDateTime createdAt
        -List~Participant~ participants
        +getId() Long
        +getJoinCode() String
        +getStatus() SessionStatus
        +getCurrentQuestionIndex() int
    }

    class Participant {
        -Long id
        -String nickname
        -String sessionToken
        -QuizSession session
        -List~Score~ scores
        +getId() Long
        +getNickname() String
        +getSessionToken() String
        +getSession() QuizSession
    }

    class Score {
        -Long id
        -Participant participant
        -Question question
        -int selectedOption
        -boolean correct
        -int pointsAwarded
        -long answerTimeMs
        +isCorrect() boolean
        +getPointsAwarded() int
        +getAnswerTimeMs() long
    }

    %% DTOs
    class QuestionMessage {
        -Long questionId
        -String text
        -String optionA
        -String optionB
        -String optionC
        -String optionD
        -int timeLimitSeconds
        -int questionNumber
        -int totalQuestions
    }

    class AnswerMessage {
        -Long questionId
        -String participantToken
        -int selectedOption
        -int answerTimeMs
    }

    class LeaderboardMessage {
        -List~LeaderboardEntry~ entries
    }

    class LeaderboardEntry {
        -String nickname
        -int score
    }

    %% Service Layer
    class QuizGameManager {
        -SessionService sessionService
        -ScoreService scoreService
        -SimpMessagingTemplate messagingTemplate
        -ScheduledExecutorService scheduler
        -Map pendingTimers
        -Map showingLeaderboard
        +handleNextClick(Long sessionId)
        +stopQuiz(Long sessionId)
        -doAdvance(Long sessionId)
        -revealLeaderboard(Long sessionId)
        -onQuestionTimeout(Long sessionId)
    }

    class SessionService {
        -QuizSessionRepository sessionRepo
        -QuizRepository quizRepo
        -SimpMessagingTemplate messagingTemplate
        +startSession(Long quizId) QuizSession
        +getSession(Long sessionId) QuizSession
        +nextQuestion(Long sessionId) QuizSession
        +stopSession(Long sessionId) QuizSession
        +broadcastCurrentState(Long sessionId, QuizSession session)
    }

    class ScoreService {
        -ScoreRepository scoreRepository
        -QuestionRepository questionRepository
        -ParticipantRepository participantRepository
        +recordAnswer(String token, Long qId, int opt, long timeMs)
        +getLeaderboard(Long sessionId) LeaderboardMessage
    }

    class ParticipantService {
        -ParticipantRepository participantRepository
        -QuizSessionRepository sessionRepository
        +joinSession(String code, String nickname) Participant
        +getByToken(String token) Participant
        +getNickNames(Long sessionId) List~String~
    }

    class QuizService {
        -QuizRepository quizRepository
        -HostRepository hostRepository
        +createQuiz(String title, String desc, Long hostId) Quiz
        +getQuiz(Long quizId, Host host) Quiz
        +addQuestion(Long quizId, Question question, Host host)
        +deleteQuiz(Long quizId, Host host)
    }

    %% Relationships
    Host "1" *-- "0..*" Quiz : owns
    Quiz "1" *-- "0..*" Question : contains
    Quiz "1" <-- "0..*" QuizSession : instantiates
    QuizSession "1" *-- "0..*" Participant : registers
    Participant "1" *-- "0..*" Score : achieves
    Question "1" <-- "0..*" Score : evaluates

    QuizGameManager ..> SessionService : orchestrates
    QuizGameManager ..> ScoreService : calculates
    QuizGameManager ..> QuestionMessage : dispatches
    QuizGameManager ..> LeaderboardMessage : broadcasts
    ScoreService ..> AnswerMessage : consumes
    ScoreService ..> LeaderboardEntry : aggregates
    ParticipantService ..> Participant : manages
    SessionService ..> QuizSession : manages
    QuizService ..> Quiz : manages
```

---

## 3. Component & Architecture Diagram

This diagram displays the multi-tier logical architecture of Kwizz, highlighting Spring Security, Web MVC, the In-Memory STOMP Broker, Services, and Data Access.

```mermaid
flowchart TD
    subgraph ClientLayer ["Client Presentation Layer"]
        HostBrowser["Host Web Browser (Desktop Display)"]
        PlayerBrowser["Participant Mobile / Desktop Browsers"]
    end

    subgraph SecurityLayer ["Security & Routing Layer"]
        SecFilter["Spring Security Filter Chain (BCrypt, SessionAuth, CSRF)"]
        SockJSEndpoint["STOMP / SockJS Endpoint (/ws)"]
    end

    subgraph ControllerLayer ["Spring Web & Messaging Controllers"]
        AuthController["AuthController (/login, /register)"]
        QuizController["QuizController (/quizzes/**)"]
        SessionController["SessionController (/sessions/**)"]
        JoinController["JoinController (/join, /play/**)"]
        WSHandler["STOMP Message Router (/app/**)"]
    end

    subgraph ServiceLayer ["Core Business Logic Layer"]
        HostService["HostService & HostUserDetailsService"]
        QuizService["QuizService"]
        SessionService["SessionService"]
        ParticipantService["ParticipantService"]
        ScoreService["ScoreService"]
        GameManager["QuizGameManager (ScheduledExecutorService)"]
        ZXing["Google ZXing QR Engine"]
    end

    subgraph BrokerLayer ["Real-Time Messaging Broker"]
        SimpleBroker["Simple In-Memory STOMP Broker (/topic)"]
    end

    subgraph DataLayer ["Data Access & Persistence"]
        JPARepos["Spring Data JPA Repositories"]
        Hibernate["Hibernate 6.5 ORM"]
        Postgres[(PostgreSQL 16 Database)]
    end

    HostBrowser -->|HTTP GET/POST| SecFilter
    PlayerBrowser -->|HTTP GET/POST| SecFilter
    HostBrowser <-->|WebSocket STOMP| SockJSEndpoint
    PlayerBrowser <-->|WebSocket STOMP| SockJSEndpoint

    SecFilter --> AuthController
    SecFilter --> QuizController
    SecFilter --> SessionController
    SecFilter --> JoinController
    SockJSEndpoint --> WSHandler

    AuthController --> HostService
    QuizController --> QuizService
    SessionController --> SessionService
    SessionController --> GameManager
    SessionController --> ZXing
    JoinController --> ParticipantService
    JoinController --> SessionService
    WSHandler --> ScoreService

    GameManager --> SessionService
    GameManager --> ScoreService
    GameManager --> SimpleBroker
    SessionService --> SimpleBroker
    JoinController --> SimpleBroker

    SimpleBroker -.->|Push Notifications| HostBrowser
    SimpleBroker -.->|Push Notifications| PlayerBrowser

    HostService --> JPARepos
    QuizService --> JPARepos
    SessionService --> JPARepos
    ParticipantService --> JPARepos
    ScoreService --> JPARepos

    JPARepos --> Hibernate
    Hibernate --> Postgres
```

---

## 4. Sequence Diagrams

### 4.1 Participant Join & Lobby Synchronization
Illustrates a participant joining an active session via room code/QR and the real-time notification pushed to the host display.

```mermaid
sequenceDiagram
    autonumber
    actor Player as Participant (Browser)
    participant JoinCtrl as JoinController
    participant PartSvc as ParticipantService
    participant SessSvc as SessionService
    participant Broker as STOMP Broker (/topic)
    actor Host as Host (Big Screen)

    Host->>SessSvc: Creates session & displays join code/QR
    Player->>JoinCtrl: POST /join (code, nickname)
    JoinCtrl->>PartSvc: joinSession(code, nickname)
    PartSvc->>PartSvc: Validate session == WAITING
    PartSvc->>PartSvc: Check nickname unique in session
    PartSvc->>PartSvc: Generate UUID sessionToken & Save
    PartSvc-->>JoinCtrl: Participant object returned
    JoinCtrl->>JoinCtrl: Store token in HttpSession
    JoinCtrl->>Broker: convertAndSend(/topic/session/{id}/participants, roster)
    Broker-->>Host: Broadcast updated roster & count
    Broker-->>Player: Broadcast updated roster & count
    JoinCtrl-->>Player: Redirect to /play/{sessionId}
    Player->>Player: Connect to WebSocket (/ws) & wait for game start
```

---

### 4.2 Question Dispatch, Answer Submission & Scoring
Shows the host advancing to a question, the server broadcasting the question without the answer key, participants submitting answers, and real-time point calculation.

```mermaid
sequenceDiagram
    autonumber
    actor Host as Host
    participant GameManager as QuizGameManager
    participant SessSvc as SessionService
    participant Broker as STOMP Broker (/topic)
    actor Player as Participant
    participant SessCtrl as SessionController
    participant ScoreSvc as ScoreService
    participant DB as PostgreSQL

    Host->>GameManager: POST /sessions/{id}/next (Host clicks Next)
    GameManager->>SessSvc: nextQuestion(sessionId)
    SessSvc->>DB: Update currentQuestionIndex, status = IN_PROGRESS
    SessSvc->>Broker: Broadcast QuestionMessage on /topic/session/{id}/question
    Broker-->>Host: Display question text & choices A/B/C/D
    Broker-->>Player: Render question & start local countdown timer

    GameManager->>GameManager: Schedule timer: onQuestionTimeout() in question.timeLimitSeconds

    Player->>Player: Clicks option tile (e.g. Option B)
    Player->>SessCtrl: STOMP SEND /app/session/{id}/answer (AnswerMessage)
    SessCtrl->>ScoreSvc: recordAnswer(token, questionId, opt, timeTakenMs)
    ScoreSvc->>DB: Check if participant already answered
    ScoreSvc->>DB: Compare selectedOption with question.correctOption
    ScoreSvc->>ScoreSvc: Compute points: 100 + max(0, timeLimitMs - timeTakenMs) / 100
    ScoreSvc->>DB: Save Score entity
```

---

### 4.3 Automated Timeout & Leaderboard Reveal
Details the countdown timer expiration handling, asynchronous cancellation, and leaderboard broadcasting.

```mermaid
sequenceDiagram
    autonumber
    participant Scheduler as ScheduledExecutorService
    participant GameManager as QuizGameManager
    participant ScoreSvc as ScoreService
    participant Broker as STOMP Broker (/topic)
    actor Host as Host Screen
    actor Player as Participant Screen

    Note over Scheduler,GameManager: Time limit expires (e.g., 20 seconds elapsed)
    Scheduler->>GameManager: onQuestionTimeout(sessionId)
    GameManager->>GameManager: cancelPendingTimer(sessionId)
    GameManager->>ScoreSvc: getLeaderboard(sessionId)
    ScoreSvc->>ScoreSvc: Aggregate total scores per participant & sort descending
    ScoreSvc-->>GameManager: LeaderboardMessage(entries)
    GameManager->>Broker: convertAndSend(/topic/session/{id}/leaderboard, LeaderboardMessage)
    GameManager->>GameManager: Mark showingLeaderboard = true

    Broker-->>Host: Render podium & ranked standings
    Broker-->>Player: Swap screen to leaderboard view, highlight personal score
```

---

## 5. State Machine Diagram (Session Lifecycle)

Models the discrete operational states of a `QuizSession` throughout its execution.

```mermaid
stateDiagram-v2
    [*] --> WAITING : Host launches session (generate joinCode & QR)

    state WAITING {
        [*] --> AcceptingPlayers
        AcceptingPlayers --> AcceptingPlayers : Participant joins (roster broadcast)
    }

    WAITING --> IN_PROGRESS : Host clicks "Start Quiz" (index = 0)

    state IN_PROGRESS {
        [*] --> QuestionActive : Broadcast QuestionMessage
        
        state QuestionActive {
            [*] --> CountdownRunning
            CountdownRunning --> AnswerCollection : Participants submit choices
            AnswerCollection --> CountdownRunning
        }

        QuestionActive --> LeaderboardFrozen : Timer expires OR Host clicks Next
        
        state LeaderboardFrozen {
            [*] --> StandingsBroadcasted : Broadcast LeaderboardMessage
            StandingsBroadcasted --> IdleWaitForHost : Await host manual trigger
        }

        LeaderboardFrozen --> QuestionActive : Host clicks "Next Question" (index++)
    }

    IN_PROGRESS --> FINISHED : Last question completed OR Host clicks "Stop Quiz"
    WAITING --> FINISHED : Host terminates session before start

    state FINISHED {
        [*] --> FinalStandings
        FinalStandings --> CleanupReady : Host exits session
    }

    FINISHED --> [*]
```

---

## 6. Activity Diagram (Gameplay Loop)

Tracks the operational workflows of the Host and Participant swimming lanes in parallel.

```mermaid
flowchart TD
    subgraph HostLane ["Host Activity"]
        H1([Log in to Host Dashboard]) --> H2[Create Quiz & Add Questions]
        H2 --> H3[Click 'Start Session']
        H3 --> H4[Project Lobby Screen with QR Code]
        H4 --> H5{Sufficient Players Joined?}
        H5 -- No --> H4
        H5 -- Yes --> H6[Click 'Start Quiz']
        H6 --> H7[Display Current Question & Timer]
        H7 --> H8{Timer Expired OR Host Clicked Next?}
        H8 -- Yes --> H9[Display Leaderboard Podium]
        H9 --> H10{More Questions in Quiz?}
        H10 -- Yes --> H11[Click 'Next Question']
        H11 --> H7
        H10 -- No --> H12[Display Final Winner Standings]
        H12 --> H13([Stop Session & Review])
    end

    subgraph PlayerLane ["Participant Activity"]
        P1([Scan QR or Navigate to /join]) --> P2[Enter Join Code & Nickname]
        P2 --> P3[Wait in Lobby]
        P3 --> P4[Receive Question via STOMP]
        P4 --> P5[View Question & Select Color Tile A/B/C/D]
        P5 --> P6[Lock Selection & Submit via WebSocket]
        P6 --> P7[Wait for Round Conclusion]
        P7 --> P8[Receive Leaderboard & View Rank]
        P8 --> P9{Session Finished?}
        P9 -- No --> P4
        P9 -- Yes --> P10([View Final Placement & Exit])
    end

    H4 -.->|Shares Code/QR| P1
    H6 -.->|Triggers Round| P4
    P6 -.->|Submits Answer| H8
    H9 -.->|Broadcasts Rank| P8
```

---

## 7. Entity-Relationship (ER) Diagram

Defines the exact PostgreSQL physical database schema, tables, column types, and relational foreign-key cardinalities.

```mermaid
erDiagram
    HOSTS ||--o{ QUIZZES : "owns (1:N)"
    QUIZZES ||--o{ QUESTIONS : "contains (1:N)"
    QUIZZES ||--o{ QUIZ_SESSIONS : "generates (1:N)"
    QUIZ_SESSIONS ||--o{ PARTICIPANTS : "enrolls (1:N)"
    PARTICIPANTS ||--o{ SCORES : "earns (1:N)"
    QUESTIONS ||--o{ SCORES : "evaluated_by (1:N)"

    HOSTS {
        bigserial id PK
        varchar(255) username UK "NOT NULL"
        varchar(255) password "NOT NULL (BCrypt)"
    }

    QUIZZES {
        bigserial id PK
        varchar(255) title "NOT NULL"
        text description
        bigint host_id FK "NOT NULL"
    }

    QUESTIONS {
        bigserial id PK
        bigint quiz_id FK "NOT NULL"
        varchar(500) text "NOT NULL"
        varchar(255) option_a
        varchar(255) option_b
        varchar(255) option_c
        varchar(255) option_d
        integer correct_option "NOT NULL (0-3)"
        integer time_limit_seconds "NOT NULL (Default: 20)"
        integer question_order "Hibernate Order Index"
    }

    QUIZ_SESSIONS {
        bigserial id PK
        bigint quiz_id FK "NOT NULL"
        varchar(8) join_code UK "NOT NULL"
        varchar(32) status "NOT NULL (WAITING, IN_PROGRESS, FINISHED)"
        integer current_question_index "NOT NULL"
        timestamp created_at "NOT NULL"
    }

    PARTICIPANTS {
        bigserial id PK
        bigint session_id FK "NOT NULL"
        varchar(255) nickname "NOT NULL"
        varchar(255) session_token UK "NOT NULL (UUID)"
    }

    SCORES {
        bigserial id PK
        bigint participant_id FK "NOT NULL"
        bigint question_id FK "NOT NULL"
        integer selected_option "NOT NULL (-1 to 3)"
        boolean correct "NOT NULL"
        integer points_awarded "NOT NULL"
        bigint answer_time_ms "NOT NULL"
    }
```

---

## 8. Deployment Diagram (Docker Architecture)

Illustrates the physical deployment node topology inside a Dockerized container environment.

```mermaid
flowchart TB
    subgraph ClientNodes ["Client Devices"]
        Desktop["Teacher Workstation (Host Display)<br/>Modern Web Browser"]
        Mobiles["Student Smartphones / Laptops (Participants)<br/>Mobile Browsers (iOS / Android / Chrome)"]
    end

    subgraph DockerHost ["Docker Container Host (Linux / Cloud VM)"]
        subgraph BridgeNet ["Docker Bridge Network: kwizz-network"]
            subgraph AppContainer ["Docker Container: kwizz-app"]
                direction TB
                Tomcat["Embedded Apache Tomcat 10<br/>Port: 8080"]
                JRE["Eclipse Temurin OpenJDK 21 JRE"]
                SpringApp["Kwizz Spring Boot Application (kwizz-0.0.1-SNAPSHOT.jar)"]
                Tomcat --- SpringApp
                JRE --- SpringApp
            end

            subgraph DBContainer ["Docker Container: kwizz-db"]
                PostgresEngine["PostgreSQL 16 Engine<br/>Port: 5432"]
                PGData[("Named Volume: pgdata<br/>/var/lib/postgresql/data")]
                PostgresEngine --- PGData
            end
        end
    end

    Desktop -->|HTTP:8080 / WS:8080| Tomcat
    Mobiles -->|HTTP:8080 / WS:8080| Tomcat
    SpringApp -->|JDBC:5432 (Internal Network)| PostgresEngine
```
