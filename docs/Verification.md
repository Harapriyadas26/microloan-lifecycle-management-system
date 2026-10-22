# Verification record - 23 October 2026

## Implemented and exercised

All six checkpoint modules are implemented: registration/login, borrower profile, KYC submission/verification, loan application, explainable risk evaluation, officer approval/rejection. Repayment and overdue modules remain outside scope.

## Commands actually run

| Check | Result |
| --- | --- |
| Existing backend: mvn -DskipTests compile | Passed before implementation |
| Final backend: mvn clean test | 19 tests passed, 0 failures/errors |
| Final backend: mvn clean package | 19 tests passed; executable JAR built |
| scripts/Test-Postgres.ps1 | 13 HTTP workflow tests passed against PostgreSQL 18.6 |
| Frontend npm install | Succeeded; 0 reported vulnerabilities |
| Frontend npm run lint | Passed |
| Frontend npm test | 9 tests passed |
| Frontend npm run build | Passed |
| Frontend npm run test:e2e | Chromium borrower/officer workflow passed |
| Swagger UI /swagger-ui/index.html | HTTP 200 during isolated demo |
| OpenAPI /v3/api-docs | HTTP 200; generated API paths inspected |
| Docker Compose YAML | Parsed; db/backend/frontend services present |
| Docker image metadata | All referenced tags returned HTTP 200 |
| Docker container build/start | Not run: Docker is unavailable |

Maven was invoked using the installed executable at C:/Users/harap/Downloads/apache-maven-3.10.0-bin/apache-maven-3.10.0/bin/mvn.cmd, with backend/pom.xml. The broken Windows Maven wrapper was not used.

## Coverage and workflow evidence

The backend HTTP tests exercise registration, duplicate emails, valid/invalid/disabled/expired authentication, profile creation/update and validation, upload content/type/size checks, KYC replacement and transitions, secure file retrieval and ownership, loan validation, duplicate active applications, concurrent submissions, cancellation, officer authorization, review, approval requiring verified KYC, rejection reason, final immutability, safe development bootstrap, and rollback file cleanup. Risk tests cover DTI/annual-income boundaries, classification boundaries, employment/KYC effects and invalid income/tenure.

The Chromium workflow uses the React forms and actual HTTP APIs: register -> login -> save profile -> upload fictional PNG -> submit -> see automatic risk -> officer login -> securely view KYC -> verify -> review -> recalculate -> approve -> borrower sees decision. Desktop/mobile screenshots were visually inspected. The mobile viewport was 390 x 844, with no document-width overflow. The final browser run completed with no collected page errors.

Generated local evidence (ignored by Git):

- docs/demo/generated/not-a-real-id.png
- docs/demo/generated/borrower-assessment.png
- docs/demo/generated/borrower-approved.png
- docs/demo/generated/mobile-dashboard.png

## Isolation and remaining limits

H2 tests use temporary uploads. PostgreSQL testing used a NEW temporary PostgreSQL 18.6 cluster on an ephemeral loopback port and a dedicated microloan_checkpoint_test database with random credentials. The temporary server was stopped and its password file removed. The user's existing PostgreSQL service stayed running and microloan_db was not accessed or changed.

Direct startup against the existing microloan_db is not verified because DB_PASSWORD was absent from this session and user/machine environment. scripts/Start-Backend.ps1 prompts for it securely in the user's terminal. Docker builds/startup remain unverified. The run guide documents academic/security limitations and modules outside this checkpoint.

Temporary demo servers were stopped after verification so ports 8080/5173 are available for normal startup. Existing repository work, original README and team documentation were preserved. No tracked files were deleted, no files were staged, and nothing was committed or pushed. Branch remains Harapriya-dev.
