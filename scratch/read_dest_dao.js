const fs = require('fs');
const content = fs.readFileSync('src/main/java/com/lakbaylokal/dao/impl/DestinationDAOImpl.java', 'utf8');
console.log(content);
