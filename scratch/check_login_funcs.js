const fs = require('fs');
const appjs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

const funcs = ['handleGlassLoginSubmit', 'togglePasswordVisibility', 'openForgotPasswordModal', 'fillLoginForm'];
funcs.forEach(f => {
    const idx = appjs.indexOf(`function ${f}`);
    console.log(`Function ${f} at char ${idx}`);
});
