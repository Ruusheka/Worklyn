$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

function Set-EnvValue([string]$Key, [string]$Value) {
    $path = Join-Path $PSScriptRoot '.env'
    $lines = @()
    if (Test-Path $path) { $lines = @(Get-Content -LiteralPath $path) }
    $pattern = '^' + [regex]::Escape($Key) + '='
    $found = $false
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match $pattern) { $lines[$i] = "$Key=$Value"; $found = $true }
    }
    if (-not $found) { $lines += "$Key=$Value" }
    Set-Content -LiteralPath $path -Value $lines -Encoding ASCII
}

function Wait-ForHealth([int]$Seconds = 180) {
    $deadline = (Get-Date).AddSeconds($Seconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $health = Invoke-RestMethod -Uri "http://localhost:$($env:WORKLYN_HOST_PORT)/actuator/health" -TimeoutSec 3
            if ($health.status -eq 'UP') { return }
        } catch { }
        Start-Sleep -Seconds 2
    }
    Write-Host 'Backend health check timed out. Recent logs:' -ForegroundColor Red
    docker compose logs --tail=100 backend
    throw 'Worklyn backend did not become healthy.'
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker CLI was not found. Install/start Docker Desktop, then run this launcher again.'
}
& docker info *> $null
if ($LASTEXITCODE -ne 0) { throw 'Docker Desktop is not ready. Start Docker Desktop and retry.' }
& docker compose version *> $null
if ($LASTEXITCODE -ne 0) { throw 'Docker Compose v2 is required.' }

if (-not (Test-Path '.env')) {
    $bytes = New-Object byte[] 48
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    $jwtSecret = [Convert]::ToBase64String($bytes)
    Set-Content -LiteralPath '.env' -Encoding ASCII -Value @(
        "APP_JWT_SECRET=$jwtSecret",
        'APP_JWT_EXPIRATION_MS=86400000',
        'WORKLYN_HOST_PORT=8082',
        'APP_BOOTSTRAP_ADMIN_ENABLED=false',
        'APP_BOOTSTRAP_ADMIN_EMAIL=',
        'APP_BOOTSTRAP_ADMIN_PASSWORD='
    )
}
$envText = Get-Content -LiteralPath '.env' -Raw
if ($envText -notmatch '(?m)^APP_JWT_SECRET=\S+') {
    $bytes = New-Object byte[] 48
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    Set-EnvValue 'APP_JWT_SECRET' ([Convert]::ToBase64String($bytes))
}
$envText = Get-Content -LiteralPath '.env' -Raw
if ($envText -notmatch '(?m)^WORKLYN_HOST_PORT=') { Set-EnvValue 'WORKLYN_HOST_PORT' '8082' }
Set-EnvValue 'APP_BOOTSTRAP_ADMIN_ENABLED' 'false'
$portLine = (Get-Content '.env' | Where-Object { $_ -match '^WORKLYN_HOST_PORT=' } | Select-Object -Last 1)
$env:WORKLYN_HOST_PORT = ($portLine -split '=',2)[1]
if ([string]::IsNullOrWhiteSpace($env:WORKLYN_HOST_PORT)) { $env:WORKLYN_HOST_PORT = '8082' }

$firstRun = -not (Test-Path '.person1-bootstrap-complete')
$adminEmail = $null
$adminPassword = $null
$initialLoginSucceeded = $false
$startupError = $null

if ($firstRun) {
    Write-Host ''
    Write-Host 'First run: create the initial Worklyn administrator.' -ForegroundColor Cyan
    $adminEmail = (Read-Host 'Admin email').Trim().ToLowerInvariant()
    if ($adminEmail -notmatch '^[^\s@]+@[^\s@]+\.[^\s@]+$') { throw 'Enter a valid email address.' }
    $securePassword = Read-Host 'Admin password (12-72 UTF-8 bytes; input is hidden)' -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try { $adminPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) }
    if ($adminPassword.Length -lt 12 -or [Text.Encoding]::UTF8.GetByteCount($adminPassword) -gt 72) {
        throw 'Password must be at least 12 characters and no more than 72 UTF-8 bytes.'
    }
    $env:APP_BOOTSTRAP_ADMIN_ENABLED = 'true'
    $env:APP_BOOTSTRAP_ADMIN_EMAIL = $adminEmail
    $env:APP_BOOTSTRAP_ADMIN_PASSWORD = $adminPassword
}
else {
    $env:APP_BOOTSTRAP_ADMIN_ENABLED = 'false'
}

