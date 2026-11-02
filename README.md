# Microloan lifecycle management system

An academic web application built with React/Vite, Spring Boot, Spring Security/JWT, Spring Data JPA, and PostgreSQL. Borrowers can register, complete a profile, upload KYC documents, apply for a loan, and track its decision and account. Officers review KYC, inspect risk and fictional income evidence, and record decisions. An ADMIN sanction manager handles higher-value approvals and records fictional disbursements. Officers record fictional payments. The app generates monthly installments, shows overdue amounts, closes paid accounts, and displays a portfolio report.

## Run locally

1. Start PostgreSQL and create `microloan_db` if it does not already exist.
2. In a PowerShell terminal, run `./scripts/Start-Backend.ps1`. The script prompts for the local database password when needed. See [the checkpoint run guide](docs/First-50-Percent.md) for environment variables and dev officer/admin setup.
3. In another terminal, run `cd frontend` followed by `npm install` (first time) and `npm run dev`.
4. Open <http://localhost:5173>.

See [the remaining lifecycle demo](docs/Remaining-Lifecycle-Demo.md) for the approval-to-closure walkthrough and calculation rules. The bank income scenarios, disbursement, payments, and late charge are simulations for demonstration. They do not verify real income or move funds.

## Verify

With Maven on `PATH`, run `mvn -f backend/pom.xml test`; otherwise use your installed `mvn.cmd` path. From `frontend`, run `npm test`, `npm run lint`, and `npm run build`. `./scripts/Test-Postgres.ps1` runs the workflow integration tests against a separate temporary PostgreSQL instance without changing `microloan_db`.
