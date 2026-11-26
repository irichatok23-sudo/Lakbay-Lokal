const fs = require('fs');

const content = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const lines = content.split('\n');

lines.forEach((line, idx) => {
    if (line.includes('handleRoleSwitch') || line.includes('role-select') || line.includes('applyRoleNavigation')) {
        console.log(`${idx + 1}: ${line}`);
    }
});
