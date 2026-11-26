const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

const start = content.indexOf('hero-thumbnails-container');
console.log(content.substring(start + 50, start + 2000));
