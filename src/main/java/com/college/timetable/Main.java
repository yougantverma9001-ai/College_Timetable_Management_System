package com.college.timetable;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class Main {
    private static final Color NAVY = new Color(30, 41, 75), BLUE = new Color(65, 105, 225),
            TEAL = new Color(20, 184, 166), PURPLE = new Color(139, 92, 246),
            ORANGE = new Color(249, 115, 22), BG = new Color(243, 246, 252);
    private JFrame frame;
    private String userId, role, fullName;
    private JTabbedPane tabs;
    private JLabel status;

    public static void main(String[] args) {
        try { FlatLightLaf.setup(); UIManager.put("Button.arc", 16); UIManager.put("Component.arc", 12); UIManager.put("TextComponent.arc", 10); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            Database.configure();
            try { Database.initialize(); new Main().showLogin(); }
            catch (SQLException e) { JOptionPane.showMessageDialog(null, "Cannot connect to MySQL.\n\n" + e.getMessage() +
                    "\n\nCheck MySQL is running and db.properties is configured.", "Database connection error", JOptionPane.ERROR_MESSAGE); }
        });
    }

    private void showLogin() {
        JFrame login = new JFrame("CampusFlow | Timetable Management");
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); login.setSize(850,520); login.setLocationRelativeTo(null);
        JPanel root = new JPanel(new GridLayout(1,2)); root.setBackground(Color.WHITE);
        JPanel brand = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,new Color(27,39,82),getWidth(),getHeight(),new Color(72,75,160))); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose(); }
        };
        brand.setLayout(new BoxLayout(brand,BoxLayout.Y_AXIS)); brand.setBorder(new EmptyBorder(70,48,50,35));
        JLabel logo = new JLabel("◈  CAMPUSFLOW"); logo.setForeground(new Color(123,235,220)); logo.setFont(new Font("SansSerif",Font.BOLD,20));
        JLabel headline = new JLabel("<html>Make every<br>period count.</html>"); headline.setForeground(Color.WHITE); headline.setFont(new Font("SansSerif",Font.BOLD,36));
        JLabel sub = new JLabel("<html>Smart scheduling. Quick substitutions.<br>One connected campus.</html>"); sub.setForeground(new Color(220,226,245)); sub.setFont(new Font("SansSerif",Font.PLAIN,15));
        brand.add(logo); brand.add(Box.createVerticalStrut(65)); brand.add(headline); brand.add(Box.createVerticalStrut(20)); brand.add(sub);
        JPanel form = new JPanel(); form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS)); form.setBorder(new EmptyBorder(70,55,45,55)); form.setBackground(Color.WHITE);
        JLabel welcome = new JLabel("Welcome back"); welcome.setFont(new Font("SansSerif",Font.BOLD,27)); welcome.setForeground(NAVY);
        JLabel hint = new JLabel("Sign in to your timetable workspace"); hint.setForeground(new Color(110,120,140));
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Administrator","Teacher"});
        JTextField id = new JTextField("admin"); JPasswordField pass = new JPasswordField("admin123");
        styleField(id); styleField(pass);
        JButton loginBtn = button("Sign in",TEAL); JLabel error = new JLabel(" ");
        form.add(welcome); form.add(Box.createVerticalStrut(8)); form.add(hint); form.add(Box.createVerticalStrut(28));
        form.add(fieldLabel("LOGIN AS")); form.add(roleBox); form.add(Box.createVerticalStrut(14));
        form.add(fieldLabel("USER ID")); form.add(id); form.add(Box.createVerticalStrut(14));
        form.add(fieldLabel("PASSWORD")); form.add(pass); form.add(Box.createVerticalStrut(22)); form.add(loginBtn);
        error.setForeground(new Color(220,60,70)); form.add(error); form.add(Box.createVerticalStrut(14));
        JLabel demo = new JLabel("<html><b>Demo Admin:</b> admin / admin123<br><b>Demo Teacher:</b> T1–T6 / password123</html>");
        demo.setForeground(new Color(110,120,140)); form.add(demo);
        loginBtn.addActionListener(e -> {
            String wanted = roleBox.getSelectedIndex()==0 ? "ADMIN" : "TEACHER";
            try (Connection c=Database.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT id,full_name,role FROM users WHERE id=? AND password_hash=? AND active=TRUE AND role=?")) {
                ps.setString(1,id.getText().trim()); ps.setString(2,Database.hash(new String(pass.getPassword()))); ps.setString(3,wanted);
                try(ResultSet rs=ps.executeQuery()) { if(rs.next()) { userId=rs.getString(1); fullName=rs.getString(2); role=rs.getString(3); login.dispose(); showMain(); }
                    else error.setText("Invalid ID, password or role."); }
            } catch(SQLException ex) { error.setText("Database error: "+ex.getMessage()); }
        });
        root.add(brand); root.add(form); login.setContentPane(root); login.setVisible(true);
    }

    private void showMain() {
        frame=new JFrame("CampusFlow | "+(role.equals("ADMIN")?"Administrator":"Teacher")+" Workspace");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setSize(1280,800); frame.setMinimumSize(new Dimension(1050,680)); frame.setLocationRelativeTo(null);
        JPanel root=new JPanel(new BorderLayout()); root.setBackground(BG);
        JPanel top=new JPanel(new BorderLayout()); top.setBackground(NAVY); top.setBorder(new EmptyBorder(18,26,18,24));
        JLabel brand=new JLabel("◈  CAMPUSFLOW     <html><span style='font-weight:normal;color:#B9C7E8'> / Timetable Management</span></html>");
        brand.setForeground(Color.WHITE); brand.setFont(new Font("SansSerif",Font.BOLD,20));
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0)); right.setOpaque(false);
        JLabel who=new JLabel("Signed in: "+fullName+"  "); who.setForeground(Color.WHITE);
        JButton logout=button("Log out",new Color(225,75,90)); logout.addActionListener(e->{frame.dispose();showLogin();});
        right.add(who);right.add(logout);top.add(brand,BorderLayout.WEST);top.add(right,BorderLayout.EAST);
        tabs=new JTabbedPane(); tabs.setFont(new Font("SansSerif",Font.BOLD,13)); tabs.setBackground(BG);
        if(role.equals("ADMIN")) {
            tabs.addTab("  Overview  ",dashboard()); tabs.addTab("  Master Timetable  ",timetable());
            tabs.addTab("  Teachers  ",teachers()); tabs.addTab("  Absence & Cover  ",absence());
            tabs.addTab("  Events & Class Merge  ",eventsAndMerge());
        } else {
            tabs.addTab("  My Timetable  ",mySchedule()); tabs.addTab("  My Free Periods  ",freePeriods());
            tabs.addTab("  Report Absence  ",teacherAbsence());
        }
        status=new JLabel("  Connected to MySQL  |  Changes are saved immediately"); status.setBorder(new EmptyBorder(9,12,9,12)); status.setForeground(new Color(90,105,130));
        root.add(top,BorderLayout.NORTH);root.add(tabs,BorderLayout.CENTER);root.add(status,BorderLayout.SOUTH);
        frame.setContentPane(root);frame.setVisible(true);
    }

    private JPanel page(String title,String subtitle) {
        JPanel p=new JPanel(new BorderLayout(15,15));p.setBackground(BG);p.setBorder(new EmptyBorder(24,26,24,26));
        JPanel head=new JPanel();head.setOpaque(false);head.setLayout(new BoxLayout(head,BoxLayout.Y_AXIS));
        JLabel t=new JLabel(title);t.setFont(new Font("SansSerif",Font.BOLD,25));t.setForeground(NAVY);
        JLabel s=new JLabel(subtitle);s.setFont(new Font("SansSerif",Font.PLAIN,13));s.setForeground(new Color(110,120,140));
        head.add(t);head.add(Box.createVerticalStrut(5));head.add(s);p.add(head,BorderLayout.NORTH);return p;
    }
    private JPanel card() { JPanel p=new JPanel(new BorderLayout(10,10));p.setBackground(Color.WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(229,234,244)),new EmptyBorder(16,16,16,16)));return p; }
    private JLabel fieldLabel(String s) { JLabel l=new JLabel(s);l.setFont(new Font("SansSerif",Font.BOLD,11));l.setForeground(new Color(90,105,130));return l; }
    private void styleField(JComponent c) { c.setFont(new Font("SansSerif",Font.PLAIN,14));c.setPreferredSize(new Dimension(220,38)); }
    private JButton button(String text,Color color) { JButton b=new JButton(text);b.setBackground(color);b.setForeground(Color.WHITE);b.setFont(new Font("SansSerif",Font.BOLD,12));b.setFocusPainted(false);b.setBorder(new EmptyBorder(10,16,10,16));return b; }
    private DefaultTableModel model(String... cols) { return new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}}; }
    private JTable table(DefaultTableModel m) { JTable t=new JTable(m);t.setRowHeight(30);t.setFont(new Font("SansSerif",Font.PLAIN,13));t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));t.getTableHeader().setBackground(new Color(235,240,250));t.setSelectionBackground(new Color(218,232,255));return t; }
    private String[] days() { return new String[]{"Monday","Tuesday","Wednesday","Thursday","Friday"}; }
    private String[] periods() { return new String[]{"1","2","3","4","5","6"}; }
    private void info(String s) { JOptionPane.showMessageDialog(frame,s,"CampusFlow",JOptionPane.INFORMATION_MESSAGE); }
    private void error(Exception e) { JOptionPane.showMessageDialog(frame,e.getMessage(),"Operation failed",JOptionPane.ERROR_MESSAGE); }
    private void refresh() { if(tabs!=null) { int i=tabs.getSelectedIndex(); String title=tabs.getTitleAt(i).trim(); tabs.setComponentAt(i, switch(title) {
        case "Overview"->dashboard(); case "Master Timetable"->timetable(); case "Teachers"->teachers();
        case "Absence & Cover"->absence(); case "Events & Class Merge"->eventsAndMerge();
        case "My Timetable"->mySchedule();case "My Free Periods"->freePeriods();case "Report Absence"->teacherAbsence();default->dashboard();}); } }

    private JPanel dashboard() {
        JPanel p=page("Good day, "+fullName,"Here is the live scheduling snapshot for your department.");
        JPanel body=new JPanel(new BorderLayout(15,15));body.setOpaque(false);
        JPanel stats=new JPanel(new GridLayout(1,4,14,0));stats.setOpaque(false);
        try(Connection c=Database.getConnection()) {
            stats.add(stat("Faculty members",count(c,"SELECT COUNT(*) FROM users WHERE role='TEACHER' AND active=TRUE"),BLUE));
            stats.add(stat("Weekly timetable slots",count(c,"SELECT COUNT(*) FROM timetable_entries"),PURPLE));
            stats.add(stat("Absence records",count(c,"SELECT COUNT(*) FROM absences WHERE status='ABSENT'"),ORANGE));
            stats.add(stat("Substitutions assigned",count(c,"SELECT COUNT(*) FROM substitutions"),TEAL));
        } catch(SQLException e){error(e);}
        JPanel lower=new JPanel(new GridLayout(1,2,14,0));lower.setOpaque(false);
        JPanel a=card();a.add(new JLabel("  Today's absence & cover activity"),BorderLayout.NORTH);
        DefaultTableModel m=model("Teacher","Day","Status");try(Connection c=Database.getConnection();Statement st=c.createStatement();ResultSet rs=st.executeQuery("SELECT u.full_name,a.day_name,a.status FROM absences a JOIN users u ON u.id=a.teacher_id ORDER BY a.id DESC LIMIT 8")){while(rs.next())m.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3)});}catch(SQLException e){error(e);}
        a.add(new JScrollPane(table(m)),BorderLayout.CENTER);
        JPanel b=card();b.add(new JLabel("  How smart cover works"),BorderLayout.NORTH);
        JTextArea guide=new JTextArea("1. Mark the absent teacher and the day.\n\n2. CampusFlow identifies every affected period.\n\n3. It finds teachers who are free at that exact time.\n\n4. Subject specialists are ranked first. Choose a suggested teacher and assign the cover.\n\n5. Every change is saved in MySQL immediately.");guide.setEditable(false);guide.setLineWrap(true);guide.setWrapStyleWord(true);guide.setFont(new Font("SansSerif",Font.PLAIN,14));guide.setBackground(Color.WHITE);b.add(guide);
        lower.add(a);lower.add(b);body.add(stats,BorderLayout.NORTH);body.add(lower,BorderLayout.CENTER);p.add(body,BorderLayout.CENTER);return p;
    }
    private int count(Connection c,String sql)throws SQLException{try(Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getInt(1);}}
    private JPanel stat(String label,int value,Color accent){JPanel p=card();JPanel bar=new JPanel();bar.setBackground(accent);bar.setPreferredSize(new Dimension(5,55));JLabel n=new JLabel(String.valueOf(value));n.setFont(new Font("SansSerif",Font.BOLD,30));n.setForeground(NAVY);JLabel l=new JLabel(label);l.setForeground(new Color(105,115,135));JPanel text=new JPanel();text.setOpaque(false);text.setLayout(new BoxLayout(text,BoxLayout.Y_AXIS));text.add(n);text.add(l);p.add(bar,BorderLayout.WEST);p.add(text,BorderLayout.CENTER);return p;}

    private JPanel timetable() {
        JPanel p=page("Master timetable","CSE-A • Monday to Friday • Six teaching periods • Fixed break after Period 3");
        DefaultTableModel m=model("Day","P1","P2","P3","BREAK","P4","P5","P6");
        try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("SELECT day_name,period_no,subject,teacher_id,room,entry_type FROM timetable_entries ORDER BY FIELD(day_name,'Monday','Tuesday','Wednesday','Thursday','Friday'),period_no");ResultSet rs=ps.executeQuery()){
            Map<String,String[]> map=new LinkedHashMap<>();for(String d:days())map.put(d,new String[6]);
            while(rs.next()){String[] row=map.get(rs.getString(1));int n=rs.getInt(2)-1;row[n]=rs.getString(3)+"\\n"+rs.getString(4)+" • "+rs.getString(5)+(rs.getString(6).equals("LAB")?" • LAB":"");}
            for(String d:days()){String[] r=map.get(d);m.addRow(new Object[]{d,r[0],r[1],r[2],"BREAK",r[3],r[4],r[5]});}
        }catch(SQLException e){error(e);}
        JTable t=table(m);t.setRowHeight(78);t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JPanel c=card();c.add(new JScrollPane(t));p.add(c,BorderLayout.CENTER);return p;
    }

    private JPanel teachers() {
        JPanel p=page("Faculty directory","Add, review and deactivate teacher accounts. Passwords are stored as SHA-256 hashes.");
        DefaultTableModel m=model("Teacher ID","Full name","Subject","Status");JTable t=table(m);
        Runnable load=()->{m.setRowCount(0);try(Connection c=Database.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,full_name,subject,IF(active,'Active','Inactive') FROM users WHERE role='TEACHER' ORDER BY id")){while(r.next())m.addRow(new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4)});}catch(SQLException e){error(e);}};
        load.run();JPanel list=card();list.add(new JScrollPane(t));JPanel form=card();form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));form.setPreferredSize(new Dimension(290,400));
        JTextField id=new JTextField(),name=new JTextField(),subject=new JTextField();JPasswordField pass=new JPasswordField();
        for(JComponent x:new JComponent[]{id,name,subject,pass})styleField(x);
        form.add(fieldLabel("TEACHER ID"));form.add(id);form.add(fieldLabel("FULL NAME"));form.add(name);form.add(fieldLabel("SUBJECT SPECIALIZATION"));form.add(subject);form.add(fieldLabel("INITIAL PASSWORD"));form.add(pass);
        JButton add=button("＋  Add teacher",TEAL);add.addActionListener(e->{try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("INSERT INTO users(id,full_name,subject,password_hash,role) VALUES(?,?,?,?, 'TEACHER')")){
            ps.setString(1,id.getText().trim());ps.setString(2,name.getText().trim());ps.setString(3,subject.getText().trim());ps.setString(4,Database.hash(new String(pass.getPassword())));
            if(id.getText().isBlank()||name.getText().isBlank()||subject.getText().isBlank()||pass.getPassword().length==0)throw new IllegalArgumentException("Fill in all teacher fields.");ps.executeUpdate();load.run();info("Teacher added successfully.");id.setText("");name.setText("");subject.setText("");pass.setText("");
        }catch(Exception ex){error(ex);}});
        JButton remove=button("Deactivate selected",new Color(220,75,90));remove.addActionListener(e->{int row=t.getSelectedRow();if(row<0){info("Select a teacher first.");return;}String tid=m.getValueAt(row,0).toString();if(JOptionPane.showConfirmDialog(frame,"Deactivate "+tid+"?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE users SET active=FALSE WHERE id=? AND role='TEACHER'")){ps.setString(1,tid);ps.executeUpdate();load.run();}catch(SQLException ex){error(ex);}});
        form.add(add);form.add(Box.createVerticalStrut(8));form.add(remove);
        JPanel center=new JPanel(new BorderLayout(14,0));center.setOpaque(false);center.add(list,BorderLayout.CENTER);center.add(form,BorderLayout.EAST);p.add(center,BorderLayout.CENTER);return p;
    }

    private JPanel absence() {
        JPanel p=page("Absence & smart cover","Find available teachers for every affected class and assign a substitute without timetable clashes.");
        JPanel top=card();top.setLayout(new FlowLayout(FlowLayout.LEFT,12,8));JComboBox<String> day=new JComboBox<>(days());JComboBox<String> teacher=teacherBox(false);JTextField reason=new JTextField();styleField(reason);
        JButton mark=button("Mark absent & find cover",ORANGE);top.add(fieldLabel("DAY"));top.add(day);top.add(fieldLabel("ABSENT TEACHER"));top.add(teacher);top.add(fieldLabel("REASON (OPTIONAL)"));top.add(reason);top.add(mark);
        DefaultTableModel m=model("Period","Class","Subject","Original teacher","Available replacements (best match first)");JTable t=table(m);
        JPanel results=card();results.add(new JScrollPane(t));
        mark.addActionListener(e->{if(teacher.getSelectedItem()==null)return;String tid=teacher.getSelectedItem().toString().split(" — ")[0],d=day.getSelectedItem().toString();
            try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("INSERT INTO absences(teacher_id,day_name,reason,status) VALUES(?,?,?,'ABSENT') ON DUPLICATE KEY UPDATE reason=VALUES(reason),status='ABSENT'")){ps.setString(1,tid);ps.setString(2,d);ps.setString(3,reason.getText());ps.executeUpdate();m.setRowCount(0);
                try(PreparedStatement q=c.prepareStatement("SELECT id,period_no,class_name,subject,teacher_id FROM timetable_entries WHERE day_name=? AND teacher_id=? ORDER BY period_no")){q.setString(1,d);q.setString(2,tid);try(ResultSet r=q.executeQuery()){while(r.next()){int entry=r.getInt(1),period=r.getInt(2);String cls=r.getString(3),sub=r.getString(4),orig=r.getString(5);
                    List<String> options=suggestions(c,d,period,orig,sub);m.addRow(new Object[]{period,cls,sub,orig,String.join("  |  ",options)});}}}
                results.putClientProperty("absence.day",d);results.putClientProperty("absence.teacher",tid);results.putClientProperty("absence.reason",reason.getText());
                results.putClientProperty("absence.entries",affectedEntries(c,d,tid));status.setText("  Absence recorded. Select a row and use Assign selected replacement below.");
            }catch(Exception ex){error(ex);}
        });
        JButton assign=button("Assign selected replacement",TEAL),present=button("Mark teacher present / undo",BLUE);
        assign.addActionListener(e->{int row=t.getSelectedRow();if(row<0){info("Select an affected period first.");return;}
            try(Connection c=Database.getConnection()){String d=(String)results.getClientProperty("absence.day"),tid=(String)results.getClientProperty("absence.teacher");if(d==null){info("Mark an absence first.");return;}
                int period=Integer.parseInt(m.getValueAt(row,0).toString());String cls=m.getValueAt(row,1).toString(),sub=m.getValueAt(row,2).toString();
                int entry=-1;try(PreparedStatement q=c.prepareStatement("SELECT id FROM timetable_entries WHERE day_name=? AND period_no=? AND class_name=? AND teacher_id=?")){q.setString(1,d);q.setInt(2,period);q.setString(3,cls);q.setString(4,tid);try(ResultSet r=q.executeQuery()){if(r.next())entry=r.getInt(1);}}
                List<String> opts=suggestions(c,d,period,tid,sub);if(opts.isEmpty()){info("No available teacher for this period.");return;}
                String choice=(String)JOptionPane.showInputDialog(frame,"Choose a free teacher (subject match ranked first):","Assign substitute",JOptionPane.PLAIN_MESSAGE,null,opts.toArray(),opts.get(0));if(choice==null)return;
                String subId=choice.split(" — ")[0];
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO substitutions(timetable_entry_id,day_name,period_no,class_name,subject,original_teacher_id,substitute_teacher_id) VALUES(?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE substitute_teacher_id=VALUES(substitute_teacher_id)")){
                    ps.setInt(1,entry);ps.setString(2,d);ps.setInt(3,period);ps.setString(4,cls);ps.setString(5,sub);ps.setString(6,tid);ps.setString(7,subId);ps.executeUpdate();}
                info("Substitute "+subId+" assigned to "+cls+", Period "+period+".");refresh();
            }catch(Exception ex){error(ex);}
        });
        present.addActionListener(e->{String d=(String)results.getClientProperty("absence.day"),tid=(String)results.getClientProperty("absence.teacher");if(d==null){info("Mark an absence first.");return;}
            try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE absences SET status='PRESENT' WHERE teacher_id=? AND day_name=?")){ps.setString(1,tid);ps.setString(2,d);ps.executeUpdate();info("Absence marked as present. Existing cover records are retained for audit.");m.setRowCount(0);}catch(SQLException ex){error(ex);}});
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));actions.setOpaque(false);actions.add(assign);actions.add(present);
        JPanel center=new JPanel(new BorderLayout(12,12));center.setOpaque(false);center.add(top,BorderLayout.NORTH);center.add(results,BorderLayout.CENTER);center.add(actions,BorderLayout.SOUTH);p.add(center,BorderLayout.CENTER);return p;
    }

    private Object affectedEntries(Connection c,String d,String tid)throws SQLException{return null;}
    private List<String> suggestions(Connection c,String day,int period,String absentId,String subject)throws SQLException{
        List<String> out=new ArrayList<>();
        String sql="""
          SELECT u.id,u.full_name,u.subject FROM users u WHERE u.role='TEACHER' AND u.active=TRUE AND u.id<>?
          AND NOT EXISTS(SELECT 1 FROM timetable_entries te WHERE te.day_name=? AND te.period_no=? AND te.teacher_id=u.id)
          AND NOT EXISTS(SELECT 1 FROM substitutions s WHERE s.day_name=? AND s.period_no=? AND s.substitute_teacher_id=u.id)
          ORDER BY CASE WHEN LOWER(u.subject)=LOWER(?) THEN 0 WHEN LOWER(u.subject) LIKE CONCAT('%',LOWER(?),'%') OR LOWER(?) LIKE CONCAT('%',LOWER(u.subject),'%') THEN 1 ELSE 2 END,u.full_name
          """;
        try(PreparedStatement ps=c.prepareStatement(sql)){ps.setString(1,absentId);ps.setString(2,day);ps.setInt(3,period);ps.setString(4,day);ps.setInt(5,period);ps.setString(6,subject);ps.setString(7,subject);ps.setString(8,subject);try(ResultSet r=ps.executeQuery()){while(r.next())out.add(r.getString(1)+" — "+r.getString(2)+" ("+r.getString(3)+")");}}
        return out;
    }
    private JComboBox<String> teacherBox(boolean includeAll){JComboBox<String> b=new JComboBox<>();try(Connection c=Database.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,full_name FROM users WHERE role='TEACHER' AND active=TRUE ORDER BY full_name")){while(r.next())b.addItem(r.getString(1)+" — "+r.getString(2));}catch(SQLException e){error(e);}return b;}

    private JPanel eventsAndMerge() {
        JPanel p=page("Events & class merging","Coordinate special events and combine two classes for a shared session.");
        JPanel left=card();left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));left.add(new JLabel("  Schedule an event / attendance"));
        JTextField title=new JTextField(),venue=new JTextField(),total=new JTextField("60"),present=new JTextField("0"),notes=new JTextField();
        JComboBox<String> day=new JComboBox<>(days()),period=new JComboBox<>(periods());for(JComponent x:new JComponent[]{title,venue,total,present,notes})styleField(x);
        left.add(fieldLabel("EVENT TITLE"));left.add(title);left.add(fieldLabel("DAY & PERIOD"));left.add(day);left.add(period);
        left.add(fieldLabel("VENUE"));left.add(venue);left.add(fieldLabel("TOTAL STUDENTS"));left.add(total);left.add(fieldLabel("PRESENT STUDENTS"));left.add(present);left.add(fieldLabel("NOTES"));left.add(notes);
        JButton save=button("Save event & attendance",PURPLE);left.add(save);
        save.addActionListener(e->{try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("INSERT INTO college_events(title,day_name,period_no,class_name,venue,total_students,present_students,notes) VALUES(?,?,?,'CSE-A',?,?,?,?)")){
            ps.setString(1,title.getText());ps.setString(2,day.getSelectedItem().toString());ps.setInt(3,Integer.parseInt(period.getSelectedItem().toString()));ps.setString(4,venue.getText());ps.setInt(5,Integer.parseInt(total.getText()));ps.setInt(6,Integer.parseInt(present.getText()));ps.setString(7,notes.getText());
            if(title.getText().isBlank())throw new IllegalArgumentException("Enter an event title.");if(Integer.parseInt(present.getText())>Integer.parseInt(total.getText()))throw new IllegalArgumentException("Present count cannot exceed total students.");ps.executeUpdate();info("Event and attendance saved.");refresh();
        }catch(Exception ex){error(ex);}});
        JPanel right=card();right.setLayout(new BoxLayout(right,BoxLayout.Y_AXIS));right.add(new JLabel("  Merge two classes"));
        JTextField c1=new JTextField("CSE-A"),c2=new JTextField("CSE-B"),subject=new JTextField("Combined lecture"),room=new JTextField("Seminar Hall");
        JComboBox<String> md=new JComboBox<>(days()),mp=new JComboBox<>(periods()),mt=teacherBox(false);
        for(JComponent x:new JComponent[]{c1,c2,subject,room})styleField(x);
        right.add(fieldLabel("CLASS ONE"));right.add(c1);right.add(fieldLabel("CLASS TWO"));right.add(c2);
        right.add(fieldLabel("DAY / PERIOD"));right.add(md);right.add(mp);right.add(fieldLabel("COMMON TEACHER"));right.add(mt);
        right.add(fieldLabel("SUBJECT / SESSION"));right.add(subject);right.add(fieldLabel("ROOM"));right.add(room);
        JButton merge=button("Create class merge",TEAL);right.add(merge);
        merge.addActionListener(e->{try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("INSERT INTO merged_classes(class_one,class_two,day_name,period_no,subject,teacher_id,room) VALUES(?,?,?,?,?,?,?)")){
            String teacherId=mt.getSelectedItem().toString().split(" — ")[0];String d=md.getSelectedItem().toString();int per=Integer.parseInt(mp.getSelectedItem().toString());
            try(PreparedStatement chk=c.prepareStatement("SELECT COUNT(*) FROM timetable_entries WHERE day_name=? AND period_no=? AND teacher_id=?")){chk.setString(1,d);chk.setInt(2,per);chk.setString(3,teacherId);try(ResultSet r=chk.executeQuery()){r.next();if(r.getInt(1)>0)throw new IllegalArgumentException("Selected teacher already has a class in this period.");}}
            ps.setString(1,c1.getText());ps.setString(2,c2.getText());ps.setString(3,d);ps.setInt(4,per);ps.setString(5,subject.getText());ps.setString(6,teacherId);ps.setString(7,room.getText());ps.executeUpdate();info("Class merge saved.");refresh();
        }catch(Exception ex){error(ex);}});
        DefaultTableModel m=model("Event","Day","Period","Venue","Attendance");try(Connection c=Database.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT title,day_name,period_no,venue,CONCAT(present_students,' / ',total_students) FROM college_events ORDER BY id DESC")){while(r.next())m.addRow(new Object[]{r.getString(1),r.getString(2),r.getInt(3),r.getString(4),r.getString(5)});}catch(SQLException e){error(e);}
        JPanel bottom=card();bottom.add(new JLabel("  Saved events & attendance"),BorderLayout.NORTH);bottom.add(new JScrollPane(table(m)));
        JPanel top=new JPanel(new GridLayout(1,2,14,0));top.setOpaque(false);top.add(left);top.add(right);
        JPanel center=new JPanel(new BorderLayout(12,12));center.setOpaque(false);center.add(top,BorderLayout.NORTH);center.add(bottom,BorderLayout.CENTER);p.add(center,BorderLayout.CENTER);return p;
    }

    private JPanel mySchedule() {
        JPanel p=page("My timetable","Your personal teaching schedule for the working week.");
        DefaultTableModel m=model("Day","Period","Class","Subject","Room","Type");
        try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("SELECT day_name,period_no,class_name,subject,room,entry_type FROM timetable_entries WHERE teacher_id=? ORDER BY FIELD(day_name,'Monday','Tuesday','Wednesday','Thursday','Friday'),period_no")){ps.setString(1,userId);try(ResultSet r=ps.executeQuery()){while(r.next())m.addRow(new Object[]{r.getString(1),r.getInt(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});}}catch(SQLException e){error(e);}
        JPanel c=card();c.add(new JScrollPane(table(m)));p.add(c,BorderLayout.CENTER);return p;
    }
    private JPanel freePeriods() {
        JPanel p=page("My free periods","Select a day to see your available teaching periods.");
        JComboBox<String> day=new JComboBox<>(days());DefaultTableModel m=model("Period","Availability");JTable t=table(m);
        JButton find=button("Check availability",BLUE);find.addActionListener(e->{m.setRowCount(0);try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("SELECT period_no FROM timetable_entries WHERE day_name=? AND teacher_id=?")){ps.setString(1,day.getSelectedItem().toString());ps.setString(2,userId);Set<Integer> busy=new HashSet<>();try(ResultSet r=ps.executeQuery()){while(r.next())busy.add(r.getInt(1));}for(int i=1;i<=6;i++)m.addRow(new Object[]{i,busy.contains(i)?"Teaching":"FREE"});}catch(SQLException ex){error(ex);}});
        JPanel top=card();top.setLayout(new FlowLayout(FlowLayout.LEFT,12,8));top.add(fieldLabel("SELECT DAY"));top.add(day);top.add(find);JPanel c=card();c.add(new JScrollPane(t));p.add(top,BorderLayout.NORTH);p.add(c,BorderLayout.CENTER);return p;
    }
    private JPanel teacherAbsence() {
        JPanel p=page("Report an absence","Submit your absence for a selected day. The administrator can arrange cover.");
        JComboBox<String> day=new JComboBox<>(days());JTextField reason=new JTextField();styleField(reason);JButton submit=button("Submit absence",ORANGE);
        JPanel c=card();c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));c.add(fieldLabel("DAY"));c.add(day);c.add(fieldLabel("REASON"));c.add(reason);c.add(submit);
        submit.addActionListener(e->{try(Connection db=Database.getConnection();PreparedStatement ps=db.prepareStatement("INSERT INTO absences(teacher_id,day_name,reason,status) VALUES(?,?,?,'ABSENT') ON DUPLICATE KEY UPDATE reason=VALUES(reason),status='ABSENT'")){ps.setString(1,userId);ps.setString(2,day.getSelectedItem().toString());ps.setString(3,reason.getText());ps.executeUpdate();info("Your absence has been reported to the administrator.");}catch(SQLException ex){error(ex);}});
        p.add(c,BorderLayout.CENTER);return p;
    }
}
