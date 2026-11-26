const fs = require('fs');
const content = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8');

const s = content.indexOf('handleSaveDestination(');
console.log(content.substring(s, s + 1500));
