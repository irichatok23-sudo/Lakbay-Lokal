const fs = require('fs');
const path = require('path');

const dir = 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded';
fs.readdirSync(dir).forEach(file => {
    const stats = fs.statSync(path.join(dir, file));
    console.log(`${file}: ${stats.size} bytes`);
});
