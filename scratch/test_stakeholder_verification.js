const http = require('http');

const BASE_URL = 'http://127.0.0.1:8080/api';

function request(path, method = 'GET', body = null, token = null) {
    return new Promise((resolve, reject) => {
        const url = new URL(BASE_URL + path);
        const options = {
            hostname: url.hostname,
            port: url.port,
            path: url.pathname + url.search,
            method: method,
            headers: {
                'Content-Type': 'application/json'
            }
        };
        if (token) {
            options.headers['X-Auth-Token'] = token;
        }

        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    const parsed = JSON.parse(data);
                    resolve({ status: res.statusCode, body: parsed });
                } catch (e) {
                    resolve({ status: res.statusCode, raw: data });
                }
            });
        });

        req.on('error', reject);
        if (body) {
            req.write(JSON.stringify(body));
        }
        req.end();
    });
}

async function runTests() {
    console.log('====================================================');
    console.log(' LAKBAY LOKAL STAKEHOLDER & RBAC VERIFICATION SUITE');
    console.log('====================================================\n');

    let passed = 0;
    let failed = 0;

    function assert(cond, name, detail = '') {
        if (cond) {
            console.log(`[PASS] ${name}`);
            passed++;
        } else {
            console.error(`[FAIL] ${name} ${detail}`);
            failed++;
        }
    }

    // TEST 1: LOGIN FOR ALL 6 ROLES
    console.log('\n--- 1. Testing Authentication for all 6 Roles ---');
    const accounts = [
        { u: 'tourist_ana', p: 'password123', role: 'TOURIST' },
        { u: 'guide_pedro', p: 'password123', role: 'STAKEHOLDER' },
        { u: 'driver_juan', p: 'password123', role: 'STAKEHOLDER' },
        { u: 'brgy_olog', p: 'password123', role: 'BARANGAY_OFFICIAL' },
        { u: 'lgu_naval', p: 'password123', role: 'LGU_OFFICER' },
        { u: 'tourismoffice', p: 'password123', role: 'TOURISM_OFFICE' },
        { u: 'admin', p: 'password123', role: 'SYSTEM_ADMIN' }
    ];

    const tokens = {};
    for (const acc of accounts) {
        const res = await request('/auth/login', 'POST', { username: acc.u, password: acc.p });
        assert(res.status === 200 && res.body.success, `Login: ${acc.u} (${acc.role})`, JSON.stringify(res.body));
        if (res.body && res.body.token) {
            tokens[acc.u] = res.body.token;
        }
    }

    // TEST 2: STAKEHOLDER LOGIN PAYLOAD ISOLATION & ATTRIBUTES
    console.log('\n--- 2. Verifying Stakeholder Login Attributes ---');
    const pedroLogin = await request('/auth/login', 'POST', { username: 'guide_pedro', password: 'password123' });
    const pedroUser = pedroLogin.body.user;
    assert(pedroUser && pedroUser.stakeholderProfileId === 1, 'Pedro user.stakeholderProfileId === 1', `Got: ${pedroUser?.stakeholderProfileId}`);
    assert(pedroUser && pedroUser.category === 'TOUR_GUIDE', 'Pedro user.category === TOUR_GUIDE', `Got: ${pedroUser?.category}`);
    assert(typeof pedroUser?.businessName === 'string' && pedroUser.businessName.length > 0, 'Pedro user.businessName present', pedroUser?.businessName);

    const juanLogin = await request('/auth/login', 'POST', { username: 'driver_juan', password: 'password123' });
    const juanUser = juanLogin.body.user;
    assert(juanUser && juanUser.stakeholderProfileId === 2, 'Juan user.stakeholderProfileId === 2', `Got: ${juanUser?.stakeholderProfileId}`);
    assert(juanUser && juanUser.category === 'TRANSPORT_DRIVER', 'Juan user.category === TRANSPORT_DRIVER', `Got: ${juanUser?.category}`);
    assert(juanUser?.stakeholderProfileId !== pedroUser?.stakeholderProfileId, 'Pedro and Juan have isolated stakeholderProfileIds');

    // TEST 3: INDIVIDUAL STAKEHOLDER DASHBOARD ENDPOINT
    console.log('\n--- 3. Verifying Individual Stakeholder Dashboard Endpoint ---');
    const pedroDash = await request('/stakeholder/dashboard', 'GET', null, tokens['guide_pedro']);
    assert(pedroDash.status === 200, 'Pedro GET /stakeholder/dashboard returns 200');
    assert(pedroDash.body.profile.businessName.includes('Sambawan'), 'Pedro profile is Sambawan Island Eco-Trek');
    assert(pedroDash.body.profile.category === 'TOUR_GUIDE', 'Pedro profile category is TOUR_GUIDE');
    assert(Array.isArray(pedroDash.body.services) && pedroDash.body.services.length > 0, 'Pedro has registered services');
    assert(Array.isArray(pedroDash.body.schedule), 'Pedro has schedule array');
    assert(Array.isArray(pedroDash.body.transactions), 'Pedro has transactions array');
    assert(typeof pedroDash.body.metrics.totalEarnings !== 'undefined', 'Pedro has totalEarnings metric');
    assert(typeof pedroDash.body.metrics.paidEarnings !== 'undefined', 'Pedro has paidEarnings metric');

    const juanDash = await request('/stakeholder/dashboard', 'GET', null, tokens['driver_juan']);
    assert(juanDash.status === 200, 'Juan GET /stakeholder/dashboard returns 200');
    assert(juanDash.body.profile.category === 'TRANSPORT_DRIVER', 'Juan profile category is TRANSPORT_DRIVER');
    assert(juanDash.body.profile.profileId === 2, 'Juan profile ID is 2');

    // Verify Business Isolation: Pedro's dashboard must NOT contain Juan's services or Juan's private items
    const pedroHasJuanServices = pedroDash.body.services.some(s => s.stakeholderId === 2);
    assert(!pedroHasJuanServices, 'Privacy Isolation: Pedro dashboard does NOT contain Juan services');
    const juanHasPedroServices = juanDash.body.services.some(s => s.stakeholderId === 1);
    assert(!juanHasPedroServices, 'Privacy Isolation: Juan dashboard does NOT contain Pedro services');

    // TEST 4: UNAUTHORIZED ROLE ACCESS TO STAKEHOLDER DASHBOARD
    console.log('\n--- 4. Enforcing Role Access Controls on Stakeholder Dashboard ---');
    const touristDash = await request('/stakeholder/dashboard', 'GET', null, tokens['tourist_ana']);
    assert(touristDash.body.code === 403 || touristDash.status === 403, 'Tourist blocked from /stakeholder/dashboard (403)');
    const adminDash = await request('/stakeholder/dashboard', 'GET', null, tokens['admin']);
    assert(adminDash.body.code === 403 || adminDash.status === 403, 'Admin blocked from /stakeholder/dashboard (403)');

    // TEST 5: ACCEPT, DECLINE, AND MARK DONE ACTIONS + CROSS-STAKEHOLDER ENFORCEMENT
    console.log('\n--- 5. Testing Booking Item Ownership & Status Updates ---');
    // Booking 15 has Item 14 (Pedro, guide) and Item 15 (Juan, driver)
    // 5a: Cross-business update attempt: Pedro tries to update Juan's item 15 -> MUST BE 403
    const crossUpdate = await request('/bookings/item-status', 'POST', { itemId: 15, status: 'ACCEPTED' }, tokens['guide_pedro']);
    assert(crossUpdate.body.code === 403 || crossUpdate.status === 403, 'Pedro updating Juan item 15 rejected with 403');
    assert(crossUpdate.body.message && crossUpdate.body.message.includes('assigned to your business'), 'Error message states only assigned business can update');

    // 5b: Pedro accepts his own item 14 -> MUST BE 200
    const pedroAccept = await request('/bookings/item-status', 'POST', { itemId: 14, status: 'ACCEPTED' }, tokens['guide_pedro']);
    assert((pedroAccept.status === 200 && pedroAccept.body.success), 'Pedro accepted his own item 14 successfully');

    // 5c: Juan updates his own item 15 -> MUST BE 200
    const juanAccept = await request('/bookings/item-status', 'POST', { itemId: 15, status: 'ACCEPTED' }, tokens['driver_juan']);
    assert((juanAccept.status === 200 && juanAccept.body.success), 'Juan accepted his own item 15 successfully');

    // 5d: Pedro marks his item 14 as COMPLETED (Mark Done) -> MUST BE 200
    const pedroDone = await request('/bookings/item-status', 'POST', { itemId: 14, status: 'COMPLETED' }, tokens['guide_pedro']);
    assert((pedroDone.status === 200 && pedroDone.body.success), 'Pedro marked his item 14 as COMPLETED (Mark Done)');

    // 5e: Juan declines his own pending item 16 -> MUST BE 200
    const juanDecline = await request('/bookings/item-status', 'POST', { itemId: 16, status: 'REJECTED' }, tokens['driver_juan']);
    assert((juanDecline.status === 200 && juanDecline.body.success), 'Juan declined his own item 16 successfully');

    // TEST 6: SERVICE AVAILABILITY TOGGLE
    console.log('\n--- 6. Testing Stakeholder Service Availability Toggle ---');
    const toggleOwn = await request('/stakeholder/service-availability', 'POST', { serviceId: 1, isAvailable: false }, tokens['guide_pedro']);
    assert(toggleOwn.status === 200 && toggleOwn.body.success, 'Pedro toggled service 1 availability to false');
    const restoreOwn = await request('/stakeholder/service-availability', 'POST', { serviceId: 1, isAvailable: true }, tokens['guide_pedro']);
    assert(restoreOwn.status === 200 && restoreOwn.body.success, 'Pedro restored service 1 availability to true');

    const toggleOther = await request('/stakeholder/service-availability', 'POST', { serviceId: 3, isAvailable: false }, tokens['guide_pedro']);
    assert(toggleOther.body.code === 403 || toggleOther.status === 403, 'Pedro toggling Juan service 3 blocked with 403');

    // TEST 7: TOURIST BOOKING FLOW & SERVICE DISTRIBUTION
    console.log('\n--- 7. Testing Tourist Booking Creation and Distribution ---');
    const newBookingPayload = {
        travelDateStart: '2026-12-05',
        travelDateEnd: '2026-12-06',
        numberOfPax: 2,
        totalAmount: 3500.00,
        items: [
            { serviceId: 1, stakeholderId: 1, unitPrice: 1500.00, quantity: 1, subtotal: 1500.00 },
            { serviceId: 4, stakeholderId: 2, unitPrice: 2000.00, quantity: 1, subtotal: 2000.00 }
        ]
    };
    const createRes = await request('/bookings', 'POST', newBookingPayload, tokens['tourist_ana']);
    assert(createRes.status === 200 && createRes.body.success, 'Tourist Ana created package booking with Guide Pedro and Driver Juan');

    // TEST 8: VERIFY OTHER ROLES ACCESS PRESERVED
    console.log('\n--- 8. Verifying Other 5 Roles Operations Preserved ---');
    const dests = await request('/destinations', 'GET', null, tokens['tourist_ana']);
    assert(dests.status === 200 && Array.isArray(dests.body), 'Tourist can browse destinations');

    const brgyRecs = await request('/barangays', 'GET', null, tokens['brgy_olog']);
    assert(brgyRecs.status === 200, 'Barangay official can fetch barangay records');

    const lguKpis = await request('/reports/kpis', 'GET', null, tokens['lgu_naval']);
    assert(lguKpis.status === 200, 'LGU officer can view municipal KPIs');

    const verifs = await request('/verifications', 'GET', null, tokens['tourismoffice']);
    assert(verifs.status === 200, 'Tourism Office can access verifications desk');

    const health = await request('/admin/system/health', 'GET', null, tokens['admin']);
    assert(health.status === 200 && (health.body.status === 'HEALTHY' || health.body.overallStatus === 'HEALTHY'), 'Admin can access system monitoring & health');

    console.log('\n====================================================');
    console.log(` RESULTS: ${passed} PASSED, ${failed} FAILED`);
    console.log('====================================================\n');
}

runTests().catch(err => {
    console.error('Test execution error:', err);
});
