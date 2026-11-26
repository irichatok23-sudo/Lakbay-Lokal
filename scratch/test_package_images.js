const http = require('http');

const packageImages = [
    'package_waterfalls.jpg',
    'package_waterfalls.webp',
    'package_higatangan.jpg',
    'package_artisan.jpg',
    'package_sambawan.jpg'
];

function checkImage(filename) {
    return new Promise((resolve) => {
        http.get('http://127.0.0.1:8080/assets/' + filename, (res) => {
            let bytes = 0;
            res.on('data', chunk => bytes += chunk.length);
            res.on('end', () => {
                resolve({
                    filename,
                    status: res.statusCode,
                    mime: res.headers['content-type'],
                    bytes
                });
            });
        }).on('error', err => resolve({ filename, error: err.message }));
    });
}

async function run() {
    console.log('Testing package image asset serving over HTTP:');
    let allOk = true;
    for (const img of packageImages) {
        const res = await checkImage(img);
        if (res.status === 200 && res.bytes > 0) {
            console.log(`  [OK] /assets/${res.filename} -> Status: 200, Content-Type: ${res.mime}, Size: ${res.bytes} bytes`);
        } else {
            console.error(`  [FAIL] /assets/${res.filename} -> Status: ${res.status}, Error: ${res.error || 'empty'}`);
            allOk = false;
        }
    }
    if (allOk) {
        console.log('\nAll curated package images are being served successfully!');
    } else {
        process.exit(1);
    }
}

run();
