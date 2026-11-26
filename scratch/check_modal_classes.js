const fs = require('fs');
const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

const regex = /<div[^>]*class="[^"]*modal-backdrop[^"]*"[^>]*>/gi;
let m;
while ((m = regex.exec(indexHtml)) !== null) {
    console.log(m[0]);
}
