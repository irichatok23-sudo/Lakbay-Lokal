const fs = require('fs');
const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8').split('\n');
html.forEach((l, i) => {
    if (l.includes('login') || l.includes('form-glass-login')) {
        console.log((i+1) + ': ' + l.trim());
    }
});
