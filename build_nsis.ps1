# Build NSIS Setup Installer
Write-Host "[NSIS] Packaging Windows Setup Installer (.exe)..."
& ".\node_modules\.bin\electron-builder.cmd" --win nsis

if ($LASTEXITCODE -eq 0) {
    Write-Host "[NSIS] NSIS installer created successfully!"
    Get-ChildItem "dist\*.exe" | Select-Object Name, Length, LastWriteTime
}
