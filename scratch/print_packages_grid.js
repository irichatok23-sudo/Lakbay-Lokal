const fs = require('fs');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');

for (let j = 1910; j < 1965; j++) {
    console.log((j+1) + ': ' + js[j]);
}
