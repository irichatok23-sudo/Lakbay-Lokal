/**
 * ==============================================================================
 * LAKBAY LOKAL: SYSTEM-WIDE PDF REPORT & DOCUMENT GENERATOR
 * ==============================================================================
 * Uses jsPDF and jspdf-autotable (with pure-JS ISO 32000 fallback) to produce
 * properly formatted, publication-quality PDF (.pdf) documents across all dashboards.
 */

class LakbayPdfDocument {
    constructor(title, subtitle) {
        this.title = title || 'LAKBAY LOKAL — Official Tourism Report';
        this.subtitle = subtitle || 'Province of Biliran • Region VIII (Eastern Visayas)';
        this.pageWidth = 612;   // 8.5 x 11 inches (Letter)
        this.pageHeight = 792;
        this.margin = 36;
        this.pages = [];
        this.currentPage = null;
        this.curY = 0;
        this.newPage();
        this.drawHeader();
    }

    newPage() {
        this.currentPage = { stream: '' };
        this.pages.push(this.currentPage);
        this.curY = this.pageHeight - this.margin;
        if (this.pages.length > 1) {
            this.drawHeader();
        }
    }

    write(str) {
        this.currentPage.stream += str;
    }

    drawHeader() {
        // Dark teal branding bar
        this.drawRect(this.margin, this.curY - 32, this.pageWidth - 2 * this.margin, 32, 0.06, 0.44, 0.38, true);
        this.drawText(this.title, this.margin + 8, this.curY - 20, 11, true, 1, 1, 1);
        this.curY -= 40;

        const dateStr = new Date().toLocaleDateString('en-US', {
            year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
        });
        this.drawText(`Generated: ${dateStr}   |   ${this.subtitle}`, this.margin, this.curY, 8, false, 0.35, 0.35, 0.35);
        this.drawLine(this.margin, this.curY - 5, this.pageWidth - this.margin, this.curY - 5, 0.8, 0.8, 0.8, 0.75);
        this.curY -= 18;
    }

    drawLine(x1, y1, x2, y2, r = 0.8, g = 0.8, b = 0.8, w = 0.5) {
        this.write(`${w.toFixed(2)} w ${r.toFixed(2)} ${g.toFixed(2)} ${b.toFixed(2)} RG ${x1.toFixed(2)} ${y1.toFixed(2)} m ${x2.toFixed(2)} ${y2.toFixed(2)} l S\n`);
    }

    drawRect(x, y, w, h, r = 0.95, g = 0.95, b = 0.95, fill = false) {
        const op = fill ? 'rg' : 'RG';
        const end = fill ? 'f' : 's';
        this.write(`${r.toFixed(2)} ${g.toFixed(2)} ${b.toFixed(2)} ${op} ${x.toFixed(2)} ${y.toFixed(2)} ${w.toFixed(2)} ${h.toFixed(2)} re ${end}\n`);
    }

    drawText(text, x, y, size = 9, bold = false, r = 0, g = 0, b = 0) {
        const font = bold ? '/F2' : '/F1';
        const safe = this.escapePdf(text);
        this.write(`BT ${font} ${size.toFixed(2)} Tf ${r.toFixed(2)} ${g.toFixed(2)} ${b.toFixed(2)} rg ${x.toFixed(2)} ${y.toFixed(2)} Td (${safe}) Tj ET\n`);
    }

    escapePdf(str) {
        if (!str) return '';
        return String(str)
            .replace(/\\/g, '\\\\')
            .replace(/\(/g, '\\(')
            .replace(/\)/g, '\\)')
            .replace(/[^\x20-\x7E]/g, ' ');
    }

