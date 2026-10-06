# 💼 JobTrack — Job Application Management System

A production-ready RESTful backend built with **Java 17**, **Spring Boot 3.2**, **Spring Security 6**, **JJWT**, and **Spring Data JPA**.

JobTrack allows job seekers to track their applications through every stage of the hiring pipeline, record interview rounds, store notes, filter/search with pagination, and view real-time pipeline analytics.

---

## 🎯 Features Implemented

- 🔐 **Authentication & Security**:
  - Stateless JWT (JSON Web Token) authentication.
  - BCrypt password hashing.
  - Multi-user isolation (users can only see and manipulate their own data).
  - Role-based authorization architecture.
- 📋 **Job Application Tracking**:
  - Store comprehensive job metadata (Company, Title, URL, Location, Employment Type, Salary Range, Applied Date, Deadlines).
  - Status management: `WISHLIST`, `APPLIED`, `SCREENING`, `INTERVIEWING`, `OFFER`, `REJECTED`, `ACCEPTED`, `WITHDRAWN`.
  - Quick status transitions via `PATCH /api/v1/applications/{id}/status` with automated status-change logging notes.
- 🤝 **Interview Pipeline Management**:
  - Track multiple interview rounds per application (`HR_PHONE_SCREEN`, `TECHNICAL`, `SYSTEM_DESIGN`, `BEHAVIORAL`, `TAKE_HOME_REVIEW`, `FINAL_ROUND`).
  - Interviewer contact info, schedule time, video link/location, feedback, and prep notes.
  - Automated workflow: Scheduling an interview automatically advances the application to `INTERVIEWING`.
- 📝 **Application Notes**:
  - Add and edit timestamped notes per application for interview feedback, recruiter conversations, and checklist tracking.
- 🔍 **Search, Filter & Pagination**:
  - Filter by application status or employment type.
  - Search across company name, job title, and location with SQL LIKE wildcards.
  - Pageable results with customizable sort direction and fields (`createdAt`, `appliedDate`, `companyName`, etc.).
- 📊 **Analytics & Metrics**:
  - Overall pipeline statistics (`/api/v1/analytics/stats`).
  - Active vs. archived application counts.
  - Status breakdown distribution.
  - Response rate % (applications moving beyond applied stage).
  - Offer conversion rate %.
  - Upcoming interview schedule and countdown.
- 🗄️ **Zero-Friction In-Memory Database**:
  - Pre-configured H2 database with web console at `/h2-console`.
  - Automated seed data initialized at startup for immediate testing.

---

## 🏗️ Project Architecture

The application follows the clean **Layered Architecture (Separation of Concerns)** pattern:

```
src/main/java/com/jobtrack/
├── JobTrackApplication.java           # Spring Boot application entry point
├── config/
│   ├── SecurityConfig.java            # Spring Security filter chain & CORS rules
│   └── DataInitializer.java          # Seeds demo account & realistic job data
├── controller/                        # REST Controllers (handles HTTP req/resp)
│   ├── AuthController.java            # /api/v1/auth
│   ├── JobApplicationController.java  # /api/v1/applications
│   ├── InterviewController.java       # /api/v1/applications/{id}/interviews
│   ├── NoteController.java            # /api/v1/applications/{id}/notes
│   └── AnalyticsController.java       # /api/v1/analytics/stats
├── dto/                               # Data Transfer Objects & Validation
│   ├── request/                       # Incoming request payloads (@Valid)
│   └── response/                      # Outgoing JSON representations
├── entity/                            # JPA Entities (Database Tables)
│   ├── User.java                      # Users table + UserDetails
│   ├── JobApplication.java            # Applications table + 1-to-many relations
│   ├── Interview.java                 # Interview rounds table
│   ├── Note.java                      # Notes table
│   └── enums/                         # Status & Type enums
├── exception/                         # Global Exception Handling
│   ├── GlobalExceptionHandler.java    # @RestControllerAdvice with structured JSON
│   └── *.java                         # Custom runtime exceptions
├── repository/                        # Spring Data JPA Repositories
│   ├── UserRepository.java
│   ├── JobApplicationRepository.java
│   ├── InterviewRepository.java
│   └── NoteRepository.java
├── security/                          # JWT filter & token provider
│   ├── JwtService.java
│   └── JwtAuthenticationFilter.java
└── service/                           # Business logic layer
    ├── AuthService.java
    ├── JobApplicationService.java
    ├── InterviewService.java
    ├── NoteService.java
    └── AnalyticsService.java
```

---

## ⚡ Quick Start

### 1. Prerequisites
- **Java 17** (or above)
- **Maven 3.8+**

