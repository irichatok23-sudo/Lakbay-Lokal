const fs = require('fs');
const lines = fs.readFileSync('bin/web/index.html', 'utf8').split('\n');
for (let i = 835; i < 865; i++) {
    console.log(i + 1, JSON.stringify(lines[i]));
}
