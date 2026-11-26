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
        loadAnnouncementsList(),
        loadSystemSettings()
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
        if (titlebar) titlebar.style.display = 'none';
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
            if (user && user.userId && user.roleCode && user.success !== false && user.code !== 401) {
                setAuthenticatedUser(user, state.authToken);
                return true;
            }
        }
        // Token invalid or expired in database
        state.authToken = null;
        state.currentUser = null;
        state.activeRole = null;
        localStorage.removeItem('lakbay_token');
        return false;
    } catch (e) {
        console.warn('Session verification notice:', e);
        return false;
    }
}

function openGlassLogin() {
    showLoginView();
}

function setAuthenticatedUser(user, token) {
    state.currentUser = user;
    state.authToken = token;
    state.activeRole = user.roleCode;
    if (token) localStorage.setItem('lakbay_token', token);

    // Update Header user info & role badge
    const hName = document.getElementById('header-user-name');
    const hBadge = document.getElementById('header-role-badge');
    const hRoleLabel = document.getElementById('header-user-role-label');
    const hScopeLabel = document.getElementById('header-user-scope-label');
    if (hName) hName.textContent = user.fullName || user.username;
    if (hBadge) hBadge.textContent = user.roleName || user.roleCode;
    if (hRoleLabel) hRoleLabel.textContent = user.roleName || formatCategory(user.roleCode);
    if (hScopeLabel) {
        if (user.barangayName) {
            hScopeLabel.textContent = `Brgy. ${user.barangayName}`;
            hScopeLabel.style.display = 'inline';
        } else if (user.municipalityName) {
            hScopeLabel.textContent = user.municipalityName;
            hScopeLabel.style.display = 'inline';
        } else {
            hScopeLabel.style.display = 'none';
        }
    }

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
            // 4.A: Tourist Homepage - Direct to Tourist Dashboard with exact 6 modules
            if (publicPortal) publicPortal.classList.remove('active');
            if (mgmtPortal) mgmtPortal.classList.add('active');
            if (banner) {
                banner.style.display = 'flex';
                if (bTag) bTag.innerHTML = '<i class="fa-solid fa-umbrella-beach"></i> Tourist Experience Workspace';
                if (bTitle) bTitle.textContent = `Welcome, ${state.currentUser.fullName || state.currentUser.username}`;
                if (bDesc) bDesc.textContent = 'Discover approved destinations, customize itineraries, explore verified community providers, and track bookings.';
                if (bScope) bScope.textContent = 'Tourist Personal Workspace';
            }
            switchTab('overview');
            break;

        case 'STAKEHOLDER':
            // 4.B: Local Tourism Stakeholder Homepage - Overview Dashboard & assigned bookings
            if (publicPortal) publicPortal.classList.remove('active');
            if (mgmtPortal) mgmtPortal.classList.add('active');
            if (banner) {
                banner.style.display = 'flex';
                if (bTag) bTag.innerHTML = '<i class="fa-solid fa-handshake-angle"></i> Local Tourism Stakeholder Desk';
                if (bTitle) bTitle.textContent = `Welcome, ${state.currentUser.fullName || state.currentUser.username}`;
                if (bDesc) bDesc.textContent = 'Maintain your tourism offerings, daily rates, availability, and guest reservations in Biliran.';
                if (bScope) bScope.textContent = state.currentUser.municipalityName ? `${state.currentUser.municipalityName} Stakeholder` : 'Community Provider';
            }
            switchTab('overview');
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
    const errBanner = document.getElementById('tourist-reg-error');
    const showErr = (msg) => {
        if (errBanner) { errBanner.textContent = msg; errBanner.style.display = ''; }
        showToast(msg, 'error');
    };

    const fullName = document.getElementById('reg-fullname').value.trim();
    const username = document.getElementById('reg-username').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const confirmPwd = document.getElementById('reg-confirmpassword').value;
    const phone = document.getElementById('reg-phone').value.trim();
    const originCity = document.getElementById('reg-origincity').value.trim();

    if (errBanner) errBanner.style.display = 'none';

    if (!fullName || !username || !email || !password) {
        return showErr('Please fill in all required fields.');
    }
    if (password.length < 6) {
        return showErr('Password must be at least 6 characters.');
    }
    if (password !== confirmPwd) {
        return showErr('Passwords do not match. Please re-enter your password.');
    }

    const btn = document.getElementById('btn-tourist-reg-submit');
    if (btn) btn.disabled = true;

    const payload = { fullName, email, username, password, phone, originCity };

    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-register');
            showToast('Tourist account created! Please sign in with your new credentials.', 'success');
            fillLoginForm(username);
        } else {
            showErr(data.message || 'Registration failed. Please try again.');
        }
    } catch (err) {
        showErr('Registration error: ' + err.message);
    } finally {
        if (btn) btn.disabled = false;
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
// STAKEHOLDER REGISTRATION & VERIFICATION NOTICE
// ------------------------------------------------------------------------------
async function handleStakeholderRegisterSubmit(e) {
    e.preventDefault();
    const errBanner = document.getElementById('stakeholder-reg-error');
    const showErr = (msg) => {
        if (errBanner) { errBanner.textContent = msg; errBanner.style.display = ''; }
        showToast(msg, 'error');
    };

    const fullName    = document.getElementById('stkreg-fullname').value.trim();
    const username    = document.getElementById('stkreg-username').value.trim();
    const email       = document.getElementById('stkreg-email').value.trim();
    const password    = document.getElementById('stkreg-password').value;
    const confirmPwd  = document.getElementById('stkreg-confirmpassword').value;
    const businessName = document.getElementById('stkreg-businessname').value.trim();
    const category    = document.getElementById('stkreg-category').value;
    const municipalityId = parseInt(document.getElementById('stkreg-municipality').value);
    const barangayId  = parseInt(document.getElementById('stkreg-barangay').value);
    const phone       = document.getElementById('stkreg-phone').value.trim();
    const accreditationNo = document.getElementById('stkreg-permit').value.trim();
    const bio         = document.getElementById('stkreg-bio').value.trim();
    const yearsExperience = parseInt(document.getElementById('stkreg-yearsexp').value) || 1;

    if (errBanner) errBanner.style.display = 'none';

    // Client-side validation
    if (!fullName || !username || !email || !password || !businessName || !category || !phone || !bio) {
        return showErr('Please fill in all required fields marked with *.');
    }
    if (password.length < 6) {
        return showErr('Password must be at least 6 characters.');
    }
    if (password !== confirmPwd) {
        return showErr('Passwords do not match. Please re-enter your password.');
    }
    if (!municipalityId || isNaN(municipalityId)) {
        return showErr('Please select a municipality.');
    }
    if (!barangayId || isNaN(barangayId)) {
        return showErr('Please select a barangay.');
    }

    const btn = document.getElementById('btn-stkreg-submit');
    if (btn) btn.disabled = true;

    // Server enforces: role = STAKEHOLDER, verificationStatus = PENDING, no bypass
    const payload = {
        fullName, username, email, password,
        businessName, category, municipalityId, barangayId,
        phone, accreditationNo, bio, yearsExperience
    };

    try {
        const res = await fetch(`${API_BASE}/auth/register-stakeholder`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-register');
            showToast(
                'Stakeholder application submitted! Your account is PENDING review by your Barangay Official. You may log in now.',
                'success'
            );
            fillLoginForm(username);
        } else {
            showErr(data.message || 'Registration failed. Please check your details and try again.');
        }
    } catch (err) {
        showErr('Registration error: ' + err.message);
    } finally {
        if (btn) btn.disabled = false;
    }
}

/**
 * Shows a pending verification banner for STAKEHOLDER role users,
 * and disables the "Add Service" button if not yet VERIFIED.
 */
async function checkStakeholderVerificationNotice() {
    if (state.activeRole !== 'STAKEHOLDER') return;
    try {
        const res = await fetch(`${API_BASE}/stakeholders/my-verification`, {
            headers: { 'X-Auth-Token': state.authToken }
        });
        if (!res.ok) return;
        const data = await res.json();
        const status = data.verificationStatus;

        // Render notice in overview and services pane
        const overviewBanner = document.getElementById('overview-verification-warning');
        const servicesBanner = document.getElementById('services-verification-warning');
        const addServiceBtn = document.getElementById('btn-add-service');

        if (status === 'VERIFIED') {
            // All clear — remove any notice
            if (overviewBanner) overviewBanner.innerHTML = '';
            if (servicesBanner) servicesBanner.innerHTML = '';
            if (addServiceBtn) {
                addServiceBtn.disabled = false;
                addServiceBtn.classList.remove('btn-disabled-notice');
            }
            return;
        }

        // Not yet verified: show banner and disable Add Service
        const isNeedsCorrection = status === 'NEEDS_CORRECTION';
        const bannerClass = isNeedsCorrection ? 'warning' : 'pending';
        const icon = isNeedsCorrection ? '⚠️' : '🕐';
        const title = isNeedsCorrection ? 'Corrections Required' : 'Verification Pending';
        const remark = data.latestRemarks ? `<p><strong>Official Remarks:</strong> ${data.latestRemarks}</p>` : '';
        const noteText = isNeedsCorrection
            ? 'Your application requires corrections. Please update your profile and resubmit for review.'
            : 'Your stakeholder application is under review by your Barangay Official. You will be notified once verified.';

        const bannerHtml = `
            <div class="verification-status-banner ${bannerClass}" style="margin-bottom:1.25rem;">
                <div class="vsb-icon">${icon}</div>
                <div class="vsb-content">
                    <h4>${title}</h4>
                    <p>${noteText}</p>
                    ${remark}
                    <p class="vsb-note" style="margin-top:.4rem;font-size:.8rem;opacity:.85;">
                        Status: <strong>${status}</strong> &nbsp;|&nbsp; Business: ${data.businessName || '-'}
                    </p>
                </div>
            </div>`;

        if (overviewBanner) overviewBanner.innerHTML = bannerHtml;
        if (servicesBanner) servicesBanner.innerHTML = bannerHtml;
        if (addServiceBtn) {
            addServiceBtn.disabled = true;
            addServiceBtn.classList.add('btn-disabled-notice');
            addServiceBtn.title = 'Service publishing is restricted until your profile is verified by a Barangay Official.';
        }
    } catch (e) {
        console.warn('Could not load stakeholder verification status:', e);
    }
}

// ------------------------------------------------------------------------------
// ROLE PERMISSIONS & ACCESS CONTROL (EXACT ROLE SPECIFICATION)
// ------------------------------------------------------------------------------
const ROLE_PERMITTED_TABS = {
    'TOURIST': [
        'overview', 'destinations', 'services', 'packages', 'bookings', 'reviews'
    ],
    'STAKEHOLDER': [
        'overview', 'destinations', 'stakeholders', 'bookings', 'reviews'
    ],
    'BARANGAY_OFFICIAL': [
        'overview', 'destinations', 'verifications', 'barangay-records', 'reviews', 'reports'
    ],
    'LGU_OFFICER': [
        'overview', 'destinations', 'announcements', 'stakeholders', 'bookings',
        'barangay-records', 'lgu-monitoring', 'reviews', 'reports'
    ],
    'TOURISM_OFFICE': [
        'overview', 'destinations', 'announcements', 'stakeholders', 'services',
        'packages', 'bookings', 'verifications', 'barangay-records', 'lgu-monitoring',
        'reviews', 'reports'
    ],
    'SYSTEM_ADMIN': [
        'overview', 'admin', 'roles-permissions', 'settings', 'audit-logs',
        'backup-recovery', 'system-monitoring', 'security-mgmt'
    ]
};

function canRoleAccessTab(role, tabId) {
    if (!role) return false;
    const allowed = ROLE_PERMITTED_TABS[role] || [];
    return allowed.includes(tabId);
}

function applyRolePermissions(role) {
    const allowedTabs = ROLE_PERMITTED_TABS[role] || [];
    const navTabMap = {
        'nav-item-overview': 'overview',
        'nav-item-destinations': 'destinations',
        'nav-item-announcements': 'announcements',
        'nav-item-stakeholders': 'stakeholders',
        'nav-item-services': 'services',
        'nav-item-packages': 'packages',
        'nav-item-bookings': 'bookings',
        'nav-item-verifications': 'verifications',
        'nav-item-barangay-records': 'barangay-records',
        'nav-item-lgu-monitoring': 'lgu-monitoring',
        'nav-item-reviews': 'reviews',
        'nav-item-reports': 'reports',
        'nav-item-admin': 'admin',
        'nav-item-roles-permissions': 'roles-permissions',
        'nav-item-settings': 'settings',
        'nav-item-audit': 'audit-logs',
        'nav-item-backup': 'backup-recovery',
        'nav-item-system-monitoring': 'system-monitoring',
        'nav-item-security-mgmt': 'security-mgmt'
    };

    for (const [navId, tabId] of Object.entries(navTabMap)) {
        const el = document.getElementById(navId);
        if (el) {
            el.style.display = allowedTabs.includes(tabId) ? 'block' : 'none';
        }
    }

    const isTourism = role === 'TOURISM_OFFICE';
    const isLGU = role === 'LGU_OFFICER';
    const isBrgy = role === 'BARANGAY_OFFICIAL';
    const isStakeholder = role === 'STAKEHOLDER';

    // Action button permissions strictly mapped (Admin has no routine operational buttons)
    const btnAddDest = document.getElementById('btn-add-destination');
    const btnAddPkg = document.getElementById('btn-add-package');
    const btnAddSvc = document.getElementById('btn-add-service');
    const btnAddStakeholderSvc = document.getElementById('btn-add-stakeholder');
    const btnMyStakeholderProfile = document.getElementById('btn-my-stakeholder-profile');
    const btnRegisterStakeholder = document.getElementById('btn-register-stakeholder');
    const btnAddAnnounce = document.getElementById('btn-add-announcement');

    if (btnAddDest) btnAddDest.style.display = (isTourism || isBrgy) ? 'inline-flex' : 'none';
    if (btnAddPkg) btnAddPkg.style.display = (isTourism) ? 'inline-flex' : 'none';
    if (btnAddSvc) btnAddSvc.style.display = (isStakeholder) ? 'inline-flex' : 'none';
    if (btnAddStakeholderSvc) btnAddStakeholderSvc.style.display = (isStakeholder) ? 'inline-flex' : 'none';
    if (btnMyStakeholderProfile) btnMyStakeholderProfile.style.display = (isStakeholder) ? 'inline-flex' : 'none';
    if (btnRegisterStakeholder) btnRegisterStakeholder.style.display = (isBrgy || isTourism) ? 'inline-flex' : 'none';
    if (btnAddAnnounce) btnAddAnnounce.style.display = (isTourism || isLGU) ? 'inline-flex' : 'none';
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
    else if (tabId === 'lgu-monitoring') loadLguMonitoringData();
    else if (tabId === 'announcements') loadAnnouncementsList();
    else if (tabId === 'reviews') loadReviewsList();
    else if (tabId === 'reports') loadReportsData();
    else if (tabId === 'admin') loadAdminData();
    else if (tabId === 'settings') loadSystemSettings();
    else if (tabId === 'audit-logs') loadAuditLogs();
    else if (tabId === 'system-monitoring') loadSystemMonitoringData();
    else if (tabId === 'overview') refreshDashboardData();
}

async function loadSystemMonitoringData() {
    try {
        const res = await fetch(`${API_BASE}/admin/system/health`, {
            headers: { 'X-Auth-Token': state.authToken }
        });
        if (res.ok) {
            const h = await res.json();
            const bStatus = document.getElementById('mon-backend-status');
            const bSub = document.getElementById('mon-backend-sub');
            const dStatus = document.getElementById('mon-db-status');
            const dSub = document.getElementById('mon-db-sub');
            const jMem = document.getElementById('mon-jvm-mem');
            const jSub = document.getElementById('mon-jvm-sub');
            const aSess = document.getElementById('mon-active-sessions');
            const jVer = document.getElementById('mon-java-ver');
            const oVer = document.getElementById('mon-os-ver');

            if (bStatus) bStatus.textContent = h.status || 'ONLINE';
            if (bSub) bSub.textContent = `Port ${h.port || 8080} Active`;
            if (dStatus) dStatus.textContent = h.databaseConnected ? 'CONNECTED' : 'DISCONNECTED';
            if (dSub) dSub.textContent = `${h.databaseName} on ${h.databaseHost}`;
            if (jMem) jMem.textContent = `${h.jvmUsedMemoryMb || 0} MB`;
            if (jSub) jSub.textContent = `${h.memoryUsagePercent || 0}% of ${h.jvmTotalMemoryMb || 0} MB Allocated`;
            if (aSess) aSess.textContent = h.activeSessions || 1;
            if (jVer) jVer.textContent = h.backend || 'Java Runtime';
            if (oVer) oVer.textContent = h.os || 'Windows';
        }
    } catch (e) {
        console.warn('Failed loading system monitoring data:', e);
    }
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
    const filters = ['dest-barangay-filter', 'dest-form-barangay', 'user-form-barangay', 'stk-form-barangay', 'edit-user-barangay'];
    filters.forEach(id => {
        const el = document.getElementById(id);
        if (!el) return;
        if (id.includes('filter')) {
            el.innerHTML = '<option value="ALL">All Barangays</option>';
        } else if (id === 'user-form-barangay' || id === 'edit-user-barangay') {
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
            if (document.getElementById('kpi-destinations')) document.getElementById('kpi-destinations').textContent = k.totalDestinations ?? 0;
            if (document.getElementById('kpi-stakeholders')) document.getElementById('kpi-stakeholders').textContent = k.verifiedStakeholders ?? 0;
            if (document.getElementById('kpi-bookings')) document.getElementById('kpi-bookings').textContent = k.activeBookings ?? 0;
            if (document.getElementById('kpi-visitors')) document.getElementById('kpi-visitors').textContent = k.totalVisitorFootfall ?? 0;
        }
    } catch (e) {
        console.warn('Failed to load KPIs:', e);
    }
}

async function loadDestinations() {
    try {
        const headers = state.authToken ? { 'X-Auth-Token': state.authToken } : {};
        const res = await fetch(`${API_BASE}/destinations`, { headers });
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

    let dests = state.destinations || [];
    if (state.activeRole === 'BARANGAY_OFFICIAL' && state.currentUser?.barangayId) {
        dests = dests.filter(d => Number(d.barangayId) === Number(state.currentUser.barangayId));
    }

    if (dests.length === 0) {
        const opt = document.createElement('option');
        opt.value = '';
        opt.textContent = 'No registered destinations in your barangay';
        opt.disabled = true;
        opt.selected = true;
        el.appendChild(opt);
        return;
    }

    dests.forEach(d => {
        const opt = document.createElement('option');
        opt.value = d.destinationId;
        opt.textContent = `${d.name} (${d.barangayName || 'Local'})`;
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

    const role = state.activeRole;
    const isAdminOrTourism = role === 'SYSTEM_ADMIN' || role === 'TOURISM_OFFICE';

    destList.forEach(d => {
        const card = document.createElement('div');
        card.className = 'item-card';

        const photo = getDestinationImage(d);
        const safeName = (d.name || '').replace(/'/g, "\\'");
        const isBrgyMatch = role === 'BARANGAY_OFFICIAL' && state.currentUser && String(state.currentUser.barangayId) === String(d.barangayId);
        const canManage = isAdminOrTourism || isBrgyMatch;
        const isValidated = d.validatedBy && Number(d.validatedBy) > 0;

        let adminControls = '';
        if (canManage) {
            adminControls = `<div class="card-mgmt-actions">`;
            if (!isValidated) {
                adminControls += `<button class="btn btn-sm btn-success" onclick="validateDestination(${d.destinationId})"><i class="fa-solid fa-check"></i> Validate</button>`;
            }
            adminControls += `<button class="btn btn-sm btn-outline" onclick="openEditDestinationModal(${d.destinationId})"><i class="fa-solid fa-pen"></i> Edit</button>`;
            if (isAdminOrTourism) {
                adminControls += `<button class="btn btn-sm btn-danger" onclick="deleteDestination(${d.destinationId})"><i class="fa-solid fa-trash"></i> Delete</button>`;
            }
            adminControls += `</div>`;
        }

        const catIcon = getCategoryIconClass(d.category);
        card.innerHTML = `
            <div class="item-card-image no-photo">
                <i class="${catIcon} dest-icon"></i>
                <span class="item-card-badge">${formatCategory(d.category)}</span>
                <span class="item-card-rating"><i class="fa-solid fa-star"></i> ${d.averageRating || '5.0'}</span>
            </div>
            <div class="item-card-content">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 0.5rem; margin-bottom: 0.25rem;">
                    <h3 class="item-card-title" style="margin-bottom: 0;">${d.name}</h3>
                    <span class="badge ${isValidated ? 'badge-success' : 'badge-warning'}" style="font-size: 0.68rem; white-space: nowrap; flex-shrink: 0;">
                        ${isValidated ? 'Validated' : 'Pending'}
                    </span>
                </div>
                <span class="item-card-location"><i class="fa-solid fa-location-dot"></i> Brgy. ${d.barangayName}, ${d.municipalityName}</span>
                <p class="item-card-desc">${d.description}</p>
                <div class="item-card-footer">
                    <div class="item-price-tag">
                        <span class="item-price-label">Entrance Fee</span>
                        <span class="item-price-val">₱${Number(d.entranceFee).toFixed(2)}</span>
                    </div>
                    <button class="btn btn-primary btn-sm" onclick="promptInquireOrBook('${safeName}', ${d.destinationId})">
                        <i class="fa-solid fa-compass"></i> Inquire / Book
                    </button>
                </div>
                ${adminControls}
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

    const canManage = state.activeRole === 'TOURISM_OFFICE' || state.activeRole === 'SYSTEM_ADMIN';

    state.packages.forEach(pkg => {
        const card = document.createElement('div');
        card.className = 'item-card';

        const safeName = (pkg.name || '').replace(/'/g, "\\'");
        const pkgIcon = pkg.name.toLowerCase().includes('waterfall')
            ? 'fa-solid fa-water'
            : (pkg.name.toLowerCase().includes('sandbar') || pkg.name.toLowerCase().includes('island'))
                ? 'fa-solid fa-umbrella-beach'
                : 'fa-solid fa-map-location-dot';

        let mgmtBtns = '';
        if (canManage) {
            mgmtBtns = `
                <div class="card-mgmt-actions">
                    <button class="btn btn-sm btn-outline" onclick="openEditPackageModal(${pkg.packageId})">
                        <i class="fa-solid fa-pen"></i> Edit Package
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deletePackage(${pkg.packageId})">
                        <i class="fa-solid fa-trash"></i> Delete
                    </button>
                </div>
            `;
        }

        card.innerHTML = `
            <div class="item-card-image no-photo" style="background: linear-gradient(135deg, #065f46 0%, #064e3b 60%, #0d9488 100%);">
                <i class="${pkgIcon} dest-icon"></i>
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
                    <button class="btn btn-primary btn-sm" onclick="openPackageBookingModal(${pkg.packageId}, '${safeName}', ${pkg.pricePerPax})">
                        <i class="fa-solid fa-calendar-check"></i> Book Package
                    </button>
                </div>
                ${mgmtBtns}
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
            const myStakeholderId = state.currentUser?.stakeholderProfileId;
            const items = b.items || [];
            const myItems = items.filter(it => !myStakeholderId || Number(it.stakeholderId) === Number(myStakeholderId));
            const otherItems = items.filter(it => myStakeholderId && Number(it.stakeholderId) !== Number(myStakeholderId));

            if (myItems.length > 0) {
                actionButtons = myItems.map(it => {
                    const itStatus = (it.status || 'PENDING').toUpperCase();
                    let itBtns = '';
                    if (itStatus === 'PENDING') {
                        itBtns = `
                            <button class="btn btn-sm btn-success" onclick="updateBookingItemStatus(${it.itemId}, 'ACCEPTED')" title="Accept your assigned service">
                                <i class="fa-solid fa-check"></i> Accept
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="updateBookingItemStatus(${it.itemId}, 'REJECTED')" title="Decline your assigned service">
                                <i class="fa-solid fa-xmark"></i> Decline
                            </button>
                        `;
                    } else if (itStatus === 'ACCEPTED') {
                        itBtns = `
                            <button class="btn btn-sm btn-outline" onclick="updateBookingItemStatus(${it.itemId}, 'COMPLETED')" title="Mark service delivered">
                                <i class="fa-solid fa-check-double"></i> Mark Done
                            </button>
                        `;
                    } else {
                        itBtns = `<span class="badge ${getStatusBadgeClass(itStatus)}">${itStatus}</span>`;
                    }
                    return `<div class="item-action-row" style="margin-bottom: 4px;">
                              <span style="font-size: 0.76rem; font-weight: 600; display: block;">${it.serviceName || 'Assigned Service'}:</span>
                              ${itBtns}
                            </div>`;
                }).join('');

                if (otherItems.length > 0) {
                    const otherSummary = otherItems.map(o => `${o.serviceName || 'Co-op Service'}: ${o.status || 'PENDING'}`).join(', ');
                    actionButtons += `<div style="font-size: 0.7rem; color: var(--neutral-500); margin-top: 4px;" title="${otherSummary}">
                                        <i class="fa-solid fa-users"></i> ${otherItems.length} other co-op service(s)
                                      </div>`;
                }
            } else {
                actionButtons = `<span class="text-muted" style="font-size: 0.78rem;">No assigned items</span>`;
            }
        } else if (role === 'TOURIST') {
            if (b.status === 'COMPLETED') {
                actionButtons = `<button class="btn btn-sm btn-primary" onclick="openReviewModalForBooking(${b.bookingId})"><i class="fa-solid fa-star"></i> Leave Review</button>`;
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

async function updateBookingItemStatus(itemId, newStatus) {
    if (!itemId || !newStatus) return;
    try {
        const res = await fetch(`${API_BASE}/bookings/item-status`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify({ itemId: Number(itemId), status: newStatus })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            loadBookingsList();
        } else {
            showToast(data.message || 'Service item update failed.', 'error');
        }
    } catch (e) {
        showToast('Error updating service item: ' + e.message, 'error');
    }
}

// ------------------------------------------------------------------------------
// STAKEHOLDERS DIRECTORY MODULE
// ------------------------------------------------------------------------------
async function loadStakeholdersList() {
    try {
        const cat = document.getElementById('stakeholder-category-filter')?.value || 'ALL';
        const st = document.getElementById('stakeholder-status-filter')?.value || 'ALL';
        const jur = state.activeRole === 'BARANGAY_OFFICIAL' ? '&jurisdictionOnly=true' : '';
        const res = await fetch(`${API_BASE}/stakeholders?category=${cat}&verificationStatus=${st}${jur}`, {
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

    const role = state.activeRole;

    state.stakeholders.forEach(stk => {
        const card = document.createElement('div');
        card.className = 'item-card';

        const safeName = (stk.businessName || '').replace(/'/g, "\\'");
        const isOwner = state.currentUser && Number(state.currentUser.stakeholderProfileId) === Number(stk.profileId);
        const isBrgyMatch = role === 'BARANGAY_OFFICIAL' && state.currentUser && Number(state.currentUser.barangayId) === Number(stk.barangayId);
        const canEdit = isOwner || isBrgyMatch || role === 'TOURISM_OFFICE' || role === 'SYSTEM_ADMIN';

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
                <div class="item-card-footer" style="gap: 0.4rem; flex-wrap: wrap;">
                    <span class="text-muted" style="font-size: 0.78rem;">Experience: ${stk.yearsExperience} Years</span>
                    <div style="display: flex; gap: 0.35rem;">
                        ${canEdit ? `<button class="btn btn-outline btn-sm" onclick="openEditStakeholderModal(${stk.profileId})"><i class="fa-solid fa-pen"></i> Edit</button>` : ''}
                        <button class="btn btn-outline btn-sm" onclick="promptInquireOrBook('${safeName}', ${stk.profileId})">
                            <i class="fa-solid fa-envelope"></i> Reserve Service
                        </button>
                    </div>
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
        const res = await fetch(`${API_BASE}/stakeholders?verificationStatus=PENDING&jurisdictionOnly=true`, {
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
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No pending stakeholder verification requests in this jurisdiction.</td></tr>`;
        return;
    }

    const isBrgyOfficial = state.activeRole === 'BARANGAY_OFFICIAL';

    list.forEach(stk => {
        const tr = document.createElement('tr');
        const safeName = (stk.businessName || '').replace(/'/g, "\\'");
        const actionHtml = isBrgyOfficial
            ? `<button class="btn btn-sm btn-primary" onclick="openVerificationModal(${stk.profileId}, '${safeName}', ${stk.barangayId})">
                 <i class="fa-solid fa-stamp"></i> Evaluate
               </button>`
            : `<span class="badge badge-outline text-muted" title="Read-only monitoring; Barangay Official holds sole evaluation authority"><i class="fa-solid fa-eye"></i> Monitoring View</span>`;

        tr.innerHTML = `
            <td><strong>${stk.businessName}</strong><br><small class="text-muted">${stk.ownerName || 'Owner'}</small></td>
            <td>${formatCategory(stk.category)}</td>
            <td>Brgy. ${stk.barangayName || 'Local'}</td>
            <td>${stk.accreditationNo || 'LGU Documents Attached'}</td>
            <td><span class="badge badge-warning">PENDING</span></td>
            <td>${actionHtml}</td>
        `;
        tbody.appendChild(tr);
    });
}

function openVerificationModal(stkId, name, brgyId) {
    if (state.activeRole !== 'BARANGAY_OFFICIAL') {
        showToast('Only the authorized Barangay Official can evaluate stakeholder applications.', 'warning');
        return;
    }
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

function openReviewModalForBooking(bookingId) {
    const b = (state.bookings || []).find(x => Number(x.bookingId) === Number(bookingId));
    if (!b) return;

    document.getElementById('review-booking-id').value = bookingId;

    const targetTypeInput = document.getElementById('review-target-type');
    const targetIdInput = document.getElementById('review-target-id');
    const targetInfoEl = document.getElementById('review-target-info');

    if (b.packageId && Number(b.packageId) > 0) {
        targetTypeInput.value = 'PACKAGE';
        targetIdInput.value = b.packageId;
        if (targetInfoEl) {
            targetInfoEl.innerHTML = `<i class="fa-solid fa-cube"></i> Reviewing Tour Package: <strong>${escapeHtml(b.packageName || 'Package #' + b.packageId)}</strong>`;
        }
    } else {
        // Custom itinerary booking: attribute to community stakeholder
        targetTypeInput.value = 'STAKEHOLDER';
        const items = b.items || [];
        const primaryItem = items.length > 0 ? items[0] : null;
        const stakeholderId = primaryItem ? primaryItem.stakeholderId : 1;
        targetIdInput.value = stakeholderId;
        const label = primaryItem ? (primaryItem.businessName || primaryItem.stakeholderName || primaryItem.serviceName) : 'Community Stakeholder';
        if (targetInfoEl) {
            targetInfoEl.innerHTML = `<i class="fa-solid fa-handshake-angle"></i> Reviewing Community Host: <strong>${escapeHtml(label)}</strong>`;
        }
    }

    setRatingVal(5);
    openModal('modal-review');
}

function openReviewModal(bookingId, targetId) {
    openReviewModalForBooking(bookingId);
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

    const canManage = state.activeRole === 'TOURISM_OFFICE' || state.activeRole === 'LGU_OFFICER' || state.activeRole === 'SYSTEM_ADMIN';

    state.announcements.forEach(a => {
        const card = document.createElement('div');
        card.className = 'card mb-3';
        let actionsHtml = '';
        if (canManage) {
            actionsHtml = `
                <div style="display: flex; gap: 0.4rem;">
                    <button class="btn btn-sm btn-outline" onclick="openEditAnnouncementModal(${a.announcementId})"><i class="fa-solid fa-pen"></i> Edit</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteAnnouncement(${a.announcementId})"><i class="fa-solid fa-trash"></i> Delete</button>
                </div>
            `;
        }
        card.innerHTML = `
            <div class="card-header">
                <h3 class="card-title"><i class="fa-solid fa-bullhorn" style="color: var(--accent-600);"></i> ${a.title}</h3>
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <span class="badge badge-info">${formatCategory(a.category)}</span>
                    ${actionsHtml}
                </div>
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

async function loadLguMonitoringData() {
    const grid = document.getElementById('carrying-capacity-grid');
    if (!grid) return;
    try {
        const headers = state.authToken ? { 'X-Auth-Token': state.authToken } : {};
        const [resFootfall, resSettings] = await Promise.all([
            fetch(`${API_BASE}/reports/footfall`, { headers }),
            fetch(`${API_BASE}/settings`, { headers })
        ]);
        const footfall = resFootfall.ok ? await resFootfall.json() : [];
        const settings = resSettings.ok ? await resSettings.json() : {};
        const alertThreshold = parseInt(settings.capacityThreshold) || 85;

        if (!Array.isArray(footfall) || footfall.length === 0) return;

        // Eco-carrying daily/monthly reference capacities derived by destination category
        const getCapacity = (row) => {
            const name = (row.destination_name || '').toLowerCase();
            if (name.includes('sambawan')) return 250;
            if (name.includes('tinago')) return 400;
            if (name.includes('higatangan')) return 180;
            if (row.category === 'WATERFALL') return 300;
            if (row.category === 'ISLAND_BEACH') return 250;
            return 200;
        };

        grid.innerHTML = '';
        footfall.slice(0, 6).forEach(row => {
            const cap = getCapacity(row);
            const visitors = Number(row.grand_total_visitors) || 0;
            const pct = Math.min(100, Math.round((visitors / cap) * 100));
            let bg = '#f0fdf4', border = '#bbf7d0', color = '#166534', icon = 'fa-circle-check', label = 'Safe';
            if (pct >= alertThreshold) {
                bg = '#fef2f2'; border = '#fecaca'; color = '#991b1b'; icon = 'fa-circle-exclamation'; label = 'Critical';
            } else if (pct >= 65) {
                bg = '#fffbeb'; border = '#fde68a'; color = '#92400e'; icon = 'fa-triangle-exclamation'; label = 'Moderate';
            }
            const card = document.createElement('div');
            card.className = 'card p-3';
            card.style.background = bg;
            card.style.borderColor = border;
            card.innerHTML = `
                <h4 style="color: ${color};"><i class="fa-solid ${icon}"></i> ${row.destination_name}</h4>
                <p style="font-size: 0.85rem; margin-top: 0.25rem;">
                    Recorded Footfall: <strong>${visitors}</strong> / ${cap} pax | Utilization: <strong>${pct}%</strong> (${label})
                </p>
            `;
            grid.appendChild(card);
        });
    } catch (e) {
        console.warn('Failed loading LGU monitoring data:', e);
    }
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
    populateArrivalDestinationDropdown();
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
    state.users = users || [];
    const tbody = document.getElementById('users-table-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    state.users.forEach(u => {
        const tr = document.createElement('tr');
        const safeUser = (u.username || '').replace(/'/g, "\\'");
        tr.innerHTML = `
            <td><strong>${u.username}</strong></td>
            <td>${u.fullName}</td>
            <td>${u.email}</td>
            <td><span class="user-role-badge">${u.roleCode}</span></td>
            <td>${u.barangayName ? `Brgy. ${u.barangayName}` : 'Province-wide'}</td>
            <td><span class="badge ${u.status === 'ACTIVE' ? 'badge-success' : 'badge-danger'}">${u.status}</span></td>
            <td>
                <div class="table-actions">
                    <button class="btn btn-sm btn-outline" onclick="openEditUserModal(${u.userId})">
                        <i class="fa-solid fa-pen"></i> Edit
                    </button>
                    <button class="btn btn-sm btn-outline" onclick="openAdminResetPasswordModal(${u.userId}, '${safeUser}')">
                        <i class="fa-solid fa-key"></i> Reset Password
                    </button>
                    <button class="btn btn-sm ${u.status === 'ACTIVE' ? 'btn-danger' : 'btn-success'}" onclick="toggleUserStatus(${u.userId}, '${u.status}')">
                        ${u.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                    </button>
                </div>
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
// PACKAGE & SERVICE BOOKING MODAL HELPER
// ------------------------------------------------------------------------------
function openPackageBookingModal(packageId, name, price) {
    document.getElementById('book-package-id').value = packageId || '';
    document.getElementById('book-service-id').value = '';
    document.getElementById('book-stakeholder-id').value = '';
    document.getElementById('book-pricing-unit').value = 'PER_PERSON';
    document.getElementById('book-unit-price').value = price || 0;
    document.getElementById('book-package-name').value = name;
    document.getElementById('book-pax').value = 2;

    const today = new Date().toISOString().split('T')[0];
    document.getElementById('book-date-start').value = today;
    document.getElementById('book-date-end').value = today;

    calculatePackageBookingTotal();
    openModal('modal-booking');
}

function openServiceBookingModal(serviceId) {
    const svc = state.services.find(s => Number(s.serviceId) === Number(serviceId));
    if (!svc) return;
    document.getElementById('book-package-id').value = '';
    document.getElementById('book-service-id').value = svc.serviceId;
    document.getElementById('book-stakeholder-id').value = svc.stakeholderId;
    document.getElementById('book-pricing-unit').value = svc.pricingUnit || 'PER_PERSON';
    document.getElementById('book-unit-price').value = svc.basePrice || 0;
    document.getElementById('book-package-name').value = `${svc.serviceName} (${svc.businessName || svc.stakeholderName || 'Local Provider'})`;
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
    const svcId = parseInt(document.getElementById('book-service-id')?.value);
    let total = 0;

    if (pkgId > 0) {
        const pkg = state.packages.find(p => p.packageId === pkgId);
        const rate = pkg ? Number(pkg.pricePerPax) : (parseFloat(document.getElementById('book-unit-price')?.value) || 1000);
        total = rate * pax;
    } else if (svcId > 0) {
        const rate = parseFloat(document.getElementById('book-unit-price')?.value) || 500;
        const unit = document.getElementById('book-pricing-unit')?.value || 'PER_PERSON';
        total = unit === 'PER_PERSON' ? rate * pax : rate;
    } else {
        const rate = parseFloat(document.getElementById('book-unit-price')?.value) || 1000;
        total = rate * pax;
    }

    const el = document.getElementById('book-total-amount');
    if (el) el.textContent = `₱${total.toFixed(2)}`;
}

async function handlePackageBookingSubmit(e) {
    e.preventDefault();
    const pkgId = parseInt(document.getElementById('book-package-id').value) || 0;
    const svcId = parseInt(document.getElementById('book-service-id').value) || 0;
    const stkId = parseInt(document.getElementById('book-stakeholder-id').value) || 0;
    const pax = parseInt(document.getElementById('book-pax').value) || 1;
    const unitPrice = parseFloat(document.getElementById('book-unit-price').value) || 0;
    const pricingUnit = document.getElementById('book-pricing-unit').value || 'PER_PERSON';

    let totalAmount = 0;
    let items = [];

    if (pkgId > 0) {
        const pkg = state.packages.find(p => p.packageId === pkgId);
        const rate = pkg ? Number(pkg.pricePerPax) : unitPrice;
        totalAmount = rate * pax;
    } else if (svcId > 0) {
        const qty = pricingUnit === 'PER_PERSON' ? pax : 1;
        totalAmount = unitPrice * qty;
        items.push({
            serviceId: svcId,
            stakeholderId: stkId,
            unitPrice: unitPrice,
            quantity: qty,
            subtotal: totalAmount
        });
    } else {
        totalAmount = unitPrice * pax;
    }

    const payload = {
        packageId: pkgId,
        travelDateStart: document.getElementById('book-date-start').value,
        travelDateEnd: document.getElementById('book-date-end').value,
        numberOfPax: pax,
        totalAmount: totalAmount,
        customNotes: document.getElementById('book-notes').value,
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
function openRegisterModal(type) {
    // Determine which tab to show (default to tourist)
    const tab = type === 'stakeholder' ? 'stakeholder' : 'tourist';
    openModal('modal-register');
    switchRegisterTab(tab);

    // If stakeholder tab, load location data
    if (tab === 'stakeholder') {
        loadRegisterLocationData();
    }
}

function switchRegisterTab(tab) {
    const touristBtn = document.getElementById('tab-btn-tourist');
    const stakeholderBtn = document.getElementById('tab-btn-stakeholder');
    const touristForm = document.getElementById('form-register');
    const stakeholderForm = document.getElementById('form-stakeholder-register');

    if (tab === 'stakeholder') {
        if (touristBtn) touristBtn.classList.remove('active');
        if (stakeholderBtn) stakeholderBtn.classList.add('active');
        if (touristForm) touristForm.style.display = 'none';
        if (stakeholderForm) stakeholderForm.style.display = '';
        loadRegisterLocationData();
    } else {
        if (touristBtn) touristBtn.classList.add('active');
        if (stakeholderBtn) stakeholderBtn.classList.remove('active');
        if (touristForm) touristForm.style.display = '';
        if (stakeholderForm) stakeholderForm.style.display = 'none';
    }
}

// Load municipalities into the stakeholder registration dropdown
let _regAllBarangays = [];
async function loadRegisterLocationData() {
    try {
        const muniSel = document.getElementById('stkreg-municipality');
        if (!muniSel || muniSel.options.length > 1) return; // already loaded
        const [muniRes, brgyRes] = await Promise.all([
            fetch(`${API_BASE}/municipalities`),
            fetch(`${API_BASE}/barangays`)
        ]);
        if (muniRes.ok) {
            const munis = await muniRes.json();
            muniSel.innerHTML = '<option value="">-- Select Municipality --</option>';
            munis.forEach(m => {
                const opt = document.createElement('option');
                opt.value = m.municipalityId;
                opt.textContent = m.name;
                muniSel.appendChild(opt);
            });
        }
        if (brgyRes.ok) {
            _regAllBarangays = await brgyRes.json();
        }
    } catch (e) {
        console.warn('Failed to load registration location data:', e);
    }
}

function onStakeholderMuniChange() {
    const muniSel = document.getElementById('stkreg-municipality');
    const brgySelect = document.getElementById('stkreg-barangay');
    if (!muniSel || !brgySelect) return;
    const selectedMuniId = parseInt(muniSel.value);
    brgySelect.innerHTML = '<option value="">-- Select Barangay --</option>';
    if (!selectedMuniId || isNaN(selectedMuniId)) return;
    const filtered = _regAllBarangays.filter(b => Number(b.municipalityId) === selectedMuniId);
    filtered.forEach(b => {
        const opt = document.createElement('option');
        opt.value = b.barangayId;
        opt.textContent = b.name;
        brgySelect.appendChild(opt);
    });
}

function openAddDestinationModal() {
    document.getElementById('dest-form-id').value = '';
    document.getElementById('dest-form-name').value = '';
    document.getElementById('dest-form-desc').value = '';
    document.getElementById('dest-form-fee').value = '50.00';
    document.getElementById('dest-form-hours').value = '6:00 AM - 5:00 PM';
    document.getElementById('dest-form-coords').value = '';
    document.getElementById('dest-form-contact').value = '';
    const st = document.getElementById('dest-form-status');
    if (st) st.value = 'ACTIVE';
    const brgySelect = document.getElementById('dest-form-barangay');
    if (brgySelect && state.activeRole === 'BARANGAY_OFFICIAL' && state.currentUser?.barangayId) {
        brgySelect.value = state.currentUser.barangayId;
    }
    const titleEl = document.getElementById('modal-add-destination-title');
    if (titleEl) titleEl.innerHTML = '<i class="fa-solid fa-map-location-dot"></i> Add New Destination';
    openModal('modal-add-destination');
}

function openEditDestinationModal(destinationId) {
    const d = state.destinations.find(x => Number(x.destinationId) === Number(destinationId));
    if (!d) return;
    document.getElementById('dest-form-id').value = d.destinationId;
    document.getElementById('dest-form-name').value = d.name || '';
    document.getElementById('dest-form-category').value = d.category || 'WATERFALL';
    document.getElementById('dest-form-barangay').value = d.barangayId || '';
    document.getElementById('dest-form-desc').value = d.description || '';
    document.getElementById('dest-form-fee').value = Number(d.entranceFee || 0).toFixed(2);
    document.getElementById('dest-form-hours').value = d.operatingHours || '6:00 AM - 5:00 PM';
    document.getElementById('dest-form-coords').value = d.locationCoordinates || '';
    document.getElementById('dest-form-contact').value = d.contactInfo || '';
    const st = document.getElementById('dest-form-status');
    if (st) st.value = d.status || 'ACTIVE';
    const titleEl = document.getElementById('modal-add-destination-title');
    if (titleEl) titleEl.innerHTML = '<i class="fa-solid fa-pen-to-square"></i> Edit Destination';
    openModal('modal-add-destination');
}

async function validateDestination(destinationId) {
    try {
        const res = await fetch(`${API_BASE}/destinations/validate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify({ destinationId })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message || 'Destination validated!', 'success');
            loadDestinations();
        } else {
            showToast(data.message || 'Validation failed.', 'error');
        }
    } catch (e) {
        showToast('Error validating destination: ' + e.message, 'error');
    }
}

async function deleteDestination(destinationId) {
    try {
        const res = await fetch(`${API_BASE}/destinations/delete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify({ destinationId })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message || 'Destination deleted.', 'success');
            loadDestinations();
        } else {
            showToast(data.message || 'Failed to delete destination.', 'error');
        }
    } catch (e) {
        showToast('Error deleting destination: ' + e.message, 'error');
    }
}

function openAddServiceModal() {
    document.getElementById('svc-form-id').value = '';
    document.getElementById('svc-form-name').value = '';
    document.getElementById('svc-form-category').value = 'GUIDING';
    document.getElementById('svc-form-unit').value = 'PER_PERSON';
    document.getElementById('svc-form-price').value = '500.00';
    document.getElementById('svc-form-capacity').value = '6';
    document.getElementById('svc-form-desc').value = '';
    const availEl = document.getElementById('svc-form-available');
    if (availEl) availEl.checked = true;
    const titleEl = document.getElementById('modal-add-service-title');
    if (titleEl) titleEl.innerHTML = '<i class="fa-solid fa-handshake-angle"></i> Register Service Offering';
    openModal('modal-add-service');
}

function openEditServiceModal(serviceId) {
    const s = state.services.find(x => Number(x.serviceId) === Number(serviceId));
    if (!s) return;
    document.getElementById('svc-form-id').value = s.serviceId;
    document.getElementById('svc-form-name').value = s.serviceName || '';
    document.getElementById('svc-form-category').value = s.serviceCategory || 'GUIDING';
    document.getElementById('svc-form-unit').value = s.pricingUnit || 'PER_PERSON';
    document.getElementById('svc-form-price').value = Number(s.basePrice || 0).toFixed(2);
    document.getElementById('svc-form-capacity').value = s.maxCapacity || 6;
    document.getElementById('svc-form-desc').value = s.description || '';
    const isAvail = s.available !== undefined ? s.available : Boolean(s.isAvailable);
    const availEl = document.getElementById('svc-form-available');
    if (availEl) availEl.checked = isAvail;
    const titleEl = document.getElementById('modal-add-service-title');
    if (titleEl) titleEl.innerHTML = '<i class="fa-solid fa-pen-to-square"></i> Edit Service Offering';
    openModal('modal-add-service');
}

function openAddAnnouncementModal() {
    document.getElementById('anno-form-id').value = '';
    document.getElementById('anno-form-title').value = '';
    document.getElementById('anno-form-type').value = 'ANNOUNCEMENT';
    document.getElementById('anno-form-content').value = '';
    const titleEl = document.getElementById('modal-add-announcement-title');
    if (titleEl) titleEl.innerHTML = '<i class="fa-solid fa-bullhorn"></i> Post Tourism Information &amp; Advisory';
    openModal('modal-add-announcement');
}

function openEditAnnouncementModal(announcementId) {
    const a = state.announcements.find(x => Number(x.announcementId) === Number(announcementId));
    if (!a) return;
    document.getElementById('anno-form-id').value = a.announcementId;
    document.getElementById('anno-form-title').value = a.title || '';
    document.getElementById('anno-form-type').value = a.category || 'ANNOUNCEMENT';
    document.getElementById('anno-form-content').value = a.content || '';
    const titleEl = document.getElementById('modal-add-announcement-title');
    if (titleEl) titleEl.innerHTML = '<i class="fa-solid fa-pen-to-square"></i> Edit Tourism Advisory';
    openModal('modal-add-announcement');
}

async function deleteAnnouncement(announcementId) {
    try {
        const res = await fetch(`${API_BASE}/announcements/delete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify({ announcementId })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message || 'Announcement deleted.', 'success');
            loadAnnouncementsList();
        } else {
            showToast(data.message || 'Failed deleting announcement.', 'error');
        }
    } catch (e) {
        showToast('Error deleting announcement: ' + e.message, 'error');
    }
}

function openCreateUserModal() { openModal('modal-create-user'); }

function populatePackageModalCheckboxes(selectedDestIds = [], selectedSvcIds = []) {
    const destBox = document.getElementById('pkg-form-destinations-list');
    const svcBox = document.getElementById('pkg-form-services-list');
    if (destBox) {
        destBox.innerHTML = '';
        state.destinations.forEach(d => {
            const checked = selectedDestIds.includes(Number(d.destinationId)) ? 'checked' : '';
            const lbl = document.createElement('label');
            lbl.style.display = 'flex';
            lbl.style.alignItems = 'center';
            lbl.style.gap = '0.5rem';
            lbl.style.fontSize = '0.84rem';
            lbl.style.padding = '0.2rem 0';
            lbl.innerHTML = `<input type="checkbox" class="pkg-dest-chk" value="${d.destinationId}" ${checked}> <span>${d.name} <small class="text-muted">(Brgy. ${d.barangayName})</small></span>`;
            destBox.appendChild(lbl);
        });
    }
    if (svcBox) {
        svcBox.innerHTML = '';
        state.services.forEach(s => {
            const checked = selectedSvcIds.includes(Number(s.serviceId)) ? 'checked' : '';
            const lbl = document.createElement('label');
            lbl.style.display = 'flex';
            lbl.style.alignItems = 'center';
            lbl.style.gap = '0.5rem';
            lbl.style.fontSize = '0.84rem';
            lbl.style.padding = '0.2rem 0';
            lbl.innerHTML = `<input type="checkbox" class="pkg-svc-chk" value="${s.serviceId}" ${checked}> <span>${s.serviceName} <small class="text-muted">(${s.businessName || s.stakeholderName || 'Provider'} - ₱${Number(s.basePrice).toFixed(2)})</small></span>`;
            svcBox.appendChild(lbl);
        });
    }
}

function openAddPackageModal() {
    document.getElementById('pkg-form-id').value = '';
    document.getElementById('pkg-form-name').value = '';
    document.getElementById('pkg-form-desc').value = '';
    document.getElementById('pkg-form-price').value = '1200.00';
    document.getElementById('pkg-form-duration').value = '1';
    document.getElementById('pkg-form-minpax').value = '2';
    document.getElementById('pkg-form-maxpax').value = '10';
    document.getElementById('pkg-form-inclusions').value = 'Accredited local guide, transport, entrance fees';
    document.getElementById('pkg-form-exclusions').value = 'Personal expenses, optional tipping';
    populatePackageModalCheckboxes([], []);
    const titleEl = document.getElementById('modal-add-package-title');
    if (titleEl) titleEl.innerHTML = '<i class="fa-solid fa-suitcase-rolling"></i> Create Curated Tourism Package';
    openModal('modal-add-package');
}

function openEditPackageModal(packageId) {
    const pkg = state.packages.find(p => Number(p.packageId) === Number(packageId));
    if (!pkg) return;
    document.getElementById('pkg-form-id').value = pkg.packageId;
    document.getElementById('pkg-form-name').value = pkg.name || '';
    document.getElementById('pkg-form-desc').value = pkg.description || '';
    document.getElementById('pkg-form-price').value = Number(pkg.pricePerPax || 0).toFixed(2);
    document.getElementById('pkg-form-duration').value = pkg.durationDays || 1;
    document.getElementById('pkg-form-minpax').value = pkg.minPax || 2;
    document.getElementById('pkg-form-maxpax').value = pkg.maxPax || 10;
    document.getElementById('pkg-form-inclusions').value = pkg.inclusions || '';
    document.getElementById('pkg-form-exclusions').value = pkg.exclusions || '';

    const destIds = Array.isArray(pkg.destinations) ? pkg.destinations.map(d => Number(d.destinationId)) : [];
    const svcIds = Array.isArray(pkg.services) ? pkg.services.map(s => Number(s.serviceId)) : [];
    populatePackageModalCheckboxes(destIds, svcIds);

    const titleEl = document.getElementById('modal-add-package-title');
    if (titleEl) titleEl.innerHTML = '<i class="fa-solid fa-pen-to-square"></i> Edit Curated Tourism Package';
    openModal('modal-add-package');
}

async function handleSavePackageSubmit(e) {
    e.preventDefault();
    const pkgId = parseInt(document.getElementById('pkg-form-id').value) || 0;
    const destIds = Array.from(document.querySelectorAll('.pkg-dest-chk:checked')).map(cb => parseInt(cb.value));
    const svcIds = Array.from(document.querySelectorAll('.pkg-svc-chk:checked')).map(cb => parseInt(cb.value));

    const payload = {
        name: document.getElementById('pkg-form-name').value,
        description: document.getElementById('pkg-form-desc').value,
        pricePerPax: parseFloat(document.getElementById('pkg-form-price').value),
        durationDays: parseInt(document.getElementById('pkg-form-duration').value) || 1,
        minPax: parseInt(document.getElementById('pkg-form-minpax').value) || 1,
        maxPax: parseInt(document.getElementById('pkg-form-maxpax').value) || 10,
        inclusions: document.getElementById('pkg-form-inclusions').value,
        exclusions: document.getElementById('pkg-form-exclusions').value,
        status: 'ACTIVE',
        destinationIds: destIds,
        serviceIds: svcIds
    };
    if (pkgId > 0) payload.packageId = pkgId;

    try {
        const res = await fetch(`${API_BASE}/packages`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-add-package');
            showToast(data.message || 'Curated package saved!', 'success');
            await loadPackages();
            renderPublicPackages(state.packages);
        } else {
            showToast(data.message || 'Failed saving package.', 'error');
        }
    } catch (err) {
        showToast('Error saving package: ' + err.message, 'error');
    }
}

async function deletePackage(packageId) {
    try {
        const res = await fetch(`${API_BASE}/packages/delete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify({ packageId })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message || 'Package deleted.', 'success');
            await loadPackages();
            renderPublicPackages(state.packages);
        } else {
            showToast(data.message || 'Failed deleting package.', 'error');
        }
    } catch (err) {
        showToast('Error deleting package: ' + err.message, 'error');
    }
}

async function handleAddDestinationSubmit(e) {
    e.preventDefault();
    const destId = parseInt(document.getElementById('dest-form-id').value) || 0;
    const payload = {
        name: document.getElementById('dest-form-name').value,
        category: document.getElementById('dest-form-category').value,
        barangayId: parseInt(document.getElementById('dest-form-barangay').value),
        description: document.getElementById('dest-form-desc').value,
        entranceFee: parseFloat(document.getElementById('dest-form-fee').value),
        operatingHours: document.getElementById('dest-form-hours').value,
        locationCoordinates: document.getElementById('dest-form-coords').value,
        contactInfo: document.getElementById('dest-form-contact').value,
        status: document.getElementById('dest-form-status')?.value || 'ACTIVE'
    };
    if (destId > 0) payload.destinationId = destId;

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
            showToast(data.message || 'Destination saved successfully!', 'success');
            await loadDestinations();
            renderPublicDestinations(state.destinations);
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

function getCategoryIconClass(cat) {
    const c = (cat || '').toUpperCase();
    if (c.includes('WATERFALL')) return 'fa-solid fa-water';
    if (c.includes('ISLAND') || c.includes('BEACH')) return 'fa-solid fa-umbrella-beach';
    if (c.includes('SPRING') || c.includes('HOT')) return 'fa-solid fa-hot-tub-person';
    if (c.includes('CULTURAL') || c.includes('HERITAGE') || c.includes('POTTERY')) return 'fa-solid fa-landmark-dome';
    return 'fa-solid fa-mountain-sun';
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
    if (dest && dest.imageUrl && (dest.imageUrl.startsWith('assets/') || dest.imageUrl.startsWith('http')) && !dest.imageUrl.includes('hero_179')) {
        return dest.imageUrl;
    }
    return '';
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

        const catIcon = getCategoryIconClass(d.category);
        card.innerHTML = `
            <div class="public-dest-thumb no-photo">
                <i class="${catIcon} dest-icon"></i>
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
        const safePkgName = pkgName.replace(/'/g, "\\'");
        const pkgIcon = pkgName.toLowerCase().includes('waterfall')
            ? 'fa-solid fa-water'
            : (pkgName.toLowerCase().includes('sandbar') || pkgName.toLowerCase().includes('island'))
                ? 'fa-solid fa-umbrella-beach'
                : 'fa-solid fa-map-location-dot';

        card.innerHTML = `
            <div class="public-dest-thumb no-photo" style="background: linear-gradient(135deg, #065f46 0%, #064e3b 60%, #0d9488 100%);">
                <i class="${pkgIcon} dest-icon"></i>
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
        const res = await fetch(`${API_BASE}/stakeholders/services?all=true`);
        if (res.ok) {
            state.services = await res.json();
            tbody.innerHTML = '';
            if (state.services.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">No services registered yet.</td></tr>';
                return;
            }
            const role = state.activeRole;
            const myProfileId = state.currentUser ? Number(state.currentUser.stakeholderProfileId) : 0;

            state.services.forEach(s => {
                const tr = document.createElement('tr');
                const isAvail = s.available !== undefined ? Boolean(s.available) : Boolean(s.isAvailable);
                const canManage = role === 'SYSTEM_ADMIN' || role === 'TOURISM_OFFICE' || (role === 'STAKEHOLDER' && myProfileId === Number(s.stakeholderId));

                let actionsHtml = '';
                if (canManage) {
                    actionsHtml = `
                        <div style="display: flex; gap: 0.35rem; flex-wrap: wrap;">
                            <button class="btn btn-sm btn-outline" onclick="openEditServiceModal(${s.serviceId})"><i class="fa-solid fa-pen"></i> Edit</button>
                            <button class="btn btn-sm ${isAvail ? 'btn-outline' : 'btn-success'}" onclick="toggleServiceAvailability(${s.serviceId})">
                                ${isAvail ? 'Set Unavailable' : 'Set Available'}
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteServiceOffering(${s.serviceId})"><i class="fa-solid fa-trash"></i></button>
                        </div>
                    `;
                } else if (isAvail) {
                    actionsHtml = `<button class="btn btn-sm btn-primary" onclick="openServiceBookingModal(${s.serviceId})"><i class="fa-solid fa-calendar-check"></i> Book</button>`;
                } else {
                    actionsHtml = `<span class="text-muted">Unavailable</span>`;
                }

                tr.innerHTML = `
                    <td><strong>${s.serviceName}</strong></td>
                    <td>${s.stakeholderName || 'Local Stakeholder'}</td>
                    <td><span class="badge badge-info">${formatCategory(s.serviceCategory)}</span></td>
                    <td><strong style="color: var(--primary-700)">₱${Number(s.basePrice).toFixed(2)}</strong> <small class="text-muted">${formatPricingUnit(s.pricingUnit)}</small></td>
                    <td>${s.maxCapacity || 1} pax</td>
                    <td><span class="badge ${isAvail ? 'badge-success' : 'badge-danger'}">${isAvail ? 'Available' : 'Unavailable'}</span></td>
                    <td>${actionsHtml}</td>
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
    const svcId = parseInt(document.getElementById('svc-form-id').value) || 0;
    const isAvail = document.getElementById('svc-form-available') ? document.getElementById('svc-form-available').checked : true;
    const payload = {
        serviceName: document.getElementById('svc-form-name').value,
        serviceCategory: document.getElementById('svc-form-category').value,
        pricingUnit: document.getElementById('svc-form-unit').value,
        basePrice: parseFloat(document.getElementById('svc-form-price').value),
        maxCapacity: parseInt(document.getElementById('svc-form-capacity').value),
        description: document.getElementById('svc-form-desc').value,
        available: isAvail,
        isAvailable: isAvail
    };
    if (svcId > 0) payload.serviceId = svcId;

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
            showToast(data.message || 'Service offering saved successfully!', 'success');
            loadServicesTable();
            loadStakeholderServices();
        } else {
            showToast(data.message || 'Failed saving service.', 'error');
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

async function toggleServiceAvailability(serviceId) {
    const s = state.services.find(x => Number(x.serviceId) === Number(serviceId));
    if (!s) return;
    const currentAvail = s.available !== undefined ? Boolean(s.available) : Boolean(s.isAvailable);
    const nextAvail = !currentAvail;
    try {
        const res = await fetch(`${API_BASE}/stakeholders/services`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify({
                serviceId: s.serviceId,
                stakeholderId: s.stakeholderId,
                serviceName: s.serviceName,
                serviceCategory: s.serviceCategory,
                pricingUnit: s.pricingUnit,
                basePrice: s.basePrice,
                maxCapacity: s.maxCapacity,
                description: s.description,
                available: nextAvail,
                isAvailable: nextAvail
            })
        });
        const data = await res.json();
        if (data.success) {
            showToast(`Service marked as ${nextAvail ? 'Available' : 'Unavailable'}.`, 'success');
            loadServicesTable();
            loadStakeholderServices();
        } else {
            showToast(data.message || 'Failed updating availability.', 'error');
        }
    } catch (e) {
        showToast('Error toggling availability: ' + e.message, 'error');
    }
}

async function deleteServiceOffering(serviceId) {
    try {
        const res = await fetch(`${API_BASE}/stakeholders/services/delete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify({ serviceId })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message || 'Service offering deleted.', 'success');
            loadServicesTable();
            loadStakeholderServices();
        } else {
            showToast(data.message || 'Failed deleting service.', 'error');
        }
    } catch (e) {
        showToast('Error deleting service: ' + e.message, 'error');
    }
}

async function openMyStakeholderProfileModal() {
    try {
        const res = await fetch(`${API_BASE}/stakeholders/me`, {
            headers: { 'X-Auth-Token': state.authToken }
        });
        if (res.ok) {
            const p = await res.json();
            document.getElementById('stk-new-account-fields').style.display = 'none';
            document.getElementById('stk-form-id').value = p.profileId || '';
            document.getElementById('stk-form-business').value = p.businessName || '';
            document.getElementById('stk-form-category').value = p.category || 'TOUR_GUIDE';
            document.getElementById('stk-form-barangay').value = p.barangayId || (state.barangays[0]?.barangayId || 1);
            document.getElementById('stk-form-accred').value = p.accreditationNo || '';
            document.getElementById('stk-form-exp').value = p.yearsExperience || 1;
            document.getElementById('stk-form-phone').value = p.contactNumber || '';
            document.getElementById('stk-form-bio').value = p.bio || '';
            const titleEl = document.getElementById('modal-stakeholder-profile-title');
            if (titleEl) titleEl.innerHTML = '<i class="fa-solid fa-id-badge"></i> My Stakeholder Profile';
            openModal('modal-stakeholder-profile');
        }
    } catch (e) {
        showToast('Error loading stakeholder profile: ' + e.message, 'error');
    }
}

function openRegisterStakeholderModal() {
    document.getElementById('stk-new-account-fields').style.display = 'block';
    document.getElementById('stk-form-id').value = '';
    document.getElementById('stk-form-fullname').value = '';
    document.getElementById('stk-form-username').value = '';
    document.getElementById('stk-form-password').value = 'password123';
    document.getElementById('stk-form-email').value = '';
    document.getElementById('stk-form-business').value = '';
    document.getElementById('stk-form-category').value = 'TOUR_GUIDE';
    const brgySelect = document.getElementById('stk-form-barangay');
    if (brgySelect && state.activeRole === 'BARANGAY_OFFICIAL' && state.currentUser?.barangayId) {
        brgySelect.value = state.currentUser.barangayId;
    }
    document.getElementById('stk-form-accred').value = '';
    document.getElementById('stk-form-exp').value = '2';
    document.getElementById('stk-form-phone').value = '';
    document.getElementById('stk-form-bio').value = '';
    const titleEl = document.getElementById('modal-stakeholder-profile-title');
    if (titleEl) titleEl.innerHTML = '<i class="fa-solid fa-user-check"></i> Register Local Stakeholder';
    openModal('modal-stakeholder-profile');
}

function openEditStakeholderModal(profileId) {
    const p = state.stakeholders.find(x => Number(x.profileId) === Number(profileId));
    if (!p) return;
    document.getElementById('stk-new-account-fields').style.display = 'none';
    document.getElementById('stk-form-id').value = p.profileId;
    document.getElementById('stk-form-business').value = p.businessName || '';
    document.getElementById('stk-form-category').value = p.category || 'TOUR_GUIDE';
    document.getElementById('stk-form-barangay').value = p.barangayId || 1;
    document.getElementById('stk-form-accred').value = p.accreditationNo || '';
    document.getElementById('stk-form-exp').value = p.yearsExperience || 1;
    document.getElementById('stk-form-phone').value = p.contactNumber || '';
    document.getElementById('stk-form-bio').value = p.bio || '';
    const titleEl = document.getElementById('modal-stakeholder-profile-title');
    if (titleEl) titleEl.innerHTML = '<i class="fa-solid fa-pen-to-square"></i> Edit Stakeholder Profile';
    openModal('modal-stakeholder-profile');
}

async function handleSaveStakeholderProfileSubmit(e) {
    e.preventDefault();
    const profileId = parseInt(document.getElementById('stk-form-id').value) || 0;
    const payload = {
        businessName: document.getElementById('stk-form-business').value,
        category: document.getElementById('stk-form-category').value,
        barangayId: parseInt(document.getElementById('stk-form-barangay').value),
        accreditationNo: document.getElementById('stk-form-accred').value,
        yearsExperience: parseInt(document.getElementById('stk-form-exp').value) || 1,
        contactNumber: document.getElementById('stk-form-phone').value,
        bio: document.getElementById('stk-form-bio').value
    };
    if (profileId > 0) {
        payload.profileId = profileId;
    } else if (document.getElementById('stk-new-account-fields').style.display !== 'none') {
        payload.fullName = document.getElementById('stk-form-fullname').value;
        payload.username = document.getElementById('stk-form-username').value;
        payload.password = document.getElementById('stk-form-password').value;
        payload.email = document.getElementById('stk-form-email').value;
    }

    try {
        const res = await fetch(`${API_BASE}/stakeholders`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-stakeholder-profile');
            showToast(data.message || 'Stakeholder profile saved!', 'success');
            loadStakeholdersList();
            loadVerificationsList();
        } else {
            showToast(data.message || 'Failed saving stakeholder profile.', 'error');
        }
    } catch (err) {
        showToast('Error saving stakeholder profile: ' + err.message, 'error');
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
            state.barangayRecords = data;
            tbody.innerHTML = '';
            const role = state.activeRole;
            data.forEach(b => {
                const bId = b.barangay_id || b.barangayId;
                const bName = b.barangay_name || b.barangayName;
                const mName = b.municipality_name || b.municipalityName;
                const cPerson = b.contact_person || b.contactPerson || 'Barangay Captain';
                const cPhone = b.contact_number || b.contactNumber || 'N/A';
                const canEdit = role === 'SYSTEM_ADMIN' || role === 'TOURISM_OFFICE' || role === 'LGU_OFFICER'
                    || (role === 'BARANGAY_OFFICIAL' && state.currentUser && Number(state.currentUser.barangayId) === Number(bId));

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${bName}</strong></td>
                    <td>${mName}</td>
                    <td>${cPerson}</td>
                    <td>${cPhone}</td>
                    <td><span class="badge badge-info">${b.total_destinations ?? b.destinationCount ?? 0}</span></td>
                    <td><span class="badge badge-info">${b.total_stakeholders ?? b.stakeholderCount ?? 0}</span></td>
                    <td><span class="badge badge-success">Active Registered</span></td>
                    <td>
                        ${canEdit ? `<button class="btn btn-sm btn-outline" onclick="openEditBarangayModal(${bId})"><i class="fa-solid fa-pen"></i> Edit</button>` : '<span class="text-muted">—</span>'}
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (e) {
        console.warn('Failed loading barangay table:', e);
    }
}

function openEditBarangayModal(barangayId) {
    const rec = (state.barangayRecords || []).find(b => Number(b.barangay_id || b.barangayId) === Number(barangayId))
        || state.barangays.find(b => Number(b.barangayId) === Number(barangayId));
    if (!rec) return;
    document.getElementById('brgy-edit-id').value = rec.barangay_id || rec.barangayId;
    document.getElementById('brgy-edit-name').value = `${rec.barangay_name || rec.name} (${rec.municipality_name || rec.municipalityName})`;
    document.getElementById('brgy-edit-contact-person').value = rec.contact_person || rec.contactPerson || '';
    document.getElementById('brgy-edit-contact-number').value = rec.contact_number || rec.contactNumber || '';
    openModal('modal-edit-barangay');
}

async function handleUpdateBarangaySubmit(e) {
    e.preventDefault();
    const payload = {
        barangayId: parseInt(document.getElementById('brgy-edit-id').value),
        contactPerson: document.getElementById('brgy-edit-contact-person').value,
        contactNumber: document.getElementById('brgy-edit-contact-number').value
    };
    try {
        const res = await fetch(`${API_BASE}/barangays`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-edit-barangay');
            showToast(data.message || 'Barangay tourism record updated!', 'success');
            loadBarangayRecordsTable();
            loadBarangays();
            loadReportsData();
        } else {
            showToast(data.message || 'Failed updating barangay record.', 'error');
        }
    } catch (err) {
        showToast('Error updating barangay record: ' + err.message, 'error');
    }
}

const SETTING_LABELS = {
    appName: 'Platform Name',
    province: 'Provincial Jurisdiction',
    capacityThreshold: 'Carrying Capacity Alert Threshold',
    currency: 'Default Currency',
    broadcastMsg: 'Live Broadcast Notice'
};

function markSettingModified(settingKey) {
    if (!state.modifiedSettings) state.modifiedSettings = new Set();
    state.modifiedSettings.add(settingKey);
    state.lastEditedSetting = settingKey;
}

function applySystemSettingsToUI(s) {
    if (!s) return;
    if (s.appName !== undefined && document.getElementById('setting-app-name')) {
        document.getElementById('setting-app-name').value = s.appName;
    }
    if (s.province !== undefined && document.getElementById('setting-province')) {
        document.getElementById('setting-province').value = s.province;
    }
    if (s.capacityThreshold !== undefined && document.getElementById('setting-capacity-threshold')) {
        document.getElementById('setting-capacity-threshold').value = s.capacityThreshold;
    }
    if (s.currency !== undefined && document.getElementById('setting-currency')) {
        const currencyEl = document.getElementById('setting-currency');
        if (currencyEl.tagName === 'SELECT') {
            const exists = Array.from(currencyEl.options).some(opt => opt.value === s.currency);
            if (!exists && s.currency) {
                const opt = document.createElement('option');
                opt.value = s.currency;
                opt.textContent = s.currency;
                currencyEl.appendChild(opt);
            }
        }
        currencyEl.value = s.currency;
    }
    if (s.broadcastMsg !== undefined) {
        if (document.getElementById('setting-broadcast-msg')) {
            document.getElementById('setting-broadcast-msg').value = s.broadcastMsg;
        }
        const notice = document.getElementById('notice-text');
        if (notice && s.broadcastMsg) notice.textContent = s.broadcastMsg;
    }
}

async function loadSystemSettings() {
    try {
        const res = await fetch(`${API_BASE}/settings`);
        if (res.ok) {
            const s = await res.json();
            state.systemSettings = {
                appName: String(s.appName ?? 'Lakbay Lokal - Biliran Tourism Information System'),
                province: String(s.province ?? 'Biliran Province, Region VIII, Philippines'),
                capacityThreshold: String(s.capacityThreshold ?? 85),
                currency: String(s.currency ?? 'PHP (Philippine Peso ₱)'),
                broadcastMsg: String(s.broadcastMsg ?? '')
            };
            state.modifiedSettings = new Set();
            state.lastEditedSetting = null;
            applySystemSettingsToUI(state.systemSettings);
        }
    } catch (e) {
        console.warn('Settings load notice:', e);
    }
}

async function saveSystemSettings() {
    const appName = (document.getElementById('setting-app-name')?.value ?? '').trim() || 'Lakbay Lokal - Biliran Tourism Information System';
    const province = (document.getElementById('setting-province')?.value ?? '').trim() || 'Biliran Province, Region VIII, Philippines';
    const threshold = String(document.getElementById('setting-capacity-threshold')?.value ?? '85').trim() || '85';
    const currency = (document.getElementById('setting-currency')?.value ?? 'PHP (Philippine Peso ₱)').trim() || 'PHP (Philippine Peso ₱)';
    const msg = (document.getElementById('setting-broadcast-msg')?.value ?? '').trim();

    const prev = state.systemSettings || {};
    const changedKeys = [];
    if (prev.appName !== undefined && String(prev.appName) !== appName) changedKeys.push('appName');
    if (prev.province !== undefined && String(prev.province) !== province) changedKeys.push('province');
    if (prev.capacityThreshold !== undefined && String(prev.capacityThreshold) !== String(threshold)) changedKeys.push('capacityThreshold');
    if (prev.currency !== undefined && String(prev.currency) !== currency) changedKeys.push('currency');
    if (prev.broadcastMsg !== undefined && String(prev.broadcastMsg) !== msg) changedKeys.push('broadcastMsg');

    // If the user edited a field (even if set back to the same text) and clicked Save Settings, include that field
    if (changedKeys.length === 0) {
        if (state.modifiedSettings && state.modifiedSettings.size > 0) {
            state.modifiedSettings.forEach(k => changedKeys.push(k));
        } else if (state.lastEditedSetting) {
            changedKeys.push(state.lastEditedSetting);
        }
    }

    try {
        const res = await fetch(`${API_BASE}/settings`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify({
                appName,
                province,
                capacityThreshold: Number(threshold) || 85,
                currency,
                broadcastMsg: msg,
                updatedFields: changedKeys
            })
        });
        const data = await res.json();
        if (data.success) {
            const saved = data.settings || {
                appName,
                province,
                capacityThreshold: threshold,
                currency,
                broadcastMsg: msg
            };
            state.systemSettings = {
                appName: String(saved.appName),
                province: String(saved.province),
                capacityThreshold: String(saved.capacityThreshold),
                currency: String(saved.currency),
                broadcastMsg: String(saved.broadcastMsg)
            };
            state.modifiedSettings = new Set();
            state.lastEditedSetting = null;
            applySystemSettingsToUI(state.systemSettings);

            let toastMessage = data.message;
            if (!toastMessage) {
                const labels = changedKeys.map(k => SETTING_LABELS[k]).filter(Boolean);
                if (labels.length === 1) {
                    toastMessage = `${labels[0]} saved successfully!`;
                } else if (labels.length > 1) {
                    toastMessage = `System settings saved: ${labels.join(', ')}.`;
                } else {
                    toastMessage = 'System settings saved successfully!';
                }
            }
            showToast(toastMessage, 'success');
        } else {
            showToast(data.message || 'Failed saving settings.', 'error');
        }
    } catch (e) {
        showToast('Error saving settings: ' + e.message, 'error');
    }
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
    const annoId = parseInt(document.getElementById('anno-form-id').value) || 0;
    const categoryVal = document.getElementById('anno-form-type').value;
    const payload = {
        title: document.getElementById('anno-form-title').value,
        category: categoryVal,
        type: categoryVal,
        content: document.getElementById('anno-form-content').value,
        priority: 'NORMAL'
    };
    if (annoId > 0) payload.announcementId = annoId;

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
            showToast(data.message || 'Announcement posted successfully!', 'success');
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

function openEditUserModal(userId) {
    const u = (state.users || []).find(x => Number(x.userId) === Number(userId));
    if (!u) return;
    document.getElementById('edit-user-id').value = u.userId;
    document.getElementById('edit-user-username').value = u.username || '';
    document.getElementById('edit-user-fullname').value = u.fullName || '';
    document.getElementById('edit-user-email').value = u.email || '';
    document.getElementById('edit-user-role').value = u.roleId || 6;
    document.getElementById('edit-user-barangay').value = u.barangayId || '';
    openModal('modal-edit-user');
}

async function handleEditUserSubmit(e) {
    e.preventDefault();
    const brgyVal = document.getElementById('edit-user-barangay').value;
    const payload = {
        userId: parseInt(document.getElementById('edit-user-id').value),
        fullName: document.getElementById('edit-user-fullname').value,
        email: document.getElementById('edit-user-email').value,
        roleId: parseInt(document.getElementById('edit-user-role').value),
        barangayId: brgyVal ? parseInt(brgyVal) : null
    };
    try {
        const res = await fetch(`${API_BASE}/admin/users/update`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-edit-user');
            showToast(data.message || 'User account updated!', 'success');
            loadAdminData();
        } else {
            showToast(data.message || 'Failed updating user.', 'error');
        }
    } catch (err) {
        showToast('Error updating user: ' + err.message, 'error');
    }
}

function openAdminResetPasswordModal(userId, username) {
    document.getElementById('reset-pwd-user-id').value = userId;
    document.getElementById('reset-pwd-username').value = username;
    document.getElementById('reset-pwd-new').value = '';
    openModal('modal-reset-password');
}

async function handleAdminResetPasswordSubmit(e) {
    e.preventDefault();
    const payload = {
        userId: parseInt(document.getElementById('reset-pwd-user-id').value),
        newPassword: document.getElementById('reset-pwd-new').value
    };
    try {
        const res = await fetch(`${API_BASE}/admin/users/reset-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Auth-Token': state.authToken
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modal-reset-password');
            showToast(data.message || 'User password reset successfully!', 'success');
        } else {
            showToast(data.message || 'Failed resetting password.', 'error');
        }
    } catch (err) {
        showToast('Error resetting password: ' + err.message, 'error');
    }
}
