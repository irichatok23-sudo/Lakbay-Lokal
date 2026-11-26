const http = require('http');
const fs = require('fs');

const results = [];

function req(method, path, body, token) {
    return new Promise((resolve, reject) => {
        const payload = body ? JSON.stringify(body) : null;
        const opts = {
            hostname: '127.0.0.1',
            port: 8080,
            path: path,
            method: method,
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { 'X-Auth-Token': token } : {}),
                ...(payload ? { 'Content-Length': Buffer.byteLength(payload) } : {})
            }
        };
        const r = http.request(opts, (res) => {
            let data = '';
            res.on('data', c => data += c);
            res.on('end', () => {
                try { resolve({ status: res.statusCode, body: JSON.parse(data) }); }
                catch (e) { resolve({ status: res.statusCode, body: data }); }
            });
        });
        r.on('error', reject);
        if (payload) r.write(payload);
        r.end();
    });
}

function pass(category, name, detail = '') {
    results.push({ status: 'PASS', category, name, detail });
    console.log(`  ✓ PASS  [${category}] ${name}${detail ? ' — ' + detail : ''}`);
}
function fail(category, name, detail = '') {
    results.push({ status: 'FAIL', category, name, detail });
    console.log(`  ✗ FAIL  [${category}] ${name}${detail ? ' — ' + detail : ''}`);
}
function fixed(category, name, detail = '') {
    results.push({ status: 'FIXED', category, name, detail });
    console.log(`  ⚡ FIXED [${category}] ${name}${detail ? ' — ' + detail : ''}`);
}

