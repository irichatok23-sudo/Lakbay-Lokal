@echo off
setlocal
title Lakbay Lokal - Windows Desktop Launcher

echo ==============================================================
echo    LAKBAY LOKAL: COMMUNITY TOURISM INFORMATION SYSTEM
echo               Biliran Province, Philippines
echo               Windows Desktop Application
echo ==============================================================
echo.

set "NODE_DIR=%LOCALAPPDATA%\Microsoft\WinGet\Packages\OpenJS.NodeJS.LTS_Microsoft.Winget.Source_8wekyb3d8bbwe\node-v24.20.0-win-x64"
set "WINGET_LINKS=%LOCALAPPDATA%\Microsoft\WinGet\Links"
set "PATH=%SystemRoot%\System32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SystemRoot%\System32\WindowsPowerShell\v1.0;%NODE_DIR%;%WINGET_LINKS%;C:\Program Files\nodejs;%LOCALAPPDATA%\Programs\nodejs;%APPDATA%\npm;%PATH%"

REM Check if backend is compiled
if not exist "bin\com\lakbaylokal\Main.class" (
    echo [Launcher] Compiling backend and syncing web assets...
    call "%~dp0powershell.cmd" -File "%~dp0build.ps1"
)

echo [Launcher] Launching Lakbay Lokal Desktop Application...
call "%~dp0powershell.cmd" -NoProfile -Command "npm start"

pause
