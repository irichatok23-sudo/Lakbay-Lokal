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

async function verifyBarangayOfficialWorkflows() {
    console.log('=== TEST 1: Barangay Official Login ===');
    const loginRes = await post('/api/auth/login', { username: 'brgy_olog', password: 'password123' });
    const token = loginRes.data.token;
    console.log('Status:', loginRes.status, 'User:', loginRes.data.user?.fullName, 'Role:', loginRes.data.user?.roleCode);

    console.log('\n=== TEST 2: Read Announcements as Barangay Official ===');
    const annRes = await get('/api/announcements', token);
    console.log('GET /api/announcements Status:', annRes.status, 'Count:', annRes.data.length);

    console.log('\n=== TEST 3: Attempt Create Announcement as Barangay Official (Expect HTTP 403) ===');
    const annCreate = await post('/api/announcements', {
        title: 'Unauthorized Announcement',
        content: 'Barangay official attempt',
        category: 'ADVISORY'
    }, token);
    console.log('POST /api/announcements Status:', annCreate.status, 'Message:', annCreate.data.message);

    console.log('\n=== TEST 4: Attempt Delete Announcement as Barangay Official (Expect HTTP 403) ===');
    const annDelete = await post('/api/announcements/delete', { announcementId: 1 }, token);
    console.log('POST /api/announcements/delete Status:', annDelete.status, 'Message:', annDelete.data.message);

    console.log('\n=== TEST 5: Verify Preserved Accreditation Workflow (GET /api/verifications) ===');
    const verifGet = await get('/api/verifications', token);
    console.log('GET /api/verifications Status:', verifGet.status, 'Pending Verifications Count:', verifGet.data.length);

    console.log('\n=== TEST 6: Verify Preserved Destination Scoping (GET /api/destinations) ===');
    const destGet = await get('/api/destinations', token);
    console.log('GET /api/destinations Status:', destGet.status, 'Scoped to Ol-og:', destGet.data.every(d => d.barangayName === 'Ol-og' || d.municipalityName === 'Maripipi'));
}

verifyBarangayOfficialWorkflows().catch(console.error);
