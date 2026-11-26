import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TestPdf {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SimplePdf pdf = new SimplePdf("LAKBAY LOKAL — Biliran Community Tourism Report", "Province of Biliran • Region VIII");
        
        List<String> headers = Arrays.asList("Destination", "Category", "Municipality", "Barangay", "Total Visitors", "Revenue (PHP)");
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Sambawan Island", "ISLAND_BEACH", "Maripipi", "Ol-og", "1,250", "PHP 125,000.00"));
        rows.add(Arrays.asList("Tinago Falls", "WATERFALL", "Caibiran", "Cabibihan", "2,100", "PHP 105,000.00"));
        rows.add(Arrays.asList("Mainit Hot Spring", "HOT_SPRING", "Caibiran", "Villa Vicenta", "890", "PHP 44,500.00"));
        
        pdf.addTable(headers, rows);
        byte[] pdfBytes = pdf.build();
        
        File f = new File("scratch/sample_report.pdf");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(pdfBytes);
        }
        System.out.println("PDF generated successfully: " + f.getAbsolutePath() + " (" + pdfBytes.length + " bytes)");
    }

    static class SimplePdf {
        private final String title;
        private final String subtitle;
        private final List<Page> pages = new ArrayList<>();
        private Page currentPage;
        private final float pageWidth = 612; // Standard Letter
        private final float pageHeight = 792;
        private final float margin = 40;
        private float curY;

        static class Page {
            ByteArrayOutputStream content = new ByteArrayOutputStream();
        }

        public SimplePdf(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
            newPage();
            drawHeader();
        }

        private void newPage() {
            currentPage = new Page();
            pages.add(currentPage);
            curY = pageHeight - margin;
            if (pages.size() > 1) {
                drawHeader();
            }
        }

        private void drawHeader() {
            // Draw teal banner
            drawRect(margin, curY - 32, pageWidth - 2 * margin, 32, 0.05f, 0.46f, 0.43f, true);
            drawText(title, margin + 10, curY - 20, 12, true, 1f, 1f, 1f);
            curY -= 40;
            
            drawText("Generated: " + new Date().toString() + "  |  " + subtitle, margin, curY, 8, false, 0.4f, 0.4f, 0.4f);
            drawLine(margin, curY - 6, pageWidth - margin, curY - 6, 0.8f, 0.8f, 0.8f, 1f);
            curY -= 20;
        }

        private void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float width) {
            String s = String.format(Locale.US, "%.2f w %.2f %.2f %.2f RG %.2f %.2f m %.2f %.2f l S\n", width, r, g, b, x1, y1, x2, y2);
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
                currentPage.content.write(s.getBytes(StandardCharsets.ISO_8859_1));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void addTable(List<String> headers, List<List<String>> rows) {
            float availableWidth = pageWidth - 2 * margin;
            int cols = headers.size();
            float colWidth = availableWidth / cols;
            float rowHeight = 22;

            // Draw table header row
            if (curY - rowHeight < margin + 30) newPage();
            drawRect(margin, curY - rowHeight, availableWidth, rowHeight, 0.12f, 0.23f, 0.35f, true);
            for (int i = 0; i < cols; i++) {
                drawText(headers.get(i), margin + (i * colWidth) + 4, curY - 15, 9, true, 1f, 1f, 1f);
            }
            curY -= rowHeight;

            // Draw rows
            boolean alt = false;
            for (List<String> row : rows) {
                if (curY - rowHeight < margin + 30) {
                    newPage();
                    // redraw header on new page
                    drawRect(margin, curY - rowHeight, availableWidth, rowHeight, 0.12f, 0.23f, 0.35f, true);
                    for (int i = 0; i < cols; i++) {
                        drawText(headers.get(i), margin + (i * colWidth) + 4, curY - 15, 9, true, 1f, 1f, 1f);
                    }
                    curY -= rowHeight;
                }

                if (alt) {
                    drawRect(margin, curY - rowHeight, availableWidth, rowHeight, 0.95f, 0.96f, 0.98f, true);
                }
                drawLine(margin, curY - rowHeight, pageWidth - margin, curY - rowHeight, 0.88f, 0.88f, 0.88f, 0.5f);

                for (int i = 0; i < Math.min(cols, row.size()); i++) {
                    String val = row.get(i) != null ? row.get(i) : "";
                    if (val.length() > 25) val = val.substring(0, 22) + "...";
                    drawText(val, margin + (i * colWidth) + 4, curY - 15, 8.5f, false, 0.15f, 0.15f, 0.15f);
                }
                curY -= rowHeight;
                alt = !alt;
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
            List<Integer> pageContentObjs = new ArrayList<>();
            for (int i = 0; i < pageCount; i++) {
                int pageObj = nextObj++;
                int contentObj = nextObj++;

                // Add footer to page before writing
                String footer = String.format(Locale.US, "BT /F1 8 Tf 0.5 0.5 0.5 rg %.2f %.2f Td (Lakbay Lokal Tourism Network • Biliran Province  |  Page %d of %d) Tj ET\n",
                        margin, margin - 15, i + 1, pageCount);
                try {
                    pages.get(i).content.write(footer.getBytes(StandardCharsets.ISO_8859_1));
                } catch (IOException ignored) {}

                byte[] streamBytes = pages.get(i).content.toByteArray();

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
