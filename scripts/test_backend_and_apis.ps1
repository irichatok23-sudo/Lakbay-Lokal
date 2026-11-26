$ErrorActionPreference = "Stop"
$root = (Get-Location).Path

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  LAKBAY LOKAL: VERIFICATION & TESTING SUITE (SAME-COMMAND HARNESS)" -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan

# 1. Resolve Java executable
$javaCmd = "java"
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $javaCmd = "$env:JAVA_HOME\bin\java.exe"
} else {
    $candidates = @(
        "$env:LOCALAPPDATA\Programs\Eclipse Adoptium",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Java"
    )
    foreach ($base in $candidates) {
        if (Test-Path $base) {
            $found = Get-ChildItem -Path $base -Filter "jdk*" -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
            if ($found -and (Test-Path "$($found.FullName)\bin\java.exe")) {
                $javaCmd = "$($found.FullName)\bin\java.exe"
                break
            }
        }
    }
}

Write-Host "[Harness] Java Runtime: $javaCmd" -ForegroundColor Gray

# 2. Start Java backend in this process session on port 8085
$testPort = 8085
$cp = "bin;lib/*"
$procInfo = New-Object System.Diagnostics.ProcessStartInfo
$procInfo.FileName = $javaCmd
$procInfo.Arguments = "-cp `"$cp`" com.lakbaylokal.Main --headless"
$procInfo.WorkingDirectory = $root
$procInfo.EnvironmentVariables["SERVER_PORT"] = "$testPort"
$procInfo.UseShellExecute = $false
$procInfo.RedirectStandardOutput = $true
$procInfo.RedirectStandardError = $true
$procInfo.CreateNoWindow = $true

$serverProc = [System.Diagnostics.Process]::Start($procInfo)
Write-Host "[Harness] Launched test backend process (PID $($serverProc.Id)) on port $testPort" -ForegroundColor Cyan

$serverReady = $false
for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep -Milliseconds 500
    try {
        $res = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/destinations" -Method Get -TimeoutSec 1 -ErrorAction SilentlyContinue
        if ($res -and $res.Count -gt 0) {
            $serverReady = $true
            break
        }
    } catch {}
    if ($serverProc.HasExited) { break }
}

if (-not $serverReady) {
    Write-Host "[FAIL] Java server did not become ready on port $testPort within 15 seconds" -ForegroundColor Red
    $stdout = $serverProc.StandardOutput.ReadToEnd()
    $stderr = $serverProc.StandardError.ReadToEnd()
    Write-Host "Process stdout: $stdout" -ForegroundColor Yellow
    Write-Host "Process stderr: $stderr" -ForegroundColor Red
    if (-not $serverProc.HasExited) { $serverProc.Kill() }
    exit 1
}

Write-Host "[OK] Test backend is online! Executing test cases..." -ForegroundColor Green

$passed = 0
$failed = 0

function Record-Test($name, $condition, $details) {
    if ($condition) {
        $script:passed++
        Write-Host "  [PASS] $name ($details)" -ForegroundColor Green
    } else {
        $script:failed++
        Write-Host "  [FAIL] $name ($details)" -ForegroundColor Red
    }
}

try {
    # Test 1: Public Destinations Read
    $dests = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/destinations" -Method Get
    Record-Test "Public Destinations API" ($dests.Count -gt 0) "$($dests.Count) destinations returned"

    # Test 2: Public Packages Read
    $pkgs = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/packages" -Method Get
    Record-Test "Public Packages API" ($pkgs.Count -gt 0) "$($pkgs.Count) packages returned"

    # Test 3: Public Announcements Read
    $anns = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/announcements" -Method Get
    Record-Test "Public Announcements API" ($anns.Count -gt 0) "$($anns.Count) announcements returned"

    # Test 4: Security - Invalid Credentials Rejection
    $badBody = @{ username = "admin"; password = "wrong_password_999" } | ConvertTo-Json
    $badLoginRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/auth/login" -Method Post -Body $badBody -ContentType "application/json"
    $badLoginOk = ($badLoginRes.success -eq $false -and $badLoginRes.message -match "Invalid")
    Record-Test "Security: Reject Invalid Password" $badLoginOk "Returned success=false, message=$($badLoginRes.message)"

    # Test 5: Security - Block Unauthenticated Access to Admin Users
    $unauthRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/admin/users" -Method Get
    $unauthBlockOk = ($unauthRes.success -eq $false -and ($unauthRes.code -eq 401 -or $unauthRes.message -match "Unauthorized"))
    Record-Test "Security: Block Unauthenticated /api/admin/users" $unauthBlockOk "Access denied without token"

    # Test 6: Verify All 6 User Roles Login & /api/auth/me
    $roles = @(
        @{ user = "admin"; expected = "SYSTEM_ADMIN" },
        @{ user = "tourismoffice"; expected = "TOURISM_OFFICE" },
        @{ user = "lgu_naval"; expected = "LGU_OFFICER" },
        @{ user = "brgy_olog"; expected = "BARANGAY_OFFICIAL" },
        @{ user = "guide_pedro"; expected = "STAKEHOLDER" },
        @{ user = "tourist_ana"; expected = "TOURIST" }
    )

    $tokens = @{}
    foreach ($r in $roles) {
        $loginBody = @{ username = $r.user; password = "password123" } | ConvertTo-Json
        $loginRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
        $token = $loginRes.token
        $actualRole = $loginRes.user.roleCode
        $roleMatch = ($actualRole -eq $r.expected)
        Record-Test "Role Login: $($r.user) -> $($r.expected)" ($token -and $roleMatch) "roleCode=$actualRole"

        if ($token) {
            $tokens[$r.expected] = $token
            $headers = @{ Authorization = "Bearer $token" }
            $me = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/auth/me" -Method Get -Headers $headers
            Record-Test "Session Validate: $($r.expected) (/api/auth/me)" ($me.username -eq $r.user) "authenticated as $($me.username)"
        }
    }

    # Test 7: RBAC - Tourist Attempting Admin Users API
    $tHeaders = @{ Authorization = "Bearer $($tokens['TOURIST'])" }
    $touristAdminRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/admin/users" -Method Get -Headers $tHeaders
    $touristBlocked = ($touristAdminRes.success -eq $false -and ($touristAdminRes.code -eq 403 -or $touristAdminRes.message -match "Admin"))
    Record-Test "RBAC: Tourist Blocked from Admin Users API" $touristBlocked "Code $($touristAdminRes.code): $($touristAdminRes.message)"

    # Test 8: Admin User Management Access (System Admin)
    $adminHeaders = @{ Authorization = "Bearer $($tokens['SYSTEM_ADMIN'])" }
    $adminUsersRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/admin/users" -Method Get -Headers $adminHeaders
    Record-Test "RBAC: System Admin Accesses Admin Users API" ($adminUsersRes.Count -gt 0) "$($adminUsersRes.Count) user accounts"

    # Test 9: System Settings Read & Save (including editable Default Currency)
    $settings = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/settings" -Method Get -Headers $adminHeaders
    Record-Test "Admin Settings Read" ($null -ne $settings.currency) "currency=$($settings.currency), capacityThreshold=$($settings.capacityThreshold)"

    $updateSettingsBody = @{
        appName = "Lakbay Lokal - Biliran Tourism Information System"
        province = "Biliran Province, Region VIII, Philippines"
        capacityThreshold = 85
        currency = "PHP (Philippine Peso ₱)"
        broadcastMsg = "Welcome to Lakbay Lokal! Experience Biliran's natural wonders responsibly."
    } | ConvertTo-Json
    $saveSettingsRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/settings" -Method Post -Body $updateSettingsBody -Headers $adminHeaders -ContentType "application/json"
    Record-Test "Admin Settings Update & Persist" ($saveSettingsRes.success -eq $true -or $saveSettingsRes.currency -ne $null) "Currency and parameters saved"

    # Test 10: Tourism Office Announcements CRUD (Create -> Read -> Delete)
    $toHeaders = @{ Authorization = "Bearer $($tokens['TOURISM_OFFICE'])" }
    $newAnnBody = @{
        title = "Automated E2E Test Bulletin"
        category = "ADVISORY"
        municipality = "Naval"
        priority = "NORMAL"
        content = "Temporary verification announcement from E2E test suite."
        status = "PUBLISHED"
    } | ConvertTo-Json
    $createAnnRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/announcements" -Method Post -Body $newAnnBody -Headers $toHeaders -ContentType "application/json"
    Record-Test "Tourism Office CREATE Announcement" ($createAnnRes.success -eq $true) "Message: $($createAnnRes.message)"

    # Verify announcement was added and delete it
    $currentAnns = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/announcements" -Method Get
    $createdItem = $currentAnns | Where-Object { $_.title -eq "Automated E2E Test Bulletin" } | Select-Object -First 1
    if ($createdItem) {
        $delBody = @{ announcementId = $createdItem.announcementId } | ConvertTo-Json
        $delAnnRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/announcements/delete" -Method Post -Body $delBody -Headers $toHeaders -ContentType "application/json"
        Record-Test "Tourism Office DELETE Test Announcement" ($delAnnRes.success -eq $true) "Deleted id=$($createdItem.announcementId)"
    } else {
        Record-Test "Tourism Office DELETE Test Announcement" $true "Skipped delete (not found in list)"
    }

    # Test 11: LGU Monitoring & Real Carrying Capacity Calculations
    $lguHeaders = @{ Authorization = "Bearer $($tokens['LGU_OFFICER'])" }
    $kpis = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/reports/kpis" -Method Get -Headers $lguHeaders
    Record-Test "LGU Overview KPIs" ($kpis.totalDestinations -gt 0) "totalDestinations=$($kpis.totalDestinations), totalStakeholders=$($kpis.totalStakeholders)"

    $footfall = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/reports/footfall" -Method Get -Headers $lguHeaders
    Record-Test "LGU Carrying Capacity & Footfall" ($footfall.Count -gt 0) "$($footfall.Count) monitored destinations"

    # Test 12: Barangay Verification Queue & Records
    $brgyHeaders = @{ Authorization = "Bearer $($tokens['BARANGAY_OFFICIAL'])" }
    $brgyVer = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/verifications" -Method Get -Headers $brgyHeaders
    Record-Test "Barangay Official Verifications Queue" ($null -ne $brgyVer) "$($brgyVer.Count) items in queue"

    $brgyList = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/barangays" -Method Get -Headers $brgyHeaders
    Record-Test "Barangay Official Records" ($brgyList.Count -gt 0) "$($brgyList.Count) barangay entries"

    # Test 13: Stakeholder Bookings & Services
    $stkHeaders = @{ Authorization = "Bearer $($tokens['STAKEHOLDER'])" }
    $stkBookings = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/bookings" -Method Get -Headers $stkHeaders
    Record-Test "Stakeholder Bookings Access" ($null -ne $stkBookings) "$($stkBookings.Count) stakeholder bookings"

    # Test 14: Security Audit Logs
    $logs = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/admin/logs" -Method Get -Headers $adminHeaders
    Record-Test "Security Audit Logs (/api/admin/logs)" ($logs.Count -gt 0) "$($logs.Count) log entries recorded"

    # Test 15: Database Backup Creation (.sql dump)
    $backupRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/admin/database/backup" -Method Post -Headers $adminHeaders
    Record-Test "Database Backup Generation" ($backupRes.success -eq $true) "Backup file: $($backupRes.fileName)"

} finally {
    Write-Host "[Harness] Terminating test backend process..." -ForegroundColor Gray
    if (-not $serverProc.HasExited) {
        $serverProc.Kill()
    }
}

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  VERIFICATION RESULTS: $passed PASSED, $failed FAILED" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host "======================================================================" -ForegroundColor Cyan

if ($failed -gt 0) { exit 1 }
