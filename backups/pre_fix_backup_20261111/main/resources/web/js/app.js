/**
 * ==============================================================================
 * LAKBAY LOKAL: COMMUNITY TOURISM INFORMATION SYSTEM
 * CLIENT-SIDE ARCHITECTURE & DESKTOP APPLICATION LOGIC
 * ==============================================================================
 */

// Application Global State
const state = {
    authToken: localStorage.getItem('lakbay_token') || null,
    currentUser: null,
    activeRole: null, // Strictly assigned from verified database user record
    activeTab: 'overview',
    destinations: [],
    packages: [],
    stakeholders: [],
    services: [],
    barangays: [],
    bookings: [],
    announcements: [],
    reviews: [],
    customBuilder: {
        selectedServices: new Set(),
        pax: 2,
        startDate: '',
        endDate: '',
        notes: '',
        total: 0
    }
};

const API_BASE = '/api';

// ------------------------------------------------------------------------------
// INITIALIZATION & LIFECYCLE
// ------------------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', async () => {
    initDefaultDates();
    checkDesktopEnvironment();

    // Verify existing authentication token if present
    const isAuthenticated = await verifyExistingSession();

    if (isAuthenticated && state.currentUser) {
        // Authenticated user session active: load app data and redirect to assigned role homepage
        try {
            await loadInitialData();
        } catch (e) {
            console.warn('Initial data load notice:', e);
        }
        redirectToRoleHomepage(state.currentUser.roleCode);
    } else {
        // Unauthenticated visitor: strictly show Login Portal first
        showLoginView();
    }
});

async function loadInitialData() {
    await Promise.allSettled([
        loadBarangays(),
        loadDestinations(),
        loadPackages(),
        loadStakeholderServices(),
        loadStakeholdersList(),
        loadAnnouncementsList()
    ]);
    
    renderPublicDestinations(state.destinations);
    renderPublicPackages(state.packages);
    updateHeroCounters();
    loadKPIs();
}

function initDefaultDates() {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    const fmtToday = today.toISOString().split('T')[0];
    const fmtTomorrow = tomorrow.toISOString().split('T')[0];

    const bStart = document.getElementById('builder-date-start');
    const bEnd = document.getElementById('builder-date-end');
    if (bStart) bStart.value = fmtToday;
    if (bEnd) bEnd.value = fmtTomorrow;

    const arrDate = document.getElementById('arr-date');
    if (arrDate) arrDate.value = fmtToday;

    state.customBuilder.startDate = fmtToday;
    state.customBuilder.endDate = fmtTomorrow;
}

// ------------------------------------------------------------------------------
// DESKTOP ENVIRONMENT & ELECTRON WINDOW CONTROLS
// ------------------------------------------------------------------------------
function checkDesktopEnvironment() {
    const titlebar = document.getElementById('desktop-window-bar');
    if (window.desktopAPI && window.desktopAPI.isElectron) {
        if (titlebar) titlebar.style.display = 'flex';
        console.log('[Desktop] Running in native Electron desktop mode.');
    } else {
        // In standard browser, show titlebar brand cleanly without redundant OS buttons
        const controls = document.getElementById('desktop-titlebar-controls');
        if (controls) controls.style.display = 'none';
    }
}

function minimizeDesktopWindow() {
    if (window.desktopAPI?.minimizeWindow) {
        window.desktopAPI.minimizeWindow();
    }
}

function maximizeDesktopWindow() {
    if (window.desktopAPI?.maximizeWindow) {
        window.desktopAPI.maximizeWindow();
    }
}

function closeDesktopWindow() {
    if (window.desktopAPI?.closeWindow) {
        window.desktopAPI.closeWindow();
    } else {
        window.close();
    }
}

// ------------------------------------------------------------------------------
// VIEW & PORTAL ROUTING (LOGIN VS PUBLIC TOURIST HUB VS MANAGEMENT DASHBOARD)
// ------------------------------------------------------------------------------
function showLoginView() {
    const loginPortal = document.getElementById('login-portal-view');
    const publicPortal = document.getElementById('public-portal-view');
    const mgmtPortal = document.getElementById('management-portal-view');

    if (loginPortal) loginPortal.classList.add('active');
    if (publicPortal) publicPortal.classList.remove('active');
    if (mgmtPortal) mgmtPortal.classList.remove('active');

    // Reset login inputs
    const uInput = document.getElementById('glass-login-username');
    const pInput = document.getElementById('glass-login-password');
    if (uInput) uInput.value = '';
    if (pInput) pInput.value = '';

    const errBox = document.getElementById('glass-login-error');
    if (errBox) errBox.classList.remove('show');

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function showPublicPortal() {
    if (!state.currentUser) {
        showLoginView();
        return;
    }
    document.getElementById('login-portal-view')?.classList.remove('active');
    document.getElementById('public-portal-view')?.classList.add('active');
    document.getElementById('management-portal-view')?.classList.remove('active');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function showManagementDashboard(tabId = 'overview') {
    if (!state.currentUser) {
        showLoginView();
        return;
    }
    document.getElementById('login-portal-view')?.classList.remove('active');
    document.getElementById('public-portal-view')?.classList.remove('active');
    document.getElementById('management-portal-view')?.classList.add('active');
    switchTab(tabId);
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function scrollToSection(sectionId) {
    const el = document.getElementById(sectionId);
    if (el) {
        el.scrollIntoView({ behavior: 'smooth' });
    }
}

// ------------------------------------------------------------------------------
// AUTHENTICATION & SESSION MANAGEMENT
// ------------------------------------------------------------------------------
async function verifyExistingSession() {
    if (!state.authToken) {
        return false;
    }
    try {
        const res = await fetch(`${API_BASE}/auth/me`, {
            headers: { 'X-Auth-Token': state.authToken }
        });
        if (res.ok) {
            const user = await res.json();
            setAuthenticatedUser(user, state.authToken);
            return true;
        } else {
            // Token invalid or expired in database
            state.authToken = null;
            state.currentUser = null;
            state.activeRole = null;
            localStorage.removeItem('lakbay_token');
            return false;
        }
    } catch (e) {
        console.warn('Session verification notice:', e);
        return false;
    }
}

function setAuthenticatedUser(user, token) {
    state.currentUser = user;
    state.authToken = token;
    state.activeRole = user.roleCode;
    if (token) localStorage.setItem('lakbay_token', token);

    // Update Header user info & role badge
    const hName = document.getElementById('header-user-name');
    const hBadge = document.getElementById('header-role-badge');
    if (hName) hName.textContent = user.fullName || user.username;
    if (hBadge) hBadge.textContent = user.roleName || user.roleCode;

    // Update Public portal greeting
    const pName = document.getElementById('public-user-name-label');
    if (pName) pName.textContent = user.fullName || user.username;
    const tGreet = document.getElementById('tourist-hub-greeting-text');
    if (tGreet) tGreet.textContent = `Mabuhay, ${user.fullName || user.username}!`;

    applyRolePermissions(state.activeRole);
}

function redirectToRoleHomepage(roleCode) {
    const loginPortal = document.getElementById('login-portal-view');
    const publicPortal = document.getElementById('public-portal-view');
    const mgmtPortal = document.getElementById('management-portal-view');

    if (loginPortal) loginPortal.classList.remove('active');

    const banner = document.getElementById('role-workspace-banner');
    const bTag = document.getElementById('role-banner-tag');
    const bTitle = document.getElementById('role-banner-title');
    const bDesc = document.getElementById('role-banner-desc');
    const bScope = document.getElementById('role-banner-scope-text');

    switch (roleCode) {
        case 'TOURIST':
            // 4.A: Tourist Homepage - Explore Biliran destinations, packages, community workers, profile
            if (publicPortal) publicPortal.classList.add('active');
            if (mgmtPortal) mgmtPortal.classList.remove('active');
            window.scrollTo({ top: 0, behavior: 'smooth' });
            break;

        case 'STAKEHOLDER':
            // 4.B: Local Tourism Stakeholder Homepage - Services & Offerings management
            if (publicPortal) publicPortal.classList.remove('active');
            if (mgmtPortal) mgmtPortal.classList.add('active');
            if (banner) {
                banner.style.display = 'flex';
                if (bTag) bTag.innerHTML = '<i class="fa-solid fa-handshake-angle"></i> Local Tourism Stakeholder Desk';
                if (bTitle) bTitle.textContent = `Welcome, ${state.currentUser.fullName || state.currentUser.username}`;
                if (bDesc) bDesc.textContent = 'Maintain your tourism offerings, daily rates, availability, and guest reservations in Biliran.';
                if (bScope) bScope.textContent = state.currentUser.municipalityName ? `${state.currentUser.municipalityName} Stakeholder` : 'Community Provider';
            }
            switchTab('services');
            break;

        case 'BARANGAY_OFFICIAL':
            // 4.C: Barangay Official Homepage - Verifications Desk (Restricted to their barangay)
            if (publicPortal) publicPortal.classList.remove('active');
            if (mgmtPortal) mgmtPortal.classList.add('active');
            if (banner) {
                banner.style.display = 'flex';
                if (bTag) bTag.innerHTML = '<i class="fa-solid fa-shield-halved"></i> Barangay Tourism Jurisdiction Desk';
                const brgyName = state.currentUser.barangayName || 'Ol-og';
                if (bTitle) bTitle.textContent = `Barangay Tourism Portal - Brgy. ${brgyName}`;
                if (bDesc) bDesc.textContent = `Review community stakeholder accreditations, monitor registrations, and verify tourism records within Brgy. ${brgyName}.`;
                if (bScope) bScope.textContent = `Brgy. ${brgyName} Jurisdiction`;
            }
            switchTab('verifications');
            break;

        case 'LGU_OFFICER':
            // 4.D: LGU Official Homepage - Municipal Tourism Monitoring & Carrying Capacity
            if (publicPortal) publicPortal.classList.remove('active');
            if (mgmtPortal) mgmtPortal.classList.add('active');
            if (banner) {
                banner.style.display = 'flex';
                if (bTag) bTag.innerHTML = '<i class="fa-solid fa-building-columns"></i> Municipal Tourism Monitoring Office';
                const muniName = state.currentUser.municipalityName || 'Naval';
                if (bTitle) bTitle.textContent = `LGU Tourism Operations - Municipality of ${muniName}`;
                if (bDesc) bDesc.textContent = 'Track carrying capacity thresholds, monitor community participants, and review consolidated municipal reports.';
                if (bScope) bScope.textContent = `Municipality of ${muniName}`;
            }
            switchTab('lgu-monitoring');
            break;

        case 'TOURISM_OFFICE':
            // 4.E: Tourism Office Personnel Homepage - Destinations & Provincial Packages
            if (publicPortal) publicPortal.classList.remove('active');
            if (mgmtPortal) mgmtPortal.classList.add('active');
            if (banner) {
                banner.style.display = 'flex';
                if (bTag) bTag.innerHTML = '<i class="fa-solid fa-compass-drafting"></i> Provincial Tourism Information Management';
                if (bTitle) bTitle.textContent = 'Biliran Provincial Tourism Office Dashboard';
                if (bDesc) bDesc.textContent = 'Manage destination records, oversee curated tourism packages, post advisories, and review stakeholder listings across all 8 municipalities.';
                if (bScope) bScope.textContent = 'Provincial Jurisdiction';
            }
            switchTab('destinations');
            break;

        case 'SYSTEM_ADMIN':
            // 4.F: System Administrator Homepage - Users, Roles, Security Audit & Settings
            if (publicPortal) publicPortal.classList.remove('active');
            if (mgmtPortal) mgmtPortal.classList.add('active');
            if (banner) {
                banner.style.display = 'flex';
                if (bTag) bTag.innerHTML = '<i class="fa-solid fa-users-gear"></i> System Administration & Governance Console';
                if (bTitle) bTitle.textContent = 'System Administrator Dashboard';
                if (bDesc) bDesc.textContent = 'Manage user accounts, assign database roles, review tamper-evident audit logs, and maintain system backups.';
                if (bScope) bScope.textContent = 'Full System Administrative Scope';
            }
            switchTab('admin');
            break;

        default:
            if (publicPortal) publicPortal.classList.add('active');
            if (mgmtPortal) mgmtPortal.classList.remove('active');
            break;
    }
}

// ------------------------------------------------------------------------------
// LOGIN & FORGOT PASSWORD WORKFLOW
// ------------------------------------------------------------------------------
function togglePasswordVisibility() {
    const pwdInput = document.getElementById('glass-login-password');
    const icon = document.getElementById('toggle-pwd-icon');
    if (!pwdInput) return;
    if (pwdInput.type === 'password') {
        pwdInput.type = 'text';
        if (icon) icon.className = 'fa-solid fa-eye-slash';
    } else {
        pwdInput.type = 'password';
        if (icon) icon.className = 'fa-solid fa-eye';
    }
}

async function handleGlassLoginSubmit(e) {
    e.preventDefault();
    const u = document.getElementById('glass-login-username')?.value.trim();
    const p = document.getElementById('glass-login-password')?.value;
    const btn = document.getElementById('btn-glass-submit');
    const btnText = document.getElementById('btn-glass-submit-text');
    const errBox = document.getElementById('glass-login-error');
    const errText = document.getElementById('glass-login-error-text');

    if (!u || !p) {
        if (errBox && errText) {
            errText.textContent = 'Please enter both username and password.';
            errBox.classList.add('show');
        }
        return;
    }

    if (btnText) btnText.textContent = 'Verifying credentials...';
    if (btn) btn.disabled = true;
    if (errBox) errBox.classList.remove('show');

    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: u, password: p })
        });
        const data = await res.json();
        if (data.success && data.user) {
            setAuthenticatedUser(data.user, data.token);
            redirectToRoleHomepage(data.user.roleCode);
            showToast(`Welcome, ${data.user.fullName || data.user.username} (${data.user.roleCode})!`, 'success');
            try {
                await loadInitialData();
            } catch (initErr) {
                console.warn('Initial data load warning:', initErr);
            }
        } else {
            if (errBox && errText) {
                errText.textContent = data.message || 'Invalid username or password.';
                errBox.classList.add('show');
            }
        }
    } catch (err) {
        if (errBox && errText) {
            errText.textContent = 'Authentication server connection error: ' + err.message;
            errBox.classList.add('show');
        }
    } finally {
        if (btnText) btnText.textContent = 'Sign In';
        if (btn) btn.disabled = false;
    }
}

