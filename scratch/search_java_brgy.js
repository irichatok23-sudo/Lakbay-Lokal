const fs = require('fs');
const path = require('path');

function searchDir(dir) {
    fs.readdirSync(dir).forEach(file => {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            searchDir(fullPath);
        } else if (file.endsWith('.java')) {
            const content = fs.readFileSync(fullPath, 'utf8');
            const lines = content.split('\n');
            lines.forEach((line, idx) => {
                if (line.includes('/barangays') || line.includes('deleteBarangay') || line.includes('BarangayDAO')) {
                    console.log(`${fullPath}:${idx + 1}: ${line.trim()}`);
                }
            });
        }
    });
}

searchDir('src/main/java');
