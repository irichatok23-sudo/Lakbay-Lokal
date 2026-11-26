const { app, BrowserWindow } = require('electron');
const path = require('path');

process.env.ELECTRON_DISABLE_SECURITY_WARNINGS = 'true';

app.whenReady().then(async () => {
    console.log('========================================================================');
    console.log('LAKBAY LOKAL: DASHBOARD BANNER VISIBILITY ACCEPTANCE TEST');
    console.log('========================================================================');

    const win = new BrowserWindow({
        title: 'Lakbay Lokal: Banner Visibility Acceptance Test',
        width: 1420,
        height: 900,
        show: false,
        backgroundColor: '#032219',
        webPreferences: {
            nodeIntegration: false,
            contextIsolation: true,
            preload: path.join(__dirname, '..', 'electron', 'preload.js')
        }
    });

    const errorLogs = [];
    win.webContents.on('console-message', (e, level, msg, line, sourceId) => {
        if (level >= 3 || msg.includes('Error') || msg.includes('Exception')) {
            errorLogs.push(`[Console Err] ${msg} (${sourceId}:${line})`);
        }
    });

    console.log('Loading http://127.0.0.1:8080/ ...');
    await win.loadURL('http://127.0.0.1:8080/');
    await new Promise(r => setTimeout(r, 1500));

    // Helper to inspect banner state
    async function getBannerState() {
        return await win.webContents.executeJavaScript(`
            (() => {
                const b = document.getElementById('role-workspace-banner');
                if (!b) return { exists: false };
                const cs = window.getComputedStyle(b);
                const rect = b.getBoundingClientRect();
                return {
                    exists: true,
                    display: cs.display,
                    visibility: cs.visibility,
                    offsetHeight: b.offsetHeight,
                    offsetWidth: b.offsetWidth,
                    rectHeight: rect.height,
                    hasHiddenClass: b.classList.contains('banner-hidden'),
                    activeTab: state.activeTab,
                    activeRole: state.activeRole
                };
            })()
        `);
    }

    async function loginAs(username, password = 'password123') {
        await win.webContents.executeJavaScript(`
            (() => {
                const uIn = document.getElementById('glass-login-username');
                const pIn = document.getElementById('glass-login-password');
                uIn.value = '${username}';
                pIn.value = '${password}';
                document.getElementById('btn-glass-submit').click();
            })()
        `);
        await new Promise(r => setTimeout(r, 2000));
    }

    async function navigateTo(tabId) {
        await win.webContents.executeJavaScript(`
            switchTab('${tabId}');
        `);
        await new Promise(r => setTimeout(r, 500));
    }

    async function logout() {
        await win.webContents.executeJavaScript(`
            handleLogout();
        `);
        await new Promise(r => setTimeout(r, 1000));
    }

    let allPassed = true;

    // ------------------------------------------------------------------------
    // SECTION A: Policy Verification across ALL 6 User Roles for Overview
    // ------------------------------------------------------------------------
    console.log('\n--- Section A: Verifying Overview Policy for ALL 6 Roles ---');
    const policyCheck = await win.webContents.executeJavaScript(`
        (() => {
            const roles = ['TOURIST', 'STAKEHOLDER', 'BARANGAY_OFFICIAL', 'LGU_OFFICER', 'TOURISM_OFFICE', 'SYSTEM_ADMIN'];
            const results = {};
            const banner = document.getElementById('role-workspace-banner');
            roles.forEach(role => {
                state.activeRole = role;
                updateWorkspaceBannerVisibility('overview');
                results[role] = {
                    display: window.getComputedStyle(banner).display,
                    hasHiddenClass: banner.classList.contains('banner-hidden')
                };
            });
            return results;
        })()
    `);
    console.log('Overview policy results:', policyCheck);
    for (const [role, res] of Object.entries(policyCheck)) {
        if (res.display !== 'flex' || res.hasHiddenClass) {
            console.error(`FAIL: Overview policy failed for ${role}`);
            allPassed = false;
        } else {
            console.log(`PASS: Banner is VISIBLE on Overview for ${role}`);
        }
    }

    // ------------------------------------------------------------------------
    // SECTION B: Multi-Role End-to-End Navigation Acceptance
    // ------------------------------------------------------------------------

    // 1. TOURIST (tourist_ana)
    console.log('\n--- 1. Testing TOURIST (tourist_ana) ---');
    await loginAs('tourist_ana');

    let bState = await getBannerState();
    console.log('Initial page (destinations):', bState);
    if (bState.display !== 'flex' || bState.offsetHeight <= 0) {
        console.error('FAIL: Banner should be VISIBLE on Tourist Destinations!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is VISIBLE on Tourist Destinations.');
    }

    // Navigate to packages
    await navigateTo('packages');
    bState = await getBannerState();
    console.log('Navigated to packages:', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Tourist Packages!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN (0 height, no space left) on Packages.');
    }

    // Navigate to bookings
    await navigateTo('bookings');
    bState = await getBannerState();
    console.log('Navigated to bookings:', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Tourist Bookings!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Bookings.');
    }

    // Navigate to services
    await navigateTo('services');
    bState = await getBannerState();
    console.log('Navigated to services:', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Tourist Services!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Services.');
    }

    // Navigate back to destinations
    await navigateTo('destinations');
    bState = await getBannerState();
    console.log('Navigated back to destinations:', bState);
    if (bState.display !== 'flex' || bState.offsetHeight <= 0) {
        console.error('FAIL: Banner should be VISIBLE again on Tourist Destinations!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is dynamically RESTORED on Tourist Destinations.');
    }

    await logout();

    // 2. STAKEHOLDER (guide_pedro)
    console.log('\n--- 2. Testing STAKEHOLDER (guide_pedro) ---');
    await loginAs('guide_pedro');

    bState = await getBannerState();
    console.log('Initial page (overview):', bState);
    if (bState.display !== 'flex' || bState.offsetHeight <= 0) {
        console.error('FAIL: Banner should be VISIBLE on Overview for Stakeholder!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is VISIBLE on Overview for Stakeholder.');
    }

    // Navigate to schedule
    await navigateTo('schedule');
    bState = await getBannerState();
    console.log('Navigated to schedule:', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Stakeholder Schedule!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Schedule.');
    }

    // Navigate to destinations (non-tourist destinations)
    await navigateTo('destinations');
    bState = await getBannerState();
    console.log('Navigated to destinations (stakeholder):', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Destinations for non-tourist!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Destinations for Stakeholder.');
    }

    // Navigate to bookings
    await navigateTo('bookings');
    bState = await getBannerState();
    console.log('Navigated to bookings:', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Stakeholder Bookings!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Bookings.');
    }

    // Navigate back to overview
    await navigateTo('overview');
    bState = await getBannerState();
    console.log('Navigated back to overview:', bState);
    if (bState.display !== 'flex' || bState.offsetHeight <= 0) {
        console.error('FAIL: Banner should be VISIBLE again on Overview!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is dynamically RESTORED on Overview.');
    }

    await logout();

    // 3. BARANGAY OFFICIAL (brgy_olog)
    console.log('\n--- 3. Testing BARANGAY OFFICIAL (brgy_olog) ---');
    await loginAs('brgy_olog');

    bState = await getBannerState();
    console.log('Initial page (verifications):', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Verifications for Barangay Official!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Verifications (0 height, no space).');
    }

    await navigateTo('barangay-records');
    bState = await getBannerState();
    console.log('Navigated to barangay-records:', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Barangay Records!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Barangay Records.');
    }

    await navigateTo('destinations');
    bState = await getBannerState();
    console.log('Navigated to destinations:', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Destinations for Barangay Official!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Destinations.');
    }

    await navigateTo('reports');
    bState = await getBannerState();
    console.log('Navigated to reports:', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Reports!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Reports.');
    }

    await logout();

    // 4. LGU OFFICER (lgu_naval)
    console.log('\n--- 4. Testing LGU OFFICER (lgu_naval) ---');
    await loginAs('lgu_naval');

    bState = await getBannerState();
    console.log('Initial page (lgu-monitoring):', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on LGU Monitoring!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on LGU Monitoring.');
    }

    await navigateTo('destinations');
    bState = await getBannerState();
    console.log('Navigated to destinations (LGU Officer):', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Destinations for LGU Officer!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Destinations for LGU Officer.');
    }

    await navigateTo('stakeholders');
    bState = await getBannerState();
    console.log('Navigated to stakeholders (LGU Officer):', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Stakeholders for LGU Officer!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Stakeholders for LGU Officer.');
    }

    await navigateTo('bookings');
    bState = await getBannerState();
    console.log('Navigated to bookings (LGU Officer):', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Bookings for LGU Officer!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Bookings for LGU Officer.');
    }

    await logout();

    // 5. TOURISM OFFICE (tourismoffice)
    console.log('\n--- 5. Testing TOURISM OFFICE (tourismoffice) ---');
    await loginAs('tourismoffice');

    bState = await getBannerState();
    console.log('Initial page (destinations - provincial management):', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Destinations for Tourism Office!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Destination Management for Tourism Office.');
    }

    await navigateTo('overview');
    bState = await getBannerState();
    console.log('Navigated to overview:', bState);
    if (bState.display !== 'flex' || bState.offsetHeight <= 0) {
        console.error('FAIL: Banner should be VISIBLE on Overview for Tourism Office!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is VISIBLE on Overview for Tourism Office.');
    }

    await navigateTo('packages');
    bState = await getBannerState();
    console.log('Navigated to packages:', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Packages for Tourism Office!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Packages for Tourism Office.');
    }

    await navigateTo('announcements');
    bState = await getBannerState();
    console.log('Navigated to announcements:', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Announcements for Tourism Office!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Announcements for Tourism Office.');
    }

    await navigateTo('overview');
    bState = await getBannerState();
    console.log('Returned to overview:', bState);
    if (bState.display !== 'flex' || bState.offsetHeight <= 0) {
        console.error('FAIL: Banner should be VISIBLE again on Overview!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is RESTORED on Overview.');
    }

    await logout();

    // 6. SYSTEM ADMINISTRATOR (admin)
    console.log('\n--- 6. Testing SYSTEM ADMINISTRATOR (admin) ---');
    await loginAs('admin');

    bState = await getBannerState();
    console.log('Initial page (admin - user management):', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on User Management for Admin!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on User Management.');
    }

    await navigateTo('overview');
    bState = await getBannerState();
    console.log('Navigated to overview:', bState);
    if (bState.display !== 'flex' || bState.offsetHeight <= 0) {
        console.error('FAIL: Banner should be VISIBLE on Overview for Admin!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is VISIBLE on Overview for Admin.');
    }

    await navigateTo('settings');
    bState = await getBannerState();
    console.log('Navigated to settings:', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Settings!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Settings.');
    }

    await navigateTo('audit-logs');
    bState = await getBannerState();
    console.log('Navigated to audit-logs:', bState);
    if (bState.display !== 'none' || bState.offsetHeight !== 0) {
        console.error('FAIL: Banner should be HIDDEN on Audit Logs!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is HIDDEN on Audit Logs.');
    }

    await navigateTo('overview');
    bState = await getBannerState();
    console.log('Returned to overview:', bState);
    if (bState.display !== 'flex' || bState.offsetHeight <= 0) {
        console.error('FAIL: Banner should be VISIBLE again on Overview!');
        allPassed = false;
    } else {
        console.log('PASS: Banner is RESTORED on Overview.');
    }

    await logout();

    console.log('\n========================================================================');
    if (allPassed) {
        console.log('SUCCESS: ALL BANNER VISIBILITY ACCEPTANCE TESTS PASSED!');
    } else {
        console.error('FAILURE: ONE OR MORE BANNER VISIBILITY TESTS FAILED.');
    }
    console.log('========================================================================');

    await win.close();
    app.quit();
    process.exit(allPassed ? 0 : 1);
});
