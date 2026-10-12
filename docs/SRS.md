# Software Requirements Specification (SRS)
## Project: Kwizz — Real-Time Interactive Classroom Quiz Platform

**Document Version:** 1.0.0  
**Status:** Approved  
**Prepared by:** Karmveer  
**Standard:** IEEE Std 830-1998 / ISO/IEC/IEEE 29148:2018 Compliant  
**Date:** October 2026  

---

## Table of Contents
1. [Introduction](#1-introduction)
   - 1.1 Purpose
   - 1.2 Document Conventions
   - 1.3 Intended Audience & Reading Suggestions
   - 1.4 Project Scope
   - 1.5 References
2. [Overall Description](#2-overall-description)
   - 2.1 Product Perspective
   - 2.2 Product Functions
   - 2.3 User Classes & Characteristics
   - 2.4 Operating Environment
   - 2.5 Design & Implementation Constraints
   - 2.6 Assumptions & Dependencies
3. [External Interface Requirements](#3-external-interface-requirements)
   - 3.1 User Interfaces
   - 3.2 Hardware Interfaces
   - 3.3 Software Interfaces
   - 3.4 Communications Interfaces (WebSocket / STOMP)
4. [System Features & Functional Requirements](#4-system-features--functional-requirements)
   - 4.1 System Feature: Host Authentication & Authorization
   - 4.2 System Feature: Quiz & Question Authoring Management
   - 4.3 System Feature: Live Session Generation & Lobby Roster
   - 4.4 System Feature: Participant Joining & Anonymous Session Management
   - 4.5 System Feature: Real-Time Game Orchestration & Question Dispatch
   - 4.6 System Feature: Real-Time Answer Processing & Dynamic Scoring
   - 4.7 System Feature: Leaderboard Aggregation & Broadcasting
   - 4.8 System Feature: Session Lifecycle & Cascading Cleanup
5. [Non-Functional Requirements](#5-non-functional-requirements)
   - 5.1 Performance Requirements
   - 5.2 Reliability & Fault Tolerance
   - 5.3 Security Requirements
   - 5.4 Usability & Accessibility Requirements
   - 5.5 Maintainability & Portability
6. [Data Requirements & Schema Specification](#6-data-requirements--schema-specification)

---

## 1. Introduction

### 1.1 Purpose
This Software Requirements Specification (SRS) describes the complete functional, non-functional, and interface requirements for **Kwizz**, a full-stack, real-time multiplayer educational quiz system. This document defines the architectural expectations, behavior under concurrent load, user personas, API/WebSocket communication contracts, and verification criteria for academic and production assessment.

### 1.2 Document Conventions
- **Requirement Identifiers:** 
  - `FR-x`: Functional Requirement
  - `NFR-x`: Non-Functional Requirement
  - `IR-x`: Interface Requirement
- **Severity Levels (RFC 2119):**
  - **SHALL / MUST**: Absolute operational requirement.
  - **SHOULD**: Highly recommended unless compelling technical constraints exist.
  - **MAY**: Optional enhancement or forward-looking capability.

### 1.3 Intended Audience & Reading Suggestions
- **Academic Evaluators & Instructors:** Review Section 2 for high-level capabilities and Section 4 for detailed functional requirements and system algorithms.
- **Software Engineers & Developers:** Review Section 3 (Interfaces), Section 4 (Real-time mechanics), and Section 6 (Database models).
- **DevOps Engineers / System Administrators:** Review Section 2.4 (Operating Environment) and Section 5.5 (Containerization & Deployment).

### 1.4 Project Scope
Kwizz is a distributed, web-based assessment platform modeled after Kahoot. It replaces passive lectures with live, competitive quizzes where the host projects questions onto a main screen while participants answer synchronously from their mobile devices or web browsers. 

The software encompasses:
- A secured Host Portal for creating, editing, and managing quizzes and individual questions.
- A live Host Control Room featuring QR code and alphanumeric room code generation, participant roster tracking, question dispatching, and dynamic leaderboard visualization.
- A zero-login, mobile-optimized Participant Client with touch-friendly color-coded answer pads and real-time countdown timers.
- A low-latency publish-subscribe STOMP/WebSocket broker orchestrating synchronized game state across all connected endpoints.

### 1.5 References
1. IEEE Std 830-1998: *Recommended Practice for Software Requirements Specifications*.
2. Spring Framework 6.1 / Spring Boot 3.3 Reference Documentation.
3. RFC 6455: *The WebSocket Protocol*.
4. STOMP Protocol Specification, Version 1.2.
5. PostgreSQL 16 Official Manual.

---

## 2. Overall Description

### 2.1 Product Perspective
Kwizz is a self-contained, multi-tier web application. It operates without dependencies on external proprietary cloud services. 

```
+--------------------------------------------------------------------------+
|                                CLIENT TIER                               |
|  +--------------------------------+   +-------------------------------+  |
|  |     Host Browser (Desktop)     |   |   Player Browser (Mobile/PC)  |  |
|  |   HTML5 / CSS / Vanilla JS     |   |    HTML5 / CSS / Vanilla JS   |  |
|  +--------------------------------+   +-------------------------------+  |
+--------------------|---------------------------------|-------------------+
                     | HTTP / WebSocket (SockJS+STOMP) |
+--------------------v---------------------------------v-------------------+
|                            APPLICATION SERVER TIER                       |
|                          Embedded Apache Tomcat 10                       |
|  +--------------------------------------------------------------------+  |
|  |                        Spring Boot 3.3.4                           |  |
|  |  +-----------------------+  +-----------------------------------+  |  |
|  |  | Spring MVC / Security |  | Spring WebSocket Message Broker   |  |  |
|  |  +-----------------------+  +-----------------------------------+  |  |
|  |  +---------------------------------------------------------------+  |  |
|  |  |  Service Layer: QuizGameManager, SessionService, ScoreService |  |  |
|  |  +---------------------------------------------------------------+  |  |
|  |  |  Data Access: Spring Data JPA 3.3 / Hibernate 6.5             |  |  |
|  |  +---------------------------------------------------------------+  |  |
|  +--------------------------------------------------------------------+  |
+------------------------------------|-------------------------------------+
                                     | JDBC
+------------------------------------v-------------------------------------+
|                               DATABASE TIER                              |
|                       PostgreSQL 16 Relational Engine                    |
+--------------------------------------------------------------------------+
```

### 2.2 Product Functions
1. **Host Account Administration:** Secure registration and credential validation using BCrypt password encryption.
2. **Quiz & Question CRUD:** Authoring quizzes with titles, descriptions, and customizable 4-choice questions with explicit time limits.
3. **Session Generation:** Instant creation of temporary game sessions with collision-resistant 6-character room codes and QR codes.
4. **Frictionless Mobile Join:** Anonymous player joining via room code or QR code scan, validating nickname uniqueness per session.
5. **Real-time Synchronized Gameplay:** Push-based question dispatch to all participants simultaneously upon host trigger.
6. **Concurrent Answer Processing:** Rapid ingestion of participant answers, measuring millisecond-level reaction times and awarding time-decay bonuses.
7. **Automated Timeout & Leaderboard Reveals:** Autonomous round termination upon time expiry via server-side scheduled executors, followed by leaderboard broadcast.
8. **Session & Quiz Deletion:** Cascading cleanup across scores, participants, sessions, questions, and quizzes.

### 2.3 User Classes & Characteristics
| User Class | Description | Technical Literacy | Privileges |
| :--- | :--- | :--- | :--- |
| **Quiz Host** | Teachers, trainers, meeting leaders who create quizzes and manage live rooms. | Moderate | Full control over owned quizzes, sessions, and game progression. |
| **Participant** | Students, audience members who join live sessions via smartphones or laptops. | Basic | Join active sessions, select answers, view personal and global standings. |
| **System Admin** | Engineers running the container or server instance. | Advanced | Configure database environment variables, port mappings, and container logs. |

### 2.4 Operating Environment
- **Operating System:** Linux (Ubuntu 22.04 LTS+, Alpine Linux), macOS, Windows 11.
- **Java Runtime:** Eclipse Temurin OpenJDK 21 (LTS) 64-bit.
- **Database Server:** PostgreSQL 15.x or 16.x.
- **Container Engine:** Docker Engine 24.x+ / Docker Compose v2.x+.
- **Client Web Browsers:** Any modern browser supporting ECMAScript 6, HTML5 WebSockets, and CSS Grid (Chrome 90+, Safari 14+, Firefox 88+, Edge 90+).

### 2.5 Design & Implementation Constraints
1. **Stateless Scalability Boundary:** Spring Session is currently memory-bounded per container instance; background timers run via Java `ScheduledExecutorService`.
2. **Fixed 4-Option Paradigm:** Questions are modeled with four distinct choices (`optionA`, `optionB`, `optionC`, `optionD`) to optimize rendering and mobile responsiveness.
3. **Security Constraints:** Participants are entirely unauthenticated (no passwords or PII stored); identity is preserved through transient HTTP session tokens and database UUIDs.
4. **Zero Frontend Framework Dependencies:** No heavy client-side frameworks (e.g., React, Angular) are used. Standard DOM manipulation and SockJS/Stomp.js ensure instant page loads even on low-end hardware.

### 2.6 Assumptions & Dependencies
- Hosts possess reliable internet/LAN connectivity to project their screen in the classroom.
- Participants possess mobile devices capable of rendering web pages and maintaining active WebSocket connections.
- Clocks across participant devices need not be synchronized, as reaction times are calculated relative to client-side question render timestamps and validated against server session bounds.

---

## 3. External Interface Requirements

### 3.1 User Interfaces
- **UI-1: Landing Page (`/`):** Clean hero section with quick-join form (Join code, Nickname) and host call-to-action buttons.
- **UI-2: Authentication Views (`/login`, `/register`):** Modern card-based forms with error flash messaging and auto-focus fields.
- **UI-3: Host Quiz Management (`/quizzes`):** Dashboard listing all quizzes owned by the logged-in host, quiz creation modal, question authoring form with option inputs, correct option radio group, and time-limit selector.
- **UI-4: Host Live Session Dashboard (`/sessions/{id}`):**
  - *Lobby Phase:* High-visibility 6-character room code, 300x300 QR code image, live player roster chips, "Start Quiz" button.
  - *Question Phase:* Live timer countdown, question text, 4 colored response previews, manual "Next Question" and "Stop Quiz" buttons.
  - *Leaderboard Phase:* Ranked list of participants showing nicknames and cumulative scores.
- **UI-5: Participant Play Screen (`/play/{sessionId}`):**
  - *Waiting Lobby:* Animated waiting prompt, participant roster.
  - *Active Question:* Synced countdown bar, 4 tactile colored buttons (Red A, Blue B, Yellow C, Green D) with instant press locking.
  - *Leaderboard View:* Dynamic podium/ranking showing the player's personal rank highlighted.

### 3.2 Hardware Interfaces
- No proprietary hardware required. The platform operates on standard commodity server architectures (x86_64, ARM64) and standard client consumer devices.

### 3.3 Software Interfaces
- **Database Interface:** Java Database Connectivity (JDBC) 4.3 over PostgreSQL Driver (`org.postgresql:postgresql:42.7.4`), configured via Spring Data JPA and Hibernate.
- **QR Code Engine:** Google ZXing 3.5.3 library directly rendering PNG byte streams to `HttpServletResponse`.

### 3.4 Communications Interfaces
- **HTTP / HTTPS:** Port 8080 (default). Standard RESTful endpoints for page navigation and form submissions.
- **WebSocket Protocol (RFC 6455) & SockJS:** Endpoint registered at `/ws` with fallback support.
- **STOMP Sub-protocol:** 
  - Application Prefix: `/app`
  - Broker Topic Prefix: `/topic`

#### STOMP Channel Matrix
| Channel Destination | Publisher | Subscribers | Payload Format | Description |
| :--- | :--- | :--- | :--- | :--- |
| `/topic/session/{id}/participants` | Server (`SessionService`) | Host & Players | `List<String>` (Nicknames) | Broadcasts updated roster as players join lobby. |
| `/topic/session/{id}/question` | Server (`SessionService`) | Host & Players | `QuestionMessage` / `{"finished": true}` | Dispatches current question text, options, and duration. |
| `/topic/session/{id}/leaderboard` | Server (`QuizGameManager`) | Host & Players | `LeaderboardMessage` | Broadcasts ranked score entries at round completion. |
| `/app/session/{id}/answer` | Player Client | Server (`SessionController`) | `AnswerMessage` (JSON) | Submits selected option, token, and reaction time in ms. |

---

## 4. System Features & Functional Requirements

### 4.1 System Feature: Host Authentication & Authorization
#### 4.1.1 Description
Provides user registration, credential validation, session establishment, and access control for quiz hosts.

#### 4.1.2 Functional Requirements
- **FR-1.1:** The system SHALL allow a new host to register with a unique username and password.
- **FR-1.2:** The system SHALL hash passwords using the BCrypt algorithm with a cost factor $\ge 10$ prior to persistence.
- **FR-1.3:** The system SHALL reject duplicate host usernames with an informative validation error.
- **FR-1.4:** The system SHALL enforce authentication on all administrative endpoints (`/quizzes/**`, `/sessions/**`).
- **FR-1.5:** The system SHALL verify ownership of quizzes on every read, update, or delete request; unauthorized attempts MUST return an HTTP 403 Forbidden error.

---

### 4.2 System Feature: Quiz & Question Authoring Management
#### 4.2.1 Description
Enables authenticated hosts to compose, organize, modify, and delete quizzes and their associated questions.

#### 4.2.2 Functional Requirements
- **FR-2.1:** The system SHALL allow hosts to create a quiz specifying a non-blank Title and optional Description.
- **FR-2.2:** The system SHALL allow hosts to add questions containing:
  - Question prompt (VARCHAR up to 500 characters, non-blank).
  - Four distinct textual options (Option A, Option B, Option C, Option D).
  - An integer index specifying the correct option ($0 \le \text{index} \le 3$).
  - A positive time limit in seconds (defaulting to 20 seconds).
- **FR-2.3:** The system SHALL maintain strict ordering of questions within a quiz using Hibernate `@OrderColumn(name = "question_order")`.
- **FR-2.4:** The system SHALL allow hosts to delete individual questions or entire quizzes.

---

### 4.3 System Feature: Live Session Generation & Lobby Roster
#### 4.3.1 Description
Initializes an interactive live room tied to a selected quiz.

#### 4.3.2 Functional Requirements
- **FR-3.1:** The system SHALL generate a unique, human-friendly 6-character uppercase alphanumeric room code excluding ambiguous characters (`ABCDEFGHJKLMNPQRSTUVWXYZ23456789`).
- **FR-3.2:** The system SHALL generate a high-resolution 300x300 PNG QR code containing the full join URL (`/join?code=XXXXXX`) for projection onto presentation displays.
- **FR-3.3:** The system SHALL maintain the session state as an enumeration: `WAITING`, `IN_PROGRESS`, `FINISHED`.
- **FR-3.4:** Upon initialization, the session status MUST be set to `WAITING` with `currentQuestionIndex = -1`.

---

### 4.4 System Feature: Participant Joining & Anonymous Session Management
#### 4.4.1 Description
Facilitates immediate mobile player joining without login barriers.

#### 4.4.2 Functional Requirements
- **FR-4.1:** The system SHALL allow a player to join by supplying a valid join code and a non-blank nickname.
- **FR-4.2:** The system SHALL verify that the nickname is unique within that specific active session. If duplicate, it MUST reject the request.
- **FR-4.3:** The system SHALL prohibit new players from joining if the session has transitioned past `WAITING` (`session.status != WAITING`).
- **FR-4.4:** Upon successful join, the system SHALL issue an immutable cryptographic UUID `sessionToken` stored in the participant's HTTP session.
- **FR-4.5:** The system SHALL immediately broadcast the updated nickname list to `/topic/session/{id}/participants`.

---

### 4.5 System Feature: Real-Time Game Orchestration & Question Dispatch
#### 4.5.1 Description
Coordinates question advancement, client synchronization, and automated timeout scheduling.

#### 4.5.2 Functional Requirements
- **FR-5.1:** When the host clicks "Start Quiz" / "Next Question", `QuizGameManager` SHALL advance the session:
  - If in `WAITING`, advance directly to question index `0` and transition status to `IN_PROGRESS`.
  - If displaying a question, transition to leaderboard view.
  - If displaying a leaderboard, advance to the subsequent question index.
- **FR-5.2:** When a question becomes active, the system SHALL broadcast `QuestionMessage` payload over `/topic/session/{id}/question` containing question ID, prompt text, options A-D, duration in seconds, current question number, and total question count. The correct answer MUST NOT be included in the broadcast payload.
- **FR-5.3:** The system SHALL schedule an asynchronous server-side timeout task via `ScheduledExecutorService` set for the exact duration of the question.
- **FR-5.4:** If the timer expires before manual host intervention, the system SHALL trigger `onQuestionTimeout()`, automatically revealing the leaderboard.
- **FR-5.5:** If all questions have completed, the session status SHALL transition to `FINISHED` and broadcast `{"finished": true}`.

---

### 4.6 System Feature: Real-Time Answer Processing & Dynamic Scoring
#### 4.6.1 Description
Receives, verifies, and scores answers submitted by participants over WebSockets.

#### 4.6.2 Functional Requirements
- **FR-6.1:** The system SHALL listen on `/app/session/{id}/answer` for participant submissions containing `participantToken`, `questionId`, `selectedOption`, and `timeTakenMs`.
- **FR-6.2:** The system SHALL reject or ignore duplicate answer submissions for the same participant and question.
- **FR-6.3:** The system SHALL determine correctness by comparing `selectedOption` against `question.getCorrectOption()`.
- **FR-6.4:** For correct answers, the system SHALL compute points awarded using the formula:
  $$\text{Points} = 100 + \left\lfloor \frac{\max(0, \text{timeLimitMs} - \text{timeTakenMs})}{100} \right\rfloor$$
- **FR-6.5:** For incorrect answers or timeouts, points awarded SHALL be zero ($0$).
- **FR-6.6:** The resulting score record SHALL be persisted to PostgreSQL with participant reference, question reference, selected option, correctness flag, points awarded, and response time in milliseconds.

---

### 4.7 System Feature: Leaderboard Aggregation & Broadcasting
#### 4.7.1 Description
Compiles and streams dynamic player standings.

#### 4.7.2 Functional Requirements
- **FR-7.1:** The system SHALL aggregate all points awarded to each participant across all questions in the current session.
- **FR-7.2:** The system SHALL sort participants in descending order of cumulative points.
- **FR-7.3:** The system SHALL construct a `LeaderboardMessage` DTO and broadcast it over `/topic/session/{id}/leaderboard`.
- **FR-7.4:** The participant client SHALL identify its own nickname in the payload and visually highlight its personal ranking.

---

### 4.8 System Feature: Session Lifecycle & Cascading Cleanup
#### 4.8.1 Description
Ensures clean database teardown without orphan records when quizzes or sessions are removed.

#### 4.8.2 Functional Requirements
- **FR-8.1:** The host SHALL be able to manually stop a live session at any point, immediately canceling all pending timers and setting the status to `FINISHED`.
- **FR-8.2:** Deleting a quiz SHALL systematically purge:
  1. All score entries associated with the quiz's sessions.
  2. All participants linked to those sessions.
  3. All quiz sessions.
  4. The quiz record and all of its associated questions via cascading constraints.
- **FR-8.3:** All deletions SHALL execute inside an atomic database transaction (`@Transactional`).

---

## 5. Non-Functional Requirements

### 5.1 Performance Requirements
- **NFR-1.1 (Latency):** Question broadcast delivery latency across active WebSocket subscribers SHALL NOT exceed 250 milliseconds under local network conditions.
- **NFR-1.2 (Concurrency):** A single standard instance SHALL comfortably support at least 200 concurrent active WebSocket connections per session without memory starvation.
- **NFR-1.3 (Database Query Efficiency):** Leaderboard calculations and score insertions SHALL execute in under 50 milliseconds using indexed relational lookups.

### 5.2 Reliability & Fault Tolerance
- **NFR-2.1 (SockJS Fallback):** In corporate or institutional networks blocking native WebSocket upgrades, the client library SHALL automatically fall back to HTTP streaming or long polling.
- **NFR-2.2 (Thread Safety):** Session timer state and leaderboard freeze flags SHALL be guarded by thread-safe structures (`ConcurrentHashMap`, `ScheduledFuture`).

### 5.3 Security Requirements
- **NFR-3.1 (Credential Storage):** Plaintext passwords SHALL NEVER be stored or logged. BCrypt with high salt rounds is mandatory.
- **NFR-3.2 (Payload Sanitization):** Question payloads sent to participants MUST NOT contain the `correctOption` attribute, preventing client-side inspection vulnerabilities.
- **NFR-3.3 (CSRF & Frame Protection):** REST endpoints SHALL implement Spring Security defaults. Unauthenticated users cannot manipulate host endpoints.
- **NFR-3.4 (Token Isolation):** Participant answers are verified strictly via the internal server-generated `sessionToken` rather than forgeable user nicknames.

### 5.4 Usability & Accessibility Requirements
- **NFR-4.1 (Mobile Responsive):** The participant interface SHALL provide high-contrast, touch-accessible button targets (minimum height 60px) conforming to WCAG 2.1 Level AA standards.
- **NFR-4.2 (Theme Adaptability):** The interface SHALL automatically inherit the user's OS preference (`prefers-color-scheme: dark`) and support manual toggle without page flash.
- **NFR-4.3 (Fast Join):** A user scanning the QR code SHALL reach the game lobby in fewer than 2 clicks.

### 5.5 Maintainability & Portability
- **NFR-5.1 (JDK Compatibility):** The application SHALL compile and run cleanly on OpenJDK 21 LTS without deprecated compiler warnings.
- **NFR-5.2 (Container Portability):** The entire application and PostgreSQL database SHALL be deployable via a standard multi-stage Docker build without requiring host tool installations.

---

## 6. Data Requirements & Schema Specification

### 6.1 Entity-Relationship Specification
The data model consists of six primary relational entities managed by JPA:

```
+------------+       1..* +------------+       1..* +--------------+
|    Host    | ---------> |    Quiz    | ---------> |   Question   |
+------------+            +------------+            +--------------+
                                 |                         |
                                 | 1..*                    | 1..*
                                 v                         v
                          +--------------+  1..*    +--------------+
                          |  QuizSession | -------> |    Score     |
                          +--------------+          +--------------+
                                 |                         ^
                                 | 1..*                    | 1..*
                                 v                         |
                          +--------------+                 |
                          |  Participant | ----------------+
                          +--------------+
```

### 6.2 Data Dictionary
- **hosts:** `id` (BIGSERIAL PK), `username` (VARCHAR 255 UNIQUE NOT NULL), `password` (VARCHAR 255 NOT NULL).
- **quizzes:** `id` (BIGSERIAL PK), `title` (VARCHAR 255 NOT NULL), `description` (TEXT), `host_id` (BIGINT FK -> hosts.id NOT NULL).
- **questions:** `id` (BIGSERIAL PK), `quiz_id` (BIGINT FK -> quizzes.id NOT NULL), `text` (VARCHAR 500 NOT NULL), `option_a`, `option_b`, `option_c`, `option_d` (VARCHAR 255), `correct_option` (INT NOT NULL, 0-3), `time_limit_seconds` (INT NOT NULL, default 20), `question_order` (INT).
- **quiz_sessions:** `id` (BIGSERIAL PK), `quiz_id` (BIGINT FK -> quizzes.id NOT NULL), `join_code` (VARCHAR 8 UNIQUE NOT NULL), `status` (VARCHAR 32 NOT NULL), `current_question_index` (INT NOT NULL), `created_at` (TIMESTAMP NOT NULL).
- **participants:** `id` (BIGSERIAL PK), `session_id` (BIGINT FK -> quiz_sessions.id NOT NULL), `nickname` (VARCHAR 255 NOT NULL), `session_token` (VARCHAR 255 UNIQUE NOT NULL).
- **scores:** `id` (BIGSERIAL PK), `participant_id` (BIGINT FK -> participants.id NOT NULL), `question_id` (BIGINT FK -> questions.id NOT NULL), `selected_option` (INT NOT NULL), `correct` (BOOLEAN NOT NULL), `points_awarded` (INT NOT NULL), `answer_time_ms` (BIGINT NOT NULL).

---

## 7. Verification & Acceptance Criteria
| Requirement ID | Verification Method | Acceptance Criteria |
| :--- | :--- | :--- |
| **FR-1.1 - FR-1.5** | Automated Integration Test | Successful registration, encrypted DB record, rejection of wrong passwords, 403 on non-owned quiz modification. |
| **FR-3.1 - FR-3.2** | Unit & Browser Test | 6-char alphanumeric code generated; QR PNG loads and routes to `/join?code=XXXXXX`. |
| **FR-4.1 - FR-4.5** | Multi-client Simulation | Duplicate nickname rejected; valid join broadcasts updated count and name to host screen. |
| **FR-5.1 - FR-5.5** | WebSocket Test Harness | Question broadcast reaches player; timer triggers auto-leaderboard after duration; answer payload omits solution. |
| **FR-6.1 - FR-6.6** | Scoring Unit Test | Rapid correct answers get base 100 + time bonus; incorrect answer gets 0; duplicate submission blocked. |
| **FR-8.2** | Transactional Test | Deleting quiz removes sessions, scores, and questions with zero foreign key constraint violations. |
