$ErrorActionPreference = "Stop"
$root = (Get-Location).Path

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  LAKBAY LOKAL: PHASE 1 COMPREHENSIVE VERIFICATION SUITE" -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan

$javaCmd = "java"
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $javaCmd = "$env:JAVA_HOME\bin\java.exe"
}
else {
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


$testPort = 8086
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
    }
    catch {
        # wait
    }
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

Write-Host "[OK] Test backend online on port $testPort! Executing Section E tests..." -ForegroundColor Green

$passed = 0
$failed = 0

function Record-Test($testNum, $name, $condition, $details) {
    if ($condition) {
        $script:passed++
        Write-Host "  [PASS] Test ${testNum}: $name`n         Details: $details" -ForegroundColor Green
    }
    else {
        $script:failed++
        Write-Host "  [FAIL] Test ${testNum}: $name`n         Details: $details" -ForegroundColor Red
    }
}

try {
    # Helper to authenticate
    function Login-User($username, $password = "password123") {
        $body = @{ username = $username; password = $password } | ConvertTo-Json
        $res = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/auth/login" -Method Post -Body $body -ContentType "application/json"
        return $res
    }

    # Log in all roles
    $loginAdmin = Login-User "admin"
    $loginTourism = Login-User "tourismoffice"
    $loginLgu = Login-User "lgu_naval"
    $loginBrgyOlog = Login-User "brgy_olog"
    $loginStakeholder = Login-User "guide_pedro"
    $loginTourist = Login-User "tourist_ana"

    # =========================================================================
    # Test 8: All Six Logins & Desktop Launcher Verification
    # =========================================================================
    $allLoginsOk = ($loginAdmin.success -and $loginAdmin.user.roleCode -eq "SYSTEM_ADMIN") -and
    ($loginTourism.success -and $loginTourism.user.roleCode -eq "TOURISM_OFFICE") -and
    ($loginLgu.success -and $loginLgu.user.roleCode -eq "LGU_OFFICER") -and
    ($loginBrgyOlog.success -and $loginBrgyOlog.user.roleCode -eq "BARANGAY_OFFICIAL") -and
    ($loginStakeholder.success -and $loginStakeholder.user.roleCode -eq "STAKEHOLDER") -and
    ($loginTourist.success -and $loginTourist.user.roleCode -eq "TOURIST")

    $lguMe = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/auth/me" -Method Get -Headers @{ Authorization = "Bearer $($loginLgu.token)" }
    $lguHasMuniId = ($lguMe.municipalityId -eq 1 -and $lguMe.municipalityName -eq "Naval")

    Record-Test "8" "All Six User Role Logins & /api/auth/me municipalityId" ($allLoginsOk -and $lguHasMuniId) "All 6 authenticated correctly. LGU Naval municipalityId=$($lguMe.municipalityId) ($($lguMe.municipalityName))"

    
    $randomSuffix = [Guid]::NewGuid().ToString().Substring(0, 6)
    $stkOlogBody = @{
        username        = "olog_cook_$randomSuffix"
        email           = "olog_$randomSuffix@test.ph"
        ownerName       = "Maria Olog Host"
        businessName    = "Olog Local Seafood & Native Catering $randomSuffix"
        category        = "LOCAL_COOK"
        barangayId      = 1
        contactNumber   = "+63 917 888 1234"
        yearsExperience = 4
        accreditationNo = "BRGY-OLOG-2026-001"
    } | ConvertTo-Json

    $createStkRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/stakeholders" -Method Post -Body $stkOlogBody -Headers @{ Authorization = "Bearer $($loginBrgyOlog.token)" } -ContentType "application/json"
    $newStkId = $createStkRes.profileId

    $verifyBody = @{
        stakeholderId         = $newStkId
        barangayId            = 1
        status                = "APPROVED"
        requirementsSubmitted = "Barangay Clearance, DTI Certificate, Sanitary Permit"
        remarks               = "Fully compliant with local ecotourism standards. Approved by Barangay Official."
    } | ConvertTo-Json

    $verifyRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/verifications" -Method Post -Body $verifyBody -Headers @{ Authorization = "Bearer $($loginBrgyOlog.token)" } -ContentType "application/json"
    $test1Ok = ($verifyRes.success -eq $true)
    Record-Test "1" "Barangay Official approves stakeholder in own barangay (Barangay 1: Ol-og)" $test1Ok "ProfileId: $newStkId, Response: $($verifyRes.message)"

    
    $crossBrgyBody = @{
        stakeholderId = 2
        barangayId    = 6
        status        = "APPROVED"
        remarks       = "Unauthorized cross-barangay evaluation attempt"
    } | ConvertTo-Json

    $test2Blocked = $false
    $test2Msg = ""
    $crossRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/verifications" -Method Post -Body $crossBrgyBody -Headers @{ Authorization = "Bearer $($loginBrgyOlog.token)" } -ContentType "application/json"
    if ($crossRes.success -eq $false -and ($crossRes.code -eq 403 -or $crossRes.message -match "jurisdiction")) {
        $test2Blocked = $true
        $test2Msg = $crossRes.message
    }
    Record-Test "2" "Barangay Official cannot approve stakeholder from another barangay" $test2Blocked "Cross-jurisdiction blocked. $test2Msg"

    
    $tourismVerifyBody = @{
        stakeholderId = $newStkId
        barangayId    = 1
        status        = "APPROVED"
        remarks       = "Tourism Office attempting approval"
    } | ConvertTo-Json

    $test3Blocked = $false
    $test3Msg = ""
    $toVerifyRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/verifications" -Method Post -Body $tourismVerifyBody -Headers @{ Authorization = "Bearer $($loginTourism.token)" } -ContentType "application/json"
    if ($toVerifyRes.success -eq $false -and ($toVerifyRes.code -eq 403 -or $toVerifyRes.message -match "Sole authority")) {
        $test3Blocked = $true
        $test3Msg = $toVerifyRes.message
    }
    Record-Test "3" "Tourism Office cannot approve/reject stakeholder (sole authority rests with Barangay)" $test3Blocked "Denied with message: $test3Msg"

    
    $navalStkBody = @{
        username        = "naval_guide_$randomSuffix"
        email           = "naval_$randomSuffix@test.ph"
        ownerName       = "Naval Local Tour Specialist"
        businessName    = "Naval Heritage & Island Tours $randomSuffix"
        category        = "TOUR_GUIDE"
        barangayId      = 9
        contactNumber   = "+63 920 333 4444"
        yearsExperience = 5
        accreditationNo = "DOT-NAV-2026-01"
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/stakeholders" -Method Post -Body $navalStkBody -Headers @{ Authorization = "Bearer $($loginAdmin.token)" } -ContentType "application/json" | Out-Null

    $lguHeaders = @{ Authorization = "Bearer $($loginLgu.token)" }
    $lguDests = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/destinations" -Method Get -Headers $lguHeaders
    $allNavalDests = $true
    foreach ($d in $lguDests) {
        if ($d.municipalityName -ne "Naval") { $allNavalDests = $false }
    }

    $lguBrgys = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/reports/barangays" -Method Get -Headers $lguHeaders
    $allNavalBrgys = $true
    foreach ($b in $lguBrgys) {
        if ($b.municipality_name -ne "Naval" -and $b.municipalityName -ne "Naval") { $allNavalBrgys = $false }
    }

    $lguStks = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/stakeholders" -Method Get -Headers $lguHeaders
    $allNavalStks = $true
    foreach ($s in $lguStks) {
        if ($s.municipalityName -ne "Naval") { $allNavalStks = $false }
    }

    $test4Ok = ($lguDests.Count -gt 0 -and $allNavalDests) -and ($lguBrgys.Count -gt 0 -and $allNavalBrgys) -and ($lguStks.Count -gt 0 -and $allNavalStks)
    Record-Test "4" "LGU Officer records strictly scoped to assigned municipality (Naval)" $test4Ok "$($lguDests.Count) destinations, $($lguBrgys.Count) barangays, $($lguStks.Count) stakeholders — 100% belonging to Naval"

    
    $badArrivalBody = @{
        destinationId         = 2
        logDate               = (Get-Date).ToString("yyyy-MM-dd")
        localTouristsCount    = 25
        domesticTouristsCount = 10
        foreignTouristsCount  = 2
        totalRevenue          = 1500.00
    } | ConvertTo-Json

    $test5Blocked = $false
    $test5Msg = ""
    $badArrRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/reports/arrival" -Method Post -Body $badArrivalBody -Headers @{ Authorization = "Bearer $($loginBrgyOlog.token)" } -ContentType "application/json"
    if ($badArrRes.success -eq $false -and ($badArrRes.code -eq 403 -or $badArrRes.message -match "authorized barangay")) {
        $test5Blocked = $true
        $test5Msg = $badArrRes.message
    }

   
    $goodArrivalBody = @{
        destinationId         = 1
        logDate               = (Get-Date).ToString("yyyy-MM-dd")
        localTouristsCount    = 12
        domesticTouristsCount = 8
        foreignTouristsCount  = 3
        totalRevenue          = 950.00
    } | ConvertTo-Json

    $goodArrRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/reports/arrival" -Method Post -Body $goodArrivalBody -Headers @{ Authorization = "Bearer $($loginBrgyOlog.token)" } -ContentType "application/json"
    $test5Ok = ($test5Blocked -and $goodArrRes.success -eq $true)
    Record-Test "5" "Arrival logging strictly restricted to local barangay destinations" $test5Ok "Cross-barangay rejected ($test5Msg); local Sambawan Island logged successfully"

   
    $multiItemBooking = @{
        travelDateStart = (Get-Date).AddDays(5).ToString("yyyy-MM-dd")
        travelDateEnd   = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")
        numberOfPax     = 2
        totalAmount     = 3500.00
        customNotes     = "Phase 1 Co-op multi-stakeholder booking test"
        items           = @(
            @{ serviceId = 1; stakeholderId = 1; unitPrice = 1500.00; quantity = 1; subtotal = 1500.00 },
            @{ serviceId = 4; stakeholderId = 2; unitPrice = 2000.00; quantity = 1; subtotal = 2000.00 }
        )
    } | ConvertTo-Json

    $bookRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/bookings" -Method Post -Body $multiItemBooking -Headers @{ Authorization = "Bearer $($loginTourist.token)" } -ContentType "application/json"
    $bookingId = $bookRes.bookingId

    
    $adminBookings = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/bookings" -Method Get -Headers @{ Authorization = "Bearer $($loginAdmin.token)" }
    $targetBooking = $adminBookings | Where-Object { $_.bookingId -eq $bookingId } | Select-Object -First 1
    $item1 = $targetBooking.items | Where-Object { $_.stakeholderId -eq 1 } | Select-Object -First 1
    $item2 = $targetBooking.items | Where-Object { $_.stakeholderId -eq 2 } | Select-Object -First 1

   
    $item1Update = @{ itemId = $item1.itemId; status = "ACCEPTED" } | ConvertTo-Json
    $item1Res = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/bookings/item-status" -Method Post -Body $item1Update -Headers @{ Authorization = "Bearer $($loginStakeholder.token)" } -ContentType "application/json"
    # Stakeholder 1 attempts to 
    modify Item 2 (assigned to Stakeholder 2) -> must be rejected (403)
    $item2Tamper = @{ itemId = $item2.itemId; status = "ACCEPTED" } | ConvertTo-Json
    $item2TamperBlocked = $false
    $tamperRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/bookings/item-status" -Method Post -Body $item2Tamper -Headers @{ Authorization = "Bearer $($loginStakeholder.token)" } -ContentType "application/json"
    if ($tamperRes.success -eq $false -and $tamperRes.code -eq 403) {
        $item2TamperBlocked = $true
    }

    # Verify item states
    $verifiedBooking = (Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/bookings" -Method Get -Headers @{ Authorization = "Bearer $($loginAdmin.token)" }) | Where-Object { $_.bookingId -eq $bookingId } | Select-Object -First 1
    $vItem1 = $verifiedBooking.items | Where-Object { $_.itemId -eq $item1.itemId } | Select-Object -First 1
    $vItem2 = $verifiedBooking.items | Where-Object { $_.itemId -eq $item2.itemId } | Select-Object -First 1

    $test6Ok = ($item1Res.success -eq $true) -and ($vItem1.status -eq "ACCEPTED") -and ($vItem2.status -eq "PENDING") -and $item2TamperBlocked
    Record-Test "6" "Stakeholder booking decision updates only their assigned item" $test6Ok "Item 1 set to ACCEPTED; Item 2 untouched (PENDING); cross-stakeholder tamper blocked (403)"

    
    $completeBookingBody = @{ bookingId = $bookingId; status = "COMPLETED" } | ConvertTo-Json
    Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/bookings/status" -Method Post -Body $completeBookingBody -Headers @{ Authorization = "Bearer $($loginAdmin.token)" } -ContentType "application/json" | Out-Null

   
    $fakePkgReview = @{
        bookingId  = $bookingId
        targetType = "PACKAGE"
        targetId   = 1
        rating     = 5
        comment    = "Attributing custom booking to Package 1 falsely"
    } | ConvertTo-Json

    $pkgReviewBlocked = $false
    $pkgRevRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/reviews" -Method Post -Body $fakePkgReview -Headers @{ Authorization = "Bearer $($loginTourist.token)" } -ContentType "application/json"
    if ($pkgRevRes.success -eq $false -and $pkgRevRes.code -eq 400) {
        $pkgReviewBlocked = $true
    }

    # 2. Custom booking correctly reviewing STAKEHOLDER (Stakeholder 1) must succeed
    $realStkReview = @{
        bookingId  = $bookingId
        targetType = "STAKEHOLDER"
        targetId   = 1
        rating     = 5
        comment    = "Pedro provided an exceptional guided tour of Sambawan and Biliran waterfalls!"
    } | ConvertTo-Json

    $stkRevRes = Invoke-RestMethod -Uri "http://127.0.0.1:$testPort/api/reviews" -Method Post -Body $realStkReview -Headers @{ Authorization = "Bearer $($loginTourist.token)" } -ContentType "application/json"
    $test7Ok = $pkgReviewBlocked -and ($stkRevRes.success -eq $true)
    Record-Test "7" "Custom itinerary reviews cannot target dummy Package 1; properly attributed to Stakeholder" $test7Ok "Dummy package review rejected; Stakeholder review recorded: $($stkRevRes.message)"

}
finally {
    Write-Host "[Harness] Terminating test backend process..." -ForegroundColor Gray
    if (-not $serverProc.HasExited) {
        $serverProc.Kill()
    }
}

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  PHASE 1 TEST RESULTS: $passed PASSED, $failed FAILED" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host "======================================================================" -ForegroundColor Cyan

if ($failed -gt 0) { exit 1 }