function fillLoginForm(username) {
    const uField = document.getElementById('glass-login-username');
    const pField = document.getElementById('glass-login-password');
    if (uField) uField.value = username;
    if (pField) pField.value = 'password123';
    showToast(`Test credentials populated for '${username}'. Click 'Sign In' to authenticate against database.`, 'info');
}

function openForgotPasswordModal() {
    const errBanner = document.getElementById('forgot-error-banner');
    const succBanner = document.getElementById('forgot-success-banner');
    if (errBanner) errBanner.style.display = 'none';
    if (succBanner) succBanner.style.display = 'none';
    openModal('modal-forgot-password');
}

async function handleForgotPasswordSubmit(e) {
    e.preventDefault();
    const ident = document.getElementById('forgot-identifier')?.value.trim();
    const newPwd = document.getElementById('forgot-new-password')?.value;
    const btn = document.getElementById('btn-forgot-submit');
    const errBanner = document.getElementById('forgot-error-banner');
    const errText = document.getElementById('forgot-error-text');
    const succBanner = document.getElementById('forgot-success-banner');
    const succText = document.getElementById('forgot-success-text');

    if (!ident || !newPwd) return;
    if (newPwd.length < 6) {
        if (errBanner && errText) {
            errText.textContent = 'New password must be at least 6 characters.';
            errBanner.style.display = 'block';
        }
        return;
    }

    if (btn) btn.disabled = true;
    if (errBanner) errBanner.style.display = 'none';
    if (succBanner) succBanner.style.display = 'none';

    try {
        const res = await fetch(`${API_BASE}/auth/forgot-password`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ identifier: ident, newPassword: newPwd })
        });
        const data = await res.json();
        if (data.success) {
            if (succBanner && succText) {
                succText.textContent = data.message || 'Password reset successfully. You may now sign in.';
                succBanner.style.display = 'block';
            }
            showToast('Password updated in database! Please sign in.', 'success');
            setTimeout(() => {
                closeModal('modal-forgot-password');
                const uField = document.getElementById('glass-login-username');
                if (uField) uField.value = ident;
                const pField = document.getElementById('glass-login-password');
                if (pField) pField.value = '';
            }, 1600);
        } else {
            if (errBanner && errText) {
                errText.textContent = data.message || 'Failed to reset password.';
                errBanner.style.display = 'block';
            }
        }
    } catch (err) {
        if (errBanner && errText) {
            errText.textContent = 'Connection error: ' + err.message;
            errBanner.style.display = 'block';
        }
    } finally {
        if (btn) btn.disabled = false;
    }
}

async function handleRegisterSubmit(e) {
    e.preventDefault();
    const payload = {
        fullName: document.getElementById('reg-fullname').value,
        email: document.getElementById('reg-email').value,
        username: document.getElementById('reg-username').value,
        password: document.getElementById('reg-password').value,
        phone: document.getElementById('reg-phone').value,
        originCity: document.getElementById('reg-origincity').value
    };

    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-register');
            showToast('Registration successful! Please sign in with your new account.', 'success');
            fillLoginForm(payload.username);
        } else {
            showToast(data.message || 'Registration failed', 'error');
        }
    } catch (err) {
        showToast('Registration error: ' + err.message, 'error');
    }
}

function logout() {
    if (state.authToken) {
        fetch(`${API_BASE}/auth/logout`, {
            method: 'POST',
            headers: { 'X-Auth-Token': state.authToken }
        }).catch(() => {});
    }
    state.authToken = null;
    state.currentUser = null;
    state.activeRole = null;
    localStorage.removeItem('lakbay_token');

    showToast('Signed out successfully. Please sign in to continue.', 'info');
    showLoginView();
}

// ------------------------------------------------------------------------------
// ROLE PERMISSIONS & ACCESS CONTROL (16 MODULE RBAC MATRIX)
// ------------------------------------------------------------------------------
const ROLE_PERMITTED_TABS = {
    'TOURIST': ['overview', 'destinations', 'packages', 'bookings', 'reviews'],
    'STAKEHOLDER': ['overview', 'destinations', 'stakeholders', 'services', 'bookings', 'reviews'],
    'BARANGAY_OFFICIAL': ['overview', 'destinations', 'verifications', 'barangay-records', 'reports', 'reviews'],
    'LGU_OFFICER': ['overview', 'destinations', 'stakeholders', 'barangay-records', 'lgu-monitoring', 'reports', 'announcements', 'reviews'],
    'TOURISM_OFFICE': ['overview', 'destinations', 'announcements', 'stakeholders', 'services', 'packages', 'barangay-records', 'lgu-monitoring', 'reports', 'reviews'],
    'SYSTEM_ADMIN': [
        'overview', 'destinations', 'announcements', 'stakeholders', 'services',
        'packages', 'bookings', 'verifications', 'barangay-records', 'lgu-monitoring',
        'reviews', 'reports', 'admin', 'settings', 'audit-logs', 'backup-recovery'
    ]
};

function canRoleAccessTab(role, tabId) {
    if (!role) return false;
    if (role === 'SYSTEM_ADMIN') return true;
    const allowed = ROLE_PERMITTED_TABS[role] || [];
    return allowed.includes(tabId);
}

