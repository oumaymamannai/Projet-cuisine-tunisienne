param(
    [string]$DbUser = "root",
    [string]$DbPassword = "",
    [switch]$NoBrowser
)

$ErrorActionPreference = "Stop"

function Step($message) {
    Write-Host "`n[STEP] $message" -ForegroundColor Cyan
}

function Ok($message) {
    Write-Host "[OK] $message" -ForegroundColor Green
}

function Warn($message) {
    Write-Host "[WARN] $message" -ForegroundColor Yellow
}

function Fail($message) {
    Write-Host "[ERROR] $message" -ForegroundColor Red
    exit 1
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $projectRoot "backend"
$sqlFile = Join-Path $backendDir "database\mondelys_db.sql"
$mysqlBin = "C:\tools\mysql\current\bin"

if (-not (Test-Path $backendDir)) {
    Fail "Dossier backend introuvable: $backendDir"
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Fail "Java introuvable. Installez Java 17+ puis relancez."
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Fail "Maven introuvable. Installez Maven puis relancez."
}

Step "Nettoyage du serveur (port 8080)"
$listener = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($listener) {
    $procId = $listener.OwningProcess
    try {
        $proc = Get-Process -Id $procId -ErrorAction Stop
        Stop-Process -Id $procId -Force
        Ok "Processus arrêté sur 8080: $($proc.ProcessName) (PID $procId)"
    }
    catch {
        Warn "Impossible d'arrêter PID $procId."
    }
}
else {
    Ok "Aucun processus sur le port 8080"
}

Step "Préparation MySQL CLI"
if (Test-Path (Join-Path $mysqlBin "mysql.exe")) {
    $env:Path += ";$mysqlBin"
    Ok "MySQL CLI détecté dans $mysqlBin"
}
elseif (-not (Get-Command mysql -ErrorAction SilentlyContinue)) {
    Warn "mysql CLI non détecté. Initialisation SQL ignorée, Spring Boot utilisera la connexion DB configurée."
}

Step "Initialisation base de données"
$mysqlCommand = Get-Command mysql -ErrorAction SilentlyContinue
if ($mysqlCommand -and (Test-Path $sqlFile)) {
    try {
        $env:MYSQL_PWD = $DbPassword
        Get-Content $sqlFile | mysql -u $DbUser | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "mysql returned $LASTEXITCODE"
        }
        Ok "Base mondelys_db et tables prêtes"
    }
    catch {
        Fail "Échec initialisation SQL. Vérifiez DbUser/DbPassword."
    }
}
else {
    Warn "Initialisation SQL sautée (mysql CLI ou fichier SQL manquant)."
}

Step "Compilation backend"
Push-Location $backendDir
try {
    mvn -DskipTests compile
    if ($LASTEXITCODE -ne 0) {
        throw "compile failed"
    }
    Ok "Compilation réussie"
}
catch {
    Pop-Location
    Fail "Compilation Maven échouée"
}

Step "Lancement backend Spring Boot"
$env:DB_USERNAME = $DbUser
$env:DB_PASSWORD = $DbPassword
if (-not $env:JWT_SECRET) {
    $env:JWT_SECRET = "replace_with_a_very_long_random_secret_key_at_least_32_chars"
}

Write-Host "`n===============================" -ForegroundColor DarkCyan
Write-Host "SITE:   http://localhost:8080/index.html" -ForegroundColor White
Write-Host "ADMIN:  http://localhost:8080/admin" -ForegroundColor White
Write-Host "LOGIN:  admin@mondelys.tn" -ForegroundColor White
Write-Host "PASS:   Admin2026!" -ForegroundColor White
Write-Host "===============================`n" -ForegroundColor DarkCyan

if (-not $NoBrowser) {
    Start-Process "http://localhost:8080/admin" | Out-Null
}

mvn spring-boot:run
$runExitCode = $LASTEXITCODE
Pop-Location
exit $runExitCode
