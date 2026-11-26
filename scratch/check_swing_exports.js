const fs = require('fs');

function search(dir) {
    fs.readdirSync(dir, { withFileTypes: true }).forEach(e => {
        const p = dir + '/' + e.name;
        if (e.isDirectory()) search(p);
        else if (e.name.endsWith('.java')) {
            const c = fs.readFileSync(p, 'utf8');
            if (c.includes('ExportUtil') || c.includes('exportTableToCSV')) {
                console.log(`ExportUtil call in: ${p}`);
            }
        }
    });
}
search('src/main/java/lakbaylokal');