function applyRolePermissions(role) {
    const isAdmin = role === 'SYSTEM_ADMIN';
    const isTourism = role === 'TOURISM_OFFICE' || isAdmin;
    const isLGU = role === 'LGU_OFFICER' || isAdmin;
    const isBrgy = role === 'BARANGAY_OFFICIAL' || isAdmin;
    const isStakeholder = role === 'STAKEHOLDER' || isAdmin;
    const isTourist = role === 'TOURIST';

    // 16 Nav Items Visibility Mapping
    const setDisplay = (id, show) => {
        const el = document.getElementById(id);
        if (el) el.style.display = show ? 'block' : 'none';
    };

    setDisplay('nav-item-overview', true);
    setDisplay('nav-item-destinations', true);
    setDisplay('nav-item-announcements', isTourism || isLGU || isAdmin);
    setDisplay('nav-item-stakeholders', isTourism || isLGU || isStakeholder || isAdmin);
    setDisplay('nav-item-services', isStakeholder || isTourism || isAdmin);
    setDisplay('nav-item-packages', isTourist || isTourism || isAdmin);
    setDisplay('nav-item-bookings', isTourist || isStakeholder || isAdmin);
    setDisplay('nav-item-verifications', isBrgy || isAdmin);
    setDisplay('nav-item-barangay-records', isBrgy || isLGU || isTourism || isAdmin);
    setDisplay('nav-item-lgu-monitoring', isLGU || isTourism || isAdmin);
    setDisplay('nav-item-reviews', true);
    setDisplay('nav-item-reports', isBrgy || isLGU || isTourism || isAdmin);
    setDisplay('nav-item-admin', isAdmin);
    setDisplay('nav-item-settings', isAdmin);
    setDisplay('nav-item-audit', isAdmin);
    setDisplay('nav-item-backup', isAdmin);

    // Action button permissions
    const btnAddDest = document.getElementById('btn-add-destination');
    const btnAddPkg = document.getElementById('btn-add-package');
    const btnAddSvc = document.getElementById('btn-add-service');
    const btnAddAnnounce = document.getElementById('btn-add-announcement');

    if (btnAddDest) btnAddDest.style.display = (isTourism || isAdmin) ? 'inline-flex' : 'none';
    if (btnAddPkg) btnAddPkg.style.display = (isTourism || isAdmin) ? 'inline-flex' : 'none';
    if (btnAddSvc) btnAddSvc.style.display = (isStakeholder) ? 'inline-flex' : 'none';
    if (btnAddAnnounce) btnAddAnnounce.style.display = (isTourism || isAdmin) ? 'inline-flex' : 'none';
}

// ------------------------------------------------------------------------------
// TAB SWITCHING & ROUTING (16 MODULES)
// ------------------------------------------------------------------------------
function switchTab(tabId) {
    if (!state.currentUser) {
        showLoginView();
        return;
    }

    if (!canRoleAccessTab(state.activeRole, tabId)) {
        showToast(`Access Denied: Your assigned role '${state.activeRole}' is not authorized to view the requested module.`, 'error');
        return;
    }

    state.activeTab = tabId;

    // Update Sidebar Active Class
    const links = document.querySelectorAll('#sidebar-nav-menu .nav-link');
    links.forEach(l => l.classList.remove('active'));
    const targetLink = document.querySelector(`#sidebar-nav-menu a[href="#${tabId}"]`);
    if (targetLink) targetLink.classList.add('active');

    // Update Panes
    const panes = document.querySelectorAll('.tab-pane');
    panes.forEach(p => p.classList.remove('active'));
    const targetPane = document.getElementById(`pane-${tabId}`);
    if (targetPane) targetPane.classList.add('active');

    // Trigger module-specific data refresh
    if (tabId === 'destinations') filterDestinations();
    else if (tabId === 'packages') renderPackagesGrid();
    else if (tabId === 'stakeholders') loadStakeholdersList();
    else if (tabId === 'services') loadServicesTable();
    else if (tabId === 'bookings') loadBookingsList();
    else if (tabId === 'verifications') loadVerificationsList();
    else if (tabId === 'barangay-records') loadBarangayRecordsTable();
    else if (tabId === 'announcements') loadAnnouncementsList();
    else if (tabId === 'reports') loadReportsData();
    else if (tabId === 'admin') loadAdminData();
    else if (tabId === 'audit-logs') loadAuditLogs();
    else if (tabId === 'overview') refreshDashboardData();
}

// ------------------------------------------------------------------------------
// DATA FETCHING & SYNCHRONIZATION
// ------------------------------------------------------------------------------
async function refreshAllData() {
    await Promise.allSettled([
        loadKPIs(),
        loadDestinations(),
        loadPackages(),
        loadStakeholderServices(),
        loadStakeholdersList(),
        loadBookingsList(),
        loadAnnouncementsList(),
        loadReviewsList()
    ]);
    renderOverviewTopDestinations();
}

async function loadBarangays() {
    try {
        const res = await fetch(`${API_BASE}/barangays`);
        if (res.ok) {
            state.barangays = await res.json();
            populateBarangayDropdowns();
        }
    } catch (e) {
        console.warn('Failed loading barangays:', e);
    }
}

function populateBarangayDropdowns() {
    const filters = ['dest-barangay-filter', 'dest-form-barangay', 'user-form-barangay'];
    filters.forEach(id => {
        const el = document.getElementById(id);
        if (!el) return;
        if (id.includes('filter')) {
            el.innerHTML = '<option value="ALL">All Barangays</option>';
        } else if (id === 'user-form-barangay') {
            el.innerHTML = '<option value="">None (Province-wide)</option>';
        } else {
            el.innerHTML = '';
        }
        state.barangays.forEach(b => {
            const opt = document.createElement('option');
            opt.value = b.barangayId;
            opt.textContent = `${b.name} (${b.municipalityName})`;
            el.appendChild(opt);
        });
    });
}

async function loadKPIs() {
    try {
        const res = await fetch(`${API_BASE}/reports/kpis`, {
            headers: state.authToken ? { 'X-Auth-Token': state.authToken } : {}
        });
        if (res.ok) {
            const k = await res.json();
            if (document.getElementById('kpi-destinations')) document.getElementById('kpi-destinations').textContent = k.totalDestinations || 8;
            if (document.getElementById('kpi-stakeholders')) document.getElementById('kpi-stakeholders').textContent = k.verifiedStakeholders || 5;
            if (document.getElementById('kpi-bookings')) document.getElementById('kpi-bookings').textContent = k.activeBookings || 3;
            if (document.getElementById('kpi-visitors')) document.getElementById('kpi-visitors').textContent = k.totalVisitorFootfall || 480;
        }
    } catch (e) {
        console.warn('Failed to load KPIs:', e);
    }
}

async function loadDestinations() {
    try {
        const res = await fetch(`${API_BASE}/destinations`);
        if (res.ok) {
            state.destinations = await res.json();
            filterDestinations();
            populateArrivalDestinationDropdown();
        }
    } catch (e) {
        console.warn('Failed to load destinations:', e);
    }
}

function populateArrivalDestinationDropdown() {
    const el = document.getElementById('arr-destination-id');
    if (!el) return;
    el.innerHTML = '';
    state.destinations.forEach(d => {
        const opt = document.createElement('option');
        opt.value = d.destinationId;
        opt.textContent = `${d.name} (${d.barangayName})`;
        el.appendChild(opt);
    });
}

// ------------------------------------------------------------------------------
// DESTINATIONS MODULE
// ------------------------------------------------------------------------------
function filterDestinations() {
    const search = (document.getElementById('dest-search-input')?.value || '').toLowerCase();
    const cat = document.getElementById('dest-category-filter')?.value || 'ALL';
    const brgy = document.getElementById('dest-barangay-filter')?.value || 'ALL';

    const filtered = state.destinations.filter(d => {
        const matchSearch = d.name.toLowerCase().includes(search) || d.description.toLowerCase().includes(search);
        const matchCat = cat === 'ALL' || d.category === cat;
        const matchBrgy = brgy === 'ALL' || String(d.barangayId) === String(brgy);
        return matchSearch && matchCat && matchBrgy;
    });

    renderDestinationsGrid(filtered);
}

