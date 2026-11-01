# Review 2 demo

This checkpoint includes a fictional bank simulator. It compares declared income with independently supplied monthly credits. It does not connect to a real bank or prove a person's actual income.

1. Register a fictional borrower, set monthly income to ₹45,000, submit fictional KYC and request a loan.
2. As the officer, verify KYC. If two borrowers submit the same document type and number, Verify marks the record CONFLICT. Inspect and reject one duplicate with a reason before verifying the other. A borrower can replace a rejected document.
3. Open the officer loan details and run CONFLICT in the bank simulation. Six fictional credits around ₹26,000 produce a major mismatch; approval is blocked.
4. Run REVIEW. Six credits around ₹37,000 produce a moderate mismatch; approval remains blocked.
5. Run MATCH. Six credits around ₹45,000 produce a median of ₹45,000 and VERIFIED. Recalculate risk, move to review, then record the decision.
6. A LOAN_OFFICER may approve up to ₹1,00,000. Above that threshold the existing ADMIN role acts as sanction manager. Officers may reject applications.

For a local demonstration, start the dev backend with APP_SANCTION_NAME, APP_SANCTION_EMAIL and APP_SANCTION_PASSWORD set to create a separate ADMIN account. These must be set before startup, and public registration still creates only BORROWER accounts.

The new income_verifications table stores declared income, median observed income, status, scenario and check time. The API returns the six fictional credits and marks its source FICTIONAL_BANK_SIMULATOR. Changing income after a simulation blocks approval until it is rerun.

Difference thresholds: up to 10% VERIFIED; above 10% through 25% MANUAL_REVIEW; above 25% CONFLICT. Approval requires VERIFIED simulated income and VERIFIED KYC. The existing risk score remains separate from this sanction gate.

Real income verification would require a consent based authorized data provider, account ownership checks, provenance and secure handling. The simulator deliberately does not scrape bank websites or handle bank passwords.
