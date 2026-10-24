$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$envFile = Get-Content '.env'
$line = $envFile | Where-Object { $_ -match '^WORKLYN_HOST_PORT=' } | Select-Object -Last 1
$port = if ($line) { ($line -split '=',2)[1] } else { '8082' }
$health = Invoke-RestMethod -Uri "http://localhost:$port/actuator/health" -TimeoutSec 10
if ($health.status -ne 'UP') { throw "Backend health is not UP: $($health.status)" }
try {
    $null = Invoke-WebRequest -Uri "http://localhost:$port/api/v1/auth/me" -TimeoutSec 10 -ErrorAction Stop
    throw 'Expected HTTP 401 from /api/v1/auth/me without a token.'
} catch {
    $status = $null
    try { $status = [int]$_.Exception.Response.StatusCode } catch { }
    if ($status -ne 401) { throw }
}
Write-Host "PASS: health endpoint is UP at http://localhost:$port/actuator/health" -ForegroundColor Green
Write-Host 'PASS: unauthenticated /api/v1/auth/me returns HTTP 401' -ForegroundColor Green