function renderDestinationsGrid(destList) {
    const grid = document.getElementById('destinations-grid');
    if (!grid) return;
    grid.innerHTML = '';

    if (destList.length === 0) {
        grid.innerHTML = `<div class="card p-4 text-center text-muted" style="grid-column: 1/-1;">No destinations matched your criteria.</div>`;
        return;
    }

    destList.forEach(d => {
        const card = document.createElement('div');
        card.className = 'item-card';

        // Select suitable photo
        let photo = 'assets/sambawan_island_hero_1791699785896.jpg';
        if (d.category === 'WATERFALL') photo = 'assets/tinago_falls_hero_1791699805710.jpg';

        card.innerHTML = `
            <div class="item-card-image">
                <img src="${photo}" alt="${d.name}" loading="lazy" onerror="this.src='assets/sambawan_island_hero_1791699785896.jpg'">
                <span class="item-card-badge">${formatCategory(d.category)}</span>
                <span class="item-card-rating"><i class="fa-solid fa-star"></i> ${d.averageRating || '5.0'}</span>
            </div>
            <div class="item-card-content">
                <h3 class="item-card-title">${d.name}</h3>
                <span class="item-card-location"><i class="fa-solid fa-location-dot"></i> Brgy. ${d.barangayName}, ${d.municipalityName}</span>
                <p class="item-card-desc">${d.description}</p>
                <div class="item-card-footer">
                    <div class="item-price-tag">
                        <span class="item-price-label">Entrance Fee</span>
                        <span class="item-price-val">₱${Number(d.entranceFee).toFixed(2)}</span>
                    </div>
                    <button class="btn btn-primary btn-sm" onclick="promptInquireOrBook('${d.name}', ${d.destinationId})">
                        <i class="fa-solid fa-compass"></i> Inquire / Book
                    </button>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });
}

function promptInquireOrBook(name, id) {
    switchTab('packages');
    togglePackageSubTab('custom');
    showToast(`Planning your trip to ${name}! Choose your services below.`, 'info');
}

// ------------------------------------------------------------------------------
// TOURISM PACKAGES & CUSTOM EXPERIENCE BUILDER
// ------------------------------------------------------------------------------
async function loadPackages() {
    try {
        const res = await fetch(`${API_BASE}/packages`);
        if (res.ok) {
            state.packages = await res.json();
            renderPackagesGrid();
        }
    } catch (e) {
        console.warn('Failed loading packages:', e);
    }
}

function togglePackageSubTab(mode) {
    const btnPre = document.getElementById('tab-btn-predesigned');
    const btnCus = document.getElementById('tab-btn-custom');
    const subPre = document.getElementById('subview-predesigned-packages');
    const subCus = document.getElementById('subview-custom-builder');

    if (mode === 'predesigned') {
        btnPre.classList.add('active');
        btnCus.classList.remove('active');
        subPre.style.display = 'block';
        subCus.style.display = 'none';
    } else {
        btnPre.classList.remove('active');
        btnCus.classList.add('active');
        subPre.style.display = 'none';
        subCus.style.display = 'block';
        renderCustomBuilderServices();
    }
}

function renderPackagesGrid() {
    const grid = document.getElementById('packages-grid');
    if (!grid) return;
    grid.innerHTML = '';

    state.packages.forEach(pkg => {
        const card = document.createElement('div');
        card.className = 'item-card';

        let photo = 'assets/sambawan_island_hero_1791699785896.jpg';
        if (pkg.name.toLowerCase().includes('waterfall')) photo = 'assets/tinago_falls_hero_1791699805710.jpg';

        card.innerHTML = `
            <div class="item-card-image">
                <img src="${photo}" alt="${pkg.name}" loading="lazy">
                <span class="item-card-badge">${pkg.durationDays} Day Circuit</span>
                <span class="item-card-rating"><i class="fa-solid fa-star"></i> ${pkg.averageRating || '5.0'}</span>
            </div>
            <div class="item-card-content">
                <h3 class="item-card-title">${pkg.name}</h3>
                <span class="item-card-location"><i class="fa-solid fa-users"></i> Min ${pkg.minPax} to Max ${pkg.maxPax} Travelers</span>
                <p class="item-card-desc">${pkg.description}</p>
                <div class="text-muted" style="font-size: 0.78rem; margin-bottom: 0.85rem;">
                    <strong>Inclusions:</strong> ${pkg.inclusions || 'Standard transport, local guide, and fees'}
                </div>
                <div class="item-card-footer">
                    <div class="item-price-tag">
                        <span class="item-price-label">Price per person</span>
                        <span class="item-price-val">₱${Number(pkg.pricePerPax).toFixed(2)}</span>
                    </div>
                    <button class="btn btn-primary btn-sm" onclick="openPackageBookingModal(${pkg.packageId}, '${pkg.name}', ${pkg.pricePerPax})">
                        <i class="fa-solid fa-calendar-check"></i> Book Package
                    </button>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });
}

async function loadStakeholderServices() {
    try {
        const res = await fetch(`${API_BASE}/stakeholders/services`);
        if (res.ok) {
            state.services = await res.json();
            renderCustomBuilderServices();
        }
    } catch (e) {
        console.warn('Failed to load services:', e);
    }
}

function renderCustomBuilderServices() {
    const list = document.getElementById('builder-services-list');
    if (!list) return;
    list.innerHTML = '';

    if (state.services.length === 0) {
        list.innerHTML = '<p class="text-muted">No community services currently registered.</p>';
        return;
    }

    state.services.forEach(svc => {
        const item = document.createElement('label');
        item.className = 'service-select-item';
        item.style.cursor = 'pointer';

        const isChecked = state.customBuilder.selectedServices.has(svc.serviceId);

        item.innerHTML = `
            <div style="display: flex; align-items: center; gap: 0.75rem;">
                <input type="checkbox" value="${svc.serviceId}" ${isChecked ? 'checked' : ''} onchange="toggleBuilderService(${svc.serviceId}, this.checked)">
                <div class="service-item-info">
                    <span class="service-item-title">${svc.serviceName}</span>
                    <span class="service-item-sub"><i class="fa-solid fa-store"></i> ${svc.businessName} (by ${svc.stakeholderName})</span>
                </div>
            </div>
            <div style="display: flex; align-items: center;">
                <span class="service-item-rate">₱${Number(svc.basePrice).toFixed(2)} <small style="font-size: 0.7rem; font-weight: normal; color: var(--slate-500);">${formatPricingUnit(svc.pricingUnit)}</small></span>
                <span class="badge badge-success"><i class="fa-solid fa-shield-check"></i> Verified</span>
            </div>
        `;
        list.appendChild(item);
    });

    updateCustomCalculation();
}

function toggleBuilderService(serviceId, isChecked) {
    if (isChecked) {
        state.customBuilder.selectedServices.add(serviceId);
    } else {
        state.customBuilder.selectedServices.delete(serviceId);
    }
    updateCustomCalculation();
}

function updateCustomCalculation() {
    const paxInput = document.getElementById('builder-pax');
    const pax = paxInput ? parseInt(paxInput.value) || 1 : 2;
    state.customBuilder.pax = pax;

    const breakdownContainer = document.getElementById('builder-items-breakdown');
    if (!breakdownContainer) return;
    breakdownContainer.innerHTML = '';

    let grandTotal = 0;
    const selectedList = [];

    state.services.forEach(svc => {
        if (state.customBuilder.selectedServices.has(svc.serviceId)) {
            let cost = 0;
            if (svc.pricingUnit === 'PER_PERSON') {
                cost = Number(svc.basePrice) * pax;
            } else {
                cost = Number(svc.basePrice); // Per group or trip
            }
            grandTotal += cost;
            selectedList.push({ svc, cost });

            const row = document.createElement('div');
            row.className = 'summary-item-row';
            row.innerHTML = `
                <span>${svc.serviceName} <small class="text-muted">(${formatPricingUnit(svc.pricingUnit)})</small></span>
                <span class="font-bold">₱${cost.toFixed(2)}</span>
            `;
            breakdownContainer.appendChild(row);
        }
    });

    if (selectedList.length === 0) {
        breakdownContainer.innerHTML = '<p class="text-muted text-center py-4">Select services above to calculate your custom experience budget.</p>';
    }

    state.customBuilder.total = grandTotal;
    const perPax = pax > 0 ? grandTotal / pax : 0;

    const totalEl = document.getElementById('builder-total-price');
    const perPaxEl = document.getElementById('builder-perpax-price');
    if (totalEl) totalEl.textContent = `₱${grandTotal.toFixed(2)}`;
    if (perPaxEl) perPaxEl.textContent = `₱${perPax.toFixed(2)}`;
}

async function submitCustomBooking() {
    if (state.customBuilder.selectedServices.size === 0) {
        showToast('Please select at least one community service for your custom trip.', 'warning');
        return;
    }

    const start = document.getElementById('builder-date-start')?.value;
    const end = document.getElementById('builder-date-end')?.value;
    const notes = document.getElementById('builder-notes')?.value || '';
    const pax = state.customBuilder.pax;

    if (!start || !end) {
        showToast('Please select travel start and end dates.', 'warning');
        return;
    }

    // Build items
    const items = [];
    state.services.forEach(svc => {
        if (state.customBuilder.selectedServices.has(svc.serviceId)) {
            let subtotal = svc.pricingUnit === 'PER_PERSON' ? Number(svc.basePrice) * pax : Number(svc.basePrice);
            items.push({
                serviceId: svc.serviceId,
                stakeholderId: svc.stakeholderId,
                unitPrice: svc.basePrice,
                quantity: svc.pricingUnit === 'PER_PERSON' ? pax : 1,
                subtotal: subtotal
            });
        }
    });

    const payload = {
        travelDateStart: start,
        travelDateEnd: end,
        numberOfPax: pax,
        totalAmount: state.customBuilder.total,
        customNotes: 'Custom Community Circuit: ' + notes,
        items: items
    };

    try {
        const res = await fetch(`${API_BASE}/bookings`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message || 'Custom booking reserved successfully!', 'success');
            // Reset builder
            state.customBuilder.selectedServices.clear();
            renderCustomBuilderServices();
            switchTab('bookings');
        } else {
            showToast(data.message || 'Booking failed.', 'error');
        }
    } catch (e) {
        showToast('Booking request error: ' + e.message, 'error');
    }
}

// ------------------------------------------------------------------------------
// BOOKINGS LEDGER MODULE
// ------------------------------------------------------------------------------
async function loadBookingsList() {
    try {
        const statusFilter = document.getElementById('bookings-status-filter')?.value || 'ALL';
        const res = await fetch(`${API_BASE}/bookings?status=${statusFilter}`, {
            headers: { 'X-Auth-Token': state.authToken }
        });
        if (res.ok) {
            state.bookings = await res.json();
            renderBookingsTable();
            const counter = document.getElementById('bookings-counter');
            if (counter) counter.textContent = state.bookings.length;
        }
    } catch (e) {
        console.warn('Failed to load bookings:', e);
    }
}

