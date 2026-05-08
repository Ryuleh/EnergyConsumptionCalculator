import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

/**
 * Full-featured energy dashboard GUI.
 * Dark industrial aesthetic — every panel updates live when appliances change.
 */
public class GUI extends JFrame {

    // ── Palette ───────────────────────────────────────────────────────────
    private static final Color BG          = new Color(13, 17, 23);
    private static final Color PANEL_BG    = new Color(22, 27, 34);
    private static final Color BORDER_CLR  = new Color(48, 54, 61);
    private static final Color ACCENT      = new Color(0, 210, 150);
    private static final Color ACCENT2     = new Color(255, 160, 50);
    private static final Color ACCENT3     = new Color(100, 160, 255);
    private static final Color TEXT_PRI    = new Color(230, 237, 243);
    private static final Color TEXT_SEC    = new Color(139, 148, 158);
    private static final Color DANGER      = new Color(220, 80, 80);

    private static final Color[] CAT_COLORS = {
        new Color(0, 210, 150), new Color(100, 160, 255),
        new Color(255, 160, 50), new Color(220, 80, 80),
        new Color(180, 100, 255), new Color(80, 200, 220)
    };

    private static final Font FONT_MONO  = new Font("Monospaced", Font.PLAIN, 13);
    private static final Font FONT_BOLD  = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_STAT  = new Font("Monospaced", Font.BOLD, 22);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.PLAIN, 12);

    // ── State ─────────────────────────────────────────────────────────────
    private User user;

    // ── UI refs ───────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JLabel lblDaily, lblMonthly, lblAnnual;
    private BarChartPanel chartPanel;

    // Preset appliances
    private static final String[][] PRESETS = {
        {"Refrigerator",       "150",  "24",  "Kitchen"},
        {"Microwave",          "1200", "0.5", "Kitchen"},
        {"Dishwasher",         "1800", "1",   "Kitchen"},
        {"Air Conditioner",    "3500", "8",   "HVAC"},
        {"Heater",             "2000", "6",   "HVAC"},
        {"Ceiling Fan",        "75",   "8",   "HVAC"},
        {"LED TV (55\")",      "80",   "5",   "Entertainment"},
        {"Gaming Console",     "200",  "3",   "Entertainment"},
        {"Desktop PC",         "300",  "6",   "Entertainment"},
        {"Laptop",             "50",   "8",   "Entertainment"},
        {"Washing Machine",    "500",  "1",   "Laundry"},
        {"Clothes Dryer",      "5000", "1",   "Laundry"},
        {"LED Lighting (home)","100",  "6",   "Lighting"},
        {"Phone Charger",      "5",    "3",   "Devices"},
        {"Electric Oven",      "2400", "1",   "Kitchen"},
        {"Water Heater",       "4000", "2",   "HVAC"},
    };

    // ── Constructor ───────────────────────────────────────────────────────
    public GUI(User user) {
        this.user = user;
        setTitle("⚡ Energy Consumption Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);

        refresh();
    }

    // ── Header ────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR));
        p.setPreferredSize(new Dimension(0, 56));

        JLabel title = new JLabel("  ⚡  ENERGY DASHBOARD");
        title.setFont(new Font("Monospaced", Font.BOLD, 16));
        title.setForeground(ACCENT);
        p.add(title, BorderLayout.WEST);

        JLabel info = new JLabel(
            user.getName() + "  ·  " + user.getLocation() +
            "  ·  $" + String.format("%.4f", user.getElectricityRate()) + "/kWh   "
        );
        info.setFont(FONT_LABEL);
        info.setForeground(TEXT_SEC);
        p.add(info, BorderLayout.EAST);

        return p;
    }

    // ── Center (main content) ─────────────────────────────────────────────
    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildLeftPanel(), buildRightPanel());
        split.setDividerLocation(560);
        split.setDividerSize(4);
        split.setBackground(BG);
        split.setBorder(null);
        return split;
    }

    // ── LEFT: stat cards + chart ──────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel p = darkPanel(new BorderLayout(0, 12));
        p.setBorder(new EmptyBorder(16, 16, 16, 8));

        // Stat cards row
        JPanel cards = new JPanel(new GridLayout(1, 3, 10, 0));
        cards.setOpaque(false);
        lblDaily   = statCard(cards, "DAILY",   ACCENT);
        lblMonthly = statCard(cards, "MONTHLY", ACCENT2);
        lblAnnual  = statCard(cards, "ANNUAL",  ACCENT3);
        p.add(cards, BorderLayout.NORTH);

        // Chart
        chartPanel = new BarChartPanel();
        JPanel chartWrap = darkPanel(new BorderLayout());
        chartWrap.setBorder(titledBorder("CONSUMPTION BY CATEGORY (kWh/mo)"));
        chartWrap.add(chartPanel, BorderLayout.CENTER);
        p.add(chartWrap, BorderLayout.CENTER);

        return p;
    }

    /** Creates a stat card and returns the value label. */
    private JLabel statCard(JPanel parent, String title, Color accent) {
        JPanel card = darkPanel(new BorderLayout(0, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent.darker(), 1),
            new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel ttl = new JLabel(title);
        ttl.setFont(FONT_SMALL);
        ttl.setForeground(TEXT_SEC);
        card.add(ttl, BorderLayout.NORTH);

        JLabel val = new JLabel("$0.00");
        val.setFont(FONT_STAT);
        val.setForeground(accent);
        card.add(val, BorderLayout.CENTER);

        JLabel sub = new JLabel("0.00 kWh");
        sub.setFont(FONT_SMALL);
        sub.setForeground(TEXT_SEC);
        card.add(sub, BorderLayout.SOUTH);

        card.putClientProperty("sub", sub);
        card.putClientProperty("accent", accent);
        parent.add(card);
        val.putClientProperty("card", card);
        return val;
    }

    // ── RIGHT: appliance table + controls ─────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel p = darkPanel(new BorderLayout(0, 10));
        p.setBorder(new EmptyBorder(16, 8, 16, 16));

        // Table
        String[] cols = {"Appliance", "Category", "Watts", "Hrs/Day", "kWh/Mo", "Cost/Mo"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(PANEL_BG);
        scroll.getViewport().setBackground(PANEL_BG);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR));

        JPanel tableWrap = darkPanel(new BorderLayout());
        tableWrap.setBorder(titledBorder("APPLIANCE INVENTORY"));
        tableWrap.add(scroll, BorderLayout.CENTER);
        p.add(tableWrap, BorderLayout.CENTER);

        // Controls
        p.add(buildControls(table), BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildControls(JTable table) {
        JPanel wrap = darkPanel(new GridLayout(2, 1, 0, 8));

        // Row 1: preset selector + add preset
        JPanel row1 = darkPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JComboBox<String> presetBox = styledCombo();
        for (String[] p : PRESETS) presetBox.addItem(p[0] + " (" + p[1] + "W)");
        row1.add(label("Preset:"));
        row1.add(presetBox);
        JButton addPreset = accentButton("Add Preset", ACCENT);
        addPreset.addActionListener(e -> {
            int idx = presetBox.getSelectedIndex();
            String[] pre = PRESETS[idx];
            user.addAppliance(new Appliance(pre[0],
                Double.parseDouble(pre[1]),
                Double.parseDouble(pre[2]),
                pre[3]));
            refresh();
        });
        row1.add(addPreset);
        wrap.add(row1);

        // Row 2: custom add + remove
        JPanel row2 = darkPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JTextField fName  = styledField("Name",      100);
        JTextField fWatts = styledField("Watts",      60);
        JTextField fHours = styledField("Hrs/Day",    60);
        JComboBox<String> catBox = styledCombo();
        for (String c : new String[]{"Kitchen","HVAC","Entertainment","Laundry","Lighting","Devices","Other"})
            catBox.addItem(c);

        JButton addCustom = accentButton("+ Add", ACCENT);
        addCustom.addActionListener(e -> {
            try {
                String n  = fName.getText().trim();
                double w  = Double.parseDouble(fWatts.getText().trim());
                double h  = Double.parseDouble(fHours.getText().trim());
                String cat = (String) catBox.getSelectedItem();
                if (n.isEmpty()) throw new IllegalArgumentException("Name required");
                user.addAppliance(new Appliance(n, w, h, cat));
                fName.setText(""); fWatts.setText(""); fHours.setText("");
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Check inputs: " + ex.getMessage(),
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton remove = accentButton("Remove Selected", DANGER);
        remove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                user.removeAppliance(row);
                refresh();
            }
        });

        row2.add(label("Name:")); row2.add(fName);
        row2.add(label("W:"));    row2.add(fWatts);
        row2.add(label("h/d:"));  row2.add(fHours);
        row2.add(label("Cat:"));  row2.add(catBox);
        row2.add(addCustom);
        row2.add(remove);
        wrap.add(row2);

        return wrap;
    }

    // ── Footer ────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CLR));

        JButton exportBtn = accentButton("Export Report", ACCENT3);
        exportBtn.addActionListener(e -> exportReport());
        p.add(exportBtn);

        JLabel tip = new JLabel("Tip: Select a row to remove it. All stats update live.");
        tip.setFont(FONT_SMALL);
        tip.setForeground(TEXT_SEC);
        p.add(tip);

        return p;
    }

    // ── Refresh ───────────────────────────────────────────────────────────
    private void refresh() {
        // Update table
        tableModel.setRowCount(0);
        for (Appliance a : user.getAppliances()) {
            tableModel.addRow(new Object[]{
                a.getName(),
                a.getCategory(),
                String.format("%.0f", a.getPowerConsumption()),
                String.format("%.1f", a.getHoursPerDay()),
                String.format("%.2f", a.getMonthlyKwh()),
                String.format("$%.2f", a.getMonthlyKwh() * user.getElectricityRate())
            });
        }

        // Update stat cards
        updateStatCard(lblDaily,
            user.getDailyCost(), user.getTotalDailyKwh(), "daily");
        updateStatCard(lblMonthly,
            user.getMonthlyCost(), user.getTotalMonthlyKwh(), "monthly");
        updateStatCard(lblAnnual,
            user.getAnnualCost(), user.getTotalAnnualKwh(), "annual");

        // Update chart
        chartPanel.setData(user.getMonthlyKwhByCategory());
        chartPanel.repaint();
    }

    private void updateStatCard(JLabel val, double cost, double kwh, String period) {
        val.setText(String.format("$%.2f", cost));
        JPanel card = (JPanel) val.getClientProperty("card");
        if (card != null) {
            JLabel sub = (JLabel) card.getClientProperty("sub");
            if (sub != null) sub.setText(String.format("%.2f kWh/%s", kwh, period));
        }
    }

    // ── Export ────────────────────────────────────────────────────────────
    private void exportReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("ENERGY CONSUMPTION REPORT\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append("User: ").append(user.getName()).append("\n");
        sb.append("Location: ").append(user.getLocation()).append("\n");
        sb.append("Rate: $").append(String.format("%.4f", user.getElectricityRate())).append("/kWh\n\n");
        sb.append(String.format("%-25s %-14s %8s %8s %10s %10s%n",
            "Appliance", "Category", "Watts", "Hrs/Day", "kWh/Mo", "$/Mo"));
        sb.append("-".repeat(78)).append("\n");
        for (Appliance a : user.getAppliances()) {
            sb.append(String.format("%-25s %-14s %8.0f %8.1f %10.2f %10.2f%n",
                a.getName(), a.getCategory(), a.getPowerConsumption(),
                a.getHoursPerDay(), a.getMonthlyKwh(),
                a.getMonthlyKwh() * user.getElectricityRate()));
        }
        sb.append("-".repeat(78)).append("\n");
        sb.append(String.format("DAILY:   %.2f kWh   $%.2f%n",
            user.getTotalDailyKwh(), user.getDailyCost()));
        sb.append(String.format("MONTHLY: %.2f kWh   $%.2f%n",
            user.getTotalMonthlyKwh(), user.getMonthlyCost()));
        sb.append(String.format("ANNUAL:  %.2f kWh   $%.2f%n",
            user.getTotalAnnualKwh(), user.getAnnualCost()));

        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(FONT_MONO);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Export Report",
            JOptionPane.PLAIN_MESSAGE);
    }

    // ── Bar Chart Panel ────────────────────────────────────────────────────
    private static class BarChartPanel extends JPanel {
        private Map<String, Double> data = new LinkedHashMap<>();

        BarChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 200));
        }

        void setData(Map<String, Double> data) { this.data = data; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int padL = 50, padR = 20, padT = 20, padB = 50;
            int chartW = w - padL - padR;
            int chartH = h - padT - padB;

            if (data.isEmpty()) {
                g2.setColor(TEXT_SEC);
                g2.setFont(FONT_LABEL);
                g2.drawString("Add appliances to see chart", padL + chartW / 2 - 90, padT + chartH / 2);
                return;
            }

            double max = data.values().stream().mapToDouble(d -> d).max().orElse(1);

            // Grid lines
            g2.setColor(BORDER_CLR);
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10, new float[]{4}, 0));
            int gridLines = 4;
            for (int i = 0; i <= gridLines; i++) {
                int y = padT + (int) (chartH * i / gridLines);
                g2.drawLine(padL, y, padL + chartW, y);
                double val = max * (gridLines - i) / gridLines;
                g2.setColor(TEXT_SEC);
                g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
                g2.drawString(String.format("%.0f", val), 2, y + 4);
                g2.setColor(BORDER_CLR);
            }
            g2.setStroke(new BasicStroke(1));

            // Bars
            String[] keys = data.keySet().toArray(new String[0]);
            int n = keys.length;
            int barW = Math.min(60, (chartW - 10 * (n + 1)) / Math.max(n, 1));
            int gap  = (chartW - barW * n) / (n + 1);

            for (int i = 0; i < n; i++) {
                double val = data.get(keys[i]);
                int barH = (int) (chartH * val / max);
                int x = padL + gap + i * (barW + gap);
                int y = padT + chartH - barH;

                Color c = CAT_COLORS[i % CAT_COLORS.length];
                GradientPaint gp = new GradientPaint(x, y, c, x, padT + chartH, c.darker().darker());
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(x, y, barW, barH, 6, 6));

                // Value on top
                g2.setColor(TEXT_PRI);
                g2.setFont(new Font("Monospaced", Font.BOLD, 10));
                String vStr = String.format("%.1f", val);
                int tx = x + barW / 2 - g2.getFontMetrics().stringWidth(vStr) / 2;
                if (barH > 18) g2.drawString(vStr, tx, y + 14);

                // Label below
                g2.setColor(TEXT_SEC);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                String label = keys[i].length() > 8 ? keys[i].substring(0, 7) + "…" : keys[i];
                int lx = x + barW / 2 - g2.getFontMetrics().stringWidth(label) / 2;
                g2.drawString(label, lx, padT + chartH + 14);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private JPanel darkPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(PANEL_BG);
        p.setOpaque(true);
        return p;
    }

    private Border titledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_CLR), " " + title + " ");
        tb.setTitleFont(FONT_SMALL);
        tb.setTitleColor(TEXT_SEC);
        return BorderFactory.createCompoundBorder(tb, new EmptyBorder(6, 6, 6, 6));
    }

    private JButton accentButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD);
        b.setForeground(color);
        b.setBackground(PANEL_BG);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1),
            new EmptyBorder(5, 12, 5, 12)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(color.darker().darker()); }
            public void mouseExited(MouseEvent e)  { b.setBackground(PANEL_BG); }
        });
        return b;
    }

    private JTextField styledField(String placeholder, int width) {
        JTextField f = new JTextField(placeholder);
        f.setPreferredSize(new Dimension(width, 28));
        f.setFont(FONT_LABEL);
        f.setForeground(TEXT_SEC);
        f.setBackground(BG);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR),
            new EmptyBorder(2, 6, 2, 6)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TEXT_PRI); }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(TEXT_SEC); }
            }
        });
        return f;
    }

    private JComboBox<String> styledCombo() {
        JComboBox<String> box = new JComboBox<>();
        box.setFont(FONT_LABEL);
        box.setBackground(BG);
        box.setForeground(TEXT_PRI);
        box.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        return box;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_SEC);
        return l;
    }

    private void styleTable(JTable table) {
        table.setBackground(PANEL_BG);
        table.setForeground(TEXT_PRI);
        table.setFont(FONT_LABEL);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(ACCENT.darker().darker());
        table.setSelectionForeground(TEXT_PRI);
        table.setGridColor(BORDER_CLR);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG);
        header.setForeground(TEXT_SEC);
        header.setFont(FONT_SMALL);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR));

        // Alternate row coloring
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? ACCENT.darker().darker()
                                  : row % 2 == 0 ? PANEL_BG : BG);
                setForeground(col == 5 ? ACCENT : TEXT_PRI);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }
}