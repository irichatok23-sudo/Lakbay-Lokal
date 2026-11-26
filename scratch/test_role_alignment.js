const http = require('http');

function post(path, data) {
    return new Promise((resolve, reject) => {
        const payload = JSON.stringify(data);
        const req = http.request({
            hostname: '127.0.0.1',
            port: 8080,
            path: path,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(payload)
            }
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
        const req = http.request({
            hostname: '127.0.0.1',
            port: 8080,
            path: path,
            method: 'GET',
            headers: { 'X-Auth-Token': token }
        }, res => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => resolve({ status: res.statusCode, data: JSON.parse(body || '{}') }));
        });
        req.on('error', reject);
        req.end();
    });
}

async function testRoles() {
    console.log('1. Testing LGU Officer login and destinations scoping:');
    const lguLogin = await post('/api/auth/login', { username: 'lgu_naval', password: 'password123' });
    console.log('LGU Login:', lguLogin.status, lguLogin.data.success ? 'SUCCESS' : 'FAILED', 'Role:', lguLogin.data.user?.roleCode);
    
    const lguDests = await get('/api/destinations', lguLogin.data.token);
    console.log(`LGU Destinations count: ${lguDests.data.length} (Scoped to Naval: ${lguDests.data.every(d => d.municipalityName === 'Naval')})`);

    console.log('\n2. Testing Tourist login and announcements access:');
    const touristLogin = await post('/api/auth/login', { username: 'tourist_ana', password: 'password123' });
    console.log('Tourist Login:', touristLogin.status, touristLogin.data.success ? 'SUCCESS' : 'FAILED');

    const touristAnnounce = await get('/api/announcements', touristLogin.data.token);
    console.log(`Tourist Announcements count: ${touristAnnounce.data.length} retrieved`);

    console.log('\n3. Testing Barangay Official scoping check:');
    const brgyLogin = await post('/api/auth/login', { username: 'brgy_olog', password: 'password123' });
    const brgyDests = await get('/api/destinations', brgyLogin.data.token);
    console.log(`Brgy Official Destinations count: ${brgyDests.data.length} (Scoped to Ol-og / Maripipi: ${brgyDests.data.every(d => d.barangayName === 'Ol-og' || d.municipalityName === 'Maripipi')})`);
}

testRoles().catch(console.error);
