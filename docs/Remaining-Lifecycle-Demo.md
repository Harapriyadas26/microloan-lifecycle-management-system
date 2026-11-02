# Remaining lifecycle: academic ledger

The project now records the steps after approval. These records simulate operations; no money is transferred and no payment gateway or bank API is connected.

## Local demo

1. Start the normal PostgreSQL backend and frontend. Set all three APP_SANCTION_NAME, APP_SANCTION_EMAIL and APP_SANCTION_PASSWORD environment variables before starting the backend with the dev profile to create a separate admin account. Public registration cannot create an admin.
2. Complete the borrower application, KYC, bank simulation and approval workflow. Keep the approved loan details page open.
3. Sign in as ADMIN and open the same loan. Enter a unique fictional disbursement reference such as DEMO-DISB-0001. Click Record disbursement. A loan account and monthly schedule appear.
4. Sign in as an officer. Enter a unique payment reference such as DEMO-PAY-0001 and a partial amount. The remaining balance and first installment update. Duplicate references are rejected.
5. To close the loan, record a second fictional payment equal to the displayed outstanding balance. The account becomes CLOSED. More payments are rejected.
6. Open Portfolio report in the officer workspace. It summarizes active and closed accounts, total recorded disbursement, outstanding amounts, overdue installments and installments due in seven days.

The borrower can view their own schedule and payment history. An officer can view all loans and record payments. Only ADMIN can record a disbursement. The application must already have a final approved decision.

## Calculation rules

The schedule uses monthly reducing balance interest and the annual rate recorded in the approval. Monthly rate = annual percent / 1200. A fixed monthly installment is calculated with the standard amortization formula; a zero rate divides principal by tenure. Each month rounds interest to paise. The final installment adjusts principal for rounding so scheduled principal equals the approved amount. Due dates occur every month from the recorded disbursement date.

Payments are allocated to the earliest unpaid installment. A payment cannot exceed the current outstanding balance. Each payment reference is unique. When all scheduled principal, interest and accrued demo charges are paid, the account closes.

An unpaid installment becomes OVERDUE after its due date. After seven full grace days, the system applies a one-time academic late charge of 1% of the unpaid installment amount, rounded to paise. This is a demo policy, not a claim about any lender's terms. The charge is saved on account access or payment; portfolio reporting includes any pending charge in its total without changing records. There is no scheduled background job. The borrower sees a due soon or overdue notice in the loan account panel. No email, SMS or push notification is sent.

New PostgreSQL tables: loan_accounts, loan_installments and loan_payments. They reference the existing approved loan application and user accounts. The backend remains configured with Hibernate schema update for development; versioned migrations and a deployment rollout are still needed before production.

## Boundaries for a real service

Real disbursement requires an authorized payment provider and verified destination account. Real payments require provider callbacks, reconciliation and a tested reversal/refund policy. The current bank income simulator is fictional; real income evidence requires a consent based authorized data source. A production system also needs reviewed policy terms, audit history, migrations, operational backups, access controls, and automated overdue processing.
