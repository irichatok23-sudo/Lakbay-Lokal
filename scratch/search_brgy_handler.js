const fs = require('fs');

const content = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8').split('\n');
console.log('=== handleUpdateBarangayRecord ===');
content.forEach((line, idx) => {
    if (line.includes('handleUpdateBarangayRecord') || line.includes('barangayService') || line.includes('barangayDao')) {
        console.log(`Java:${idx + 1}: ${line.trim()}`);
    }
});
