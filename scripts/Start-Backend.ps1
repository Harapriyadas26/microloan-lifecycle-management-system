$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot -Parent
$taskMaven = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $taskMaven) {
    $taskMavenPath = 'C:\Users\harap\Downloads\apache-maven-3.10.0-bin\apache-maven-3.10.0\bin\mvn.cmd'
    if (-not (Test-Path -LiteralPath $taskMavenPath)) { throw 'Maven is missing. Add Maven bin to PATH.' }
} else { $taskMavenPath = $taskMaven.Source }
if (-not $env:JWT_SECRET) { $env:JWT_SECRET = [Environment]::GetEnvironmentVariable('JWT_SECRET','User') }
if (-not $env:JWT_SECRET) { throw 'Set JWT_SECRET securely before starting the backend.' }
if (-not $env:DB_PASSWORD) {
    $taskSecurePassword = Read-Host 'Enter PostgreSQL password' -AsSecureString
    $env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $taskSecurePassword).Password
    Remove-Variable taskSecurePassword
}
try {
    & $taskMavenPath -f (Join-Path $taskRoot 'backend/pom.xml') spring-boot:run '-Dspring-boot.run.profiles=dev'
} finally { Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue }

