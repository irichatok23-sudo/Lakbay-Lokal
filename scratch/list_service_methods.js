const fs = require('fs');
const path = require('path');

const serviceDir = 'src/main/java/com/lakbaylokal/service';
const files = fs.readdirSync(serviceDir);

for (const file of files) {
    if (file.endsWith('.java')) {
        const content = fs.readFileSync(path.join(serviceDir, file), 'utf8');
        const methods = [];
        const regex = /public\s+([a-zA-Z0-9_<>,\[\]]+)\s+([a-zA-Z0-9_]+)\s*\(/g;
        let m;
        while ((m = regex.exec(content)) !== null) {
            if (!m[2].equals && m[2] !== 'class' && m[2] !== file.replace('.java', '')) {
                methods.push(m[2]);
            }
        }
        console.log(`${file}: ${methods.join(', ')}`);
    }
}
