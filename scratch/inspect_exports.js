const fs = require('fs');

const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

console.log('=== BUTTONS & ACTIONS IN index.html ===');
const buttonRegex = /<button[^>]*>([\s\S]*?)<\/button>/gi;
let match;
let count = 0;
while ((match = buttonRegex.exec(indexHtml)) !== null) {
    const fullTag = match[0];
    const text = match[1].replace(/<[^>]+>/g, '').trim();
    if (/export|download|report|csv|pdf|print/i.test(fullTag)) {
        console.log(`[Button ${++count}] text="${text}" snippet: ${fullTag.substring(0, 150)}...`);
    }
}

console.log('\n=== ONCLICK CALLS WITH EXPORT/DOWNLOAD/REPORT IN index.html ===');
const onclickRegex = /onclick="([^"]*?(?:export|download|report|csv|pdf|print)[^"]*?)"/gi;
while ((match = onclickRegex.exec(indexHtml)) !== null) {
    console.log(`- onclick: ${match[1]}`);
}

console.log('\n=== EXPORT/CSV/PDF REFERENCES IN app.js ===');
const appLines = appJs.split('\n');
appLines.forEach((line, idx) => {
    if (/export|csv|pdf|voucher/i.test(line)) {
        console.log(`L${idx+1}: ${line.trim()}`);
    }
});
