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

async function verifyCommunityWorkerWorkflows() {
    console.log('=== TEST 1: Community Worker Login ===');
    const loginRes = await post('/api/auth/login', { username: 'guide_pedro', password: 'password123' });
    const token = loginRes.data.token;
    console.log('Status:', loginRes.status, 'User:', loginRes.data.user?.fullName, 'Role:', loginRes.data.user?.roleCode);

    console.log('\n=== TEST 2: Read Announcements as Community Worker ===');
    const annRes = await get('/api/announcements', token);
    console.log('GET /api/announcements Status:', annRes.status, 'Count:', annRes.data.length);

    console.log('\n=== TEST 3: Attempt Create Announcement as Community Worker (Expect HTTP 403) ===');
    const annCreate = await post('/api/announcements', {
        title: 'Unauthorized Announcement',
        content: 'Community worker attempt',
        category: 'ADVISORY'
    }, token);
    console.log('POST /api/announcements Status:', annCreate.status, 'Message:', annCreate.data.message);

    console.log('\n=== TEST 4: Attempt Delete Announcement as Community Worker (Expect HTTP 403) ===');
    const annDelete = await post('/api/announcements/delete', { announcementId: 1 }, token);
    console.log('POST /api/announcements/delete Status:', annDelete.status, 'Message:', annDelete.data.message);

    console.log('\n=== TEST 5: Verify Preserved Stakeholder Endpoints ===');
    const dashRes = await get('/api/stakeholder/dashboard', token);
    console.log('GET /api/stakeholder/dashboard Status:', dashRes.status, 'Profile Name:', dashRes.data.profile?.businessName);

    const earnRes = await get('/api/stakeholder/earnings', token);
    console.log('GET /api/stakeholder/earnings Status:', earnRes.status, 'Total Earnings:', earnRes.data.metrics?.totalEarnings);

    const bookRes = await get('/api/bookings', token);
    console.log('GET /api/bookings Status:', bookRes.status, 'Assigned Bookings Count:', bookRes.data.length);
}

verifyCommunityWorkerWorkflows().catch(console.error);
