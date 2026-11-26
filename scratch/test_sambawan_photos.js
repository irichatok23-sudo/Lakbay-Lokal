const http = require('http');

const photos = [
    'sambawan_island.jpg',
    'package_sambawan.jpg',
    'sambawan_island_hero_1791699785896.jpg',
    'sambawan_hilltop.jpg'
];

async function check() {
    for (const p of photos) {
        await new Promise((resolve) => {
            http.get('http://127.0.0.1:8080/assets/' + p, (res) => {
                let len = 0;
                res.on('data', c => len += c.length);
                res.on('end', () => {
                    console.log(`[PHOTO] /assets/${p} -> ${res.statusCode} (${len} bytes)`);
                    resolve();
                });
            }).on('error', e => {
                console.error(`[ERROR] ${p}: ${e.message}`);
                resolve();
            });
        });
    }
}

check();
