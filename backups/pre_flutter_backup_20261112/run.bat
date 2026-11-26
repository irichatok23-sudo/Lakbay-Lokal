@echo off
setlocal enabledelayedexpansion
title Lakbay Lokal - Launcher
cd /d "%~dp0"

echo ======================================================================
echo   LAKBAY LOKAL: COMMUNITY-BASED TOURISM INFORMATION SYSTEM
echo   Detecting Java Environment...
echo ======================================================================

set "JAVA_CMD=java"
set "JAVAC_CMD=javac"

:: 1. Check if java is already in PATH
where java >nul 2>nul
if %errorlevel% equ 0 goto :java_found

:: 2. Check JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
        set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
        goto :java_found
    )
)

:: 3. Auto-search standard JDK installation directories
for %%D in (
    "C:\Program Files\Java"
    "C:\Program Files\Eclipse Adoptium"
    "C:\Program Files\Microsoft"
    "C:\Program Files\Amazon Corretto"
    "C:\Program Files\Zulu"
    "C:\Program Files\BellSoft"
    "C:\Program Files (x86)\Java"
    "%USERPROFILE%\.jdks"
    "%LOCALAPPDATA%\Programs\Eclipse Adoptium"
) do (
    if exist "%%~D" (
        for /f "delims=" %%F in ('dir /b /ad /o-n "%%~D\jdk*" 2^>nul') do (
            if exist "%%~D\%%F\bin\java.exe" (
                set "JAVA_CMD=%%~D\%%F\bin\java.exe"
                set "JAVAC_CMD=%%~D\%%F\bin\javac.exe"
                goto :java_found
            )
        )
    )
)

:java_found
echo [OK] Using Java: "%JAVA_CMD%"

if not exist "bin" mkdir "bin"

if exist "src\main\resources" (
    xcopy /s /y /q "src\main\resources\*" "bin\" >nul 2>nul
)

echo [1/2] Compiling Java source files...
:: Write all Java source files (excluding standalone Spring-specific wrappers if compiling via javac)
> sources.txt (
    for /r "src\main\java\lakbaylokal" %%F in (*.java) do (
        set "SOURCE_FILE=%%F"
        set "SOURCE_FILE=!SOURCE_FILE:\=/!"
        echo "!SOURCE_FILE!"
    )
    for /r "src\main\java\com\lakbaylokal" %%F in (*.java) do (
        set "SOURCE_FILE=%%F"
        set "SOURCE_FILE=!SOURCE_FILE:\=/!"
        echo "!SOURCE_FILE!" | findstr /i /v "LakbayWebApplication WebMvcConfig SpringApiController" >nul
        if !errorlevel! equ 0 (
            echo "!SOURCE_FILE!"
        )
    )
)

"%JAVAC_CMD%" -encoding UTF-8 -cp "lib/*" -d "bin" @sources.txt
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    del sources.txt >nul 2>nul
    pause
    exit /b 1
)
del sources.txt >nul 2>nul
if exist "target\classes" (
    xcopy /s /y /q "bin\*" "target\classes\" >nul 2>nul
)

echo [2/2] Launching Lakbay Lokal Portal...
set "CP=bin"
if exist "lib\*.jar" set "CP=bin;lib/*"
for /r "%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j" %%J in (*.jar) do (
    if exist "%%J" set "CP=!CP!;%%J"
)
start "" "%JAVA_CMD%" -cp "!CP!" com.lakbaylokal.Main
echo [SUCCESS] Application is now running!
echo [INFO] Web Portal: http://localhost:8080/
exit /b 0
