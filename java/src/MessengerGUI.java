/* CS166 Winter 2016
 * Project Phase 3 with GUI
 * Christopher Kotyluk 861069101
 * Jesse Gomez 861056174
 */
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.SystemColor;
import java.awt.Font;

import javax.swing.UIManager;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JPasswordField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MessengerGUI {
    //Provided Database Functions
    //reference to physical database connection.
    private Connection _connection = null;

    //handling the keyboard inputs through a BufferedReader
    //this variable can be global for convenience.
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    //global variables to set the status of the menus
    static String authorisedUser;
    static int chat_id = -1;
    static boolean usermenu;
    /**
     * Creates a new instance of Messenger
     *
     * @param hostname the MySQL or PostgreSQL server hostname
     * @param database the name of the database
     * @param username the user name used to login to the database
     * @param password the user login password
     * @throws java.sql.SQLException when failed to make a connection.
     */
    public MessengerGUI(String dbname, String dbport, String user, String passwd) throws SQLException
    {
        System.out.println("Connecting to database...");
        try {
            //constructs the connection URL
            String url = "jdbc:postgresql://localhost:" + "61302" + "/" + "Phase3";
            System.out.println("Connection URL: " + url + "\n");

            //obtain a physical connection
            this._connection = DriverManager.getConnection(url, "ckoty001", passwd);
            System.out.println("Completed database startup procedures");
            initialize(); //Setup the GUI
        } catch(Exception e) {
            System.err.println("Error: Unable to Connect to Database: " + e.getMessage());
            System.out.println("Make sure you started postgres on this machine");
            System.exit(-1);
        }
    }

    /**
     * Method to execute an update SQL statement.  Update SQL instructions
     * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
     *
     * @param sql the input SQL string
     * @throws java.sql.SQLException when update failed
     */
    public void executeUpdate(String sql) throws SQLException 
    {
        //creates a statement object
        Statement stmt = this._connection.createStatement();

        //issues the update instruction
        stmt.executeUpdate(sql);

        //close the instruction
        stmt.close();
    }

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT). This
     * method issues the query to the DBMS and outputs the results to
     * standard out.
     *
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQueryAndPrintResult(String query) throws SQLException 
    {
        //creates a statement object
        Statement stmt = this._connection.createStatement();

        //issues the query instruction
        ResultSet rs = stmt.executeQuery(query);

        /**
         * Obtains the metadata object for the returned result set. The metadata
         * contains row and column info.
         */
        ResultSetMetaData rsmd = rs.getMetaData();
        int numCol = rsmd.getColumnCount();
        int rowCount = 0;

        //iterates through the result set and output them to standard out.
        boolean outputHeader = true;
        while(rs.next()) {
            if(outputHeader) {
                for(int i = 1; i <= numCol; i++) {
                    System.out.print(String.format("%1$-" + rsmd.getColumnDisplaySize(i) + "s", rsmd.getColumnName(i)));
                }
                System.out.println();

                //Output dotted line below
                int n = 0;
                for(int i = 1; i <= numCol; i++) {
                    n += rsmd.getColumnDisplaySize(i);
                }
                String dash = new String(new char[n]).replace("\0", "-");

                System.out.print(dash);
                System.out.println();
                outputHeader = false;
            }
            for(int i=1; i<=numCol; ++i) {
                System.out.print(String.format("%1$-" + rsmd.getColumnDisplaySize(i) + "s", rs.getString(i)));
            }
            System.out.println();
            ++rowCount;
        }
        stmt.close();
        return rowCount;
    }

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT). This
     * method issues the query to the DBMS and returns the results as
     * a list of records. Each record in turn is a list of attribute values
     *
     * @param query the input query string
     * @return the query result as a list of records
     * @throws java.sql.SQLException when failed to execute the query
     */
    public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException 
    { 
        //creates a statement object 
        Statement stmt = this._connection.createStatement(); 

        //issues the query instruction 
        ResultSet rs = stmt.executeQuery(query); 

        /* 
         * Obtains the metadata object for the returned result set. The metadata 
         * contains row and column info. 
         */ 
        ResultSetMetaData rsmd = rs.getMetaData(); 
        int numCol = rsmd.getColumnCount(); 
        int rowCount = 0; 

        //iterates through the result set and saves the data returned by the query. 
        boolean outputHeader = false;
        List<List<String>> result  = new ArrayList<List<String>>(); 
        while(rs.next()) {
            List<String> record = new ArrayList<String>(); 
            for(int i=1; i<=numCol; ++i) 
                record.add(rs.getString(i)); 
            result.add(record); 
        }
        stmt.close(); 
        return result; 
    }

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT). This
     * method issues the query to the DBMS and returns the number of results
     *
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQuery(String query) throws SQLException 
    {
        //creates a statement object
        Statement stmt = this._connection.createStatement();

        //issues the query instruction
        ResultSet rs = stmt.executeQuery(query);

        int rowCount = 0;

        //iterates through the result set and count nuber of results.
        if(rs.next())
            rowCount++;

        stmt.close();
        return rowCount;
    }

    /**
     * Method to fetch the last value from sequence. This
     * method issues the query to the DBMS and returns the current 
     * value of sequence used for autogenerated keys
     *
     * @param sequence name of the DB sequence
     * @return current value of a sequence
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int getCurrSeqVal(String sequence) throws SQLException 
    {
        Statement stmt = this._connection.createStatement();
        ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')", sequence));
        if(rs.next())
            return rs.getInt(1);
        return -1;
    }

    /**
     * Method to close the physical connection if it is open.
     */
    public void cleanup() {
        try {
            if(this._connection != null) {
                this._connection.close();
            }
        } catch(SQLException e) {
            //ignored.
        }
    }
    //End of provided datebase functions

    private JFrame frmCsDbMessengerGUI;
    private JTextField textField;
    private JPasswordField passwordField;
    private JTextField reg_login_field;
    private JPasswordField reg_pass_field;
    private JTextField reg_phonen_field;
    private CardLayout cardlayout = new CardLayout();

    private JScrollPane contactTablePane;
    private JTable contactTable;
    private Object[][] ContactTableData;
    private String[] ContactTableCols;

    private JScrollPane blockedTablePane;
    private JList blockedTable;
    private String[] blockedTableData;
    
    JPanel chatViewer;    
    private JScrollPane chatManagerPane;
    private static JTable chatManagerTable;
    private Object[][] chatManagerData;
    private String[] chatManagerCols;
    
    JPanel messageViewer;
    static JScrollPane messagePane;
    private static JTable messageTable;
    static String[] messageViewerCols;
    static Object[][] messageViewerData;
    
    JList msgMemberList;
    JScrollPane msgMemberPane;
    String[] msgMembersData;
    
    final static JLabel lblHelloUser = new JLabel("Hello, " + authorisedUser);
    final static JTextArea textArea_CurStatus = new JTextArea();
    
    static int MAX_login = 50;
    static int MAX_phone_num = 16;
    static int MAX_status = 140; 
    static int MAX_message = 300;

    public class MyTable extends DefaultTableModel
    {
        @Override
        public
        boolean isCellEditable(int row, int column)
        {
            return false;
        }
    };
    
    /**
     * Launch the application.
     */
    static boolean keepon = true;
    static MessengerGUI esql = null;
    
    public static void main(String[] args) {
        if(args.length != 3) {
             System.err.println(
                "Usage: " +
                "java [-classpath <classpath>] " +
                MessengerGUI.class.getName() +
                " <dbname> <port> <user>");
             return;
          }
        
        try {
            //use postgres JDBC driver.
            Class.forName("org.postgresql.Driver").newInstance();
            //instantiate the Messenger object and creates a physical
            //connection.
            String dbname = args[0];
            String dbport = args[1];
            String user = args[2];
            esql = new MessengerGUI(dbname, dbport, user, "");
            
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    try {
                        esql.frmCsDbMessengerGUI.setVisible(true);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            while(keepon);
            
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }       
    }
    
    public void killDB()
    {
        try {
            if(esql != null) {
                System.out.println("Disconnecting from database...");
                esql.cleanup();
                System.out.println("Completed database shutdown procedures");
            }
        } catch(Exception e) {
            //ignored.
        }
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() throws SQLException {
        frmCsDbMessengerGUI = new JFrame();
        frmCsDbMessengerGUI.setTitle("CS166 DB MessengerGUI");
        frmCsDbMessengerGUI.setBounds(100, 100, 670, 393);
        frmCsDbMessengerGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmCsDbMessengerGUI.getContentPane().setLayout(new CardLayout(0, 0));
        
        final JPanel Cards = new JPanel();
        frmCsDbMessengerGUI.getContentPane().add(Cards, "name_16229416674857");
        Cards.setLayout(cardlayout);
        
        JPanel LoginMenu = new JPanel();
        Cards.add(LoginMenu, "card_LoginMenu");
        
        JPanel RegisterMenu = new JPanel();
        Cards.add(RegisterMenu, "card_RegisterMenu");
        
        chatViewer = new JPanel();
        Cards.add(chatViewer, "card_ChatViewer");

        JButton btnRegister = new JButton("Register");
        btnRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {
                cardlayout.show(Cards, "card_RegisterMenu");
            }
        });
        btnRegister.setBackground(SystemColor.menu);
        
        JButton btnLogin = new JButton("Login");
        btnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {
                
                String user = textField.getText().toString();
                if(user.length() > MAX_login) {
                    JOptionPane.showMessageDialog(null, "Error: Login is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String pass = String.valueOf(passwordField.getPassword());
                if(pass.length() > MAX_login) {
                    JOptionPane.showMessageDialog(null, "Error: Password is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                authorisedUser = LogIn(esql, user, pass);
                if(authorisedUser != null)
                {   
                    personalizeMainMenu(esql, authorisedUser);
                    cardlayout.show(Cards, "card_MainMenu");
                } 
                else {
                    JOptionPane.showMessageDialog(null, "Invalid user/password. Try again.", "Login Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnLogin.setBackground(SystemColor.menu);
        
        JButton btnExit = new JButton("Exit");
        btnExit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                keepon = false;
                killDB();
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });
        btnExit.setBackground(SystemColor.menu);
        
        JLabel login_label = new JLabel("Login:");
        JLabel pass_label = new JLabel("Password:");
        
        textField = new JTextField();
        textField.setColumns(10);
        
        passwordField = new JPasswordField();
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String user = textField.getText().toString();
                    if(user.length() > MAX_login) {
                        JOptionPane.showMessageDialog(null, "Error: Login is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String pass = String.valueOf(passwordField.getPassword());
                    if(pass.length() > MAX_login) {
                        JOptionPane.showMessageDialog(null, "Error: Password is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    authorisedUser = LogIn(esql, user, pass);
                    if(authorisedUser != null)
                    {   
                        personalizeMainMenu(esql, authorisedUser);
                        cardlayout.show(Cards, "card_MainMenu");
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Invalid user/password. Try again.", "Login Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        GroupLayout gl_LoginMenu = new GroupLayout(LoginMenu);
        gl_LoginMenu.setHorizontalGroup(
            gl_LoginMenu.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_LoginMenu.createSequentialGroup()
                    .addGap(90)
                    .addGroup(gl_LoginMenu.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_LoginMenu.createSequentialGroup()
                            .addComponent(btnRegister, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                            .addGap(20)
                            .addComponent(btnLogin, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                            .addGap(18)
                            .addComponent(btnExit, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE))
                        .addGroup(gl_LoginMenu.createSequentialGroup()
                            .addGroup(gl_LoginMenu.createParallelGroup(Alignment.LEADING)
                                .addComponent(pass_label, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                                .addComponent(login_label))
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addGroup(gl_LoginMenu.createParallelGroup(Alignment.LEADING)
                                .addComponent(textField, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
                                .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))))
                    .addContainerGap(108, Short.MAX_VALUE))
        );
        gl_LoginMenu.setVerticalGroup(
            gl_LoginMenu.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_LoginMenu.createSequentialGroup()
                    .addGap(80)
                    .addGroup(gl_LoginMenu.createParallelGroup(Alignment.BASELINE)
                        .addComponent(login_label)
                        .addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGap(9)
                    .addGroup(gl_LoginMenu.createParallelGroup(Alignment.LEADING)
                        .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGroup(gl_LoginMenu.createSequentialGroup()
                            .addGap(3)
                            .addComponent(pass_label)))
                    .addGap(6)
                    .addGroup(gl_LoginMenu.createParallelGroup(Alignment.BASELINE)
                        .addComponent(btnRegister)
                        .addComponent(btnExit)
                        .addComponent(btnLogin))
                    .addContainerGap(111, Short.MAX_VALUE))
        );
        LoginMenu.setLayout(gl_LoginMenu);
        
        //-----RegisterMenu-----      
        JButton reg_reg_but = new JButton("Register");
        reg_reg_but.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                String user = reg_login_field.getText().toString();
                if(user.length() > MAX_login) {
                    JOptionPane.showMessageDialog(null, "Error: Login is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String pass = String.valueOf(reg_pass_field.getPassword());
                if(pass.length() > MAX_login) {
                    JOptionPane.showMessageDialog(null, "Error: Password is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String num = reg_phonen_field.getText().toString();
                if(num.length() > MAX_phone_num) {
                    JOptionPane.showMessageDialog(null, "Error: Phone number is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                CreateUser(esql, user, pass, num);
                
                textField.setText("");
                passwordField.setText("");
                
                cardlayout.show(Cards, "card_LoginMenu");

                reg_login_field.setText("");
                reg_pass_field.setText("");
                reg_phonen_field.setText("");
            }
        });
        reg_reg_but.setBackground(SystemColor.menu);
        
        JButton reg_cancel_but = new JButton("Cancel");
        reg_cancel_but.setBackground(SystemColor.menu);
        reg_cancel_but.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {
                cardlayout.show(Cards, "card_LoginMenu");
            }
        });
        
        JLabel reg_login_label = new JLabel("Login:");
        
        reg_login_field = new JTextField();
        reg_login_field.setColumns(10);
        
        JLabel reg_pass_label = new JLabel("Password:");
        
        reg_pass_field = new JPasswordField();
        
        JLabel reg_phonen_label = new JLabel("Phone number:");
        
        reg_phonen_field = new JTextField();
        reg_phonen_field.setColumns(10);
        GroupLayout gl_RegisterMenu = new GroupLayout(RegisterMenu);
        gl_RegisterMenu.setHorizontalGroup(
            gl_RegisterMenu.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_RegisterMenu.createSequentialGroup()
                    .addGap(90)
                        //.addContainerGap(106, Short.MAX_VALUE)
                    .addGroup(gl_RegisterMenu.createParallelGroup(Alignment.LEADING, false)
                        .addGroup(gl_RegisterMenu.createSequentialGroup()
                            .addComponent(reg_reg_but, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(reg_cancel_but, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
                        .addGroup(gl_RegisterMenu.createSequentialGroup()
                            .addGroup(gl_RegisterMenu.createParallelGroup(Alignment.LEADING)
                                .addComponent(reg_phonen_label, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                .addComponent(reg_pass_label, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
                                .addComponent(reg_login_label))
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addGroup(gl_RegisterMenu.createParallelGroup(Alignment.LEADING)
                                .addComponent(reg_login_field, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
                                .addComponent(reg_pass_field, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
                                .addComponent(reg_phonen_field, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))))
                    .addGap(60))
        );
        gl_RegisterMenu.setVerticalGroup(
            gl_RegisterMenu.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_RegisterMenu.createSequentialGroup()
                    .addGap(80)
                        //.addContainerGap(80, Short.MAX_VALUE)
                    .addGroup(gl_RegisterMenu.createParallelGroup(Alignment.BASELINE)
                        .addComponent(reg_login_field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(reg_login_label))
                    .addGap(10)
                    .addGroup(gl_RegisterMenu.createParallelGroup(Alignment.BASELINE)
                        .addComponent(reg_pass_label)
                        .addComponent(reg_pass_field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGap(9)
                    .addGroup(gl_RegisterMenu.createParallelGroup(Alignment.BASELINE)
                        .addComponent(reg_phonen_label)
                        .addComponent(reg_phonen_field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(gl_RegisterMenu.createParallelGroup(Alignment.BASELINE)
                        .addComponent(reg_reg_but)
                        .addComponent(reg_cancel_but))
                    .addGap(68))
        );
        RegisterMenu.setLayout(gl_RegisterMenu);
        
        JPanel MainMenu = new JPanel();
        Cards.add(MainMenu, "card_MainMenu");
        
        JButton btnListManager = new JButton("List Manager");
        btnListManager.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                updateContactTable(authorisedUser);
                updateBlockedTable(authorisedUser);
                
                cardlayout.show(Cards, "card_BrowseCtcsBlcks");
            }
        });
        btnListManager.setBackground(SystemColor.menu);
        
        JButton btnChatManager = new JButton("Chat Manager");
        btnChatManager.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                updateChatTable(authorisedUser);
                cardlayout.show(Cards, "card_ChatViewer");
            }
        });
        btnChatManager.setBackground(SystemColor.menu);

        lblHelloUser.setFont(new Font("Tahoma", Font.PLAIN, 15));
        
        JButton btnLogOut = new JButton("Log out");
        btnLogOut.setBackground(SystemColor.menu);
        btnLogOut.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent arg0) {
                textField.setText("");
                passwordField.setText("");
                System.out.println(authorisedUser + " logged out!\n");
                cardlayout.show(Cards, "card_LoginMenu");
            }
        });
        
        JButton btnDeleteAccount = new JButton("Delete Account");
        btnDeleteAccount.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DeleteAccount(esql, authorisedUser);
                textField.setText("");
                passwordField.setText("");
                cardlayout.show(Cards, "card_LoginMenu"); //Go back to main menu
            }
        });
        btnDeleteAccount.setBackground(new Color(255, 52, 52));
        
        JButton btnUpdateStatus = new JButton("Update Status");
        btnUpdateStatus.setBackground(SystemColor.menu);
        btnUpdateStatus.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                updateStatus(esql, authorisedUser);
            }
        });
        textArea_CurStatus.setLineWrap(true);
        textArea_CurStatus.setRows(4);
        textArea_CurStatus.setEditable(false);
        textArea_CurStatus.setBackground(UIManager.getColor("Panel.background"));
        textArea_CurStatus.setWrapStyleWord(true);
        
        textArea_CurStatus.setFont(new Font("Tahoma", Font.PLAIN, 13));
        
        JLabel lblCurrentStatus_1 = new JLabel("Current Status:");
        lblCurrentStatus_1.setFont(new Font("Tahoma", Font.PLAIN, 13));
        GroupLayout gl_MainMenu = new GroupLayout(MainMenu);
        gl_MainMenu.setHorizontalGroup(
            gl_MainMenu.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_MainMenu.createSequentialGroup()
                    .addGap(32)
                    .addGroup(gl_MainMenu.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_MainMenu.createSequentialGroup()
                            .addComponent(btnChatManager)
                            .addContainerGap())
                        .addGroup(gl_MainMenu.createSequentialGroup()
                            .addGroup(gl_MainMenu.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_MainMenu.createSequentialGroup()
                                    .addComponent(btnDeleteAccount)
                                    .addGap(218)
                                    .addComponent(btnLogOut))
                                .addComponent(lblHelloUser)
                                .addGroup(gl_MainMenu.createSequentialGroup()
                                    .addGroup(gl_MainMenu.createParallelGroup(Alignment.LEADING)
                                        .addGroup(gl_MainMenu.createSequentialGroup()
                                            .addComponent(btnUpdateStatus)
                                            .addPreferredGap(ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                                            .addComponent(lblCurrentStatus_1))
                                        .addComponent(btnListManager))
                                    .addPreferredGap(ComponentPlacement.RELATED)
                                    .addComponent(textArea_CurStatus, GroupLayout.PREFERRED_SIZE, 326, GroupLayout.PREFERRED_SIZE)))
                            .addContainerGap(89, Short.MAX_VALUE))))
        );
        gl_MainMenu.setVerticalGroup(
            gl_MainMenu.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_MainMenu.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_MainMenu.createParallelGroup(Alignment.TRAILING)
                        .addGroup(gl_MainMenu.createSequentialGroup()
                            .addComponent(lblHelloUser)
                            .addGap(99))
                        .addGroup(gl_MainMenu.createParallelGroup(Alignment.BASELINE)
                            .addComponent(textArea_CurStatus, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCurrentStatus_1)
                            .addGroup(gl_MainMenu.createSequentialGroup()
                                .addComponent(btnUpdateStatus)
                                .addGap(8)
                                .addComponent(btnListManager)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(btnChatManager))))
                    .addGap(136)
                    .addGroup(gl_MainMenu.createParallelGroup(Alignment.BASELINE)
                        .addComponent(btnLogOut)
                        .addComponent(btnDeleteAccount))
                    .addContainerGap(67, Short.MAX_VALUE))
        );
        MainMenu.setLayout(gl_MainMenu);
        
        JPanel BrowseCtcsBlcks = new JPanel();
        Cards.add(BrowseCtcsBlcks, "card_BrowseCtcsBlcks");
        
        contactTablePane = new JScrollPane();
        
        blockedTablePane = new JScrollPane();

        chatManagerPane = new JScrollPane();
        
        JLabel lblContactsList = new JLabel("Contacts List");
        lblContactsList.setFont(new Font("Tahoma", Font.PLAIN, 13));
        
        JLabel lblBlockedList = new JLabel("Blocked List");
        lblBlockedList.setFont(new Font("Tahoma", Font.PLAIN, 13));
        
        JButton btnAddAContact = new JButton("Add a contact");
        btnAddAContact.setBackground(SystemColor.menu);
        btnAddAContact.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent arg0) {
                System.out.println("Starting New Contact dialog");

                AddToList(esql, authorisedUser, "contacts");
                updateContactTable(authorisedUser);
            }
        });
        
        JButton btnRemoveAContact = new JButton("Remove a contact");
        btnRemoveAContact.setBackground(SystemColor.menu);
        btnRemoveAContact.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent arg0) {
                System.out.println("Starting New Contact dialog");

                RemoveFromList(esql, authorisedUser, "contacts");
                updateContactTable(authorisedUser);
            }
        });
        
        JButton btnBlockAUser = new JButton("Block a user");
        btnBlockAUser.setBackground(SystemColor.menu);
        btnBlockAUser.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent arg0) {
                System.out.println("Starting Block User dialog");

                AddToList(esql, authorisedUser, "blocked");
                updateBlockedTable(authorisedUser);
            }
        });
        
        JButton btnUnblockAUser = new JButton("Unblock a user");
        btnUnblockAUser.setBackground(SystemColor.menu);
        btnUnblockAUser.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent arg0) {
                System.out.println("Starting New Contact dialog");

                RemoveFromList(esql, authorisedUser, "blocked");
                updateBlockedTable(authorisedUser);
            }
        });
        
        JButton btnGoBack = new JButton("Go back");
        btnGoBack.setBackground(SystemColor.menu);
        btnGoBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                cardlayout.show(Cards, "card_MainMenu");
            }
        });
        
        
        JButton btnRefresh_3 = new JButton("Refresh");
        btnRefresh_3.setBackground(SystemColor.menu);
        btnRefresh_3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                updateContactTable(authorisedUser);
                updateBlockedTable(authorisedUser);
            }
        });
        
        GroupLayout gl_BrowseCtcsBlcks = new GroupLayout(BrowseCtcsBlcks);
        gl_BrowseCtcsBlcks.setHorizontalGroup(
            gl_BrowseCtcsBlcks.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_BrowseCtcsBlcks.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_BrowseCtcsBlcks.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblContactsList)
                        .addComponent(contactTablePane, GroupLayout.PREFERRED_SIZE, 299, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(gl_BrowseCtcsBlcks.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_BrowseCtcsBlcks.createSequentialGroup()
                            .addComponent(blockedTablePane, GroupLayout.PREFERRED_SIZE, 164, GroupLayout.PREFERRED_SIZE)
                            .addGroup(gl_BrowseCtcsBlcks.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_BrowseCtcsBlcks.createSequentialGroup()
                                    .addGap(18)
                                    .addGroup(gl_BrowseCtcsBlcks.createParallelGroup(Alignment.LEADING)
                                        .addComponent(btnGoBack)
                                        .addComponent(btnUnblockAUser)
                                        .addComponent(btnBlockAUser)
                                        .addComponent(btnRemoveAContact)
                                        .addComponent(btnAddAContact)))
                                .addGroup(gl_BrowseCtcsBlcks.createSequentialGroup()
                                    .addGap(18)
                                    .addComponent(btnRefresh_3))))
                        .addComponent(lblBlockedList))
                    .addGap(11))
        );
        gl_BrowseCtcsBlcks.setVerticalGroup(
            gl_BrowseCtcsBlcks.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_BrowseCtcsBlcks.createSequentialGroup()
                    .addGap(7)
                    .addGroup(gl_BrowseCtcsBlcks.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblContactsList)
                        .addComponent(lblBlockedList))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(gl_BrowseCtcsBlcks.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_BrowseCtcsBlcks.createSequentialGroup()
                            .addComponent(btnAddAContact)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnRemoveAContact)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnBlockAUser)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnUnblockAUser)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnRefresh_3)
                            .addPreferredGap(ComponentPlacement.RELATED, 153, Short.MAX_VALUE)
                            .addComponent(btnGoBack))
                        .addComponent(contactTablePane, GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                        .addComponent(blockedTablePane, GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
                    .addContainerGap())
        );
        
        ContactTableCols = new String[]{"User", "Status"};
        ContactTableData = new Object[][]{};
        
        contactTable = new JTable(ContactTableData, ContactTableCols) {
            public boolean isCellEditable(int row, int column) {                
                return false;               
            };
        };
        
        contactTablePane.setViewportView(contactTable);
        
        blockedTableData = new String[]{};
        blockedTable = new JList<String>(blockedTableData);
        blockedTablePane.setViewportView(blockedTable);
        
        BrowseCtcsBlcks.setLayout(gl_BrowseCtcsBlcks);
        
        JButton btnStartChat = new JButton("Start chat");
        btnStartChat.setBackground(SystemColor.menu);
        btnStartChat.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent arg0) {
                System.out.println("Starting a new chat");
                StartChat(esql, authorisedUser);
                updateChatTable(authorisedUser);
            }
        });
        
        JButton btnDeleteChat = new JButton("Delete chat");
        btnDeleteChat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DeleteChat(esql, authorisedUser);
                updateChatTable(authorisedUser);
            }
        });
        btnDeleteChat.setBackground(SystemColor.menu);
        
        JButton btnViewChat = new JButton("View chat");
        btnViewChat.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent arg0) {
                if(updateMessageTable(authorisedUser))
                {
                    cardlayout.show(Cards, "card_MessageViewer");
                }

            }
        });
        btnViewChat.setBackground(SystemColor.menu);
        
        JButton btnGoBack_1 = new JButton("Go back");
        btnGoBack_1.setBackground(SystemColor.menu);
        btnGoBack_1.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent arg0) {
                chat_id = -1;
                cardlayout.show(Cards, "card_MainMenu");
            }
        });
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(SystemColor.menu);
        btnRefresh.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                updateChatTable(authorisedUser);
            }
        });
        
        JLabel lblChatViewer = new JLabel("Chat Viewer");
        lblChatViewer.setFont(new Font("Tahoma", Font.PLAIN, 15));
        GroupLayout gl_ChatViewer = new GroupLayout(chatViewer);
        gl_ChatViewer.setHorizontalGroup(
            gl_ChatViewer.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_ChatViewer.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_ChatViewer.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_ChatViewer.createSequentialGroup()
                            .addComponent(chatManagerPane, GroupLayout.PREFERRED_SIZE, 439, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addGroup(gl_ChatViewer.createParallelGroup(Alignment.LEADING)
                                .addComponent(btnStartChat)
                                .addComponent(btnViewChat)
                                .addComponent(btnDeleteChat)
                                .addComponent(btnGoBack_1)
                                .addComponent(btnRefresh)))
                        .addComponent(lblChatViewer))
                    .addContainerGap(108, Short.MAX_VALUE))
        );
        gl_ChatViewer.setVerticalGroup(
            gl_ChatViewer.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_ChatViewer.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblChatViewer)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(gl_ChatViewer.createParallelGroup(Alignment.LEADING, false)
                        .addGroup(gl_ChatViewer.createSequentialGroup()
                            .addComponent(btnViewChat)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnStartChat)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnDeleteChat)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnRefresh)
                            .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnGoBack_1))
                        .addComponent(chatManagerPane, GroupLayout.PREFERRED_SIZE, 296, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(23, Short.MAX_VALUE))
        );
        
        chatManagerCols = new String[]{"ID", "Type", "Timestamp", "Initial Sender"};
        chatManagerData = new Object[][]{};
        chatManagerTable = new JTable(chatManagerData, chatManagerCols) {
            public boolean isCellEditable(int row, int column) {                
                return false;               
            };
        };
        
        chatManagerTable.setAutoResizeMode(chatManagerTable.AUTO_RESIZE_OFF);
        chatManagerPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        chatManagerPane.setViewportView(chatManagerTable);
        
        chatViewer.setLayout(gl_ChatViewer);

        messageViewer = new JPanel();
        Cards.add(messageViewer, "card_MessageViewer");
        
        messagePane = new JScrollPane();
        
        JButton btnEditMessage = new JButton("Edit message");
        btnEditMessage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("Attempting to edit a message");
                EditMessage(esql, authorisedUser);
                updateMessageTable(authorisedUser);
            }
        });
        btnEditMessage.setBackground(SystemColor.menu);
        
        JButton btnReply = new JButton("Reply");
        btnReply.setBackground(SystemColor.menu);
        btnReply.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent arg0) {
                SendNewMessage(esql, authorisedUser);
            }
        });
        
        JButton btnDeleteMessage = new JButton("Delete message");
        btnDeleteMessage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("Attempting to delete a message");
                DeleteMessage(esql, authorisedUser);
                updateMessageTable(authorisedUser);
            }
        });
        btnDeleteMessage.setBackground(SystemColor.menu);
        
        JButton btnViewMembers = new JButton("View members");
        btnViewMembers.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent arg0) {
                updateMemberTable(authorisedUser);
                cardlayout.show(Cards, "card_ViewMembers");
            }
        });
        btnViewMembers.setBackground(SystemColor.menu);
        
        JButton btnGoBack_2 = new JButton("Go back");
        btnGoBack_2.setBackground(SystemColor.menu);
        btnGoBack_2.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent arg0) {
                updateChatTable(authorisedUser);
                cardlayout.show(Cards, "card_ChatViewer");
            }
        });
        
        JButton btnRefresh_1 = new JButton("Refresh");
        btnRefresh_1.setBackground(SystemColor.menu);
        btnRefresh_1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                updateMessageTable(authorisedUser);
            }
        });
        
        JLabel lblMessageViewer = new JLabel("Message Viewer");
        lblMessageViewer.setFont(new Font("Tahoma", Font.PLAIN, 15));
        GroupLayout gl_MessageViewer = new GroupLayout(messageViewer);
        gl_MessageViewer.setHorizontalGroup(
            gl_MessageViewer.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_MessageViewer.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_MessageViewer.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_MessageViewer.createSequentialGroup()
                            .addComponent(messagePane, GroupLayout.PREFERRED_SIZE, 439, GroupLayout.PREFERRED_SIZE)
                            .addGap(10)
                            .addGroup(gl_MessageViewer.createParallelGroup(Alignment.LEADING)
                                .addComponent(btnReply, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnEditMessage)
                                .addComponent(btnDeleteMessage)
                                .addComponent(btnViewMembers)
                                .addComponent(btnGoBack_2)
                                .addComponent(btnRefresh_1)))
                        .addComponent(lblMessageViewer))
                    .addContainerGap(86, Short.MAX_VALUE))
        );
        gl_MessageViewer.setVerticalGroup(
            gl_MessageViewer.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_MessageViewer.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblMessageViewer)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(gl_MessageViewer.createParallelGroup(Alignment.LEADING, false)
                        .addComponent(messagePane, GroupLayout.PREFERRED_SIZE, 296, GroupLayout.PREFERRED_SIZE)
                        .addGroup(gl_MessageViewer.createSequentialGroup()
                            .addComponent(btnReply)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnEditMessage)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnDeleteMessage)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnViewMembers)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnRefresh_1)
                            .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnGoBack_2)))
                    .addContainerGap(23, Short.MAX_VALUE))
        );
        
        messageViewerCols = new String[]{"ID", "Timestamp", "Sender", "Message"};
        messageViewerData = new Object[][]{};
        
        messageTable = new JTable(messageViewerData, messageViewerCols) {
            public boolean isCellEditable(int row, int column) {                
                return false;               
            };
        };

        messageTable.setAutoResizeMode(messageTable.AUTO_RESIZE_OFF);
        messagePane.setViewportView(messageTable);
        messagePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        messageViewer.setLayout(gl_MessageViewer);
        
        JPanel ViewMembers = new JPanel();
        Cards.add(ViewMembers, "card_ViewMembers");
        
        msgMemberPane = new JScrollPane();
        
        JLabel lblChatMembers = new JLabel("Chat Members");
        lblChatMembers.setFont(new Font("Tahoma", Font.PLAIN, 13));
        
        JButton btnGoBack_3 = new JButton("Go back");
        btnGoBack_3.setBackground(SystemColor.menu);
        btnGoBack_3.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent arg0) {
                updateMessageTable(authorisedUser);
                cardlayout.show(Cards, "card_MessageViewer");
            }
        });
        
        JButton btnAddMember = new JButton("Add member(s)");
        btnAddMember.setBackground(SystemColor.menu);
        btnAddMember.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                AddMembers(esql, authorisedUser);
                updateMemberTable(authorisedUser);
            }
        });
        
        JButton btnRemoveMember = new JButton("Remove member(s)");
        btnRemoveMember.setBackground(SystemColor.menu);
        btnRemoveMember.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DeleteMembers(esql, authorisedUser);
                updateMemberTable(authorisedUser);
            }
        });
        
        JButton btnRefresh_2 = new JButton("Refresh");
        btnRefresh_2.setBackground(SystemColor.menu);
        btnRefresh_2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {
                updateMemberTable(authorisedUser);
            }
        });
        
        GroupLayout gl_ViewMembers = new GroupLayout(ViewMembers);
        gl_ViewMembers.setHorizontalGroup(
            gl_ViewMembers.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_ViewMembers.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_ViewMembers.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_ViewMembers.createSequentialGroup()
                            .addComponent(msgMemberPane, GroupLayout.PREFERRED_SIZE, 167, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addGroup(gl_ViewMembers.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_ViewMembers.createParallelGroup(Alignment.TRAILING)
                                    .addGroup(gl_ViewMembers.createSequentialGroup()
                                        .addGroup(gl_ViewMembers.createParallelGroup(Alignment.LEADING)
                                            .addComponent(btnRemoveMember)
                                            .addComponent(btnAddMember))
                                        .addGap(378))
                                    .addGroup(gl_ViewMembers.createSequentialGroup()
                                        .addComponent(btnGoBack_3)
                                        .addGap(69)))
                                .addComponent(btnRefresh_2)))
                        .addComponent(lblChatMembers))
                    .addContainerGap(37, Short.MAX_VALUE))
        );
        gl_ViewMembers.setVerticalGroup(
            gl_ViewMembers.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_ViewMembers.createSequentialGroup()
                    .addGap(5)
                    .addComponent(lblChatMembers)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(gl_ViewMembers.createParallelGroup(Alignment.BASELINE)
                        .addComponent(msgMemberPane, GroupLayout.PREFERRED_SIZE, 319, GroupLayout.PREFERRED_SIZE)
                        .addGroup(gl_ViewMembers.createSequentialGroup()
                            .addComponent(btnAddMember)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnRemoveMember)
                            .addGap(5)
                            .addComponent(btnRefresh_2)
                            .addGap(196)
                            .addComponent(btnGoBack_3)))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        msgMembersData = new String[]{};
        msgMemberList = new JList<String>(msgMembersData);
        msgMemberPane.setViewportView(msgMemberList);
        ViewMembers.setLayout(gl_ViewMembers);
    }
    
    //dynamically resizes columns based on title width or data width, whichever is larger
    public static void resizeColumns(JTable table, String type) {
        int currWidth = 0, width = 30;
        final TableColumnModel columnModel = table.getColumnModel();
        for(int col = 0; col < table.getColumnCount(); col++) {
            width = 30;
            if(col + 1 >= table.getColumnCount())
                if(type.equals("contacts"))
                    width = 296 - currWidth;
                else if(type.equals("chats") || type.equals("msgs"))
                    width = 436 - currWidth;

            TableCellRenderer rendererHeader = columnModel.getColumn(col).getHeaderRenderer();
            if(rendererHeader == null)
                rendererHeader = table.getTableHeader().getDefaultRenderer();
            
            Component compHeader = rendererHeader.getTableCellRendererComponent(table, columnModel.getColumn(col).getHeaderValue(), false, false, 0, 0);
            if(table.getRowCount() == 0)
                width = Math.max(width, compHeader.getPreferredSize().width + 4);
            else
                for(int row = 0; row < table.getRowCount(); row++) {
                    TableCellRenderer renderer = table.getCellRenderer(row, col);
                    Component comp = table.prepareRenderer(renderer, row, col);
                    width = Math.max(width, Math.max(compHeader.getPreferredSize().width + 4, comp.getPreferredSize().width + 4));
                }
            
            columnModel.getColumn(col).setPreferredWidth(width);
            currWidth += width;
        }
    }
    
    //--------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------
    /**
     * Reads the users choice given from the keyboard
     * @int
     */
    public static int readChoice() 
    {
        int input;
        //returns only if a correct value is given.
        do {
            System.out.print("Please make your choice: ");
            try {//read the integer, parse it and break.
                input = Integer.parseInt(in.readLine());
                break;
            } catch(Exception e) {
                System.out.println("Your input is invalid!");
                continue;
            }
        } while(true);
        return input;
    }

    /**
     * Creates a new user with provided login, passowrd and phoneNum
     * An empty block and contact list would be generated and associated with a user
     */
    public static void CreateUser(MessengerGUI esql, String user, String pass, String number)
    {
        if(user==null || user.equals("") || pass==null || pass.equals("") || number==null || number.equals(""))
        {
            JOptionPane.showMessageDialog(null, "Error: Invalid inputs! Try again.", "Register Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String query = String.format("SELECT * FROM USR U WHERE U.login='%s'",user);
            int loginExists = esql.executeQuery(query);
            if(loginExists > 0) {
                System.out.println("User already exists!");
                JOptionPane.showMessageDialog(null, "Error: user already exists! Try again.", "Register Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            else {
                query = String.format("SELECT * FROM USR U WHERE phoneNum='%s'", number);
                int phoneNumExists = esql.executeQuery(query);
                if(phoneNumExists > 0) {
                    JOptionPane.showMessageDialog(null, "Error: phone number is already used! Try again.", "Register Error", JOptionPane.ERROR_MESSAGE);
                } else{
                    //Creating empty contact\block lists for a user
                    esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES('block')");
                    int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
                    esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES('contact')");
                    int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");
                           
                    query = String.format("INSERT INTO USR(phoneNum, login, password, block_list, contact_list) VALUES('%s','%s','%s',%s,%s)", number, user, pass, block_id, contact_id);
    
                    esql.executeUpdate(query);
                    JOptionPane.showMessageDialog(null, "Registered user: " + user, "Registration", JOptionPane.INFORMATION_MESSAGE);
                    System.out.println("User successfully created!");
                }
            }
        } catch(Exception e) {
             System.err.println(e.getMessage());
        }
    }

    /**
     * Check log in credentials for an existing user
     * @return User login or null is the user does not exist
     */
    public static String LogIn(MessengerGUI esql, String user, String pass)
    {
        try {
            if((user != null && !user.equals("")) && (pass != null && !pass.equals("")))
            {
                String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", user, pass);
                int userNum = esql.executeQuery(query);
                if(userNum > 0) {
                    System.out.println("\nLogin successful!");
                    return user;
                } else {
                    System.out.println("Login failed! Login/password is invalid.\n");
                    return null;
                }
            }
            else
            {
                System.out.println("Login failed! User/password is invalid.\n");
                return null;
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static void AddToList(MessengerGUI esql, String user, String choice)
    {
        try {
            String query;
            List<List<String>> list;
            int list_id;
            int list_id_1;
            String login = null;

            if(choice.equalsIgnoreCase("contacts"))
            {
                //Get id of contact list
                query = String.format("SELECT U.contact_list FROM USR U WHERE U.login='%s'", user);
                list = esql.executeQueryAndReturnResult(query);
                list_id = Integer.parseInt(list.get(0).get(0));
                
                //Get id of block list
                query = String.format("SELECT U.block_list FROM USR U WHERE U.login='%s'", user);
                list = esql.executeQueryAndReturnResult(query);
                list_id_1 = Integer.parseInt(list.get(0).get(0)); 
                
                
                login = JOptionPane.showInputDialog(null, "Enter login of user to add.", "AddToList Contacts",JOptionPane.QUESTION_MESSAGE); 
            }
            else if(choice.equalsIgnoreCase("blocked"))
            {
                //Gets id of block list
                query = String.format("SELECT U.block_list FROM USR U WHERE U.login='%s'", user);
                list = esql.executeQueryAndReturnResult(query);
                
                //Get id of contact list
                list_id = Integer.parseInt(list.get(0).get(0)); 
                query = String.format("SELECT U.contact_list FROM USR U WHERE U.login='%s'", user);
                list = esql.executeQueryAndReturnResult(query);
                list_id_1 = Integer.parseInt(list.get(0).get(0));
                
                login = JOptionPane.showInputDialog(null, "Enter login of user to block.", "AddToList Blocked",JOptionPane.QUESTION_MESSAGE);   
            }
            else
            {
                System.out.println("Unrecognized choice!");
                return;
            }

            if(login != null) {
                if(login.length() > MAX_login) {
                    JOptionPane.showMessageDialog(null, "Error: Login is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                System.out.println("Attempting to add " + login + " to " + choice + " list");
                query = String.format("SELECT * FROM USR U WHERE U.login='%s'", login);
                if((esql.executeQuery(query) > 0))
                {
                    query = String.format("SELECT * FROM USER_LIST_CONTAINS ULC WHERE ULC.list_id='%s' AND ULC.list_member='%s'", list_id, login);
                    String query1 = String.format("SELECT * FROM USER_LIST_CONTAINS ULC WHERE ULC.list_id='%s' AND ULC.list_member='%s'", list_id_1, login);
                    if(!(esql.executeQuery(query) > 0) && !(esql.executeQuery(query1) > 0))
                    {
                        query = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES('%d','%s')", list_id, login);
                    
                        esql.executeUpdate(query);
                        System.out.println("Contact successfully added!");
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "User is already on one of your lists.", "AddToList Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "User does not exist.", "AddToList Error", JOptionPane.ERROR_MESSAGE);
                    System.out.println("User does not exist!");
                }
            }

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void RemoveFromList(MessengerGUI esql, String user, String choice)
    {
        try {
            String query;
            List<List<String>> list;
            int list_id;
            String login = null;

            if(choice.equalsIgnoreCase("contacts"))
            {
                query = String.format("SELECT U.contact_list FROM USR U WHERE U.login='%s'", user);
                list = esql.executeQueryAndReturnResult(query);
                list_id = Integer.parseInt(list.get(0).get(0));
                login = JOptionPane.showInputDialog(null, "Enter login of user to remove from contacts.", "RemoveFromList Contacts",JOptionPane.QUESTION_MESSAGE); 
            }
            else if(choice.equalsIgnoreCase("blocked"))
            {
                query = String.format("SELECT U.block_list FROM USR U WHERE U.login='%s'", user);
                list = esql.executeQueryAndReturnResult(query);
                list_id = Integer.parseInt(list.get(0).get(0));   
                login = JOptionPane.showInputDialog(null, "Enter login of user to remove from blocked list.", "RemoveFromList Blocked",JOptionPane.QUESTION_MESSAGE);   
            }
            else
            {
                System.out.println("Unrecognized choice!");
                return;
            }

            if(login != null) {
                if(login.length() > MAX_login) {
                    JOptionPane.showMessageDialog(null, "Error: Login is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                query = String.format("SELECT * FROM USR U WHERE U.login='%s'", login);
                if(esql.executeQuery(query) > 0)
                {
                    query = String.format("SELECT * FROM USER_LIST_CONTAINS ULC WHERE ULC.list_id='%s' AND ULC.list_member='%s';", list_id, login);
                    if(esql.executeQuery(query) > 0) {                    
                        query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id='%s' AND list_member='%s' ", list_id, login);
                        esql.executeUpdate(query);
                        System.out.println("User successfully removed from list!");
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "User is not in the list.", "RemoveFromList Error", JOptionPane.ERROR_MESSAGE);
                        System.out.println("User is not in the list!");
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "User does not exist in that list.", "RemoveFromList Error", JOptionPane.ERROR_MESSAGE);
                    System.out.println("User does not exist!");
                }
            }
        } catch(Exception e) {
         System.err.println(e.getMessage());
        }
    }

    public static void ViewList(MessengerGUI esql, String user)
    {
        try {
            System.out.print("\tEnter list (\"contacts\" or \"blocked\"): ");
            String choice = in.readLine();
            
            String query;
            if(choice.equalsIgnoreCase("contacts"))
            {
                query = String.format("SELECT U2.login, U2.status FROM USR U, USR U2, USER_LIST UL, USER_LIST_CONTAINS ULC WHERE U.login='%s' AND U.contact_list=UL.list_id AND UL.list_id=ULC.list_id AND ULC.list_member=U2.login;", user);
            }
            else if(choice.equalsIgnoreCase("blocked"))
            {
                query = String.format("SELECT U2.login FROM USR U, USR U2, USER_LIST UL, USER_LIST_CONTAINS ULC WHERE U.login='%s' AND U.block_list=UL.list_id AND UL.list_id=ULC.list_id AND ULC.list_member=U2.login;", user);
            }
            else
            {
                System.out.println("Unrecognized choice!");
                return;
            }
            esql.executeQueryAndPrintResult(query);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void StartChat(MessengerGUI esql, String user)
    {
        try {
            String input = JOptionPane.showInputDialog(null, "Enter logins to start chat with (seperated by commas): ", "Starting New Message",JOptionPane.QUESTION_MESSAGE);
            if(input != null)
            {
                String query;
                String chat_type;
    
                List<String> member_list = Arrays.asList(input.split(",[ ]*"));
                member_list = new ArrayList<String>(new LinkedHashSet<String>(member_list));
                
                for(int i = 0; i < member_list.size(); i++)
                {
                    if(member_list.get(i).length() > MAX_login) {
                        JOptionPane.showMessageDialog(null, "Error: Login is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    //Prevent adding invalid users
                    query = String.format("SELECT * FROM USR U WHERE U.login='%s'", member_list.get(i));
                    int loginExists = esql.executeQuery(query);
                    if(loginExists <= 0) {
                        System.out.println(member_list.get(i) + " is not a valid user");
                        JOptionPane.showMessageDialog(null, member_list.get(i) + " is not a valid user.", "Starting Chat Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
    
                    //Prevent adding yourself to the chat
                    if(user.equals(member_list.get(i)))
                    {
                        System.out.println("Cannot add yourself to the chat");
                        JOptionPane.showMessageDialog(null, "Cannot add yourself to the chat.", "Starting Chat Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
    
                  //Check that you are not on their blocked list.
                    query = String.format("SELECT U2.login FROM USR U, USR U2, USER_LIST UL, USER_LIST_CONTAINS ULC WHERE U.login='%s' AND U2.login='%s' AND U2.block_list=UL.list_id AND UL.list_id=ULC.list_id AND ULC.list_member=U.login", user, member_list.get(i));
                    int exists = esql.executeQuery(query);
                    if(exists > 0)
                    {
                        System.out.println(member_list.get(i) + " has you blocked. You cannot start a chat with them.");
                        JOptionPane.showMessageDialog(null, member_list.get(i) + " has you blocked. You cannot start a chat with them.", "Starting Chat Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    //Checks that they are not on your blocked list.
                    query = String.format("SELECT U2.login FROM USR U, USR U2, USER_LIST UL, USER_LIST_CONTAINS ULC WHERE U.login='%s' AND U2.login='%s' AND U2.block_list=UL.list_id AND UL.list_id=ULC.list_id AND ULC.list_member=U.login", member_list.get(i), user);
                    exists = esql.executeQuery(query);
                    if(exists > 0)
                    {
                        System.out.println(member_list.get(i) + " is on your blocked list");
                        JOptionPane.showMessageDialog(null, member_list.get(i) + " is on your blocked list. You cannot add them to this chat.", "Adding Members Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
    
                if(member_list.size() > 1)
                    chat_type = "group";
                else
                    chat_type = "private";
    
                query = String.format("INSERT INTO CHAT(chat_type, init_sender) VALUES('%s','%s')", chat_type, user);
                esql.executeUpdate(query);
                int chatid = esql.getCurrSeqVal("chat_chat_id_seq");
    
                query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES('%s','%s')", chatid, user);
                esql.executeUpdate(query);
    
                for(int i = 0; i < member_list.size(); i++)
                {
                    query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES('%s','%s')", chatid, member_list.get(i));
                    esql.executeUpdate(query);
                }

                //Send initial message to all member of chat
                String text = "You have been added to this chat!";
                query = String.format("INSERT INTO MESSAGE(msg_text, msg_timestamp, sender_login, chat_id) VALUES('%s', NOW()::TIMESTAMP(0),'%s','%s');", text, user, chatid);
                esql.executeUpdate(query);
    
                System.out.println("Chat successfully created!");
                JOptionPane.showMessageDialog(null, "Chat successfully created!", "Starting New Chat", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void ViewChats(MessengerGUI esql, String user)
    {
        try {
            String query = String.format("SELECT C.chat_id, C.chat_type, C.init_sender, M.msg_timestamp "
                                + "FROM USR U, CHAT_LIST CL, CHAT C, MESSAGE M "
                                + "WHERE U.login='%s' AND CL.member=U.login AND C.chat_id=CL.chat_id AND C.chat_id=M.chat_id AND M.msg_id in "
                                + "( SELECT DISTINCT ON (M.chat_id) "
                                + "      M.msg_id "
                                + "FROM MESSAGE M "
                                + "ORDER BY M.chat_id, M.msg_timestamp DESC) "
                                + "ORDER BY M.msg_timestamp DESC;", user);
            esql.executeQueryAndPrintResult(query);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }  

    public static String ChatViewer(MessengerGUI esql, String user)
    {
        try {
            System.out.print("\tEnter chat id: ");
            String chatID = in.readLine();

            String query = String.format("SELECT * FROM CHAT_LIST CL WHERE CL.chat_id='%s' AND CL.member='%s';", chatID, user);
            int isMember = esql.executeQuery(query);

            if(isMember > 0)
            {
                return chatID;
            }
            else
            {
                System.out.println("Invalid chat id!");
                return null;
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static void DeleteAccount(MessengerGUI esql, String user)
    {
        try {
            //System.out.print("You are about to delete your account. Type \"DELETE\" to confirm or \"abort\" to cancel: ");
            //String choice = in.readLine();
            String input = JOptionPane.showInputDialog(null, "You are about to delete your account.\nType \"DELETE\" to confirm: ", "Deleting Account", JOptionPane.QUESTION_MESSAGE);
            if(input != null) {
                if(input.length() > 10) {
                    JOptionPane.showMessageDialog(null, "Error: Input is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if(input.equals("DELETE")) {
                    String query = String.format("DELETE FROM USR WHERE login='%s'", user);
                    esql.executeUpdate(query);
                    authorisedUser = null;
                    usermenu = false;
                    System.out.println(user + " has deleted their account");
                    JOptionPane.showMessageDialog(null, "Your account has been deleted. Returning to the main menu.", "Delete Account", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    JOptionPane.showMessageDialog(null, "Account not deleted.", "Delete Account", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void EditMembers(MessengerGUI esql, String user, String chatID)
    {
        try {
            int chat_id = Integer.parseInt(chatID);

            System.out.print("Would you like to \"add\" or \"remove\" members: ");
            String choice = in.readLine();

            String query = String.format("SELECT * FROM CHAT C WHERE C.chat_id='%s' AND C.init_sender='%s';", chat_id, user);
            int isInitSender = esql.executeQuery(query);
            if(isInitSender > 0)
            {
                if(choice.equalsIgnoreCase("add")) {
                    System.out.print("Enter logins to add to chat (seperated by commas): ");
                    String input = in.readLine();

                    List<String> member_list = Arrays.asList(input.split(",[ ]*"));

                    //Insert the users into the chat list
                    for(int i = 0; i < member_list.size(); i++)
                    {
                        query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES ('%s','%s');", chat_id, member_list.get(i));
                        esql.executeUpdate(query);
                    }

                    //Get the number of users in the chat
                    query = String.format("SELECT COUNT(*) FROM CHAT_LIST CL WHERE CL.chat_id='%s';", chat_id);
                    List<List<String>> tmp = esql.executeQueryAndReturnResult(query);
                    int numMembers = Integer.parseInt(tmp.get(0).get(0));

                    if(numMembers > 2) {
                        query = String.format("UPDATE CHAT SET chat_type = 'group' WHERE CHAT.chat_id='%s';", chat_id);
                        esql.executeUpdate(query);
                    }

                    System.out.println("User(s) added!");
                }
                else if(choice.equalsIgnoreCase("remove")) {
                    System.out.print("Enter logins to remove from chat (seperated by commas): ");
                    String input = in.readLine();

                    List<String> member_list = Arrays.asList(input.split(",[ ]*"));

                    for(int i = 0; i < member_list.size(); i++)
                    {
                        query = String.format("DELETE FROM CHAT_LIST WHERE chat_id='%s' AND member='%s';", chat_id, member_list.get(i));
                        esql.executeUpdate(query);
                    }

                    query = String.format("SELECT COUNT(*) FROM CHAT_LIST CL WHERE CL.chat_id=%s;", chat_id);
                    List<List<String>> tmp = esql.executeQueryAndReturnResult(query);
                    int numMembers = Integer.parseInt(tmp.get(0).get(0));
                    
                    if(numMembers <= 2) {
                        query = String.format("UPDATE CHAT SET chat_type = 'private' WHERE CHAT.chat_id='%s';", chat_id);
                        esql.executeUpdate(query);
                    }
                    System.out.println("Users removed!");
                }
                else {
                    System.out.println("Invalid choice!");                    
                }
                return;
            }
            else
            {
                System.out.println("You are not the initial sender!");
                return;
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void ViewMessages(MessengerGUI esql, String user, String chatID)
    {
        try {
            int numMsgs = 10;
            System.out.print("\tEnter page number (page 1 is the most recent messages): ");
            int pageNum = Integer.parseInt(in.readLine());

            System.out.println("PAGE " + Integer.toString(pageNum));

            String query = String.format("SELECT DISTINCT ON (M.msg_timestamp) M.msg_id, M.sender_login, M.msg_timestamp, M.msg_text "
                                            + "FROM CHAT_LIST CL, MESSAGE M "
                                            + "WHERE CL.chat_id='%s' AND CL.chat_id=M.chat_id "
                                            + "ORDER BY M.msg_timestamp DESC "
                                            + "LIMIT '%s' "
                                            + "OFFSET '%s';",chatID, numMsgs, 10*(pageNum-1));
            esql.executeQueryAndPrintResult(query);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void ListMembers(MessengerGUI esql, String user, String chatID)
    {
        try {
            String query = String.format("SELECT CL.member FROM CHAT_LIST CL WHERE CL.chat_id='%s'", chatID);

            esql.executeQueryAndPrintResult(query);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    //Returns true if chat still exists, chatViewer can continue running
    //Return false if chat is deleted, chatViewer needs to quit
    public static boolean DeleteChat(MessengerGUI esql, String user)
    {       
        int sel = chatManagerTable.getSelectedRow();
        if((sel >= 0) && (sel < chatManagerTable.getRowCount()))
        {
            String result = (String) chatManagerTable.getModel().getValueAt(sel, 0);
            int id = Integer.parseInt(result);
            
            try {
                String query = String.format("SELECT * FROM CHAT C WHERE C.chat_id='%s' AND C.init_sender='%s';", id, user);
                int isInitSender = esql.executeQuery(query);
    
                if(isInitSender > 0) {
                    String response = JOptionPane.showInputDialog(null, "Type \"DELETE\" to delete the chat.", "Delete Attempt", JOptionPane.QUESTION_MESSAGE);
                    if(response == null) return true;
                    if(response.length() > 10) {
                        JOptionPane.showMessageDialog(null, "Error: Input is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                        return true;
                    }
                    if(response.equals("DELETE"))
                    {
                        //Delete all messages
                        query = String.format("DELETE FROM MESSAGE WHERE chat_id='%s'", id, user);
                        esql.executeUpdate(query);
        
                        //Delete all in chat_list
                        query = String.format("DELETE FROM CHAT_LIST WHERE chat_id='%s'", id);
                        esql.executeUpdate(query);
        
                        //Delete from chat
                        query = String.format("DELETE FROM CHAT WHERE chat_id='%s';", id, user);
                        esql.executeUpdate(query);      
                        JOptionPane.showMessageDialog(null, "Chat deleted.", "Delete a Chat", JOptionPane.INFORMATION_MESSAGE);
                        System.out.println("Chat deleted!");
                        return false;
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Chat not deleted.", "Delete a Chat", JOptionPane.INFORMATION_MESSAGE);
                        return true;
                    }
    
                } else {
                    System.out.println("You are not the chat owner!");
                    JOptionPane.showMessageDialog(null, "Unable to delete. You must be the chat owner.", "Delete Chat Error", JOptionPane.ERROR_MESSAGE);
                    return true;
                }
    
            } catch(Exception e) {
                System.err.println(e.getMessage());
                return false;
            }
        }
        else {
            JOptionPane.showMessageDialog(null, "Please select a chat to delete.", "Delete Chat Error", JOptionPane.ERROR_MESSAGE);
            return true;
        }
    }

    public static void SendNewMessage(MessengerGUI esql, String user)
    {
        String message = JOptionPane.showInputDialog(null, "Enter message to send:", "Replying",JOptionPane.QUESTION_MESSAGE);
        if(message != null) {
            if(message.length() > MAX_message) {
                JOptionPane.showMessageDialog(null, "Error: Message is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String query = String.format("INSERT INTO MESSAGE (msg_text, msg_timestamp, sender_login, chat_id) VALUES ('%s', NOW()::TIMESTAMP(0), '%s', '%s')", message, authorisedUser, chat_id);
            
            try {
                esql.executeUpdate(query);
                System.out.println("Message sent.");
                JOptionPane.showMessageDialog(null, "Message sent.", "Reply", JOptionPane.INFORMATION_MESSAGE);
                updateMessageTable(authorisedUser);
            } catch(Exception e1) {
                System.err.println(e1.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Message not sent.", "Reply", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public static void DeleteMessage(MessengerGUI esql, String user) {
        if(chat_id == -1) {
            System.out.println("Chat id error");
            return;
        }
        try {
            int sel = messageTable.getSelectedRow();
            if((sel >= 0) && (sel < messageTable.getRowCount()))
            {
                String result = (String) messageTable.getModel().getValueAt(sel, 0);
                int msg_id = Integer.parseInt(result);

                String query = String.format("SELECT * FROM Message M WHERE M.sender_login='%s' AND M.msg_id='%s' AND M.chat_id='%s'", user, msg_id, chat_id);
                List<List<String>> list = esql.executeQueryAndReturnResult(query);
                int msgExists = list.size();

                if(msgExists > 0) {
                    String response = JOptionPane.showInputDialog(null, "Enter \"DELETE\" to delete this message.", "Delete Message.", JOptionPane.QUESTION_MESSAGE);
                    if(response != null)
                    {
                        if(response.length() > 10) {
                            JOptionPane.showMessageDialog(null, "Error: Input is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        if(response.equals("DELETE"))
                        {
                            query = String.format("DELETE FROM MESSAGE WHERE sender_login='%s' AND msg_id='%s' AND chat_id='%s'", user, msg_id, chat_id);
                            esql.executeUpdate(query);
                            System.out.println("Message deleted!");
                            JOptionPane.showMessageDialog(null, "Message deleted.", "Delete Message", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else {
                            JOptionPane.showMessageDialog(null, "Message not deleted.", "Delete Message", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Message not deleted.", "Delete Message", JOptionPane.INFORMATION_MESSAGE);
                    }

                } else {
                    System.out.println("Not allowed to delete this message");
                    JOptionPane.showMessageDialog(null, "You must be the initial sender in order to delete this message.", "Delete Message Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            else {
                JOptionPane.showMessageDialog(null, "Please select a message to delete.", "Delete Message Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void EditMessage(MessengerGUI esql, String user)
    {
        if(chat_id == -1) {
            System.out.println("Chat id error");
            return;
        }
        try {
            int sel = messageTable.getSelectedRow();
            if((sel >= 0) && (sel < messageTable.getRowCount()))
            {
                String result = (String) messageTable.getModel().getValueAt(sel, 0);
                int msg_id = Integer.parseInt(result);

                String query = String.format("SELECT * FROM Message M WHERE M.sender_login='%s' AND M.msg_id='%s' AND M.chat_id='%s'", user, msg_id, chat_id);
                List<List<String>> list = esql.executeQueryAndReturnResult(query);
                int msgExists = list.size();

                if(msgExists > 0) {
                    String response = JOptionPane.showInputDialog(null, "Enter the new message:", "Edit Message.", JOptionPane.QUESTION_MESSAGE);
                    if(response != null)
                    {
                        if(response.length() > MAX_message) {
                            JOptionPane.showMessageDialog(null, "Error: Message is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        query = String.format("UPDATE MESSAGE SET msg_text='%s' WHERE sender_login='%s' AND msg_id='%s' AND chat_id='%s';", response, user, msg_id, chat_id);
                        esql.executeUpdate(query);
                        System.out.println("Message updated!");
                        JOptionPane.showMessageDialog(null, "Message updated.", "Edit Message", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Message not updated.", "Edit Message", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    System.out.println("Not allowed to edit this message");
                    JOptionPane.showMessageDialog(null, "You must be the initial sender in order to edit this message.", "Edit Message Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a message to edit.", "Edit Message Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void updateContactTable(String user) {
        String s = String.format("SELECT U2.login, U2.status FROM USR U, USR U2, USER_LIST UL, USER_LIST_CONTAINS ULC WHERE U.login='%s' AND U.contact_list=UL.list_id AND UL.list_id=ULC.list_id AND ULC.list_member=U2.login ORDER BY U2.login;", authorisedUser);
        System.out.println("Refreshing \"List Manager\" of user: " + user);
        List<List<String>> temp = new ArrayList<>();
        
        try {
            temp = esql.executeQueryAndReturnResult(s);
        } catch(Exception e1) {
            System.err.println(e1.getMessage());
        }
        
        ContactTableData = new Object[temp.size()][];
        for(int i = 0; i < temp.size(); i++) {
            List<String> row = temp.get(i);
            for(int j = 0; j < row.size(); j++) {
                if(row.get(j) != null) {
                    row.set(j, row.get(j).trim());
                }
            }
            ContactTableData[i] = row.toArray(new String[row.size()]);
        }
        
        contactTable = new JTable(ContactTableData, ContactTableCols) {
            public boolean isCellEditable(int row, int column) {                
                return false;               
            };
        };

        contactTable.setAutoResizeMode(contactTable.AUTO_RESIZE_OFF);
        resizeColumns(contactTable, "contacts");

        contactTablePane.setViewportView(contactTable);
    }

    public void updateBlockedTable(String user)
    {
        String s = String.format("SELECT U2.login FROM USR U, USR U2, USER_LIST UL, USER_LIST_CONTAINS ULC WHERE U.login='%s' AND U.block_list=UL.list_id AND UL.list_id=ULC.list_id AND ULC.list_member=U2.login ORDER BY U2.login;", authorisedUser);
        List<List<String>> temp = new ArrayList<>();
        try {
            temp = esql.executeQueryAndReturnResult(s);
        } catch(Exception e1) {
            System.err.println(e1.getMessage());
        }
        
        blockedTableData = new String[temp.size()];
        for(int i = 0; i < temp.size(); i++) {
            List<String> row = temp.get(i);
            for(int j = 0; j < row.size(); j++) {
                row.set(j, row.get(j).trim());
            }
            blockedTableData[i] = row.get(0);
        }
        
        blockedTable = new JList<String>(blockedTableData);
        blockedTablePane.setViewportView(blockedTable);
    }

    public void updateChatTable(String user)
    {
        String s = String.format("SELECT C.chat_id, C.chat_type, M.msg_timestamp, C.init_sender "
                + "FROM USR U, CHAT_LIST CL, CHAT C, MESSAGE M "
                + "WHERE U.login='%s' AND CL.member=U.login AND C.chat_id=CL.chat_id AND C.chat_id=M.chat_id AND M.msg_id in "
                + "( SELECT DISTINCT ON (M.chat_id) "
                + "      M.msg_id "
                + "FROM MESSAGE M "
                + "ORDER BY M.chat_id, M.msg_timestamp DESC) "
                + "ORDER BY M.msg_timestamp DESC;", authorisedUser);
        
        System.out.println("Refreshing \"Chat Manager\" of user: " + user);
        List<List<String>> temp = new ArrayList<>();
        try {
            temp = esql.executeQueryAndReturnResult(s);
        } catch(Exception e1) {
            System.err.println(e1.getMessage());
        }
        
        chatManagerData = new Object[temp.size()][];
        for(int i = 0; i < temp.size(); i++) {
            List<String> row = temp.get(i);
            for(int j = 0; j < row.size(); j++) {
                row.set(j, row.get(j).trim());
            }
            chatManagerData[i] = row.toArray(new String[row.size()]);
        }
        
        chatManagerTable = new JTable(chatManagerData, chatManagerCols) {
            public boolean isCellEditable(int row, int column) {                
                return false;               
            };
        };
        
        chatManagerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatManagerTable.setAutoResizeMode(chatManagerTable.AUTO_RESIZE_OFF);
        resizeColumns(chatManagerTable, "chats");
                
        chatManagerPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        chatManagerPane.setViewportView(chatManagerTable);
    }
    
    //Returns true if the table is updated
    public static boolean updateMessageTable(String user)
    {
        //Grab the selected row
        int sel = chatManagerTable.getSelectedRow();
        int numRows = chatManagerTable.getRowCount();       
        
        if((sel >= 0) && (sel < chatManagerTable.getRowCount())) {//Check if the index is within the table's range
            //Get the chat id from that result
            String result = (String) chatManagerTable.getModel().getValueAt(sel, 0);
            int id = Integer.parseInt(result);
            //Run query to get chat data (message lists)
            String s = String.format("SELECT DISTINCT ON (M.msg_timestamp) M.msg_id, M.msg_timestamp, M.sender_login, M.msg_text "
                    + "FROM CHAT_LIST CL, MESSAGE M "
                    + "WHERE CL.chat_id='%s' AND CL.chat_id=M.chat_id "
                    + "ORDER BY M.msg_timestamp DESC;",id);
            System.out.println("Refreshing \"Messages\" of user: " + user + " chat id: " + Integer.toString(id));
            List<List<String>> temp = new ArrayList<>();
            try {
                temp = esql.executeQueryAndReturnResult(s);
            } catch(Exception e1) {
                System.err.println(e1.getMessage());
            }
            
            //Fill the message table accordingly
            messageViewerData = new Object[temp.size()][];
            for(int i = 0; i < temp.size(); i++) {
                List<String> row = temp.get(i);
                for(int j = 0; j < row.size(); j++) {
                    row.set(j, row.get(j).trim());
                }
                messageViewerData[i] = row.toArray(new String[row.size()]);
            }
            
            messageTable = new JTable(messageViewerData, messageViewerCols) {
                public boolean isCellEditable(int row, int column) {                
                    return false;               
                };
            };
            
            messageTable.setAutoResizeMode(messageTable.AUTO_RESIZE_OFF);
            resizeColumns(messageTable, "msgs");
            
            messagePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            messagePane.setViewportView(messageTable);
            
            chat_id = id; //Set the global variable to the chat we are viewing
            
            return true;
        }
        else {
            System.out.println("Invalid chat selection");
            JOptionPane.showMessageDialog(null, "Please select a chat to view.", "ChatViewer Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean updateMessageTableWChatId(String user, int chatid)
    {   
        if(chatid >= 0) {
            //Run query to get chat data (message lists)
            String s = String.format("SELECT DISTINCT ON (M.msg_timestamp) M.msg_id, M.msg_timestamp, M.sender_login, M.msg_text "
                    + "FROM CHAT_LIST CL, MESSAGE M "
                    + "WHERE CL.chat_id='%s' AND CL.chat_id=M.chat_id "
                    + "ORDER BY M.msg_timestamp DESC;",chatid);
            System.out.println("Refreshing \"Messages\" of user: " + user + " chat id: " + Integer.toString(chatid));
            List<List<String>> temp = new ArrayList<>();
            try {
                temp = esql.executeQueryAndReturnResult(s);
            } catch(Exception e1) {
                System.err.println(e1.getMessage());
            }
            
            //Fill the message table accordingly
            messageViewerData = new Object[temp.size()][];
            for(int i = 0; i < temp.size(); i++) {
                List<String> row = temp.get(i);
                for(int j = 0; j < row.size(); j++) {
                    row.set(j, row.get(j).trim());
                }
                messageViewerData[i] = row.toArray(new String[row.size()]);
            }
            
            messageTable = new JTable(messageViewerData, messageViewerCols) {
                public boolean isCellEditable(int row, int column) {
                    return false;               
                };
            };
            
            messageTable.setAutoResizeMode(messageTable.AUTO_RESIZE_OFF);
            resizeColumns(messageTable, "msgs");
            
            chat_id = chatid; //Set the global variable to the chat we are viewing
            return true;
        }
        else {
            System.out.println("Error refreshing \"Messages\" of user: " + user + " chat id: " + Integer.toString(chatid));
            System.out.println("Invalid chat selection");
            JOptionPane.showMessageDialog(null, "Please select a chat to view.", "ChatViewer Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public void updateMemberTable(String user)
    {
        if(chat_id != -1) {
            //Run query to get members
            String s = String.format("SELECT CL.member FROM CHAT_LIST CL WHERE CL.chat_id='%s' ORDER BY CL.member", chat_id);
            System.out.println("Refreshing chat members of chat_id: " + Integer.toString(chat_id));
            List<List<String>> temp = new ArrayList<>();
            try {
                temp = esql.executeQueryAndReturnResult(s);
            } catch(Exception e) {
                System.err.println(e.getMessage());
            }
            
            msgMembersData = new String[temp.size()];
            for(int i = 0; i < temp.size(); i++) {
                List<String> row = temp.get(i);
                for(int j = 0; j < row.size(); j++) {
                    if(row.get(j) != null) {
                        row.set(j,  row.get(j).trim());
                    }
                }
                msgMembersData[i] = row.get(0);
            }
            
            //Save in the JList
            msgMemberList = new JList<String>(msgMembersData);
            //Update the view
            msgMemberPane.setViewportView(msgMemberList);
        }
    }
    
    public static void AddMembers(MessengerGUI esql, String user) {
        try {
            String query = String.format("SELECT * FROM CHAT C WHERE C.chat_id='%s' AND C.init_sender='%s';", chat_id, user);
            int isInitSender = esql.executeQuery(query);
            
            if(isInitSender > 0)
            {
                String input = JOptionPane.showInputDialog(null, "Enter logins to add to chat (seperated by commas):  ", "Add Members", JOptionPane.QUESTION_MESSAGE);

                List<String> member_list = Arrays.asList(input.split(",[ ]*"));

                //Check if the user is valid(exists, not you, not on their blocked list)
                for(int i = 0; i < member_list.size(); i++)
                {
                    if(member_list.get(i).length() > MAX_login) {
                        JOptionPane.showMessageDialog(null, "Error: Login is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    //Prevent adding invalid users
                    query = String.format("SELECT * FROM USR U WHERE U.login='%s'", member_list.get(i));
                    int loginExists = esql.executeQuery(query);
                    if(loginExists <= 0) {
                        System.out.println(member_list.get(i) + " is not a valid user");
                        JOptionPane.showMessageDialog(null, member_list.get(i) + " is not a valid user.", "Adding Members Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
        
                    //Prevent adding yourself to the chat
                    if(user.equals(member_list.get(i)))
                    {
                        System.out.println("Cannot add yourself to the chat");
                        JOptionPane.showMessageDialog(null, "Cannot add yourself to the chat.", "Adding Members Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    //Check that you are not on their blocked list.
                    query = String.format("SELECT U2.login FROM USR U, USR U2, USER_LIST UL, USER_LIST_CONTAINS ULC WHERE U.login='%s' AND U2.login='%s' AND U2.block_list=UL.list_id AND UL.list_id=ULC.list_id AND ULC.list_member=U.login", user, member_list.get(i));
                    int exists = esql.executeQuery(query);
                    if(exists > 0)
                    {
                        System.out.println(member_list.get(i) + " has you blocked, you cannot start a chat with them");
                        JOptionPane.showMessageDialog(null, member_list.get(i) + " has you blocked. You cannot add them to this chat.", "Adding Members Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    //Checks that they are not on your blocked list.
                    query = String.format("SELECT U2.login FROM USR U, USR U2, USER_LIST UL, USER_LIST_CONTAINS ULC WHERE U.login='%s' AND U2.login='%s' AND U2.block_list=UL.list_id AND UL.list_id=ULC.list_id AND ULC.list_member=U.login", member_list.get(i), user);
                    exists = esql.executeQuery(query);
                    if(exists > 0)
                    {
                        System.out.println(member_list.get(i) + " is on your blocked list, you cannot start a chat with them");
                        JOptionPane.showMessageDialog(null, member_list.get(i) + " is on your blocked list. You cannot add them to this chat.", "Adding Members Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                //Insert the users into the chat list
                for(int i = 0; i < member_list.size(); i++)
                {
                    query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES ('%s','%s');", chat_id, member_list.get(i));
                    esql.executeUpdate(query);
                    
                    //Sends a new message for every user added to the chat.
                    query = String.format("INSERT INTO MESSAGE (msg_text, msg_timestamp, sender_login, chat_id) VALUES ('%s', NOW()::TIMESTAMP(0), '%s', '%s')", member_list.get(i) + " has been added to the chat.", user, chat_id);
                    esql.executeUpdate(query);
                }

                //Get the number of users in the chat
                query = String.format("SELECT COUNT(*) FROM CHAT_LIST CL WHERE CL.chat_id='%s';", chat_id);
                List<List<String>> tmp = esql.executeQueryAndReturnResult(query);
                int numMembers = Integer.parseInt(tmp.get(0).get(0));

                if(numMembers > 2) {
                    query = String.format("UPDATE CHAT SET chat_type = 'group' WHERE CHAT.chat_id='%s';", chat_id);
                    esql.executeUpdate(query);
                }
                    System.out.println("User(s) added!");
                    JOptionPane.showMessageDialog(null, "User(s) added.", "Add Members", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                System.out.println("Must be init sender to do this");
                JOptionPane.showMessageDialog(null,"You must be the initial sender to add members.", "Adding Members Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    public static void DeleteMembers(MessengerGUI esql, String user) {

        try {
            String query = String.format("SELECT * FROM CHAT C WHERE C.chat_id='%s' AND C.init_sender='%s';", chat_id, user);
            int isInitSender = esql.executeQuery(query);
            
            if(isInitSender > 0)
            {
                String input = JOptionPane.showInputDialog(null, "Enter logins to remove from the chat (seperated by commas):  ", "Remove Members", JOptionPane.QUESTION_MESSAGE);
                List<String> member_list = Arrays.asList(input.split(",[ ]*"));

                //Check if the users are valid for deletion (user exits, user is part of the chat)
                for(int i = 0; i < member_list.size(); i++)
                {
                    if(member_list.get(i).length() > MAX_login) {
                        JOptionPane.showMessageDialog(null, "Error: Login is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                for(int i = 0; i < member_list.size(); i++)
                {
                    query = String.format("DELETE FROM CHAT_LIST WHERE chat_id='%s' AND member='%s';", chat_id, member_list.get(i));
                    esql.executeUpdate(query);
                }

                query = String.format("SELECT COUNT(*) FROM CHAT_LIST CL WHERE CL.chat_id=%s;", chat_id);
                List<List<String>> tmp = esql.executeQueryAndReturnResult(query);
                int numMembers = Integer.parseInt(tmp.get(0).get(0));
                
                if(numMembers <= 2) {
                    query = String.format("UPDATE CHAT SET chat_type = 'private' WHERE CHAT.chat_id='%s';", chat_id);
                    esql.executeUpdate(query);
                }
                System.out.println("Users removed!");
                JOptionPane.showMessageDialog(null, "Users removed.", "Removed Members", JOptionPane.INFORMATION_MESSAGE);
            }
            else
            {
                System.out.println("You are not the initial sender!");
                JOptionPane.showMessageDialog(null,"You must be the initial sender to remove members.", "Removing Members Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void updateStatus(MessengerGUI esql, String user) {
        String input = JOptionPane.showInputDialog(null, "Enter your new status:  ", "Update Status", JOptionPane.QUESTION_MESSAGE);
        if(input != null)
        {
            if(input.length() > MAX_status) {
                JOptionPane.showMessageDialog(null, "Error: Status is too long.", "Size Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                String query = String.format("UPDATE USR SET status='%s' WHERE USR.login='%s';", input, user);
                esql.executeUpdate(query);
                System.out.println(user + " has updated their status to: " + input);
                JOptionPane.showMessageDialog(null, "Status update", "Update Status", JOptionPane.INFORMATION_MESSAGE);
                personalizeMainMenu(esql, user);
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void personalizeMainMenu(MessengerGUI esql, String user) {
        String query = String.format("SELECT U.status FROM USR U WHERE U.login='%s'", user);
        String status = "";
        try {
            status = esql.executeQueryAndReturnResult(query).get(0).get(0);
            if(status != null) {
                status = status.trim();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        
        System.out.println("User authorised as: " + authorisedUser);
        lblHelloUser.setText("Hello, " + authorisedUser);
        if(status == null) {
            textArea_CurStatus.setText("\"" + "\"");
        } else{  
            textArea_CurStatus.setText("\"" + status + "\"");
        }
    }
}
