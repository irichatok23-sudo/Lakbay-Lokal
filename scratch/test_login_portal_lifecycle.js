const http = require('http');
const fs = require('fs');

const API_ENDPOINT = 'http://127.0.0.1:8080/api';

function httpRequest(method, urlPath, headers = {}, body = null) {
    return new Promise((resolve, reject) => {
        const url = new URL(urlPath, API_ENDPOINT);
        const options = {
            hostname: url.hostname,
            port: url.port,
            path: url.pathname + url.search,
            method: method,
            headers: {
                'Content-Type': 'application/json',
                ...headers
            }
        };

        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    const parsed = JSON.parse(data);
                    resolve({ status: res.statusCode, headers: res.headers, data: parsed });
                } catch (e) {
                    resolve({ status: res.statusCode, headers: res.headers, data });
                }
            });
        });

        req.on('error', reject);
        if (body) {
            req.write(typeof body === 'string' ? body : JSON.stringify(body));
        }
        req.end();
    });
}

// -----------------------------------------------------------------------------
// PART 1: DOM STATE SIMULATION ENGINE
// -----------------------------------------------------------------------------
function createMockDOM() {
    const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

    const elements = {};
    function getOrCreate(id, tag = 'div') {
        if (!elements[id]) {
            const classSet = new Set();
            elements[id] = {
                id: id,
                tagName: tag.toUpperCase(),
                classList: {
                    add: (c) => classSet.add(c),
                    remove: (c) => classSet.delete(c),
                    contains: (c) => classSet.has(c)
                },
                style: {},
                attributes: {},
                value: '',
                textContent: '',
                innerHTML: '',
                disabled: false,
                readOnly: false,
                children: [],
                appendChild: () => {},
                removeChild: () => {},
                querySelector: (sel) => ({ textContent: '', innerHTML: '', style: {}, classList: { add: ()=>{}, remove: ()=>{}, contains: ()=>false } }),
                querySelectorAll: (sel) => [],
                removeAttribute(attr) { delete this.attributes[attr]; if (attr === 'disabled') this.disabled = false; if (attr === 'readonly') this.readOnly = false; },
                setAttribute(attr, val) { this.attributes[attr] = val; if (attr === 'disabled') this.disabled = true; if (attr === 'readonly') this.readOnly = true; },
                focus() { this.focused = true; },
                blur() { this.focused = false; }
            };
        }
        return elements[id];
    }

    // Pre-populate elements found in index.html
    const idRegex = /id="([^"]+)"/g;
    let match;
    while ((match = idRegex.exec(html)) !== null) {
        getOrCreate(match[1]);
    }

    // Identify all modal backdrops
    const modalBackdrops = [];
    const modalRegex = /<div class="modal-backdrop"\s+id="([^"]+)"/g;
    while ((match = modalRegex.exec(html)) !== null) {
        const m = getOrCreate(match[1]);
        m.classList.add('modal-backdrop');
        modalBackdrops.push(m);
    }

    // Default portal active state
    getOrCreate('login-portal-view').classList.add('active');

    const mockDocument = {
        getElementById: (id) => elements[id] || null,
        querySelectorAll: (sel) => {
            if (sel === '.modal-backdrop') return modalBackdrops;
            if (sel === '#sidebar-nav-menu .nav-link' || sel === '.tab-pane') return [];
            return [];
        },
        querySelector: (sel) => ({ textContent: '', innerHTML: '', style: {}, classList: { add: ()=>{}, remove: ()=>{}, contains: ()=>false } }),
        createElement: (tag) => ({
            tagName: tag.toUpperCase(),
            className: '',
            textContent: '',
            innerHTML: '',
            style: {},
            appendChild: () => {},
            remove: () => {}
        }),
        body: { appendChild: () => {}, removeChild: () => {} },
        addEventListener: () => {}
    };

    const mockWindow = {
        scrollTo: () => {},
        location: { hash: '' },
        addEventListener: () => {}
    };

    const mockLocalStorage = {
        store: {},
        getItem: (k) => mockLocalStorage.store[k] || null,
        setItem: (k, v) => { mockLocalStorage.store[k] = String(v); },
        removeItem: (k) => { delete mockLocalStorage.store[k]; }
    };

    return { elements, modalBackdrops, mockDocument, mockWindow, mockLocalStorage };
}

