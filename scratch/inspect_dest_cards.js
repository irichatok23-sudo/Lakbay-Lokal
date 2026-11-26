const fs = require('fs');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');

for (let i = 0; i < js.length; i++) {
    if (js[i].includes('function renderDestinationsGrid') || js[i].includes('function renderDestinationsList') || js[i].includes('function promptInquireOrBook')) {
        console.log(`--- Line ${i+1} ---`);
        for (let j = i; j < Math.min(js.length, i + 50); j++) {
            console.log((j+1) + ': ' + js[j]);
        }
    }
}
