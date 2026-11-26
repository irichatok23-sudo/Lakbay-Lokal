const fs = require('fs');

const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8').split('\n');
console.log('=== HTML: Edit Barangay Modal ===');
html.forEach((line, idx) => {
    if (line.includes('modal-edit-barangay') || line.includes('Update Barangay') || line.includes('Save Barangay')) {
        console.log(`HTML:${idx + 1}: ${line.trim()}`);
    }
});

const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');
console.log('\n=== JS: Edit Barangay Functions ===');
js.forEach((line, idx) => {
    if (line.includes('openEditBarangayModal') || line.includes('edit-barangay') || line.includes('handleEditBarangay') || line.includes('saveBarangay')) {
        console.log(`JS:${idx + 1}: ${line.trim()}`);
    }
});
