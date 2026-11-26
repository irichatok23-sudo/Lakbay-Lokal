const fs = require('fs');

try {
    const code = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
    new Function(code);
    console.log('app.js parsed without syntax errors.');
} catch (e) {
    console.error('SYNTAX ERROR in app.js:', e);
}
