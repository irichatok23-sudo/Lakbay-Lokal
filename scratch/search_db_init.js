const fs = require('fs');
const path = require('path');

function search(dir) {
    fs.readdirSync(dir).forEach(file => {
        const full = path.join(dir, file);
        if (fs.statSync(full).isDirectory()) search(full);
        else if (file.toLowerCase().includes('database') || file.toLowerCase().includes('schema') || file.toLowerCase().includes('init')) {
            console.log(full);
        }
    });
}
search('src');
