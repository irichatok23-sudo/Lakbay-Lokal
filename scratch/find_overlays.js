const fs = require('fs');

const css = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');
const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

console.log('=== FIXED ELEMENTS IN style.css ===');
const fixedRegex = /([^{}]+)\{[^{}]*position:\s*fixed[^{}]*\}/g;
let m;
while ((m = fixedRegex.exec(css)) !== null) {
    const sel = m[1].trim().replace(/\s+/g, ' ');
    console.log(`Selector: ${sel}`);
}

console.log('\n=== ABSOLUTE ELEMENTS WITH INSET OR 100% IN style.css ===');
const absRegex = /([^{}]+)\{[^{}]*position:\s*absolute[^{}]*(?:inset:\s*0|width:\s*100%)[^{}]*\}/g;
while ((m = absRegex.exec(css)) !== null) {
    const sel = m[1].trim().replace(/\s+/g, ' ');
    console.log(`Selector: ${sel}`);
}
