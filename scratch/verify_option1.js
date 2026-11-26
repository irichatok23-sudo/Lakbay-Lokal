const http = require('http');
const fs = require('fs');

function get(path) {
    return new Promise((resolve, reject) => {
        http.get('http://127.0.0.1:8080' + path, res => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => resolve(JSON.parse(body || '[]')));
        }).on('error', reject);
    });
}

async function verifyOption1() {
    console.log('=== Verifying Clean Dropdowns (Option 1) ===');
    const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

    // Check all dropdowns
    const dropdownMatches = [
        { name: 'Public Hero Filter', regex: /<select id="public-hero-cat-filter"[\s\S]*?<\/select>/ },
        { name: 'Dashboard Filter', regex: /<select id="dest-category-filter"[\s\S]*?<\/select>/ },
        { name: 'Add/Edit Modal', regex: /<select id="dest-form-category"[\s\S]*?<\/select>/ },
        { name: 'Public Filter Pills', regex: /<div class="category-filter-pills"[\s\S]*?<\/div>/ }
    ];

    for (const d of dropdownMatches) {
        const match = indexHtml.match(d.regex);
        const text = match ? match[0] : '';
        const hasCave = text.includes('CAVE_MOUNTAIN');
        const hasAgri = text.includes('AGRICULTURAL_ECO');
        console.log(`[${d.name}] Clean: ${!hasCave && !hasAgri} (Has CAVE: ${hasCave}, Has AGRI: ${hasAgri})`);
    }

    // Check live database destinations
    const dests = await get('/api/destinations?status=ALL');
    const activeCats = [...new Set(dests.map(d => d.category))];
    console.log('\nActive Categories in Database:', activeCats);
    console.log('Total destinations count:', dests.length);
}

verifyOption1().catch(console.error);
