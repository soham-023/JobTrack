# Changelog

All notable changes to JobTrack are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- GitHub Actions CI workflow running `mvn verify` on every push and pull request.
- Multi-stage `Dockerfile` (non-root runtime, uploads volume) and `.dockerignore`.
- `.editorconfig` for consistent formatting across editors.
- Unit tests for `JobApplicationService`, `NoteService`, `InterviewService`, `FileStorageService`,
  `GlobalExceptionHandler`, request DTO validation, and controller paging/download helpers.
- README sections for validation rules, error status codes, configuration and Docker.

### Changed
- `GET /api/v1/applications` validates `page`, `size` (1–100), `sortBy` (whitelisted fields) and `sortDir`,
  returning `400` instead of a server error for bad values.
- Job URLs must be `http://` or `https://` links; blank URLs are stored as no URL.
- Applied dates in the future are rejected.
- Updating a note with a blank title keeps the existing title.
- Malformed JSON, wrongly typed and missing request parameters return `400`; oversized uploads return `413`.
- Unexpected errors return a generic `500` message and are logged server-side.
- The H2 console only accepts connections from localhost.
- `run.sh` resolves the project root, supports `--offline`/`--help`, and fails fast on errors.

### Fixed
- `HomeController` compilation error.
- Minimum salary greater than maximum salary, negative salaries and invalid currency codes are rejected.
- Download `Content-Disposition` headers are built safely for filenames containing quotes,
  control characters or non-ASCII characters.
- Stored-file lookups and deletions can no longer escape the upload directory.
- Failed file deletions are logged instead of being silently ignored.

## [1.0.0]

### Added
- Initial release: JWT authentication, job application CRUD with filtering and CSV export,
  interview rounds, notes, document attachments, analytics dashboard, scheduled interview
  reminders and notifications, Swagger UI, and a seeded demo account.