### 2. Run the Application
You can run the application directly using the helper script:
```bash
./run.sh
```
Or via Maven:
```bash
mvn spring-boot:run -o
```

The application starts on `http://localhost:8080`.

### 3. Pre-seeded Demo Account
For testing out-of-the-box, the following demo account is pre-loaded:
- **Email**: `demo@jobtrack.com`
- **Password**: `password123`
*(Contains 4 sample applications: Google, Netflix, Stripe, Amazon with interviews and notes).*

---

## 🧪 Running Automated Tests

Run the full integration and unit test suite:
```bash
mvn test -o
```

---

## 📡 API Reference & Curl Examples

### 1. Authentication

#### Register a New User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "candidate@example.com",
    "password": "strongPassword123",
    "fullName": "Alex Mercer"
  }'
```

#### Login (Obtain JWT Token)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@jobtrack.com",
    "password": "password123"
  }'
```
*Response contains the `token` string. Save this token for subsequent requests.*

---

### 2. Job Applications

*(Replace `<TOKEN>` with your Bearer token)*

#### Create a Job Application
```bash
curl -X POST http://localhost:8080/api/v1/applications \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Microsoft",
    "jobTitle": "Software Engineer II",
    "jobUrl": "https://careers.microsoft.com/job/123",
    "location": "Redmond, WA (Hybrid)",
    "employmentType": "FULL_TIME",
    "minSalary": 150000,
    "maxSalary": 185000,
    "salaryCurrency": "USD",
    "status": "APPLIED",
    "appliedDate": "2026-10-06",
    "initialNotes": "Applied through employee referral."
  }'
```

#### List Applications (with Search, Filter & Pagination)
```bash
# Get page 0, size 10, filtering by status and searching keyword "Engineer"
curl -X GET "http://localhost:8080/api/v1/applications?status=INTERVIEWING&search=Engineer&page=0&size=10&sortBy=createdAt&sortDir=desc" \
  -H "Authorization: Bearer <TOKEN>"
```

#### Get Application by ID
```bash
curl -X GET http://localhost:8080/api/v1/applications/1 \
  -H "Authorization: Bearer <TOKEN>"
```

#### Update Application Status (PATCH)
```bash
curl -X PATCH http://localhost:8080/api/v1/applications/1/status \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INTERVIEWING",
    "optionalNote": "Recruiter reached out to schedule Round 1."
  }'
```

---

### 3. Interview Rounds

#### Schedule an Interview Round
```bash
curl -X POST http://localhost:8080/api/v1/applications/1/interviews \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "roundName": "System Design Round",
    "interviewType": "SYSTEM_DESIGN",
    "scheduledAt": "2026-10-15T15:30:00",
    "interviewerName": "Jane Doe",
    "interviewerEmail": "jdoe@company.com",
    "locationOrLink": "https://meet.google.com/xyz-abcd-efg",
    "notes": "Prepare caching strategies and distributed messaging."
  }'
```

#### Get Interviews for an Application
```bash
curl -X GET http://localhost:8080/api/v1/applications/1/interviews \
  -H "Authorization: Bearer <TOKEN>"
```

---

### 4. Application Notes

#### Add a Note
```bash
curl -X POST http://localhost:8080/api/v1/applications/1/notes \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Hiring Manager 1-on-1",
    "content": "Discussed team culture, quarterly goals, and microservices architecture."
  }'
```

#### Get All Notes for Application
```bash
curl -X GET http://localhost:8080/api/v1/applications/1/notes \
  -H "Authorization: Bearer <TOKEN>"
```

---

### 5. Analytics & Dashboard Stats

#### Get Pipeline Metrics
```bash
curl -X GET http://localhost:8080/api/v1/analytics/stats \
  -H "Authorization: Bearer <TOKEN>"
```

*Example Response:*
```json
{
  "totalApplications": 4,
  "activeApplications": 4,
  "totalInterviews": 2,
  "upcomingInterviewsCount": 2,
  "responseRatePercentage": 75.0,
  "offerRatePercentage": 25.0,
  "statusCounts": {
    "WISHLIST": 0,
    "APPLIED": 1,
    "SCREENING": 1,
    "INTERVIEWING": 1,
    "OFFER": 1,
    "REJECTED": 0,
    "ACCEPTED": 0,
    "WITHDRAWN": 0
  },
  "upcomingInterviews": [ ... ]
}
```

---

## 🗃️ H2 In-Memory Database Web Console

To inspect the database tables directly in your browser:
1. Open [http://localhost:8080/h2-console](http://localhost:8080/h2-console).
2. Set **JDBC URL**: `jdbc:h2:mem:jobtrackdb`.
3. Set **User Name**: `sa` (leave Password empty).
4. Click **Connect**.
