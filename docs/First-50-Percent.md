# Microloan: first 50% checkpoint

This checkpoint implements registration/login, borrower profiles, KYC submission and officer verification, loan applications, explainable risk assessment, and officer approval/rejection. Existing team documentation and the original README are preserved. No repayment or overdue modules are included.

## Architecture and stack

Java 25 / Spring Boot 4.1.1 REST backend, Spring Security and Auth0 java-jwt, JPA/Hibernate, PostgreSQL 18, Jakarta Validation, springdoc 3.1.1. React JavaScript / Vite / React Router / Fetch frontend. Official springdoc documentation confirms its 3.x line supports Spring Boot 4: https://springdoc.org/ .

Requests pass through JWT authentication, role checks, controllers, transactional services and JPA repositories. DTOs are returned instead of entities. KYC metadata lives in SQL; file contents live in a private upload directory.

Directories:

- backend/src/main/java/com/microloan/backend: config, controller, dto, entity, error, repository, security, service.
- backend/src/test: isolated H2 tests and a test-only browser-demo launcher.
- frontend/src: api, context, components, layouts, pages, styles.
- frontend/e2e: browser approval workflow.
- scripts: secure local startup and isolated demo startup.
- docs/postman: API collection; docs/demo: fictional fixture source and ignored generated images.

## Local PostgreSQL setup

Ensure PostgreSQL 18 is running. In psql, connected as a user permitted to create databases:

~~~sql
CREATE DATABASE microloan_db;
~~~

The backend defaults to localhost:5432, database microloan_db, username postgres. It uses the existing Hibernate ddl-auto=update setting. Database creation is manual; the application never drops PostgreSQL tables. Use a dedicated development database and backups. Versioned migrations remain future production work.

## Environment and startup (PowerShell)

Use two terminals from the repository root.

~~~powershell
# Add the installed Maven to this terminal if necessary.
$env:MAVEN_HOME = 'C:\Users\harap\Downloads\apache-maven-3.10.0-bin\apache-maven-3.10.0'
$env:Path = "$env:MAVEN_HOME\bin;$env:Path"
$env:JWT_SECRET = [Environment]::GetEnvironmentVariable('JWT_SECRET', 'User')
# This script prompts securely for DB_PASSWORD if it is absent.
.\scripts\Start-Backend.ps1
~~~

~~~powershell
cd frontend
npm install
npm run dev
~~~

Backend: http://localhost:8080 . Frontend: http://localhost:5173 . The dev server binds localhost (works around an observed Windows IPv4 binding error). Stop each server with Ctrl+C.

Required variables: DB_PASSWORD, JWT_SECRET. The JWT key should contain at least 32 random bytes; do not use the test key. Optional: UPLOAD_DIR (default uploads/kyc, resolved from backend working directory), CORS_ORIGINS (comma-separated origins), SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SERVER_PORT. Frontend: copy frontend/.env.example to frontend/.env only if the API base changes. VITE_API_BASE_URL defaults to http://localhost:8080 and is embedded at build time. Never put secrets in a VITE_ variable.

### Safe officer bootstrap

Only the dev profile enables bootstrap. All three variables are required. Public registration always creates a BORROWER. An existing email is never overwritten or promoted.

~~~powershell
$env:APP_OFFICER_NAME = Read-Host 'Officer name'
$env:APP_OFFICER_EMAIL = Read-Host 'Officer email'
$taskOfficerSecret = Read-Host 'Officer password (8+ characters, at most 72 UTF-8 bytes)' -AsSecureString
$env:APP_OFFICER_PASSWORD = [System.Net.NetworkCredential]::new('', $taskOfficerSecret).Password
Remove-Variable taskOfficerSecret
.\scripts\Start-Backend.ps1
# After stopping the server:
Remove-Item Env:APP_OFFICER_PASSWORD
~~~

JWT_SECRET and DB_PASSWORD retain their environment placeholders in application.properties. Do not print them or put them in source, screenshots, Postman exports, or documentation. No officer credentials are shipped.

