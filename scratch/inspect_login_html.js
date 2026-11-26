const fs = require('fs');
const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

const startIdx = indexHtml.indexOf('<div id="login-portal-view"');
const endIdx = indexHtml.indexOf('<div id="public-portal-view"');
console.log('=== #login-portal-view HTML ===');
console.log(indexHtml.substring(startIdx, endIdx));
