# Packaging script for Lakbay Lokal Windows Desktop Application
Write-Host "=========================================================="
Write-Host "  Lakbay Lokal: Building Windows Desktop Application"
Write-Host "=========================================================="

# 1. Verify bin and assets
if (-not (Test-Path "bin\com\lakbaylokal\Main.class")) {
    Write-Host "[Packager] Compiling Java backend first..."
    .\powershell.cmd -File build.ps1
}

# 2. Package into unpacked directory first
Write-Host "[Packager] Packing Windows application directory (dist/win-unpacked)..."
& ".\node_modules\.bin\electron-builder.cmd" --win --dir

if ($LASTEXITCODE -eq 0) {
    Write-Host "[Packager] Successfully generated unpacked desktop build!"
    Get-ChildItem "dist\win-unpacked" | Select-Object Name, Length
} else {
    Write-Error "[Packager] Directory packaging failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}
