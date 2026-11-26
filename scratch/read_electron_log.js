const fs = require('fs');
if (fs.existsSync('lakbay_lokal_electron.log')) {
    console.log(fs.readFileSync('lakbay_lokal_electron.log', 'utf8'));
} else {
    console.log('No electron log found');
}
