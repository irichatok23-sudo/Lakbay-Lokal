const fs = require('fs');

['src/main/java/com/lakbaylokal/dao/DestinationDAO.java',
 'src/main/java/com/lakbaylokal/dao/impl/DestinationDAOImpl.java',
 'src/main/java/com/lakbaylokal/service/DestinationService.java'
].forEach(f => {
    console.log('=== ' + f + ' ===');
    const lines = fs.readFileSync(f, 'utf8').split('\n');
    lines.forEach((l, i) => {
        if (l.includes('updateBarangayContact') || l.includes('Barangay') || l.includes('deleteBarangay')) {
            console.log((i+1) + ': ' + l.trim());
        }
    });
});
