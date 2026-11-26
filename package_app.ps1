# Packaging script for Lakbay Lokal Windows Desktop Application
Write-Host "=========================================================="
Write-Host "  Lakbay Lokal: Building Windows Desktop Application"
Write-Host "=========================================================="

# 1. Verify bin and assets
if (-not (Test-Path "bin\com\lakbaylokal\Main.class")) {
    Write-Host "[Packager] Compiling Java backend first..."
    .\powershell.cmd -File build.ps1
}

# 2. Package into unpacked directory
Write-Host "[Packager] Assembling Windows application directory (dist/win-unpacked)..."
& .\powershell.cmd -File scripts\assemble_unpacked.ps1

if ($LASTEXITCODE -eq 0 -and (Test-Path "dist\win-unpacked\Lakbay Lokal.exe")) {
    Write-Host "[Packager] Successfully generated unpacked desktop build!"
    Get-ChildItem "dist\win-unpacked\*.exe" | Select-Object Name, Length
} else {
    Write-Error "[Packager] Directory packaging failed."
    exit 1
}
