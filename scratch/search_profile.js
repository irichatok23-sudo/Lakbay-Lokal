const fs = require('fs');

const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

function findLines(text, query) {
    const lines = text.split('\n');
    const res = [];
    lines.forEach((l, i) => {
        if (l.toLowerCase().includes(query.toLowerCase())) res.push((i+1) + ': ' + l.trim());
    });
    return res;
}

console.log('--- profile in HTML ---');
findLines(html, 'profile').forEach(l => console.log(l));

console.log('\n--- profile in JS ---');
findLines(js, 'profile').forEach(l => console.log(l));
