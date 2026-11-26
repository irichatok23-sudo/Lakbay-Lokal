const fs = require('fs');
const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const findIn = (src, name) => {
    src.split('\n').forEach((l, i) => {
        if (l.includes('fillLoginForm')) console.log(`${name}:${i+1}: ${l.trim()}`);
    });
};
findIn(html, 'index.html');
findIn(js, 'app.js');
