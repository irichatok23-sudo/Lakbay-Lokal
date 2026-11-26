const fs = require('fs');
const appjs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

const idx = appjs.indexOf('const ROLE_PERMITTED_TABS');
console.log(appjs.substring(idx, idx + 600));
