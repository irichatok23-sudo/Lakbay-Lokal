const fs = require('fs');
const binHtml = fs.readFileSync('bin/web/index.html', 'utf8');
const countInBin = (binHtml.match(/pane-barangay-records/g) || []).length;
console.log('bin/web/index.html matches:', countInBin, 'length:', binHtml.length);