function renderBookingsTable() {
    const tbody = document.getElementById('bookings-table-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (state.bookings.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-4">No booking records found.</td></tr>`;
        return;
    }

    state.bookings.forEach(b => {
        const tr = document.createElement('tr');
        const badgeClass = getStatusBadgeClass(b.status);

        let actionButtons = '';
        const role = state.activeRole;

        // Action options per role
        if (role === 'STAKEHOLDER') {
            if (b.status === 'PENDING') {
                actionButtons = `
                    <button class="btn btn-sm btn-success" onclick="updateBookingStatus(${b.bookingId}, 'ACCEPTED')">Accept</button>
                    <button class="btn btn-sm btn-danger" onclick="updateBookingStatus(${b.bookingId}, 'REJECTED')">Decline</button>
                `;
            } else if (b.status === 'ACCEPTED' || b.status === 'CONFIRMED') {
                actionButtons = `<button class="btn btn-sm btn-outline" onclick="updateBookingStatus(${b.bookingId}, 'COMPLETED')">Mark Done</button>`;
            }
        } else if (role === 'TOURIST') {
            if (b.status === 'COMPLETED') {
                actionButtons = `<button class="btn btn-sm btn-primary" onclick="openReviewModal(${b.bookingId}, ${b.packageId || 1})"><i class="fa-solid fa-star"></i> Leave Review</button>`;
            } else if (b.status === 'PENDING') {
                actionButtons = `<button class="btn btn-sm btn-danger" onclick="updateBookingStatus(${b.bookingId}, 'CANCELLED')">Cancel</button>`;
            }
        } else {
            // Admin, Tourism Office, LGU, Barangay
            actionButtons = `
                <select class="form-select" style="padding: 0.2rem 0.4rem; font-size: 0.75rem; width: auto;" onchange="updateBookingStatus(${b.bookingId}, this.value)">
                    <option value="" disabled selected>Change Status</option>
                    <option value="ACCEPTED">Accept</option>
                    <option value="CONFIRMED">Confirm</option>
                    <option value="COMPLETED">Complete</option>
                    <option value="CANCELLED">Cancel</option>
                </select>
            `;
        }

        tr.innerHTML = `
            <td><strong>${b.bookingReference}</strong></td>
            <td>${b.touristName || 'Tourist Traveler'}</td>
            <td>${b.packageName || 'Custom Experience Circuit'}</td>
            <td>${b.travelDateStart} to ${b.travelDateEnd}</td>
            <td>${b.numberOfPax}</td>
            <td class="font-bold">₱${Number(b.totalAmount).toFixed(2)}</td>
            <td><span class="badge ${badgeClass}">${b.status}</span></td>
            <td>${actionButtons || '<span class="text-muted">—</span>'}</td>
        `;
        tbody.appendChild(tr);
    });
}

async function updateBookingStatus(bookingId, newStatus) {
    if (!newStatus) return;
    try {
        const res = await fetch(`${API_BASE}/bookings/status`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify({ bookingId: bookingId, status: newStatus })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            loadBookingsList();
        } else {
            showToast(data.message || 'Status update failed.', 'error');
        }
    } catch (e) {
        showToast('Error updating booking: ' + e.message, 'error');
    }
}

// ------------------------------------------------------------------------------
// STAKEHOLDERS DIRECTORY MODULE
// ------------------------------------------------------------------------------
async function loadStakeholdersList() {
    try {
        const cat = document.getElementById('stakeholder-category-filter')?.value || 'ALL';
        const st = document.getElementById('stakeholder-status-filter')?.value || 'ALL';
        const res = await fetch(`${API_BASE}/stakeholders?category=${cat}&verificationStatus=${st}`, {
            headers: state.authToken ? { 'X-Auth-Token': state.authToken } : {}
        });
        if (res.ok) {
            const data = await res.json();
            if (Array.isArray(data)) {
                state.stakeholders = data;
                renderStakeholdersGrid();
            }
        }
    } catch (e) {
        console.warn('Failed loading stakeholders:', e);
    }
}

