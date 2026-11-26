/**
 * Comprehensive Dashboard Button & API Test Suite
 * Tests all 6 dashboards and their API endpoints
 */
const http = require('http');

const BASE = 'http://127.0.0.1:8080';
const results = [];

function req(method, path, body, token) {
    return new Promise((resolve, reject) => {
        const opts = {
            hostname: '127.0.0.1',
            port: 8080,
            path: path,
            method: method,
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { 'X-Auth-Token': token } : {})
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
        if (body) r.write(JSON.stringify(body));
        r.end();
    });
}

function pass(name, detail = '') {
    results.push({ status: 'PASS', name, detail });
    console.log(`  ✓ PASS  ${name}${detail ? ' — ' + detail : ''}`);
}
function fail(name, detail = '') {
    results.push({ status: 'FAIL', name, detail });
    console.log(`  ✗ FAIL  ${name}${detail ? ' — ' + detail : ''}`);
}

async function runTests() {
    console.log('\n=== LAKBAY LOKAL — COMPREHENSIVE BUTTON & API TEST ===\n');

    // -------------------------------------------------------------------------
    // LOGIN: Get tokens for each role
    // -------------------------------------------------------------------------
    console.log('--- Authentication & Login ---');

    const tokens = {};
    const creds = {
        'SYSTEM_ADMIN':       { username: 'admin',           password: 'password123' },
        'TOURISM_OFFICE':     { username: 'tourismoffice',   password: 'password123' },
        'LGU_OFFICER':        { username: 'lgu_naval',       password: 'password123' },
        'BARANGAY_OFFICIAL':  { username: 'brgy_olog',       password: 'password123' },
        'STAKEHOLDER':        { username: 'guide_pedro',     password: 'password123' },
        'TOURIST':            { username: 'tourist_ana',     password: 'password123' },
    };

    for (const [role, cred] of Object.entries(creds)) {
        try {
            const r = await req('POST', '/api/auth/login', cred);
            if (r.body.success && r.body.token) {
                tokens[role] = r.body.token;
                pass(`Login: ${role}`, `token: ${r.body.token.substring(0, 12)}...`);
            } else {
                fail(`Login: ${role}`, r.body.message || 'No token');
            }
        } catch (e) { fail(`Login: ${role}`, e.message); }
    }

    // -------------------------------------------------------------------------
    // PUBLIC ENDPOINTS
    // -------------------------------------------------------------------------
    console.log('\n--- Public API Endpoints ---');
    try {
        const r = await req('GET', '/api/destinations');
        if (r.status === 200 && Array.isArray(r.body)) pass('GET /api/destinations', `${r.body.length} destinations`);
        else fail('GET /api/destinations', `status ${r.status}`);
    } catch(e) { fail('GET /api/destinations', e.message); }

    try {
        const r = await req('GET', '/api/packages');
        if (r.status === 200 && Array.isArray(r.body)) pass('GET /api/packages', `${r.body.length} packages`);
        else fail('GET /api/packages', `status ${r.status}`);
    } catch(e) { fail('GET /api/packages', e.message); }

    try {
        const r = await req('GET', '/api/announcements');
        if (r.status === 200 && Array.isArray(r.body)) pass('GET /api/announcements', `${r.body.length} announcements`);
        else fail('GET /api/announcements', `status ${r.status}`);
    } catch(e) { fail('GET /api/announcements', e.message); }

    try {
        const r = await req('GET', '/api/barangays');
        if (r.status === 200 && Array.isArray(r.body)) pass('GET /api/barangays', `${r.body.length} barangays`);
        else fail('GET /api/barangays', `status ${r.status}`);
    } catch(e) { fail('GET /api/barangays', e.message); }

    try {
        const r = await req('GET', '/api/municipalities');
        if (r.status === 200 && Array.isArray(r.body)) pass('GET /api/municipalities', `${r.body.length} municipalities`);
        else fail('GET /api/municipalities', `status ${r.status}`);
    } catch(e) { fail('GET /api/municipalities', e.message); }

    try {
        const r = await req('GET', '/api/stakeholders/services');
        if (r.status === 200) pass('GET /api/stakeholders/services (public)');
        else fail('GET /api/stakeholders/services (public)', `status ${r.status}`);
    } catch(e) { fail('GET /api/stakeholders/services (public)', e.message); }

    try {
        const r = await req('GET', '/api/settings');
        if (r.status === 200) pass('GET /api/settings (public)');
        else fail('GET /api/settings (public)', `status ${r.status}`);
    } catch(e) { fail('GET /api/settings (public)', e.message); }

    try {
        const r = await req('GET', '/api/reports/kpis');
        if (r.status === 200) pass('GET /api/reports/kpis');
        else fail('GET /api/reports/kpis', `status ${r.status}`);
    } catch(e) { fail('GET /api/reports/kpis', e.message); }

    // -------------------------------------------------------------------------
    // TOURIST DASHBOARD
    // -------------------------------------------------------------------------
    console.log('\n--- Tourist Dashboard ---');
    const touristToken = tokens['TOURIST'];

    try {
        const r = await req('GET', '/api/auth/me', null, touristToken);
        if (r.status === 200 && r.body.userId) pass('GET /api/auth/me (Tourist)', `userId: ${r.body.userId}, role: ${r.body.roleCode}`);
        else fail('GET /api/auth/me (Tourist)', JSON.stringify(r.body));
    } catch(e) { fail('GET /api/auth/me (Tourist)', e.message); }

    try {
        const r = await req('GET', '/api/bookings?status=ALL', null, touristToken);
        if (r.status === 200 && Array.isArray(r.body)) pass('GET /api/bookings (Tourist)', `${r.body.length} bookings`);
        else fail('GET /api/bookings (Tourist)', `status ${r.status}`);
    } catch(e) { fail('GET /api/bookings (Tourist)', e.message); }

    try {
        const r = await req('GET', '/api/reviews', null, touristToken);
        if (r.status === 200) pass('GET /api/reviews (Tourist)');
        else fail('GET /api/reviews (Tourist)', `status ${r.status}`);
    } catch(e) { fail('GET /api/reviews (Tourist)', e.message); }

    // Test POST /api/auth/me (new profile update endpoint)
    try {
        const r = await req('POST', '/api/auth/me', { fullName: 'Ana Sophia Cortez', phone: '0917-555-1234' }, touristToken);
        if (r.status === 200 && r.body.success) pass('POST /api/auth/me (Tourist profile update)');
        else fail('POST /api/auth/me (Tourist profile update)', JSON.stringify(r.body));
    } catch(e) { fail('POST /api/auth/me (Tourist profile update)', e.message); }

    // -------------------------------------------------------------------------
    // STAKEHOLDER DASHBOARD
    // -------------------------------------------------------------------------
    console.log('\n--- Stakeholder Dashboard ---');
    const stkToken = tokens['STAKEHOLDER'];

    try {
        const r = await req('GET', '/api/auth/me', null, stkToken);
        if (r.status === 200) pass('GET /api/auth/me (Stakeholder)', `role: ${r.body.roleCode}`);
        else fail('GET /api/auth/me (Stakeholder)', `status ${r.status}`);
    } catch(e) { fail('GET /api/auth/me (Stakeholder)', e.message); }

    try {
        const r = await req('GET', '/api/stakeholders/me', null, stkToken);
        if (r.status === 200) pass('GET /api/stakeholders/me (Stakeholder profile)');
        else fail('GET /api/stakeholders/me', `status ${r.status}`);
    } catch(e) { fail('GET /api/stakeholders/me', e.message); }

    try {
        const r = await req('GET', '/api/bookings?status=ALL', null, stkToken);
        if (r.status === 200 && Array.isArray(r.body)) pass('GET /api/bookings (Stakeholder)', `${r.body.length} bookings`);
        else fail('GET /api/bookings (Stakeholder)', `status ${r.status}`);
    } catch(e) { fail('GET /api/bookings (Stakeholder)', e.message); }

    try {
        const r = await req('GET', '/api/stakeholders/my-verification', null, stkToken);
        if (r.status === 200 || r.status === 404) pass('GET /api/stakeholders/my-verification (Stakeholder)', `status ${r.status}`);
        else fail('GET /api/stakeholders/my-verification', `status ${r.status}`);
    } catch(e) { fail('GET /api/stakeholders/my-verification', e.message); }

    // -------------------------------------------------------------------------
    // BARANGAY DASHBOARD
    // -------------------------------------------------------------------------
    console.log('\n--- Barangay Dashboard ---');
    const brgyToken = tokens['BARANGAY_OFFICIAL'];

    try {
        const r = await req('GET', '/api/verifications', null, brgyToken);
        if (r.status === 200 && Array.isArray(r.body)) pass('GET /api/verifications (Barangay)', `${r.body.length} verifications`);
        else fail('GET /api/verifications (Barangay)', `status ${r.status}`);
    } catch(e) { fail('GET /api/verifications (Barangay)', e.message); }

    try {
        const r = await req('GET', '/api/destinations?status=ALL', null, brgyToken);
        if (r.status === 200) pass('GET /api/destinations (Barangay - with auth)');
        else fail('GET /api/destinations (Barangay)', `status ${r.status}`);
    } catch(e) { fail('GET /api/destinations (Barangay)', e.message); }

    try {
        const r = await req('GET', '/api/reports/barangays', null, brgyToken);
        if (r.status === 200) pass('GET /api/reports/barangays (Barangay)');
        else fail('GET /api/reports/barangays', `status ${r.status}`);
    } catch(e) { fail('GET /api/reports/barangays', e.message); }

    try {
        const r = await req('GET', '/api/stakeholders', null, brgyToken);
        if (r.status === 200) pass('GET /api/stakeholders (Barangay)');
        else fail('GET /api/stakeholders (Barangay)', `status ${r.status}`);
    } catch(e) { fail('GET /api/stakeholders (Barangay)', e.message); }

    // -------------------------------------------------------------------------
    // LGU DASHBOARD
    // -------------------------------------------------------------------------
    console.log('\n--- LGU Dashboard ---');
    const lguToken = tokens['LGU_OFFICER'];

    try {
        const r = await req('GET', '/api/reports/footfall', null, lguToken);
        if (r.status === 200) pass('GET /api/reports/footfall (LGU)');
        else fail('GET /api/reports/footfall (LGU)', `status ${r.status}`);
    } catch(e) { fail('GET /api/reports/footfall (LGU)', e.message); }

    try {
        const r = await req('GET', '/api/reports/stakeholders', null, lguToken);
        if (r.status === 200) pass('GET /api/reports/stakeholders (LGU)');
        else fail('GET /api/reports/stakeholders (LGU)', `status ${r.status}`);
    } catch(e) { fail('GET /api/reports/stakeholders (LGU)', e.message); }

    try {
        const r = await req('GET', '/api/admin/users', null, lguToken);
        if (r.status === 200 && Array.isArray(r.body)) pass('GET /api/admin/users (LGU)', `${r.body.length} users`);
        else fail('GET /api/admin/users (LGU)', `status ${r.status}`);
    } catch(e) { fail('GET /api/admin/users (LGU)', e.message); }

    // -------------------------------------------------------------------------
    // TOURISM OFFICE DASHBOARD
    // -------------------------------------------------------------------------
    console.log('\n--- Tourism Office Dashboard ---');
    const toToken = tokens['TOURISM_OFFICE'];

    try {
        const r = await req('GET', '/api/announcements', null, toToken);
        if (r.status === 200) pass('GET /api/announcements (Tourism Office)');
        else fail('GET /api/announcements (Tourism Office)', `status ${r.status}`);
    } catch(e) { fail('GET /api/announcements (Tourism Office)', e.message); }

    // Test Create Announcement
    let annoId = null;
    try {
        const r = await req('POST', '/api/announcements', {
            title: 'TEST — Automated Test Announcement',
            category: 'ANNOUNCEMENT',
            content: 'This is an automated test announcement. Safe to delete.',
            status: 'ACTIVE'
        }, toToken);
        if (r.body.success) {
            annoId = r.body.id || r.body.announcementId;
            pass('POST /api/announcements (Create)', `id: ${annoId}`);
        } else fail('POST /api/announcements (Create)', JSON.stringify(r.body));
    } catch(e) { fail('POST /api/announcements (Create)', e.message); }

    // Test Delete Announcement
    if (annoId) {
        try {
            const r = await req('POST', '/api/announcements/delete', { announcementId: annoId }, toToken);
            if (r.body.success) pass('POST /api/announcements/delete (Delete)');
            else fail('POST /api/announcements/delete (Delete)', JSON.stringify(r.body));
        } catch(e) { fail('POST /api/announcements/delete (Delete)', e.message); }
    }

    try {
        const r = await req('GET', '/api/admin/users', null, toToken);
        if (r.status === 200) pass('GET /api/admin/users (Tourism Office)');
        else fail('GET /api/admin/users (Tourism Office)', `status ${r.status}`);
    } catch(e) { fail('GET /api/admin/users (Tourism Office)', e.message); }

    // -------------------------------------------------------------------------
    // SYSTEM ADMINISTRATOR DASHBOARD
    // -------------------------------------------------------------------------
    console.log('\n--- System Administrator Dashboard ---');
    const adminToken = tokens['SYSTEM_ADMIN'];

    try {
        const r = await req('GET', '/api/admin/users', null, adminToken);
        if (r.status === 200 && Array.isArray(r.body)) pass('GET /api/admin/users (Admin)', `${r.body.length} users`);
        else fail('GET /api/admin/users (Admin)', `status ${r.status}`);
    } catch(e) { fail('GET /api/admin/users (Admin)', e.message); }

    try {
        const r = await req('GET', '/api/admin/logs?limit=10', null, adminToken);
        if (r.status === 200 && Array.isArray(r.body)) pass('GET /api/admin/logs (Admin)', `${r.body.length} logs`);
        else fail('GET /api/admin/logs (Admin)', `status ${r.status}`);
    } catch(e) { fail('GET /api/admin/logs (Admin)', e.message); }

    try {
        const r = await req('POST', '/api/settings', { broadcastMsg: 'Welcome to Lakbay Lokal!' }, adminToken);
        if (r.body.success) pass('POST /api/settings (Admin save settings)');
        else fail('POST /api/settings (Admin save settings)', JSON.stringify(r.body));
    } catch(e) { fail('POST /api/settings (Admin save settings)', e.message); }

    // Test toggle user status (uses a test user, then revert)
    try {
        const usersRes = await req('GET', '/api/admin/users', null, adminToken);
        const testUser = (usersRes.body || []).find(u => u.roleCode === 'TOURIST' && u.username !== 'tourist_ana');
        if (testUser) {
            const r = await req('POST', '/api/admin/users/status', { userId: testUser.userId, status: testUser.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' }, adminToken);
            if (r.body.success) {
                // Revert
                await req('POST', '/api/admin/users/status', { userId: testUser.userId, status: testUser.status }, adminToken);
                pass('POST /api/admin/users/status (Toggle user status)', `user: ${testUser.username}`);
            } else fail('POST /api/admin/users/status', JSON.stringify(r.body));
        } else {
            results.push({ status: 'NOT TESTED', name: 'POST /api/admin/users/status', detail: 'No suitable test user found' });
            console.log(`  - NOT TESTED: POST /api/admin/users/status`);
        }
    } catch(e) { fail('POST /api/admin/users/status', e.message); }

    // -------------------------------------------------------------------------
    // AUTHORIZATION SECURITY TESTS
    // -------------------------------------------------------------------------
    console.log('\n--- Authorization Security Tests ---');

    // Tourist cannot access admin endpoints
    try {
        const r = await req('GET', '/api/admin/users', null, touristToken);
        if (r.status === 403) pass('SECURITY: Tourist blocked from /api/admin/users (403)');
        else fail('SECURITY: Tourist should be blocked from /api/admin/users', `Got status ${r.status}`);
    } catch(e) { fail('SECURITY: Tourist blocked from admin', e.message); }

    // Tourist cannot delete destinations
    try {
        const r = await req('POST', '/api/destinations/delete', { destinationId: 1 }, touristToken);
        if (r.status === 403) pass('SECURITY: Tourist blocked from deleting destinations (403)');
        else fail('SECURITY: Tourist should be blocked from deleting destinations', `Got status ${r.status}`);
    } catch(e) { fail('SECURITY: Tourist blocked from destination delete', e.message); }

    // Unauthenticated request blocked
    try {
        const r = await req('GET', '/api/bookings');
        if (r.status === 401) pass('SECURITY: Unauthenticated /api/bookings blocked (401)');
        else fail('SECURITY: Unauthenticated /api/bookings should return 401', `Got ${r.status}`);
    } catch(e) { fail('SECURITY: Unauthenticated request check', e.message); }

    // Barangay cannot process verification for other barangay's stakeholder
    try {
        const r = await req('GET', '/api/verifications', null, touristToken);
        if (r.status === 403) pass('SECURITY: Tourist blocked from verifications (403)');
        else fail('SECURITY: Tourist should be blocked from verifications', `Got ${r.status}`);
    } catch(e) { fail('SECURITY: Tourist blocked from verifications', e.message); }

    // -------------------------------------------------------------------------
    // SUMMARY
    // -------------------------------------------------------------------------
    console.log('\n=== TEST SUMMARY ===');
    const passed = results.filter(r => r.status === 'PASS').length;
    const failed = results.filter(r => r.status === 'FAIL').length;
    const notTested = results.filter(r => r.status === 'NOT TESTED').length;
    console.log(`PASS:       ${passed}`);
    console.log(`FAIL:       ${failed}`);
    console.log(`NOT TESTED: ${notTested}`);
    console.log(`TOTAL:      ${results.length}`);

    if (failed > 0) {
        console.log('\nFailed tests:');
        results.filter(r => r.status === 'FAIL').forEach(r => console.log(`  - ${r.name}: ${r.detail}`));
    }
    console.log('\n=== DONE ===');
}

runTests().catch(console.error);
