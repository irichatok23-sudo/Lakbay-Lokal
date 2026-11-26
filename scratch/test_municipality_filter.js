const fs = require('fs');
const http = require('http');

function get(url) {
    return new Promise((resolve, reject) => {
        http.get(url, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => resolve({ status: res.statusCode, data: JSON.parse(data) }));
        }).on('error', reject);
    });
}

async function testMunicipalityFilter() {
    console.log('--- TEST SUITE: Phase 1 Task 1.1 Municipality Filter ---');
    let passed = 0;
    let failed = 0;

    function assert(cond, msg) {
        if (cond) {
            console.log(`[PASS] ${msg}`);
            passed++;
        } else {
            console.error(`[FAIL] ${msg}`);
            failed++;
        }
    }

    // 1. Verify HTML template
    const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
    assert(html.includes('id="dest-municipality-filter"'), 'HTML contains #dest-municipality-filter select');
    assert(html.includes('onchange="onDestinationMunicipalityChange()"'), 'HTML attaches onDestinationMunicipalityChange listener');

    // 2. Verify JS app code
    const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
    assert(js.includes('loadMunicipalities()'), 'app.js defines loadMunicipalities()');
    assert(js.includes('populateMunicipalityDropdowns()'), 'app.js defines populateMunicipalityDropdowns()');
    assert(js.includes('onDestinationMunicipalityChange()'), 'app.js defines onDestinationMunicipalityChange()');
    assert(js.includes('const destMuni = d.municipalityName'), 'filterDestinations accounts for municipality');

    // 3. Verify CSS styling
    const css = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');
    assert(css.includes('.filter-controls {') && css.includes('flex-wrap: wrap;'), 'CSS supports responsive wrapping for filter controls');

    // 4. Verify API endpoints live
    const resMuni = await get('http://127.0.0.1:8080/api/municipalities');
    assert(resMuni.status === 200 && resMuni.data.length === 8, `GET /api/municipalities returned 8 municipalities (HTTP ${resMuni.status})`);

    const resDest = await get('http://127.0.0.1:8080/api/destinations');
    assert(resDest.status === 200 && resDest.data.length >= 8, `GET /api/destinations returned destinations (HTTP ${resDest.status})`);

    const resBrgy = await get('http://127.0.0.1:8080/api/barangays');
    assert(resBrgy.status === 200 && resBrgy.data.length >= 12, `GET /api/barangays returned barangays (HTTP ${resBrgy.status})`);

    // 5. Test Filtering Logic Simulation
    const dests = resDest.data;
    const brgys = resBrgy.data;

    function simulateFilter(search, muni, cat, brgy) {
        return dests.filter(d => {
            const matchSearch = !search || d.name.toLowerCase().includes(search.toLowerCase()) || d.description.toLowerCase().includes(search.toLowerCase());
            const matchCat = cat === 'ALL' || d.category === cat;
            const destMuni = d.municipalityName || brgys.find(b => Number(b.barangayId) === Number(d.barangayId))?.municipalityName || '';
            const matchMuni = muni === 'ALL' || destMuni.toLowerCase() === muni.toLowerCase();
            const matchBrgy = brgy === 'ALL' || String(d.barangayId) === String(brgy);
            return matchSearch && matchCat && matchMuni && matchBrgy;
        });
    }

    // Filter test: Maripipi
    const maripipiDests = simulateFilter('', 'Maripipi', 'ALL', 'ALL');
    assert(maripipiDests.length === 2, `Maripipi has 2 destinations (got ${maripipiDests.length})`);
    assert(maripipiDests.some(d => d.name.includes('Sambawan')), 'Maripipi includes Sambawan Island');
    assert(maripipiDests.some(d => d.name.includes('Pottery')), 'Maripipi includes Maripipi Pottery');

    // Dynamic barangay cascade test:
    const maripipiBrgys = brgys.filter(b => b.municipalityName.toLowerCase() === 'maripipi');
    assert(maripipiBrgys.length === 3, `Maripipi has 3 barangays: Bato, Ermita, Ol-og (got ${maripipiBrgys.length})`);

    // Filter test: Almeria with Category = WATERFALL
    const almeriaWaterfalls = simulateFilter('', 'Almeria', 'WATERFALL', 'ALL');
    assert(almeriaWaterfalls.length === 2, `Almeria has 2 waterfalls: Reco Falls & Ulan-Ulan Falls (got ${almeriaWaterfalls.length})`);

    // Filter test: Naval with Search = 'sandbar'
    const navalSandbar = simulateFilter('sandbar', 'Naval', 'ALL', 'ALL');
    assert(navalSandbar.length === 1 && navalSandbar[0].name.includes('Higatangan'), 'Naval search "sandbar" matches Higatangan Island & Shifting Sandbar');

    // Filter test: Reset to ALL
    const allDests = simulateFilter('', 'ALL', 'ALL', 'ALL');
    assert(allDests.length === dests.length, `Reset to ALL returns all ${dests.length} destinations`);

    console.log(`\nRESULTS: ${passed} passed, ${failed} failed.`);
    if (failed > 0) process.exit(1);
}

testMunicipalityFilter().catch(err => {
    console.error('Fatal test error:', err);
    process.exit(1);
});