function renderStakeholdersGrid() {
    const grid = document.getElementById('stakeholders-grid');
    if (!grid) return;
    grid.innerHTML = '';

    state.stakeholders.forEach(stk => {
        const card = document.createElement('div');
        card.className = 'item-card';

        card.innerHTML = `
            <div class="card-body">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;">
                    <div>
                        <h3 class="item-card-title">${stk.businessName}</h3>
                        <span class="text-muted" style="font-size: 0.8rem;"><i class="fa-solid fa-user"></i> ${stk.ownerName}</span>
                    </div>
                    <span class="badge ${stk.verificationStatus === 'VERIFIED' ? 'badge-success' : 'badge-warning'}">
                        ${stk.verificationStatus}
                    </span>
                </div>
                <p class="text-muted" style="font-size: 0.82rem; margin-bottom: 0.75rem;">
                    <i class="fa-solid fa-tag"></i> <strong>Category:</strong> ${formatCategory(stk.category)}<br>
                    <i class="fa-solid fa-location-dot"></i> <strong>Barangay:</strong> ${stk.barangayName}, ${stk.municipalityName}<br>
                    <i class="fa-solid fa-id-card"></i> <strong>Accreditation:</strong> ${stk.accreditationNo || 'Pending Submission'}<br>
                    <i class="fa-solid fa-phone"></i> <strong>Contact:</strong> ${stk.contactNumber || 'N/A'}
                </p>
                <div class="item-card-footer">
                    <span class="text-muted" style="font-size: 0.78rem;">Experience: ${stk.yearsExperience} Years</span>
                    <button class="btn btn-outline btn-sm" onclick="promptInquireOrBook('${stk.businessName}', ${stk.profileId})">
                        <i class="fa-solid fa-envelope"></i> Reserve Service
                    </button>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });
}

// ------------------------------------------------------------------------------
// BARANGAY VERIFICATION MODULE
// ------------------------------------------------------------------------------
async function loadVerificationsList() {
    try {
        const res = await fetch(`${API_BASE}/stakeholders?verificationStatus=PENDING`, {
            headers: state.authToken ? { 'X-Auth-Token': state.authToken } : {}
        });
        if (res.ok) {
            const list = await res.json();
            if (Array.isArray(list)) {
                renderVerificationsTable(list);
            }
        }
    } catch (e) {
        console.warn('Failed loading verifications:', e);
    }
}

function renderVerificationsTable(list) {
    const tbody = document.getElementById('verifications-table-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">All local stakeholders in this barangay are verified. No pending reviews.</td></tr>`;
        return;
    }

    list.forEach(stk => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${stk.businessName}</strong><br><small class="text-muted">${stk.ownerName}</small></td>
            <td>${formatCategory(stk.category)}</td>
            <td>Brgy. ${stk.barangayName}</td>
            <td>${stk.accreditationNo || 'LGU Documents Attached'}</td>
            <td><span class="badge badge-warning">PENDING</span></td>
            <td>
                <button class="btn btn-sm btn-primary" onclick="openVerificationModal(${stk.profileId}, '${stk.businessName}', ${stk.barangayId})">
                    <i class="fa-solid fa-stamp"></i> Evaluate
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function openVerificationModal(stkId, name, brgyId) {
    document.getElementById('verify-stakeholder-id').value = stkId;
    document.getElementById('verify-barangay-id').value = brgyId;
    document.getElementById('verify-stakeholder-name').value = name;
    document.getElementById('verify-requirements').value = 'Barangay clearance, valid government ID, training credentials submitted.';
    openModal('modal-verification-action');
}

async function handleVerificationSubmit(e) {
    e.preventDefault();
    const payload = {
        stakeholderId: parseInt(document.getElementById('verify-stakeholder-id').value),
        barangayId: parseInt(document.getElementById('verify-barangay-id').value),
        status: document.getElementById('verify-decision').value,
        requirementsSubmitted: document.getElementById('verify-requirements').value,
        remarks: document.getElementById('verify-remarks').value
    };

    try {
        const res = await fetch(`${API_BASE}/verifications`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-verification-action');
            showToast('Stakeholder evaluation recorded!', 'success');
            loadVerificationsList();
            loadStakeholdersList();
        } else {
            showToast(data.message || 'Verification failed.', 'error');
        }
    } catch (err) {
        showToast('Error recording verification: ' + err.message, 'error');
    }
}

// ------------------------------------------------------------------------------
// REVIEWS & RATINGS MODULE
// ------------------------------------------------------------------------------
async function loadReviewsList() {
    try {
        const res = await fetch(`${API_BASE}/reviews`, {
            headers: state.authToken ? { 'X-Auth-Token': state.authToken } : {}
        });
        if (res.ok) {
            const data = await res.json();
            if (Array.isArray(data)) {
                state.reviews = data;
                renderReviewsList();
            }
        }
    } catch (e) {
        console.warn('Failed loading reviews:', e);
    }
}

function renderReviewsList() {
    const container = document.getElementById('reviews-list-container');
    if (!container) return;
    container.innerHTML = '';

    if (state.reviews.length === 0) {
        container.innerHTML = `<p class="text-muted text-center py-4">No reviews submitted yet.</p>`;
        return;
    }

    state.reviews.forEach(r => {
        const card = document.createElement('div');
        card.className = 'card mb-3';
        card.innerHTML = `
            <div class="card-body">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                    <div>
                        <strong>${r.touristName || 'Verified Tourist'}</strong>
                        <span class="text-muted" style="font-size: 0.78rem; margin-left: 0.5rem;">Reviewed ${r.targetType}</span>
                    </div>
                    <div style="color: #f59e0b;">
                        ${'★'.repeat(r.rating)}${'☆'.repeat(5 - r.rating)}
                    </div>
                </div>
                <p style="font-size: 0.88rem; color: var(--slate-700); margin-bottom: 0.5rem;">${r.comment}</p>
                <small class="text-muted">${r.createdAt || 'Recent review'}</small>
            </div>
        `;
        container.appendChild(card);
    });
}

function openReviewModal(bookingId, targetId) {
    document.getElementById('review-booking-id').value = bookingId;
    document.getElementById('review-target-type').value = 'PACKAGE';
    document.getElementById('review-target-id').value = targetId;
    setRatingVal(5);
    openModal('modal-review');
}

function setRatingVal(val) {
    document.getElementById('review-rating-val').value = val;
    const stars = document.querySelectorAll('#star-selector .star');
    stars.forEach((s, idx) => {
        if (idx < val) s.classList.add('active');
        else s.classList.remove('active');
    });
}

async function handleReviewSubmit(e) {
    e.preventDefault();
    const payload = {
        bookingId: parseInt(document.getElementById('review-booking-id').value),
        targetType: document.getElementById('review-target-type').value,
        targetId: parseInt(document.getElementById('review-target-id').value),
        rating: parseInt(document.getElementById('review-rating-val').value),
        comment: document.getElementById('review-comment').value
    };

    try {
        const res = await fetch(`${API_BASE}/reviews`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-review');
            showToast('Review submitted! Thank you for supporting Biliran tourism.', 'success');
            loadReviewsList();
        } else {
            showToast(data.message || 'Failed to submit review.', 'error');
        }
    } catch (err) {
        showToast('Error publishing review: ' + err.message, 'error');
    }
}

// ------------------------------------------------------------------------------
// TOURISM INFORMATION & ANNOUNCEMENTS MODULE
// ------------------------------------------------------------------------------
async function loadAnnouncementsList() {
    try {
        const res = await fetch(`${API_BASE}/announcements`);
        if (res.ok) {
            state.announcements = await res.json();
            renderAnnouncementsList();
        }
    } catch (e) {
        console.warn('Failed loading announcements:', e);
    }
}

function renderAnnouncementsList() {
    const grid = document.getElementById('announcements-cards-grid');
    if (!grid) return;
    grid.innerHTML = '';

    state.announcements.forEach(a => {
        const card = document.createElement('div');
        card.className = 'card mb-3';
        card.innerHTML = `
            <div class="card-header">
                <h3 class="card-title"><i class="fa-solid fa-bullhorn" style="color: var(--accent-600);"></i> ${a.title}</h3>
                <span class="badge badge-info">${formatCategory(a.category)}</span>
            </div>
            <div class="card-body">
                <p style="font-size: 0.88rem; line-height: 1.5; color: var(--slate-700); margin-bottom: 0.75rem;">${a.content}</p>
                <div style="font-size: 0.78rem; color: var(--slate-500); display: flex; justify-content: space-between;">
                    <span><i class="fa-solid fa-user-tie"></i> Published by: ${a.publisherName || 'Tourism Office'}</span>
                    <span><i class="fa-solid fa-calendar"></i> Date: ${a.publishDate}</span>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });
}

// ------------------------------------------------------------------------------
// REPORTS & MONITORING MODULE
// ------------------------------------------------------------------------------
async function loadReportsData() {
    try {
        const headers = state.authToken ? { 'X-Auth-Token': state.authToken } : {};
        const [resFootfall, resBrgy] = await Promise.all([
            fetch(`${API_BASE}/reports/footfall`, { headers }),
            fetch(`${API_BASE}/reports/barangays`, { headers })
        ]);

        if (resFootfall.ok) {
            const footfall = await resFootfall.json();
            if (Array.isArray(footfall)) renderFootfallTable(footfall);
        }
        if (resBrgy.ok) {
            const brgyData = await resBrgy.json();
            if (Array.isArray(brgyData)) renderBarangayReportsTable(brgyData);
        }
    } catch (e) {
        console.warn('Failed loading reports:', e);
    }
}

function renderFootfallTable(data) {
    const tbody = document.getElementById('footfall-report-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    data.forEach(row => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${row.destination_name}</strong></td>
            <td>${formatCategory(row.category)}</td>
            <td>${row.municipality_name} (${row.barangay_name})</td>
            <td>${row.total_local_visitors}</td>
            <td>${row.total_domestic_visitors}</td>
            <td>${row.total_foreign_visitors}</td>
            <td class="font-bold">${row.grand_total_visitors}</td>
            <td class="font-bold">₱${Number(row.total_revenue_generated).toFixed(2)}</td>
            <td><i class="fa-solid fa-star text-amber" style="color:#f59e0b"></i> ${row.average_rating}</td>
        `;
        tbody.appendChild(tr);
    });
}

function renderBarangayReportsTable(data) {
    const tbody = document.getElementById('barangay-report-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    data.forEach(b => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${b.barangay_name}</strong></td>
            <td>${b.municipality_name}</td>
            <td>${b.contact_person || 'Barangay Council'}</td>
            <td>${b.contact_number || 'N/A'}</td>
            <td>${b.total_destinations}</td>
            <td>${b.total_stakeholders}</td>
            <td><span class="badge badge-success">${b.verified_stakeholders} Verified</span></td>
        `;
        tbody.appendChild(tr);
    });
}

function exportReportCSV() {
    let csv = "Destination,Category,Municipality,Barangay,Local,Domestic,Foreign,Total Footfall,Revenue PHP\n";
    const rows = document.querySelectorAll('#footfall-report-body tr');
    rows.forEach(tr => {
        const cols = Array.from(tr.querySelectorAll('td')).map(td => `"${td.textContent.trim()}"`);
        csv += cols.join(",") + "\n";
    });

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Lakbay_Lokal_Tourism_Report_${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    showToast('Report CSV exported successfully!', 'success');
}

function printReport() {
    window.print();
}

function openArrivalEntryModal() {
    openModal('modal-arrival-entry');
}

async function handleArrivalSubmit(e) {
    e.preventDefault();
    const payload = {
        destinationId: parseInt(document.getElementById('arr-destination-id').value),
        logDate: document.getElementById('arr-date').value,
        localTouristsCount: parseInt(document.getElementById('arr-local').value) || 0,
        domesticTouristsCount: parseInt(document.getElementById('arr-domestic').value) || 0,
        foreignTouristsCount: parseInt(document.getElementById('arr-foreign').value) || 0,
        totalRevenue: parseFloat(document.getElementById('arr-revenue').value) || 0
    };

    try {
        const res = await fetch(`${API_BASE}/reports/arrival`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-arrival-entry');
            showToast('Visitor arrival count logged!', 'success');
            loadReportsData();
            loadKPIs();
        } else {
            showToast(data.message || 'Log failed', 'error');
        }
    } catch (err) {
        showToast('Error recording arrival: ' + err.message, 'error');
    }
}

// ------------------------------------------------------------------------------
// SYSTEM ADMINISTRATION MODULE
// ------------------------------------------------------------------------------
async function loadAdminData() {
    try {
        const [resUsers, resLogs] = await Promise.all([
            fetch(`${API_BASE}/admin/users`, { headers: { 'X-Auth-Token': state.authToken } }),
            fetch(`${API_BASE}/admin/logs?limit=40`, { headers: { 'X-Auth-Token': state.authToken } })
        ]);

        if (resUsers.ok) {
            const users = await resUsers.json();
            renderUsersTable(users);
        }
        if (resLogs.ok) {
            const logs = await resLogs.json();
            renderAuditLogsTable(logs);
        }
    } catch (e) {
        console.warn('Failed loading admin data:', e);
    }
}

function renderUsersTable(users) {
    const tbody = document.getElementById('users-table-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    users.forEach(u => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${u.username}</strong></td>
            <td>${u.fullName}</td>
            <td>${u.email}</td>
            <td><span class="user-role-badge">${u.roleCode}</span></td>
            <td>${u.barangayName ? `Brgy. ${u.barangayName}` : 'Province-wide'}</td>
            <td><span class="badge ${u.status === 'ACTIVE' ? 'badge-success' : 'badge-danger'}">${u.status}</span></td>
            <td>
                <button class="btn btn-sm btn-outline" onclick="toggleUserStatus(${u.userId}, '${u.status}')">
                    ${u.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function renderAuditLogsTable(logs) {
    const tbody = document.getElementById('audit-table-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    logs.forEach(l => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td style="font-size: 0.78rem;">${l.timestamp}</td>
            <td><strong>${l.username}</strong></td>
            <td><span class="badge badge-info">${l.action}</span></td>
            <td>${l.entityName}</td>
            <td style="font-family: monospace;">${l.ipAddress}</td>
            <td style="font-size: 0.8rem; color: var(--slate-600);">${l.details}</td>
        `;
        tbody.appendChild(tr);
    });
}

async function toggleUserStatus(userId, currentStatus) {
    const newStatus = currentStatus === 'ACTIVE' ? 'DEACTIVATED' : 'ACTIVE';
    try {
        const res = await fetch(`${API_BASE}/admin/users/status`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify({ userId: userId, status: newStatus })
        });
        const data = await res.json();
        if (data.success) {
            showToast(`User status set to ${newStatus}`, 'success');
            loadAdminData();
        }
    } catch (e) {
        showToast('Error updating status: ' + e.message, 'error');
    }
}

async function triggerDbBackup() {
    try {
        showToast('Generating database backup dump...', 'info');
        const res = await fetch(`${API_BASE}/admin/database/backup`, {
            method: 'POST',
            headers: { 'X-Auth-Token': state.authToken }
        });
        const data = await res.json();
        if (data.success) {
            showToast(`Backup created: ${data.fileName}`, 'success');
            loadAdminData();
        } else {
            showToast(data.message || 'Backup failed.', 'error');
        }
    } catch (e) {
        showToast('Backup request error: ' + e.message, 'error');
    }
}

// ------------------------------------------------------------------------------
// OVERVIEW SCREEN HELPERS
// ------------------------------------------------------------------------------
function refreshDashboardData() {
    loadKPIs();
    renderOverviewTopDestinations();
    renderOverviewRecentActivity();
    showToast('Dashboard data refreshed.', 'info');
}

function renderOverviewTopDestinations() {
    const container = document.getElementById('overview-top-destinations');
    if (!container) return;
    container.innerHTML = '';

    const topList = state.destinations.slice(0, 4);
    topList.forEach(d => {
        const div = document.createElement('div');
        div.style.display = 'flex';
        div.style.justifyContent = 'space-between';
        div.style.alignItems = 'center';
        div.style.padding = '0.65rem 0';
        div.style.borderBottom = '1px solid var(--slate-100)';
        div.innerHTML = `
            <div>
                <strong>${d.name}</strong><br>
                <small class="text-muted"><i class="fa-solid fa-location-dot"></i> Brgy. ${d.barangayName}, ${d.municipalityName}</small>
            </div>
            <div style="text-align: right;">
                <span class="font-bold" style="color: var(--primary-700);">₱${Number(d.entranceFee).toFixed(2)}</span><br>
                <small><i class="fa-solid fa-star" style="color: #f59e0b"></i> ${d.averageRating || '5.0'}</small>
            </div>
        `;
        container.appendChild(div);
    });
}

async function renderOverviewRecentActivity() {
    const container = document.getElementById('overview-recent-activity');
    if (!container) return;
    try {
        const res = await fetch(`${API_BASE}/admin/logs?limit=5`, {
            headers: { 'X-Auth-Token': state.authToken }
        });
        if (res.ok) {
            const logs = await res.json();
            container.innerHTML = '';
            logs.forEach(l => {
                const item = document.createElement('div');
                item.style.padding = '0.45rem 0';
                item.style.borderBottom = '1px solid var(--slate-100)';
                item.innerHTML = `
                    <div style="display: flex; justify-content: space-between; font-size: 0.78rem;">
                        <strong>${l.username}</strong>
                        <span class="text-muted">${l.timestamp}</span>
                    </div>
                    <div style="font-size: 0.82rem; color: var(--slate-700);">${l.details}</div>
                `;
                container.appendChild(item);
            });
        }
    } catch (e) {
        console.warn('Failed loading activity:', e);
    }
}

// ------------------------------------------------------------------------------
// PACKAGE BOOKING MODAL HELPER
// ------------------------------------------------------------------------------
function openPackageBookingModal(packageId, name, price) {
    document.getElementById('book-package-id').value = packageId;
    document.getElementById('book-package-name').value = name;
    document.getElementById('book-pax').value = 2;

    const today = new Date().toISOString().split('T')[0];
    document.getElementById('book-date-start').value = today;
    document.getElementById('book-date-end').value = today;

    calculatePackageBookingTotal();
    openModal('modal-booking');
}

function calculatePackageBookingTotal() {
    const pax = parseInt(document.getElementById('book-pax')?.value) || 1;
    const pkgId = parseInt(document.getElementById('book-package-id')?.value);
    const pkg = state.packages.find(p => p.packageId === pkgId);
    const rate = pkg ? Number(pkg.pricePerPax) : 1000;
    const total = rate * pax;

    const el = document.getElementById('book-total-amount');
    if (el) el.textContent = `₱${total.toFixed(2)}`;
}

async function handlePackageBookingSubmit(e) {
    e.preventDefault();
    const pkgId = parseInt(document.getElementById('book-package-id').value);
    const pax = parseInt(document.getElementById('book-pax').value);
    const pkg = state.packages.find(p => p.packageId === pkgId);
    const rate = pkg ? Number(pkg.pricePerPax) : 1000;

    const payload = {
        packageId: pkgId,
        travelDateStart: document.getElementById('book-date-start').value,
        travelDateEnd: document.getElementById('book-date-end').value,
        numberOfPax: pax,
        totalAmount: rate * pax,
        customNotes: document.getElementById('book-notes').value
    };

    try {
        const res = await fetch(`${API_BASE}/bookings`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-booking');
            showToast(`Booking submitted! Ref: ${data.reference}`, 'success');
            loadBookingsList();
            switchTab('bookings');
        } else {
            showToast(data.message || 'Booking failed', 'error');
        }
    } catch (err) {
        showToast('Error booking package: ' + err.message, 'error');
    }
}

// ------------------------------------------------------------------------------
// MODAL CONTROLS & UTILITIES
// ------------------------------------------------------------------------------
function openModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.add('open');
}

function closeModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.remove('open');
}

function openLoginModal() { openModal('modal-login'); }
function openRegisterModal() { openModal('modal-register'); }
function openAddDestinationModal() { openModal('modal-add-destination'); }
function openAddServiceModal() { openModal('modal-add-service'); }
function openAddAnnouncementModal() { openModal('modal-add-announcement'); }
function openCreateUserModal() { openModal('modal-create-user'); }
function openAddPackageModal() {
    togglePackageSubTab('custom');
    showToast('Select community services below to build a tourism package.', 'info');
}

async function handleAddDestinationSubmit(e) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('dest-form-name').value,
        category: document.getElementById('dest-form-category').value,
        barangayId: parseInt(document.getElementById('dest-form-barangay').value),
        description: document.getElementById('dest-form-desc').value,
        entranceFee: parseFloat(document.getElementById('dest-form-fee').value),
        operatingHours: document.getElementById('dest-form-hours').value,
        locationCoordinates: document.getElementById('dest-form-coords').value,
        contactInfo: document.getElementById('dest-form-contact').value,
        status: 'ACTIVE'
    };

    try {
        const res = await fetch(`${API_BASE}/destinations`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-add-destination');
            showToast('Destination saved successfully!', 'success');
            loadDestinations();
        } else {
            showToast(data.message || 'Failed saving destination.', 'error');
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    let icon = '<i class="fa-solid fa-info-circle"></i>';
    if (type === 'success') icon = '<i class="fa-solid fa-check-circle"></i>';
    if (type === 'error') icon = '<i class="fa-solid fa-triangle-exclamation"></i>';
    if (type === 'warning') icon = '<i class="fa-solid fa-bell"></i>';

    toast.innerHTML = `${icon} <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function formatCategory(cat) {
    if (!cat) return '';
    return cat.replace(/_/g, ' ').replace(/\w\S*/g, (txt) => txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase());
}

function formatPricingUnit(unit) {
    if (!unit) return '';
    if (unit === 'PER_PERSON') return '/ person';
    if (unit === 'PER_GROUP') return '/ group';
    if (unit === 'PER_TRIP') return '/ trip';
    if (unit === 'PER_ITEM') return '/ item';
    return unit;
}

function getStatusBadgeClass(status) {
    if (status === 'CONFIRMED' || status === 'COMPLETED' || status === 'VERIFIED') return 'badge-success';
    if (status === 'PENDING') return 'badge-warning';
    if (status === 'CANCELLED' || status === 'REJECTED') return 'badge-danger';
    return 'badge-info';
}

// ------------------------------------------------------------------------------
// PUBLIC HOMEPAGE RENDERING & INTERACTIVE SEARCH
// ------------------------------------------------------------------------------
function getDestinationImage(dest) {
    if (!dest) return 'assets/sambawan_island_hero_1791699785896.jpg';
    const name = (dest.name || '').toLowerCase();
    if (name.includes('sambawan')) return 'assets/sambawan_island_hero_1791699785896.jpg';
    if (name.includes('tinago')) return 'assets/tinago_falls_hero_1791699805710.jpg';
    if (name.includes('ulan')) return 'assets/ulan_ulan_falls_hero.jpg';
    if (name.includes('higatangan')) return 'assets/higatangan_sandbar_hero.jpg';
    if (name.includes('reco')) return 'assets/ulan_ulan_falls_hero.jpg';
    if (name.includes('mainit')) return 'assets/tinago_falls_hero_1791699805710.jpg';
    if (name.includes('pottery') || name.includes('maripipi')) return 'assets/sambawan_island_hero_1791699785896.jpg';
    if (name.includes('iyusan')) return 'assets/ulan_ulan_falls_hero.jpg';
    if (dest.imageUrl && (dest.imageUrl.startsWith('assets/') || dest.imageUrl.startsWith('http'))) return dest.imageUrl;
    return 'assets/sambawan_island_hero_1791699785896.jpg';
}

function updateHeroCounters() {
    const dCount = document.getElementById('stat-hero-dest-count');
    const sCount = document.getElementById('stat-hero-stakeholder-count');
    if (dCount && state.destinations.length > 0) dCount.textContent = state.destinations.length;
    if (sCount && state.stakeholders.length > 0) sCount.textContent = state.stakeholders.length;
}

function renderPublicDestinations(list) {
    const grid = document.getElementById('public-destinations-grid');
    if (!grid) return;
    grid.innerHTML = '';

    const items = (list && list.length > 0) ? list : state.destinations;
    if (!items || items.length === 0) {
        grid.innerHTML = `<div class="card p-4 text-center text-muted" style="grid-column: 1/-1;">No destinations matched your search. Try different keywords!</div>`;
        return;
    }

    items.forEach(d => {
        const card = document.createElement('div');
        card.className = 'public-dest-card';
        const img = getDestinationImage(d);
        const fee = Number(d.entranceFee || 0).toFixed(2);
        const rating = d.averageRating || '5.0';
        const bName = d.barangayName ? `Brgy. ${d.barangayName}` : 'Biliran Province';

        card.innerHTML = `
            <div class="public-dest-thumb">
                <img src="${img}" alt="${d.name}" onerror="this.src='assets/sambawan_island_hero_1791699785896.jpg'">
                <span class="public-dest-cat-badge">${formatCategory(d.category)}</span>
                <span class="public-dest-fee-tag">₱${fee} entrance</span>
            </div>
            <div class="public-dest-body">
                <h3 class="public-dest-title">${d.name}</h3>
                <div class="public-dest-location">
                    <i class="fa-solid fa-location-dot" style="color: var(--primary-600)"></i>
                    <span>${bName}</span>
                    <span style="margin-left: auto; color: #f59e0b; font-weight: 700;">
                        <i class="fa-solid fa-star"></i> ${rating}
                    </span>
                </div>
                <p class="public-dest-desc">${d.description || 'Pristine natural destination in Biliran Province.'}</p>
                <div class="public-dest-footer">
                    <span style="font-size: 0.8rem; color: var(--slate-500);"><i class="fa-solid fa-clock"></i> ${d.operatingHours || '6:00 AM - 5:00 PM'}</span>
                    <button class="btn btn-primary btn-sm" onclick="handlePublicDestinationAction(${d.destinationId})">
                        <i class="fa-solid fa-compass"></i> Explore / Book Guide
                    </button>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });
}

