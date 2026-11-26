const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

const s = content.indexOf('id="pane-reports"');
console.log(content.substring(s, s + 2000));
