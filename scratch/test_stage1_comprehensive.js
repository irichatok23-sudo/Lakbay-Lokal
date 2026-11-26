const http = require('http');

function request(options, data = null) {
    return new Promise((resolve, reject) => {
        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                let json = null;
                try { json = JSON.parse(body); } catch (_) { json = body; }
                resolve({ status: res.statusCode, headers: res.headers, data: json });
            });
        });
        req.on('error', reject);
        if (data) {
            req.write(typeof data === 'string' ? data : JSON.stringify(data));
        }
        req.end();
    });
}

async function login(username, password) {
    const res = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/auth/login',
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    }, { username, password });
    return res.data && res.data.token ? res.data.token : null;
}

async function runAllTests() {
    console.log('================================================================');
    console.log('  LAKBAY LOKAL — STAGE 1 COMPREHENSIVE AUTOMATED TEST SUITE');
    console.log('================================================================\n');

    const testResults = [];
    function record(suite, testName, passed, details) {
        testResults.push({ suite, testName, passed, details });
        const tag = passed ? '\x1b[32m[PASS]\x1b[0m' : '\x1b[31m[FAIL]\x1b[0m';
        console.log(`${tag} [${suite}] ${testName} -> ${details}`);
    }

    // Phase 0: Authenticate required test actors
    console.log('>>> 0. Authenticating Role Test Actors');
    const adminToken = await login('admin', 'password123');
    const toToken = await login('tourismoffice', 'password123');
    const lguNavalToken = await login('lgu_naval', 'password123');
    const brgyOlogToken = await login('brgy_olog', 'password123');
    const guidePedroToken = await login('guide_pedro', 'password123');
    const touristToken = await login('tourist_ana', 'password123');

    record('AUTH', 'Login System Admin', !!adminToken, adminToken ? 'Token acquired' : 'Login failed');
    record('AUTH', 'Login Tourism Office', !!toToken, toToken ? 'Token acquired' : 'Login failed');
    record('AUTH', 'Login LGU Naval Officer', !!lguNavalToken, lguNavalToken ? 'Token acquired' : 'Login failed');
    record('AUTH', 'Login Barangay Official (Ol-og)', !!brgyOlogToken, brgyOlogToken ? 'Token acquired' : 'Login failed');
    record('AUTH', 'Login Stakeholder (guide_pedro)', !!guidePedroToken, guidePedroToken ? 'Token acquired' : 'Login failed');
    record('AUTH', 'Login Tourist (tourist_ana)', !!touristToken, touristToken ? 'Token acquired' : 'Login failed');

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 2: Government Account Creation Validation (POST /api/admin/users)
    // Run creation first so newly created accounts exist for directory tests
    // ─────────────────────────────────────────────────────────────────────────
    console.log('\n>>> 1. Government Account Creation Validation Tests');

    const timestamp = Date.now().toString().slice(-5);

    // 1.1 Valid LGU account creation succeeds (Tourism Office -> LGU Officer for Almeria muni 2)
    const validLguUser = `lgu_test_${timestamp}`;
    const rCreateLguValid = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': toToken }
    }, {
        username: validLguUser,
        fullName: 'Test LGU Officer Almeria',
        email: `${validLguUser}@biliran.gov.ph`,
        roleId: 3, // LGU_OFFICER
        municipalityId: 2 // Almeria
    });
    record('CREATION', 'Valid LGU account creation succeeds',
        rCreateLguValid.status === 200 && rCreateLguValid.data.success,
        `Status ${rCreateLguValid.status}, message: ${rCreateLguValid.data?.message}`);

    // 1.2 Missing municipality is rejected (400)
    const rCreateLguNoMuni = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': toToken }
    }, {
        username: `lgu_nomuni_${timestamp}`,
        fullName: 'Missing Muni Officer',
        email: `nomuni_${timestamp}@biliran.gov.ph`,
        roleId: 3
    });
    record('CREATION', 'Missing municipality is rejected with HTTP 400',
        rCreateLguNoMuni.status === 400,
        `Status ${rCreateLguNoMuni.status}, message: ${rCreateLguNoMuni.data?.message}`);

    // 1.3 Invalid municipality is rejected (400)
    const rCreateLguInvalidMuni = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': toToken }
    }, {
        username: `lgu_invmuni_${timestamp}`,
        fullName: 'Invalid Muni Officer',
        email: `invmuni_${timestamp}@biliran.gov.ph`,
        roleId: 3,
        municipalityId: 9999
    });
    record('CREATION', 'Invalid municipality ID is rejected with HTTP 400',
        rCreateLguInvalidMuni.status === 400,
        `Status ${rCreateLguInvalidMuni.status}, message: ${rCreateLguInvalidMuni.data?.message}`);

    // 1.4 Valid Barangay Official creation succeeds (LGU Naval creates Brgy Official for Naval brgy 10)
    const validBrgyUser = `brgy_test_${timestamp}`;
    const rCreateBrgyValid = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': lguNavalToken }
    }, {
        username: validBrgyUser,
        fullName: 'Test Barangay Official P.I. Garcia',
        email: `${validBrgyUser}@biliran.gov.ph`,
        roleId: 4, // BARANGAY_OFFICIAL
        barangayId: 10 // P.I. Garcia in Naval (muni 1)
    });
    record('CREATION', 'Valid Barangay Official creation succeeds',
        rCreateBrgyValid.status === 200 && rCreateBrgyValid.data.success,
        `Status ${rCreateBrgyValid.status}, message: ${rCreateBrgyValid.data?.message}`);

    // 1.5 Missing barangay is rejected (400)
    const rCreateBrgyNoBrgy = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': lguNavalToken }
    }, {
        username: `brgy_nobrgy_${timestamp}`,
        fullName: 'No Barangay Official',
        email: `nobrgy_${timestamp}@biliran.gov.ph`,
        roleId: 4
    });
    record('CREATION', 'Missing barangay is rejected with HTTP 400',
        rCreateBrgyNoBrgy.status === 400,
        `Status ${rCreateBrgyNoBrgy.status}, message: ${rCreateBrgyNoBrgy.data?.message}`);

    // 1.6 Cross-municipality barangay assignment is rejected (403)
    // Brgy 1 (Ol-og) belongs to Maripipi (muni 8), but creator is LGU Naval (muni 1)
    const rCreateBrgyCrossMuni = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': lguNavalToken }
    }, {
        username: `brgy_cross_${timestamp}`,
        fullName: 'Cross Muni Official',
        email: `cross_${timestamp}@biliran.gov.ph`,
        roleId: 4,
        barangayId: 1 // Ol-og in Maripipi
    });
    record('CREATION', 'Cross-municipality barangay assignment rejected with HTTP 403',
        rCreateBrgyCrossMuni.status === 403,
        `Status ${rCreateBrgyCrossMuni.status}, message: ${rCreateBrgyCrossMuni.data?.message}`);

    // 1.7 Duplicate username is rejected (400)
    const rCreateDuplicate = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': adminToken }
    }, {
        username: 'admin', // already exists
        fullName: 'Duplicate Administrator',
        email: `duplicate_${timestamp}@biliran.gov.ph`,
        roleId: 1
    });
    record('CREATION', 'Duplicate username rejected with HTTP 400',
        rCreateDuplicate.status === 400,
        `Status ${rCreateDuplicate.status}, message: ${rCreateDuplicate.data?.message}`);

    // 1.8 Unauthorized role assignment is rejected (403)
    // Tourism Office attempting to create SYSTEM_ADMIN
    const rCreateUnauthRole = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': toToken }
    }, {
        username: `illegal_admin_${timestamp}`,
        fullName: 'Illegal Admin',
        email: `illegal_${timestamp}@biliran.gov.ph`,
        roleId: 1 // SYSTEM_ADMIN
    });
    record('CREATION', 'Unauthorized role assignment rejected with HTTP 403',
        rCreateUnauthRole.status === 403,
        `Status ${rCreateUnauthRole.status}, message: ${rCreateUnauthRole.data?.message}`);

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 2: Government Account Directory (GET /api/admin/users)
    // ─────────────────────────────────────────────────────────────────────────
    console.log('\n>>> 2. Government Account Directory Tests');

    // 2.1 System Admin can view all accounts
    const rAdminUsers = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'GET',
        headers: { 'X-Auth-Token': adminToken }
    });
    record('DIRECTORY', 'System Admin views all accounts',
        rAdminUsers.status === 200 && Array.isArray(rAdminUsers.data) && rAdminUsers.data.length >= 10,
        `Status ${rAdminUsers.status}, count: ${rAdminUsers.data?.length}`);

    // 2.2 Tourism Office can view LGU Officers only
    const rToUsers = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'GET',
        headers: { 'X-Auth-Token': toToken }
    });
    const toAllLgu = Array.isArray(rToUsers.data) && rToUsers.data.length > 0 &&
                     rToUsers.data.every(u => u.roleCode === 'LGU_OFFICER');
    record('DIRECTORY', 'Tourism Office views LGU Officers only',
        rToUsers.status === 200 && toAllLgu,
        `Status ${rToUsers.status}, count: ${rToUsers.data?.length}, all LGU: ${toAllLgu}`);

    // 2.3 LGU Officer can view Barangay Officials within assigned municipality only (Naval = 1)
    const rLguUsers = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'GET',
        headers: { 'X-Auth-Token': lguNavalToken }
    });
    const lguAllNavalBrgy = Array.isArray(rLguUsers.data) && rLguUsers.data.length > 0 &&
                            rLguUsers.data.every(u => u.roleCode === 'BARANGAY_OFFICIAL' && u.municipalityId === 1);
    record('DIRECTORY', 'LGU Officer views Barangay Officials in own municipality',
        rLguUsers.status === 200 && lguAllNavalBrgy,
        `Status ${rLguUsers.status}, count: ${rLguUsers.data?.length}, all Naval Brgy: ${lguAllNavalBrgy}`);

    // 2.4 LGU Officer cannot access another municipality's records via query spoofing
    const rLguSpoofMuni = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users?municipalityId=8', method: 'GET',
        headers: { 'X-Auth-Token': lguNavalToken }
    });
    const spoofIgnored = Array.isArray(rLguSpoofMuni.data) &&
                         rLguSpoofMuni.data.every(u => u.municipalityId === 1);
    record('DIRECTORY', 'LGU Officer cannot access another municipality (session enforced)',
        rLguSpoofMuni.status === 200 && spoofIgnored,
        `Spoof query ?municipalityId=8 ignored; records confined to muni 1: ${spoofIgnored}`);

    // 2.5 Tourist receives HTTP 403
    const rTouristUsers = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'GET',
        headers: { 'X-Auth-Token': touristToken }
    });
    record('DIRECTORY', 'Tourist receives HTTP 403 on directory access',
        rTouristUsers.status === 403, `Status: ${rTouristUsers.status}`);

    // 2.6 Stakeholder receives HTTP 403
    const rStkUsers = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'GET',
        headers: { 'X-Auth-Token': guidePedroToken }
    });
    record('DIRECTORY', 'Stakeholder receives HTTP 403 on directory access',
        rStkUsers.status === 403, `Status: ${rStkUsers.status}`);

    // 2.7 Barangay Official receives HTTP 403
    const rBrgyUsers = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'GET',
        headers: { 'X-Auth-Token': brgyOlogToken }
    });
    record('DIRECTORY', 'Barangay Official receives HTTP 403 on directory access',
        rBrgyUsers.status === 403, `Status: ${rBrgyUsers.status}`);

    // 2.8 Unauthenticated request receives HTTP 401
    const rUnauthUsers = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/users', method: 'GET'
    });
    record('DIRECTORY', 'Unauthenticated request receives HTTP 401',
        rUnauthUsers.status === 401, `Status: ${rUnauthUsers.status}`);

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 3: Stakeholder Resubmission Workflow & Data Integrity
    // ─────────────────────────────────────────────────────────────────────────
    console.log('\n>>> 3. Stakeholder Resubmission & Verification Tests');

    // Register a new test stakeholder in Maripipi (Ol-og, brgy 1, muni 8)
    const stkUsername = `guide_resub_${timestamp}`;
    const rStkReg = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/auth/register-stakeholder', method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    }, {
        username: stkUsername,
        password: 'password123',
        fullName: 'Resubmission Test Guide',
        email: `${stkUsername}@example.com`,
        phone: '09170001122',
        businessName: 'Maripipi Island Expeditions',
        category: 'TOUR_GUIDE',
        accreditationNo: 'DOT-R08-INITIAL',
        yearsExperience: 3,
        barangayId: 1, // Ol-og
        municipalityId: 8 // Maripipi
    });

    const stkToken = await login(stkUsername, 'password123');
    record('RESUBMISSION', 'Register fresh stakeholder for resubmission lifecycle',
        rStkReg.status === 200 && !!stkToken,
        `Status ${rStkReg.status}, Token: ${!!stkToken}`);

    // Check initial status is PENDING
    const rInitialVerif = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/stakeholders/my-verification', method: 'GET',
        headers: { 'X-Auth-Token': stkToken }
    });
    const profileId = rInitialVerif.data?.profileId;
    record('RESUBMISSION', 'Initial stakeholder status is PENDING',
        rInitialVerif.data?.verificationStatus === 'PENDING' && profileId > 0,
        `Status: ${rInitialVerif.data?.verificationStatus}, Profile ID: ${profileId}`);

    // Official review by authorized Barangay Official (Ol-og): Request Correction
    const rReviewCorrection = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/verifications', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': brgyOlogToken }
    }, {
        stakeholderId: profileId,
        barangayId: 1,
        status: 'NEEDS_CORRECTION',
        requirementsSubmitted: 'Barangay clearance only',
        remarks: 'Please provide DOT Regional Accreditation certificate and updated contact number.'
    });
    record('RESUBMISSION', 'Barangay Official requests correction (NEEDS_CORRECTION)',
        rReviewCorrection.status === 200 && rReviewCorrection.data.success,
        `Status ${rReviewCorrection.status}, message: ${rReviewCorrection.data?.message}`);

    // Verify stakeholder sees correction notice and official remarks
    const rVerifNeedsCorrection = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/stakeholders/my-verification', method: 'GET',
        headers: { 'X-Auth-Token': stkToken }
    });
    record('RESUBMISSION', 'Stakeholder sees NEEDS_CORRECTION and official remarks',
        rVerifNeedsCorrection.data?.verificationStatus === 'NEEDS_CORRECTION' &&
        rVerifNeedsCorrection.data?.latestRemarks?.includes('DOT Regional Accreditation'),
        `Status: ${rVerifNeedsCorrection.data?.verificationStatus}, Remarks: "${rVerifNeedsCorrection.data?.latestRemarks}"`);

    // 3.4 Stakeholder cannot resubmit another person's application (403)
    const rResubCrossStk = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/stakeholders/resubmit', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': stkToken }
    }, {
        profileId: 999999, // Another profile ID
        businessName: 'Unauthorized Takeover',
        contactNumber: '09171234567'
    });
    record('RESUBMISSION', 'Stakeholder cannot resubmit another person application (403)',
        rResubCrossStk.status === 403,
        `Status ${rResubCrossStk.status}, message: ${rResubCrossStk.data?.message}`);

    // 3.5 Missing required information is rejected (400)
    const rResubBlank = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/stakeholders/resubmit', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': stkToken }
    }, {
        businessName: '   ', // Blank business name
        contactNumber: '09171234567'
    });
    record('RESUBMISSION', 'Missing required information rejected with HTTP 400',
        rResubBlank.status === 400,
        `Status ${rResubBlank.status}, message: ${rResubBlank.data?.message}`);

    // 3.3 Stakeholder can resubmit their own application
    const rResubSuccess = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/stakeholders/resubmit', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': stkToken }
    }, {
        businessName: 'Maripipi Island Expeditions (Accredited)',
        category: 'TOUR_GUIDE',
        contactNumber: '09178887766',
        yearsExperience: 4,
        accreditationNo: 'DOT-R08-2026-EXP-777',
        remarks: 'Attached certified true copy of DOT Regional Accreditation and updated contact info.'
    });
    record('RESUBMISSION', 'Stakeholder can resubmit their own application',
        rResubSuccess.status === 200 && rResubSuccess.data.success,
        `Status ${rResubSuccess.status}, message: ${rResubSuccess.data?.message}`);

    // 3.6 Profile returns to PENDING
    const rVerifAfterResub = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/stakeholders/my-verification', method: 'GET',
        headers: { 'X-Auth-Token': stkToken }
    });
    record('RESUBMISSION', 'Profile returns to PENDING on resubmission',
        rVerifAfterResub.data?.verificationStatus === 'PENDING',
        `Current Status: ${rVerifAfterResub.data?.verificationStatus}`);

    // 3.7 Previous official verification history remains intact
    const historyCount = Array.isArray(rVerifAfterResub.data?.history) ? rVerifAfterResub.data.history.length : 0;
    const hasCorrectionDecision = rVerifAfterResub.data?.history?.some(h =>
        h.status === 'REQUEST_CORRECTION' || h.status === 'NEEDS_CORRECTION'
    );
    record('RESUBMISSION', 'Previous official verification history remains intact',
        historyCount >= 1 && hasCorrectionDecision,
        `History entries: ${historyCount}, Contains correction decision: ${hasCorrectionDecision}`);

    // 3.8 Audit log correctly identifies the stakeholder
    const rAuditLogs = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/admin/logs?limit=20', method: 'GET',
        headers: { 'X-Auth-Token': adminToken }
    });
    const stkAudit = Array.isArray(rAuditLogs.data) &&
                     rAuditLogs.data.find(l => l.action === 'STAKEHOLDER_RESUBMIT' && l.username === stkUsername);
    record('RESUBMISSION', 'Audit log correctly identifies stakeholder as actor',
        !!stkAudit,
        stkAudit ? `Action: ${stkAudit.action}, Actor: ${stkAudit.username}, Details: ${stkAudit.details}` : 'Not found');

    // 3.9 Barangay Official can review the resubmitted application (Approves to VERIFIED)
    const rFinalApprove = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/verifications', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': brgyOlogToken }
    }, {
        stakeholderId: profileId,
        barangayId: 1,
        status: 'APPROVED',
        requirementsSubmitted: 'Barangay clearance, DOT Regional Accreditation DOT-R08-2026-EXP-777',
        remarks: 'Resubmitted documents verified complete and authenticated. Approved.'
    });
    record('RESUBMISSION', 'Barangay Official can review resubmitted application (APPROVED)',
        rFinalApprove.status === 200 && rFinalApprove.data.success,
        `Status ${rFinalApprove.status}, message: ${rFinalApprove.data?.message}`);

    const rVerifFinal = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/stakeholders/my-verification', method: 'GET',
        headers: { 'X-Auth-Token': stkToken }
    });
    record('RESUBMISSION', 'Stakeholder profile reaches VERIFIED status after approval',
        rVerifFinal.data?.verificationStatus === 'VERIFIED',
        `Final status: ${rVerifFinal.data?.verificationStatus}`);

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 4: Service Publishing Restriction
    // ─────────────────────────────────────────────────────────────────────────
    console.log('\n>>> 4. Service Publishing & Availability Protection');

    // Register an unverified stakeholder
    const unverifiedUser = `unverif_${timestamp}`;
    await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/auth/register-stakeholder', method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    }, {
        username: unverifiedUser,
        password: 'password123',
        fullName: 'Unverified Operator',
        email: `${unverifiedUser}@example.com`,
        phone: '09171112233',
        businessName: 'Unverified Boat Rental',
        category: 'BOAT_OPERATOR',
        barangayId: 1,
        municipalityId: 8
    });
    const unverifiedToken = await login(unverifiedUser, 'password123');

    // Attempt to publish service while PENDING -> should be rejected with 403
    const rPublishAttempt = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/stakeholders/services', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': unverifiedToken }
    }, {
        serviceName: 'Illegal Boat Tour',
        category: 'BOAT_OPERATOR',
        basePrice: 1500,
        capacity: 10,
        available: true
    });
    record('SERVICES', 'Unverified stakeholder blocked from publishing service (403)',
        rPublishAttempt.status === 403,
        `Status ${rPublishAttempt.status}, message: ${rPublishAttempt.data?.message}`);

    // Tourist only sees available/verified services
    const rPublicServices = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/stakeholders/services', method: 'GET',
        headers: { 'X-Auth-Token': touristToken }
    });
    const touristOnlyVerified = Array.isArray(rPublicServices.data) &&
                                rPublicServices.data.every(s => s.available === true);
    record('SERVICES', 'Tourist query returns only active, available services',
        rPublicServices.status === 200 && touristOnlyVerified,
        `Count: ${rPublicServices.data?.length}, all available: ${touristOnlyVerified}`);

    // Summary
    console.log('\n================================================================');
    const passedTests = testResults.filter(t => t.passed).length;
    const totalTests = testResults.length;
    console.log(`  COMPREHENSIVE SUITE RESULT: ${passedTests}/${totalTests} TESTS PASSED`);
    console.log('================================================================\n');

    if (passedTests !== totalTests) {
        process.exit(1);
    }
}

runAllTests().catch(err => {
    console.error('Test execution error:', err);
    process.exit(1);
});
