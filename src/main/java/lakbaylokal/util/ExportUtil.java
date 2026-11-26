package lakbaylokal.util;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Utility for exporting reports, analytics, and table datasets to genuine,
 * properly formatted PDF files (.pdf) conforming to ISO 32000 / PDF-1.4 standards.
 */
public class ExportUtil {

    private static final String DEFAULT_REPORTS_DIR = "reports";

    public static File getReportsDirectory() {
        File dir = new File(DEFAULT_REPORTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    // -------------------------------------------------------------------------
    // TABLE EXPORT TO PDF (Replacing legacy CSV with formatted PDF)
    // -------------------------------------------------------------------------

    public static boolean exportTableToCSV(JTable table, String baseName) {
        return exportTableToPDF(null, table, baseName, baseName.replace('_', ' '));
    }

    public static boolean exportTableToCSV(Component parent, JTable table, String baseName) {
        return exportTableToPDF(parent, table, baseName, baseName.replace('_', ' '));
    }

    public static boolean exportTableToCSV(Component parent, JTable table, String baseName, String reportTitle) {
        return exportTableToPDF(parent, table, baseName, reportTitle);
    }

    public static boolean exportTableToPDF(JTable table, String baseName) {
        return exportTableToPDF(null, table, baseName, baseName.replace('_', ' '));
    }

    public static boolean exportTableToPDF(Component parent, JTable table, String baseName, String reportTitle) {
        if (table == null || table.getModel() == null) {
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(parent, "No data available to export.", "Export Notice", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
        String defaultFileName = "Lakbay_Lokal_" + baseName + "_" + timestamp + ".pdf";
        File defaultFile = new File(getReportsDirectory(), defaultFileName);
        File targetFile = defaultFile;

        if (!GraphicsEnvironment.isHeadless() && parent != null) {
            JFileChooser chooser = new JFileChooser(getReportsDirectory());
            chooser.setSelectedFile(defaultFile);
            chooser.setDialogTitle("Export Report to PDF — Lakbay Lokal");

            int userSelection = chooser.showSaveDialog(parent);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return false;
            }

            targetFile = chooser.getSelectedFile();
            if (!targetFile.getName().toLowerCase().endsWith(".pdf")) {
                targetFile = new File(targetFile.getParentFile(), targetFile.getName() + ".pdf");
            }
        }

        try {
            TableModel model = table.getModel();
            int colCount = model.getColumnCount();
            int rowCount = model.getRowCount();

            List<String> headers = new ArrayList<>();
            for (int col = 0; col < colCount; col++) {
                headers.add(model.getColumnName(col));
            }

            List<List<String>> rows = new ArrayList<>();
            for (int row = 0; row < rowCount; row++) {
                List<String> r = new ArrayList<>();
                for (int col = 0; col < colCount; col++) {
                    Object val = model.getValueAt(row, col);
                    r.add(val != null ? val.toString() : "");
                }
                rows.add(r);
            }

            String title = (reportTitle != null && !reportTitle.isBlank()) ? reportTitle : baseName.replace('_', ' ');
            PdfReportBuilder builder = new PdfReportBuilder("LAKBAY LOKAL — " + title, "Province of Biliran • Region VIII (Eastern Visayas)");
            builder.addTable(headers, rows);
            byte[] pdfBytes = builder.build();

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(pdfBytes);
            }

            if (!GraphicsEnvironment.isHeadless() && parent != null) {
                int choice = JOptionPane.showOptionDialog(
                        parent,
                        "Report successfully exported as PDF:\n" + targetFile.getAbsolutePath() + "\n\nTotal records exported: " + rowCount,
                        "Export Successful",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[]{"Open File Location", "OK"},
                        "OK"
                );

                if (choice == 0 && Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(targetFile.getParentFile());
                    } catch (Exception ignored) {}
                }
            }
            return true;
        } catch (Exception ex) {
            System.err.println("[ExportUtil] PDF Export error: " + ex.getMessage());
            if (!GraphicsEnvironment.isHeadless() && parent != null) {
                JOptionPane.showMessageDialog(parent, "Failed to export PDF: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // TEXT REPORT EXPORT TO PDF (Vouchers, Official Summaries, DOT Forms)
    // -------------------------------------------------------------------------

    public static boolean exportTextReport(String reportContent, String baseName) {
        return exportTextReport(null, reportContent, baseName, baseName.replace('_', ' '));
    }

    public static boolean exportTextReport(Component parent, String reportContent, String baseName) {
        return exportTextReport(parent, reportContent, baseName, baseName.replace('_', ' '));
    }

    public static boolean exportTextReport(Component parent, String reportContent, String baseName, String reportTitle) {
        if (reportContent == null || reportContent.isBlank()) {
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(parent, "Report content is empty.", "Export Notice", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
        String defaultFileName = "Lakbay_Lokal_" + baseName + "_" + timestamp + ".pdf";
        File defaultFile = new File(getReportsDirectory(), defaultFileName);
        File targetFile = defaultFile;

        if (!GraphicsEnvironment.isHeadless() && parent != null) {
            JFileChooser chooser = new JFileChooser(getReportsDirectory());
            chooser.setSelectedFile(defaultFile);
            chooser.setDialogTitle("Export Official Report to PDF — Lakbay Lokal");

            int userSelection = chooser.showSaveDialog(parent);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return false;
            }

            targetFile = chooser.getSelectedFile();
            if (!targetFile.getName().toLowerCase().endsWith(".pdf")) {
                targetFile = new File(targetFile.getParentFile(), targetFile.getName() + ".pdf");
            }
        }

        try {
            String title = (reportTitle != null && !reportTitle.isBlank()) ? reportTitle : baseName.replace('_', ' ');
            PdfReportBuilder builder = new PdfReportBuilder("LAKBAY LOKAL — " + title, "Official Provincial Tourism Record • Biliran Province");
            builder.addTextContent(reportContent);
            byte[] pdfBytes = builder.build();

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(pdfBytes);
            }

            if (!GraphicsEnvironment.isHeadless() && parent != null) {
                int choice = JOptionPane.showOptionDialog(
                        parent,
                        "Official report successfully exported as PDF:\n" + targetFile.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[]{"Open File Location", "OK"},
                        "OK"
                );

                if (choice == 0 && Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(targetFile.getParentFile());
                    } catch (Exception ignored) {}
                }
            }
            return true;
        } catch (Exception ex) {
            System.err.println("[ExportUtil] PDF Text Export error: " + ex.getMessage());
            if (!GraphicsEnvironment.isHeadless() && parent != null) {
                JOptionPane.showMessageDialog(parent, "Failed to export report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // EMBEDDED PURE-JAVA ISO 32000 PDF-1.4 GENERATOR
    // -------------------------------------------------------------------------

    public static class PdfReportBuilder {
        private final String title;
        private final String subtitle;
        private final List<PageContent> pages = new ArrayList<>();
        private PageContent currentPage;
        private final float pageWidth = 612;  // Letter width in points (8.5 in * 72 pt/in)
        private final float pageHeight = 792; // Letter height in points (11.0 in * 72 pt/in)
        private final float margin = 36;      // 0.5 in margin
        private float curY;

        static class PageContent {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
        }

        public PdfReportBuilder(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
            newPage();
            drawHeader();
        }

        private void newPage() {
            currentPage = new PageContent();
            pages.add(currentPage);
            curY = pageHeight - margin;
            if (pages.size() > 1) {
                drawHeader();
            }
        }

        private void drawHeader() {
            // Dark Teal branding banner
            drawRect(margin, curY - 32, pageWidth - 2 * margin, 32, 0.06f, 0.44f, 0.38f, true);
            drawText(title, margin + 8, curY - 20, 11, true, 1f, 1f, 1f);
            curY -= 40;

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy • HH:mm:ss"));
            drawText("Generated: " + dateStr + "   |   " + subtitle, margin, curY, 8, false, 0.35f, 0.35f, 0.35f);
            drawLine(margin, curY - 5, pageWidth - margin, curY - 5, 0.8f, 0.8f, 0.8f, 0.75f);
            curY -= 18;
        }

        public void addTable(List<String> headers, List<List<String>> rows) {
            float availableWidth = pageWidth - 2 * margin;
            int cols = Math.max(1, headers.size());
            float colWidth = availableWidth / cols;
            float rowHeight = 20;

            // Header row
            if (curY - rowHeight < margin + 35) newPage();
            drawRect(margin, curY - rowHeight, availableWidth, rowHeight, 0.12f, 0.22f, 0.35f, true);
            for (int i = 0; i < cols; i++) {
                String h = headers.get(i);
                if (h.length() > 22) h = h.substring(0, 20) + "..";
                drawText(h, margin + (i * colWidth) + 4, curY - 14, 8.5f, true, 1f, 1f, 1f);
            }
            curY -= rowHeight;

            boolean alt = false;
            for (List<String> row : rows) {
                if (curY - rowHeight < margin + 35) {
                    newPage();
                    drawRect(margin, curY - rowHeight, availableWidth, rowHeight, 0.12f, 0.22f, 0.35f, true);
                    for (int i = 0; i < cols; i++) {
                        String h = headers.get(i);
                        if (h.length() > 22) h = h.substring(0, 20) + "..";
                        drawText(h, margin + (i * colWidth) + 4, curY - 14, 8.5f, true, 1f, 1f, 1f);
                    }
                    curY -= rowHeight;
                }

                if (alt) {
                    drawRect(margin, curY - rowHeight, availableWidth, rowHeight, 0.96f, 0.97f, 0.98f, true);
                }
                drawLine(margin, curY - rowHeight, pageWidth - margin, curY - rowHeight, 0.88f, 0.88f, 0.88f, 0.5f);

                for (int i = 0; i < Math.min(cols, row.size()); i++) {
                    String val = row.get(i) != null ? row.get(i).trim() : "";
                    if (val.length() > 24) val = val.substring(0, 22) + "..";
                    drawText(val, margin + (i * colWidth) + 4, curY - 14, 8f, false, 0.15f, 0.15f, 0.15f);
                }
                curY -= rowHeight;
                alt = !alt;
            }
        }

        public void addTextContent(String text) {
            String[] lines = text.split("\n");
            float lineHeight = 13;

            for (String line : lines) {
                String trimmed = line.trim();
                if (curY - lineHeight < margin + 35) {
                    newPage();
                }

                if (trimmed.startsWith("#") || trimmed.startsWith("===") || trimmed.startsWith("---")) {
                    // Section divider
                    curY -= 4;
                    drawLine(margin, curY, pageWidth - margin, curY, 0.75f, 0.75f, 0.75f, 0.75f);
                    curY -= 6;
                    continue;
                }

                boolean isHeading = trimmed.startsWith("[") || (trimmed.endsWith(":") && trimmed.length() < 50);
                if (isHeading) {
                    curY -= 4;
                    drawText(trimmed, margin, curY - 10, 9f, true, 0.08f, 0.32f, 0.28f);
                    curY -= (lineHeight + 4);
                } else {
                    // Wrap long lines
                    if (trimmed.length() > 95) {
                        String p1 = trimmed.substring(0, 92) + "-";
                        String p2 = "  " + trimmed.substring(92);
                        drawText(p1, margin, curY - 9, 8f, false, 0.2f, 0.2f, 0.2f);
                        curY -= lineHeight;
                        if (curY - lineHeight < margin + 35) newPage();
                        drawText(p2, margin, curY - 9, 8f, false, 0.2f, 0.2f, 0.2f);
                        curY -= lineHeight;
                    } else {
                        drawText(trimmed, margin, curY - 9, 8f, false, 0.2f, 0.2f, 0.2f);
                        curY -= lineHeight;
                    }
                }
            }
        }

        private void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float width) {
            String s = String.format(Locale.US, "%.2f w %.2f %.2f %.2f RG %.2f %.2f m %.2f %.2f l S\n",
                    width, r, g, b, x1, y1, x2, y2);
            write(s);
        }

        private void drawRect(float x, float y, float w, float h, float r, float g, float b, boolean fill) {
            String s = String.format(Locale.US, "%.2f %.2f %.2f %s %.2f %.2f %.2f %.2f re %s\n",
                    r, g, b, fill ? "rg" : "RG", x, y, w, h, fill ? "f" : "s");
            write(s);
        }

        private void drawText(String text, float x, float y, float size, boolean bold, float r, float g, float b) {
            String font = bold ? "/F2" : "/F1";
            String safe = escapePdf(text);
            String s = String.format(Locale.US, "BT %s %.2f Tf %.2f %.2f %.2f rg %.2f %.2f Td (%s) Tj ET\n",
                    font, size, r, g, b, x, y, safe);
            write(s);
        }

        private String escapePdf(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        }

        private void write(String s) {
            try {
                currentPage.stream.write(s.getBytes(StandardCharsets.ISO_8859_1));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public byte[] build() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.ISO_8859_1));

            List<Integer> offsets = new ArrayList<>();
            pw.print("%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n");
            pw.flush();

            // Obj 1: Catalog
            offsets.add(out.size());
            pw.print("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
            pw.flush();

            // Obj 2: Pages
            int pageCount = pages.size();
            StringBuilder kids = new StringBuilder("[");
            for (int i = 0; i < pageCount; i++) {
                kids.append(6 + i * 2).append(" 0 R ");
            }
            kids.append("]");

            offsets.add(out.size());
            pw.print("2 0 obj\n<< /Type /Pages /Kids " + kids.toString().trim() + " /Count " + pageCount + " >>\nendobj\n");
            pw.flush();

            // Obj 3: ProcSet
            offsets.add(out.size());
            pw.print("3 0 obj\n[/PDF /Text /ImageB /ImageC /ImageI]\nendobj\n");
            pw.flush();

            // Obj 4: Font F1 (Helvetica)
            offsets.add(out.size());
            pw.print("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n");
            pw.flush();

            // Obj 5: Font F2 (Helvetica-Bold)
            offsets.add(out.size());
            pw.print("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n");
            pw.flush();

            // Pages & Contents
            int nextObj = 6;
            for (int i = 0; i < pageCount; i++) {
                int pageObj = nextObj++;
                int contentObj = nextObj++;

                // Running footer
                String footer = String.format(Locale.US,
                        "BT /F1 7.5 Tf 0.45 0.45 0.45 rg %.2f %.2f Td (Lakbay Lokal Tourism Network • Biliran Province, Philippines  |  Page %d of %d) Tj ET\n",
                        margin, margin - 14, i + 1, pageCount);
                try {
                    pages.get(i).stream.write(footer.getBytes(StandardCharsets.ISO_8859_1));
                } catch (IOException ignored) {}

                byte[] streamBytes = pages.get(i).stream.toByteArray();

                // Page Object
                offsets.add(out.size());
                pw.print(pageObj + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + (int)pageWidth + " " + (int)pageHeight + "] " +
                        "/Resources << /ProcSet 3 0 R /Font << /F1 4 0 R /F2 5 0 R >> >> " +
                        "/Contents " + contentObj + " 0 R >>\nendobj\n");
                pw.flush();

                // Content Stream Object
                offsets.add(out.size());
                pw.print(contentObj + " 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
                pw.flush();
                try {
                    out.write(streamBytes);
                } catch (IOException ignored) {}
                pw.print("\nendstream\nendobj\n");
                pw.flush();
            }

            int xrefOffset = out.size();
            int totalObjs = nextObj;
            pw.print("xref\n0 " + totalObjs + "\n0000000000 65535 f \n");
            for (int off : offsets) {
                pw.printf(Locale.US, "%010d 00000 n \n", off);
            }
            pw.print("trailer\n<< /Size " + totalObjs + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n");
            pw.flush();

            return out.toByteArray();
        }
    }
}
