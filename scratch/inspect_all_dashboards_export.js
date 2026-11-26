const fs = require('fs');

const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

// Match all buttons in index.html with their text, onclick, and id
const btnRegex = /<button[^>]*>([\s\S]*?)<\/button>/gi;
let m;
const buttons = [];
while ((m = btnRegex.exec(indexHtml)) !== null) {
    const full = m[0];
    const text = m[1].replace(/<[^>]*>/g, '').trim();
    const onclickMatch = full.match(/onclick="([^"]*)"/i);
    const idMatch = full.match(/id="([^"]*)"/i);
    const onclick = onclickMatch ? onclickMatch[1] : '';
    const id = idMatch ? idMatch[1] : '';
    
    // Find section/pane context
    const idx = m.index;
    const before = indexHtml.substring(Math.max(0, idx - 2000), idx);
    const paneMatches = [...before.matchAll(/id="(pane-[^"]+)"/g)];
    const currentPane = paneMatches.length > 0 ? paneMatches[paneMatches.length - 1][1] : 'global/nav';

    if (
        full.toLowerCase().includes('export') ||
        full.toLowerCase().includes('download') ||
        full.toLowerCase().includes('report') ||
        full.toLowerCase().includes('print') ||
        onclick.toLowerCase().includes('export') ||
        onclick.toLowerCase().includes('report')
    ) {
        buttons.push({ id, text, onclick, pane: currentPane, snippet: full.substring(0, 100) });
    }
}

console.log('=== EXPORT / REPORT BUTTONS IN INDEX.HTML ===');
console.log(JSON.stringify(buttons, null, 2));

// Also check all export functions in app.js and pdf-export.js
console.log('\n=== EXPORT FUNCTIONS IN APP.JS & PDF-EXPORT.JS ===');
const fns = [];
appJs.split('\n').forEach((line, i) => {
    if (line.match(/(?:function|const|let)\s+(export\w+|download\w+|generate\w+Report)/i)) {
        fns.push(`app.js:${i+1}: ${line.trim()}`);
    }
});

const pdfExportJs = fs.readFileSync('src/main/resources/web/js/pdf-export.js', 'utf8');
pdfExportJs.split('\n').forEach((line, i) => {
    if (line.match(/(?:function|const|let)\s+(export\w+|download\w+|generate\w+Report)/i)) {
        fns.push(`pdf-export.js:${i+1}: ${line.trim()}`);
    }
});
console.log(fns.join('\n'));
