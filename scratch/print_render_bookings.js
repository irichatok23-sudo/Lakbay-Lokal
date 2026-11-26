const fs = require('fs');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');

for (let j = 2140; j < 2280; j++) {
    console.log((j+1) + ': ' + js[j]);
}
