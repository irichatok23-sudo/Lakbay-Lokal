$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  LAKBAY LOKAL: COMMUNITY-BASED TOURISM INFORMATION SYSTEM" -ForegroundColor Yellow
Write-Host "  Detecting Java Environment..." -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$javacCmd = "javac"
$javaCmd = "java"

# 1. Check if javac is in PATH
if (-not (Get-Command "javac" -ErrorAction SilentlyContinue)) {
    # 2. Check JAVA_HOME
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
        $javacCmd = "$env:JAVA_HOME\bin\javac.exe"
        $javaCmd = "$env:JAVA_HOME\bin\java.exe"
    } else {
        # 3. Search common JDK locations
        $jdkDirs = @(
            "C:\Program Files\Java",
            "C:\Program Files\Eclipse Adoptium",
            "C:\Program Files\Microsoft",
            "C:\Program Files\Amazon Corretto",
            "C:\Program Files\Zulu",
            "C:\Program Files\BellSoft",
            "$env:USERPROFILE\.jdks",
            "$env:LOCALAPPDATA\Programs\Eclipse Adoptium"
        )
        foreach ($d in $jdkDirs) {
            if (Test-Path $d) {
                $found = Get-ChildItem -Path $d -Filter "jdk*" -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
                if ($found -and (Test-Path "$($found.FullName)\bin\javac.exe")) {
                    $javacCmd = "$($found.FullName)\bin\javac.exe"
                    $javaCmd = "$($found.FullName)\bin\java.exe"
                    break
                }
            }
        }
    }
}

Write-Host "[OK] Using Java Compiler: $javacCmd" -ForegroundColor Green
Write-Host "[OK] Using Java Runtime:  $javaCmd" -ForegroundColor Green

# Create bin directory
if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

# Copy resources
if (Test-Path "src/main/resources") {
    Copy-Item -Path "src/main/resources/*" -Destination "bin" -Recurse -Force
}

Write-Host "[1/2] Compiling Java source files..." -ForegroundColor Green

# Collect source files (excluding standalone Spring wrappers if building offline)
$sources = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | Where-Object {
    $_.Name -notmatch "(LakbayWebApplication|WebMvcConfig|SpringApiController)"
} | ForEach-Object {
    "`"$($_.FullName.Replace('\', '/'))`""
}
[System.IO.File]::WriteAllLines((Join-Path $root "sources.txt"), $sources, [System.Text.Encoding]::ASCII)

& $javacCmd -encoding UTF-8 -cp "lib/*" -d "bin" "@sources.txt"
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    Remove-Item "sources.txt" -ErrorAction SilentlyContinue
    exit 1
}
Remove-Item "sources.txt" -ErrorAction SilentlyContinue
if (Test-Path "target/classes") {
    Copy-Item -Path "bin/*" -Destination "target/classes" -Recurse -Force
}

Write-Host "[2/2] Checking MySQL and Launching Lakbay Lokal Portal..." -ForegroundColor Green

# Verify MySQL connection or auto-start WAMP MySQL if available
$mysqlReady = $false
try {
    $tcp = New-Object System.Net.Sockets.TcpClient
    $iar = $tcp.BeginConnect("127.0.0.1", 3306, $null, $null)
    $success = $iar.AsyncWaitHandle.WaitOne(800, $false)
    if ($success -and $tcp.Connected) {
        $tcp.EndConnect($iar)
        $mysqlReady = $true
    }
    $tcp.Close()
} catch {}

if (-not $mysqlReady) {
    $wampMysqld = "c:\wamp64\bin\mysql\mysql8.4.7\bin\mysqld.exe"
    if (Test-Path $wampMysqld) {
        Write-Host "[INFO] Starting local MySQL service (port 3306)..." -ForegroundColor Yellow
        Start-Process $wampMysqld -ArgumentList "--defaults-file=`"c:\wamp64\bin\mysql\mysql8.4.7\my.ini`"", "--console" -WindowStyle Hidden
        Start-Sleep -Seconds 2
    } else {
        Write-Host "[WARN] MySQL port 3306 is not open. Please ensure MySQL (WAMP/XAMPP) is running." -ForegroundColor Yellow
    }
}

$cp = "bin"
if (Test-Path "lib\*.jar") { $cp += ";lib/*" }
$m2Jars = Get-ChildItem -Path "$env:USERPROFILE\.m2\repository\com\mysql\mysql-connector-j" -Filter "*.jar" -Recurse -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
if ($m2Jars) { $cp += ";$($m2Jars -join ';')" }

Start-Process $javaCmd -ArgumentList "-cp", "`"$cp`"", "com.lakbaylokal.Main", "--headless" -WindowStyle Hidden
if (Test-Path "dist\win-unpacked\Lakbay Lokal.exe") {
    Start-Process "dist\win-unpacked\Lakbay Lokal.exe"
} elseif (Test-Path "node_modules\electron\dist\electron.exe") {
    Start-Process "node_modules\electron\dist\electron.exe" -ArgumentList "."
}
Write-Host "[SUCCESS] Lakbay Lokal Standalone Desktop Application started!" -ForegroundColor Cyan
Write-Host "[INFO] REST API Backend active on http://127.0.0.1:8080/" -ForegroundColor Green
