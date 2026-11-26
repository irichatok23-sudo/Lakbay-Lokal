const fs = require('fs');

console.log('=== FINDING EXPORT / DOWNLOAD FUNCTIONS ===');

// Check app.js for export
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const exportFns = [];
const lines = appJs.split('\n');
lines.forEach((l, i) => {
    if (l.match(/export|download|report.*csv|\.csv/i)) {
        exportFns.push(`app.js:${i+1}: ${l.trim().substring(0, 120)}`);
    }
});
console.log(`--- app.js export mentions (${exportFns.length}) ---`);
console.log(exportFns.join('\n'));

// Check index.html for export/download buttons
const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const exportBtns = [];
indexHtml.split('\n').forEach((l, i) => {
    if (l.match(/export|download/i)) {
        exportBtns.push(`index.html:${i+1}: ${l.trim().substring(0, 120)}`);
    }
});
console.log(`--- index.html export mentions (${exportBtns.length}) ---`);
console.log(exportBtns.join('\n'));

// Check Java files
function searchDir(dir) {
    fs.readdirSync(dir, { withFileTypes: true }).forEach(entry => {
        const full = dir + '/' + entry.name;
        if (entry.isDirectory()) {
            if (entry.name !== 'node_modules' && entry.name !== '.git' && entry.name !== 'target' && entry.name !== 'dist') {
                searchDir(full);
            }
        } else if (entry.name.endsWith('.java')) {
            const content = fs.readFileSync(full, 'utf8');
            if (content.match(/export|pdf|csv/i)) {
                console.log(`Java file match: ${full}`);
            }
        }
    });
}
console.log('--- Java files mentioning export/pdf/csv ---');
searchDir('src/main/java');
