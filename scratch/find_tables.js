const fs = require('fs');

const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const tables = [];
const tableRegex = /<table[^>]*id="([^"]*)"[^>]*>[\s\S]*?<\/table>/g;
let match;
while ((match = tableRegex.exec(indexHtml)) !== null) {
    const id = match[1];
    // Find closest section/pane
    const pos = match.index;
    const before = indexHtml.substring(Math.max(0, pos - 1500), pos);
    const paneMatch = before.match(/id="(pane-[^"]+)"/g);
    const pane = paneMatch ? paneMatch[paneMatch.length - 1] : 'unknown';
    tables.push({ id, pane });
}
console.log('Tables in index.html:', tables);

// Also search for any "print" or "export" or "generate" or "download" in index.html
const actionRegex = /<(?:button|a)[^>]*onclick="([^"]*)"[^>]*>([\s\S]*?)<\/(?:button|a)>/g;
const exportActions = [];
while ((match = actionRegex.exec(indexHtml)) !== null) {
    const onclick = match[1];
    const text = match[2].replace(/<[^>]*>/g, '').trim();
    if (onclick.match(/export|print|download|report/i) || text.match(/export|print|download|report/i)) {
        exportActions.push({ onclick, text });
    }
}
console.log('Export/Download/Report actions in index.html:', exportActions);
