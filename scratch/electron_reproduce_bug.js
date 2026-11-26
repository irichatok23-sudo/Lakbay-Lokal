const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');

const logFile = path.join(__dirname, 'reproduce_output.log');
fs.writeFileSync(logFile, 'Starting Reproduction Test in Electron...\n', 'utf8');

function log(...args) {
    const msg = args.map(a => (typeof a === 'object' ? JSON.stringify(a, null, 2) : String(a))).join(' ');
    console.log(msg);
    fs.appendFileSync(logFile, msg + '\n', 'utf8');
}

app.whenReady().then(async () => {
    log('Electron Ready. Creating window matching main.js...');
    const win = new BrowserWindow({
        title: 'Lakbay Lokal Reproduction Test',
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

    win.webContents.on('console-message', (event, level, message, line, sourceId) => {
        log(`[Renderer Console] ${message}`);
    });

    try {
        log('1. Loading http://127.0.0.1:8080/ ...');
        await win.loadURL('http://127.0.0.1:8080/');
        await new Promise(r => setTimeout(r, 2000));

        // Initial check: get coords of username field
        const coords = await win.webContents.executeJavaScript(`
            (() => {
                const u = document.getElementById('glass-login-username');
                const rect = u.getBoundingClientRect();
                return { x: Math.round(rect.left + rect.width / 2), y: Math.round(rect.top + rect.height / 2) };
            })()
        `);
        log('Username field coords:', coords);

        // Test typing into username on fresh load
        win.webContents.sendInputEvent({ type: 'mouseDown', x: coords.x, y: coords.y, button: 'left', clickCount: 1 });
        win.webContents.sendInputEvent({ type: 'mouseUp', x: coords.x, y: coords.y, button: 'left', clickCount: 1 });
        await new Promise(r => setTimeout(r, 200));

        const activeElem1 = await win.webContents.executeJavaScript(`
            (() => {
                return {
                    activeId: document.activeElement?.id,
                    activeTag: document.activeElement?.tagName,
                    uValue: document.getElementById('glass-login-username')?.value
                };
            })()
        `);
        log('Initial Click Active Element:', activeElem1);

        // Send real keyboard strokes for Account A (tourist_ana)
        log('2. Logging in as Account A (tourist_ana)...');
        await win.webContents.executeJavaScript(`
            (async () => {
                document.getElementById('glass-login-username').value = 'tourist_ana';
                document.getElementById('glass-login-password').value = 'password123';
                await handleGlassLoginSubmit();
            })()
        `);
        await new Promise(r => setTimeout(r, 3000));

        const dashState = await win.webContents.executeJavaScript(`
            (() => {
                const mgmt = document.getElementById('management-portal-view');
                const login = document.getElementById('login-portal-view');
                return {
                    mgmtActive: mgmt?.classList.contains('active'),
                    mgmtDisplay: window.getComputedStyle(mgmt).display,
                    loginActive: login?.classList.contains('active'),
                    loginDisplay: window.getComputedStyle(login).display,
                    currentUser: state.currentUser?.username
                };
            })()
        `);
        log('Dashboard State:', dashState);

        // Now logout
        log('3. Clicking Logout...');
        await win.webContents.executeJavaScript(`
            (() => {
                logout();
            })()
        `);
        await new Promise(r => setTimeout(r, 2000));

        // Now inspect state after logout
        log('4. Deep Inspection of Electron Window after logout...');
        const inspection = await win.webContents.executeJavaScript(`
            (() => {
                const u = document.getElementById('glass-login-username');
                const p = document.getElementById('glass-login-password');
                const btn = document.getElementById('btn-glass-submit');
                const portal = document.getElementById('login-portal-view');
                const rect = u.getBoundingClientRect();
                const elemAtPoint = document.elementFromPoint(Math.round(rect.left + rect.width / 2), Math.round(rect.top + rect.height / 2));

                const allOpenModals = Array.from(document.querySelectorAll('.modal-backdrop.open, [class*="modal"].open')).map(m => m.id);

                return {
                    activeElementId: document.activeElement?.id,
                    activeElementTag: document.activeElement?.tagName,
                    elemAtPointId: elemAtPoint?.id,
                    elemAtPointTag: elemAtPoint?.tagName,
                    elemAtPointClass: elemAtPoint?.className,
                    uDisabled: u?.disabled,
                    uReadOnly: u?.readOnly,
                    uPointerEvents: window.getComputedStyle(u).pointerEvents,
                    uVisibility: window.getComputedStyle(u).visibility,
                    portalDisplay: window.getComputedStyle(portal).display,
                    portalPointerEvents: window.getComputedStyle(portal).pointerEvents,
                    openModals: allOpenModals,
                    windowFocused: document.hasFocus(),
                    uValue: u.value
                };
            })()
        `);
        log('Inspection After Logout:', inspection);

        // Try physical clicks and keyboard input on username field via Electron
        log('5. Sending mouse click to username input in Electron...');
        win.webContents.sendInputEvent({ type: 'mouseDown', x: coords.x, y: coords.y, button: 'left', clickCount: 1 });
        win.webContents.sendInputEvent({ type: 'mouseUp', x: coords.x, y: coords.y, button: 'left', clickCount: 1 });
        await new Promise(r => setTimeout(r, 200));

        const focusAfterClick = await win.webContents.executeJavaScript(`
            (() => {
                return {
                    activeElementId: document.activeElement?.id,
                    activeElementTag: document.activeElement?.tagName,
                    windowFocused: document.hasFocus()
                };
            })()
        `);
        log('Focus After Click:', focusAfterClick);

        // Send characters 'g', 'u', 'i', 'd', 'e'
        log('6. Sending keystrokes via sendInputEvent...');
        for (const char of 'guide') {
            win.webContents.sendInputEvent({ type: 'char', keyCode: char });
            await new Promise(r => setTimeout(r, 50));
        }

        const valueAfterTyping = await win.webContents.executeJavaScript(`
            (() => {
                const u = document.getElementById('glass-login-username');
                return {
                    value: u?.value,
                    selectionStart: u?.selectionStart,
                    selectionEnd: u?.selectionEnd
                };
            })()
        `);
        log('Username Value After sendInputEvent typing:', valueAfterTyping);

    } catch (err) {
        log('Error in test:', err.stack || err);
    } finally {
        app.quit();
    }
});
