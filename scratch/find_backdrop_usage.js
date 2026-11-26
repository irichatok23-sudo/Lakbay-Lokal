const fs = require('fs');
const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
console.log('HTML glass-portal-backdrop:', html.includes('glass-portal-backdrop'));
console.log('JS glass-portal-backdrop:', js.includes('glass-portal-backdrop'));
