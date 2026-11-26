const fs = require('fs');
const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8').split('\n');

for (let j = 485; j < 525; j++) {
    console.log((j+1) + ': ' + html[j]);
}
