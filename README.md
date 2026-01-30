

# 🔗 URL Shortener (Java + PostgreSQL + Redis)

A backend URL shortener system built in Java with:

* PostgreSQL for persistence
* Redis for caching (cache-aside strategy)
* Lightweight HTTP server (no Spring Boot)

---

## 🚀 Prerequisites

Make sure the following are installed:

* Java 17+
* PostgreSQL
* Redis

---

## 🗄️ PostgreSQL Setup & Start

### ▶ macOS (Homebrew)

```bash
# Start PostgreSQL
brew services start postgresql

# Verify PostgreSQL is running
pg_isready

# Connect to PostgreSQL
psql postgres
```

Expected output:

```
accepting connections
```

---

### ▶ Windows

1. Start PostgreSQL from **Services**

   * Press `Win + R` → type `services.msc`
   * Start **PostgreSQL**

OR using command line:

```powershell
net start postgresql
```

2. Connect to PostgreSQL:

```powershell
psql -U postgres
```

---

### ▶ Create and use the database (both OS)

```sql
CREATE DATABASE url_shortener;
\c url_shortener
CREATE TABLE url_mapping (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(20) UNIQUE,
    long_url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    click_count INT DEFAULT 0
);

```

---

## 📦 Redis Setup & Start

### ▶ macOS (Homebrew)

```bash
# Start Redis
brew services start redis

# Verify Redis is running
redis-cli ping
```

Expected output:

```
PONG
```

---

### ▶ Windows

1. Download Redis for Windows from:
   [https://github.com/microsoftarchive/redis/releases](https://github.com/microsoftarchive/redis/releases)

2. Start Redis server:

```powershell
redis-server
```

3. Open Redis CLI:

```powershell
redis-cli
```

4. Verify:

```redis
PING
```

Expected output:

```
PONG
```

---

## ▶ Running the Application

```bash
java com.example.URLShortener.Main
```

Expected output:

```
🚀 Server started on http://localhost:8080
```

---

## 🔗 Using the Application

### Redirect using short URL

```
http://localhost:8080/{shortCode}
```

Example:

```
http://localhost:8080/9
```

➡️ Redirects to the original long URL
➡️ Click count increments
➡️ Cached in Redis after first access

---

## ⚡ Redis Cache Verification (Optional)

```bash
redis-cli
KEYS *
GET 9
```

---

## 🧠 Architecture Overview

* **Controller**: Handles HTTP requests
* **Service**: Business logic + cache-aside strategy
* **Repository**: Database access
* **PostgreSQL**: Source of truth
* **Redis**: In-memory cache for fast redirects

---

## 🏗️ Cache Strategy

This project uses **Cache-Aside Pattern**:

1. Check Redis
2. On cache miss → query DB
3. Populate Redis
4. Redirect user



## 📌 Notes

* Redis is used only as a cache
* PostgreSQL remains the authoritative data store
* System remains functional even if Redis is down






