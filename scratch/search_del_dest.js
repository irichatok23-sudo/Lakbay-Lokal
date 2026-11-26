const fs = require('fs');

const content = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8').split('\n');
console.log('=== handleDeleteDestination ===');
content.forEach((line, idx) => {
    if (line.includes('handleDeleteDestination')) {
        console.log(`Java:${idx + 1}: ${line.trim()}`);
    }
});
