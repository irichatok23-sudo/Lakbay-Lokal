const fs = require('fs');
const path = require('path');

function compareTree(dirA, dirB) {
    if (!fs.existsSync(dirA)) return [];
    let diffs = [];
    function scan(d) {
        fs.readdirSync(d, { withFileTypes: true }).forEach(e => {
            const pathA = path.join(d, e.name);
            const rel = path.relative(dirA, pathA);
            const pathB = path.join(dirB, rel);
            if (e.isDirectory()) {
                scan(pathA);
            } else {
                if (!fs.existsSync(pathB)) {
                    diffs.push(`MISSING in B: ${rel}`);
                } else {
                    const sizeA = fs.statSync(pathA).size;
                    const sizeB = fs.statSync(pathB).size;
                    if (sizeA !== sizeB) {
                        diffs.push(`DIFFERENT SIZE: ${rel} (A: ${sizeA}, B: ${sizeB})`);
                    }
                }
            }
        });
    }
    scan(dirA);
    return diffs;
}

console.log('=== Comparing backups/stakeholder_fix_backup/src with src ===');
const diffs = compareTree('backups/stakeholder_fix_backup/src', 'src');
diffs.slice(0, 30).forEach(d => console.log(d));
console.log(`Total differences: ${diffs.length}`);
