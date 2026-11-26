const http = require('http');

http.get('http://127.0.0.1:8080/api/barangays', (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
        const brgy = JSON.parse(data);
        console.log('Sample barangay:', brgy[0]);
        console.log('Total barangays:', brgy.length);
        const munis = [...new Set(brgy.map(b => b.municipalityName))];
        console.log('Municipalities in barangays:', munis);
    });
}).on('error', err => {
    console.error('Error fetching barangays:', err.message);
});
