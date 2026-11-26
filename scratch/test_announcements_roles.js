const http = require('http');

function post(path, data, token) {
    return new Promise((resolve, reject) => {
        const payload = JSON.stringify(data);
        const headers = {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(payload)
        };
        if (token) headers['X-Auth-Token'] = token;

        const req = http.request({
            hostname: '127.0.0.1',
            port: 8080,
            path: path,
            method: 'POST',
            headers: headers
        }, res => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => resolve({ status: res.statusCode, data: JSON.parse(body || '{}') }));
        });
        req.on('error', reject);
        req.write(payload);
        req.end();
    });
}

function get(path, token) {
    return new Promise((resolve, reject) => {
        const headers = {};
        if (token) headers['X-Auth-Token'] = token;
        const req = http.request({
            hostname: '127.0.0.1',
            port: 8080,
            path: path,
            method: 'GET',
            headers: headers
        }, res => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => resolve({ status: res.statusCode, data: JSON.parse(body || '{}') }));
        });
        req.on('error', reject);
        req.end();
    });
}

async function verifyAllRolesAnnouncements() {
    console.log('=== TEST 1: Community Worker (guide_pedro) Announcements Read-Only Verification ===');
    const cwLogin = await post('/api/auth/login', { username: 'guide_pedro', password: 'password123' });
    console.log('Community Worker Login:', cwLogin.status, 'Role:', cwLogin.data.user?.roleCode);
    const cwGet = await get('/api/announcements', cwLogin.data.token);
    console.log('GET /api/announcements Status:', cwGet.status, 'Total Announcements:', cwGet.data.length);

    // Attempt unauthorized creation (should fail / be rejected)
    const cwCreate = await post('/api/announcements', {
        title: 'Unauthorized Community Worker Bulletin',
        content: 'This should be blocked',
        category: 'ADVISORY'
    }, cwLogin.data.token);
    console.log('POST /api/announcements (Create Attempt):', cwCreate.status, 'Message:', cwCreate.data.message);

    console.log('\n=== TEST 2: Barangay Official (brgy_olog) Announcements Read-Only Verification ===');
    const brgyLogin = await post('/api/auth/login', { username: 'brgy_olog', password: 'password123' });
    console.log('Barangay Official Login:', brgyLogin.status, 'Role:', brgyLogin.data.user?.roleCode);
    const brgyGet = await get('/api/announcements', brgyLogin.data.token);
    console.log('GET /api/announcements Status:', brgyGet.status, 'Total Announcements:', brgyGet.data.length);

    // Attempt unauthorized creation (should fail / be rejected)
    const brgyCreate = await post('/api/announcements', {
        title: 'Unauthorized Barangay Official Bulletin',
        content: 'This should be blocked',
        category: 'ADVISORY'
    }, brgyLogin.data.token);
    console.log('POST /api/announcements (Create Attempt):', brgyCreate.status, 'Message:', brgyCreate.data.message);

    console.log('\n=== TEST 3: Authorized Publishers (Tourism Office / LGU Officer) Verification ===');
    const toLogin = await post('/api/auth/login', { username: 'tourismoffice', password: 'password123' });
    const toCreate = await post('/api/announcements', {
        title: 'Official Provincial CBT Advisory',
        content: 'Advisory on marine safety gear requirements for Biliran pumpboat operators.',
        category: 'ADVISORY'
    }, toLogin.data.token);
    console.log('Tourism Office POST /api/announcements Status:', toCreate.status, 'Message:', toCreate.data.message);
}

verifyAllRolesAnnouncements().catch(console.error);
