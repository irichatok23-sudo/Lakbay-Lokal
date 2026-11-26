const http = require('http');

async function checkDb() {
  const loginRes = await new Promise((resolve) => {
    const r = http.request({ hostname: '127.0.0.1', port: 8080, path: '/api/auth/login', method: 'POST', headers: { 'Content-Type': 'application/json' } }, res => {
      let b = ''; res.on('data', c => b += c); res.on('end', () => resolve(JSON.parse(b)));
    });
    r.write(JSON.stringify({ username: 'admin', password: 'password123' }));
    r.end();
  });
  const token = loginRes.token;

  function req(path) {
    return new Promise((resolve) => {
      http.get({ hostname: '127.0.0.1', port: 8080, path, headers: { 'X-Auth-Token': token } }, res => {
        let b = ''; res.on('data', c => b += c); res.on('end', () => {
          try { resolve(JSON.parse(b)); } catch (e) { resolve(b); }
        });
      });
    });
  }

  const [users, dests, ann, pkgs, verifs, logs] = await Promise.all([
    req('/api/admin/users'),
    req('/api/destinations'),
    req('/api/announcements'),
    req('/api/packages'),
    req('/api/verifications'),
    req('/api/admin/logs?limit=50')
  ]);

  console.log('=== DATABASE INTEGRITY AUDIT ===');
  console.log('Total Users:', Array.isArray(users) ? users.length : 'ERR');
  console.log('Total Destinations:', Array.isArray(dests) ? dests.length : 'ERR');
  console.log('Total Announcements:', Array.isArray(ann) ? ann.length : 'ERR');
  console.log('Total Packages:', Array.isArray(pkgs) ? pkgs.length : 'ERR');
  console.log('Total Verification Records:', Array.isArray(verifs) ? verifs.length : 'ERR');
  console.log('Total Recent Audit Logs:', Array.isArray(logs) ? logs.length : 'ERR');
}
checkDb();