### API documentation

With the dev profile: http://localhost:8080/swagger-ui/index.html (also /swagger-ui.html) and /v3/api-docs. Select a group and use Authorize with your JWT. Authentication operations do not require authorization; all borrower/officer operations enforce their roles independently. Swagger is disabled outside dev by default. The test profile enables it only for verification.

## Roles and ownership

BORROWER: own profile, KYC files and loan records only. LOAN_OFFICER and ADMIN: all KYC and loan review operations. The UI redirects inappropriate roles; the server independently enforces access. Disabled users are rejected even with an unexpired token. Tokens are held in sessionStorage and expire after 24 hours. Logout removes the local session; token revocation is not implemented.

## API inventory

| Method | Path | Access / purpose |
| --- | --- | --- |
| POST | /api/auth/register | Public borrower registration |
| POST | /api/auth/login | Public login |
| PUT, GET | /api/borrower/profile | Own profile |
| POST, GET | /api/borrower/kyc | Multipart upload / list own KYC |
| GET | /api/borrower/kyc/{id}/file?download=false | Own authorized file |
| POST, GET | /api/borrower/loans | Submit / list own loans |
| GET | /api/borrower/loans/{id} | Own loan |
| GET | /api/borrower/loans/{id}/details | Own loan, profile, masked KYC, risk and decision |
| GET | /api/borrower/loans/{id}/risk | Own risk result |
| GET | /api/borrower/loans/{id}/decision | Own decision; null until decided |
| PATCH | /api/borrower/loans/{id}/cancel | Cancel active own loan |
| GET | /api/officer/kyc?status=PENDING | Officer list; omit status for all |
| GET | /api/officer/kyc/{id} | Masked KYC metadata |
| GET | /api/officer/kyc/{id}/file?download=false | Authorized document view/download |
| PATCH | /api/officer/kyc/{id}/verify | Verify PENDING document |
| PATCH | /api/officer/kyc/{id}/reject | Reject PENDING; body: reason |
| GET | /api/officer/loans?status=SUBMITTED | Officer queue; omit status for all |
| GET | /api/officer/loans/{id} | Full review details |
| PATCH | /api/officer/loans/{id}/review | SUBMITTED to UNDER_REVIEW |
| POST | /api/officer/loans/{id}/risk | Recalculate active loan assessment |
| POST | /api/officer/loans/{id}/decision | Final APPROVED or REJECTED decision |

KYC upload is multipart/form-data with documentType, documentNumber and file. Decision body: decision, reason, approvedAmount and annualInterestRate. Only approval requires the last two fields. Interest is annual percent, 0–60 inclusive; approved amount must be positive, at most the requested amount. Reasons require 5–500 characters (KYC rejection: 5–300).

Errors use timestamp, status, error, message, path, validationErrors. Validation errors are field-specific. Missing/invalid authentication is 401, role denial 403, absent/unowned resource 404, transition/concurrency conflict 409, oversize 413, unsupported media 415. Internal exception details are not returned.

## Frontend routes

Public: /login, /register. The root redirects by session/role; unknown URLs show a 404 page.

Borrower: /borrower (dashboard), /borrower/profile, /borrower/kyc, /borrower/apply, /borrower/loans, /borrower/loans/:id.

Officer/admin: /officer (dashboard), /officer/kyc, /officer/loans, /officer/loans/:id.

The interface includes loading/empty/error/success states, field validation, secure authenticated file fetching, status badges, Indian currency formatting, and confirmation for cancellation and final decisions. Passwords are never persisted by the frontend. Session tokens are cleared on expiry or a server 401.

## Data relationships

User 1–1 BorrowerProfile; User 1–many KycDocument; User 1–many LoanApplication. Each application has at most one RiskAssessment and one immutable LoanDecision. LoanDecision references its deciding User. KYC is unique per user/document type; assessment and decision application IDs are unique. No new repayment-related tables exist.

