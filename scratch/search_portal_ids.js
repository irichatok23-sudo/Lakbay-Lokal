const fs = require('fs');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

['management-portal-view', 'public-portal-view', 'login-portal-view'].forEach(id => {
    console.log(`\n=== Uses of: ${id} ===`);
    appJs.split('\n').forEach((l, i) => {
        if (l.includes(id)) {
            console.log(`L${i+1}: ${l.trim()}`);
        }
    });
});
