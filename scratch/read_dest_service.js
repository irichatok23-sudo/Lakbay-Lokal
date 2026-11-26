const fs = require('fs');
const content = fs.readFileSync('src/main/java/com/lakbaylokal/service/DestinationService.java', 'utf8');
console.log(content);
