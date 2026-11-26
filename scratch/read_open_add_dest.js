const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

const s = content.indexOf('function openAddDestinationModal');
console.log(content.substring(s, s + 1500));
