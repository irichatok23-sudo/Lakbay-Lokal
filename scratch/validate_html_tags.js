const fs = require('fs');
const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

// Basic tag stack check for divs
const tokens = indexHtml.match(/<\/?div\b[^>]*>/gi) || [];
let depth = 0;
let errors = 0;
tokens.forEach((t, i) => {
    if (t.startsWith('</')) {
        depth--;
        if (depth < 0) {
            console.log(`Unmatched closing tag at token ${i}: ${t}`);
            errors++;
            depth = 0;
        }
    } else if (!t.endsWith('/>')) {
        depth++;
    }
});
console.log(`Final div depth: ${depth} (should be 0)`);
