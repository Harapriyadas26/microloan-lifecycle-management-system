$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot -Parent
$taskPgBin = 'C:\Program Files\PostgreSQL\18\bin'
if (-not (Test-Path -LiteralPath (Join-Path $taskPgBin 'initdb.exe'))) { throw 'PostgreSQL 18 binaries not found.' }
$taskMavenPath = (Get-Command mvn -ErrorAction SilentlyContinue).Source
if (-not $taskMavenPath) { $taskMavenPath = 'C:\Users\harap\Downloads\apache-maven-3.10.0-bin\apache-maven-3.10.0\bin\mvn.cmd' }
$taskTempRoot = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
$taskCluster = Join-Path $taskTempRoot ('microloan-checkpoint-pg-' + [Guid]::NewGuid())
New-Item -ItemType Directory -Path $taskCluster | Out-Null
$taskData = Join-Path $taskCluster 'data'
$taskPasswordFile = Join-Path $taskCluster 'init-password'
$taskSecret = [Convert]::ToHexString([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(24))
[IO.File]::WriteAllText($taskPasswordFile, $taskSecret)
$taskListener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback,0)
$taskListener.Start()
$taskPort = $taskListener.LocalEndpoint.Port
$taskListener.Stop()
$taskPriorPgPassword = $env:PGPASSWORD
$taskStarted = $false
try {
    & (Join-Path $taskPgBin 'initdb.exe') -D $taskData -U checkpoint_user --auth=scram-sha-256 --pwfile=$taskPasswordFile --encoding=UTF8 --locale=C *> (Join-Path $taskCluster 'init.log')
    if ($LASTEXITCODE -ne 0) { throw 'Isolated PostgreSQL initialization failed.' }
    # Detach the server's handles so a redirected PowerShell pipeline cannot wait on PostgreSQL indefinitely.
    $taskStartArgs = @('-D', ('"' + $taskData + '"'), '-l', ('"' + (Join-Path $taskCluster 'server.log') + '"'), '-o', ('"-p ' + $taskPort + ' -h 127.0.0.1"'), '-w', 'start')
    $taskProcess = Start-Process -FilePath (Join-Path $taskPgBin 'pg_ctl.exe') -ArgumentList $taskStartArgs -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $taskCluster 'start.log') -RedirectStandardError (Join-Path $taskCluster 'start-error.log')
    $taskStarted = $true
    if (-not $taskProcess.WaitForExit(30000) -or $taskProcess.ExitCode -ne 0) { throw 'Isolated PostgreSQL startup failed.' }
    $env:PGPASSWORD = $taskSecret
    & (Join-Path $taskPgBin 'createdb.exe') -h 127.0.0.1 -p $taskPort -U checkpoint_user microloan_checkpoint_test
    if ($LASTEXITCODE -ne 0) { throw 'Isolated test database creation failed.' }
    $env:MICROLOAN_TEST_PG_URL = "jdbc:postgresql://127.0.0.1:$taskPort/microloan_checkpoint_test"
    $env:MICROLOAN_TEST_PG_PASSWORD = $taskSecret
    & $taskMavenPath -B -f (Join-Path $taskRoot 'backend/pom.xml') '-Dtest=WorkflowIntegrationTests' test
    if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL workflow tests failed.' }
} finally {
    if ($taskStarted -and [IO.Path]::GetFullPath($taskData).StartsWith($taskTempRoot,[StringComparison]::OrdinalIgnoreCase)) {
        & (Join-Path $taskPgBin 'pg_ctl.exe') -D $taskData -m fast -w stop
    }
    if (Test-Path -LiteralPath $taskPasswordFile) { Remove-Item -LiteralPath $taskPasswordFile }
    $env:PGPASSWORD = $taskPriorPgPassword
    Remove-Item Env:MICROLOAN_TEST_PG_URL, Env:MICROLOAN_TEST_PG_PASSWORD -ErrorAction SilentlyContinue
}
