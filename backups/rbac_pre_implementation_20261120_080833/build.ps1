$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
if (-not $root) { $root = Get-Location }
Set-Location $root

Write-Host "[Build] Compiling Lakbay Lokal Java backend..." -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

# Copy resources
if (Test-Path "src/main/resources") {
    Copy-Item -Path "src/main/resources/*" -Destination "bin" -Recurse -Force
    if (Test-Path "target/classes") {
        Copy-Item -Path "src/main/resources/*" -Destination "target/classes" -Recurse -Force
    }
}

$javacCmd = "javac"
if (-not (Get-Command $javacCmd -ErrorAction SilentlyContinue)) {
    $adoptium = Join-Path $env:LOCALAPPDATA "Programs\Eclipse Adoptium"
    if (Test-Path $adoptium) {
        $jdkDir = Get-ChildItem -Path $adoptium -Directory | Sort-Object Name -Descending | Select-Object -First 1
        if ($jdkDir) {
            $candidate = Join-Path $jdkDir.FullName "bin\javac.exe"
            if (Test-Path $candidate) { $javacCmd = $candidate }
        }
    }
}

$sources = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | Where-Object {
    $_.Name -notmatch "(LakbayWebApplication|WebMvcConfig|SpringApiController)"
} | ForEach-Object {
    "`"$($_.FullName.Replace('\', '/'))`""
}

$srcFile = Join-Path $root "sources.txt"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllLines($srcFile, $sources, $utf8NoBom)

& $javacCmd -encoding UTF-8 -cp "lib/*" -d "bin" "@$srcFile"
if (Test-Path "target/classes") {
    Copy-Item -Path "bin/*" -Destination "target/classes" -Recurse -Force
}
if ($LASTEXITCODE -eq 0) {
    Write-Host "[Build] Java compilation successful!" -ForegroundColor Green
} else {
    Write-Host "[Build] Java compilation failed with code $LASTEXITCODE" -ForegroundColor Red
}

Remove-Item $srcFile -ErrorAction SilentlyContinue
