# Implementation file inventory

Branch: Harapriya-dev. Nothing staged, committed, or pushed. No tracked files deleted.

## Modified files (19)

- backend/pom.xml
- backend/src/main/java/com/microloan/backend/config/SecurityConfig.java
- backend/src/main/java/com/microloan/backend/controller/AuthController.java
- backend/src/main/java/com/microloan/backend/controller/BorrowerProfileController.java
- backend/src/main/java/com/microloan/backend/controller/KycController.java
- backend/src/main/java/com/microloan/backend/controller/LoanApplicationController.java
- backend/src/main/java/com/microloan/backend/dto/KycDocumentResponse.java
- backend/src/main/java/com/microloan/backend/dto/LoanApplicationRequest.java
- backend/src/main/java/com/microloan/backend/dto/RegisterRequest.java
- backend/src/main/java/com/microloan/backend/entity/KycDocument.java
- backend/src/main/java/com/microloan/backend/repository/KycDocumentRepository.java
- backend/src/main/java/com/microloan/backend/repository/LoanApplicationRepository.java
- backend/src/main/java/com/microloan/backend/repository/UserRepository.java
- backend/src/main/java/com/microloan/backend/service/AuthService.java
- backend/src/main/java/com/microloan/backend/service/BorrowerProfileService.java
- backend/src/main/java/com/microloan/backend/service/KycService.java
- backend/src/main/java/com/microloan/backend/service/LoanApplicationService.java
- backend/src/main/resources/application.properties
- backend/src/test/java/com/microloan/backend/BackendApplicationTests.java

## Created files (69)

- .env.example
- .gitignore
- backend/.dockerignore
- backend/Dockerfile
- backend/src/main/java/com/microloan/backend/config/OfficerBootstrap.java
- backend/src/main/java/com/microloan/backend/config/OpenApiConfig.java
- backend/src/main/java/com/microloan/backend/controller/BorrowerLoanDetailsController.java
- backend/src/main/java/com/microloan/backend/controller/OfficerKycController.java
- backend/src/main/java/com/microloan/backend/controller/OfficerLoanController.java
- backend/src/main/java/com/microloan/backend/dto/LoanDecisionRequest.java
- backend/src/main/java/com/microloan/backend/dto/LoanDecisionResponse.java
- backend/src/main/java/com/microloan/backend/dto/LoanDetailsResponse.java
- backend/src/main/java/com/microloan/backend/dto/ReasonRequest.java
- backend/src/main/java/com/microloan/backend/dto/RiskAssessmentResponse.java
- backend/src/main/java/com/microloan/backend/entity/DecisionType.java
- backend/src/main/java/com/microloan/backend/entity/LoanDecision.java
- backend/src/main/java/com/microloan/backend/entity/RiskAssessment.java
- backend/src/main/java/com/microloan/backend/entity/RiskLevel.java
- backend/src/main/java/com/microloan/backend/entity/RiskRecommendation.java
- backend/src/main/java/com/microloan/backend/error/ApiError.java
- backend/src/main/java/com/microloan/backend/error/GlobalExceptionHandler.java
- backend/src/main/java/com/microloan/backend/repository/LoanDecisionRepository.java
- backend/src/main/java/com/microloan/backend/repository/RiskAssessmentRepository.java
- backend/src/main/java/com/microloan/backend/service/LoanDecisionService.java
- backend/src/main/java/com/microloan/backend/service/RiskEvaluationService.java
- backend/src/main/resources/application-dev.properties
- backend/src/test/java/com/microloan/backend/DemoApplication.java
- backend/src/test/java/com/microloan/backend/RiskEvaluationTests.java
- backend/src/test/java/com/microloan/backend/WorkflowIntegrationTests.java
- backend/src/test/resources/application-test.properties
- docker-compose.yml
- docs/First-50-Percent.md
- docs/Implementation-Files.md
- docs/Verification.md
- docs/demo/identity.html
- docs/postman/Microloan.postman_collection.json
- frontend/.dockerignore
- frontend/.env.example
- frontend/Dockerfile
- frontend/e2e/workflow.spec.js
- frontend/eslint.config.js
- frontend/index.html
- frontend/nginx.conf
- frontend/package-lock.json
- frontend/package.json
- frontend/playwright.config.js
- frontend/src/App.jsx
- frontend/src/api/apiClient.js
- frontend/src/components/ProtectedRoute.jsx
- frontend/src/components/RoleRoute.jsx
- frontend/src/components/UI.jsx
- frontend/src/components/routes.test.jsx
- frontend/src/context/AuthContext.jsx
- frontend/src/layouts/Workspace.jsx
- frontend/src/main.jsx
- frontend/src/pages/AuthPage.jsx
- frontend/src/pages/Dashboard.jsx
- frontend/src/pages/KycPage.jsx
- frontend/src/pages/LoanApplicationPage.jsx
- frontend/src/pages/LoanDetailsPage.jsx
- frontend/src/pages/LoansPage.jsx
- frontend/src/pages/OfficerKycPage.jsx
- frontend/src/pages/ProfilePage.jsx
- frontend/src/styles/global.css
- frontend/src/testSetup.js
- frontend/vite.config.js
- scripts/Start-Backend.ps1
- scripts/Start-IsolatedDemo.ps1
- scripts/Test-Postgres.ps1

Generated binaries, test credentials, uploads, build outputs and screenshots are ignored and not included.
