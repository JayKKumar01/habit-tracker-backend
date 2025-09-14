# Habit Tracker – Full Stack Web Application

## 🚀 Live Demo

Explore the live application here:
🔗 **[https://jaykkumar01.github.io/habit-tracker](https://jaykkumar01.github.io/habit-tracker)**

## 📁 Frontend Repository

View the source code for the React.js frontend:
📂 **[JayKKumar01/habit-tracker](https://github.com/JayKKumar01/habit-tracker)**

---

## 📌 Overview

**Habit Tracker** is a full-stack web application developed using **React.js** for the frontend and **Spring Boot** for the backend. It helps users build positive routines by allowing them to:

* Create custom daily or weekly habits
* Log progress with a visual tracker
* Organize habits using tags
* Maintain consistency through a streak-based view

The application focuses on simplicity, usability, and performance—making habit tracking easy and rewarding.

---

## ✅ Key Features

* 🔐 **User Authentication** — Register and log in securely
* 📝 **Habit Management** — Create, edit, and delete habits (daily or weekly)
* 📊 **Progress Logging** — Track daily completions and view progress history
* 🏷️ **Tagging System** — Assign tags for better habit organization
* 📱 **Responsive UI** — Optimized for both desktop and mobile browsers
---

## 🔄 Entity Relationships (JPA)

The backend follows a clean, normalized relational structure using **JPA annotations** to define relationships between entities. Here’s how the core entities are connected:

---

### 🧑 `User` Entity

Each user can have:

* **Multiple habits** (`@OneToMany`)
* **One profile** (`@OneToOne`)

```java
// User.java

@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Habit> habits = new ArrayList<>();

@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private Profile profile;
```

---

### 📋 `Habit` Entity

Each habit:

* Belongs to **one user** (`@ManyToOne`)
* Has **many logs** (`@OneToMany`)
* Can have **multiple tags** (`@ManyToMany`)

```java
// Habit.java

@ManyToOne
@JoinColumn(name = "user_id", nullable = false)
private User user;

@OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
private List<HabitLog> habitLogs = new ArrayList<>();

@ManyToMany
@JoinTable(
    name = "habit_tag_mapping",
    joinColumns = @JoinColumn(name = "habit_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id")
)
private Set<Tag> tags = new HashSet<>();
```

---

### 🧠 `Profile` Entity

Each profile is tied to exactly **one user**.

```java
// Profile.java

@OneToOne
@JoinColumn(name = "user_id", nullable = false, unique = true)
private User user;
```

---

### ✅ `HabitLog` Entity

Each log entry is connected to:

* **One habit** (`@ManyToOne`)

```java
// HabitLog.java

@ManyToOne
@JoinColumn(name = "habit_id", nullable = false)
private Habit habit;
```

It also enforces **one log per habit per date** via a unique constraint:

```java
@Table(
  name = "habit_logs",
  uniqueConstraints = @UniqueConstraint(columnNames = {"habit_id", "date"})
)
```

---

### 🏷️ `Tag` Entity

Each tag:

* Can be assigned to **multiple habits** (`@ManyToMany`, inverse side)

```java
// Tag.java

@ManyToMany(mappedBy = "tags")
private Set<Habit> habits = new HashSet<>();
```

---

### 💡 Summary Table

| Entity   | Relationship | Target Entity | Cardinality  | Annotation Used |
| -------- | ------------ | ------------- | ------------ | --------------- |
| User     | Habits       | Habit         | One-to-Many  | `@OneToMany`    |
| User     | Profile      | Profile       | One-to-One   | `@OneToOne`     |
| Habit    | User         | User          | Many-to-One  | `@ManyToOne`    |
| Habit    | Logs         | HabitLog      | One-to-Many  | `@OneToMany`    |
| Habit    | Tags         | Tag           | Many-to-Many | `@ManyToMany`   |
| Tag      | Habits       | Habit         | Many-to-Many | `@ManyToMany`   |
| HabitLog | Habit        | Habit         | Many-to-One  | `@ManyToOne`    |

---

## 🔌 REST API Endpoints

### 1. Authentication (`AuthController`)

Manages user registration and login, issuing JWT tokens for secure authentication.

| HTTP Method | Endpoint           | Description                                     |
| ----------- | ------------------ | ----------------------------------------------- |
| POST        | `/api/auth/signup` | Register a new user (`signup`)                  |
| POST        | `/api/auth/login`  | Authenticate user and issue JWT token (`login`) |

```java
@PostMapping("/signup")
public ResponseEntity<?> signup(@RequestBody UserRegistration dto)

@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request)
```

---

### 2. User Management (`UserController`)

Provides secure access to user details, such as fetching user information by email.

| HTTP Method | Endpoint     | Description                                       |
| ----------- | ------------ | ------------------------------------------------- |
| GET         | `/api/users` | Retrieve user details by email (`getUserByEmail`) |

```java
@GetMapping
public ResponseEntity<?> getUserByEmail(@RequestParam String email, HttpServletRequest request)
```

---

### 3. Profile Management (`ProfileController`)

Handles creation, updating, and retrieval of user profile data with proper authorization.

| HTTP Method | Endpoint                     | Description                                           |
| ----------- | ---------------------------- | ----------------------------------------------------- |
| POST        | `/api/profile/save/{userId}` | Create or update user profile (`saveOrUpdateProfile`) |
| GET         | `/api/profile/user/{userId}` | Retrieve profile details by user ID (`getProfile`)    |

```java
@PostMapping("/save/{userId}")
public ResponseEntity<?> saveOrUpdateProfile(@PathVariable Long userId, @RequestBody ProfileRequest profileRequest, HttpServletRequest request)

@GetMapping("/user/{userId}")
public ResponseEntity<?> getProfile(@PathVariable Long userId, HttpServletRequest request)
```

---

### 4. Habit Management (`HabitController`)

Enables users to create, update, view, and delete habits, ensuring access control.

| HTTP Method | Endpoint                                | Description                                          |
| ----------- | --------------------------------------- | ---------------------------------------------------- |
| POST        | `/api/habits/create/{userId}`           | Create a new habit (`createHabit`)                   |
| PUT         | `/api/habits/edit/{userId}`             | Update existing habit (`updateHabit`)                |
| GET         | `/api/habits/habits/{userId}`           | Retrieve all habits for a user (`getHabitsByUserId`) |
| DELETE      | `/api/habits/delete/{userId}/{habitId}` | Delete a habit (`deleteHabit`)                       |

```java
@PostMapping("/create/{userId}")
public ResponseEntity<?> createHabit(@PathVariable Long userId, @RequestBody HabitRequest requestDTO, HttpServletRequest request)

@PutMapping("/edit/{userId}")
public ResponseEntity<?> updateHabit(@PathVariable Long userId, @RequestBody HabitUpdate editRequest, HttpServletRequest request)

@GetMapping("/habits/{userId}")
public ResponseEntity<?> getHabitsByUserId(@PathVariable Long userId, HttpServletRequest request)

@DeleteMapping("/delete/{userId}/{habitId}")
public ResponseEntity<?> deleteHabit(@PathVariable Long userId, @PathVariable Long habitId, HttpServletRequest request)
```

---

### 5. Habit Log Management (`HabitLogController`)

Supports secure updates to habit progress logs.

| HTTP Method | Endpoint                         | Description                           |
| ----------- | -------------------------------- | ------------------------------------- |
| POST        | `/api/habit-log/update/{userId}` | Update a habit log (`updateHabitLog`) |

```java
@PostMapping("/update/{userId}")
public ResponseEntity<?> updateHabitLog(@PathVariable Long userId, @RequestBody HabitLogDto habitLogDto, HttpServletRequest request)
```

---

### 6. Tag Management (`TagController`)

Provides endpoints to manage tags associated with habits, with enforced authorization.

| HTTP Method | Endpoint                              | Description                                  |
| ----------- | ------------------------------------- | -------------------------------------------- |
| POST        | `/api/tags/add-habit-tag/{userId}`    | Add a tag to a habit (`addHabitTag`)         |
| POST        | `/api/tags/remove-habit-tag/{userId}` | Remove a tag from a habit (`removeHabitTag`) |

```java
@PostMapping("/add-habit-tag/{userId}")
public ResponseEntity<?> addHabitTag(@PathVariable Long userId, @RequestBody TagAddRequest tagRequest, HttpServletRequest request)

@PostMapping("/remove-habit-tag/{userId}")
public ResponseEntity<?> removeHabitTag(@PathVariable Long userId, @RequestBody TagDeleteRequest tagRequest, HttpServletRequest request)
```

---
## ⚙️ Service Layer Overview

The service layer encapsulates the core business logic and database interaction. To ensure optimal performance, it combines Spring Data JPA with **native SQL queries** through `EntityManager` for critical operations like authentication, data retrieval, and efficient inserts/updates.

---

### 1. 🔐 Auth Service (`AuthServiceImpl`)

Handles secure user registration and login via native SQL for performance-critical paths.

**Responsibilities:**

* Register users with encrypted passwords.
* Authenticate users using email and hashed password.
* Generate JWT tokens on successful login.

```java
// Native SQL for login authentication
SELECT id, password FROM users WHERE email = :email
```

---

### 2. 👤 User Service (`UserServiceImpl`)

Retrieves user data quickly using lightweight SQL projections instead of full entity loading.

**Responsibilities:**

* Fetch user details by email using selected fields only.

```java
// SQL projection for efficient user retrieval
SELECT id, name, email, created_at FROM users WHERE email = :email
```

---

### 3. 🧾 Profile Service (`ProfileServiceImpl`)

Supports user profile creation and updates using `UPSERT` operations and JPQL fetch.

**Responsibilities:**

* Create or update bio and name using upsert pattern.
* Fetch user profile via JPQL.

```java
// Upsert user profile
INSERT INTO profiles (...) ON DUPLICATE KEY UPDATE ...

// Update user name
UPDATE users SET name = :name WHERE id = :userId
```

---

### 4. 📋 Habit Service (`HabitServiceImpl`)

Implements optimized logic for habit creation, updates, and aggregated fetching.

**Responsibilities:**

* Create habits and auto-assign default tags (like DAILY).
* Update habits with flexible SQL.
* Retrieve habits + logs + tags in a single optimized query.

```java
// Dynamic habit update logic
UPDATE habits SET title = ..., description = ... WHERE id = :habitId

// Fetch all habits, logs, and tags efficiently
SELECT h.id, h.title, ..., ht.name FROM habits h LEFT JOIN ...
```

---

### 5. 📈 Habit Log Service (`HabitLogServiceImpl`)

Performs idempotent logging of habit progress using upsert operations.

**Responsibilities:**

* Insert or update habit logs for a given day.

```java
// Insert or update if log already exists
INSERT INTO habit_logs (...) VALUES (...) ON DUPLICATE KEY UPDATE ...
```

---

### 6. 🏷️ Tag Service (`TagServiceImpl`)

Manages tagging logic for habits, ensuring uniqueness and avoiding duplication.

**Responsibilities:**

* Add new tags or reuse existing ones.
* Link tags to habits only if not already mapped.
* Remove habit-tag associations.

```java
// Insert tag mapping only if not already linked
INSERT INTO habit_tag_mapping (...) SELECT ... WHERE NOT EXISTS (...)
```

---
## 📦 Technologies Used

* **Backend**: Java 17, Spring Boot, Spring Web, Spring Data JPA, Spring Security
* **Database**: MySQL
* **Authentication**: JWT (JSON Web Token)
* **Build Tool**: Maven
* **Other Tools**: Lombok, JPA Native Queries via EntityManager

---

## 🚀 Deployment

You can run the project locally using:

```bash
mvn spring-boot:run
```

Or build the JAR:

```bash
mvn clean install
java -jar target/habit-tracker.jar
```

---

## 🔒 Security

All endpoints (except `/auth/signup` and `/auth/login`) are secured using JWT tokens.
Tokens must be included in the `Authorization` header as:

```
Authorization: Bearer <your_token_here>
```

---

## 📁 Project Structure

```
src/
├── controller        # REST API controllers
├── dto              # Request and response models
├── entity           # JPA entity models
├── mapper           # DTO to Entity converters
├── repository       # Spring Data JPA repositories
├── service          # Business logic and DB interaction
└── util             # JWT utilities and helpers
```

---

## 🧠 Design Philosophy

* **Efficient Native SQL**: Leveraging `EntityManager` with native queries avoids unnecessary lazy loading and ORM overhead.
* **Modular Codebase**: Clear separation of concerns across controller, service, and persistence layers.
* **Clean DTO Mapping**: Keeps domain logic decoupled from data representation.

---

## 🙌 Acknowledgments

This project was developed with performance, simplicity, and extensibility in mind.
Special thanks to open-source libraries and the Spring community.

---