    addTable(headers, rows) {
        const availableWidth = this.pageWidth - 2 * this.margin;
        const cols = Math.max(1, headers.length);
        const colWidth = availableWidth / cols;
        const rowHeight = 20;

        if (this.curY - rowHeight < this.margin + 35) this.newPage();
        this.drawRect(this.margin, this.curY - rowHeight, availableWidth, rowHeight, 0.12, 0.22, 0.35, true);
        for (let i = 0; i < cols; i++) {
            let h = headers[i] || '';
            if (h.length > 22) h = h.substring(0, 20) + '..';
            this.drawText(h, this.margin + (i * colWidth) + 4, this.curY - 14, 8.5, true, 1, 1, 1);
        }
        this.curY -= rowHeight;

        let alt = false;
        for (const row of rows) {
            if (this.curY - rowHeight < this.margin + 35) {
                this.newPage();
                this.drawRect(this.margin, this.curY - rowHeight, availableWidth, rowHeight, 0.12, 0.22, 0.35, true);
                for (let i = 0; i < cols; i++) {
                    let h = headers[i] || '';
                    if (h.length > 22) h = h.substring(0, 20) + '..';
                    this.drawText(h, this.margin + (i * colWidth) + 4, this.curY - 14, 8.5, true, 1, 1, 1);
                }
                this.curY -= rowHeight;
            }

            if (alt) {
                this.drawRect(this.margin, this.curY - rowHeight, availableWidth, rowHeight, 0.96, 0.97, 0.98, true);
            }
            this.drawLine(this.margin, this.curY - rowHeight, this.pageWidth - this.margin, this.curY - rowHeight, 0.88, 0.88, 0.88, 0.5);

            for (let i = 0; i < Math.min(cols, row.length); i++) {
                let val = (row[i] !== null && row[i] !== undefined) ? String(row[i]).trim() : '';
                if (val.length > 25) val = val.substring(0, 22) + '..';
                this.drawText(val, this.margin + (i * colWidth) + 4, this.curY - 14, 8, false, 0.15, 0.15, 0.15);
            }
            this.curY -= rowHeight;
            alt = !alt;
        }
    }

    buildBlob() {
        const pageCount = this.pages.length;
        let body = '%PDF-1.4\n%\xE2\xE3\xCF\xD3\n';
        const offsets = [];

        function getOffset() { return body.length; }

        offsets.push(getOffset());
        body += '1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n';

        let kids = '[';
        for (let i = 0; i < pageCount; i++) kids += `${6 + i * 2} 0 R `;
        kids = kids.trim() + ']';

        offsets.push(getOffset());
        body += `2 0 obj\n<< /Type /Pages /Kids ${kids} /Count ${pageCount} >>\nendobj\n`;

        offsets.push(getOffset());
        body += '3 0 obj\n[/PDF /Text /ImageB /ImageC /ImageI]\nendobj\n';

        offsets.push(getOffset());
        body += '4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n';

        offsets.push(getOffset());
        body += '5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n';

        let nextObj = 6;
        for (let i = 0; i < pageCount; i++) {
            const pageObj = nextObj++;
            const contentObj = nextObj++;

            const footer = `BT /F1 7.5 Tf 0.45 0.45 0.45 rg ${this.margin.toFixed(2)} ${(this.margin - 14).toFixed(2)} Td (Lakbay Lokal Tourism Network • Biliran Province, Philippines  |  Page ${i + 1} of ${pageCount}) Tj ET\n`;
            const streamContent = this.pages[i].stream + footer;

            offsets.push(getOffset());
            body += `${pageObj} 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ${this.pageWidth} ${this.pageHeight}] ` +
                    `/Resources << /ProcSet 3 0 R /Font << /F1 4 0 R /F2 5 0 R >> >> ` +
                    `/Contents ${contentObj} 0 R >>\nendobj\n`;

            offsets.push(getOffset());
            body += `${contentObj} 0 obj\n<< /Length ${streamContent.length} >>\nstream\n${streamContent}\nendstream\nendobj\n`;
        }

        const xrefOffset = getOffset();
        const totalObjs = nextObj;
        body += `xref\n0 ${totalObjs}\n0000000000 65535 f \n`;
        for (const off of offsets) {
            body += String(off).padStart(10, '0') + ' 00000 n \n';
        }
        body += `trailer\n<< /Size ${totalObjs} /Root 1 0 R >>\nstartxref\n${xrefOffset}\n%%EOF\n`;

        return new Blob([body], { type: 'application/pdf' });
    }

    download(filename) {
        const safeName = filename.endsWith('.pdf') ? filename : `${filename}.pdf`;
        const blob = this.buildBlob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = safeName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        setTimeout(() => URL.revokeObjectURL(url), 1000);
    }
}

/**
 * Universal PDF export function using jsPDF + autoTable when available,
 * with seamless fallback to LakbayPdfDocument.
 */
