package lakbaylokal.shared;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern Tourism-Inspired UI Theme and Component Factory for Lakbay Lokal.
 * Palette: Forest Green, Emerald, Teal, Slate Neutral, and Warm Amber.
 */
public class UITheme {

    // Primary Colors — Forest Green & Emerald
    public static final Color PRIMARY_DARK = new Color(6, 78, 59);       // #064e3b Deep Forest
    public static final Color PRIMARY = new Color(4, 120, 87);            // #047857 Emerald Green
    public static final Color PRIMARY_HOVER = new Color(5, 150, 105);     // #059669
    public static final Color PRIMARY_LIGHT = new Color(209, 250, 229);   // #d1fae5 Emerald Tint

    // Teal Accent
    public static final Color TEAL = new Color(13, 148, 136);             // #0d9488 Vibrant Teal
    public static final Color TEAL_DARK = new Color(15, 118, 110);        // #0f766e
    public static final Color TEAL_LIGHT = new Color(204, 251, 241);      // #ccfbf1

    // Amber / Warning
    public static final Color ACCENT_AMBER = new Color(217, 119, 6);      // #d97706 Warm Amber
    public static final Color ACCENT_AMBER_LIGHT = new Color(254, 243, 199);// #fef3c7

    // Neutral & Backgrounds
    public static final Color BG_LIGHT = new Color(248, 250, 252);        // #f8fafc Clean Slate 50
    public static final Color BG_ALT = new Color(241, 245, 249);          // #f1f5f9 Slate 100
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(15, 23, 42);          // #0f172a Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);      // #64748b Slate 500
    public static final Color BORDER_COLOR = new Color(226, 232, 240);    // #e2e8f0 Slate 200
    public static final Color BORDER_FOCUS = new Color(16, 185, 129);     // #10b981

    // Functional Status Colors
    public static final Color SUCCESS = new Color(16, 185, 129);          // #10b981
    public static final Color SUCCESS_LIGHT = new Color(220, 252, 231);    // #dcfce7
    public static final Color DANGER = new Color(239, 68, 68);            // #ef4444 Rose Red
    public static final Color DANGER_LIGHT = new Color(254, 226, 226);     // #fee2e2
    public static final Color INFO_BLUE = new Color(37, 99, 235);         // #2563eb
    public static final Color INFO_BLUE_LIGHT = new Color(219, 234, 254);  // #dbeafe

    // Compatibility aliases for legacy panels
    public static final Color COLOR_PRIMARY = PRIMARY;
    public static final Color COLOR_PRIMARY_DARK = PRIMARY_DARK;
    public static final Color COLOR_PRIMARY_LIGHT = PRIMARY_LIGHT;
    public static final Color COLOR_BG = BG_LIGHT;
    public static final Color COLOR_CARD_BG = CARD_BG;
    public static final Color COLOR_TEXT_PRIMARY = TEXT_DARK;
    public static final Color COLOR_TEXT_SECONDARY = TEXT_MUTED;
    public static final Color COLOR_BORDER = BORDER_COLOR;
    public static final Color COLOR_SUCCESS = SUCCESS;
    public static final Color COLOR_DANGER = DANGER;
    public static final Color COLOR_WARNING = ACCENT_AMBER;
    public static final Color COLOR_ACCENT_GREEN = SUCCESS;
    public static final Color COLOR_ACCENT_ORANGE = ACCENT_AMBER;
    public static final Color COLOR_TEXT_MUTED = TEXT_MUTED;