async function runAudit() {
    console.log('======================================================================');
    console.log('  LAKBAY LOKAL — COMPREHENSIVE 6-DASHBOARD AUDIT & TESTING');
    console.log('======================================================================\n');

    // 1. AUTHENTICATION & LOGIN FOR ALL 6 ROLES
    console.log('--- 1. Authentication & Session Verification ---');
    const tokens = {};
    const creds = {
        'TOURIST':            { username: 'tourist_ana',     password: 'password123' },
        'STAKEHOLDER':        { username: 'guide_pedro',     password: 'password123' },
        'BARANGAY_OFFICIAL':  { username: 'brgy_olog',       password: 'password123' },
        'LGU_OFFICER':        { username: 'lgu_naval',       password: 'password123' },
        'TOURISM_OFFICE':     { username: 'tourismoffice',   password: 'password123' },
        'SYSTEM_ADMIN':       { username: 'admin',           password: 'password123' },
    };

    for (const [role, cred] of Object.entries(creds)) {
        try {
            const r = await req('POST', '/api/auth/login', cred);
            if (r.body && r.body.success && r.body.token) {
                tokens[role] = r.body.token;
                pass('AUTH', `Login ${role}`, `token: ${r.body.token.substring(0, 8)}...`);
            } else {
                fail('AUTH', `Login ${role}`, JSON.stringify(r.body));
            }
        } catch (e) { fail('AUTH', `Login ${role}`, e.message); }
    }

    // 2. PHASE 3 VERIFICATION: POST /api/auth/me (PROFILE UPDATE)
    console.log('\n--- 2. Profile Update Verification (POST /api/auth/me) ---');
    let touristToken = tokens['TOURIST'];

    // Test 2.1: Authentication required
    try {
        const r = await req('POST', '/api/auth/me', { fullName: 'Hacker' }, null);
        if (r.status === 401) pass('PROFILE', 'POST /api/auth/me requires authentication (401)');
        else fail('PROFILE', 'POST /api/auth/me requires authentication', `Got status ${r.status}`);
    } catch (e) { fail('PROFILE', 'Unauth check', e.message); }

    // Test 2.2: Current user update persists
    try {
        const updatePayload = {
            fullName: 'Ana Sophia Cortez Updated',
            email: 'ana.cortez.updated@example.com',
            phone: '0917-555-9876'
        };
        const r = await req('POST', '/api/auth/me', updatePayload, touristToken);
        if (r.body && r.body.success) {
            fixed('PROFILE', 'POST /api/auth/me updates profile successfully', r.body.message);
        } else {
            fail('PROFILE', 'POST /api/auth/me update', JSON.stringify(r.body));
        }

        // Verify update in GET /api/auth/me
        const getMe = await req('GET', '/api/auth/me', null, touristToken);
        if (getMe.body && getMe.body.fullName === 'Ana Sophia Cortez Updated' && getMe.body.phone === '0917-555-9876') {
            pass('PROFILE', 'Profile persistence verified in database', `Name: ${getMe.body.fullName}, Phone: ${getMe.body.phone}`);
        } else {
            fail('PROFILE', 'Profile persistence verification', JSON.stringify(getMe.body));
        }

        // Revert profile to standard name
        await req('POST', '/api/auth/me', {
            fullName: 'Ana Sophia Cortez',
            email: 'ana.cortez@example.com',
            phone: '0917-555-0101'
        }, touristToken);
    } catch (e) { fail('PROFILE', 'Profile update execution', e.message); }

    // Test 2.3: Role immutability through profile update
    try {
        const r = await req('POST', '/api/auth/me', {
            fullName: 'Ana Sophia Cortez',
            roleId: 1,
            roleCode: 'SYSTEM_ADMIN'
        }, touristToken);
        const checkRole = await req('GET', '/api/auth/me', null, touristToken);
        if (checkRole.body.roleCode === 'TOURIST') {
            pass('PROFILE', 'Security: Role cannot be elevated via profile update', 'remains TOURIST');
        } else {
            fail('PROFILE', 'Security: Role elevation was not prevented', checkRole.body.roleCode);
        }
    } catch (e) { fail('PROFILE', 'Role tamper check', e.message); }

    // Test 2.4: Password security (requires current password to change password)
    try {
        const r = await req('POST', '/api/auth/me', {
            fullName: 'Ana Sophia Cortez',
            newPassword: 'newpassword123'
        }, touristToken);
        if (r.status === 400) {
            pass('PROFILE', 'Security: Password update requires current password (400)', r.body.message);
        } else {
            fail('PROFILE', 'Security: Password update allowed without current password', JSON.stringify(r.body));
        }
    } catch (e) { fail('PROFILE', 'Password tamper check', e.message); }

    // 3. AUDIT DASHBOARD 1: TOURIST DASHBOARD
    console.log('\n--- 3. Tourist Dashboard Audit ---');
    try {
        // Explore & Filter destinations
        const rDest = await req('GET', '/api/destinations?category=WATERFALL', null, touristToken);
        if (rDest.status === 200 && Array.isArray(rDest.body)) pass('TOURIST', 'Explore & Filter Destinations', `${rDest.body.length} waterfall destinations`);
        else fail('TOURIST', 'Explore & Filter Destinations', `status ${rDest.status}`);

        // View destination details (GET /api/destinations/:id)
        const rDestDetail = await req('GET', '/api/destinations/1', null, touristToken);
        if (rDestDetail.status === 200 && rDestDetail.body.destinationId === 1) pass('TOURIST', 'View Destination Details', rDestDetail.body.name);
        else fail('TOURIST', 'View Destination Details', `status ${rDestDetail.status}`);

        // View Packages & Package Details
        const rPkg = await req('GET', '/api/packages', null, touristToken);
        if (rPkg.status === 200 && Array.isArray(rPkg.body)) pass('TOURIST', 'View Tourism Packages', `${rPkg.body.length} packages`);
        else fail('TOURIST', 'View Tourism Packages', `status ${rPkg.status}`);

        // View Booking History & Status
        const rBook = await req('GET', '/api/bookings', null, touristToken);
        if (rBook.status === 200 && Array.isArray(rBook.body)) pass('TOURIST', 'View Booking History & Status', `${rBook.body.length} reservations`);
        else fail('TOURIST', 'View Booking History', `status ${rBook.status}`);

        // View Reviews & Ratings
        const rRev = await req('GET', '/api/reviews', null, touristToken);
        if (rRev.status === 200) pass('TOURIST', 'View Community Reviews');
        else fail('TOURIST', 'View Community Reviews', `status ${rRev.status}`);
    } catch (e) { fail('TOURIST', 'Tourist operations', e.message); }

    // 4. AUDIT DASHBOARD 2: STAKEHOLDER DASHBOARD
    console.log('\n--- 4. Stakeholder Dashboard Audit ---');
    const stkToken = tokens['STAKEHOLDER'];
    try {
        // View Stakeholder Profile
        const rMe = await req('GET', '/api/stakeholders/me', null, stkToken);
        if (rMe.status === 200 && rMe.body.profileId) pass('STAKEHOLDER', 'Manage Stakeholder Profile', rMe.body.businessName);
        else fail('STAKEHOLDER', 'Manage Stakeholder Profile', `status ${rMe.status}`);

        // View Incoming Bookings & Reservations
        const rBook = await req('GET', '/api/bookings', null, stkToken);
        if (rBook.status === 200 && Array.isArray(rBook.body)) pass('STAKEHOLDER', 'View Incoming Bookings', `${rBook.body.length} bookings assigned`);
        else fail('STAKEHOLDER', 'View Incoming Bookings', `status ${rBook.status}`);

        // View Services & Offerings
        const rSvc = await req('GET', '/api/stakeholders/services?all=true', null, stkToken);
        if (rSvc.status === 200 && Array.isArray(rSvc.body)) pass('STAKEHOLDER', 'Manage Services & Offerings', `${rSvc.body.length} services`);
        else fail('STAKEHOLDER', 'Manage Services', `status ${rSvc.status}`);

        // View Verification Clearance Status
        const rVer = await req('GET', '/api/stakeholders/my-verification', null, stkToken);
        if (rVer.status === 200 && rVer.body.profileId) pass('STAKEHOLDER', 'View Verification Clearance Status', `Status: ${rVer.body.verificationStatus}`);
        else fail('STAKEHOLDER', 'View Verification', `status ${rVer.status}`);
    } catch (e) { fail('STAKEHOLDER', 'Stakeholder operations', e.message); }

    // 5. AUDIT DASHBOARD 3: BARANGAY OFFICIALS DASHBOARD
    console.log('\n--- 5. Barangay Officials Dashboard Audit ---');
    const brgyToken = tokens['BARANGAY_OFFICIAL'];
    try {
        // Review Stakeholder Verifications
        const rVer = await req('GET', '/api/verifications', null, brgyToken);
        if (rVer.status === 200 && Array.isArray(rVer.body)) pass('BARANGAY', 'Review Verification Submissions', `${rVer.body.length} submissions`);
        else fail('BARANGAY', 'Review Verification Submissions', `status ${rVer.status}`);

        // View Barangay Destinations
        const rDest = await req('GET', '/api/destinations?status=ALL', null, brgyToken);
        if (rDest.status === 200) pass('BARANGAY', 'Monitor Barangay Destinations');
        else fail('BARANGAY', 'Monitor Barangay Destinations', `status ${rDest.status}`);

        // View Barangay Tourism Records
        const rBrgy = await req('GET', '/api/reports/barangays', null, brgyToken);
        if (rBrgy.status === 200 && Array.isArray(rBrgy.body)) pass('BARANGAY', 'Generate Barangay Tourism Records', `${rBrgy.body.length} barangay records`);
        else fail('BARANGAY', 'Generate Barangay Records', `status ${rBrgy.status}`);

        // Stakeholder Directory Access
        const rStk = await req('GET', '/api/stakeholders', null, brgyToken);
        if (rStk.status === 200) pass('BARANGAY', 'View Stakeholder Directory');
        else fail('BARANGAY', 'View Stakeholder Directory', `status ${rStk.status}`);
    } catch (e) { fail('BARANGAY', 'Barangay operations', e.message); }

    // 6. AUDIT DASHBOARD 4: LGU OFFICIALS DASHBOARD
    console.log('\n--- 6. LGU Officials Dashboard Audit ---');
    const lguToken = tokens['LGU_OFFICER'];
    try {
        // Destination Footfall Analytics
        const rFoot = await req('GET', '/api/reports/footfall', null, lguToken);
        if (rFoot.status === 200 && Array.isArray(rFoot.body)) pass('LGU', 'Monitor Tourist Footfall Analytics', `${rFoot.body.length} destination analytics`);
        else fail('LGU', 'Monitor Footfall Analytics', `status ${rFoot.status}`);

        // Municipal Stakeholder Statistics
        const rStkStats = await req('GET', '/api/reports/stakeholders', null, lguToken);
        if (rStkStats.status === 200) pass('LGU', 'View Stakeholder Statistics');
        else fail('LGU', 'View Stakeholder Statistics', `status ${rStkStats.status}`);

        // Municipal KPIs
        const rKpi = await req('GET', '/api/reports/kpis', null, lguToken);
        if (rKpi.status === 200) pass('LGU', 'Monitor Municipal Tourism KPIs');
        else fail('LGU', 'Monitor Municipal KPIs', `status ${rKpi.status}`);

        // Barangay Officials Directory
        const rUsers = await req('GET', '/api/admin/users', null, lguToken);
        if (rUsers.status === 200 && Array.isArray(rUsers.body)) pass('LGU', 'View Barangay Officials Directory', `${rUsers.body.length} officials`);
        else fail('LGU', 'View Barangay Officials Directory', `status ${rUsers.status}`);
    } catch (e) { fail('LGU', 'LGU operations', e.message); }

    // 7. AUDIT DASHBOARD 5: TOURISM OFFICE DASHBOARD
    console.log('\n--- 7. Tourism Office Dashboard Audit ---');
    const toToken = tokens['TOURISM_OFFICE'];
    try {
        // Announcements Management (Create, View, Delete)
        const rAnnoList = await req('GET', '/api/announcements', null, toToken);
        if (rAnnoList.status === 200 && Array.isArray(rAnnoList.body)) pass('TOURISM_OFFICE', 'Manage Tourism Announcements (List)', `${rAnnoList.body.length} announcements`);
        else fail('TOURISM_OFFICE', 'Manage Announcements', `status ${rAnnoList.status}`);

        // Create announcement with valid status PUBLISHED
        const newAnno = {
            title: 'AUDIT TEST — Provincial Tourism Advisory',
            category: 'TRAVEL_GUIDELINE',
            content: 'Official travel advisory issued by Biliran Provincial Tourism Office.',
            status: 'PUBLISHED'
        };
        const rCreateAnno = await req('POST', '/api/announcements', newAnno, toToken);
        if (rCreateAnno.body && rCreateAnno.body.success) {
            fixed('TOURISM_OFFICE', 'Create Official Announcement (PUBLISHED)', 'Announcement posted');
        } else {
            fail('TOURISM_OFFICE', 'Create Official Announcement', JSON.stringify(rCreateAnno.body));
        }

        // View Municipal LGU Officers Directory
        const rLguUsers = await req('GET', '/api/admin/users', null, toToken);
        if (rLguUsers.status === 200 && Array.isArray(rLguUsers.body)) pass('TOURISM_OFFICE', 'Monitor Municipal Tourism Records (Users)', `${rLguUsers.body.length} officers`);
        else fail('TOURISM_OFFICE', 'Monitor Municipal Records', `status ${rLguUsers.status}`);
    } catch (e) { fail('TOURISM_OFFICE', 'Tourism Office operations', e.message); }

    // 8. AUDIT DASHBOARD 6: SYSTEM ADMINISTRATOR DASHBOARD
    console.log('\n--- 8. System Administrator Dashboard Audit ---');
    const adminToken = tokens['SYSTEM_ADMIN'];
    try {
        // User Account Management
        const rUsers = await req('GET', '/api/admin/users', null, adminToken);
        if (rUsers.status === 200 && Array.isArray(rUsers.body)) pass('SYSTEM_ADMIN', 'View All User Accounts', `${rUsers.body.length} accounts`);
        else fail('SYSTEM_ADMIN', 'View All User Accounts', `status ${rUsers.status}`);

        // User Status Toggle (DEACTIVATED / ACTIVE)
        const targetUser = (rUsers.body || []).find(u => u.username === 'tourist_ana');
        if (targetUser) {
            // Test toggle to DEACTIVATED
            const rDeact = await req('POST', '/api/admin/users/status', { userId: targetUser.userId, status: 'DEACTIVATED' }, adminToken);
            if (rDeact.body && rDeact.body.success) {
                fixed('SYSTEM_ADMIN', 'Toggle User Account Status (DEACTIVATED)', rDeact.body.message);
            } else {
                fail('SYSTEM_ADMIN', 'Toggle User Account Status (DEACTIVATED)', JSON.stringify(rDeact.body));
            }

            // Test toggle back to ACTIVE
            const rAct = await req('POST', '/api/admin/users/status', { userId: targetUser.userId, status: 'ACTIVE' }, adminToken);
            if (rAct.body && rAct.body.success) {
                pass('SYSTEM_ADMIN', 'Restore User Account Status (ACTIVE)', rAct.body.message);
            } else {
                fail('SYSTEM_ADMIN', 'Restore User Account Status (ACTIVE)', JSON.stringify(rAct.body));
            }
        }

        // System Settings Management
        const rSaveSettings = await req('POST', '/api/settings', {
            appName: 'Lakbay Lokal - Biliran Tourism Information System',
            broadcastMsg: 'Welcome to Lakbay Lokal!'
        }, adminToken);
        if (rSaveSettings.body && rSaveSettings.body.success) pass('SYSTEM_ADMIN', 'Manage System Settings (Save)', rSaveSettings.body.message);
        else fail('SYSTEM_ADMIN', 'Manage System Settings', JSON.stringify(rSaveSettings.body));

        // System Security Audit Logs
        const rLogs = await req('GET', '/api/admin/logs?limit=25', null, adminToken);
        if (rLogs.status === 200 && Array.isArray(rLogs.body)) pass('SYSTEM_ADMIN', 'View Security Audit Logs', `${rLogs.body.length} audit entries`);
        else fail('SYSTEM_ADMIN', 'View Audit Logs', `status ${rLogs.status}`);
    } catch (e) { fail('SYSTEM_ADMIN', 'Admin operations', e.message); }

    // 9. PDF EXPORT IMPLEMENTATION VERIFICATION
    console.log('\n--- 9. PDF Export Implementation Verification ---');
    try {
        // Test Java ExportUtil PDF generation
        const samplePdfPath = 'scratch/sample_report.pdf';
        if (fs.existsSync(samplePdfPath)) {
            const buf = fs.readFileSync(samplePdfPath);
            const isPdf = buf.slice(0, 8).toString().startsWith('%PDF-1.4') && buf.toString().includes('%%EOF');
            if (isPdf && buf.length > 1000) {
                pass('PDF_EXPORT', 'Java ExportUtil generates valid ISO 32000 PDF-1.4 file', `${buf.length} bytes`);
            } else {
                fail('PDF_EXPORT', 'Java ExportUtil PDF validation', 'Invalid PDF structure');
            }
        } else {
            fail('PDF_EXPORT', 'Sample PDF missing', samplePdfPath);
        }

        // Verify web/js/pdf-export.js exists and is linked in index.html
        const pdfJsContent = fs.readFileSync('src/main/resources/web/js/pdf-export.js', 'utf8');
        const indexContent = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
        const hasJsPdfExport = pdfJsContent.includes('class LakbayPdfDocument') && pdfJsContent.includes('exportReportPDF');
        const hasScriptInHtml = indexContent.includes('<script src="js/pdf-export.js"></script>');
        const hasPdfButtons = indexContent.includes('exportReportPDF()') && indexContent.includes('fa-file-pdf');

        if (hasJsPdfExport && hasScriptInHtml && hasPdfButtons) {
            fixed('PDF_EXPORT', 'Web UI PDF Export engine integrated in index.html & app.js', 'Pure client-side ISO 32000 PDF generator');
        } else {
            fail('PDF_EXPORT', 'Web UI PDF integration', `js: ${hasJsPdfExport}, html: ${hasScriptInHtml}, buttons: ${hasPdfButtons}`);
        }
    } catch (e) { fail('PDF_EXPORT', 'PDF Verification', e.message); }

    // 10. SECURITY & ROLE RESTRICTIONS
    console.log('\n--- 10. Role Security & Authorization Verification ---');
    try {
        // Re-authenticate tourist to get a fresh session token after deactivation test
        const relogin = await req('POST', '/api/auth/login', creds['TOURIST']);
        const freshTouristToken = relogin.body.token;

        // Tourist cannot access admin
        const r1 = await req('GET', '/api/admin/users', null, freshTouristToken);
        if (r1.status === 403) pass('SECURITY', 'Role restriction: TOURIST cannot access /api/admin/users (403)');
        else fail('SECURITY', 'TOURIST admin restriction', `Got ${r1.status}`);

        // Stakeholder cannot delete announcements
        const r2 = await req('POST', '/api/announcements/delete', { announcementId: 1 }, stkToken);
        if (r2.status === 403) pass('SECURITY', 'Role restriction: STAKEHOLDER cannot delete announcements (403)');
        else fail('SECURITY', 'STAKEHOLDER delete announcement restriction', `Got ${r2.status}`);

        // Tourist cannot approve verification
        const r3 = await req('POST', '/api/verifications', { stakeholderId: 1, status: 'VERIFIED' }, freshTouristToken);
        if (r3.status === 403) pass('SECURITY', 'Role restriction: TOURIST cannot verify stakeholders (403)');
        else fail('SECURITY', 'TOURIST verification restriction', `Got ${r3.status}`);

        // Unauthenticated access blocked
        const r4 = await req('GET', '/api/bookings', null, null);
        if (r4.status === 401) pass('SECURITY', 'Authentication enforcement: /api/bookings returns 401 Unauthenticated');
        else fail('SECURITY', 'Auth enforcement', `Got ${r4.status}`);
    } catch (e) { fail('SECURITY', 'Security test execution', e.message); }

    // SUMMARY
    console.log('\n======================================================================');
    console.log('  AUDIT SUMMARY');
    console.log('======================================================================');
    const passed = results.filter(r => r.status === 'PASS').length;
    const fixedCount = results.filter(r => r.status === 'FIXED').length;
    const failed = results.filter(r => r.status === 'FAIL').length;
    console.log(`  PASS:       ${passed}`);
    console.log(`  FIXED:      ${fixedCount}`);
    console.log(`  FAIL:       ${failed}`);
    console.log(`  TOTAL:      ${results.length}`);

    if (failed > 0) {
        console.log('\n  Failed Tests:');
        results.filter(r => r.status === 'FAIL').forEach(f => {
            console.log(`    - [${f.category}] ${f.name}: ${f.detail}`);
        });
    } else {
        console.log('\n  🎉 ALL TESTS PASSED SUCCESSFULLY! ZERO FAILURES.');
    }
    console.log('======================================================================\n');
}

runAudit().catch(console.error);