function exportTableToPdf(tableId, reportTitle, filenamePrefix, customMetadata = {}) {
    const table = document.getElementById(tableId);
    if (!table) {
        if (typeof showToast === 'function') showToast('Report table not found.', 'warning');
        return;
    }

    const headerCells = Array.from(table.querySelectorAll('thead th'));
    const headers = headerCells.map(th => th.innerText.replace(/\s+/g, ' ').trim());

    const rows = [];
    const trs = Array.from(table.querySelectorAll('tbody tr'));
    trs.forEach(tr => {
        const cells = Array.from(tr.querySelectorAll('td'));
        if (cells.length > 0 && !tr.querySelector('.text-center.text-muted')) {
            rows.push(cells.map(td => td.innerText.replace(/\s+/g, ' ').trim()));
        }
    });

    if (rows.length === 0) {
        if (typeof showToast === 'function') showToast('No report records to export.', 'warning');
        return;
    }

    const activeRole = (window.state && window.state.activeRole) ? window.state.activeRole : 'OFFICIAL';
    const activeUser = (window.state && window.state.currentUser) ? (window.state.currentUser.fullName || window.state.currentUser.username) : 'User';
    const dateStr = new Date().toISOString().split('T')[0];
    const timeStr = new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    const fullDateStr = new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
    const prefix = filenamePrefix || 'Lakbay_Lokal_Report';
    const filename = `${prefix}_${dateStr}.pdf`;

    // Extract active filter information if present
    const filterInfo = [];
    if (customMetadata.filters) {
        filterInfo.push(customMetadata.filters);
    } else {
        const muniFilter = document.getElementById('dest-municipality-filter')?.value;
        const catFilter = document.getElementById('dest-category-filter')?.value;
        const statusFilter = document.getElementById('bookings-status-filter')?.value || document.getElementById('schedule-status-filter')?.value;
        if (muniFilter && muniFilter !== 'ALL') filterInfo.push(`Municipality: ${muniFilter}`);
        if (catFilter && catFilter !== 'ALL') filterInfo.push(`Category: ${catFilter}`);
        if (statusFilter && statusFilter !== 'ALL') filterInfo.push(`Status: ${statusFilter}`);
    }
    const filterSubtitle = filterInfo.length > 0 ? `Filters: [${filterInfo.join(' | ')}]` : 'Scope: Province-wide (All Records)';

    // Preferred path: jsPDF + autoTable
    const jspdfModule = (window.jspdf && window.jspdf.jsPDF) ? window.jspdf : (window.jsPDF ? { jsPDF: window.jsPDF } : null);
    if (jspdfModule) {
        try {
            const { jsPDF } = jspdfModule;
            const doc = new jsPDF({ orientation: headers.length > 6 ? 'landscape' : 'portrait', unit: 'pt', format: 'letter' });

            // Brand Banner Header
            const pageWidth = doc.internal.pageSize.getWidth();
            const pageHeight = doc.internal.pageSize.getHeight();
            doc.setFillColor(15, 118, 110); // Teal 700
            doc.rect(36, 36, pageWidth - 72, 36, 'F');

            // Header Text
            doc.setFont('helvetica', 'bold');
            doc.setFontSize(12);
            doc.setTextColor(255, 255, 255);
            doc.text('LAKBAY LOKAL — ' + (reportTitle || 'Community Tourism Report').toUpperCase(), 46, 58);

            // Subtitle & Metadata
            doc.setFont('helvetica', 'normal');
            doc.setFontSize(8.5);
            doc.setTextColor(71, 85, 105); // Slate 600
            doc.text(`Generated: ${fullDateStr} at ${timeStr}   |   Role: ${activeRole}   |   ${filterSubtitle}`, 36, 88);
            doc.setDrawColor(203, 213, 225); // Slate 300
            doc.setLineWidth(0.75);
            doc.line(36, 94, pageWidth - 36, 94);

            // Render autoTable
            if (typeof doc.autoTable === 'function') {
                doc.autoTable({
                    head: [headers],
                    body: rows,
                    startY: 104,
                    margin: { left: 36, right: 36, bottom: 45 },
                    theme: 'grid',
                    headStyles: {
                        fillColor: [30, 41, 59], // Slate 800
                        textColor: [255, 255, 255],
                        fontStyle: 'bold',
                        fontSize: 8.5,
                        halign: 'left'
                    },
                    bodyStyles: {
                        fontSize: 8,
                        textColor: [30, 41, 59],
                        cellPadding: 4
                    },
                    alternateRowStyles: {
                        fillColor: [248, 250, 252] // Slate 50
                    },
                    didDrawPage: function(data) {
                        // Running Footer on each page
                        const str = `Lakbay Lokal Tourism Network • Biliran Province, Region VIII   |   Page ${doc.internal.getNumberOfPages()}`;
                        doc.setFontSize(7.5);
                        doc.setTextColor(148, 163, 184);
                        doc.text(str, 36, pageHeight - 20);
                    }
                });
            }

            doc.save(filename);
            if (typeof showToast === 'function') {
                showToast(`Official PDF report exported successfully! (${rows.length} records)`, 'success');
            }
            return;
        } catch (e) {
            console.warn('[pdf-export] jsPDF rendering issue, using robust built-in fallback:', e);
        }
    }

    // Fallback path: Pure-JS ISO 32000 PDF engine
    const subtitle = `Role: ${activeRole}  |  Generated: ${dateStr}  |  ${filterSubtitle}`;
    const pdf = new LakbayPdfDocument(reportTitle || 'Lakbay Lokal Tourism Report', subtitle);
    pdf.addTable(headers, rows);
    pdf.download(filename);

    if (typeof showToast === 'function') {
        showToast(`Official PDF report exported successfully! (${rows.length} records)`, 'success');
    }
}

