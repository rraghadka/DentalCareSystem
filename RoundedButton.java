package boundary;

import entity.Staff;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import entity.Patient;
import controller.PatientController;

/**
 * DentalCare landing screen – rev-7
 */
public class Main extends JFrame {

    /* ────────── feedback persistence ────────── */
    private static final Path FEEDBACK_FILE =
            Paths.get(System.getProperty("user.home"), "dentalcare_feedback.txt");

    /* ────────── data ────────── */
    private final List<String> feedbacks = new ArrayList<>(20);
    private final JLabel[]     preview   = new JLabel[3];

    private boolean loggedInAsPatient = false;

    /* random tips */
    private static final String[] TIPS = {
            "Replace your toothbrush every 3 months.",
            "Brush for 2 minutes twice a day.",
            "Floss daily to keep gums healthy.",
            "Limit sugary snacks & drinks.",
            "Visit your dentist twice a year."
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    /* ---------- ctor ---------- */
    private Main() {
        loadFeedbacks();

        setTitle("DentalCare");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    /* ================= HEADER ================= */
    private JComponent buildHeader() {
        Color blue = new Color(0, 70, 160);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        /* --- row 1 ----------------------------------------------------- */
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);

        /* center search */
        JPanel centre = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centre.setOpaque(false);
        centre.add(makeSearchPill());
        row1.add(centre, BorderLayout.CENTER);

        /* right: avatar + login popup */
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(new JLabel(icon("/icons/person.png", 28)));

        JButton loginBtn = new RoundedButton("Login ▼", blue, 20);

        JPopupMenu popup = new JPopupMenu();

        JMenuItem patItem   = menuItem("Patient Login", blue);
        JMenuItem staffItem = menuItem("Staff Login",   blue);
        popup.add(patItem);
        popup.add(staffItem);

        /* attach actions */
        patItem.addActionListener(e -> openPatientLoginDialog(blue));
        staffItem.addActionListener(e -> openStaffLoginDialog(blue));

        loginBtn.addActionListener(e -> popup.show(loginBtn, 0, loginBtn.getHeight()));
        right.add(loginBtn);

        row1.add(right, BorderLayout.EAST);
        header.add(row1);

        /* --- row 2 ----------------------------------------------------- */
        JLabel title = new JLabel("Your Smile, Our Priority");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(blue);
        header.add(Box.createVerticalStrut(4));
        header.add(title);

        return header;
    }

    private JMenuItem menuItem(String txt, Color c) {
        JMenuItem mi = new JMenuItem(txt, SwingConstants.CENTER);
        mi.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mi.setForeground(c);
        return mi;
    }

    /* ================= BODY ================= */
    private JComponent buildBody() {
        Color blue = new Color(0, 70, 160);
        JPanel root = new JPanel(new BorderLayout());

        root.add(buildRightColumn(blue), BorderLayout.EAST);

        /* centre: GIF + feedback tab */
        JLayeredPane layer = new JLayeredPane();
        layer.setBackground(blue);
        layer.setOpaque(true);
        root.add(layer, BorderLayout.CENTER);

        JLabel gif = new JLabel(new ImageIcon(
                getClass().getResource("/backgrounds/DentalCaregif.gif")));
        gif.setHorizontalAlignment(SwingConstants.CENTER);
        gif.setVerticalAlignment(SwingConstants.CENTER);
        layer.add(gif, JLayeredPane.DEFAULT_LAYER);

        RotatedTab tab = new RotatedTab("Give Feedback", blue);
        layer.add(tab, JLayeredPane.PALETTE_LAYER);

        JPanel previewPanel = buildPreviewPanel(blue);
        previewPanel.setVisible(false);
        layer.add(previewPanel, JLayeredPane.POPUP_LAYER);

        layer.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = layer.getWidth(), h = layer.getHeight();
                gif.setBounds(0, 0, w, h);
                tab.setBounds(6, h / 2 - 45, 24, 90);
                previewPanel.setLocation(tab.getX() + tab.getWidth() + 8, tab.getY());
            }
        });

        MouseAdapter hover = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { previewPanel.setVisible(true);  }
            public void mouseExited (MouseEvent e) { previewPanel.setVisible(false); }
            public void mouseClicked(MouseEvent  e) { openFeedbackDialog(blue);     }
        };
        tab.addMouseListener(hover);
        previewPanel.addMouseListener(hover);

        return root;
    }

    /* ---------------- right column ---------------- */
    private JPanel buildRightColumn(Color blue) {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(400, 10));
        right.setBackground(new Color(0, 38, 76));
        right.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 30));

        JLabel welcome = new JLabel("Welcome to DentalCare", SwingConstants.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcome.setForeground(Color.WHITE);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcome.setMaximumSize(new Dimension(300, 50));
        right.add(welcome);
        right.add(Box.createVerticalStrut(18));

        JLabel headline = new JLabel("<html><div style='width:300px;'>Your all-in-one clinic:</div></html>");
        headline.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headline.setForeground(Color.WHITE);
        headline.setAlignmentX(Component.CENTER_ALIGNMENT);
        right.add(headline);
        right.add(Box.createVerticalStrut(14));

        JLabel tagline = new JLabel("<html><div style='width:300px;'>Smile confidently – every day!</div></html>");
        tagline.setFont(new Font("Segoe UI", Font.BOLD, 17));
        tagline.setForeground(Color.WHITE);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        right.add(tagline);
        right.add(Box.createVerticalStrut(30));

        JPanel bookCard = buildBookCard(blue);
        bookCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookCard.setMaximumSize(new Dimension(340, 120));
        right.add(bookCard);
        right.add(Box.createVerticalStrut(20));

        JPanel tipCard = buildTipCard(blue);
        tipCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        tipCard.setMaximumSize(new Dimension(340, 70));
        right.add(tipCard);
        right.add(Box.createVerticalStrut(26));

        JLabel bullets = new JLabel("<html><div style='color:white;font-size:15px;width:340px;'>"
                + "• Personalized care<br>"
                + "• Digital records<br>"
                + "• Appointment reminders<br>"
                + "• Inventory & staff management<br>"
                + "• Reporting & analysis</div></html>");
        bullets.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        bullets.setAlignmentX(Component.CENTER_ALIGNMENT);
        right.add(bullets);
        right.add(Box.createVerticalStrut(28));

        JLabel sponsor = new JLabel("<html><b>Sponsored by:</b><br>Crest + Oral-B</html>");
        sponsor.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sponsor.setForeground(Color.WHITE);
        sponsor.setAlignmentX(Component.CENTER_ALIGNMENT);
        right.add(sponsor);

        right.add(Box.createVerticalGlue());
        return right;
    }

    /* ---------------- cards ---------------- */
    private JPanel buildBookCard(Color blue) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(0xFAFAFA));
        p.setBorder(new LineBorder(blue, 1, true));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension cardSize = new Dimension(340, 120);
        p.setPreferredSize(cardSize);
        p.setMaximumSize(cardSize);

        JLabel title = new JLabel("📅 Book an Appointment");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        title.setBorder(new EmptyBorder(10, 16, 0, 16));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title);

        JLabel phone = new JLabel("☎ Secretary  03-5551234");
        phone.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        phone.setBorder(new EmptyBorder(6, 16, 0, 16));
        phone.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(phone);

        JButton online = new JButton("Or book through site");
        online.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        online.setBackground(blue);
        online.setForeground(Color.WHITE);
        online.setFocusPainted(false);
        online.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        online.setBorder(new EmptyBorder(6, 12, 6, 12));
        online.setAlignmentX(Component.LEFT_ALIGNMENT);
        online.setToolTipText("Need to login first!");

        online.addActionListener(e -> {
            if (loggedInAsPatient) {
                JOptionPane.showMessageDialog(this,
                        "Opening Patient Appointment Manager...",
                        "Redirect", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please login first to book online.",
                        "Login Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        p.add(Box.createVerticalStrut(10));
        p.add(online);
        p.add(Box.createVerticalStrut(10));
        return p;
    }

    private JPanel buildTipCard(Color blue) {
        final int CARD_WIDTH = 340, CARD_HEIGHT = 70;

        RoundedPanel p = new RoundedPanel(20);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(0xF4C542));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(6, 12, 6, 12));

        String tip = "🦷 Tip of the day: " + TIPS[(int) (Math.random() * TIPS.length)];
        JLabel lbl = new JLabel("<html><div style='width:300px;'>" + tip + "</div></html>");
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        lbl.setForeground(Color.DARK_GRAY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);

        Dimension d = new Dimension(CARD_WIDTH, CARD_HEIGHT);
        p.setPreferredSize(d);
        p.setMaximumSize(d);

        return p;
    }

    /* ---------------- search pill ---------------- */
    private JPanel makeSearchPill() {
        Color pillBg = new Color(245, 245, 245);
        Color txtCol = new Color(0, 38, 76);

        JTextField tf = new JTextField("Search", 28);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setBorder(null);
        tf.setForeground(txtCol);
        tf.setBackground(pillBg);
        tf.setCaretColor(txtCol);

        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (tf.getText().equals("Search")) tf.setText(""); }
            public void focusLost  (FocusEvent e) { if (tf.getText().isBlank())        tf.setText("Search"); }
        });

        JLabel emoji = new JLabel("\uD83D\uDD0D");  // 🔍
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        emoji.setForeground(txtCol);

        JPanel pill = new RoundedPanel(36);
        pill.setLayout(new BoxLayout(pill, BoxLayout.X_AXIS));
        pill.setBackground(pillBg);
        pill.setBorder(new EmptyBorder(4, 10, 4, 10));
        pill.add(emoji);
        pill.add(Box.createHorizontalStrut(6));
        pill.add(tf);

        return pill;
    }

    /* ---------------- feedback preview ---------------- */
    private JPanel buildPreviewPanel(Color blue) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(255, 255, 255, 235));
        p.setBorder(new LineBorder(blue, 1));
        p.setSize(220, 110);

        for (int i = 0; i < 3; i++) {
            preview[i] = new JLabel(" ");
            p.add(preview[i]);
        }

        JLabel more = new JLabel("<html><u>See more…</u></html>");
        more.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        more.setForeground(blue);
        more.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        more.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { showAllFeedback(); } });

        p.add(Box.createVerticalStrut(4));
        p.add(more);

        refreshPreview();
        return p;
    }
    private void refreshPreview() {
        if (feedbacks.isEmpty()) {
            preview[0].setText("<html><i>No feedback yet – click the tab</i></html>");
            for (int i = 1; i < 3; i++) preview[i].setText(" ");
        } else {
            for (int i = 0; i < 3; i++)
                preview[i].setText(i < feedbacks.size() ? "• " + feedbacks.get(i) : " ");
        }
    }

    /* ---------------- feedback dialog ---------------- */
    private void openFeedbackDialog(Color blue) {
        JDialog dlg = new JDialog(this, "Submit Feedback", true);
        dlg.setUndecorated(true);
        dlg.setSize(360, 180);
        dlg.setLocationRelativeTo(this);

        JPanel wrap = new JPanel();
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(blue, 2), new EmptyBorder(18, 18, 18, 18)));
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel("We’d love your feedback:");
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(blue);

        JTextField txt = new JTextField();
        txt.setMaximumSize(new Dimension(320, 32));

        JPanel btns = new JPanel(); btns.setOpaque(false);
        JButton ok = makeBlueBtn("Submit"), cancel = new JButton("Cancel");
        cancel.setFocusPainted(false);
        btns.add(ok); btns.add(cancel);

        ok.addActionListener(e -> {
            String m = txt.getText().trim();
            if (!m.isEmpty()) {
                if (feedbacks.size() == 20) feedbacks.remove(19);
                feedbacks.add(0, m);
                saveFeedbacks();
                refreshPreview();
            }
            dlg.dispose();
        });
        cancel.addActionListener(e -> dlg.dispose());

        wrap.add(lbl);
        wrap.add(Box.createVerticalStrut(12));
        wrap.add(txt);
        wrap.add(Box.createVerticalStrut(18));
        wrap.add(btns);
        dlg.add(wrap);
        dlg.setVisible(true);
    }

    /* ---------------- login dialogs ---------------- */
    private void openPatientLoginDialog(Color blue) {
        JDialog dlg = new JDialog(this, "Patient Login", true);
        PatientLoginPanel panel = new PatientLoginPanel(id -> {
            loggedInAsPatient = true;
            dlg.dispose();
            onPatientOK(id);          // now launches the dashboard
        });
        dlg.getContentPane().add(panel);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void openStaffLoginDialog(Color blue) {
        JDialog dlg = new JDialog(this, "Staff Login", true);
        StaffLoginPanel panel = new StaffLoginPanel(staff -> {
            loggedInAsPatient = false;
            dlg.dispose();
            onStaffOK(staff);         // now launches the main-menu
        });
        dlg.getContentPane().add(panel);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /* ---------------- persistence ---------------- */
    private void loadFeedbacks() {
        try {
            if (Files.exists(FEEDBACK_FILE))
                feedbacks.addAll(Files.readAllLines(FEEDBACK_FILE));
            if (feedbacks.size() > 20) feedbacks.subList(20, feedbacks.size()).clear();
        } catch (Exception ignored) { }
    }
    private void saveFeedbacks() {
        try { Files.write(FEEDBACK_FILE, feedbacks); } catch (Exception ignored) { }
    }
    private void showAllFeedback() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        feedbacks.forEach(f -> area.append("• " + f + "\n"));
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(350, 220));
        JOptionPane.showMessageDialog(this, sp,
                "All Feedback", JOptionPane.PLAIN_MESSAGE);
    }

    /* ---------- helpers ---------- */
    private JButton makeBlueBtn(String txt) {
        JButton b = new JButton(txt);
        b.setBackground(new Color(0, 70, 160));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        return b;
    }
    private ImageIcon icon(String path, int size) {
        URL u = getClass().getResource(path); if (u == null) return null;
        Image img = new ImageIcon(u).getImage()
                .getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
    /** Called after a successful patient login */
    private void onPatientOK(int patientID) {
        SwingUtilities.invokeLater(() -> {
            // Fetch patient object from controller
            Patient p = PatientController.getInstance().getPatientByID(patientID);
            new FrmPatientMenu(p).setVisible(true);  // ✅ use correct constructor
            dispose();  // close login screen
        });
    }



    /** Called after a successful staff login */
    private void onStaffOK(Staff staff) {
        SwingUtilities.invokeLater(() ->
                new FrmMainMenu(staff).setVisible(true));
        dispose(); // close landing screen
    }

    /* ---------- utility inner classes ---------- */
    static class RoundedPanel extends JPanel {
        private final int arc;
        RoundedPanel(int arc) { this.arc = arc; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
    static class RotatedTab extends JPanel {
        String text; Color bg;
        RotatedTab(String t, Color bg) { this.text = t; this.bg = bg;
            setPreferredSize(new Dimension(24, 90));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getHeight() - fm.stringWidth(text)) / 2;
            int y = (getWidth() + fm.getAscent() - fm.getDescent()) / 2;
            g2.rotate(-Math.PI / 2);
            g2.drawString(text, -getHeight() + x, y);
            g2.dispose();
        }
    }
    static class RoundedButton extends JButton {
        private final int radius;
        private final Color base;
        RoundedButton(String text, Color bg, int radius) {
            super(text);
            this.radius = radius;
            this.base   = bg;

            setBackground(bg);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setMargin(new Insets(10, 20, 10, 20));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(base.brighter()); }
                public void mouseExited (MouseEvent e) { setBackground(base); }
            });
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}


