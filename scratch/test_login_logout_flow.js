const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');

const logFile = path.join(__dirname, 'test_output.log');
fs.writeFileSync(logFile, 'Starting test...\n', 'utf8');

function log(...args) {
    const msg = args.map(a => (typeof a === 'object' ? JSON.stringify(a, null, 2) : String(a))).join(' ');
    console.log(msg);
    fs.appendFileSync(logFile, msg + '\n', 'utf8');
}

app.whenReady().then(async () => {
    const win = new BrowserWindow({
        width: 1400,
        height: 900,
        show: false,
        webPreferences: {
            contextIsolation: false,
            nodeIntegration: true
        }
    });

    win.webContents.on('console-message', (event, level, message, line, sourceId) => {
        log(`[Browser Console] ${message}`);
    });

    try {
        log('1. Loading http://127.0.0.1:8080/ ...');
        await win.loadURL('http://127.0.0.1:8080/');
        await new Promise(r => setTimeout(r, 2000));

        // Step 1: Check initial login portal state
        const initialCheck = await win.webContents.executeJavaScript(`
            (() => {
                const u = document.getElementById('glass-login-username');
                const p = document.getElementById('glass-login-password');
                const btn = document.getElementById('btn-glass-submit');
                const portal = document.getElementById('login-portal-view');
                return {
                    portalActive: portal?.classList.contains('active'),
                    uExists: !!u,
                    uDisabled: u?.disabled,
                    uReadOnly: u?.readOnly,
                    pExists: !!p,
                    pDisabled: p?.disabled,
                    btnDisabled: btn?.disabled
                };
            })()
        `);
        log('Initial Portal Check:', initialCheck);

        // Step 2: Log in using Account A (e.g. tourist_ana)
        log('2. Logging in as tourist_ana...');
        const loginResult = await win.webContents.executeJavaScript(`
            (async () => {
                const u = document.getElementById('glass-login-username');
                const p = document.getElementById('glass-login-password');
                u.value = 'tourist_ana';
                p.value = 'password123';
                await handleGlassLoginSubmit();
                return {
                    authToken: state.authToken,
                    currentUser: state.currentUser?.username,
                    roleCode: state.currentUser?.roleCode
                };
            })()
        `);
        log('Account A Login Result:', loginResult);
        await new Promise(r => setTimeout(r, 2500));

        // Step 3: Verify dashboard is active
        const dashCheck = await win.webContents.executeJavaScript(`
            (() => {
                const mgmt = document.getElementById('management-portal-view');
                const pub = document.getElementById('public-portal-view');
                const login = document.getElementById('login-portal-view');
                return {
                    mgmtActive: mgmt?.classList.contains('active'),
                    pubActive: pub?.classList.contains('active'),
                    loginActive: login?.classList.contains('active'),
                    currentUser: state.currentUser?.username,
                    activeRole: state.activeRole
                };
            })()
        `);
        log('Account A Dashboard State:', dashCheck);

        // Step 4: Perform Logout
        log('4. Performing logout...');
        await win.webContents.executeJavaScript(`
            (() => {
                logout(); // directly call logout
            })()
        `);
        await new Promise(r => setTimeout(r, 1500));

        // Step 5: Deep inspection of Login Portal after logout
        log('5. Inspecting Login Portal responsiveness after logout...');
        const afterLogoutCheck = await win.webContents.executeJavaScript(`
            (() => {
                const u = document.getElementById('glass-login-username');
                const p = document.getElementById('glass-login-password');
                const btn = document.getElementById('btn-glass-submit');
                const portal = document.getElementById('login-portal-view');
                const mgmt = document.getElementById('management-portal-view');
                const form = document.getElementById('form-glass-login');

                // Check bounding rect and elementFromPoint
                const uRect = u ? u.getBoundingClientRect() : null;
                const pointElem = uRect ? document.elementFromPoint(uRect.left + 20, uRect.top + 10) : null;

                // Check all open modals
                const openModals = Array.from(document.querySelectorAll('.modal-backdrop.open, [class*="modal"].open, [class*="backdrop"].open')).map(el => el.id || el.className);

                // Check styles of u and container
                const uStyle = u ? window.getComputedStyle(u) : null;
                const portalStyle = portal ? window.getComputedStyle(portal) : null;

                // Test typing into u
                let typingSuccess = false;
                try {
                    u.focus();
                    u.value = 'admin';
                    p.focus();
                    p.value = 'admin123';
                    typingSuccess = (u.value === 'admin' && p.value === 'admin123');
                } catch (e) {
                    typingSuccess = false;
                }

                // Check event listeners / isSubmittingLogin
                const subState = typeof isSubmittingLogin !== 'undefined' ? isSubmittingLogin : 'undefined';

                return {
                    portalActive: portal?.classList.contains('active'),
                    portalDisplay: portalStyle?.display,
                    mgmtDisplay: mgmt ? window.getComputedStyle(mgmt).display : null,
                    uDisabled: u?.disabled,
                    uReadOnly: u?.readOnly,
                    uPointerEvents: uStyle?.pointerEvents,
                    uVisibility: uStyle?.visibility,
                    pointElemTagName: pointElem?.tagName,
                    pointElemId: pointElem?.id,
                    pointElemClass: pointElem?.className,
                    openModals: openModals,
                    isSubmittingLogin: subState,
                    btnDisabled: btn?.disabled,
                    typingSuccess: typingSuccess,
                    uCurrentValue: u?.value
                };
            })()
        `);
        log('Inspection After Logout:', afterLogoutCheck);

        // Step 6: Try Account B login (admin)
        log('6. Attempting login as Account B (admin)...');
        const loginBResult = await win.webContents.executeJavaScript(`
            (async () => {
                const u = document.getElementById('glass-login-username');
                const p = document.getElementById('glass-login-password');
                u.value = 'admin';
                p.value = 'admin123';
                await handleGlassLoginSubmit();
                return {
                    authToken: state.authToken,
                    currentUser: state.currentUser?.username,
                    roleCode: state.currentUser?.roleCode
                };
            })()
        `);
        log('Account B Login Result:', loginBResult);
        await new Promise(r => setTimeout(r, 2000));

        const dashBCheck = await win.webContents.executeJavaScript(`
            (() => {
                const mgmt = document.getElementById('management-portal-view');
                return {
                    mgmtActive: mgmt?.classList.contains('active'),
                    currentUser: state.currentUser?.username,
                    activeRole: state.activeRole
                };
            })()
        `);
        log('Account B Dashboard State:', dashBCheck);
        log('TEST COMPLETED SUCCESSFULLY');
    } catch (err) {
        log('Test error:', err.stack || err);
    } finally {
        app.quit();
    }
});
