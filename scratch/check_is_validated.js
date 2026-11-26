const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

const lines = content.split('\n');
lines.forEach((l, i) => {
    if (l.includes('isValidated')) {
        console.log(`Line ${i+1}: ${l}`);
    }
});
