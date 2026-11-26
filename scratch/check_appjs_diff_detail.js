const { execSync } = require('child_process');
const fs = require('fs');

try {
    const diff = execSync('git diff -U1 src/main/resources/web/js/app.js', { encoding: 'utf8' });
    console.log('app.js diff length:', diff.length);
    // Write diff to scratch file to inspect
    fs.writeFileSync('scratch/appjs_diff.txt', diff);
    console.log('First 500 chars of diff:');
    console.log(diff.substring(0, 500));
} catch (e) {
    console.log('Error:', e.message);
}
