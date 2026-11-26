const fs = require('fs');

const content = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const lines = content.split('\n');

lines.forEach((line, idx) => {
    if (line.includes('nav-item-') || line.includes('sidebar-nav-menu')) {
        console.log(`${idx + 1}: ${line}`);
    }
});
