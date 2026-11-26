const { execSync } = require('child_process');
const fs = require('fs');

console.log('=== Git Stash ===');
try {
    console.log(execSync('git stash list', { encoding: 'utf8' }));
} catch (e) {
    console.log('Stash error:', e.message);
}

console.log('\n=== Git Diff on index.html (first 20 lines) ===');
try {
    console.log(execSync('git diff --stat src/main/resources/web/index.html', { encoding: 'utf8' }));
} catch (e) {
    console.log('Diff error:', e.message);
}

console.log('\n=== Git Diff on app.js (first 20 lines) ===');
try {
    console.log(execSync('git diff --stat src/main/resources/web/js/app.js', { encoding: 'utf8' }));
} catch (e) {
    console.log('Diff error:', e.message);
}
