const http = require('http');

function get(url) {
    return new Promise((resolve, reject) => {
        http.get(url, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => resolve(JSON.parse(data)));
        }).on('error', reject);
    });
}

async function run() {
    const dests = await get('http://127.0.0.1:8080/api/destinations');
    const brgy = await get('http://127.0.0.1:8080/api/barangays');
    const munis = await get('http://127.0.0.1:8080/api/municipalities');

    console.log(`Loaded ${dests.length} destinations, ${brgy.length} barangays, ${munis.length} municipalities.`);

    function filter(search = '', muni = 'ALL', cat = 'ALL', barangayId = 'ALL') {
        return dests.filter(d => {
            const matchSearch = !search || d.name.toLowerCase().includes(search.toLowerCase()) || d.description.toLowerCase().includes(search.toLowerCase());
            const matchCat = cat === 'ALL' || d.category === cat;
            const matchMuni = muni === 'ALL' || String(d.municipalityName).toLowerCase() === String(muni).toLowerCase();
            const matchBrgy = barangayId === 'ALL' || String(d.barangayId) === String(barangayId);
            return matchSearch && matchCat && matchMuni && matchBrgy;
        });
    }

    // Test 1: All
    console.log('Test 1 (All):', filter().map(d => `${d.name} (${d.municipalityName})`));

    // Test 2: Filter by Municipality = 'Maripipi'
    console.log('Test 2 (Maripipi):', filter('', 'Maripipi').map(d => `${d.name} (${d.municipalityName})`));

    // Test 3: Filter by Municipality = 'Naval' and search = 'sandbar'
    console.log('Test 3 (Naval + sandbar):', filter('sandbar', 'Naval').map(d => `${d.name} (${d.municipalityName})`));

    // Test 4: Dynamic barangay sub-filtering in Maripipi
    const maripipiBrgys = brgy.filter(b => b.municipalityName.toLowerCase() === 'maripipi');
    console.log('Test 4 (Maripipi Barangays):', maripipiBrgys.map(b => b.name));
}

run();