try {
    Write-Host ''
    Write-Host 'Building and starting MongoDB, Redis, and the Person 1 backend...' -ForegroundColor Cyan
    & docker compose up -d --build
    if ($LASTEXITCODE -ne 0) { throw 'docker compose up failed. See the Docker output above.' }
    Wait-ForHealth

    if ($firstRun) {
        $body = @{ email = $adminEmail; password = $adminPassword } | ConvertTo-Json -Compress
        try {
            $login = Invoke-RestMethod -Uri "http://localhost:$($env:WORKLYN_HOST_PORT)/api/v1/auth/login" -Method Post -ContentType 'application/json' -Body $body -TimeoutSec 15
            if (-not $login.token -or -not ($login.user.roles -contains 'ROLE_ADMIN')) { throw 'Login response did not contain an administrator JWT.' }
            $headers = @{ Authorization = "Bearer $($login.token)" }
            $me = Invoke-RestMethod -Uri "http://localhost:$($env:WORKLYN_HOST_PORT)/api/v1/auth/me" -Headers $headers -TimeoutSec 10
            if ($me.email -ne $adminEmail -or -not ($me.roles -contains 'ROLE_ADMIN')) { throw 'JWT /me verification failed.' }
            $initialLoginSucceeded = $true
        }
        catch {
            $startupError = $_
        }
    }
}
catch {
    $startupError = $_
}
finally {
    # Always recreate without bootstrap credentials so the one-time flag and password are not left in the container.
    $env:APP_BOOTSTRAP_ADMIN_ENABLED = 'false'
    Remove-Item Env:APP_BOOTSTRAP_ADMIN_EMAIL -ErrorAction SilentlyContinue
    Remove-Item Env:APP_BOOTSTRAP_ADMIN_PASSWORD -ErrorAction SilentlyContinue
    Set-EnvValue 'APP_BOOTSTRAP_ADMIN_ENABLED' 'false'
    try {
        & docker compose up -d --force-recreate backend *> $null
    } catch { }
    Remove-Item Env:APP_BOOTSTRAP_ADMIN_ENABLED -ErrorAction SilentlyContinue
}

if ($startupError) {
    Write-Host 'Startup/bootstrap did not fully verify. The bootstrap flag has been disabled.' -ForegroundColor Red
    Write-Host 'If this database already had an administrator, use that account instead of trying to create another.' -ForegroundColor Yellow
    docker compose logs --tail=100 backend
    throw $startupError
}

Wait-ForHealth
if ($firstRun) {
    if (-not $initialLoginSucceeded) { throw 'Initial administrator login was not verified; bootstrap-complete marker was not created.' }
    # Verify again after bootstrap has been disabled and the backend recreated.
    $body = @{ email = $adminEmail; password = $adminPassword } | ConvertTo-Json -Compress
    $login = Invoke-RestMethod -Uri "http://localhost:$($env:WORKLYN_HOST_PORT)/api/v1/auth/login" -Method Post -ContentType 'application/json' -Body $body -TimeoutSec 15
    $headers = @{ Authorization = "Bearer $($login.token)" }
    $me = Invoke-RestMethod -Uri "http://localhost:$($env:WORKLYN_HOST_PORT)/api/v1/auth/me" -Headers $headers -TimeoutSec 10
    if ($me.email -ne $adminEmail -or -not ($me.roles -contains 'ROLE_ADMIN')) { throw 'Post-restart JWT verification failed.' }
    Set-Content -LiteralPath '.person1-bootstrap-complete' -Encoding ASCII -Value 'Administrator bootstrap verified; bootstrap disabled.'
    $adminPassword = $null
    $securePassword = $null
}

# Smoke test: /me must reject requests without a JWT.
try {
    $null = Invoke-WebRequest -Uri "http://localhost:$($env:WORKLYN_HOST_PORT)/api/v1/auth/me" -TimeoutSec 10 -ErrorAction Stop
    throw 'Security smoke test failed: /api/v1/auth/me unexpectedly allowed an unauthenticated request.'
}
catch {
    $status = $null
    try { $status = [int]$_.Exception.Response.StatusCode } catch { }
    if ($status -ne 401) { throw }
}

Write-Host ''
Write-Host 'WORKLYN PERSON 1 BACKEND IS UP' -ForegroundColor Green
Write-Host "API:    http://localhost:$($env:WORKLYN_HOST_PORT)"
Write-Host "Health: http://localhost:$($env:WORKLYN_HOST_PORT)/actuator/health"
Write-Host 'Smoke check passed: unauthenticated /api/v1/auth/me returns HTTP 401.'
Write-Host 'To stop it, double-click STOP-PERSON1.bat. Data volumes are preserved.'
