const fs = require('fs');
const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

const matches = [];
const regex = /id=["'](modal-[^"']+)["']/g;
let m;
while ((m = regex.exec(html)) !== null) {
    matches.push(m[1]);
}
console.log('Modals in index.html:', matches);
