const fs = require('fs');
const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

const scriptRegex = /<script(?:\s+[^>]*)?>([\s\S]*?)<\/script>/gi;
let m;
let count = 0;
while ((m = scriptRegex.exec(indexHtml)) !== null) {
    count++;
    const srcMatch = m[0].match(/src="([^"]+)"/);
    if (srcMatch) {
        console.log(`Script ${count}: src="${srcMatch[1]}"`);
    } else {
        console.log(`Script ${count} (inline, ${m[1].length} chars):\n${m[1].substring(0, 300)}...`);
    }
}