    // Typography
    public static final Font FONT_BRAND = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);

    /**
     * Initializes global Swing Look & Feel with modern anti-aliasing hints.
     */
    public static void initializeLookAndFeel() {
        try {
            // Check if FlatLaf is on classpath reflectively
            Class<?> flatLaf = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            UIManager.setLookAndFeel((LookAndFeel) flatLaf.getDeclaredConstructor().newInstance());
        } catch (Throwable ignored) {
            // Fallback to Nimbus or System
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored2) {}
            }
        }

        // Enable subpixel text antialiasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }

    // ------------------------------------------------------------------------
    // BUTTON FACTORIES
    // ------------------------------------------------------------------------

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY, PRIMARY_HOVER, Color.WHITE, FONT_BOLD, 8, 18);
    }

    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, Color.WHITE, BG_ALT, TEXT_DARK, FONT_BODY, 8, 16, BORDER_COLOR);
    }

    public static JButton createNeutralButton(String text) {
        return createSecondaryButton(text);
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS, new Color(5, 150, 105), Color.WHITE, FONT_BOLD, 8, 16);
    }

    public static JButton createWarningButton(String text) {
        return createStyledButton(text, ACCENT_AMBER, new Color(180, 83, 9), Color.WHITE, FONT_BOLD, 8, 16);
    }

    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER, new Color(220, 38, 38), Color.WHITE, FONT_BOLD, 8, 16);
    }

    private static JButton createStyledButton(String text, Color bg, Color hoverBg, Color fg, Font font, int padV, int padH) {
        return createStyledButton(text, bg, hoverBg, fg, font, padV, padH, null);
    }

    private static JButton createStyledButton(String text, Color bg, Color hoverBg, Color fg, Font font, int padV, int padH, Color borderCol) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (borderCol != null) {
                    g2.setColor(borderCol);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(font);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(padV, padH, padV, padH));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
                btn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
                btn.repaint();
            }
        });

        return btn;
    }

    // ------------------------------------------------------------------------
    // CARD & CONTAINER FACTORIES
    // ------------------------------------------------------------------------

    public static JPanel createCardPanel() {
        return createCardPanel(null);
    }

    public static JPanel createCardPanel(String title) {
        JPanel card = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        if (title != null && !title.isBlank()) {
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(FONT_HEADER);
            lblTitle.setForeground(TEXT_DARK);
            card.add(lblTitle, BorderLayout.NORTH);
        }
        return card;
    }

    public static JPanel createStatCard(String title, String value, String subtext, Color accent) {
        Color borderAccent = (accent != null) ? accent : PRIMARY;

        JPanel card = new JPanel(new BorderLayout(6, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                // Subtle border
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                // Top accent strip
                g2.setColor(borderAccent);
                g2.fillRoundRect(2, 2, getWidth() - 5, 4, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_MUTED);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblVal.setForeground(borderAccent);

        JLabel lblSub = new JLabel(subtext);
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(TEXT_MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    public static JPanel createStatCard(String title, JLabel valueLabel, String note) {
        JPanel card = createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(FONT_SMALL);
        t.setForeground(TEXT_MUTED);

        JLabel n = new JLabel(note);
        n.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        n.setForeground(TEXT_MUTED);

        card.add(t);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(n);
        return card;
    }

    // ------------------------------------------------------------------------
    // TABLE STYLING & STATUS BADGES
    // ------------------------------------------------------------------------

    public static void styleTable(JTable table) {
        if (table == null) return;

        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        // Header
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setFont(FONT_BOLD);
            header.setBackground(new Color(241, 245, 249));
            header.setForeground(TEXT_DARK);
            header.setPreferredSize(new Dimension(0, 40));
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
            header.setReorderingAllowed(false);
        }

        // Default cell renderer with zebra striping and status badge support
        ModernTableCellRenderer renderer = new ModernTableCellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    public static JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        styleTable(table);
        return table;
    }

    /**
     * Modern table cell renderer with alternating row colors and pill badge formatting.
     */
    public static class ModernTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setBorder(new EmptyBorder(0, 12, 0, 12));
            String strVal = (value != null) ? value.toString().trim() : "";

            // Check if column or value represents a recognized status
            if (isStatusValue(strVal)) {
                return createStatusPill(strVal);
            }

            // Align numbers/currency to right, ID to center, text to left
            if (strVal.startsWith("₱") || strVal.matches("^\\d+(\\.\\d+)?%$")) {
                setHorizontalAlignment(JLabel.RIGHT);
            } else if (column == 0 || strVal.endsWith("★") || strVal.matches("^\\d+$")) {
                setHorizontalAlignment(JLabel.CENTER);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
            }

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                c.setForeground(TEXT_DARK);
            } else {
                c.setBackground(PRIMARY_LIGHT);
                c.setForeground(PRIMARY_DARK);
            }

            return c;
        }

        private boolean isStatusValue(String s) {
            if (s == null || s.isEmpty()) return false;
            String upper = s.toUpperCase();
            return upper.equals("ACTIVE") || upper.equals("INACTIVE") ||
                   upper.equals("CONFIRMED") || upper.equals("COMPLETED") ||
                   upper.equals("PENDING") || upper.equals("CANCELLED") ||
                   upper.equals("REJECTED") || upper.equals("APPROVED") ||
                   upper.equals("VERIFIED") || upper.equals("OPEN") ||
                   upper.equals("CLOSED") || upper.equals("UNPAID") ||
                   upper.equals("PAID") || upper.equals("PUBLISHED") ||
                   upper.equals("ARCHIVED");
        }

        private JPanel createStatusPill(String status) {
            String upper = status.toUpperCase();
            Color bgTint;
            Color textCol;

            switch (upper) {
                case "ACTIVE", "CONFIRMED", "APPROVED", "VERIFIED", "PAID", "OPEN", "PUBLISHED" -> {
                    bgTint = SUCCESS_LIGHT;
                    textCol = new Color(5, 150, 105);
                }
                case "PENDING", "UNPAID" -> {
                    bgTint = ACCENT_AMBER_LIGHT;
                    textCol = new Color(180, 83, 9);
                }
                case "COMPLETED" -> {
                    bgTint = TEAL_LIGHT;
                    textCol = TEAL_DARK;
                }
                case "CANCELLED", "REJECTED", "CLOSED", "INACTIVE", "ARCHIVED" -> {
                    bgTint = DANGER_LIGHT;
                    textCol = new Color(185, 28, 28);
                }
                default -> {
                    bgTint = BG_ALT;
                    textCol = TEXT_MUTED;
                }
            }

            JPanel pillPanel = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bgTint);
                    int padX = 8;
                    int padY = 4;
                    g2.fillRoundRect(padX, padY, getWidth() - (padX * 2), getHeight() - (padY * 2), 16, 16);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pillPanel.setOpaque(false);

            JLabel lbl = new JLabel(upper);
            lbl.setFont(FONT_BADGE);
            lbl.setForeground(textCol);
            pillPanel.add(lbl);

            return pillPanel;
        }
    }

    // ------------------------------------------------------------------------
    // HEADER BANNER FACTORY
    // ------------------------------------------------------------------------

    public static JPanel createHeaderBanner(String roleTitle, String userName, Runnable onLogout) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_DARK);
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        // Brand + Role
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel logo = new JLabel("🌴 LAKBAY LOKAL");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);

        JLabel roleBadge = new JLabel("  " + roleTitle.toUpperCase() + "  ");
        roleBadge.setFont(FONT_BADGE);
        roleBadge.setBackground(PRIMARY);
        roleBadge.setForeground(Color.WHITE);
        roleBadge.setOpaque(true);
        roleBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL, 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        left.add(logo);
        left.add(roleBadge);

        // User + Logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        JLabel lblUser = new JLabel("👤 " + (userName != null ? userName : "Logged In"));
        lblUser.setFont(FONT_BOLD);
        lblUser.setForeground(new Color(226, 232, 240));

        JButton btnLogout = createDangerButton("Log Out");
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    header,
                    "Are you sure you want to log out of Lakbay Lokal?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION && onLogout != null) {
                onLogout.run();
            }
        });

        right.add(lblUser);
        right.add(btnLogout);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ------------------------------------------------------------------------
    // SEARCH & FILTER BAR FACTORY
    // ------------------------------------------------------------------------

    public static JPanel createFilterBar(JTextField searchField, String placeholder, JComboBox<?> categoryCombo, Runnable onSearch, Runnable onReset) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        bar.setOpaque(false);

        if (searchField != null) {
            searchField.setPreferredSize(new Dimension(220, 34));
            searchField.setFont(FONT_BODY);
            searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                    new EmptyBorder(4, 10, 4, 10)
            ));
            bar.add(new JLabel("🔍 Search:"));
            bar.add(searchField);
        }

        if (categoryCombo != null) {
            categoryCombo.setFont(FONT_BODY);
            categoryCombo.setPreferredSize(new Dimension(170, 34));
            bar.add(new JLabel("Filter:"));
            bar.add(categoryCombo);
        }

        if (onSearch != null) {
            JButton btnSearch = createPrimaryButton("Apply Filter");
            btnSearch.addActionListener(e -> onSearch.run());
            bar.add(btnSearch);
        }

        if (onReset != null) {
            JButton btnReset = createSecondaryButton("Reset / Refresh");
            btnReset.addActionListener(e -> onReset.run());
            bar.add(btnReset);
        }

        return bar;
    }
}
