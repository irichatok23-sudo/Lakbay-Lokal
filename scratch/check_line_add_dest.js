const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const lines = content.split('\n');
for (let i = 0; i < lines.length; i++) {
    if (lines[i].includes('handleAddDestinationSubmit')) {
        console.log(`Line ${i+1}: ${lines[i]}`);
        console.log(lines.slice(i, i + 25).join('\n'));
    }
}
