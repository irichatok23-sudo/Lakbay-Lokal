const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');

process.env.ELECTRON_DISABLE_SECURITY_WARNINGS = 'true';

app.whenReady().then(async () => {
    const win = new BrowserWindow({
        width: 1420,
        height: 900,
        show: false,
        webPreferences: {
            nodeIntegration: false,
            contextIsolation: true,
            preload: path.join(__dirname, '..', 'electron', 'preload.js')
        }
    });

    win.webContents.on('console-message', (e, level, msg) => {
        console.log('[Electron Page Console]', msg);
    });

    console.log('1. Loading http://127.0.0.1:8080/ ...');
    await win.loadURL('http://127.0.0.1:8080/');
    await new Promise(r => setTimeout(r, 1500));

    // Inspect initial login portal
    const initCheck = await win.webContents.executeJavaScript(`
        (() => {
            const u = document.getElementById('glass-login-username');
            const p = document.getElementById('glass-login-password');
            const btn = document.getElementById('btn-glass-submit');
            const uRect = u.getBoundingClientRect();
            const pRect = p.getBoundingClientRect();
            const btnRect = btn.getBoundingClientRect();
            const topAtU = document.elementFromPoint(uRect.left + 20, uRect.top + 15);
            return {
                uRect: { x: uRect.left + 20, y: uRect.top + 15 },
                pRect: { x: pRect.left + 20, y: pRect.top + 15 },
                btnRect: { x: btnRect.left + 20, y: btnRect.top + 15 },
                topAtU: topAtU ? topAtU.outerHTML.substring(0, 100) : null
            };
        })()
    `);
    console.log('Initial Portal Coordinates:', JSON.stringify(initCheck, null, 2));

    // Step 1: Login as tourist_ana
    console.log('2. Performing login as tourist_ana...');
    await win.webContents.executeJavaScript(`
        document.getElementById('glass-login-username').value = 'tourist_ana';
        document.getElementById('glass-login-password').value = 'password123';
        handleGlassLoginSubmit();
    `);
    await new Promise(r => setTimeout(r, 2000));

    // Check dashboard state
    const dashCheck = await win.webContents.executeJavaScript(`
        (() => {
            const login = document.getElementById('login-portal-view');
            const mgmt = document.getElementById('management-portal-view');
            const pub = document.getElementById('public-portal-view');
            const body = document.body;
            return {
                loginStyleDisplay: login.style.display,
                loginHasActive: login.classList.contains('active'),
                loginComputedDisplay: window.getComputedStyle(login).display,
                mgmtStyleDisplay: mgmt.style.display,
                mgmtHasActive: mgmt.classList.contains('active'),
                mgmtComputedDisplay: window.getComputedStyle(mgmt).display,
                mgmtOffsetHeight: mgmt.offsetHeight,
                pubStyleDisplay: pub.style.display,
                pubComputedDisplay: window.getComputedStyle(pub).display,
                activeRole: state.activeRole,
                currentUser: state.currentUser ? state.currentUser.username : null,
                bodyChildrenVisible: Array.from(body.children).map(c => ({
                    id: c.id,
                    tag: c.tagName,
                    display: window.getComputedStyle(c).display,
                    height: c.offsetHeight
                }))
            };
        })()
    `);
    console.log('Dashboard Check After Login:', JSON.stringify(dashCheck, null, 2));

    // Step 2: Now perform logout via handleLogout()
    console.log('3. Performing logout...');
    await win.webContents.executeJavaScript(`
        // Override window.confirm to return true automatically
        window.confirm = () => true;
        handleLogout();
    `);
    await new Promise(r => setTimeout(r, 1500));

    // Step 3: Inspect Login Portal Hit-Testing and Elements
    const logoutCheck = await win.webContents.executeJavaScript(`
        (() => {
            const u = document.getElementById('glass-login-username');
            const p = document.getElementById('glass-login-password');
            const btn = document.getElementById('btn-glass-submit');
            const uRect = u.getBoundingClientRect();
            const pRect = p.getBoundingClientRect();
            const btnRect = btn.getBoundingClientRect();

            const topAtU = document.elementFromPoint(uRect.left + 20, uRect.top + 15);
            const topAtP = document.elementFromPoint(pRect.left + 20, pRect.top + 15);
            const topAtBtn = document.elementFromPoint(btnRect.left + 20, btnRect.top + 15);

            // Check all elements overlapping or covering
            const elementsAtU = document.elementsFromPoint ? document.elementsFromPoint(uRect.left + 20, uRect.top + 15).map(e => e.tagName + '#' + e.id + '.' + e.className) : [];

            return {
                uRect: { x: uRect.left + 20, y: uRect.top + 15, w: uRect.width, h: uRect.height },
                pRect: { x: pRect.left + 20, y: pRect.top + 15, w: pRect.width, h: pRect.height },
                btnRect: { x: btnRect.left + 20, y: btnRect.top + 15, w: btnRect.width, h: btnRect.height },
                topAtU: topAtU ? topAtU.tagName + '#' + topAtU.id + '.' + topAtU.className : null,
                topAtP: topAtP ? topAtP.tagName + '#' + topAtP.id + '.' + topAtP.className : null,
                topAtBtn: topAtBtn ? topAtBtn.tagName + '#' + topAtBtn.id + '.' + topAtBtn.className : null,
                elementsAtU: elementsAtU,
                uDisabled: u.disabled,
                uReadOnly: u.readOnly,
                uPointerEvents: window.getComputedStyle(u).pointerEvents,
                uTabIndex: u.tabIndex,
                activeElement: document.activeElement ? document.activeElement.tagName + '#' + document.activeElement.id : null
            };
        })()
    `);
    console.log('Post-Logout Diagnostics:', JSON.stringify(logoutCheck, null, 2));

    // Step 4: Test real OS mouse clicks via webContents.sendInputEvent
    console.log('4. Testing Real OS Mouse Clicks...');
    const uX = Math.round(logoutCheck.uRect.x);
    const uY = Math.round(logoutCheck.uRect.y);

    win.webContents.sendInputEvent({ type: 'mouseMove', x: uX, y: uY });
    win.webContents.sendInputEvent({ type: 'mouseDown', x: uX, y: uY, button: 'left', clickCount: 1 });
    win.webContents.sendInputEvent({ type: 'mouseUp', x: uX, y: uY, button: 'left', clickCount: 1 });
    await new Promise(r => setTimeout(r, 200));

    const focusAfterRealClick = await win.webContents.executeJavaScript(`
        (() => {
            const u = document.getElementById('glass-login-username');
            return {
                activeElement: document.activeElement ? document.activeElement.tagName + '#' + document.activeElement.id : null,
                uIsActive: document.activeElement === u
            };
        })()
    `);
    console.log('Focus After Real Click on Username:', JSON.stringify(focusAfterRealClick, null, 2));

    // Step 5: Test real OS keyboard typing via webContents.sendInputEvent
    console.log('5. Testing Real OS Keyboard Typing...');
    win.webContents.sendInputEvent({ type: 'char', keyCode: 'g' });
    win.webContents.sendInputEvent({ type: 'char', keyCode: 'u' });
    win.webContents.sendInputEvent({ type: 'char', keyCode: 'i' });
    win.webContents.sendInputEvent({ type: 'char', keyCode: 'd' });
    win.webContents.sendInputEvent({ type: 'char', keyCode: 'e' });
    await new Promise(r => setTimeout(r, 200));

    const valueAfterRealTyping = await win.webContents.executeJavaScript(`
        (() => {
            const u = document.getElementById('glass-login-username');
            return {
                usernameValue: u.value
            };
        })()
    `);
    console.log('Value After Real Typing:', JSON.stringify(valueAfterRealTyping, null, 2));

    await win.close();
    app.quit();
});
