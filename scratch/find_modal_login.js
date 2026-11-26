const fs = require('fs');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');
js.forEach((l, i) => {
    if (l.includes('modal-login')) {
        console.log((i+1) + ': ' + l);
    }
});
