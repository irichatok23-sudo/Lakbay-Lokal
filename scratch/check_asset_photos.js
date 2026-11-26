const fs = require('fs');
const path = require('path');

const assetDir = 'src/main/resources/web/assets';
fs.readdirSync(assetDir).forEach(f => {
    const s = fs.statSync(path.join(assetDir, f));
    console.log(`asset: ${f} (${s.size} bytes)`);
});
