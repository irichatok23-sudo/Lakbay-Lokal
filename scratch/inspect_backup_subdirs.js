const fs = require('fs');
const path = require('path');

function inspectDir(d) {
    if (!fs.existsSync(d)) return;
    console.log(`\n=== Directory: ${d} ===`);
    function walk(dir, level) {
        if (level > 3) return;
        fs.readdirSync(dir, { withFileTypes: true }).forEach(e => {
            const full = path.join(dir, e.name);
            if (e.isDirectory()) {
                console.log('  '.repeat(level) + `[DIR]  ${e.name}`);
                walk(full, level + 1);
            } else {
                console.log('  '.repeat(level) + `[FILE] ${e.name} (${fs.statSync(full).size} bytes)`);
            }
        });
    }
    walk(d, 1);
}

inspectDir('backups/stakeholder_fix_backup');
inspectDir('backups/role_visibility_fix');
inspectDir('backups/rbac_master_backup_20261120_094946');