Transactional writes acquire the borrower row lock before mutating profile/KYC/loan state. Concurrent submissions are serialized before checking the single-active-application rule. Officer actions also lock and refresh the application. Decision creation, refreshed assessment and loan status update commit together. This invariant is enforced through application locking; direct database writes must not bypass it.

## Loan and KYC transitions

Loan: SUBMITTED -> UNDER_REVIEW -> APPROVED or REJECTED. SUBMITTED or UNDER_REVIEW -> CANCELLED by owner. Repeated review and any transition out of a final/cancelled state return 409. Clients cannot set application status. APPROVED and REJECTED are final for this checkpoint; an approved application is no longer an active application for the single-pending-application rule.

Submission requires a profile and at least one PENDING or VERIFIED KYC document. Approval requires a complete profile, an existing risk assessment and at least one VERIFIED document. Risk is refreshed at the human decision to use current income/KYC. A high-risk recommendation does not automatically decide the loan: the officer records a reasoned final decision.

KYC: upload -> PENDING -> VERIFIED or REJECTED. A PENDING/REJECTED document may be replaced and returns to PENDING with a refreshed upload time and cleared rejection/verification fields. VERIFIED documents cannot be replaced. verifiedAt records the officer verification/rejection event timestamp.

## File restrictions and consistency

PDF/JPEG/PNG only, 5 MB per file and 6 MB per multipart request. Magic bytes must match the supplied media type; this is not malware scanning or a complete document authenticity check. Filenames are sanitized; private stored names are UUIDs. Stored paths/names and unmasked identity numbers are never returned. Authorized file responses use the recorded media type, no-store, nosniff and a sandbox policy. Files are outside static resources, path traversal/symlink checks are enforced, and uploads are ignored by Git.

New files are removed on rollback; old files are removed only after a successful SQL commit. Filesystem failures during cleanup are best-effort. A process crash can leave orphan files, so a production installation needs reconciliation/backups. Do not put actual identity documents in this academic system.

## Explainable rules: RULES_V1

BigDecimal calculations use HALF_UP. Principal-only installment = requested amount / tenure, scale 2; interest is excluded. DTI = (existing monthly debt + estimate) / monthly income * 100. Loan/income ratio = amount / (income * 12). Scoring ratios use 12 decimal places; persisted/display ratios use 6. Zero/negative income, invalid amount/tenure and negative debt fail explicitly.

Start at 100; subtract these penalties and clamp to 0–100:

| Factor | Penalty |
| --- | --- |
| DTI <=30%; >30–40%; >40–50%; >50% | 0; 15; 30; 50 |
| Loan / annual income <=0.5; >0.5–1; >1–2; >2 | 0; 10; 20; 35 |
| SALARIED; SELF_EMPLOYED; DAILY_WAGE; OTHER; STUDENT; UNEMPLOYED | 0; 5; 15; 10; 25; 30 |
| At least one VERIFIED document | 0 |
| No verified document | 10; approval recommendation blocked |

75–100 LOW; 50–74 MEDIUM; 0–49 HIGH. HIGH recommends REJECT. LOW with verified KYC recommends APPROVE. Other cases recommend MANUAL_REVIEW. Any rejected KYC is highlighted in the reasons; a separate verified document satisfies the verified-KYC rule. Reasons, score, recommendation, ratios, estimate, evaluatedAt and ruleVersion are stored. Reassessment safely updates the one assessment for an active application; final/cancelled assessments cannot be rerun. Assessment history is not retained.

## Tests and browser demo

~~~powershell
cd backend
mvn clean test
mvn clean package
cd ../frontend
npm install
npm run lint
npm test
npm run build
~~~

Tests use H2 in PostgreSQL compatibility mode, a test-only signing key and temporary upload directories. They never connect to the user's PostgreSQL database. H2 compatibility tests do not prove all PostgreSQL-specific behavior.

