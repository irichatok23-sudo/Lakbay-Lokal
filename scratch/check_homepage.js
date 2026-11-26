const fs = require('fs');
const appjs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

const regex = /function redirectToRoleHomepage[\s\S]*?switchTab\((.*?)\)/;
const match = appjs.match(/function redirectToRoleHomepage[\s\S]*?switchTab\(.*?\);[\s\S]*?}/);
if (match) {
    console.log(match[0]);
} else {
    console.log("Not found via regex, searching string");
    const idx = appjs.indexOf('function redirectToRoleHomepage');
    console.log(appjs.substring(idx, idx + 400));
}
