const http = require('http');

function request(options, data = null) {
    return new Promise((resolve, reject) => {
        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                let json = null;
                try { json = JSON.parse(body); } catch (_) { json = body; }
                resolve({ status: res.statusCode, data: json });
            });
        });
        req.on('error', reject);
        if (data) req.write(typeof data === 'string' ? data : JSON.stringify(data));
        req.end();
    });
}

async function login(username, password) {
    const res = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/auth/login', method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    }, { username, password });
    return res.data && res.data.token ? res.data.token : null;
}

async function run() {
    console.log('Testing Unverified Service Publishing & Booking Protection...');

    // 1. Register a new pending stakeholder
    const username = 'pending_stk_' + Date.now().toString().slice(-4);
    await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/auth/register-stakeholder', method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    }, {
        username,
        password: 'password123',
        fullName: 'Pending Ecotour Guide',
        email: `${username}@test.com`,
        phone: '09181112222',
        businessName: 'Pending Guides Biliran',
        category: 'TOUR_GUIDE',
        yearsExperience: 2,
        barangayId: 1,
        municipalityId: 8
    });

    const pendingToken = await login(username, 'password123');

    // 2. Pending stakeholder tries to publish a service -> should be rejected with 403
    const pubRes = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/stakeholders/services', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': pendingToken }
    }, {
        serviceName: 'Unauthorized Sambawan Trek',
        serviceCategory: 'TOUR_GUIDE',
        basePrice: 500,
        pricingUnit: 'PER_PERSON',
        description: 'Trek without verification'
    });

    console.log(`[${pubRes.status === 403 ? 'PASS' : 'FAIL'}] Unverified stakeholder publishing service blocked (403): Status ${pubRes.status}, Error: ${pubRes.data.message}`);

    // 3. Tourist views services list -> confirm only available and verified services are visible
    const touristToken = await login('tourist_ana', 'password123');
    const svcRes = await request({
        hostname: '127.0.0.1', port: 8080, path: '/api/stakeholders/services?all=true', method: 'GET',
        headers: { 'X-Auth-Token': touristToken }
    });

    const touristSeesOnlyVerified = Array.isArray(svcRes.data) && svcRes.data.length > 0 && svcRes.data.every(s => s.available === true);
    console.log(`[${touristSeesOnlyVerified ? 'PASS' : 'FAIL'}] Tourist querying services cannot see unavailable/unverified services: Count ${svcRes.data.length}, All Available: ${touristSeesOnlyVerified}`);
}

run().catch(console.error);
