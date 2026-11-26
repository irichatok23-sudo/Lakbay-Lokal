const http = require('http');
async function req(path, method='GET', body=null, token=null) {
    return new Promise((resolve) => {
        const u = new URL('http://127.0.0.1:8080/api' + path);
        const opt = { hostname: u.hostname, port: u.port, path: u.pathname + u.search, method, headers: {'Content-Type': 'application/json'} };
        if (token) opt.headers['X-Auth-Token'] = token;
        const r = http.request(opt, res => {
            let d = ''; res.on('data', c => d += c);
            res.on('end', () => resolve({ status: res.statusCode, body: d }));
        });
        if (body) r.write(JSON.stringify(body));
        r.end();
    });
}
(async () => {
    const lAdmin = await req('/auth/login', 'POST', {username: 'admin', password: 'password123'});
    const tAdmin = JSON.parse(lAdmin.body).token;
    const h = await req('/admin/system/health', 'GET', null, tAdmin);
    console.log('Health status:', h.status, 'body:', h.body);

    const lPedro = await req('/auth/login', 'POST', {username: 'guide_pedro', password: 'password123'});
    const tPedro = JSON.parse(lPedro.body).token;
    const cross = await req('/bookings/item-status', 'POST', {itemId: 15, status: 'ACCEPTED'}, tPedro);
    console.log('Cross item status:', cross.status, 'body:', cross.body);

    const lTourist = await req('/auth/login', 'POST', {username: 'tourist_ana', password: 'password123'});
    const tTourist = JSON.parse(lTourist.body).token;
    const sDash = await req('/stakeholder/dashboard', 'GET', null, tTourist);
    console.log('Tourist stakeholder dash status:', sDash.status, 'body:', sDash.body);

    const togOther = await req('/stakeholder/service-availability', 'POST', {serviceId: 3, isAvailable: false}, tPedro);
    console.log('Toggle other service status:', togOther.status, 'body:', togOther.body);
})();
