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

async function verifyBrowserDOMSimulation() {
    const dests = await get('http://127.0.0.1:8080/api/destinations');
    const munis = await get('http://127.0.0.1:8080/api/municipalities');
    const brgys = await get('http://127.0.0.1:8080/api/barangays');

    // Simulate DOM elements as in browser
    const elements = {
        'dest-search-input': { value: '' },
        'dest-municipality-filter': { value: 'ALL' },
        'dest-category-filter': { value: 'ALL' },
        'dest-barangay-filter': { value: 'ALL' }
    };

    function filterDestinations() {
        const search = (elements['dest-search-input']?.value || '').toLowerCase();
        const muni = elements['dest-municipality-filter']?.value || 'ALL';
        const cat = elements['dest-category-filter']?.value || 'ALL';
        const brgy = elements['dest-barangay-filter']?.value || 'ALL';

        return dests.filter(d => {
            const matchSearch = !search || d.name.toLowerCase().includes(search) || d.description.toLowerCase().includes(search);
            const matchCat = cat === 'ALL' || d.category === cat;

            const destMuni = d.municipalityName || brgys?.find(b => Number(b.barangayId) === Number(d.barangayId))?.municipalityName || '';
            const matchMuni = muni === 'ALL' || destMuni.toLowerCase() === muni.toLowerCase();

            const matchBrgy = brgy === 'ALL' || String(d.barangayId) === String(brgy);
            return matchSearch && matchCat && matchMuni && matchBrgy;
        });
    }

    const filtered = filterDestinations();
    console.log(`Filtered result count: ${filtered.length} out of ${dests.length}`);
    filtered.forEach(d => console.log(` - ID ${d.destinationId}: ${d.name} (${d.municipalityName})`));
}

verifyBrowserDOMSimulation();
