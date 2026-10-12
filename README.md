# ⚡ Kwizz

> **A modern, open-source, Kahoot-style live multiplayer quiz application built with Java 21, Spring Boot 3.3.4, Spring WebSocket (STOMP), and PostgreSQL.**

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP%20%2F%20SockJS-010101?style=for-the-badge&logo=socketdotio&logoColor=white)](https://docs.spring.io/spring-framework/reference/web/websocket.html)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

---

## 📖 Complete Documentation Suite

Comprehensive engineering documentation is included in the [`docs/`](docs/) directory:

| Document | Description | Format |
| :--- | :--- | :--- |
| 📄 **[Project Proposal](docs/PROJECT_PROPOSAL.md)** | Executive summary, problem statement, solution pillars, tech stack rationale, and deliverables. | Markdown |
| 📋 **[Software Requirements Specification (SRS)](docs/SRS.md)** | Standard IEEE 830 / ISO/IEC/IEEE 29148 requirements document, functional modules, and non-functional metrics. | Markdown |
| 📊 **[UML Diagrams Specification](docs/UML_DIAGRAMS.md)** | Complete visual models: Use Case, Domain Class, Sequence Diagrams, State Machine, Activity, ER, and Docker Deployment. | Mermaid / Markdown |
| 🎓 **[Academic Project Report](docs/PROJECT_REPORT.md)** | In-depth 10-chapter technical report: literature survey, architecture, code deep-dive, QA test matrices, benchmarks, and future roadmap. | Markdown |

---

## ✨ Features

- **🚀 Zero-Friction Joining:** Participants join instantly by scanning an on-screen QR code (generated dynamically via Google ZXing) or typing a 6-character room code. No participant registration or app downloads required.
- **⚡ Real-Time Full-Duplex Gameplay:** Powered by Spring WebSocket and STOMP over SockJS. Live roster updates, countdowns, question pushes, and leaderboard reveals happen simultaneously in sub-second latency across all devices.
- **⏱️ Competitive Time-Decay Scoring:** Rewards both accuracy and reaction speed:
  $$\text{Points} = 100 + \left\lfloor \frac{\max(0, \text{TimeLimit} - \text{ReactionTime})}{100} \right\rfloor$$
- **🔒 Host Security & Isolation:** Secured by Spring Security 6 with BCrypt password hashing. Instructors have full CRUD control over quizzes with ownership validation.
- **📱 Responsive, Mobile-First UI:** Built with lightweight semantic HTML5, pure CSS design tokens, dynamic dark/light theme switching, and tactile touch buttons. Zero heavyweight frontend framework dependencies.
- **🐳 Production Ready & Dockerized:** Multi-stage Docker build utilizing Eclipse Temurin 21 JRE for minimal footprint and maximum security.

---

## 🏗️ System Architecture

```
+-------------------------------------------------------------------------+
|                                CLIENTS                                  |
|   +-------------------------------+   +-----------------------------+   |
|   |    Host Screen (Projector)    |   |  Player Smartphones/Laptops |   |
|   +-------------------------------+   +-----------------------------+   |
+-------------------|----------------------------------|------------------+
                    |  HTTP / STOMP over SockJS (/ws)  |
+-------------------v----------------------------------v------------------+
|                            SPRING BOOT 3.3.4                            |
|                                                                         |
|  [SecurityFilterChain] -> BCrypt, Host Session Auth, Resource Isolation |
|                                                                         |
|  [Controllers]         -> AuthController, QuizController,                |
|                           SessionController, JoinController             |
|                                                                         |
|  [Services]            -> QuizGameManager (ScheduledExecutorService)    |
|                           SessionService, ScoreService, HostService     |
|                                                                         |
|  [Message Broker]      -> Simple In-Memory Broker (/topic, /app)        |
|                                                                         |
|  [Data Access]         -> Spring Data JPA / Hibernate 6.5               |
+-----------------------------------|-------------------------------------+
                                    | JDBC (Port 5432)
+-----------------------------------v-------------------------------------+
|                         POSTGRESQL 16 DATABASE                          |
|  hosts · quizzes · questions · quiz_sessions · participants · scores    |
+-------------------------------------------------------------------------+
```

---

## 🔌 WebSocket / STOMP Endpoints

| Destination | Direction | Payload | Description |
| :--- | :--- | :--- | :--- |
| `/ws` | Handshake | HTTP 101 Switching Protocols | SockJS STOMP connection endpoint. |
| `/topic/session/{id}/participants` | Server $\rightarrow$ Clients | `List<String>` | Broadcasts updated nickname roster in lobby. |
| `/topic/session/{id}/question` | Server $\rightarrow$ Clients | `QuestionMessage` | Dispatches active question text, options, and duration. |
| `/topic/session/{id}/leaderboard` | Server $\rightarrow$ Clients | `LeaderboardMessage` | Broadcasts sorted player rankings and cumulative scores. |
| `/app/session/{id}/answer` | Client $\rightarrow$ Server | `AnswerMessage` | Player submits selected option and reaction time in ms. |

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java 21 LTS** installed (`java -version`)
- **Maven 3.9+** installed (`mvn -version`)
- **PostgreSQL 15+** running locally on port 5432

---

### Option 1: Run Locally

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/kwizz.git
   cd kwizz
   ```

2. **Create the PostgreSQL database:**
   ```sql
   CREATE DATABASE quizdb;
   ```

3. **Configure database credentials:**
   Edit [`src/main/resources/application.properties`](src/main/resources/application.properties) if your local PostgreSQL credentials differ:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/quizdb
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   ```

4. **Build and launch:**
   ```bash
   mvn spring-boot:run
   ```

5. **Open your browser:**
   - Host Portal: [http://localhost:8080](http://localhost:8080)
   - Join Screen: [http://localhost:8080/join](http://localhost:8080/join)

---

### Option 2: Run with Docker

1. **Build the Docker container:**
   ```bash
   docker build -t kwizz:latest .
   ```

2. **Run against an external or local PostgreSQL database:**
   ```bash
   docker run -d \
     -p 8080:8080 \
     -e DB_URL="jdbc:postgresql://host.docker.internal:5432/quizdb" \
     -e DB_USER="postgres" \
     -e DB_PASSWORD="secretpassword" \
     -e SPRING_PROFILES_ACTIVE="prod" \
     --name kwizz-app \
     kwizz:latest
   ```

---

## 🎮 How to Play

1. **Register as a Host:**
   - Navigate to `/register` and create an account.
   - Click **"New Quiz"**, supply a title, and add 4-choice questions with time limits.
2. **Launch a Live Session:**
   - Open your quiz and click **"Start Live Session"**.
   - Project the host dashboard onto the classroom screen showing the **Join Code** and **QR Code**.
3. **Players Join:**
   - Students scan the QR code or visit `/join` on their smartphones.
   - They enter their nickname and wait in the lobby.
4. **Run the Game:**
   - Host clicks **"Start Quiz"**.
   - Questions stream live to all phones; participants tap their color-coded tile (A, B, C, D).
   - Rounds automatically transition to the leaderboard upon timer expiry, or the host can manually click **"Next Question"**.
   - At the end of the final question, the winner podium is revealed!

---

## 📂 Project Structure

```
kwizz/
├── Dockerfile                              # Multi-stage Docker build configuration
├── pom.xml                                 # Maven dependencies and build definition
├── README.md                               # Project documentation & quickstart
├── docs/                                   # Full engineering documentation suite
│   ├── PROJECT_PROPOSAL.md                 # Project proposal & executive summary
│   ├── SRS.md                              # IEEE 830 Software Requirements Specification
│   ├── UML_DIAGRAMS.md                     # Complete Mermaid UML diagrams
│   └── PROJECT_REPORT.md                   # Formal 10-chapter academic project report
└── src/
    └── main/
        ├── java/com/kwizz/
        │   ├── QuizAppApplication.java     # Spring Boot application entry point
        │   ├── config/                     # SecurityConfig & WebSocketConfig
        │   ├── controller/                 # Auth, Quiz, Session, Join, Home controllers
        │   ├── dto/                        # QuestionMessage, AnswerMessage, Leaderboard DTOs
        │   ├── entity/                     # Host, Quiz, Question, Session, Participant, Score
        │   ├── exception/                  # GlobalExceptionHandler, Custom Exceptions
        │   ├── repository/                 # Spring Data JPA Repositories
        │   └── service/                    # QuizGameManager, SessionService, ScoreService, etc.
        └── resources/
            ├── application.properties      # Local development profile
            ├── application-prod.properties # Docker production profile
            ├── static/                     # CSS stylesheets, JS theme switchers
            └── templates/                  # Thymeleaf dynamic HTML5 templates
```

---

## 👥 Author & License

- **Author:** Karmveer
- **Module:** Advanced Java Development
- **License:** Open-source for educational and academic use.
