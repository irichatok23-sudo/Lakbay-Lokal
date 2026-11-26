package lakbaylokal.util;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for exporting reports, analytics, and table datasets to CSV and Text files.
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

    public static boolean exportTableToCSV(JTable table, String baseName) {
        return exportTableToCSV(null, table, baseName, baseName.replace('_', ' '));
    }

    public static boolean exportTableToCSV(Component parent, JTable table, String baseName) {
        return exportTableToCSV(parent, table, baseName, baseName.replace('_', ' '));
    }

    public static boolean exportTableToCSV(Component parent, JTable table, String baseName, String reportTitle) {
        if (table == null || table.getModel() == null) {
            JOptionPane.showMessageDialog(parent, "No data available to export.", "Export Notice", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String defaultFileName = baseName + "_" + timestamp + ".csv";
        File defaultFile = new File(getReportsDirectory(), defaultFileName);

        JFileChooser chooser = new JFileChooser(getReportsDirectory());
        chooser.setSelectedFile(defaultFile);
        chooser.setDialogTitle("Export Data to CSV — Lakbay Lokal");

        int userSelection = chooser.showSaveDialog(parent);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File targetFile = chooser.getSelectedFile();
        if (!targetFile.getName().toLowerCase().endsWith(".csv")) {
            targetFile = new File(targetFile.getParentFile(), targetFile.getName() + ".csv");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            TableModel model = table.getModel();
            int colCount = model.getColumnCount();
            int rowCount = model.getRowCount();

            // Header info
            if (reportTitle != null && !reportTitle.isBlank()) {
                writer.println("# LAKBAY LOKAL — " + escapeCsv(reportTitle));
                writer.println("# Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println();
            }

            // Columns
            for (int col = 0; col < colCount; col++) {
                writer.print(escapeCsv(model.getColumnName(col)));
                if (col < colCount - 1) writer.print(",");
            }
            writer.println();

            // Rows
            for (int row = 0; row < rowCount; row++) {
                for (int col = 0; col < colCount; col++) {
                    Object val = model.getValueAt(row, col);
                    writer.print(escapeCsv(val != null ? val.toString() : ""));
                    if (col < colCount - 1) writer.print(",");
                }
                writer.println();
            }

            int choice = JOptionPane.showOptionDialog(
                    parent,
                    "Data successfully exported to:\n" + targetFile.getAbsolutePath() + "\n\nTotal records exported: " + rowCount,
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
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Failed to export data: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static boolean exportTextReport(String reportContent, String baseName) {
        return exportTextReport(null, reportContent, baseName, baseName.replace('_', ' '));
    }

    public static boolean exportTextReport(Component parent, String reportContent, String baseName) {
        return exportTextReport(parent, reportContent, baseName, baseName.replace('_', ' '));
    }

    public static boolean exportTextReport(Component parent, String reportContent, String baseName, String reportTitle) {
        if (reportContent == null || reportContent.isBlank()) {
            JOptionPane.showMessageDialog(parent, "Report content is empty.", "Export Notice", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String defaultFileName = baseName + "_" + timestamp + ".txt";
        File defaultFile = new File(getReportsDirectory(), defaultFileName);

        JFileChooser chooser = new JFileChooser(getReportsDirectory());
        chooser.setSelectedFile(defaultFile);
        chooser.setDialogTitle("Export Report — Lakbay Lokal");

        int userSelection = chooser.showSaveDialog(parent);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File targetFile = chooser.getSelectedFile();
        if (!targetFile.getName().toLowerCase().endsWith(".txt") && !targetFile.getName().toLowerCase().endsWith(".csv")) {
            targetFile = new File(targetFile.getParentFile(), targetFile.getName() + ".txt");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.print(reportContent);

            int choice = JOptionPane.showOptionDialog(
                    parent,
                    "Report successfully exported to:\n" + targetFile.getAbsolutePath(),
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
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Failed to export report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static String escapeCsv(String val) {
        if (val == null) return "";
        String s = val.trim();
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }
}
