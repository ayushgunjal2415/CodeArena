# CodeArena

<p align="center">

<img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk"/>
<img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot"/>
<img src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react"/>
<img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql"/>
<img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis"/>
<img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity"/>
<img src="https://img.shields.io/badge/JWT-Authentication-black?style=for-the-badge&logo=jsonwebtokens"/>
<img src="https://img.shields.io/badge/WebSocket-Real_Time-blue?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Spring_AI-Groq-purple?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Maven-Build-red?style=for-the-badge&logo=apachemaven"/>

</p>

CodeArena is a full-stack coding platform developed to provide an interactive environment for coding practice, programming contests, MCQ assessments, and real-time coding battles. The platform integrates AI-assisted code review, secure authentication, live collaboration, and performance analytics to create a modern competitive programming experience.

---

# Table of Contents

- Project Overview
- Core Features
- Technology Stack
- System Architecture
- Project Structure
- Installation
- Configuration
- Usage
- Screenshots
- Contributors
- License

---

# Project Overview

CodeArena is designed as an all-in-one platform for programmers to practice coding, participate in coding contests, solve MCQs, compete in real-time coding rooms, and receive AI-powered feedback on their solutions.

The application follows a full-stack architecture consisting of a Spring Boot backend and a React frontend. It uses JWT-based authentication for secure access, Redis for caching and OTP management, MySQL for persistent storage, and WebSocket communication for live multiplayer features.

The project aims to simplify coding practice while providing an engaging and competitive learning environment.

---

# Core Features

## User Authentication

- User Registration
- Secure Login
- JWT Authentication
- Role-Based Authorization
- Password Reset
- Email Verification using OTP

---

## Coding Practice

- Coding Question Practice
- Difficulty-Based Questions
- Starter Code Support
- Multiple Programming Languages
- Automatic Code Evaluation
- Test Case Validation

---

## Real-Time Coding Battles

- Create Coding Rooms
- Join Existing Rooms
- Live Coding Competition
- WebSocket Communication
- Real-Time Result Generation

---

## MCQ Assessment

- Subject-wise MCQ Practice
- Timed Assessments
- Automatic Evaluation
- Performance Analysis

---

## AI Features

- AI Code Review
- AI Programming Assistance
- Code Improvement Suggestions
- Intelligent Feedback Generation

---

## Dashboard

- Practice History
- Coding Statistics
- Leaderboard
- User Performance Analysis
- Match History

---

## Code Execution

- Execute Code Online
- Multiple Language Support
- Runtime Output
- Error Handling
- Execution Timeout Management

---

## Notification System

- OTP Verification
- Email Notifications
- Room Invitations

---

# Technology Stack

| Layer          | Technologies                                           |
| -------------- | ------------------------------------------------------ |
| Frontend       | React 19, Vite, JavaScript, HTML5, CSS3                |
| Backend        | Java 17, Spring Boot, Spring Security, Spring Data JPA |
| Database       | MySQL                                                  |
| Cache          | Redis                                                  |
| Authentication | JWT, Spring Security                                   |
| AI Integration | Spring AI, Groq API                                    |
| Communication  | REST API, WebSocket                                    |
| Build Tool     | Maven                                                  |


---

# System Architecture

```
                           +----------------------+
                           |    React Frontend    |
                           |      (Vite)          |
                           +----------+-----------+
                                      |
                          REST API / WebSocket
                                      |
                           +----------v-----------+
                           |   Spring Boot API    |
                           |  Spring Security     |
                           |        JWT           |
                           +----------+-----------+
                                      |
          +---------------------------+----------------------------+
          |                           |                            |
          |                           |                            |
+---------v--------+        +----------v---------+       +----------v---------+
|     MySQL        |        |       Redis        |       |    Spring AI       |
| User & Question  |        | OTP & Cache Store  |       |     Groq API       |
+------------------+        +--------------------+       +--------------------+
                                      |
                         +------------+------------+
                         |                         |
                Real-Time Coding Rooms      AI Code Review
                   (WebSocket)           & Programming Help
```

---

# Project Structure

```
CodeArena
│
├── Backend
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── com.codearena.backend
│   │   │   │       ├── config
│   │   │   │       ├── controller
│   │   │   │       ├── dto
│   │   │   │       ├── entity
│   │   │   │       ├── repository
│   │   │   │       ├── service
│   │   │   │       ├── serviceImpl
│   │   │   │       ├── utils
│   │   │   │       └── exception
│   │   │   │
│   │   │   └── resources
│   │   │       ├── application-example.properties
│   │   │       └── data
│   │   │
│   │   └── test
│   │
│   └── pom.xml
│
├── Frontend
│   ├── public
│   ├── src
│   │   ├── components
│   │   ├── config
│   │   ├── pages
│   │   ├── routes
│   │   ├── services
│   │   ├── utils
│   │   └── assets
│   │
│   ├── package.json
│   └── vite.config.js
│
└── README.md
```

---

# Installation

## Clone Repository

```bash
git clone https://github.com/ayushgunjal2415/CodeArena.git
cd CodeArena
```

## Backend

```bash
cd Backend

mvn clean install

mvn spring-boot:run
```

## Frontend

```bash
cd Frontend

npm install

npm run dev
```

---

# Configuration

## Backend

Create

```
application.properties
```

using

```
application-example.properties
```

Update the following values:

- Database URL
- Database Username
- Database Password
- JWT Secret
- Groq API Key
- Mail Configuration

---

## Frontend

Create

```
.env
```

using

```
.env.example
```

Configure:

```
VITE_API_BASE_URL

VITE_SOCKET_URL
```

---

# Usage

1. Register a new account.
2. Verify your email using OTP.
3. Login to the platform.
4. Practice coding problems or MCQs.
5. Create or join coding rooms.
6. Execute code online.
7. View AI-generated code reviews.
8. Track progress using the dashboard and leaderboard.

---

# Screenshots

### Home Page

> _Screenshot will be added here._

---

### Dashboard

> _Screenshot will be added here._

---

### Coding Editor

> _Screenshot will be added here._

---

### Coding Battle Room

> _Screenshot will be added here._

---

### AI Code Review

> _Screenshot will be added here._

---

### Leaderboard

> _Screenshot will be added here._

---

# Contributors

**Ayush Gunjal**

GitHub

https://github.com/ayushgunjal2415

LinkedIn

https://linkedin.com/in/ayush-gunjal-9918732bb

---

# License

This project is licensed under the MIT License.
