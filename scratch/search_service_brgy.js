const fs = require('fs');
const path = require('path');

function search(dir) {
    fs.readdirSync(dir).forEach(file => {
        const full = path.join(dir, file);
        if (fs.statSync(full).isDirectory()) search(full);
        else if (file.endsWith('.java')) {
            const content = fs.readFileSync(full, 'utf8');
            if (content.includes('updateBarangayContact') || content.includes('deleteBarangay')) {
                console.log(full);
            }
        }
    });
}
search('src/main/java');
