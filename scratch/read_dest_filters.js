const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

const start = content.indexOf('featured-destination-hero');
console.log(content.substring(start + 500, start + 2500));
