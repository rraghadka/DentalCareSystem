package boundary;

import controller.*;
import entity.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Secretary dashboard – blue-card theme, 2025-06-28
 */
public class FrmSecretaryDashboard extends JPanel {

    /* remember sterilised appointments */
    private static final Path STERILE =
            Paths.get(System.getProperty("user.home"), ".dentalcare", "sterilised.txt");

    /* controllers */
    private final Staff                        me;
    private final AppointmentController        apptCtl  = new AppointmentController();
    private final InventoryItemController      invCtl   = new InventoryItemController();
    private final PatientController            patCtl   = new PatientController();
    private final TreatmentItemUsageController usageCtl = new TreatmentItemUsageController();

    private final Set<Integer> sterilised = new HashSet<>();

    /* palette */
    private static final Color BLUE      = new Color(0x00467F); // deep blue
    private static final Color PANEL_BG  = new Color(0xF6F9FD);
    private static final Color SHADOW    = new Color(0,0,0,34);

    public FrmSecretaryDashboard(Staff user) {
        this.me = user;
        loadSterilised();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(ticker(),  BorderLayout.NORTH);
        add(messages(),BorderLayout.EAST);
        add(centre(),  BorderLayout.CENTER);
    }

    /* ─────────────────── ticker ─────────────────── */
    private JComponent ticker() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BLUE);

        JLabel lbl = new JLabel(" ", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(font(14, Font.BOLD));
        bar.add(lbl);

        String[] news = {
                "Clinic revenue up 12% this quarter • ",
                "ADA: AI speeds diagnosis • ",
                "Next supplier import – 1 Aug • "
        };
        StringBuilder sb = new StringBuilder(news[0]);
        int[] idx = {0};

        new Timer(130, e -> {
            char c = sb.charAt(0);
            sb.deleteCharAt(0).append(c);
            lbl.setText(sb.toString());
            if (c=='•') { idx[0]=(idx[0]+1)%news.length; sb.setLength(0); sb.append(news[idx[0]]); }
        }).start();
        return bar;
    }

    /* ─────────────────── messages rail ─────────────────── */
    private JComponent messages() {
        JPanel rail = new JPanel(new BorderLayout());
        rail.setPreferredSize(new Dimension(240,0));
        rail.setBorder(new CompoundBorder(
                new LineBorder(new Color(200,210,255)),
                new EmptyBorder(10,12,10,12)));
        rail.setBackground(PANEL_BG);

        JLabel hdr = new JLabel("Messages", SwingConstants.CENTER);
        hdr.setFont(font(15, Font.BOLD));
        rail.add(hdr, BorderLayout.NORTH);

        JTextArea ta = new JTextArea("""
                💌  New lab result available
                📦  Supplier confirmed delivery
                😊  Patient Maya A. sent feedback
                """);
        ta.setOpaque(false); ta.setEditable(false);
        ta.setFont(font(13, Font.PLAIN));
        rail.add(ta, BorderLayout.CENTER);
        return rail;
    }

    /* ─────────────────── centre column ─────────────────── */
    private JComponent centre() {
        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(25,60,25,60));

        JLabel pic = avatar();
        if (pic!=null) { pic.setAlignmentX(CENTER_ALIGNMENT); root.add(pic); }

        root.add(vgap(8));
        root.add(lbl(me.getFirstName()+" "+me.getLastName(), 28, Font.BOLD, BLUE));

        root.add(vgap(4));
        root.add(lbl("☎ "+me.getPhoneNumt()+"   ✉ "+me.getEmail(), 14, Font.PLAIN, Color.DARK_GRAY));

        root.add(vgap(2));
        root.add(lbl("🎓 "+me.getQualification(), 14, Font.PLAIN, Color.DARK_GRAY));

        root.add(vgap(2));
       
        root.add(vgap(6));
        root.add(lbl("Dentists are assigned to patients by specialisation (e.g. orthodontics).",
                     13, Font.ITALIC, Color.GRAY));

        root.add(vgap(18));
        root.add(card("📞  Calls to Return", """
                • Call supplier: confirm ETA
                • Follow-up with patient Gal Peled
                • Notify Dr. Brown: inventory delay
                """));

        root.add(vgap(14));
        root.add(sterilisationCard());

        root.add(vgap(14));
        root.add(inventoryAlerts());

        return root;
    }

    /* ── blue card helper ── */
    private JComponent card(String title, String body) {
        JTextArea ta = new JTextArea(body); ta.setOpaque(false); ta.setEditable(false);
        ta.setFont(font(13, Font.PLAIN)); ta.setForeground(Color.WHITE);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(true);
        inner.setBackground(BLUE);
        inner.setBorder(new EmptyBorder(12,18,14,18));
        inner.add(lbl(title,15, Font.BOLD, Color.WHITE));
        inner.add(vgap(6));
        inner.add(ta);

        return shadow(inner);
    }

    /* ── sterilisation checklist ── */
    private JComponent sterilisationCard() {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(true);
        inner.setBackground(BLUE);
        inner.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(Color.WHITE),"🧴  Sterilisation Clearance"){
                    { setTitleColor(Color.WHITE); }},
                new EmptyBorder(6,8,8,8)));

        List<Appointment> today = apptCtl.getTodaysAppointments();
        if (today.isEmpty()) {
            inner.add(lbl("No appointments today.",13,Font.ITALIC,Color.WHITE));
        } else {
            for (Appointment a: today) {
                Patient p = patCtl.getPatient(a.getPatientID());
                String pn = (p==null) ? "Unknown" : p.getFirstName()+" "+p.getLastName();
                boolean done = sterilised.contains(a.getAppointmentID());

                JCheckBox chk = new JCheckBox(a.getAppointmentTime()+" – "+pn);
                chk.setSelected(done); chk.setEnabled(!done);
                chk.setFont(font(13, Font.PLAIN));
                chk.setForeground(Color.WHITE); chk.setOpaque(false);
                chk.addActionListener(e -> {
                    sterilised.add(a.getAppointmentID());
                    saveSterilised(); chk.setEnabled(false);
                });
                inner.add(chk);
            }
        }
        return shadow(inner);
    }

    /* ── inventory alerts ── */
    private JComponent inventoryAlerts() {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(true);
        inner.setBackground(BLUE);
        inner.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(Color.WHITE),"🔔  Inventory Alerts"){
                    { setTitleColor(Color.WHITE); }},
                new EmptyBorder(6,8,8,8)));

        int shown = 0;
        LocalDate now = LocalDate.now();

        for (InventoryItem it: invCtl.getAllInventoryItems()) {
            LocalDate exp = it.getExpirationDt()==null
                    ? LocalDate.MAX
                    : it.getExpirationDt().toInstant()
                          .atZone(ZoneId.systemDefault()).toLocalDate();
            long days = ChronoUnit.DAYS.between(now, exp);
            boolean red = days>=0 && days<=30;

            int used30 = usageCtl.getUsedLast30Days(it.getItemID());
            int diff   = it.getQuantityInSt() - used30;
            boolean yel = diff < 30;

            if (!red && !yel) continue;

            Color clr = red ? Color.PINK.brighter() : new Color(255, 220, 120);
            JLabel l = new JLabel("• "+it.getItemName()+"  Qty="+it.getQuantityInSt()+
                    (red ? "  (Exp "+days+" d)" : "  (Δ="+diff+")"));
            l.setFont(font(13, Font.PLAIN)); l.setForeground(clr);
            inner.add(l); shown++;
        }
        if (shown==0)
            inner.add(lbl("All inventory items are sufficient.",13,Font.ITALIC,Color.WHITE));

        return shadow(inner);
    }

    /* ── avatar ── */
    private JLabel avatar() {
        for (String f: new String[]{"/icons/profile.png","/icons/person.png","/icons/profile.avif"}) {
            var url=getClass().getResource(f);
            if (url!=null) {
                Image img=new ImageIcon(url).getImage().getScaledInstance(110,110,Image.SCALE_SMOOTH);
                return new JLabel(new ImageIcon(img));
            }
        }
        return null;
    }

    /* ── UI helpers ── */
    private Font font(int sz,int style){return new Font("Segoe UI Emoji",style,sz);}
    private Component vgap(int px){return Box.createVerticalStrut(px);}
    private JLabel lbl(String t,int s,int sty,Color c){
        JLabel l=new JLabel(t,SwingConstants.CENTER);
        l.setFont(font(s,sty)); l.setForeground(c); l.setAlignmentX(CENTER_ALIGNMENT); return l;
    }
    private JLabel lbl(String t,int s,int sty){return lbl(t,s,sty,Color.WHITE);}
    private JPanel shadow(JComponent content){
        JPanel w=new JPanel(new BorderLayout()){
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                g.setColor(SHADOW);
                g.fillRoundRect(2,2,getWidth()-4,getHeight()-4,12,12);
            }};
        w.setOpaque(false); w.add(content); w.setAlignmentX(CENTER_ALIGNMENT); return w;
    }

    /* ── persistence ── */
    private void loadSterilised(){
        try{ if(Files.exists(STERILE))
                 for(String ln:Files.readAllLines(STERILE))
                     sterilised.add(Integer.parseInt(ln.trim()));
        }catch(IOException ignored){}
    }
    private void saveSterilised(){
        try{
            Files.createDirectories(STERILE.getParent());
            Files.write(STERILE, sterilised.stream().map(String::valueOf).toList(),
                        StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
        }catch(IOException ignored){}
    }
}
