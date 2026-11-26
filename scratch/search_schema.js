const fs = require('fs');
const path = require('path');

function search(dir) {
    fs.readdirSync(dir).forEach(file => {
        const full = path.join(dir, file);
        if (fs.statSync(full).isDirectory()) search(full);
        else if (file.endsWith('.java') || file.endsWith('.sql')) {
            const content = fs.readFileSync(full, 'utf8');
            if (content.includes('CREATE TABLE') && content.includes('barangays')) {
                console.log(full);
            }
        }
    });
}
search('src');
