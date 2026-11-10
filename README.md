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
- 📥 **CSV Export**:
  - Download application history as an RFC 4180-compliant `.csv` file with UTF-8 BOM support.
  - Filters by application status and search keyword, or exports complete application records with one click.
- 📖 **Interactive Swagger UI & OpenAPI 3**:
  - Full interactive REST API documentation explorer available at `/swagger-ui.html`.
  - Global JWT Bearer authorization support for one-click in-browser endpoint testing.
  - Raw JSON specification served at `/v3/api-docs`.
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
│   ├── OpenApiConfig.java             # Swagger UI & OpenAPI 3 specification configuration
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
./run.sh              # first run downloads dependencies
./run.sh --offline    # later runs, once dependencies are cached
./run.sh --help       # show all options
```
Or via Maven:
```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

Or with Docker (no local Java/Maven needed):
```bash
docker build -t jobtrack .
docker run -p 8080:8080 \
  -e APPLICATION_SECURITY_JWT_SECRET_KEY=<your-256-bit-hex-secret> \
  -v jobtrack-uploads:/app/uploads \
  jobtrack
```
The image runs as a non-root user and stores uploaded documents in the `/app/uploads` volume.

### 3. Pre-seeded Demo Account
For testing out-of-the-box, the following demo account is pre-loaded:
- **Email**: `demo@jobtrack.com`
- **Password**: `password123`
*(Contains 4 sample applications: Google, Netflix, Stripe, Amazon with interviews and notes).*

---

## 🧪 Running Automated Tests

Run the full integration and unit test suite:
```bash
mvn test
```

Run a single test class:
```bash
mvn test -Dtest=JobApplicationServiceTest
```

### Continuous Integration
Every push and pull request to `main` runs `mvn -B verify` on JDK 17 via GitHub Actions (`.github/workflows/ci.yml`). Surefire reports are uploaded as a build artifact when a run fails.

---

## ✅ Validation Rules & Error Responses

Request bodies are validated before reaching the service layer. Invalid input returns `400 Bad Request` with a structured JSON body:

| Field | Rule |
|---|---|
| `companyName`, `jobTitle` | Required, max 150 characters |
| `jobUrl` | Optional `http://` or `https://` link, max 500 characters; a blank value is stored as no URL |
| `location` | Max 150 characters |
| `minSalary`, `maxSalary` | Must not be negative; `minSalary` must not exceed `maxSalary` |
| `salaryCurrency` | 3-letter ISO 4217 code (e.g. `USD`, `INR`, `EUR`); defaults to `USD` |
| `appliedDate` | Must not be in the future; defaults to today |
| `email` (register) | Required, valid email address |
| `password` (register) | 6–100 characters |

List endpoints (`GET /api/v1/applications`) also validate their query parameters:

| Parameter | Rule |
|---|---|
| `page` | `0` or greater |
| `size` | 1–100 |
| `sortBy` | One of `createdAt`, `updatedAt`, `appliedDate`, `deadline`, `companyName`, `jobTitle`, `location`, `status`, `employmentType`, `minSalary`, `maxSalary` |
| `sortDir` | `asc` or `desc` |

Error status codes used across the API:

| Status | When |
|---|---|
| `400` | Validation failure, invalid business input, malformed JSON, or a bad/missing query parameter |
| `401` | Missing/invalid JWT or wrong login credentials |
| `403` | Authenticated but not permitted to perform the action |
| `404` | Resource not found, or owned by another user |
| `409` | Duplicate resource (e.g. email already registered) |
| `413` | Uploaded file larger than the 15 MB limit |
| `500` | Unexpected server error (details are logged server-side, never returned to the client) |

---

## ⚙️ Configuration

Key settings live in `src/main/resources/application.properties` and can be overridden with environment variables or `-D` flags:

| Property | Default | Purpose |
|---|---|---|
| `server.port` | `8080` | HTTP port |
| `application.security.jwt.expiration` | `86400000` | JWT lifetime in ms (24 h) |
| `application.storage.upload-dir` | `uploads` | Where uploaded documents are stored |
| `spring.servlet.multipart.max-file-size` | `15MB` | Max upload size |
| `application.reminders.cron` | `0 0 * * * *` | Interview reminder scan schedule (hourly) |
| `application.reminders.lookahead-hours` | `24` | How far ahead reminders look |

