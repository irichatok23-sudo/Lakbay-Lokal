const http = require('http');

function post(url, body) {
    return new Promise((resolve, reject) => {
        const u = new URL(url);
        const req = http.request({
            hostname: u.hostname,
            port: u.port,
            path: u.pathname,
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) }
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

function get(url, token = null) {
    return new Promise((resolve, reject) => {
        const headers = token ? { 'X-Auth-Token': token } : {};
        http.get(url, { headers }, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => resolve({ status: res.statusCode, data: JSON.parse(data) }));
        }).on('error', reject);
    });
}

async function runDeepInvestigation() {
    console.log('====================================================');
    console.log(' DEEP INVESTIGATION: MUNICIPALITY FILTER REGRESSION ');
    console.log('====================================================\n');

    // TEST 1: Check Database records directly from REST API
    const rolesToTest = [
        { name: 'Unauthenticated / Public', user: null, pass: null },
        { name: 'TOURIST (tourist_ana)', user: 'tourist_ana', pass: 'password123' },
        { name: 'TOURISM_OFFICE (tourismoffice)', user: 'tourismoffice', pass: 'password123' },
        { name: 'SYSTEM_ADMIN (admin)', user: 'admin', pass: 'password123' },
        { name: 'BARANGAY_OFFICIAL (brgy_olog)', user: 'brgy_olog', pass: 'password123' },
        { name: 'LGU_OFFICER (lgu_naval)', user: 'lgu_naval', pass: 'password123' }
    ];

    for (const r of rolesToTest) {
        let token = null;
        if (r.user) {
            const login = await post('http://127.0.0.1:8080/api/auth/login', JSON.stringify({ username: r.user, password: r.pass }));
            token = login.data.token;
        }
        const dests = await get('http://127.0.0.1:8080/api/destinations', token);
        console.log(`[API Response] ${r.name}: Status ${dests.status}, Total Destinations = ${dests.data.length}`);
        if (dests.data.length < 8) {
            console.log(`   --> Scoped destinations: ${dests.data.map(d => d.name).join(', ')}`);
        }
    }

    // TEST 2: Inspect municipalityName on every destination
    console.log('\n--- MUNICIPALITY NAME MAPPING AUDIT ---');
    const allDests = (await get('http://127.0.0.1:8080/api/destinations')).data;
    const allBrgys = (await get('http://127.0.0.1:8080/api/barangays')).data;
    const allMunis = (await get('http://127.0.0.1:8080/api/municipalities')).data;

    let muniMappingErrors = 0;
    allDests.forEach(d => {
        const destMuni = d.municipalityName || allBrgys.find(b => Number(b.barangayId) === Number(d.barangayId))?.municipalityName;
        const existsInMunis = allMunis.some(m => m.name.toLowerCase() === (destMuni || '').toLowerCase());
        console.log(`Dest [${d.destinationId}] "${d.name}":`);
        console.log(`   d.municipalityName: "${d.municipalityName}" | d.barangayId: ${d.barangayId} | resolved Muni: "${destMuni}" | existsInMunis: ${existsInMunis}`);
        if (!destMuni || !existsInMunis) muniMappingErrors++;
    });
    console.log(`Municipality Mapping Errors: ${muniMappingErrors}`);

    // TEST 3: Trace filterDestinations() with default and various inputs
    console.log('\n--- FILTER LOGIC TRACE ---');
    function testFilter(dests, search, muni, cat, brgy) {
        return dests.filter(d => {
            const matchSearch = !search || d.name.toLowerCase().includes(search) || d.description.toLowerCase().includes(search);
            const matchCat = cat === 'ALL' || d.category === cat;
            const destMuni = d.municipalityName || allBrgys.find(b => Number(b.barangayId) === Number(d.barangayId))?.municipalityName || '';
            const matchMuni = muni === 'ALL' || destMuni.toLowerCase() === muni.toLowerCase();
            const matchBrgy = brgy === 'ALL' || String(d.barangayId) === String(brgy);
            return matchSearch && matchCat && matchMuni && matchBrgy;
        });
    }

    console.log('Default filter (search="", muni="ALL", cat="ALL", brgy="ALL"):', testFilter(allDests, '', 'ALL', 'ALL', 'ALL').length);
    console.log('Filter with muni="ALL", brgy="ALL":', testFilter(allDests, '', 'ALL', 'ALL', 'ALL').map(d => d.name));
    
    // Check if dropdown initial value could be something else
    console.log('What if dest-municipality-filter is empty string "":', testFilter(allDests, '', '', 'ALL', 'ALL').length);
    console.log('What if dest-barangay-filter is empty string "":', testFilter(allDests, '', 'ALL', 'ALL', '').length);
}

runDeepInvestigation().catch(console.error);
