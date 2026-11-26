const http = require('http');

function get(path) {
    return new Promise((resolve, reject) => {
        http.get('http://127.0.0.1:8080' + path, res => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => resolve(JSON.parse(body || '[]')));
        }).on('error', reject);
    });
}

// Emulate client-side filter function from app.js
function testClientFiltering(destinations, category) {
    return destinations.filter(d => {
        const matchCat = category === 'ALL' || d.category === category;
        return matchCat;
    });
}

async function verifyCategoryFiltering() {
    const allDests = await get('/api/destinations?status=ALL');
    console.log(`Total Destinations Loaded: ${allDests.length}`);

    const normalizedCategories = [
        'ALL',
        'WATERFALL',
        'ISLAND_BEACH',
        'HOT_SPRING',
        'CULTURAL_HERITAGE'
    ];

    console.log('\n=== Testing Client-Side and API Filtering for Every Normalized Category ===');
    for (const cat of normalizedCategories) {
        // Test client-side filter
        const clientFiltered = testClientFiltering(allDests, cat);
        // Test API filter
        const apiFiltered = await get(`/api/destinations?category=${cat}&status=ALL`);

        console.log(`Category: ${cat.padEnd(18)} | Matched: ${clientFiltered.length} records | API matches: ${apiFiltered.length}`);
        clientFiltered.forEach(d => console.log(`   └─ [${d.municipalityName}] ${d.name}`));
    }
}

verifyCategoryFiltering().catch(console.error);