/**
 * Primary export handler called by dashboard buttons
 */
function exportReportPDF() {
    const activeTab = (window.state && window.state.activeTab) ? window.state.activeTab : 'reports';
    const role = (window.state && window.state.activeRole) ? window.state.activeRole : '';
    const dateStr = new Date().toISOString().split('T')[0];

    if (activeTab === 'lgu-monitoring') {
        exportTableToPdf(
            'footfall-report-table',
            'LGU & Tourism Office Strategic Monitoring Report',
            `Lakbay_Lokal_LGU_Monitoring_${dateStr}`
        );
    } else if (activeTab === 'barangay-records') {
        exportTableToPdf(
            'barangay-full-table',
            'Barangay Tourism Records & Stakeholder Directory',
            `Lakbay_Lokal_Barangay_Report_${dateStr}`
        );
    } else if (activeTab === 'verifications') {
        exportTableToPdf(
            'verifications-table',
            'Community Worker Accreditation & Verification Clearances',
            `Lakbay_Lokal_Barangay_Clearances_${dateStr}`
        );
    } else if (activeTab === 'bookings') {
        exportTableToPdf(
            'bookings-table',
            'Tourism Reservations & Bookings Ledger',
            `Lakbay_Lokal_Bookings_Report_${dateStr}`
        );
    } else if (activeTab === 'schedule') {
        exportTableToPdf(
            'schedule-table',
            'Stakeholder Service Tour Schedule & Assignments',
            `Lakbay_Lokal_Stakeholder_Schedule_${dateStr}`
        );
    } else if (activeTab === 'earnings') {
        exportTableToPdf(
            'transactions-table',
            'Stakeholder Livelihood Earnings & Transaction History',
            `Lakbay_Lokal_Stakeholder_Earnings_${dateStr}`
        );
    } else if (activeTab === 'admin') {
        exportTableToPdf(
            'users-table',
            'User Accounts Registry & Role Matrix',
            `Lakbay_Lokal_Admin_User_Registry_${dateStr}`
        );
    } else if (activeTab === 'audit-logs') {
        exportTableToPdf(
            'audit-table',
            'System Security & Activity Audit Trail',
            `Lakbay_Lokal_Admin_Audit_Logs_${dateStr}`
        );
    } else {
        // Default to footfall report table in reports pane
        exportTableToPdf(
            'footfall-report-table',
            'Destination Footfall & Revenue Summary Report',
            `Lakbay_Lokal_Tourism_Report_${dateStr}`
        );
    }
}

/**
 * Dedicated Booking Voucher PDF Export for Tourist and Stakeholder
 */