function renderPublicPackages(list) {
    const grid = document.getElementById('public-packages-grid');
    if (!grid) return;
    grid.innerHTML = '';

    const items = (list && list.length > 0) ? list : state.packages;
    if (!items || !Array.isArray(items) || items.length === 0) return;

    items.forEach(p => {
        const card = document.createElement('div');
        card.className = 'public-dest-card';
        const price = Number(p.pricePerPax || 0).toFixed(2);
        const pkgName = p.name || p.title || 'Community Tourism Package';
        let img = 'assets/sambawan_island_hero_1791699785896.jpg';
        const title = pkgName.toLowerCase();
        if (title.includes('waterfall') || title.includes('ulan')) img = 'assets/ulan_ulan_falls_hero.jpg';
        else if (title.includes('sandbar') || title.includes('higatangan')) img = 'assets/higatangan_sandbar_hero.jpg';
        else if (title.includes('tinago')) img = 'assets/tinago_falls_hero_1791699805710.jpg';

        const safePkgName = pkgName.replace(/'/g, "\\'");

        card.innerHTML = `
            <div class="public-dest-thumb">
                <img src="${img}" alt="${pkgName}" onerror="this.src='assets/sambawan_island_hero_1791699785896.jpg'">
                <span class="public-dest-cat-badge"><i class="fa-solid fa-clock"></i> ${p.durationDays || 1} Day(s)</span>
                <span class="public-dest-fee-tag">₱${price} / pax</span>
            </div>
            <div class="public-dest-body">
                <h3 class="public-dest-title">${pkgName}</h3>
                <p class="public-dest-desc">${p.description || 'Curated community tourism circuit with accredited guides.'}</p>
                <div class="public-dest-footer">
                    <span style="font-size: 0.82rem; font-weight: 600; color: var(--primary-700);"><i class="fa-solid fa-users"></i> Min ${p.minPax || 2} Pax</span>
                    <button class="btn btn-primary btn-sm" onclick="handlePublicPackageAction(${p.packageId}, '${safePkgName}', ${price})">
                        <i class="fa-solid fa-receipt"></i> Reserve Now
                    </button>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });
}

function handlePublicSearch() {
    const q = (document.getElementById('public-hero-search-input')?.value || '').toLowerCase();
    const cat = document.getElementById('public-hero-cat-filter')?.value || 'ALL';
    const muni = document.getElementById('public-hero-muni-filter')?.value || 'ALL';

    const filtered = state.destinations.filter(d => {
        const matchQ = !q || d.name.toLowerCase().includes(q) || (d.description && d.description.toLowerCase().includes(q));
        const matchCat = cat === 'ALL' || d.category === cat;
        const matchMuni = muni === 'ALL' || (d.municipalityName && d.municipalityName.toLowerCase() === muni.toLowerCase());
        return matchQ && matchCat && matchMuni;
    });

    renderPublicDestinations(filtered);
}

function filterPublicCategory(cat) {
    const pills = document.querySelectorAll('#public-category-pills .cat-pill');
    pills.forEach(p => {
        if (p.getAttribute('data-cat') === cat) p.classList.add('active');
        else p.classList.remove('active');
    });

    const select = document.getElementById('public-hero-cat-filter');
    if (select) select.value = cat;

    if (cat === 'ALL') {
        renderPublicDestinations(state.destinations);
    } else {
        const filtered = state.destinations.filter(d => d.category === cat);
        renderPublicDestinations(filtered);
    }
}

function handlePublicDestinationAction(destId) {
    const dest = state.destinations.find(d => d.destinationId === destId);
    if (!state.currentUser) {
        showToast('Please sign in or select a quick role to book guided services for ' + (dest?.name || 'this destination') + '.', 'info');
        openGlassLogin();
    } else {
        showManagementDashboard('packages');
        showToast(`Viewing packages & guides for ${dest?.name || 'destination'}.`, 'info');
    }
}

function handlePublicPackageAction(packageId, title, price) {
    if (!state.currentUser) {
        showToast(`Sign in to reserve "${title}".`, 'info');
        openGlassLogin();
    } else {
        openPackageBookingModal(packageId, title, price);
    }
}

// ------------------------------------------------------------------------------
// SUPPORTING 16-MODULE TABLE LOADERS & ACTIONS
// ------------------------------------------------------------------------------
async function loadServicesTable() {
    const tbody = document.getElementById('services-table-body');
    if (!tbody) return;
    try {
        const res = await fetch(`${API_BASE}/stakeholders/services`);
        if (res.ok) {
            state.services = await res.json();
            tbody.innerHTML = '';
            if (state.services.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">No services registered yet.</td></tr>';
                return;
            }
            state.services.forEach(s => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${s.serviceName}</strong></td>
                    <td>${s.stakeholderName || 'Local Stakeholder'}</td>
                    <td><span class="badge badge-info">${formatCategory(s.serviceCategory)}</span></td>
                    <td><strong style="color: var(--primary-700)">₱${Number(s.basePrice).toFixed(2)}</strong> <small class="text-muted">${formatPricingUnit(s.pricingUnit)}</small></td>
                    <td>${s.maxCapacity || 1} pax</td>
                    <td><span class="badge ${s.isAvailable ? 'badge-success' : 'badge-danger'}">${s.isAvailable ? 'Available' : 'Unavailable'}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline" onclick="openPackageBookingModal(null, '${s.serviceName.replace(/'/g, "\\'")}', ${s.basePrice})">Book</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (e) {
        console.warn('Failed loading services table:', e);
    }
}

async function handleAddServiceSubmit(e) {
    e.preventDefault();
    const payload = {
        serviceName: document.getElementById('svc-form-name').value,
        serviceCategory: document.getElementById('svc-form-category').value,
        pricingUnit: document.getElementById('svc-form-unit').value,
        basePrice: parseFloat(document.getElementById('svc-form-price').value),
        maxCapacity: parseInt(document.getElementById('svc-form-capacity').value),
        description: document.getElementById('svc-form-desc').value,
        isAvailable: true
    };

    try {
        const res = await fetch(`${API_BASE}/stakeholders/services`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-add-service');
            showToast('Service offering saved successfully!', 'success');
            loadServicesTable();
            loadStakeholderServices();
        } else {
            showToast(data.message || 'Failed saving service.', 'error');
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

async function loadBarangayRecordsTable() {
    const tbody = document.getElementById('barangay-full-body');
    if (!tbody) return;
    try {
        const res = await fetch(`${API_BASE}/reports/barangays`, {
            headers: state.authToken ? { 'X-Auth-Token': state.authToken } : {}
        });
        if (res.ok) {
            const data = await res.json();
            if (!Array.isArray(data)) return;
            tbody.innerHTML = '';
            data.forEach(b => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${b.barangay_name || b.barangayName}</strong></td>
                    <td>${b.municipality_name || b.municipalityName}</td>
                    <td>${b.contact_person || b.contactPerson || 'Barangay Captain'}</td>
                    <td>${b.contact_number || b.contactNumber || 'N/A'}</td>
                    <td><span class="badge badge-info">${b.total_destinations ?? b.destinationCount ?? 0}</span></td>
                    <td><span class="badge badge-info">${b.total_stakeholders ?? b.stakeholderCount ?? 0}</span></td>
                    <td><span class="badge badge-success">Active Registered</span></td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (e) {
        console.warn('Failed loading barangay table:', e);
    }
}

function saveSystemSettings() {
    const threshold = document.getElementById('setting-capacity-threshold')?.value;
    const msg = document.getElementById('setting-broadcast-msg')?.value;
    if (msg) {
        const notice = document.getElementById('notice-text');
        if (notice) notice.textContent = msg;
    }
    showToast(`System settings saved! Carrying capacity alert set to ${threshold}%.`, 'success');
}

async function loadAuditLogs() {
    const tbody = document.getElementById('audit-table-body');
    if (!tbody) return;
    try {
        const res = await fetch(`${API_BASE}/admin/logs?limit=50`, {
            headers: { 'X-Auth-Token': state.authToken }
        });
        if (res.ok) {
            const logs = await res.json();
            if (!Array.isArray(logs)) return;
            tbody.innerHTML = '';
            logs.forEach(l => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><small class="text-muted">${l.timestamp}</small></td>
                    <td><strong>${l.username}</strong></td>
                    <td><span class="badge badge-info">${l.action}</span></td>
                    <td>${l.entityName || l.targetEntity || '-'}</td>
                    <td><code>${l.ipAddress || '127.0.0.1'}</code></td>
                    <td>${l.details}</td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (e) {
        console.warn('Failed loading audit logs:', e);
    }
}

async function handleAddAnnouncementSubmit(e) {
    e.preventDefault();
    const payload = {
        title: document.getElementById('anno-form-title').value,
        type: document.getElementById('anno-form-type').value,
        content: document.getElementById('anno-form-content').value,
        priority: 'NORMAL'
    };

    try {
        const res = await fetch(`${API_BASE}/announcements`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-add-announcement');
            showToast('Announcement posted successfully!', 'success');
            loadAnnouncementsList();
        } else {
            showToast(data.message || 'Failed posting announcement.', 'error');
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

async function handleCreateUserSubmit(e) {
    e.preventDefault();
    const brgyVal = document.getElementById('user-form-barangay').value;
    const payload = {
        fullName: document.getElementById('user-form-fullname').value,
        username: document.getElementById('user-form-username').value,
        password: document.getElementById('user-form-password').value,
        email: document.getElementById('user-form-email').value,
        roleId: parseInt(document.getElementById('user-form-role').value),
        barangayId: brgyVal ? parseInt(brgyVal) : null,
        status: 'ACTIVE'
    };

    try {
        const res = await fetch(`${API_BASE}/admin/users`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-create-user');
            showToast('User account created successfully!', 'success');
            loadAdminData();
        } else {
            showToast(data.message || 'Failed creating user.', 'error');
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}
