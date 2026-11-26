const fs = require('fs');
const content = fs.readFileSync('src/main/java/com/lakbaylokal/database/ConnectionFactory.java', 'utf8');
console.log(content);