function exportBookingVoucherPDF(bookingId) {
    const b = (window.state && window.state.bookings)
        ? window.state.bookings.find(x => Number(x.bookingId) === Number(bookingId))
        : null;
    if (!b) {
        if (typeof showToast === 'function') showToast('Booking voucher record not found.', 'error');
        return;
    }

    const jspdfModule = (window.jspdf && window.jspdf.jsPDF) ? window.jspdf : (window.jsPDF ? { jsPDF: window.jsPDF } : null);
    const dateStr = new Date().toISOString().split('T')[0];
    const filename = `Lakbay_Lokal_Voucher_${b.bookingReference}_${dateStr}.pdf`;

    if (jspdfModule) {
        try {
            const { jsPDF } = jspdfModule;
            const doc = new jsPDF({ orientation: 'portrait', unit: 'pt', format: 'letter' });
            const pageWidth = doc.internal.pageSize.getWidth();

            // Brand Header Banner
            doc.setFillColor(15, 118, 110);
            doc.rect(36, 36, pageWidth - 72, 40, 'F');
            doc.setFont('helvetica', 'bold');
            doc.setFontSize(13);
            doc.setTextColor(255, 255, 255);
            doc.text('LAKBAY LOKAL — OFFICIAL TRAVEL VOUCHER', 48, 61);

            // Voucher Details Box
            doc.setFillColor(248, 250, 252);
            doc.setDrawColor(203, 213, 225);
            doc.rect(36, 88, pageWidth - 72, 100, 'FD');

            doc.setFont('helvetica', 'bold');
            doc.setFontSize(10);
            doc.setTextColor(15, 118, 110);
            doc.text(`Booking Reference: ${b.bookingReference}`, 48, 108);

            doc.setFont('helvetica', 'normal');
            doc.setFontSize(9);
            doc.setTextColor(51, 65, 85);
            doc.text(`Tourist Name: ${b.touristName || 'Traveler'}`, 48, 126);
            doc.text(`Package / Circuit: ${b.packageName || 'Custom Experience Circuit'}`, 48, 142);
            doc.text(`Travel Dates: ${b.travelDateStart} to ${b.travelDateEnd}`, 48, 158);
            doc.text(`Number of Pax: ${b.numberOfPax} travelers`, 48, 174);

            doc.text(`Status: ${b.status}`, 350, 126);
            doc.setFont('helvetica', 'bold');
            doc.text(`Total Amount: PHP ${Number(b.totalAmount || 0).toFixed(2)}`, 350, 158);

            // Items Table
            const items = b.items || [];
            const itemHeaders = ['Service', 'Provider / Stakeholder', 'Qty', 'Unit Price', 'Subtotal', 'Status'];
            const itemRows = items.map(it => [
                it.serviceName || 'Service',
                it.stakeholderName || it.businessName || '—',
                String(it.quantity || 1),
                `PHP ${Number(it.unitPrice || 0).toFixed(2)}`,
                `PHP ${Number(it.subtotal || 0).toFixed(2)}`,
                it.status || 'PENDING'
            ]);

            if (itemRows.length > 0 && typeof doc.autoTable === 'function') {
                doc.autoTable({
                    head: [itemHeaders],
                    body: itemRows,
                    startY: 202,
                    margin: { left: 36, right: 36 },
                    headStyles: { fillColor: [30, 41, 59], textColor: [255, 255, 255], fontStyle: 'bold' },
                    alternateRowStyles: { fillColor: [248, 250, 252] }
                });
            }

            doc.save(filename);
            if (typeof showToast === 'function') showToast('Travel voucher downloaded as PDF!', 'success');
            return;
        } catch (e) {
            console.warn('[pdf-export] Error generating voucher via jsPDF, falling back:', e);
        }
    }

    // Fallback: built-in generator
    const pdf = new LakbayPdfDocument('LAKBAY LOKAL — TRAVEL VOUCHER', `Booking: ${b.bookingReference}`);
    pdf.addTextContent(`
BOOKING REFERENCE: ${b.bookingReference}
Tourist: ${b.touristName || 'Traveler'}
Package: ${b.packageName || 'Custom Circuit'}
Travel Dates: ${b.travelDateStart} to ${b.travelDateEnd}
Number of Pax: ${b.numberOfPax}
Total Amount: PHP ${Number(b.totalAmount || 0).toFixed(2)}
Status: ${b.status}
    `);
    pdf.download(filename);
    if (typeof showToast === 'function') showToast('Travel voucher downloaded as PDF!', 'success');
}

// Retain alias for existing callers
function exportReportCSV() {
    exportReportPDF();
}