For a browser demonstration without any PostgreSQL credentials:

~~~powershell
# Terminal 1, repository root. Stop any real backend on 8080 first.
.\scripts\Start-IsolatedDemo.ps1
# Terminal 2
cd frontend
npm run dev
# Terminal 3
cd frontend
npx playwright install chromium
npm run test:e2e
~~~

The test-only launcher activates test + dev, binds the backend to 127.0.0.1:8080, uses in-memory H2 and a temporary upload directory, and bootstraps one fictional officer with random credentials. Ephemeral credentials are written only under ignored backend/target/e2e-credentials.json, consumed by Playwright, and never printed. Stop the demo after use. Do not expose the test profile or launcher publicly. No fake records are seeded into PostgreSQL. Running Maven clean removes the ephemeral credentials file; rerun the demo launcher before another browser test.

Playwright creates a clearly labelled NOT A REAL ID PNG from docs/demo/identity.html and tests registration/login, profile, upload, automatic risk assessment, officer secure viewing/verification/review/approval and the borrower outcome. It captures desktop/mobile evidence under ignored docs/demo/generated/. These generated fixtures/screenshots are not source files and are not staged.

For manual demonstration, use Ananya Rao, a unique @example.test email, phone 9876501234, Bengaluru/Karnataka/560001, SALARIED, income 45000. Use fictional DEMOP1234A with the generated PNG. Request 100000 for BUSINESS, 24 months, existing debt 3000. Log in as a locally bootstrapped officer, verify the KYC, review the loan, recalculate the assessment and record approval or rejection. Reopen the borrower loan to see the outcome. Test rejection/cancellation with separate applications.

### PostgreSQL 18 verification without your database password

From the repository root, run .\scripts\Test-Postgres.ps1. It uses the installed PostgreSQL 18 binaries to create a completely new temporary cluster, chooses an unused loopback port, creates microloan_checkpoint_test with random credentials, runs the HTTP workflow tests against PostgreSQL, and stops only that temporary instance. It does not access the existing service or microloan_db. Credentials are never printed. The tests only accept the exact local checkpoint database name for this override. Temporary cluster logs/data remain under the OS temp directory; the temporary password file is removed after the run.

## Postman

Import docs/postman/Microloan.postman_collection.json. Supply local borrower/officer passwords and emails in variables (never export live credentials). Variables include baseUrl, borrowerToken, officerToken, loanId and kycId. Successful auth/upload/submission requests populate IDs/tokens locally. Choose the fictional PNG in the multipart file field. The main folder follows approval; alternatives require separate records. Never run both approve and reject on one loan and expect both to succeed.

## Docker (optional local setup)

Docker and Compose must be installed separately. Copy .env.example to .env and set your own secrets without committing it, then:

~~~powershell
docker compose up --build
# Later: stop without deleting volumes
docker compose down
~~~

PostgreSQL 18 uses a persistent postgres_data volume under /var/lib/postgresql; KYC uses kyc_uploads. The database is internal to Compose, avoiding conflict with the host PostgreSQL service. Backend/frontend expose only localhost ports 8080/5173. PostgreSQL and frontend have health checks. Backend starts after PostgreSQL is healthy. The backend image runs as an unprivileged user. Compose enables dev for local Swagger/bootstrap; disable it for production. Docker image builds/container startup must be tested on a Docker-equipped machine.

## Limits and remaining half

This is an academic checkpoint. Before real deployment: replace Hibernate update with reviewed migrations, add TLS/proxy hardening, login throttling, account recovery/email verification, JWT revocation, audit trails and assessment history, sensitive data encryption/retention/consent controls, malware scanning and durable private object storage. The risk estimate is not an interest quote or repayment schedule. Profile/KYC shown in details are current values; full immutable underwriting snapshots are future work.

Outside scope: repayment schedules, installment payments, outstanding balances, overdue tracking, default handling and the remaining notifications/reporting modules. No production/compliance readiness is claimed.
