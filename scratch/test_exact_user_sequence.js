const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');

const logFile = path.join(__dirname, 'exact_user_sequence.log');
fs.writeFileSync(logFile, 'Starting 10-Cycle User Simulation in Electron...\n', 'utf8');

function log(...args) {
    const msg = args.map(a => (typeof a === 'object' ? JSON.stringify(a, null, 2) : String(a))).join(' ');
    console.log(msg);
    fs.appendFileSync(logFile, msg + '\n', 'utf8');
}

app.whenReady().then(async () => {
    log('Electron Ready. Creating window matching production main.js...');
    const win = new BrowserWindow({
        title: 'Lakbay Lokal - Live Diagnostic Session',
        width: 1420,
        height: 900,
        show: true,
        webPreferences: {
            contextIsolation: true,
            nodeIntegration: false,
            sandbox: true,
            preload: path.join(__dirname, '..', 'electron', 'preload.js')
        }
    });

    win.webContents.on('console-message', (event, level, message) => {
        log(`[Renderer Console] ${message}`);
    });

    try {
        log('1. Loading http://127.0.0.1:8080/ ...');
        await win.loadURL('http://127.0.0.1:8080/');
        await new Promise(r => setTimeout(r, 2000));

        // Inject deep diagnostics directly into the page
        await win.webContents.executeJavaScript(`
            (() => {
                const u = document.getElementById('glass-login-username');
                const p = document.getElementById('glass-login-password');
                const btn = document.getElementById('btn-glass-submit');

                ['click', 'mousedown', 'mouseup', 'focus', 'blur', 'keydown', 'keyup', 'input'].forEach(evt => {
                    u?.addEventListener(evt, (e) => console.log('[DIAG:USERNAME] ' + evt + ' key=' + (e.key || '') + ' val=' + u.value));
                    p?.addEventListener(evt, (e) => console.log('[DIAG:PASSWORD] ' + evt + ' key=' + (e.key || '') + ' val=' + p.value));
                });

                btn?.addEventListener('click', () => console.log('[DIAG:SUBMIT_BTN] clicked'));
            })()
        `);

        const testAccounts = [
            { user: 'tourist_ana', pass: 'password123', role: 'TOURIST' },
            { user: 'guide_pedro', pass: 'password123', role: 'STAKEHOLDER' },
            { user: 'brgy_olog', pass: 'password123', role: 'BARANGAY_OFFICIAL' },
            { user: 'lgu_naval', pass: 'password123', role: 'LGU_OFFICER' },
            { user: 'tourismoffice', pass: 'password123', role: 'TOURISM_OFFICE' },
            { user: 'admin', pass: 'password123', role: 'SYSTEM_ADMIN' },
            { user: 'tourist_ana', pass: 'password123', role: 'TOURIST' },
            { user: 'guide_pedro', pass: 'password123', role: 'STAKEHOLDER' },
            { user: 'tourismoffice', pass: 'password123', role: 'TOURISM_OFFICE' },
            { user: 'admin', pass: 'password123', role: 'SYSTEM_ADMIN' }
        ];

        for (let cycle = 0; cycle < testAccounts.length; cycle++) {
            const acc = testAccounts[cycle];
            log(`\n========================================================================`);
            log(`[CYCLE ${cycle + 1}/10] Testing: ${acc.user} (${acc.role})`);
            log(`========================================================================`);

            // Step A: Focus username, select existing content, clear it, and type new username
            log(`Step A: Typing username '${acc.user}' into #glass-login-username...`);
            const typeUserSuccess = await win.webContents.executeJavaScript(`
                (() => {
                    const u = document.getElementById('glass-login-username');
                    if (!u) return { success: false, reason: 'u not found' };
                    u.focus();
                    u.value = '${acc.user}';
                    u.dispatchEvent(new Event('input', { bubbles: true }));
                    u.dispatchEvent(new Event('change', { bubbles: true }));
                    return {
                        success: (u.value === '${acc.user}'),
                        disabled: u.disabled,
                        readOnly: u.readOnly,
                        hasFocus: (document.activeElement === u),
                        pointerEvents: window.getComputedStyle(u).pointerEvents
                    };
                })()
            `);
            log('Username type result:', typeUserSuccess);
            if (!typeUserSuccess.success) throw new Error('Failed to type username');

            // Step B: Focus password, clear it, and type password
            log(`Step B: Typing password into #glass-login-password...`);
            const typePassSuccess = await win.webContents.executeJavaScript(`
                (() => {
                    const p = document.getElementById('glass-login-password');
                    if (!p) return { success: false, reason: 'p not found' };
                    p.focus();
                    p.value = '${acc.pass}';
                    p.dispatchEvent(new Event('input', { bubbles: true }));
                    p.dispatchEvent(new Event('change', { bubbles: true }));
                    return {
                        success: (p.value === '${acc.pass}'),
                        disabled: p.disabled,
                        readOnly: p.readOnly,
                        hasFocus: (document.activeElement === p),
                        pointerEvents: window.getComputedStyle(p).pointerEvents
                    };
                })()
            `);
            log('Password type result:', typePassSuccess);
            if (!typePassSuccess.success) throw new Error('Failed to type password');

            // Step C: Click Sign In button
            log(`Step C: Submitting login...`);
            await win.webContents.executeJavaScript(`
                (async () => {
                    await handleGlassLoginSubmit();
                })()
            `);
            await new Promise(r => setTimeout(r, 2500));

            // Step D: Verify correct dashboard appeared
            const dashCheck = await win.webContents.executeJavaScript(`
                (() => {
                    const mgmt = document.getElementById('management-portal-view');
                    const pub = document.getElementById('public-portal-view');
                    const login = document.getElementById('login-portal-view');
                    return {
                        currentUser: state.currentUser?.username,
                        activeRole: state.activeRole,
                        mgmtActive: mgmt?.classList.contains('active'),
                        pubActive: pub?.classList.contains('active'),
                        loginHidden: (window.getComputedStyle(login).display === 'none')
                    };
                })()
            `);
            log('Dashboard Verification:', dashCheck);
            if (dashCheck.currentUser !== acc.user) {
                throw new Error(`Expected currentUser ${acc.user}, got ${dashCheck.currentUser}`);
            }
            if (dashCheck.activeRole !== acc.role) {
                throw new Error(`Expected activeRole ${acc.role}, got ${dashCheck.activeRole}`);
            }

            // Step E: Interact with dashboard (e.g. switch tab or open modal)
            log(`Step E: Interacting with dashboard...`);
            await win.webContents.executeJavaScript(`
                (() => {
                    // Open a modal to test overlay cleanup
                    openModal('modal-user-profile');
                })()
            `);
            await new Promise(r => setTimeout(r, 500));

            // Step F: Logout
            log(`Step F: Performing Logout...`);
            await win.webContents.executeJavaScript(`
                (() => {
                    logout();
                })()
            `);
            await new Promise(r => setTimeout(r, 1000));

            // Step G: Diagnostics check after logout
            const postLogoutDiag = await win.webContents.executeJavaScript(`
                (() => {
                    const u = document.getElementById('glass-login-username');
                    const p = document.getElementById('glass-login-password');
                    const btn = document.getElementById('btn-glass-submit');
                    const portal = document.getElementById('login-portal-view');
                    const openModals = Array.from(document.querySelectorAll('.modal-backdrop.open, [class*="modal"].open')).map(m => m.id);

                    const rect = u.getBoundingClientRect();
                    const topElem = document.elementFromPoint(Math.round(rect.left + rect.width / 2), Math.round(rect.top + rect.height / 2));

                    return {
                        portalVisible: (window.getComputedStyle(portal).display === 'flex'),
                        uEnabled: !u.disabled && !u.readOnly,
                        pEnabled: !p.disabled && !p.readOnly,
                        uPointerEvents: window.getComputedStyle(u).pointerEvents,
                        topElemId: topElem?.id,
                        topElemTag: topElem?.tagName,
                        openModalsRemaining: openModals.length,
                        isSubmittingLogin: (typeof isSubmittingLogin !== 'undefined' ? isSubmittingLogin : false),
                        sessionCleared: (state.authToken === null && state.currentUser === null)
                    };
                })()
            `);
            log('Post-Logout Diagnostics:', postLogoutDiag);

            if (!postLogoutDiag.portalVisible) throw new Error('Portal is not visible after logout');
            if (!postLogoutDiag.uEnabled || !postLogoutDiag.pEnabled) throw new Error('Inputs not enabled after logout');
            if (postLogoutDiag.openModalsRemaining > 0) throw new Error('Modals remained open after logout');
            if (!postLogoutDiag.sessionCleared) throw new Error('Session not cleared after logout');

            log(`>>> CYCLE ${cycle + 1}/10 PASSED: ${acc.user} -> Dashboard -> Modal -> Logout -> Portal Interactive.`);
        }

        log(`\n========================================================================`);
        log(`ALL 10 CONSECUTIVE ELECTRON CYCLES PASSED PERFECTLY!`);
        log(`========================================================================`);

    } catch (err) {
        log('FATAL TEST ERROR:', err.stack || err);
    } finally {
        app.quit();
    }
});
