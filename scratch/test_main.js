const { spawn } = require('child_process');

console.log('Spawning java process...');
const child = spawn('java', ['-cp', 'bin;lib/*', 'com.lakbaylokal.Main', '--headless'], {
    stdio: ['ignore', 'pipe', 'pipe']
});

child.stdout.on('data', data => console.log('JAVA STDOUT:', data.toString()));
child.stderr.on('data', data => console.error('JAVA STDERR:', data.toString()));
child.on('close', code => console.log('JAVA EXITED with code:', code));

setTimeout(() => {
    console.log('Java process status check after 3s, pid:', child.pid);
}, 3000);
