const http = require('http');

function checkUrl(url) {
    return new Promise((resolve, reject) => {
        http.get(url, (res) => {
            let data = [];
            res.on('data', chunk => data.push(chunk));
            res.on('end', () => {
                const buffer = Buffer.concat(data);
                resolve({
                    status: res.statusCode,
                    contentType: res.headers['content-type'],
                    size: buffer.length
                });
            });
        }).on('error', err => reject(err));
    });
}

async function run() {
    console.log("Checking server health and carousel assets...");
    const urls = [
        'http://127.0.0.1:8080/index.html',
        'http://127.0.0.1:8080/css/style.css',
        'http://127.0.0.1:8080/js/app.js',
        'http://127.0.0.1:8080/assets/sambawan_island.jpg',
        'http://127.0.0.1:8080/assets/package_sambawan.jpg',
        'http://127.0.0.1:8080/assets/sambawan_island_hero_1791699785896.jpg',
        'http://127.0.0.1:8080/assets/sambawan_hilltop.jpg'
    ];

    let passed = 0;
    for (const u of urls) {
        try {
            const res = await checkUrl(u);
            if (res.status === 200 && res.size > 0) {
                console.log(`[PASS] ${u} -> HTTP ${res.status} (${res.size} bytes)`);
                passed++;
            } else {
                console.error(`[FAIL] ${u} -> HTTP ${res.status} (${res.size} bytes)`);
            }
        } catch (e) {
            console.error(`[ERROR] ${u} -> ${e.message}`);
        }
    }

    console.log(`Total checks passed: ${passed}/${urls.length}`);
    if (passed === urls.length) {
        process.exit(0);
    } else {
        process.exit(1);
    }
}

// Allow a couple seconds for server warmup
setTimeout(run, 2500);
