const { execSync } = require('child_process');

console.log('Stopping java processes...');
try {
    execSync('taskkill /F /IM java.exe', { stdio: 'inherit' });
    console.log('Java processes stopped.');
} catch (e) {
    console.log('No java process was running or already stopped.');
}

console.log('Starting backend...');
const cp = 'bin;lib/*;target/classes';
const cmd = `start /B javaw -cp "bin;lib/*" com.lakbaylokal.Main --headless`;
try {
    execSync(cmd, { stdio: 'ignore', windowsHide: true });
    console.log('Backend launch triggered.');
} catch (e) {
    console.error('Launch error:', e.message);
}
