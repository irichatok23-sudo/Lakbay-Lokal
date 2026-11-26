const fs = require('fs');

const indexHtmlPath = 'src/main/resources/web/index.html';
const appJsPath = 'src/main/resources/web/js/app.js';

let indexHtml = fs.readFileSync(indexHtmlPath, 'utf8');
let appJs = fs.readFileSync(appJsPath, 'utf8');

// Replacements in index.html:
// Nav:
// <span>Stakeholders Directory</span> -> <span>Community Worker Directory</span>
// <span>Stakeholder Verification</span> -> <span>Community Worker Accreditation</span>
// Reference accounts & tags:
// <span class="ref-role-tag lgu">LGU Officer</span> -> <span class="ref-role-tag lgu">Municipal Tourism Officer</span>
// Modal tabs & headings:
// <h2 class="page-title">Community Tourism Stakeholders</h2> -> <h2 class="page-title">Community Worker Directory</h2>
// <h3 class="card-title"><i class="fa-solid fa-stamp"></i> Stakeholder Accreditation Queue</h3> -> <h3 class="card-title"><i class="fa-solid fa-stamp"></i> Community Worker Accreditation Queue</h3>
// <h2 class="page-title">Barangay Tourism Verification</h2> -> <h2 class="page-title">Community Worker Accreditation</h2>
// Buttons:
// <i class="fa-solid fa-id-badge"></i> My Stakeholder Profile -> <i class="fa-solid fa-id-badge"></i> My Community Worker Profile
// <i class="fa-solid fa-user-check"></i> Register Stakeholder -> <i class="fa-solid fa-user-check"></i> Register Community Worker
// Registration modal:
// <i class="fa-solid fa-handshake-angle"></i> Local Stakeholder Registration -> <i class="fa-solid fa-handshake-angle"></i> Community Worker Registration
// Role options in selects:
// <option value="5">Local Stakeholder</option> -> <option value="5">Community Worker</option>
// <option value="3">LGU Officer</option> -> <option value="3">Municipal Tourism Officer</option>

let updatedHtml = indexHtml
    .replace('<span>Stakeholders Directory</span>', '<span>Community Worker Directory</span>')
    .replace('<span>Stakeholder Verification</span>', '<span>Community Worker Accreditation</span>')
    .replace('<h2 class="page-title">Community Tourism Stakeholders</h2>', '<h2 class="page-title">Community Worker Directory</h2>')
    .replace('<h2 class="page-title">Barangay Tourism Verification</h2>', '<h2 class="page-title">Community Worker Accreditation</h2>')
    .replace('<h3 class="card-title"><i class="fa-solid fa-stamp"></i> Stakeholder Accreditation Queue</h3>', '<h3 class="card-title"><i class="fa-solid fa-stamp"></i> Community Worker Accreditation Queue</h3>')
    .replace('<i class="fa-solid fa-id-badge"></i> My Stakeholder Profile', '<i class="fa-solid fa-id-badge"></i> My Community Worker Profile')
    .replace('<i class="fa-solid fa-user-check"></i> Register Stakeholder', '<i class="fa-solid fa-user-check"></i> Register Community Worker')
    .replace('<i class="fa-solid fa-handshake-angle"></i> Local Stakeholder Registration', '<i class="fa-solid fa-handshake-angle"></i> Community Worker Registration')
    .replaceAll('<span class="ref-role-tag lgu">LGU Officer</span>', '<span class="ref-role-tag lgu">Municipal Tourism Officer</span>')
    .replaceAll('<option value="5">Local Stakeholder</option>', '<option value="5">Community Worker</option>')
    .replaceAll('<option value="3">LGU Officer</option>', '<option value="3">Municipal Tourism Officer</option>');

fs.writeFileSync(indexHtmlPath, updatedHtml, 'utf8');

// In app.js:
// Format role display names and banner text:
let updatedAppJs = appJs
    .replace("if (bTag) bTag.innerHTML = '<i class=\"fa-solid fa-handshake-angle\"></i> Local Tourism Stakeholder Desk';", "if (bTag) bTag.innerHTML = '<i class=\"fa-solid fa-handshake-angle\"></i> Community Worker Desk';")
    .replace("if (bTag) bTag.innerHTML = '<i class=\"fa-solid fa-building-columns\"></i> Municipal Tourism Monitoring Office';", "if (bTag) bTag.innerHTML = '<i class=\"fa-solid fa-building-columns\"></i> Municipal Tourism Office Desk';")
    .replace("bScope.textContent = state.currentUser.municipalityName ? `${state.currentUser.municipalityName} Stakeholder` : 'Community Provider';", "bScope.textContent = state.currentUser.municipalityName ? `${state.currentUser.municipalityName} Community Worker` : 'Community Worker';")
    .replace(
        "if (hRoleLabel) hRoleLabel.textContent = user.roleName || formatCategory(user.roleCode);",
        "const roleDisplayMap = { 'STAKEHOLDER': 'Community Worker', 'LGU_OFFICER': 'Municipal Tourism Officer', 'TOURIST': 'Tourist', 'BARANGAY_OFFICIAL': 'Barangay Official', 'TOURISM_OFFICE': 'Tourism Office', 'SYSTEM_ADMIN': 'System Administrator' };\n    if (hRoleLabel) hRoleLabel.textContent = roleDisplayMap[user.roleCode] || user.roleName || formatCategory(user.roleCode);\n    if (hBadge && roleDisplayMap[user.roleCode]) hBadge.textContent = roleDisplayMap[user.roleCode];"
    );

fs.writeFileSync(appJsPath, updatedAppJs, 'utf8');

console.log('Replacements completed successfully.');
