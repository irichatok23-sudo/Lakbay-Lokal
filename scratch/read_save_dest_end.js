const fs = require('fs');
const content = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8');

const s = content.indexOf('destinationService.createDestination');
console.log(content.substring(s - 500, s + 500));
