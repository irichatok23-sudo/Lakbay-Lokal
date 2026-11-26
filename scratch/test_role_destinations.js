const http = require('http');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://127.0.0.1:8080/api';

function request(urlPath, method = 'GET', body = null, token = null) {
    return new Promise((resolve, reject) => {
        const u = new URL(BASE_URL + urlPath);
        const options = {
            hostname: u.hostname,
            port: u.port,
            path: u.pathname + u.search,
            method: method,
            headers: {
                'Content-Type': 'application/json'
            }
        };
        if (token) options.headers['X-Auth-Token'] = token;

        const req = http.request(options, (res) => {
            let resBody = '';
            res.on('data', chunk => resBody += chunk);
            res.on('end', () => {
                let parsed = null;
                try { parsed = JSON.parse(resBody); } catch(e) { parsed = resBody; }
                resolve({ status: res.statusCode, body: parsed });
            });
        });
        req.on('error', reject);
        if (body) req.write(JSON.stringify(body));
        req.end();
    });
}

async function runTests() {
    let passed = 0;
    let failed = 0;

    function assert(desc, condition, extra = '') {
        if (condition) {
            console.log(`  [PASS] ${desc}`);
            passed++;
        } else {
            console.error(`  [FAIL] ${desc} ${extra ? '(' + extra + ')' : ''}`);
            failed++;
        }
    }

    console.log('=== 1. VERIFY LOGIN ACROSS ALL 6 ROLES ===');
    const accounts = [
        { role: 'TOURIST', user: 'tourist_ana', pass: 'password123' },
        { role: 'STAKEHOLDER', user: 'guide_pedro', pass: 'password123' },
        { role: 'BARANGAY_OFFICIAL', user: 'brgy_olog', pass: 'password123' },
        { role: 'LGU_OFFICER', user: 'lgu_naval', pass: 'password123' },
        { role: 'TOURISM_OFFICE', user: 'tourismoffice', pass: 'password123' },
        { role: 'SYSTEM_ADMIN', user: 'admin', pass: 'password123' },
    ];

    const tokens = {};
    const userProfiles = {};

    for (const acc of accounts) {
        const res = await request('/auth/login', 'POST', { username: acc.user, password: acc.pass });
        assert(`Login for role ${acc.role} (${acc.user}) succeeded`, res.status === 200 && res.body.success === true, JSON.stringify(res.body));
        tokens[acc.role] = res.body.token;
        userProfiles[acc.role] = res.body.user;
    }

    console.log('\n=== 2. DESTINATION ACCESS & JURISDICTION RESTRICTIONS ===');
    // Barangay official (brgy_olog, brgy 1)
    const brgyDest = await request('/destinations', 'GET', null, tokens['BARANGAY_OFFICIAL']);
    assert('Barangay official destination list returns array', Array.isArray(brgyDest.body));
    const allCabibihan = brgyDest.body.every(d => String(d.barangayId) === String(userProfiles['BARANGAY_OFFICIAL'].barangayId));
    assert(`Barangay official destinations strictly scoped to assigned barangay (${userProfiles['BARANGAY_OFFICIAL'].barangayId})`, allCabibihan);

    // LGU officer (lgu_naval, muni 1)
    const lguDest = await request('/destinations', 'GET', null, tokens['LGU_OFFICER']);
    assert('LGU officer destination list returns array', Array.isArray(lguDest.body));
    const allCaibiran = lguDest.body.every(d => String(d.municipalityId) === String(userProfiles['LGU_OFFICER'].municipalityId));
    assert(`LGU officer destinations strictly scoped to assigned municipality (${userProfiles['LGU_OFFICER'].municipalityId})`, allCaibiran);

    // Tourism Office & Admin
    const ptoDest = await request('/destinations', 'GET', null, tokens['TOURISM_OFFICE']);
    assert('Tourism Office sees full province-wide destinations', Array.isArray(ptoDest.body) && ptoDest.body.length >= 6);

    const adminDest = await request('/destinations', 'GET', null, tokens['SYSTEM_ADMIN']);
    assert('System Admin sees full province-wide destinations', Array.isArray(adminDest.body) && adminDest.body.length >= 6);

    console.log('\n=== 3. BACKEND BOOKING RESTRICTION TO TOURIST ROLE ===');
    // Try booking as non-tourists
    for (const r of ['STAKEHOLDER', 'BARANGAY_OFFICIAL', 'LGU_OFFICER', 'TOURISM_OFFICE', 'SYSTEM_ADMIN']) {
        const fakeBooking = {
            travelDateStart: '2026-12-01',
            travelDateEnd: '2026-12-02',
            numberOfPax: 2,
            totalAmount: 1500,
            items: []
        };
        const bRes = await request('/bookings', 'POST', fakeBooking, tokens[r]);
        const isBlocked = bRes.status === 403 || (bRes.body && (bRes.body.code === 403 || bRes.body.success === false));
        assert(`Backend rejects POST /api/bookings for ${r}`, isBlocked, JSON.stringify(bRes.body));
    }

    // Try booking as TOURIST (Valid role)
    const touristBookingRes = await request('/bookings', 'POST', {
        travelDateStart: '2026-12-10',
        travelDateEnd: '2026-12-11',
        numberOfPax: 2,
        totalAmount: 500,
        customNotes: 'Automated role verification test booking',
        items: []
    }, tokens['TOURIST']);
    assert('Backend allows POST /api/bookings for TOURIST', touristBookingRes.status === 200 && touristBookingRes.body.success === true, JSON.stringify(touristBookingRes.body));

    console.log('\n=== 4. STAKEHOLDER EARNINGS & TRANSACTIONS ISOLATION ===');
    // Stakeholder can access earnings
    const stkEarnRes = await request('/stakeholder/earnings', 'GET', null, tokens['STAKEHOLDER']);
    assert('Stakeholder can access /api/stakeholder/earnings', stkEarnRes.status === 200 && (stkEarnRes.body.totalEarnings !== undefined || stkEarnRes.body.transactions !== undefined));

    // Non-stakeholders are blocked from /api/stakeholder/earnings
    for (const r of ['TOURIST', 'BARANGAY_OFFICIAL', 'LGU_OFFICER', 'TOURISM_OFFICE', 'SYSTEM_ADMIN']) {
        const eRes = await request('/stakeholder/earnings', 'GET', null, tokens[r]);
        const isBlocked = eRes.status === 403 || (eRes.body && (eRes.body.code === 403 || eRes.body.success === false));
        assert(`Backend blocks ${r} from /api/stakeholder/earnings`, isBlocked, JSON.stringify(eRes.body));
    }

    console.log('\n=== 5. FRONTEND CODE VERIFICATION IN APP.JS ===');
    const appJsPath = path.join(__dirname, '..', 'src', 'main', 'resources', 'web', 'js', 'app.js');
    const appJs = fs.readFileSync(appJsPath, 'utf8');

    // 1. Destination Inquire / Book buttons only rendered for TOURIST
    assert('renderDestinationsGrid checks isTourist = role === "TOURIST"', appJs.includes("const isTourist = role === 'TOURIST';"));
    assert('touristActionHtml conditionally rendered with promptInquire and promptInquireOrBook', appJs.includes('if (isTourist) {') && appJs.includes('promptInquire(') && appJs.includes('promptInquireOrBook('));
    assert('item-card-footer uses ${touristActionHtml}', appJs.includes('${touristActionHtml}'));

    // 2. promptInquire and promptInquireOrBook role checks
    assert('promptInquire validates state.activeRole === "TOURIST"', appJs.includes("if (state.activeRole !== 'TOURIST') {\n        showToast('Access restricted: Only registered tourists can submit inquiries.', 'error');"));
    assert('promptInquireOrBook validates state.activeRole === "TOURIST"', appJs.includes("if (state.activeRole !== 'TOURIST') {\n        showToast('Access restricted: Only registered tourists can create bookings.', 'error');"));

    // 3. Packages and services booking guards
    assert('renderPackagesGrid guards Book Package button with state.activeRole === "TOURIST"', appJs.includes("${state.activeRole === 'TOURIST' ? `\n                    <button class=\"btn btn-primary btn-sm\" onclick=\"openPackageBookingModal("));
    assert('openPackageBookingModal guards state.activeRole === "TOURIST"', appJs.includes("if (state.activeRole !== 'TOURIST') {\n        showToast('Access restricted: Only registered tourists can book packages.', 'error');"));
    assert('openServiceBookingModal guards state.activeRole === "TOURIST"', appJs.includes("if (state.activeRole !== 'TOURIST') {\n        showToast('Access restricted: Only registered tourists can book services.', 'error');"));
    assert('submitCustomBooking guards state.activeRole === "TOURIST"', appJs.includes("if (state.activeRole !== 'TOURIST') {\n        showToast('Access restricted: Only registered tourists can create bookings.', 'error');"));

    // 4. Stakeholders directory Reserve Service button guarded
    assert('renderStakeholdersList guards Reserve Service with role === "TOURIST"', appJs.includes("${role === 'TOURIST' ? `\n                        <button class=\"btn btn-outline btn-sm\" onclick=\"promptInquireOrBook("));

    // 5. Hashchange protection
    assert('window.addEventListener("hashchange") is registered', appJs.includes("window.addEventListener('hashchange'"));
    assert('hashchange checks canRoleAccessTab(state.activeRole, rawHash)', appJs.includes("canRoleAccessTab(state.activeRole, rawHash)"));

    // 6. Logout clean-up
    assert('logout cleans up cachedStakeholderData', appJs.includes('cachedStakeholderData = null;'));
    assert('logout cleans up pane-earnings and pane-schedule', appJs.includes("paneEarnings.style.display = 'none'") && appJs.includes("paneSchedule.style.display = 'none'"));
    assert('logout cleans up stakeholder-overview-dashboard', appJs.includes("stakeholderDash.style.display = 'none'"));

    console.log(`\n========================================`);
    console.log(`FINAL RESULT: ${passed} PASSED, ${failed} FAILED`);
    console.log(`========================================`);

    if (failed > 0) {
        process.exit(1);
    }
}

runTests().catch(err => {
    console.error('Test execution error:', err);
    process.exit(1);
});
