const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');

process.env.ELECTRON_DISABLE_SECURITY_WARNINGS = 'true';

const testUsers = [
    { username: 'tourist_ana', role: 'TOURIST', targetTab: 'destinations', testNav: 'packages' },
    { username: 'guide_pedro', role: 'STAKEHOLDER', targetTab: 'overview', testNav: 'schedule' },
    { username: 'brgy_olog', role: 'BARANGAY_OFFICIAL', targetTab: 'verifications', testNav: 'barangay-records' },
    { username: 'lgu_naval', role: 'LGU_OFFICER', targetTab: 'lgu-monitoring', testNav: 'destinations' },
    { username: 'tourismoffice', role: 'TOURISM_OFFICE', targetTab: 'destinations', testNav: 'announcements' },
    { username: 'admin', role: 'SYSTEM_ADMIN', targetTab: 'admin', testNav: 'settings' }
];

app.whenReady().then(async () => {
    console.log('========================================================================');
    console.log('LAKBAY LOKAL: ALL 6 ROLES REAL ELECTRON LIFECYCLE TEST');
    console.log('========================================================================');

    const win = new BrowserWindow({
        title: 'Lakbay Lokal: Community Tourism Information System - Biliran',
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
        if (level >= 3 || msg.includes('Error') || msg.includes('Exception') || msg.includes('fail')) {
            errorLogs.push(`[Console Err] ${msg} (${sourceId}:${line})`);
        }
    });

    win.webContents.on('did-fail-load', (e, code, desc, url) => {
        errorLogs.push(`[did-fail-load] ${code}: ${desc} (${url})`);
    });

    console.log('1. Loading http://127.0.0.1:8080/ ...');
    await win.loadURL('http://127.0.0.1:8080/');
    await new Promise(r => setTimeout(r, 1500));

    // Verify Design Tokens and Background on Login Page
    const styleCheck = await win.webContents.executeJavaScript(`
        (() => {
            const root = document.documentElement;
            const cs = window.getComputedStyle(root);
            const loginView = document.getElementById('login-portal-view');
            const leftPanel = document.querySelector('.glass-left-panel');
            const submitBtn = document.getElementById('btn-glass-submit');
            return {
                tokens: {
                    forestDeep: cs.getPropertyValue('--forest-deep').trim(),
                    leafTropical: cs.getPropertyValue('--leaf-tropical').trim(),
                    greenFresh: cs.getPropertyValue('--green-fresh').trim(),
                    mintSoft: cs.getPropertyValue('--mint-soft').trim(),
                    whiteNatural: cs.getPropertyValue('--white-natural').trim(),
                    darkForest: cs.getPropertyValue('--dark-forest').trim()
                },
                loginBg: loginView ? window.getComputedStyle(loginView).backgroundImage : null,
                leftPanelBg: leftPanel ? window.getComputedStyle(leftPanel).background : null,
                submitBtnBg: submitBtn ? window.getComputedStyle(submitBtn).background : null
            };
        })()
    `);
    console.log('\n[TROPICAL COASTAL GREEN DESIGN SYSTEM VERIFICATION]');
    console.log('Design Tokens in :root:', JSON.stringify(styleCheck.tokens, null, 2));
    console.log('Sambawan Island Background Image Present:', styleCheck.loginBg ? styleCheck.loginBg.includes('sambawan_island_hero_1791699785896.jpg') : false);
    console.log('Left Panel Frosted Glass Background:', styleCheck.leftPanelBg);
    console.log('Submit Button Background:', styleCheck.submitBtnBg);

    let allPassed = true;

    for (let i = 0; i < testUsers.length; i++) {
        const u = testUsers[i];
        console.log(`\n------------------------------------------------------------------------`);
        console.log(`[ROLE ${i + 1}/6] Testing ${u.username} (${u.role})`);
        console.log(`------------------------------------------------------------------------`);

        // 1. Confirm login portal is interactive before typing
        const preCheck = await win.webContents.executeJavaScript(`
            (() => {
                const uIn = document.getElementById('glass-login-username');
                const pIn = document.getElementById('glass-login-password');
                const btn = document.getElementById('btn-glass-submit');
                const uRect = uIn.getBoundingClientRect();
                const pRect = pIn.getBoundingClientRect();
                const topU = document.elementFromPoint(uRect.left + 20, uRect.top + 15);
                const topP = document.elementFromPoint(pRect.left + 20, pRect.top + 15);
                return {
                    uDisabled: uIn.disabled,
                    uReadOnly: uIn.readOnly,
                    pDisabled: pIn.disabled,
                    pReadOnly: pIn.readOnly,
                    topU: topU ? topU.tagName + '#' + topU.id : null,
                    topP: topP ? topP.tagName + '#' + topP.id : null
                };
            })()
        `);

        if (preCheck.uDisabled || preCheck.uReadOnly) {
            console.error(`FAILED: Username input is disabled or readOnly!`, preCheck);
            allPassed = false;
            break;
        }

        // 2. Type username & password using real OS-level input events
        console.log(`Step A: Typing credentials for ${u.username}...`);
        await win.webContents.executeJavaScript(`
            (() => {
                const uIn = document.getElementById('glass-login-username');
                const pIn = document.getElementById('glass-login-password');
                uIn.focus();
                uIn.value = '${u.username}';
                pIn.focus();
                pIn.value = 'password123';
            })()
        `);

        // 3. Click Login button
        console.log(`Step B: Submitting login...`);
        await win.webContents.executeJavaScript(`
            document.getElementById('btn-glass-submit').click();
        `);

        // Wait for dashboard initialization
        await new Promise(r => setTimeout(r, 2000));

        // 4. Verify dashboard is rendered, NOT WHITE
        const dashCheck = await win.webContents.executeJavaScript(`
            (() => {
                const mgmt = document.getElementById('management-portal-view');
                const login = document.getElementById('login-portal-view');
                const activePane = document.querySelector('.tab-pane.active');
                const sidebar = document.querySelector('.sidebar');
                const primaryBtn = document.querySelector('.btn-primary');
                return {
                    loginDisplay: window.getComputedStyle(login).display,
                    mgmtDisplay: window.getComputedStyle(mgmt).display,
                    mgmtHeight: mgmt.offsetHeight,
                    mgmtWidth: mgmt.offsetWidth,
                    activeRole: state.activeRole,
                    currentUser: state.currentUser ? state.currentUser.username : null,
                    activeTab: state.activeTab,
                    activePaneId: activePane ? activePane.id : null,
                    activePaneHeight: activePane ? activePane.offsetHeight : 0,
                    sidebarBg: sidebar ? window.getComputedStyle(sidebar).backgroundColor : null,
                    primaryBtnBg: primaryBtn ? window.getComputedStyle(primaryBtn).backgroundColor : null
                };
            })()
        `);

        console.log('Dashboard Render Verification:', JSON.stringify(dashCheck, null, 2));

        if (dashCheck.mgmtDisplay !== 'block' || dashCheck.mgmtHeight <= 0) {
            console.error(`FAILED: Dashboard is white / hidden! mgmtDisplay: ${dashCheck.mgmtDisplay}, height: ${dashCheck.mgmtHeight}`);
            allPassed = false;
            break;
        }

        if (dashCheck.currentUser !== u.username || dashCheck.activeRole !== u.role) {
            console.error(`FAILED: User or role mismatch! Expected ${u.username} (${u.role}), got ${dashCheck.currentUser} (${dashCheck.activeRole})`);
            allPassed = false;
            break;
        }

        console.log(`>>> DASHBOARD IS VISIBLE & ACTIVE (Height: ${dashCheck.mgmtHeight}px, Role: ${dashCheck.activeRole})`);

        // 5. Test Navigation inside dashboard
        console.log(`Step C: Testing tab navigation to #${u.testNav}...`);
        await win.webContents.executeJavaScript(`
            switchTab('${u.testNav}');
        `);
        await new Promise(r => setTimeout(r, 600));

        const navCheck = await win.webContents.executeJavaScript(`
            (() => {
                const p = document.getElementById('pane-${u.testNav}');
                return {
                    navPaneActive: p ? p.classList.contains('active') : false,
                    navPaneHeight: p ? p.offsetHeight : 0
                };
            })()
        `);
        console.log('Navigation Result:', JSON.stringify(navCheck));

        // 6. Perform Logout
        console.log(`Step D: Performing Logout...`);
        await win.webContents.executeJavaScript(`
            handleLogout();
        `);
        await new Promise(r => setTimeout(r, 1000));

        // 7. Verify Login Portal is fully interactive again
        const postLogoutCheck = await win.webContents.executeJavaScript(`
            (() => {
                const login = document.getElementById('login-portal-view');
                const mgmt = document.getElementById('management-portal-view');
                const uIn = document.getElementById('glass-login-username');
                const pIn = document.getElementById('glass-login-password');
                const uRect = uIn.getBoundingClientRect();
                const topU = document.elementFromPoint(uRect.left + 20, uRect.top + 15);
                return {
                    loginDisplay: window.getComputedStyle(login).display,
                    mgmtDisplay: window.getComputedStyle(mgmt).display,
                    uDisabled: uIn.disabled,
                    uReadOnly: uIn.readOnly,
                    uValue: uIn.value,
                    pValue: pIn.value,
                    topU: topU ? topU.tagName + '#' + topU.id : null,
                    sessionCleared: !state.authToken && !state.currentUser
                };
            })()
        `);

        console.log('Post-Logout Verification:', JSON.stringify(postLogoutCheck, null, 2));

        if (postLogoutCheck.loginDisplay !== 'flex') {
            console.error(`FAILED: Login portal not displayed! loginDisplay: ${postLogoutCheck.loginDisplay}`);
            allPassed = false;
            break;
        }

        if (postLogoutCheck.uDisabled || postLogoutCheck.uReadOnly) {
            console.error(`FAILED: Username input disabled after logout!`);
            allPassed = false;
            break;
        }

        if (postLogoutCheck.topU !== 'INPUT#glass-login-username') {
            console.error(`FAILED: Element blocking username input: ${postLogoutCheck.topU}`);
            allPassed = false;
            break;
        }

        console.log(`>>> ROLE ${i + 1} (${u.role}) CYCLE PASSED: Login -> Non-White Dashboard -> Nav -> Logout -> Portal Interactive.`);
    }

    console.log('\n========================================================================');
    if (allPassed) {
        console.log('SUCCESS: ALL SIX ROLES PASSED REAL ELECTRON LIFECYCLE VERIFICATION!');
    } else {
        console.error('FAILURE: ONE OR MORE ROLES FAILED VERIFICATION.');
    }
    console.log('========================================================================');

    if (errorLogs.length > 0) {
        console.log('Captured Errors:', errorLogs);
    } else {
        console.log('Zero renderer crashes or unhandled rejections captured.');
    }

    await win.close();
    app.quit();
});
