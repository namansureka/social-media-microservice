<div align="center">

# 🚀 Redis Guardrail Microservice
### Spring Boot + Redis + PostgreSQL

Robust backend microservice implementing distributed guardrails, concurrency protection, Redis atomic locks and smart notification batching.

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?style=for-the-badge&logo=springboot)
![Redis](https://img.shields.io/badge/Redis-Enabled-red?style=for-the-badge&logo=redis)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Supported-blue?style=for-the-badge&logo=docker)

</div>

---

# 📌 What This Project Does

This project simulates a high-traffic social platform backend where users and AI bots can create content and interact through posts, comments and likes.

The primary challenge was not simply creating REST APIs, but ensuring that bot activity remains controlled under concurrent requests while maintaining consistency across PostgreSQL and Redis.

The system implements:

- Real-time virality scoring
- Redis-based atomic guardrails
- Bot interaction limits
- Notification throttling
- Smart notification batching
- Scheduler-driven processing
- Distributed state management

The application is designed to remain:

- Stateless
- Thread-safe
- Concurrency-safe
- Scalable

PostgreSQL acts as the **source of truth**, while Redis acts as the **distributed gatekeeper**.

---

# 🛠 Tech Stack

| Technology | Purpose |
|------------|----------|
| Java 17 | Programming Language |
| Spring Boot 3.x | Backend Framework |
| Spring Data JPA | ORM |
| PostgreSQL | Persistent Database |
| Redis | Distributed State Management |
| Docker Compose | Local Setup |
| Lombok | Boilerplate Reduction |

---

# 🏗 System Architecture

```text
Client Request
       ↓
Controller Layer
       ↓
PostServiceImpl
       ↓
GuardrailService
       ↓
Redis (Gatekeeper)
       ↓
PostgreSQL (Source of Truth)
       ↓
NotificationService
       ↓
Notification Scheduler
```

---

# 📂 Project Structure

```text
src
├── controllers
├── dto
├── entities
├── mappers
├── repositories
├── scheduler
├── services
│   ├── GuardrailService
│   ├── NotificationService
│   └── PostService
└── services/impl
```

---

# 🗄 Database Schema

## User

| Field | Type |
|---------|------|
| id | Long |
| username | String |
| isPremium | Boolean |

## Bot

| Field | Type |
|---------|------|
| id | Long |
| name | String |
| personaDescription | String |

## Post

| Field | Type |
|---------|------|
| id | Long |
| authorId | Long |
| content | String |
| createdAt | LocalDateTime |

## Comment

| Field | Type |
|---------|------|
| id | Long |
| postId | Long |
| authorId | Long |
| content | String |
| depthLevel | Integer |
| createdAt | LocalDateTime |

---

# 🌐 REST APIs

## Create Post

```http
POST /api/posts
```

Request:

```json
{
   "authorId":1,
   "content":"Hello World"
}
```

---

## Add Comment

```http
POST /api/posts/{postId}/comments
```

Request:

```json
{
   "authorId":2,
   "authorType":"BOT",
   "content":"Nice post!",
   "parentCommentId":null
}
```

---

## Like Post

```http
POST /api/posts/{postId}/like
```

Request:

```json
{
   "userId":1
}
```

---

# ⚡ Redis Design

## 🛡 Guardrails Implemented

The system enforces three Redis-based guardrails to prevent uncontrolled bot activity and maintain system stability under concurrent requests.

### 1. Horizontal Cap

Purpose:

```text
Limit total bot replies on a post
```

Rule:

```text
Maximum 100 bot replies allowed per post
```

Redis Key:

```text
post:{id}:bot_count
```

Redis Operation:

```redis
INCR
```

---

### 2. Vertical Cap

Purpose:

```text
Prevent infinitely deep comment chains
```

Rule:

```text
Maximum thread depth = 20
```

Validation:

```java
if(depthLevel > 20){

     throw exception;
}
```

---

### 3. Cooldown Cap

Purpose:

```text
Prevent repeated bot interaction with the same user
```

Rule:

```text
Bot ↔ Human interaction cooldown = 10 minutes
```

Redis Key:

```text
cooldown:bot_{id}:human_{id}
```

Redis Operation:

```redis
SET key value NX EX
```

---

## Virality Score

Redis Key:

```text
post:{id}:virality_score
```

Rules:

```text
Bot Reply      → +1
Human Like     → +20
Human Comment  → +50
```

Implementation:

```java
redisTemplate.opsForValue()
             .increment(...)
```

---

# 🔔 Notification Engine

## Notification Flow

```text
Bot Interaction
        ↓
Check notification cooldown
        ↓

No cooldown
        ↓
Immediate notification

Cooldown exists
        ↓
Store notification in queue
```

---

## Notification Configuration

```text
Bot ↔ Human Cooldown = 10 minutes

Notification Cooldown = 15 minutes
```

---

## Redis Structures

```text
user:{id}:notif_cooldown

user:{id}:pending_notifs

pending_users

user:{id}:processing_notifs
```

---

## Notification Scheduler

Runs every:

```text
5 minutes for testing purposes
(simulating a 15-minute production sweep)
```

Flow:

```text
Get users with pending notifications
              ↓
Move queue atomically
              ↓
Read notifications
              ↓
Generate summary
              ↓
Delete processed queue
              ↓
Remove processed user
```

Example:

```text
Summarized Push Notification:

Bot A replied to your post and 3 others interacted with your posts
```

---

# 🔒 Thread Safety & Concurrency Decisions

The most important part of this assignment was guaranteeing thread safety under concurrent requests.

---

## Horizontal Cap Race Condition

Naive implementation:

```java
if(botCount < 100){

     botCount++;
}
```

Problem:

```text
Thread A reads 99

Thread B reads 99

Both increment
```

Result:

```text
More than 100 bot replies may be allowed
```

Solution:

```java
Long count = incrementBotCount(postId);

if(count > 100){

     rollbackBotCount();

     throw exception;
}
```

Redis Operation:

```redis
INCR
```

Reason:

```text
Redis INCR is atomic
```

---

## Cooldown Race Condition (TOCTOU)

TOCTOU:

```text
Time Of Check To Time Of Use
```

Problem:

```java
if(!hasKey()){

     set()
}
```

Concurrent scenario:

```text
Thread A → hasKey=false

Thread B → hasKey=false

Both continue
```

Result:

```text
Multiple requests bypass cooldown restrictions
```

Solution:

```java
setIfAbsent(...)
```

Redis equivalent:

```redis
SET key value NX EX
```

Reason:

```text
Check and creation happen atomically
```

---

## Redis ↔ Database Consistency Problem

Problem:

```text
Redis updates succeed

Database save fails
```

Result:

```text
Redis says interaction happened

Database says interaction never happened
```

Solution:

```java
try{

     save()

}
catch(){

     rollbackBotCount();

     rollbackCooldown();

     rollbackViralityScore();

}
```

---

## Notification Queue Race Condition

Problem:

```text
Scheduler reads notifications

Meanwhile:

New notification arrives

Scheduler deletes queue
```

Result:

```text
Notification gets lost
```

Solution:

```java
rename(
      pendingKey,
      processingKey
)
```

Reason:

```text
Redis rename operation is atomic
```

---

# 📌 Stateless Design

Application remains completely stateless.

No usage of:

```java
HashMap
static variables
in-memory counters
```

All distributed state stored inside Redis:

```text
Virality Score
Bot Count
Cooldown Keys
Pending Notifications
Pending Users
```

---

# ▶ Running Locally

Start Redis and PostgreSQL:

```bash
docker-compose up -d
```

Run Spring Boot application:

```bash
./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

Application URL:

```text
http://localhost:8081
```

---

# ✨ Features Implemented

### Core API Features

- Create posts
- Add comments
- Like posts
- Support for both users and bots

---

### Redis Guardrails

- Horizontal bot reply limit
- Vertical thread depth limit
- Bot ↔ Human cooldown restriction
- Atomic Redis operations

---

### Virality Engine

- Real-time virality score calculation
- Redis-based distributed counters
- Separate weights for different interactions

---

### Notification Engine

- Notification cooldown mechanism
- Pending notification queue
- Notification batching
- Scheduler-based notification processing

---

### Reliability Features

- Race condition handling
- Thread-safe operations
- Redis ↔ Database consistency handling
- Stateless architecture
- Concurrency-safe notification processing

---

<div align="center">

### Built by Naman Sureka

</div>