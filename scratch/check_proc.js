const cp = require('child_process');
try {
    const out = cp.execSync('tasklist /FI "IMAGENAME eq java.exe"').toString();
    console.log(out);
} catch (e) {
    console.error(e.message);
}
