const fs = require('fs');
const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const regex = /<section[^>]*class=["'][^"']*tab-pane[^"']*["'][^>]*id=["']([^"']+)["'][^>]*>/gi;
let match;
while ((match = regex.exec(html)) !== null) {
    console.log(match[1]);
}
