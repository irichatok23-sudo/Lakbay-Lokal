const fs = require('fs');
const annoService = fs.readFileSync('src/main/java/com/lakbaylokal/service/AnnouncementService.java', 'utf8');
console.log('has getAnnouncements:', annoService.includes('getAnnouncements('));
console.log('has getFilteredAnnouncements:', annoService.includes('getFilteredAnnouncements('));
