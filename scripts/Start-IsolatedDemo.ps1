$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot -Parent
$taskMavenPath = (Get-Command mvn -ErrorAction SilentlyContinue).Source
if (-not $taskMavenPath) { $taskMavenPath = 'C:\Users\harap\Downloads\apache-maven-3.10.0-bin\apache-maven-3.10.0\bin\mvn.cmd' }
$taskPassword = [Convert]::ToHexString([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(20))
$taskKey = [Convert]::ToHexString([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$taskUploads = Join-Path ([IO.Path]::GetTempPath()) ('microloan-browser-' + [Guid]::NewGuid())
$taskTarget = Join-Path $taskRoot 'backend/target'
New-Item -ItemType Directory -Force -Path $taskTarget | Out-Null
@{ email = 'officer.e2e@example.test'; password = $taskPassword } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $taskTarget 'e2e-credentials.json')
$env:APP_OFFICER_NAME = 'Demo Loan Officer'
$env:APP_OFFICER_EMAIL = 'officer.e2e@example.test'
$env:APP_OFFICER_PASSWORD = $taskPassword
# Only isolated H2 data and temporary files; no PostgreSQL access.
$env:SPRING_DATASOURCE_URL = 'jdbc:h2:mem:browser_demo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1'
$env:APP_UPLOAD_DIR = $taskUploads
$env:APP_JWT_SECRET = $taskKey
$env:SERVER_ADDRESS = '127.0.0.1'
try {
    & $taskMavenPath -f (Join-Path $taskRoot 'backend/pom.xml') test-compile spring-boot:test-run '-Dspring-boot.run.main-class=com.microloan.backend.DemoApplication'
} finally {
    Remove-Item Env:APP_OFFICER_PASSWORD, Env:APP_JWT_SECRET, Env:SPRING_DATASOURCE_URL, Env:APP_UPLOAD_DIR, Env:SERVER_ADDRESS -ErrorAction SilentlyContinue
}

