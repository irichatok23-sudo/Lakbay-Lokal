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

async function runTests() {
    console.log('===============================================================');
    console.log(' LAKBAY LOKAL — STAGE 1 VERIFICATION & SECURITY TEST SUITE');
    console.log('===============================================================');

    const results = [];
    function record(name, passed, details) {
        results.push({ name, passed, details });
        console.log(`[${passed ? 'PASS' : 'FAIL'}] ${name}: ${details}`);
    }

    // 1. Authenticate test users
    console.log('\n--- 1. Authenticating System Roles ---');
    const adminToken = await login('admin', 'password123');
    const toToken = await login('tourismoffice', 'password123');
    const lguNavalToken = await login('lgu_naval', 'password123');
    const brgyOlogToken = await login('brgy_olog', 'password123');
    const touristToken = await login('tourist_ana', 'password123');

    record('Login Admin', !!adminToken, adminToken ? 'Token acquired' : 'Failed');
    record('Login Tourism Office', !!toToken, toToken ? 'Token acquired' : 'Failed');
    record('Login LGU Naval', !!lguNavalToken, lguNavalToken ? 'Token acquired' : 'Failed');
    record('Login Barangay Official (Ol-og)', !!brgyOlogToken, brgyOlogToken ? 'Token acquired' : 'Failed');
    record('Login Tourist', !!touristToken, touristToken ? 'Token acquired' : 'Failed');

    // 2. Stakeholder Registration & Automatic PENDING
    console.log('\n--- 2. Stakeholder Registration ---');
    const testUsername = 'test_guide_' + Date.now().toString().slice(-4);
    const regPayload = {
        username: testUsername,
        password: 'password123',
        fullName: 'Test Registered Guide',
        email: `${testUsername}@example.com`,
        phone: '09171234567',
        businessName: 'Biliran Forest Trekking',
        category: 'TOUR_GUIDE',
        accreditationNo: 'DOT-R08-TEST-001',
        yearsExperience: 4,
        barangayId: 1, // Ol-og
        municipalityId: 8 // Maripipi
    };

    const regRes = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/auth/register-stakeholder',
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    }, regPayload);

    record('Register Stakeholder API', regRes.status === 200 && regRes.data.success, `Status: ${regRes.status}, Message: ${regRes.data.message}`);

    const newStkToken = await login(testUsername, 'password123');
    record('Login Newly Registered Stakeholder', !!newStkToken, newStkToken ? 'Token acquired' : 'Failed');

    // 3. Stakeholder Verification Status Retrieval
    console.log('\n--- 3. Stakeholder Verification Retrieval ---');
    const myVerifRes = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/stakeholders/my-verification',
        method: 'GET',
        headers: { 'X-Auth-Token': newStkToken }
    });

    const isPending = myVerifRes.data && myVerifRes.data.verificationStatus === 'PENDING';
    record('Stakeholder Initial Status is PENDING', isPending, `Status: ${myVerifRes.data?.verificationStatus}`);
    const stkProfileId = myVerifRes.data?.profileId;

    // 4. Verification in Barangay Official Dashboard
    console.log('\n--- 4. Barangay Official Verification Queue ---');
    const brgyVerifListRes = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/stakeholders?jurisdictionOnly=true',
        method: 'GET',
        headers: { 'X-Auth-Token': brgyOlogToken }
    });

    const appearsInBrgy = Array.isArray(brgyVerifListRes.data) && brgyVerifListRes.data.some(s => s.profileId === stkProfileId);
    record('Application appears in Barangay Official queue', appearsInBrgy, `Found in Ol-og: ${appearsInBrgy}`);

    // 5. Request Correction with mandatory remarks
    console.log('\n--- 5. Barangay Official Evaluation: Request Correction ---');
    // Try without remarks (should be rejected with 400)
    const emptyRemarksRes = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/verifications',
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': brgyOlogToken }
    }, {
        stakeholderId: stkProfileId,
        barangayId: 1,
        status: 'NEEDS_CORRECTION',
        remarks: '   '
    });
    record('Mandatory Remarks Enforcement (Empty Rejected)', emptyRemarksRes.status === 400, `Status: ${emptyRemarksRes.status}, Error: ${emptyRemarksRes.data.message}`);

    // Now submit with valid remarks
    const requestCorrectionRes = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/verifications',
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': brgyOlogToken }
    }, {
        stakeholderId: stkProfileId,
        barangayId: 1,
        status: 'NEEDS_CORRECTION',
        requirementsSubmitted: 'Barangay clearance only',
        remarks: 'Please attach DOT Tour Guide Accreditation certificate and renew clearance.'
    });
    record('Record Correction Request', requestCorrectionRes.status === 200 && requestCorrectionRes.data.success, `Message: ${requestCorrectionRes.data.message}`);

    // 6. Stakeholder sees remarks
    console.log('\n--- 6. Stakeholder Receives Correction Notice ---');
    const myVerifAfterCorrection = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/stakeholders/my-verification',
        method: 'GET',
        headers: { 'X-Auth-Token': newStkToken }
    });

    const hasRemarks = myVerifAfterCorrection.data?.verificationStatus === 'NEEDS_CORRECTION' &&
                       myVerifAfterCorrection.data?.latestRemarks?.includes('DOT Tour Guide Accreditation');
    record('Stakeholder Views Official Remarks', hasRemarks, `Remarks: "${myVerifAfterCorrection.data?.latestRemarks}"`);

    // 7. Stakeholder Resubmission
    console.log('\n--- 7. Stakeholder Resubmission ---');
    const resubmitRes = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/stakeholders/resubmit',
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': newStkToken }
    }, {
        businessName: 'Biliran Forest Trekking (Updated)',
        category: 'TOUR_GUIDE',
        contactNumber: '09179998888',
        yearsExperience: 5,
        accreditationNo: 'DOT-R08-2026-TG-099',
        remarks: 'Uploaded DOT accreditation certificate and updated emergency contact.'
    });

    record('Stakeholder Resubmit Profile', resubmitRes.status === 200 && resubmitRes.data.success, `Message: ${resubmitRes.data.message}`);

    const myVerifAfterResubmit = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/stakeholders/my-verification',
        method: 'GET',
        headers: { 'X-Auth-Token': newStkToken }
    });

    const resetToPending = myVerifAfterResubmit.data?.verificationStatus === 'PENDING';
    const historyPreserved = Array.isArray(myVerifAfterResubmit.data?.history) && myVerifAfterResubmit.data?.history.length >= 1;
    record('Profile Status Reset to PENDING on Resubmit', resetToPending, `Status: ${myVerifAfterResubmit.data?.verificationStatus}`);
    record('Previous Evaluation History Preserved', historyPreserved, `History items: ${myVerifAfterResubmit.data?.history?.length}`);

    // 8. Official Approval (VERIFIED)
    console.log('\n--- 8. Barangay Official Approval ---');
    const approveRes = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/verifications',
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': brgyOlogToken }
    }, {
        stakeholderId: stkProfileId,
        barangayId: 1,
        status: 'APPROVED',
        requirementsSubmitted: 'Barangay clearance, DOT Tour Guide Accreditation DOT-R08-2026-TG-099, Valid ID',
        remarks: 'All credentials verified and compliant. Approved for community ecotourism operations.'
    });

    record('Barangay Official Approval (VERIFIED)', approveRes.status === 200 && approveRes.data.success, `Message: ${approveRes.data.message}`);

    const myVerifFinal = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/stakeholders/my-verification',
        method: 'GET',
        headers: { 'X-Auth-Token': newStkToken }
    });

    record('Stakeholder Confirmed VERIFIED', myVerifFinal.data?.verificationStatus === 'VERIFIED', `Final Status: ${myVerifFinal.data?.verificationStatus}`);

    // 9. Government Account Management (GET /api/admin/users)
    console.log('\n--- 9. Government Account Management (GET /api/admin/users) ---');
    const adminGetUsers = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/admin/users',
        method: 'GET',
        headers: { 'X-Auth-Token': adminToken }
    });
    record('System Admin views all users', adminGetUsers.status === 200 && Array.isArray(adminGetUsers.data) && adminGetUsers.data.length >= 10, `Total accounts: ${adminGetUsers.data?.length}`);

    const toGetUsers = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/admin/users',
        method: 'GET',
        headers: { 'X-Auth-Token': toToken }
    });
    const toOnlySeesLgu = Array.isArray(toGetUsers.data) && toGetUsers.data.every(u => u.roleCode === 'LGU_OFFICER');
    record('Tourism Office sees only LGU Officer accounts', toOnlySeesLgu, `Count: ${toGetUsers.data?.length}`);

    const lguGetUsers = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/admin/users',
        method: 'GET',
        headers: { 'X-Auth-Token': lguNavalToken }
    });
    const lguOnlySeesNavalBrgy = Array.isArray(lguGetUsers.data) && lguGetUsers.data.every(u => u.roleCode === 'BARANGAY_OFFICIAL' && u.municipalityId === 1);
    record('LGU Officer sees only own Municipality Barangay Officials', lguOnlySeesNavalBrgy, `Count: ${lguGetUsers.data?.length}`);

    // 10. Government Account Creation Hierarchy
    console.log('\n--- 10. Government Account Creation Hierarchy ---');
    // Tourism Office creating LGU Officer without municipality -> should fail (400)
    const toCreateLguNoMuni = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/admin/users',
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': toToken }
    }, {
        username: 'lgu_invalid_' + Date.now().toString().slice(-4),
        fullName: 'Invalid LGU Officer',
        email: 'invalid_lgu@example.com',
        roleId: 3 // LGU_OFFICER
    });
    record('Tourism Office creating LGU without municipality fails (400)', toCreateLguNoMuni.status === 400, `Message: ${toCreateLguNoMuni.data.message}`);

    // LGU Officer creating Barangay Official outside own municipality -> should fail (403)
    const lguCreateCrossMuniBrgy = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/admin/users',
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': lguNavalToken }
    }, {
        username: 'brgy_cross_' + Date.now().toString().slice(-4),
        fullName: 'Cross Municipality Official',
        email: 'cross_brgy@example.com',
        roleId: 4, // BARANGAY_OFFICIAL
        barangayId: 4 // Burabod in Biliran municipality, not Naval!
    });
    record('LGU Officer creating cross-municipality Barangay Official rejected (403)', lguCreateCrossMuniBrgy.status === 403, `Message: ${lguCreateCrossMuniBrgy.data.message}`);

    // 11. Security Tests
    console.log('\n--- 11. Security Tests ---');
    // Tourist trying to access verification
    const touristVerifAttempt = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/verifications',
        method: 'GET',
        headers: { 'X-Auth-Token': touristToken }
    });
    record('Tourist accessing internal verification endpoint blocked (403)', touristVerifAttempt.status === 403, `Status: ${touristVerifAttempt.status}`);

    // Stakeholder trying to approve themselves
    const selfApproveAttempt = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/verifications',
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': newStkToken }
    }, {
        stakeholderId: stkProfileId,
        barangayId: 1,
        status: 'APPROVED',
        remarks: 'Self approval attempt'
    });
    record('Stakeholder self-approval attempt blocked (403)', selfApproveAttempt.status === 403, `Status: ${selfApproveAttempt.status}`);

    // Barangay Official trying to verify cross-barangay stakeholder
    // Profile 4 belongs to Brgy. Burabod (ID 4)
    const crossBrgyAttempt = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/verifications',
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': brgyOlogToken }
    }, {
        stakeholderId: 4,
        barangayId: 4,
        status: 'APPROVED',
        remarks: 'Cross-barangay evaluation attempt'
    });
    record('Barangay Official cross-barangay evaluation blocked (403)', crossBrgyAttempt.status === 403, `Status: ${crossBrgyAttempt.status}, Message: ${crossBrgyAttempt.data?.message}`);

    // Unauthenticated request to /api/verifications
    const unauthAttempt = await request({
        hostname: '127.0.0.1',
        port: 8080,
        path: '/api/verifications',
        method: 'GET'
    });
    record('Unauthenticated request to protected endpoint blocked (401)', unauthAttempt.status === 401, `Status: ${unauthAttempt.status}`);

    console.log('\n===============================================================');
    const passedCount = results.filter(r => r.passed).length;
    console.log(` SUMMARY: ${passedCount}/${results.length} Tests Passed`);
    console.log('===============================================================');
}

runTests().catch(err => console.error('Test script crashed:', err));
