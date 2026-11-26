$ErrorActionPreference = "Stop"
$root = (Get-Location).Path

$dest = Join-Path $root "dist\win-unpacked"
if (-not (Test-Path $dest)) {
    New-Item -ItemType Directory -Path $dest -Force | Out-Null
}

$electronDist = Join-Path $root "node_modules\electron\dist"
Get-ChildItem -Path $electronDist | ForEach-Object {
    Copy-Item -Path $_.FullName -Destination $dest -Recurse -Force
}

$electronExe = Join-Path $dest "electron.exe"
$targetExe = Join-Path $dest "Lakbay Lokal.exe"
if (Test-Path $electronExe) {
    Copy-Item -Path $electronExe -Destination $targetExe -Force
}

$appDir = Join-Path $dest "resources\app"
if (-not (Test-Path $appDir)) {
    New-Item -ItemType Directory -Path $appDir -Force | Out-Null
}

Copy-Item -Path (Join-Path $root "package.json") -Destination $appDir -Force
Copy-Item -Path (Join-Path $root "electron") -Destination $appDir -Recurse -Force
Copy-Item -Path (Join-Path $root "bin") -Destination $appDir -Recurse -Force
Copy-Item -Path (Join-Path $root "lib") -Destination $appDir -Recurse -Force
Copy-Item -Path (Join-Path $root "src") -Destination $appDir -Recurse -Force

Write-Host "Unpacked build assembled successfully in: $dest"
Get-ChildItem -Path $dest -Filter "*.exe" | Format-Table Name, Length, LastWriteTime
