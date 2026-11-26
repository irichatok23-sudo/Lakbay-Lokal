const fs = require('fs');
const crypto = require('crypto');
const path = require('path');

const dir = 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded';
if (fs.existsSync(dir)) {
    const files = fs.readdirSync(dir);
    for (const f of files) {
        const full = path.join(dir, f);
        const stat = fs.statSync(full);
        const md5 = crypto.createHash('md5').update(fs.readFileSync(full)).digest('hex');
        console.log(`${f} | size: ${stat.size} | mtime: ${stat.mtime.toISOString()} | md5: ${md5}`);
    }
}
