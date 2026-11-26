const fs = require('fs');

const content = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');
const lines = content.split('\n');

lines.forEach((line, idx) => {
    if (line.includes('role-workspace-banner')) {
        console.log(`${idx + 1}: ${line}`);
    }
});
