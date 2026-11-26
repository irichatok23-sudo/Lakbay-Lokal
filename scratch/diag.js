const http = require('http');

function req(path, token) {
  return new Promise((resolve) => {
    http.get({ hostname: '127.0.0.1', port: 8080, path, headers: { 'X-Auth-Token': token } }, res => {
      let b = ''; res.on('data', c => b += c); res.on('end', () => {
        try { resolve(JSON.parse(b)); } catch (e) { resolve(b); }
      });
    });
  });
}

async function run() {
  const loginRes = await new Promise((resolve) => {
    const r = http.request({ hostname: '127.0.0.1', port: 8080, path: '/api/auth/login', method: 'POST', headers: { 'Content-Type': 'application/json' } }, res => {
      let b = ''; res.on('data', c => b += c); res.on('end', () => resolve(JSON.parse(b)));
    });
    r.write(JSON.stringify({ username: 'admin', password: 'password123' }));
    r.end();
  });
  const adminToken = loginRes.token;
  const users = await req('/api/admin/users', adminToken);
  console.log('--- ALL USERS WITH ROLE ---');
  users.forEach(u => console.log(u.username, 'role:', u.roleCode, 'muniId:', u.municipalityId, 'brgyId:', u.barangayId));

  const lguLogin = await new Promise((resolve) => {
    const r = http.request({ hostname: '127.0.0.1', port: 8080, path: '/api/auth/login', method: 'POST', headers: { 'Content-Type': 'application/json' } }, res => {
      let b = ''; res.on('data', c => b += c); res.on('end', () => resolve(JSON.parse(b)));
    });
    r.write(JSON.stringify({ username: 'lgu_naval', password: 'password123' }));
    r.end();
  });
  console.log('\n--- LGU NAVAL USER OBJECT ---', lguLogin.user);
  const lguUsers = await req('/api/admin/users', lguLogin.token);
  console.log('\n--- LGU NAVAL GET /api/admin/users ---', lguUsers);
}
run();
