const fs = require('fs');
const css = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');

const s = css.indexOf('.filter-toolbar');
console.log(css.substring(s, s + 1000));
