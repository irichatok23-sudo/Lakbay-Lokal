# Build Windows Portable & NSIS Installer for Lakbay Lokal
Write-Host "=========================================================="
Write-Host "  Lakbay Lokal: Building Windows Installer (.exe)"
Write-Host "=========================================================="

# Build target
Write-Host "[Installer] Packaging Windows Installer & Portable Executable..."
& ".\node_modules\.bin\electron-builder.cmd" --win portable

if ($LASTEXITCODE -eq 0) {
    Write-Host "[Installer] Build completed successfully!"
    Get-ChildItem "dist\*.exe" | Select-Object Name, Length, LastWriteTime
} else {
    Write-Error "[Installer] Installer generation failed with code $LASTEXITCODE"
    exit $LASTEXITCODE
}
