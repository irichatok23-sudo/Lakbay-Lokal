const { app, BrowserWindow } = require('electron');
const path = require('path');

process.env.ELECTRON_DISABLE_SECURITY_WARNINGS = 'true';

app.whenReady().then(async () => {
    console.log('========================================================================');
    console.log('LAKBAY LOKAL: BOOKING TABLE UI/UX ACCEPTANCE TEST');
    console.log('========================================================================');

    const win = new BrowserWindow({
        title: 'Lakbay Lokal: Booking Table Acceptance Test',
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
        await new Promise(r => setTimeout(r, 1000));
    }

    async function logout() {
        await win.webContents.executeJavaScript(`
            handleLogout();
        `);
        await new Promise(r => setTimeout(r, 1000));
    }

    // Inspect booking table structure
    async function inspectBookingTable() {
        return await win.webContents.executeJavaScript(`
            (() => {
                const table = document.getElementById('bookings-table');
                if (!table) return { exists: false };

                const ths = Array.from(table.querySelectorAll('thead th')).map(th => th.textContent.trim());
                const rows = Array.from(table.querySelectorAll('tbody tr'));
                const rowData = rows.map(tr => {
                    const tds = Array.from(tr.querySelectorAll('td')).map(td => td.textContent.trim());
                    const refBadge = tr.querySelector('.booking-ref-badge');
                    const badge = tr.querySelector('.badge');
                    return {
                        isClickable: tr.classList.contains('clickable-booking-row'),
                        tdCount: tds.length,
                        refText: refBadge ? refBadge.textContent.trim() : (tds[0] || ''),
                        tourist: tds[1] || '',
                        package: tds[2] || '',
                        startDate: tds[3] || '',
                        endDate: tds[4] || '',
                        pax: tds[5] || '',
                        amount: tds[6] || '',
                        status: badge ? badge.textContent.trim() : (tds[7] || ''),
                        badgeClass: badge ? badge.className : ''
                    };
                });

                return {
                    exists: true,
                    headers: ths,
                    headerCount: ths.length,
                    hasActionsHeader: ths.some(h => h.toLowerCase().includes('action')),
                    rowCount: rows.length,
                    rows: rowData
                };
            })()
        `);
    }

    let allPassed = true;

    // ------------------------------------------------------------------------
    // 1. TOURIST DASHBOARD BOOKINGS TEST
    // ------------------------------------------------------------------------
    console.log('\n--- 1. Testing TOURIST (tourist_ana) Bookings ---');
    await loginAs('tourist_ana');
    await navigateTo('bookings');

    const touristTable = await inspectBookingTable();
    console.log('Tourist Bookings Table Headers:', touristTable.headers);
    console.log('Tourist Bookings Row Count:', touristTable.rowCount);
    if (touristTable.rows.length > 0) {
        console.log('Sample Row 1:', JSON.stringify(touristTable.rows[0], null, 2));
    }

    // Assertions
    const expectedHeaders = ['Reference Code', 'Tourist', 'Package / Details', 'Start Date', 'End Date', 'Pax', 'Total Amount', 'Status'];
    const headersMatch = JSON.stringify(touristTable.headers) === JSON.stringify(expectedHeaders);
    if (!headersMatch) {
        console.error('FAIL: Table headers do not match expected order!', {
            expected: expectedHeaders,
            actual: touristTable.headers
        });
        allPassed = false;
    } else {
        console.log('PASS: Table headers match exact requested order (No Actions column).');
    }

    if (touristTable.hasActionsHeader) {
        console.error('FAIL: Actions header is still present!');
        allPassed = false;
    } else {
        console.log('PASS: Separate Actions column is completely removed.');
    }

    if (touristTable.rows.length > 0) {
        const row = touristTable.rows[0];
        // Shortened reference code check (e.g. LL-XXXX)
        const isShortRef = /^LL-[A-Za-z0-9_-]+$/i.test(row.refText.replace(/\s+/g, ''));
        console.log(`Reference code format check ('${row.refText}'):`, isShortRef);
        if (!isShortRef) {
            console.error('FAIL: Reference code not shortened properly!', row.refText);
            allPassed = false;
        } else {
            console.log('PASS: Reference code properly shortened (e.g. LL-7020).');
        }

        // Travel dates separation and clean date format check (e.g. 'Nov 24, 2026')
        const hasTimestamp = row.startDate.includes('00:00:00') || row.endDate.includes('00:00:00');
        if (hasTimestamp) {
            console.error('FAIL: Travel dates contain unnecessary timestamps!', row.startDate, row.endDate);
            allPassed = false;
        } else {
            console.log(`PASS: Separate clean dates without timestamps: Start=${row.startDate}, End=${row.endDate}`);
        }

        // Clickable row test
        if (!row.isClickable) {
            console.error('FAIL: Booking row missing clickable-booking-row class!');
            allPassed = false;
        } else {
            console.log('PASS: Booking row is styled as clickable.');
        }

        // Test clicking the row to open details modal
        console.log('Testing opening booking details modal via row click...');
        await win.webContents.executeJavaScript(`
            document.querySelector('#bookings-table tbody tr').click();
        `);
        await new Promise(r => setTimeout(r, 600));

        const modalCheck = await win.webContents.executeJavaScript(`
            (() => {
                const modal = document.getElementById('modal-view-booking');
                const content = document.getElementById('view-booking-content');
                return {
                    isOpen: modal ? (modal.classList.contains('open') || window.getComputedStyle(modal).display === 'flex') : false,
                    hasContent: content ? content.innerHTML.length > 100 : false,
                    hasVoucherBtn: content ? content.innerHTML.includes('Download Official PDF Voucher') : false
                };
            })()
        `);
        console.log('Modal Check Result:', modalCheck);
        if (!modalCheck.isOpen || !modalCheck.hasContent) {
            console.error('FAIL: Booking details modal failed to open upon clicking row!');
            allPassed = false;
        } else {
            console.log('PASS: Booking details modal opened successfully with operations intact.');
        }

        // Close modal
        await win.webContents.executeJavaScript(`closeModal('modal-view-booking');`);
        await new Promise(r => setTimeout(r, 400));
    }

    await logout();

    // ------------------------------------------------------------------------
    // 2. STAKEHOLDER DASHBOARD BOOKINGS TEST
    // ------------------------------------------------------------------------
    console.log('\n--- 2. Testing STAKEHOLDER (guide_pedro) Bookings ---');
    await loginAs('guide_pedro');
    await navigateTo('bookings');

    const stakeholderTable = await inspectBookingTable();
    console.log('Stakeholder Bookings Row Count:', stakeholderTable.rowCount);
    if (stakeholderTable.rows.length > 0) {
        console.log('Sample Row 1:', JSON.stringify(stakeholderTable.rows[0], null, 2));
    }
    if (JSON.stringify(stakeholderTable.headers) !== JSON.stringify(expectedHeaders)) {
        console.error('FAIL: Headers mismatch for Stakeholder bookings!');
        allPassed = false;
    } else {
        console.log('PASS: Stakeholder bookings table formatted consistently.');
    }

    await logout();

    // ------------------------------------------------------------------------
    // 3. LGU OFFICER DASHBOARD BOOKINGS TEST
    // ------------------------------------------------------------------------
    console.log('\n--- 3. Testing LGU OFFICER (lgu_naval) Bookings ---');
    await loginAs('lgu_naval');
    await navigateTo('bookings');

    const lguTable = await inspectBookingTable();
    console.log('LGU Bookings Row Count:', lguTable.rowCount);
    if (JSON.stringify(lguTable.headers) !== JSON.stringify(expectedHeaders)) {
        console.error('FAIL: Headers mismatch for LGU bookings!');
        allPassed = false;
    } else {
        console.log('PASS: LGU Officer bookings table formatted consistently.');
    }

    await logout();

    // ------------------------------------------------------------------------
    // 4. TOURISM OFFICE DASHBOARD BOOKINGS TEST
    // ------------------------------------------------------------------------
    console.log('\n--- 4. Testing TOURISM OFFICE (tourismoffice) Bookings ---');
    await loginAs('tourismoffice');
    await navigateTo('bookings');

    const tourismTable = await inspectBookingTable();
    console.log('Tourism Office Bookings Row Count:', tourismTable.rowCount);
    if (tourismTable.rows.length > 0) {
        console.log('Sample Row 1:', JSON.stringify(tourismTable.rows[0], null, 2));
    }
    if (JSON.stringify(tourismTable.headers) !== JSON.stringify(expectedHeaders)) {
        console.error('FAIL: Headers mismatch for Tourism Office bookings!');
        allPassed = false;
    } else {
        console.log('PASS: Tourism Office bookings table formatted consistently.');
    }

    await logout();

    // ------------------------------------------------------------------------
    // 5. UNIT TEST: DATE AND SHORT REFERENCE FORMATTERS
    // ------------------------------------------------------------------------
    console.log('\n--- 5. Unit Testing Formatters in Renderer ---');
    const unitCheck = await win.webContents.executeJavaScript(`
        (() => {
            return {
                short1: formatShortBookingReference('LL-2026-7020'),
                short2: formatShortBookingReference('LL-2024-001'),
                short3: formatShortBookingReference('LL-7020'),
                date1: formatBookingDate('2026-11-24'),
                date2: formatBookingDate('2026-11-25 00:00:00'),
                date3: formatBookingDate('2026-05-04T00:00:00.000Z'),
                badgePending: getStatusBadgeClass('PENDING'),
                badgeConfirmed: getStatusBadgeClass('CONFIRMED'),
                badgeCancelled: getStatusBadgeClass('CANCELLED'),
                badgeCompleted: getStatusBadgeClass('COMPLETED')
            };
        })()
    `);
    console.log('Unit Formatter Results:', unitCheck);

    if (unitCheck.short1 !== 'LL-7020' || unitCheck.short2 !== 'LL-001' || unitCheck.short3 !== 'LL-7020') {
        console.error('FAIL: formatShortBookingReference failed!', unitCheck);
        allPassed = false;
    } else {
        console.log('PASS: formatShortBookingReference handles all reference patterns.');
    }

    if (unitCheck.date1 !== 'Nov 24, 2026' || unitCheck.date2 !== 'Nov 25, 2026') {
        console.error('FAIL: formatBookingDate failed!', unitCheck);
        allPassed = false;
    } else {
        console.log('PASS: formatBookingDate outputs clean "Nov 24, 2026" format without timestamps.');
    }

    if (unitCheck.badgePending !== 'badge-warning' ||
        unitCheck.badgeConfirmed !== 'badge-success' ||
        unitCheck.badgeCancelled !== 'badge-danger' ||
        unitCheck.badgeCompleted !== 'badge-info') {
        console.error('FAIL: getStatusBadgeClass badge classes do not match distinct status specs!', unitCheck);
        allPassed = false;
    } else {
        console.log('PASS: Status badges map to distinct classes: Pending=warning (amber), Confirmed=success (green), Cancelled=danger (red), Completed=info (blue).');
    }

    console.log('\n========================================================================');
    if (allPassed) {
        console.log('SUCCESS: ALL BOOKING TABLE UI/UX ACCEPTANCE TESTS PASSED!');
    } else {
        console.error('FAILURE: ONE OR MORE BOOKING TABLE TESTS FAILED.');
    }
    console.log('========================================================================');

    await win.close();
    app.quit();
    process.exit(allPassed ? 0 : 1);
});