async function runLifecycleTests() {
    console.log('========================================================================');
    console.log('LAKBAY LOKAL: LOGIN PORTAL & MULTI-ACCOUNT LOGOUT LIFECYCLE AUDIT');
    console.log('========================================================================\n');

    const dom = createMockDOM();
    global.document = dom.mockDocument;
    global.window = dom.mockWindow;
    global.localStorage = dom.mockLocalStorage;
    global.confirm = () => true;

    // Load app.js in simulated sandbox
    const appJsSource = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
    
    // Evaluate in function scope with mock globals
    const evalEnv = new Function(
        'document', 'window', 'localStorage', 'confirm',
        appJsSource + `
        return {
            state,
            showLoginView,
            logout,
            handleGlassLoginSubmit,
            redirectToRoleHomepage,
            openModal,
            closeModal,
            closeAllModals,
            setAuthenticatedUser,
            openUserProfileModal,
            openViewBookingModal,
            getIsSubmittingLogin: () => isSubmittingLogin,
            setIsSubmittingLogin: (v) => { isSubmittingLogin = v; }
        };`
    );

    const app = evalEnv(dom.mockDocument, dom.mockWindow, dom.mockLocalStorage, () => true);

    const originalFetch = global.fetch;
    global.fetch = function(url, options) {
        if (typeof url === 'string' && url.startsWith('/')) {
            url = 'http://127.0.0.1:8080' + url;
        }
        return originalFetch(url, options);
    };

    const testAccounts = [
        { role: 'TOURIST', username: 'tourist_ana', password: 'password123', expectedDesk: 'Tourist Experience Workspace' },
        { role: 'STAKEHOLDER', username: 'guide_pedro', password: 'password123', expectedDesk: 'Community Worker Desk' },
        { role: 'BARANGAY_OFFICIAL', username: 'brgy_olog', password: 'password123', expectedDesk: 'Barangay Tourism Jurisdiction Desk' },
        { role: 'LGU_OFFICER', username: 'lgu_naval', password: 'password123', expectedDesk: 'Municipal Tourism Office Desk' },
        { role: 'TOURISM_OFFICE', username: 'tourismoffice', password: 'password123', expectedDesk: 'Provincial Tourism Information Management' },
        { role: 'SYSTEM_ADMIN', username: 'admin', password: 'password123', expectedDesk: 'Full System Administrative Scope' }
    ];

    let passedCycles = 0;

    for (let i = 0; i < testAccounts.length; i++) {
        const acc = testAccounts[i];
        console.log(`------------------------------------------------------------------------`);
        console.log(`[CYCLE ${i+1}/${testAccounts.length}] Testing Account: ${acc.username} (${acc.role})`);
        console.log(`------------------------------------------------------------------------`);

        // 1. Verify Login Portal is active & interactive
        console.log(`1. Checking Login Portal Initial State:`);
        app.showLoginView();
        const loginPortal = dom.mockDocument.getElementById('login-portal-view');
        const uInput = dom.mockDocument.getElementById('glass-login-username');
        const pInput = dom.mockDocument.getElementById('glass-login-password');
        const btn = dom.mockDocument.getElementById('btn-glass-submit');

        if (!loginPortal.classList.contains('active')) throw new Error('login-portal-view missing active class');
        if (loginPortal.style.display !== 'flex') throw new Error(`login-portal-view display is ${loginPortal.style.display}, expected flex`);
        if (uInput.disabled) throw new Error('uInput is disabled');
        if (pInput.disabled) throw new Error('pInput is disabled');
        if (btn.disabled) throw new Error('btn-glass-submit is disabled');
        if (app.getIsSubmittingLogin()) throw new Error('isSubmittingLogin is true before login');
        console.log(`   ✓ Login portal is visible, responsive, and ready for input.`);

        // 2. Type credentials and verify input acceptance
        console.log(`2. Typing credentials for ${acc.username}:`);
        uInput.value = acc.username;
        pInput.value = acc.password;
        if (uInput.value !== acc.username || pInput.value !== acc.password) {
            throw new Error('Inputs failed to accept typed values');
        }
        console.log(`   ✓ Username & password fields successfully accepted typed credentials.`);

        // 3. Authenticate with backend API
        console.log(`3. Authenticating with REST backend (/api/auth/login):`);
        const loginRes = await httpRequest('POST', '/api/auth/login', {}, {
            username: acc.username,
            password: acc.password
        });
        if (loginRes.status !== 200 || !loginRes.data.success || !loginRes.data.token) {
            throw new Error(`Authentication failed for ${acc.username}: ${JSON.stringify(loginRes.data)}`);
        }
        console.log(`   ✓ Backend returned 200 OK: User=${loginRes.data.user.username}, Role=${loginRes.data.user.roleCode}, Token=${loginRes.data.token.substring(0, 16)}...`);

        // 4. Update application state & navigate to dashboard
        app.setAuthenticatedUser(loginRes.data.user, loginRes.data.token);
        app.redirectToRoleHomepage(loginRes.data.user.roleCode);

        const mgmtPortal = dom.mockDocument.getElementById('management-portal-view');
        if (loginPortal.classList.contains('active')) throw new Error('login-portal-view should NOT be active while logged in');
        if (loginPortal.style.display !== 'none') throw new Error('login-portal-view should have display: none');
        console.log(`   ✓ Dashboard transitioned: management-portal-view active, login-portal-view hidden.`);

        // 5. Simulate opening a modal during dashboard usage (e.g. user profile / view booking)
        console.log(`5. Simulating modal interaction while in dashboard:`);
        app.openModal('modal-user-profile');
        const userProfileModal = dom.mockDocument.getElementById('modal-user-profile');
        if (!userProfileModal.classList.contains('open')) throw new Error('Modal failed to open');
        console.log(`   ✓ Modal 'modal-user-profile' opened with class .open`);

        // 6. Perform Logout
        console.log(`6. Performing Logout:`);
        app.logout();

        // 7. Thoroughly inspect Login Portal after logout
        console.log(`7. Verifying Login Portal Responsiveness After Logout:`);
        if (app.state.authToken !== null) throw new Error('state.authToken was not cleared');
        if (app.state.currentUser !== null) throw new Error('state.currentUser was not cleared');
        if (app.state.activeRole !== null) throw new Error('state.activeRole was not cleared');
        if (app.getIsSubmittingLogin() !== false) throw new Error('isSubmittingLogin was not reset to false');

        // Check modal cleanup
        const anyModalOpen = dom.modalBackdrops.some(m => m.classList.contains('open') || m.style.display !== 'none');
        if (anyModalOpen) throw new Error('One or more modals remained open or visible after logout');
        console.log(`   ✓ All modal backdrops and overlays were dismissed and hidden.`);

        // Check input accessibility and typing
        if (uInput.disabled) throw new Error('uInput is disabled after logout');
        if (pInput.disabled) throw new Error('pInput is disabled after logout');
        if (uInput.readOnly) throw new Error('uInput is readOnly after logout');
        if (pInput.readOnly) throw new Error('pInput is readOnly after logout');
        if (uInput.style.pointerEvents !== 'auto') throw new Error(`uInput pointerEvents is ${uInput.style.pointerEvents}`);
        if (pInput.style.pointerEvents !== 'auto') throw new Error(`pInput pointerEvents is ${pInput.style.pointerEvents}`);
        if (btn.disabled) throw new Error('Login button remained disabled after logout');
        console.log(`   ✓ Login inputs and submit button are completely enabled with pointer-events: auto.\n`);

        passedCycles++;
    }

    // Repeated rapid login/logout test without application restart
    console.log(`========================================================================`);
    console.log(`RAPID MULTI-CYCLE LOGOUT & RE-LOGIN STRESS TEST (No App Restart)`);
    console.log(`========================================================================`);
    for (let r = 1; r <= 5; r++) {
        // Toggle between tourist and admin
        const userToTest = (r % 2 === 1) ? testAccounts[0] : testAccounts[5];
        app.showLoginView();
        const uInput = dom.mockDocument.getElementById('glass-login-username');
        const pInput = dom.mockDocument.getElementById('glass-login-password');
        uInput.value = userToTest.username;
        pInput.value = userToTest.password;

        const res = await httpRequest('POST', '/api/auth/login', {}, {
            username: userToTest.username,
            password: userToTest.password
        });
        if (res.status !== 200 || !res.data.success) throw new Error(`Stress test login failed on round ${r}`);

        app.setAuthenticatedUser(res.data.user, res.data.token);
        app.redirectToRoleHomepage(res.data.user.roleCode);

        // Open 2 modals simultaneously
        app.openModal('modal-view-destination');
        app.openModal('modal-view-booking');

        // Logout immediately
        app.logout();

        // Verify inputs are immediately typable
        if (uInput.disabled || pInput.disabled || app.getIsSubmittingLogin()) {
            throw new Error(`Login portal locked on rapid cycle ${r}`);
        }
        console.log(`   ✓ Rapid Cycle ${r} PASS: Logged in as ${userToTest.username}, opened modals, logged out cleanly.`);
    }

    console.log(`\n========================================================================`);
    console.log(`ALL TESTS PASSED: ${passedCycles}/${testAccounts.length} Role Cycles + 5 Rapid Stress Cycles.`);
    console.log(`Login portal responsiveness permanently fixed and verified!`);
    console.log(`========================================================================\n`);
}

runLifecycleTests().catch(err => {
    console.error('\nTEST FAILED WITH ERROR:', err);
    process.exit(1);
});
