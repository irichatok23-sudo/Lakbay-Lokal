const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const lines = content.split('\n');
lines.forEach((line, idx) => {
    if (line.match(/function (handleLogin|showLoginView|showDashboard|renderDashboard|initDashboard|logout|closeModal|openModal)/)) {
        console.log((idx+1) + ': ' + line);
    }
});
