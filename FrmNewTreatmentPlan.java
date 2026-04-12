package boundary;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;

import controller.AppointmentController;import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import java.util.HashMap;
import java.sql.Connection;

import javax.swing.JOptionPane;

import controller.PatientController;
import entity.Appointment;
import entity.Consts;
import entity.Patient;
import entity.Staff;
import controller.InventoryItemController;
import controller.TreatmentItemUsageController;
import entity.InventoryItem;
import boundary.FrmPendingAppointments;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import java.io.InputStream;
import java.sql.Connection;


/** DentalCare – Main menu with sidebar + rich welcome panel */
public class FrmMainMenu extends JFrame {

    private final Staff  currentUser;
    private final JPanel rightPanel = new JPanel(new BorderLayout());

    /* ─── stretch-GIF helper ─── */
    private static class StretchGifPanel extends JPanel {
        private final ImageIcon gif;
        StretchGifPanel(String path) {
            gif = new ImageIcon(FrmMainMenu.class.getResource(path));
            setLayout(new BorderLayout());
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(gif.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    }

    /* ─── ctor ─── */
    public FrmMainMenu(Staff user) {
        this.currentUser = user;

        setTitle("DentalCare – Main Menu");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(260);
        split.setDividerSize(2);
        add(split);

        /* ========== LEFT SIDEBAR ========== */
        StretchGifPanel gif      = new StretchGifPanel("/icons/f.gif");
        JPanel          overlay  = new JPanel();
        overlay.setOpaque(false);
        overlay.setLayout(new BoxLayout(overlay, BoxLayout.Y_AXIS));
        overlay.setBorder(new EmptyBorder(25, 20, 25, 10));

        JLabel hdr = new JLabel("DentalCare System");
        hdr.setAlignmentX(CENTER_ALIGNMENT);
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 24));
        hdr.setForeground(Color.WHITE);
        overlay.add(hdr);
        overlay.add(Box.createVerticalStrut(25));

        /* ---------- reusable nav buttons ---------- */
        JButton btnInventory  = navButton("Manage Inventory Items",
                () -> setRight(new FrmInventoryItem().getContentPanel()));

        JButton btnSuppliers  = navButton("Manage Suppliers",
                () -> setRight(new FrmSupplier().getContentPanel()));

        JButton btnInvoices   = navButton("Generate Invoice",
                () -> setRight(new FrmInvoiceByPatient(this, currentUser.getStaffID())));

        JButton btnNewPlan    = navButton("New Treatment Plan",
                () -> new FrmNewTreatmentPlan(this, currentUser.getStaffID()).setVisible(true));

        JButton btnPlansMgmt  = navButton("Plans Management",
                () -> setRight(new FrmPlansManagement(currentUser.getStaffID())));

        JButton btnDashboard  = navButton("Treatment Progress",
                () -> setRight(new FrmDentistDashboard(currentUser.getStaffID()).getContentPanel()));

        /* ---------- secretary-only placeholders ---------- */
        JButton btnBookAppt = navButton("Book Appointment",
        	    () -> setRight(new FrmBookAppointment().getContentPanel()));
        JButton btnConfirmAppt = navButton("Confirm Appointments",
                () -> setRight(new FrmPendingAppointments().getContentPanel()));
        JButton btnSterilization = navButton("Sterilization Requests",
        	    () -> setRight(new FrmSterilizationRequests().getContentPanel()));

        
        overlay.add(Box.createVerticalStrut(12));

