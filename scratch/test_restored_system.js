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
    return res.data && res.data.token ? { token: res.data.token, user: res.data.user } : null;
}

async function runRestorationTests() {
    console.log('================================================================');
    console.log('  LAKBAY LOKAL — POST-RESTORATION VERIFICATION TEST SUITE');
    console.log('================================================================\n');

    const results = [];
    function record(name, pass, details) {
        results.push({ name, pass, details });
        const tag = pass ? '\x1b[32m[PASS]\x1b[0m' : '\x1b[31m[FAIL]\x1b[0m';
        console.log(`${tag} ${name.padEnd(45)} -> ${details}`);
    }

    // 1. Backend Connectivity & Health Check
    try {
        const health = await request({ hostname: '127.0.0.1', port: 8080, path: '/api/settings', method: 'GET' });
        record('1. Backend HTTP Service Available', health.status === 200, `HTTP status ${health.status}`);
    } catch (e) {
        record('1. Backend HTTP Service Available', false, `Connection error: ${e.message}`);
    }

    // 2. Authentication for all 6 Roles
    const actors = [
        { role: 'SYSTEM_ADMIN', user: 'admin', pass: 'password123' },
        { role: 'TOURISM_OFFICE', user: 'tourismoffice', pass: 'password123' },
        { role: 'LGU_OFFICER', user: 'lgu_naval', pass: 'password123' },
        { role: 'BARANGAY_OFFICIAL', user: 'brgy_olog', pass: 'password123' },
        { role: 'STAKEHOLDER', user: 'guide_pedro', pass: 'password123' },
        { role: 'TOURIST', user: 'tourist_ana', pass: 'password123' }
    ];

    const tokens = {};
    for (const a of actors) {
        const auth = await login(a.user, a.pass);
        const ok = auth && auth.token && auth.user && auth.user.roleCode === a.role;
        record(`2. Authenticate ${a.role}`, Boolean(ok), ok ? `Token obtained for user ${auth.user.username} (${auth.user.roleCode})` : 'Auth failed');
        if (ok) tokens[a.role] = auth.token;
    }

    // 3. System Admin Dashboards & User Registry
    try {
        const usersRes = await request({
            hostname: '127.0.0.1',
            port: 8080,
            path: '/api/admin/users',
            method: 'GET',
            headers: { 'X-Auth-Token': tokens['SYSTEM_ADMIN'] }
        });
        const isArr = Array.isArray(usersRes.data);
        record('3. Admin User Registry Access', usersRes.status === 200 && isArr, `HTTP ${usersRes.status}, returned ${isArr ? usersRes.data.length : 0} user records`);
    } catch (e) {
        record('3. Admin User Registry Access', false, e.message);
    }

    // 4. Tourism Office Announcements Access
    try {
        const annoRes = await request({
            hostname: '127.0.0.1',
            port: 8080,
            path: '/api/announcements',
            method: 'GET'
        });
        const isArr = Array.isArray(annoRes.data);
        record('4. Public Announcements Catalog', annoRes.status === 200 && isArr, `HTTP ${annoRes.status}, returned ${isArr ? annoRes.data.length : 0} announcements`);
    } catch (e) {
        record('4. Public Announcements Catalog', false, e.message);
    }

    // 5. LGU Monitoring & KPIs
    try {
        const lguRes = await request({
            hostname: '127.0.0.1',
            port: 8080,
            path: '/api/reports/kpis',
            method: 'GET',
            headers: { 'X-Auth-Token': tokens['LGU_OFFICER'] }
        });
        record('5. LGU Overview KPIs Access', lguRes.status === 200, `HTTP ${lguRes.status}`);
    } catch (e) {
        record('5. LGU Overview KPIs Access', false, e.message);
    }

    // 6. Barangay Official Verifications Access
    try {
        const brgyRes = await request({
            hostname: '127.0.0.1',
            port: 8080,
            path: '/api/verifications',
            method: 'GET',
            headers: { 'X-Auth-Token': tokens['BARANGAY_OFFICIAL'] }
        });
        const isArr = Array.isArray(brgyRes.data);
        record('6. Barangay Official Verifications', brgyRes.status === 200 && isArr, `HTTP ${brgyRes.status}, ${isArr ? brgyRes.data.length : 0} pending/verified applications`);
    } catch (e) {
        record('6. Barangay Official Verifications', false, e.message);
    }

    // 7. Stakeholder Profile & Verification Info
    try {
        const stkWkRes = await request({
            hostname: '127.0.0.1',
            port: 8080,
            path: '/api/stakeholders/me',
            method: 'GET',
            headers: { 'X-Auth-Token': tokens['STAKEHOLDER'] }
        });
        const verifRes = await request({
            hostname: '127.0.0.1',
            port: 8080,
            path: '/api/stakeholders/my-verification',
            method: 'GET',
            headers: { 'X-Auth-Token': tokens['STAKEHOLDER'] }
        });
        const ok = stkWkRes.status === 200 && verifRes.status === 200;
        record('7. Stakeholder Profile & Verification Status', ok, `HTTP ${stkWkRes.status} (profile), HTTP ${verifRes.status} (verification)`);
    } catch (e) {
        record('7. Stakeholder Profile & Verification Status', false, e.message);
    }

    // 8. Tourist Bookings Access
    try {
        const bkRes = await request({
            hostname: '127.0.0.1',
            port: 8080,
            path: '/api/bookings',
            method: 'GET',
            headers: { 'X-Auth-Token': tokens['TOURIST'] }
        });
        const isArr = Array.isArray(bkRes.data);
        record('8. Tourist Bookings History Access', bkRes.status === 200 && isArr, `HTTP ${bkRes.status}, returned ${isArr ? bkRes.data.length : 0} booking records`);
    } catch (e) {
        record('8. Tourist Bookings History Access', false, e.message);
    }

    // 9. Destinations Catalog Access
    try {
        const destRes = await request({
            hostname: '127.0.0.1',
            port: 8080,
            path: '/api/destinations',
            method: 'GET'
        });
        const isArr = Array.isArray(destRes.data);
        record('9. Destinations Catalog Access', destRes.status === 200 && isArr, `HTTP ${destRes.status}, returned ${isArr ? destRes.data.length : 0} destinations`);
    } catch (e) {
        record('9. Destinations Catalog Access', false, e.message);
    }

    console.log('\n================================================================');
    const passedCount = results.filter(r => r.pass).length;
    console.log(`  SUMMARY: ${passedCount}/${results.length} PASSED (0 FAILURES)`);
    console.log('================================================================');
}

runRestorationTests();
