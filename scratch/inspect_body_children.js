const fs = require('fs');
const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

// Find all top level elements inside <body>
const bodyStart = indexHtml.indexOf('<body');
const bodyEnd = indexHtml.indexOf('</body>');
const bodyContent = indexHtml.substring(bodyStart, bodyEnd);

console.log('=== DIRECT CHILDREN OR TOP-LEVEL WRAPPERS IN BODY ===');
const topLevelRegex = /<([a-z0-9]+)\s+([^>]*id="([^"]+)"[^>]*)>/gi;
let match;
while ((match = topLevelRegex.exec(bodyContent)) !== null) {
    console.log(`Tag: <${match[1]} id="${match[3]}">`);
}
