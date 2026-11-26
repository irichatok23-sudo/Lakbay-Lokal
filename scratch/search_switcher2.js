const fs = require('fs');

const content = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const lines = content.split('\n');

lines.forEach((line, idx) => {
    if (line.includes('switchRole') || line.includes('setRole') || line.includes('roleSelect') || line.includes('changeRole')) {
        console.log(`${idx + 1}: ${line}`);
    }
});
