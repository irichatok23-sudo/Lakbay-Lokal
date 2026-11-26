const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

function getModifiedFiles() {
    try {
        const out = execSync('git status -s', { encoding: 'utf8' });
        return out.split('\n').filter(Boolean);
    } catch (e) {
        return e.message;
    }
}

console.log('Modified files in git status:');
console.log(getModifiedFiles());
