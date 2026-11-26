const http = require('http');

http.get('http://127.0.0.1:8080/api/destinations', res => {
    let d = '';
    res.on('data', c => d += c);
    res.on('end', () => {
        const list = JSON.parse(d);
        console.log(JSON.stringify(list.map(x => ({
            destinationId: x.destinationId,
            name: x.name,
            barangayId: x.barangayId,
            barangayName: x.barangayName,
            municipalityName: x.municipalityName,
            category: x.category,
            imageUrl: x.imageUrl
        })), null, 2));
    });
}).on('error', err => console.error(err));
