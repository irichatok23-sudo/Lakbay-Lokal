const fs = require('fs');
const css = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');

css.split('\n').forEach((line, idx) => {
    if (line.includes('login-portal-view')) {
        console.log(`L${idx+1}: ${line}`);
    }
});
