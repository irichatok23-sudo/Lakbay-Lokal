const fs = require('fs');
const content = fs.readFileSync('src/main/java/com/lakbaylokal/config/AppConfig.java', 'utf8');
console.log(content);
