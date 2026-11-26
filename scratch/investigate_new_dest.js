const http = require('http');

function post(url, body, headers = {}) {
    return new Promise((resolve, reject) => {
        const u = new URL(url);
        const req = http.request({
            hostname: u.hostname,
            port: u.port,
            path: u.pathname,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(body),
                ...headers
            }
        }, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => resolve({ status: res.statusCode, data: JSON.parse(data) }));
        });
        req.on('error', reject);
        req.write(body);
        req.end();
    });
}

function get(url, headers = {}) {
    return new Promise((resolve, reject) => {
        http.get(url, { headers }, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => resolve({ status: res.statusCode, data: JSON.parse(data) }));
        }).on('error', reject);
    });
}

async function testInvestigation() {
    console.log('=== INVESTIGATION LIFECYCLE FOR NEW DESTINATIONS ===');

    // 1. Login as Tourism Office (who can add destinations)
    const loginRes = await post('http://127.0.0.1:8080/api/auth/login', JSON.stringify({
        username: 'tourismoffice',
        password: 'password123'
    }));
    const token = loginRes.data.token;
    console.log('Logged in as tourismoffice, token:', !!token);

    // 2. Fetch current destinations before adding
    const beforeDests = await get('http://127.0.0.1:8080/api/destinations', { 'X-Auth-Token': token });
    console.log(`Current destinations count: ${beforeDests.data.length}`);
    beforeDests.data.forEach(d => console.log(` - [${d.destinationId}] ${d.name} (Brgy: ${d.barangayName}, Muni: ${d.municipalityName})`));

    // 3. Test adding a destination in Caibiran (Barangay 5: Manlabang)
    const newDestPayload = {
        name: 'Test Casiawan Falls',
        category: 'WATERFALL',
        barangayId: 5,
        description: 'A newly added scenic waterfall in Caibiran.',
        entranceFee: 30.0,
        operatingHours: '6:00 AM - 5:00 PM',
        locationCoordinates: '11.55, 124.55',
        contactInfo: 'caibiran.tourism@biliran.gov.ph',
        status: 'ACTIVE'
    };

    console.log('\nPosting new destination...');
    const addRes = await post('http://127.0.0.1:8080/api/destinations', JSON.stringify(newDestPayload), { 'X-Auth-Token': token });
    console.log('Add response:', addRes.status, addRes.data);

    // 4. Fetch destinations after adding
    const afterDests = await get('http://127.0.0.1:8080/api/destinations', { 'X-Auth-Token': token });
    console.log(`\nDestinations count after adding: ${afterDests.data.length}`);
    const added = afterDests.data.find(d => d.name === 'Test Casiawan Falls');
    console.log('Found added destination in GET /api/destinations:', added);

    // 5. Test filterDestinations() logic on the added destination
    const barangaysRes = await get('http://127.0.0.1:8080/api/barangays');
    const stateBarangays = barangaysRes.data;

    function simulateFilter(dests, search, muni, cat, brgy) {
        return dests.filter(d => {
            const matchSearch = !search || d.name.toLowerCase().includes(search) || d.description.toLowerCase().includes(search);
            const matchCat = cat === 'ALL' || d.category === cat;
            const destMuni = d.municipalityName || stateBarangays?.find(b => Number(b.barangayId) === Number(d.barangayId))?.municipalityName || '';
            const matchMuni = muni === 'ALL' || destMuni.toLowerCase() === muni.toLowerCase();
            const matchBrgy = brgy === 'ALL' || String(d.barangayId) === String(brgy);
            return matchSearch && matchCat && matchMuni && matchBrgy;
        });
    }

    if (added) {
        console.log('\n--- SIMULATING filterDestinations() ---');
        console.log('With muni = ALL:', simulateFilter(afterDests.data, '', 'ALL', 'ALL', 'ALL').some(d => d.name === added.name));
        console.log('With muni = Caibiran:', simulateFilter(afterDests.data, '', 'Caibiran', 'ALL', 'ALL').some(d => d.name === added.name));
        console.log('With muni = Almeria:', simulateFilter(afterDests.data, '', 'Almeria', 'ALL', 'ALL').some(d => d.name === added.name));
        console.log('With cat = WATERFALL:', simulateFilter(afterDests.data, '', 'ALL', 'WATERFALL', 'ALL').some(d => d.name === added.name));
        console.log('With search = Casiawan:', simulateFilter(afterDests.data, 'Casiawan', 'ALL', 'ALL', 'ALL').some(d => d.name === added.name));

        // Clean up test destination
        console.log('\nCleaning up test destination ID:', added.destinationId);
        const delRes = await post('http://127.0.0.1:8080/api/destinations/delete', JSON.stringify({ destinationId: added.destinationId }), { 'X-Auth-Token': token });
        console.log('Delete response:', delRes.data);
    }
}

testInvestigation().catch(console.error);
