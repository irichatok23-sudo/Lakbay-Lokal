const fs = require('fs');
const content = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8');
const lines = content.split('\n');
console.log('Total lines:', lines.length);
console.log(lines.slice(0, 50).join('\n'));
