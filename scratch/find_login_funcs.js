const fs = require('fs');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

['handleLoginSubmit', 'showLoginView', 'initLoginForm', 'loginForm', 'glass-login', 'login-overlay', 'login-modal'].forEach(term => {
    console.log(`\n=== Matches for: ${term} ===`);
    appJs.split('\n').forEach((line, idx) => {
        if (line.includes(term)) {
            console.log(`L${idx+1}: ${line.trim()}`);
        }
    });
});
