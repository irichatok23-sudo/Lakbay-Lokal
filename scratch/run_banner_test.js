const { spawnSync } = require('child_process');
const path = require('path');

const electronExe = path.join(__dirname, '..', 'node_modules', 'electron', 'dist', 'electron.exe');
const testScript = path.join(__dirname, 'test_banner_visibility_lifecycle.js');

const env = { ...process.env };
delete env.ELECTRON_RUN_AS_NODE;

console.log('Starting Electron banner test runner...');
const result = spawnSync(electronExe, [testScript, '--no-sandbox', '--disable-gpu'], {
    env,
    stdio: 'inherit',
    encoding: 'utf-8'
});

console.log('Electron banner test runner exited with code:', result.status);
process.exit(result.status || 0);