> ⚠️ The bundled JWT secret is for local development only. Override `application.security.jwt.secret-key` (e.g. `APPLICATION_SECURITY_JWT_SECRET_KEY` env var) before deploying anywhere public.

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

### 5. Document & Resume Attachments

#### Upload a Document (Multipart)
```bash
curl -X POST http://localhost:8080/api/v1/applications/1/documents \
  -H "Authorization: Bearer <TOKEN>" \
  -F "file=@/path/to/resume.pdf" \
  -F "documentType=RESUME"
```
*(Document types: `RESUME`, `COVER_LETTER`, `OFFER_LETTER`, `TRANSCRIPT`, `PORTFOLIO`, `OTHER`)*

#### List Documents for an Application
```bash
curl -X GET http://localhost:8080/api/v1/applications/1/documents \
  -H "Authorization: Bearer <TOKEN>"
```

#### Download a Document
```bash
curl -X GET http://localhost:8080/api/v1/documents/1/download \
  -H "Authorization: Bearer <TOKEN>" \
  -O -J
```

#### Delete a Document
```bash
curl -X DELETE http://localhost:8080/api/v1/documents/1 \
  -H "Authorization: Bearer <TOKEN>"
```

---

### 6. Analytics & Dashboard Stats

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

### 7. Scheduled Interview Reminders & Alerts

#### List User Notifications
```bash
# Get all notifications (or ?unreadOnly=true for unread only)
curl -X GET "http://localhost:8080/api/v1/notifications?unreadOnly=false" \
  -H "Authorization: Bearer <TOKEN>"
```

#### Get Unread Count
```bash
curl -X GET http://localhost:8080/api/v1/notifications/unread-count \
  -H "Authorization: Bearer <TOKEN>"
```

#### Mark Notification as Read
```bash
curl -X PATCH http://localhost:8080/api/v1/notifications/1/read \
  -H "Authorization: Bearer <TOKEN>"
```

#### Trigger On-Demand Reminder Scan
```bash
curl -X POST http://localhost:8080/api/v1/notifications/check-now \
  -H "Authorization: Bearer <TOKEN>"
```

---

### 8. CSV Data Export

#### Export Job Applications as CSV
```bash
# Export all applications (or with optional ?status=... and &search=... query parameters)
curl -X GET "http://localhost:8080/api/v1/applications/export?status=INTERVIEWING" \
  -H "Authorization: Bearer <TOKEN>" \
  -O -J
```
*Saves an RFC 4180-compliant `.csv` file with BOM encoding (e.g., `jobtrack-applications-2026-10-10.csv`), compatible directly with Microsoft Excel, Apple Numbers, and Google Sheets.*

---

## 📖 Interactive Swagger UI & OpenAPI Specification

JobTrack includes SpringDoc OpenAPI 3.0 documentation and an interactive Swagger UI.

### 1. Access Swagger UI in Browser
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### 2. Testing Authenticated Endpoints via Swagger UI
1. Scroll to **`1. Authentication`** -> `POST /api/v1/auth/login`.
2. Click **Try it out** and execute with the demo credentials:
   ```json
   {
     "email": "demo@jobtrack.com",
     "password": "password123"
   }
   ```
3. Copy the returned `token` string from the JSON response.
4. Click the green **Authorize 🔓** button located at the top right of the Swagger UI page.
5. In the **Value** field, enter your token (or `Bearer <your-token>`) and click **Authorize**.
6. All secured endpoints (Applications, Interviews, Notes, Documents, Notifications, Analytics) can now be executed directly with live responses!

---

## 🗃️ H2 In-Memory Database Web Console

To inspect the database tables directly in your browser:
1. Open [http://localhost:8080/h2-console](http://localhost:8080/h2-console).
2. Set **JDBC URL**: `jdbc:h2:mem:jobtrackdb`.
3. Set **User Name**: `sa` (leave Password empty).
4. Click **Connect**.