        JButton btnRevenueReport = navButton("📈 Revenue Report", this::openRevenueReport);
        btnRevenueReport.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));

        JButton btnInventoryUsageReport = navButton("📦 Inventory Usage Report", this::openInventoryUsageReport);
        btnInventoryUsageReport.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));

        JButton btnExportAppointments = navButton("📤 Export Appointments Report", this::exportAppointmentsToJSON);
        btnExportAppointments.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));

        /* ---------- role-based layout ---------- */
        switch (currentUser.getRoleID()) {

            /* --- dentist --- */
            case Consts.ROLE_DENTIST -> {
                overlay.add(btnNewPlan);
                overlay.add(Box.createVerticalStrut(12));
                overlay.add(btnPlansMgmt);
                overlay.add(Box.createVerticalStrut(18));
                overlay.add(btnInvoices);
                overlay.add(Box.createVerticalStrut(18));
                overlay.add(btnDashboard);
            }

            /* --- secretary --- (no inventory / suppliers here) */
            case Consts.ROLE_SECRETARY -> {
                overlay.add(btnBookAppt);
                overlay.add(Box.createVerticalStrut(18));
                overlay.add(btnConfirmAppt);
                overlay.add(Box.createVerticalStrut(18));
                overlay.add(btnSterilization); // ← like your other buttons



                }
            
            case Consts.ROLE_MANAGER -> {
                overlay.add(btnInventory);
                overlay.add(Box.createVerticalStrut(12));
                overlay.add(btnSuppliers);
                overlay.add(Box.createVerticalStrut(18));

                overlay.add(btnNewPlan);
                overlay.add(Box.createVerticalStrut(12));
                overlay.add(btnPlansMgmt);
                overlay.add(Box.createVerticalStrut(12));
                overlay.add(btnInvoices);
                overlay.add(Box.createVerticalStrut(12));
                overlay.add(btnDashboard);
                overlay.add(Box.createVerticalStrut(18));

                // כפתורי הדוחות – רק למנהל
                overlay.add(btnRevenueReport);
                overlay.add(Box.createVerticalStrut(12));
                overlay.add(btnInventoryUsageReport);
                overlay.add(Box.createVerticalStrut(12));
                overlay.add(btnExportAppointments); 
            }

        }
        
      

        /* logout */
        JButton logout = new JButton("Logout");
        logout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logout.setForeground(Color.WHITE);
        logout.setBackground(new Color(180, 0, 0));
        logout.setFocusPainted(false);
        logout.setBorder(new EmptyBorder(8, 20, 8, 20));
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.addActionListener(e -> { dispose(); Main.main(null); });
        overlay.add(Box.createVerticalStrut(12));
        overlay.add(logout);

        gif.add(overlay, BorderLayout.CENTER);
        split.setLeftComponent(gif);

        /* ========== RIGHT PANEL ========== */
        rightPanel.setBackground(Color.WHITE);
        if (currentUser.getRoleID() == Consts.ROLE_SECRETARY) {
            showSecretaryWelcome();  // styled version
        } else {
            showWelcome();           // original version
        }
        split.setRightComponent(rightPanel);
                     // first view
        split.setRightComponent(rightPanel);
    }

    /* ─── helper: create a styled sidebar button ─── */
    private JButton navButton(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setAlignmentX(CENTER_ALIGNMENT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setBackground(new Color(0, 70, 130));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setMaximumSize(new Dimension(220, 38));
        b.setBorder(new LineBorder(new Color(0, 70, 130), 1, true));
        b.addActionListener(e -> action.run());
        return b;
    }
    /* ---------- helper to create white labels ---------- */
    private JLabel label(String txt,int size,int style){
        JLabel l=new JLabel(txt,SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI Emoji",style,size));
        l.setForeground(Color.WHITE);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    /* ---------- avatar loader ---------- */
    private JLabel loadAvatar(){
        for(String p:new String[]{"/icons/profile.png","/icons/person.png","/icons/profile.avif"}){
            var url=getClass().getResource(p);
            if(url!=null){
                Image img=new ImageIcon(url).getImage()
                             .getScaledInstance(110,110,Image.SCALE_SMOOTH);
                JLabel lbl=new JLabel(new ImageIcon(img));
                lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                lbl.setBorder(new LineBorder(Color.WHITE,2,true));
                return lbl;
            }
        }
        return null;
    }

    /* ---------- inventory alerts panel ---------- */
    private JPanel inventoryAlertsPanel(){
        JPanel p=new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

        p.add(label("🔔  Inventory Alerts",15,Font.BOLD));
        p.add(Box.createVerticalStrut(8));

        InventoryItemController invCtl=new InventoryItemController();
        TreatmentItemUsageController usageCtl=new TreatmentItemUsageController();
        int shown=0;
        LocalDate today=LocalDate.now();

        for(InventoryItem it:invCtl.getAllInventoryItems()){
        	LocalDate exp = ((java.sql.Date) it.getExpirationDt()).toLocalDate();


            long days=ChronoUnit.DAYS.between(today,exp);
            boolean red = days>=0 && days<=30;

            int used30=usageCtl.getUsedLast30Days(it.getItemID());
            int diff=it.getQuantityInSt()-used30;
            boolean yellow=diff<30;

            if(!red && !yellow) continue;
            Color c=red?Color.PINK.brighter():new Color(255,220,120);
            JLabel l=new JLabel("• "+it.getItemName()+"  Qty="+it.getQuantityInSt()+
                               (red?"  (Exp "+days+" d)":"  (Δ="+diff+")"));
            l.setFont(new Font("Segoe UI Emoji",Font.PLAIN,13));
            l.setForeground(c);
            p.add(l); p.add(Box.createVerticalStrut(5));
            shown++;
        }
        if(shown==0) p.add(label("• All inventory items are sufficient.",13,Font.ITALIC));
        return p;
    }

    /* ─── welcome screen ─── */
    /* ─── welcome screen ─── */
    /* ─── welcome screen ─── */
    /* ------------------ helper ------------------ */
    private JLabel info(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));   // ← emoji-friendly font
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    /* ─── welcome screen ─── */
    private void showWelcome() {

        rightPanel.removeAll();
        rightPanel.setLayout(new BorderLayout());

        /* 1 ▸ greeting */
        String name = switch (currentUser.getRoleID()) {
            case Consts.ROLE_DENTIST   -> "Dr. " + currentUser.getLastName();
            case Consts.ROLE_MANAGER   -> "Manager " + currentUser.getFirstName();
            case Consts.ROLE_SECRETARY -> currentUser.getFirstName();
            default -> currentUser.getFirstName();
        };
        JLabel greet = new JLabel("Welcome " + name, SwingConstants.CENTER);
        greet.setFont(new Font("Segoe UI", Font.BOLD, 28));
        greet.setForeground(new Color(0, 70, 130));
        greet.setBorder(new EmptyBorder(25, 0, 10, 0));

        /* 2 ▸ scrolling ticker */
        JPanel tickerBar = new JPanel(new BorderLayout());
        tickerBar.setBackground(new Color(0, 70, 130));

        JLabel lblTicker = new JLabel(" ", SwingConstants.CENTER);
        lblTicker.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));   // emoji font
        lblTicker.setForeground(Color.WHITE);
        tickerBar.add(lblTicker, BorderLayout.CENTER);
        startNewsTicker(lblTicker);

        /* 3 ▸ tips panel */
        JPanel tipsPanel = new JPanel();
        tipsPanel.setOpaque(false);
        tipsPanel.setLayout(new BoxLayout(tipsPanel, BoxLayout.Y_AXIS));
        tipsPanel.setBorder(new EmptyBorder(15, 40, 15, 40));

        String[] tips = (currentUser.getRoleID()==Consts.ROLE_SECRETARY)
            ? new String[] {
                "• Check today's low-stock alerts.",
                "• Review supplier confirmations.",
                "• Tip: Always sanitize tools before delivery." }
            : new String[] {
                "• DentalCare is ready to brighten smiles today!",
                "• Review treatment plans or create a new one.",
                "• Tip: Change toothbrushes every 3 months." };

        for (String t : tips) {
            JLabel l = new JLabel(t);
            l.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15)); // emoji font
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            tipsPanel.add(l);
            tipsPanel.add(Box.createVerticalStrut(6));
        }

        /* 4 ▸ role-specific block */
        JPanel rolePanel = new JPanel();
        rolePanel.setOpaque(false);
        rolePanel.setLayout(new BoxLayout(rolePanel, BoxLayout.Y_AXIS));
        rolePanel.setBorder(new EmptyBorder(10, 40, 20, 40));

        /* ==================== DENTIST ==================== */
        if (currentUser.getRoleID()==Consts.ROLE_DENTIST) {

            /* profile picture */
            try {
                Image img = new ImageIcon(getClass()
                        .getResource("/icons/profile.png"))
                        .getImage().getScaledInstance(110,110,Image.SCALE_SMOOTH);
                JLabel av = new JLabel(new ImageIcon(img));
                av.setAlignmentX(Component.CENTER_ALIGNMENT);
                rolePanel.add(av);
                rolePanel.add(Box.createVerticalStrut(10));
            } catch (Exception ignored) {}

            /* contact + qualification */
            JLabel roleLbl = new JLabel("🦷 Dentist", SwingConstants.CENTER);
            roleLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
            roleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            rolePanel.add(roleLbl);

            rolePanel.add(Box.createVerticalStrut(6));
            rolePanel.add(info("☎ "+currentUser.getPhoneNumt()+"   ✉ "+currentUser.getEmail()));
            rolePanel.add(info("🎓 "+currentUser.getQualification()));
            rolePanel.add(Box.createVerticalStrut(18));

            /* upcoming appointments */
            JLabel h = new JLabel("Your Upcoming Appointments:");
            h.setFont(new Font("Segoe UI", Font.BOLD, 16));
            h.setAlignmentX(Component.CENTER_ALIGNMENT);
            rolePanel.add(h); rolePanel.add(Box.createVerticalStrut(8));

            List<Appointment> list =
                new AppointmentController().getUpcomingAppointmentsForStaff(currentUser.getStaffID());
            int shown = 0;
            for (Appointment a : list) {
                if (shown == 3) break;
                String line = "• " + a.getAppointmentDate() + " – " + getPatientName(a.getPatientID());
                JLabel l = new JLabel(line);
                l.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                l.setAlignmentX(Component.CENTER_ALIGNMENT);
                rolePanel.add(l); rolePanel.add(Box.createVerticalStrut(4));
                shown++;
            }
            if (shown == 0) rolePanel.add(info("No upcoming appointments."));

        /* ==================== SECRETARY ==================== */
        } else if (currentUser.getRoleID()==Consts.ROLE_SECRETARY) {

            JLabel h = new JLabel("📞 Calls to Return:");
            h.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
            h.setAlignmentX(Component.CENTER_ALIGNMENT);
            rolePanel.add(h); rolePanel.add(Box.createVerticalStrut(8));

            for (String s : new String[]{
                    "• Call supplier: confirm ETA",
                    "• Follow-up with patient Gal Peled",
                    "• Notify Dr. Brown: inventory delay"}) {

                JLabel l = new JLabel(s);
                l.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                l.setAlignmentX(Component.CENTER_ALIGNMENT);
                rolePanel.add(l); rolePanel.add(Box.createVerticalStrut(3));
            }
        }
        /* ==================== MANAGER ==================== */
        else if (currentUser.getRoleID() == Consts.ROLE_MANAGER) {

            JLabel title = new JLabel(" Clinic Manager Profile:");
            title.setFont(new Font("Segoe UI", Font.BOLD, 16));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            rolePanel.add(title);
            rolePanel.add(Box.createVerticalStrut(10));

            // Profile Info
            rolePanel.add(info("👤 " + currentUser.getFirstName() + " " + currentUser.getLastName()));
            rolePanel.add(info("☎ " + currentUser.getPhoneNumt()));
            rolePanel.add(info("✉ " + currentUser.getEmail()));
            rolePanel.add(info("🎓 " + currentUser.getQualification()));
            rolePanel.add(Box.createVerticalStrut(12));

            // Optional: Motivational or statistical info
            rolePanel.add(info("📈 This quarter: Clinic revenue up 12%"));
            rolePanel.add(info("📝 3 supplier orders confirmed today"));
        }


        /* 5 ▸ messages rail */
        DefaultListModel<String> dm = new DefaultListModel<>();
        dm.addElement("💌 New lab result available");
        dm.addElement("📦 Supplier confirmed delivery");
        dm.addElement("😊 Patient feedback received");

        JList<String> lst = new JList<>(dm);
        lst.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        lst.setFixedCellHeight(28);
        lst.setBorder(new EmptyBorder(10,10,10,10));

        JPanel rail = new JPanel(new BorderLayout());
        rail.setPreferredSize(new Dimension(220,0));
        rail.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220,220,220)),
                new EmptyBorder(5,5,5,5)));
        JLabel railHdr = new JLabel("Messages",SwingConstants.CENTER);
        railHdr.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        rail.add(railHdr,BorderLayout.NORTH);
        rail.add(new JScrollPane(lst),BorderLayout.CENTER);

        /* 6 ▸ assemble center panel */
        JPanel center = new JPanel(); center.setOpaque(false);
        center.setLayout(new BoxLayout(center,BoxLayout.Y_AXIS));
        center.add(greet);

        if (currentUser.getRoleID()==Consts.ROLE_SECRETARY) {
            JLabel ph = new JLabel("☎  " + currentUser.getPhoneNumt());
            ph.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            ph.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(ph);
        }

        center.add(tipsPanel);
        center.add(rolePanel);

        rightPanel.add(tickerBar, BorderLayout.NORTH);
        rightPanel.add(center,    BorderLayout.CENTER);
        rightPanel.add(rail,      BorderLayout.EAST);
        rightPanel.revalidate(); 
        rightPanel.repaint();
    }


    private void showSecretaryWelcome() {
        rightPanel.removeAll();
        rightPanel.setLayout(new BorderLayout());

        /* -------- colours -------- */
        Color bg      = new Color(0x1E5CAB);   // lighter blue
        Color sideBar = new Color(0x164889);

        /* -------- north ticker -------- */
        JLabel t = new JLabel(" ", SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        t.setForeground(Color.WHITE);
        JPanel ticker = new JPanel(new BorderLayout());
        ticker.setBackground(sideBar);
        ticker.add(t);
        startNewsTicker(t);
        rightPanel.add(ticker, BorderLayout.NORTH);

        /* -------- west : calls -------- */
        JPanel calls = new JPanel();
        calls.setPreferredSize(new Dimension(220,0));
        calls.setBackground(sideBar);
        calls.setLayout(new BoxLayout(calls,BoxLayout.Y_AXIS));
        calls.setBorder(new EmptyBorder(12,12,12,12));
        calls.add(label("📞  Calls to Return",15,Font.BOLD));
        for (String s : new String[]{
                "• Call supplier: confirm ETA",
                "• Follow-up: Gal Peled",
                "• Notify Dr Brown: inventory delay"}) {
            calls.add(Box.createVerticalStrut(8));
            calls.add(label(s,13,Font.PLAIN));
        }
        rightPanel.add(calls, BorderLayout.WEST);

        /* -------- east : messages -------- */
        JPanel msgs = new JPanel(new BorderLayout());
        msgs.setPreferredSize(new Dimension(240,0));
        msgs.setBackground(sideBar);
        msgs.setBorder(new EmptyBorder(12,12,12,12));
        msgs.add(label("✉  Messages",15,Font.BOLD), BorderLayout.NORTH);
        DefaultListModel<String> dm = new DefaultListModel<>();
        dm.addElement("💌  New lab result available");
        dm.addElement("📦  Supplier confirmed delivery");
        dm.addElement("😊  Patient Maya A. sent feedback");
        JList<String> lst = new JList<>(dm);
        lst.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        lst.setOpaque(false);
        lst.setForeground(Color.WHITE);
        msgs.add(new JScrollPane(lst){{
            setBorder(null); getViewport().setOpaque(false); setOpaque(false);
        }}, BorderLayout.CENTER);
        rightPanel.add(msgs, BorderLayout.EAST);

        /* -------- centre : profile & alerts -------- */
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center,BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(30,40,30,40));

        JLabel pic = loadAvatar();
        if (pic!=null) { center.add(pic); center.add(Box.createVerticalStrut(10)); }

        center.add(label(currentUser.getFirstName()+" "+currentUser.getLastName()+" — Secretary",
                         26,Font.BOLD));
        center.add(Box.createVerticalStrut(4));
        center.add(label("☎ "+currentUser.getPhoneNumt()+"   ✉ "+currentUser.getEmail(),
                         14,Font.PLAIN));
        center.add(Box.createVerticalStrut(2));
        center.add(label("🎓 "+currentUser.getQualification(),14,Font.PLAIN));
        center.add(Box.createVerticalStrut(6));
       
        center.add(Box.createVerticalStrut(18));
        center.add(inventoryAlertsPanel());

        rightPanel.setBackground(bg);
        rightPanel.add(center, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }


    /* helper labels */
    

    /* ticker animation */
    private void startNewsTicker(JLabel lbl){
        String[] lines={
                "ADA: AI speeds diagnosis • ",
                "New whitening tech wins award • ",
                "Clinic revenue up 12% this quarter • "};
        final int[] idx={0};
        StringBuilder sb=new StringBuilder(lines[0]);
        new Timer(140,e->{
            char c=sb.charAt(0);
            sb.deleteCharAt(0).append(c);
            lbl.setText(sb.toString());
            if(c=='•'){idx[0]=(idx[0]+1)%lines.length;
                sb.setLength(0);sb.append(lines[idx[0]]);}
        }).start();
    }

    /* helpers */
    private String getPatientName(int id){
        Patient p=new PatientController().getPatient(id);
        return p==null?"(Unknown)":p.getFirstName()+" "+p.getLastName();
    }
    private void setRight(JPanel p){
        rightPanel.removeAll();
        rightPanel.add(p,BorderLayout.CENTER);
        rightPanel.revalidate(); rightPanel.repaint();
    }
    private void openRevenueReport() {
        try {
            InputStream reportStream = getClass().getResourceAsStream("/Revenue.jasper");

            if (reportStream == null) {
                JOptionPane.showMessageDialog(this, "Report not found in resources!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Connection conn = controller.DataBaseManager.getConnection(); // or your actual method
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, null, conn);
            JasperViewer.viewReport(jasperPrint, false); // false = don’t exit app on close

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading report:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void openInventoryUsageReport() {
        try {
            InputStream reportStream = getClass().getResourceAsStream("/InventoryUsageReport.jasper");

            if (reportStream == null) {
                JOptionPane.showMessageDialog(this, "❌ InventoryUsageReport.jasper not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Connection conn = controller.DataBaseManager.getConnection();
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, null, conn);
            JasperViewer.viewReport(jasperPrint, false);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error loading Inventory Usage Report:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void exportAppointmentsToJSON() {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = controller.DataBaseManager.getConnection()) {
            String sql = """
                SELECT * FROM TblAppointment
                WHERE status = 'Completed'
                  AND MONTH(appointmentDate) = MONTH(CURRENT_DATE)
                  AND YEAR(appointmentDate) = YEAR(CURRENT_DATE)
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("appointmentID", rs.getInt("appointmentID"));
                row.put("patientID", rs.getInt("patientID"));
                row.put("staffID", rs.getInt("staffID"));
                row.put("treatmentID", rs.getInt("treatmentID"));
                row.put("appointmentDate", rs.getDate("appointmentDate"));
                row.put("appointmentTime", rs.getString("appointmentTime"));
                row.put("reasonID", rs.getInt("reasonID"));
                row.put("status", rs.getString("status"));
                row.put("paid", rs.getBoolean("paid"));
                rows.add(row);
            }

            // המרה ל-JSON וכתיבה לקובץ
            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(rows);
            java.nio.file.Files.write(java.nio.file.Paths.get("appointments_report.json"), json.getBytes());

            JOptionPane.showMessageDialog(this, "✅ appointments_report.json created!");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Failed to export JSON:\n" + e.getMessage());
        }
    }


}
