const fs = require('fs');

const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

console.log('--- TABLES IN INDEX.HTML ---');
const tableMatches = html.matchAll(/<table[^>]*>[\s\S]*?<\/table>/g);
for (const tm of tableMatches) {
    const tableHtml = tm[0];
    const idMatch = tableHtml.match(/id="([^"]+)"/);
    const tableId = idMatch ? idMatch[1] : 'unnamed';
    if (tableHtml.toLowerCase().includes('action') || tableHtml.includes('eye') || tableHtml.includes('cancel')) {
        console.log(`Table ID: ${tableId}`);
        const ths = tableHtml.match(/<th[^>]*>[\s\S]*?<\/th>/g) || [];
        console.log(`  Headers:`, ths.map(t => t.replace(/<[^>]+>/g, '').trim()));
    }
}

console.log('\n--- EYE / CANCEL / ACTION BUTTONS IN APP.JS ---');
const lines = js.split('\n');
lines.forEach((line, idx) => {
    if (line.includes('fa-eye') || line.includes('openViewBookingModal') || line.includes('Cancel') || line.includes('openView')) {
        if (line.includes('<button') || line.includes('btn') || line.includes('onclick')) {
            console.log(`Line ${idx + 1}: ${line.trim()}`);
        }
    }
});
