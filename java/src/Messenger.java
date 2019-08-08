/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science & Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

/*
 * CS166 Winter 2016
 * Project Phase 3
 * Christopher Kotyluk 861069101
 * Jesse Gomez 861056174
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.*;
import java.lang.*;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 */
public class Messenger {
    // reference to physical database connection.
    private Connection _connection = null;

    // handling the keyboard inputs through a BufferedReader
    // This variable can be global for convenience.
    static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

    //Global variables to set the status of the menus
    static String authorisedUser;
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
    public Messenger(String dbname, String dbport, String user, String passwd) throws SQLException
    {

        System.out.print("Connecting to database...");
        try {
            // constructs the connection URL
            String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
            System.out.println("Connection URL: " + url + "\n");

            // obtain a physical connection
            this._connection = DriverManager.getConnection(url, user, passwd);
            System.out.println("Done");
        } catch(Exception e) {
            System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
            System.out.println("Make sure you started postgres on this machine");
            System.exit(-1);
        }//end catch
    }//end Messenger

    /**
     * Method to execute an update SQL statement.  Update SQL instructions
     * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
     *
     * @param sql the input SQL string
     * @throws java.sql.SQLException when update failed
     */
    public void executeUpdate(String sql) throws SQLException 
    {
        // creates a statement object
        Statement stmt = this._connection.createStatement();

        // issues the update instruction
        stmt.executeUpdate(sql);

        // close the instruction
        stmt.close();
    }//end executeUpdate

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
        // creates a statement object
        Statement stmt = this._connection.createStatement();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery(query);

        /**
         * Obtains the metadata object for the returned result set. The metadata
         * contains row and column info.
         */
        ResultSetMetaData rsmd = rs.getMetaData();
        int numCol = rsmd.getColumnCount();
        int rowCount = 0;

        // iterates through the result set and output them to standard out.
        boolean outputHeader = true;
        while(rs.next()) {
            if(outputHeader) {
                for(int i = 1; i <= numCol; i++) {
                    System.out.print(String.format("%1$-" + rsmd.getColumnDisplaySize(i) + "s", rsmd.getColumnName(i) ));
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
                System.out.print(String.format("%1$-" + rsmd.getColumnDisplaySize(i) + "s", rs.getString(i) ));
            }
            System.out.println();
            ++rowCount;
        }//end while
        stmt.close();
        return rowCount;
    }//end executeQuery

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
        // creates a statement object 
        Statement stmt = this._connection.createStatement(); 

        // issues the query instruction 
        ResultSet rs = stmt.executeQuery(query); 

        /* 
         * Obtains the metadata object for the returned result set. The metadata 
         * contains row and column info. 
         */ 
        ResultSetMetaData rsmd = rs.getMetaData(); 
        int numCol = rsmd.getColumnCount(); 
        int rowCount = 0; 

        // iterates through the result set and saves the data returned by the query. 
        boolean outputHeader = false;
        List<List<String>> result  = new ArrayList<List<String>>(); 
        while(rs.next()) {
            List<String> record = new ArrayList<String>(); 
            for(int i=1; i<=numCol; ++i) 
                record.add(rs.getString(i)); 
            result.add(record); 
        }//end while 
        stmt.close(); 
        return result; 
    }//end executeQueryAndReturnResult

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
        // creates a statement object
        Statement stmt = this._connection.createStatement();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery(query);

        int rowCount = 0;

        // iterates through the result set and count nuber of results.
        if(rs.next()) {
            rowCount++;
        }
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
            }//end if
        } catch(SQLException e) {
         // ignored.
        }//end try
    }//end cleanup

    /**
     * The main execution method
     *
     * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
     */
    public static void main(String[] args) 
    {
      if(args.length != 3) {
         System.err.println(
            "Usage: " +
            "java [-classpath <classpath>] " +
            Messenger.class.getName() +
            " <dbname> <port> <user>");
         return;
      }//end if
      
      Greeting();
      Messenger esql = null;
      try {
            // use postgres JDBC driver.
            Class.forName("org.postgresql.Driver").newInstance();
            // instantiate the Messenger object and creates a physical
            // connection.
            String dbname = args[0];
            String dbport = args[1];
            String user = args[2];
            esql = new Messenger(dbname, dbport, user, "");

            boolean keepon = true;
            while(keepon) {
            // These are sample SQL statements
            System.out.println("\nMAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            authorisedUser = null;
            switch(readChoice()) {
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if(authorisedUser != null) {
                usermenu = true;
                while(usermenu) {
                    System.out.println("\nMAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Add to contacts/blocked list");
                    System.out.println("2. Remove from contacts/blocked list");
                    System.out.println("3. Browse contacts/blocked list");
                    System.out.println("4. Start a chat");
                    System.out.println("5. Browse chat list");
                    System.out.println("6. Enter chat viewer");
                    System.out.println("7. Delete account");
                    //System.out.println("4. Write a new message");
                    System.out.println(".........................");
                    System.out.println("9. Log out");

                    String chatID = null;
                    switch(readChoice()) {
                        case 1: AddToList(esql, authorisedUser); break;
                        case 2: RemoveFromList(esql, authorisedUser); break;
                        case 3: ViewList(esql, authorisedUser); break;
                        case 4: StartChat(esql, authorisedUser); break;
                        case 5: ViewChats(esql, authorisedUser); break;
                        case 6: chatID = ChatViewer(esql, authorisedUser); break;
                        case 7: DeleteAccount(esql, authorisedUser); break;

                        //case 4: NewMessage(esql); break;
                        case 9: usermenu = false; break;
                        default : System.out.println("Unrecognized choice!"); break;
                    }
                    if(chatID != null)
                    {
                        boolean chatViewer = true;   
                        while(chatViewer) {
                            System.out.println("\nCHAT VIEWER");
                            System.out.println("---------");
                            System.out.println("1. Browse recent messages");
                            System.out.println("2. Browse chat members");
                            System.out.println("3. Add/remove chat members");
                            System.out.println("4. Delete the chat");
                            System.out.println("5. Send a message");
                            System.out.println("6. Edit/remove a message");

                            System.out.println(".........................");
                            System.out.println("9. Exit chat viewer");
                            switch(readChoice()) {
                                case 1: ViewMessages(esql, authorisedUser, chatID); break; 
                                case 2: ListMembers(esql, authorisedUser, chatID); break;
                                case 3: EditMembers(esql, authorisedUser, chatID); break;
                                case 4: chatViewer = DeleteChat(esql, authorisedUser, chatID); break;
                                case 5: SendNewMessage(esql, authorisedUser, chatID); break;
                                case 6: EditMessage(esql, authorisedUser, chatID); break;

                                case 9: chatViewer = false; break;
                                default: System.out.println("Unrecognized choice!"); break;
                            }
                        }
                    }
                }
            }
         }//end while
      } catch(Exception e) {
            System.err.println(e.getMessage());
      } finally {
            // make sure to cleanup the created table and close the connection.
            try {
                if(esql != null) {
                    System.out.print("Disconnecting from database...");
                    esql.cleanup();
                    System.out.println("Done\n\nBye !");
                }//end if
            } catch(Exception e) {
                // ignored.
            }//end try
      }//end try
    }//end main

    public static void Greeting() {
        System.out.println("\n\n" +
        "*******************************************************\n" +
        "              User Interface                         \n" +
        "*******************************************************\n");
    }//end Greeting

    /**
     * Reads the users choice given from the keyboard
     * @int
     */
    public static int readChoice() 
    {
        int input;
        // returns only if a correct value is given.
        do {
            System.out.print("Please make your choice: ");
            try { // read the integer, parse it and break.
                input = Integer.parseInt(in.readLine());
                break;
            } catch(Exception e) {
                System.out.println("Your input is invalid!");
                continue;
            }//end try
        } while(true);
        return input;
    }//end readChoice

    /**
     * Creates a new user with privided login, passowrd and phoneNum
     * An empty block and contact list would be generated and associated with a user
     */
    public static void CreateUser(Messenger esql)
    {
        try {
            System.out.print("\tEnter user login: ");
            String login = in.readLine();

            String query = String.format("SELECT * FROM USR U WHERE U.login='%s'",login);
            int loginExists = esql.executeQuery(query);
            if(loginExists > 0){
                System.out.println("User already exists!");
                return;
            }
            else{
                System.out.print("\tEnter user password: ");
                String password = in.readLine();
                System.out.print("\tEnter user phone: ");
                String phone = in.readLine();

                 //Creating empty contact\block lists for a user
                esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES('block')");
                int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
                esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES('contact')");
                int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");
                       
                query = String.format("INSERT INTO USR(phoneNum, login, password, block_list, contact_list) VALUES('%s','%s','%s',%s,%s)", phone, login, password, block_id, contact_id);

                esql.executeUpdate(query);
                System.out.println("User successfully created!");
            }
        } catch(Exception e) {
             System.err.println(e.getMessage());
        }
    }//end

    /**
     * Check log in credentials for an existing user
     * @return User login or null is the user does not exist
     */
    public static String LogIn(Messenger esql)
    {
        try {
            System.out.print("\tEnter user login: ");
            String login = in.readLine();
            System.out.print("\tEnter user password: ");
            String password = in.readLine();

            String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
            int userNum = esql.executeQuery(query);
            if(userNum > 0) {
                System.out.println("Login successful!");
                return login;
            }else{
                System.out.println("Login failed! User/password is invalid.");
                return null;
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }//end

    public static void AddToList(Messenger esql, String user)
    {
        try {
            System.out.print("\tEnter list to add to (\"contacts\" or \"blocked\"): ");
            String choice = in.readLine();

            String query;
            List<List<String>> list;
            int list_id;

            //if(choice == "contacts")
            if(choice.equalsIgnoreCase("contacts"))
            {
                query = String.format("SELECT U.contact_list FROM USR U WHERE U.login='%s'", user);
                list = esql.executeQueryAndReturnResult(query);
                list_id = Integer.parseInt( list.get(0).get(0) );
            }
            //else if(choice == "blocked")
            else if(choice.equalsIgnoreCase("blocked"))
            {
                query = String.format("SELECT U.block_list FROM USR U WHERE U.login='%s'", user);
                list = esql.executeQueryAndReturnResult(query);
                list_id = Integer.parseInt( list.get(0).get(0) );   
            }
            else
            {
                System.out.println("Unrecognized choice!");
                return;
            }

            System.out.print("\tEnter persons's login: ");
            String login = in.readLine();

            query = String.format("SELECT * FROM USR U WHERE U.login='%s'", login);
            int loginExists = esql.executeQuery(query);
            if(loginExists <= 0){
                System.out.println("User does not exist.");
                return;
            }

            query = String.format("SELECT * FROM USR U WHERE U.login='%s'", login);
            if(( esql.executeQuery(query) > 0 ))
            {
                query = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES('%d','%s')", list_id, login);
            
                esql.executeUpdate(query);
                System.out.println("Person successfully added!");
            }
            else
            {
                System.out.println("Person doesn't exist!");
            }

        } catch(Exception e) {
         System.err.println(e.getMessage());
        }
    }//end

    public static void RemoveFromList(Messenger esql, String user)
    {
        try {
            System.out.print("\tEnter list to remove from (\"contacts\" or \"blocked\"): ");
            String choice = in.readLine();

            String query;
            List<List<String>> list;
            int list_id;

            //if(choice == "contacts")
            if(choice.equalsIgnoreCase("contacts"))
            {
                query = String.format("SELECT U.contact_list FROM USR U WHERE U.login='%s'", user);
                list = esql.executeQueryAndReturnResult(query);
                list_id = Integer.parseInt( list.get(0).get(0) );
            }
            //else if(choice == "blocked")
            else if(choice.equalsIgnoreCase("blocked"))
            {
                query = String.format("SELECT U.block_list FROM USR U WHERE U.login='%s'", user);
                list = esql.executeQueryAndReturnResult(query);
                list_id = Integer.parseInt( list.get(0).get(0) );   
            }
            else
            {
                System.out.println("Unrecognized choice!");
                return;
            }

            System.out.print("\tEnter persons's login: ");
            String login = in.readLine();

            query = String.format("SELECT * FROM USR U WHERE U.login='%s'", login);
            if( esql.executeQuery(query) > 0 )
            {
                query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id='%s' AND list_member='%s' ", list_id, login);
                esql.executeUpdate(query);
                System.out.println("Person successfully TERMINATED!");
            }
            else
            {
                System.out.println("Person doesn't exist!");
            }

        } catch(Exception e) {
         System.err.println(e.getMessage());
        }
    }//end

    public static void ViewList(Messenger esql, String user)
    {
        try {
            System.out.print("\tEnter list (\"contacts\" or \"blocked\"): ");
            String choice = in.readLine();
            
            String query;
            //if(choice == "contacts")
            if(choice.equalsIgnoreCase("contacts"))
            {
                query = String.format("SELECT U2.login, U2.status FROM USR U, USR U2, USER_LIST UL, USER_LIST_CONTAINS ULC WHERE U.login='%s' AND U.contact_list=UL.list_id AND UL.list_id=ULC.list_id AND ULC.list_member=U2.login;", user);
            }
            //else if(choice == "blocked")
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
    }//end

    public static void StartChat(Messenger esql, String user)
    {
        try {
            System.out.print("Enter logins to start chat with (seperated by commas): ");
            String input = in.readLine();
            String query;
            String chat_type;

            List<String> member_list = Arrays.asList(input.split(",[ ]*"));
            for(int i = 0; i < member_list.size(); i++)
            {
                //Prevent adding invalid users
                query = String.format("SELECT * FROM USR U WHERE U.login='%s'", member_list.get(i));
                int loginExists = esql.executeQuery(query);
                if(loginExists <= 0){
                    System.out.println(member_list.get(i) + " is not a valid user.");
                    return;
                }

                //Prevent adding yourself to the chat
                if(user.equals(member_list.get(i)))
                {
                    System.out.println("Cannot add yourself to the chat.");
                    return;
                }

                query = String.format("SELECT U2.login FROM USR U, USR U2, USER_LIST UL, USER_LIST_CONTAINS ULC WHERE U.login='%s' AND U2.login='%s' AND U2.block_list=UL.list_id AND UL.list_id=ULC.list_id AND ULC.list_member=U.login", user, member_list.get(i));
                int exists = esql.executeQuery(query);
                if(exists > 0)
                {
                    System.out.println(member_list.get(i) + " has you blocked. You cannot start a chat with them.");
                    return;
                }


            }

            if(member_list.size() > 1)
                chat_type = "group";
            else
                chat_type = "private";

            query = String.format("INSERT INTO CHAT(chat_type, init_sender) VALUES('%s','%s')", chat_type, user);
            esql.executeUpdate(query);
            int chat_id = esql.getCurrSeqVal("chat_chat_id_seq");

            query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES('%s','%s')", chat_id, user);
            esql.executeUpdate(query);

            for(int i = 0; i < member_list.size(); i++)
            {
                query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES('%s','%s')", chat_id, member_list.get(i));
                esql.executeUpdate(query);
            }


            // Send initial message to all member of chat
            String text = "You have been added to this chat!";
            query = String.format("INSERT INTO MESSAGE(msg_text, msg_timestamp, sender_login, chat_id) VALUES('%s', NOW()::TIMESTAMP(0),'%s','%s');", text, user, chat_id);
            esql.executeUpdate(query);


            System.out.println("Chat successfully created!");

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    public static void ViewChats(Messenger esql, String user)
    {
        try {
            String query;
            
            //query = String.format("SELECT C.chat_type, C.init_sender FROM USR U, CHAT_LIST CL, CHAT C WHERE U.login='%s' AND CL.member=U.login AND C.chat_id=CL.chat_id;", user);
            query = String.format("SELECT C.chat_id, C.chat_type, C.init_sender, M.msg_timestamp "
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
    }//end   

    public static String ChatViewer(Messenger esql, String user)
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
    }//end

    public static void DeleteAccount(Messenger esql, String user)
    {
        try {
            System.out.print("You are about to delete your account. Type \"DELETE\" to confirm or \"abort\" to cancel: ");
            String choice = in.readLine();
            
            if(choice.equals("DELETE")){
                String query = String.format("DELETE FROM USR WHERE login='%s'", user);
                esql.executeUpdate(query);
                authorisedUser = null;
                usermenu = false;
                System.out.println("Your account has been deleted. Returning to the main menu.");
            }
            else if(choice.equalsIgnoreCase("abort")){
                System.out.println("Aborting the deletion process.");
            }
            else{
                System.out.println("Account not deleted.");
            }

            

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    public static void EditMembers(Messenger esql, String user, String chatID)
    {
        try {
            int chat_id = Integer.parseInt(chatID);

            System.out.print("Would you like to \"add\" or \"remove\" members: ");
            String choice = in.readLine();

            String query = String.format("SELECT * FROM CHAT C WHERE C.chat_id='%s' AND C.init_sender='%s';", chat_id, user);
            int isInitSender = esql.executeQuery(query);
            if(isInitSender > 0)
            {
                if(choice.equalsIgnoreCase("add")){
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

                    if(numMembers > 2){
                        query = String.format("UPDATE CHAT SET chat_type = 'group' WHERE CHAT.chat_id='%s';", chat_id);
                        esql.executeUpdate(query);
                    }

                    System.out.println("Users added!");
                }
                else if(choice.equalsIgnoreCase("remove")){
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
                    
                    if(numMembers <= 2){
                        query = String.format("UPDATE CHAT SET chat_type = 'private' WHERE CHAT.chat_id='%s';", chat_id);
                        esql.executeUpdate(query);
                    }
                    System.out.println("Users removed!");
                }
                else{
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
    }//end

    public static void ViewMessages(Messenger esql, String user, String chatID)
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
    }//end

    public static void ListMembers(Messenger esql, String user, String chatID)
    {
        try {

            String query = String.format("SELECT CL.member FROM CHAT_LIST CL WHERE CL.chat_id='%s'", chatID);

            esql.executeQueryAndPrintResult(query);

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    //Returns true if chat still exists, chatViewer can continue running
    //Return false if chat is deleted, chatViewer needs to quit
    public static boolean DeleteChat(Messenger esql, String user, String chatID)
    {
        try {
            String query = String.format("SELECT * FROM CHAT C WHERE C.chat_id='%s' AND C.init_sender='%s';", chatID, user);
            int isInitSender = esql.executeQuery(query);

            if(isInitSender > 0){
                
                //Delete all messages
                query = String.format("DELETE FROM MESSAGE WHERE chat_id='%s'", chatID, user);
                esql.executeUpdate(query);

                //Delete all in chat_list
                query = String.format("DELETE FROM CHAT_LIST WHERE chat_id='%s'", chatID);
                esql.executeUpdate(query);

                //Delete from chat
                query = String.format("DELETE FROM CHAT WHERE chat_id='%s';", chatID, user);
                esql.executeUpdate(query);                
                System.out.println("Chat deleted!");
                return false;

            }else{
                System.out.println("You are not the chat owner!");
                return true;
            }

        } catch(Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }//end

    public static void SendNewMessage(Messenger esql, String user, String chatID)
    {
        try {
            System.out.print("\tEnter message text: ");
            String text = in.readLine();

            String query = String.format("INSERT INTO MESSAGE (msg_text, msg_timestamp, sender_login, chat_id) VALUES ('%s', NOW()::TIMESTAMP(0), '%s', '%s')", text, user, chatID);

            esql.executeUpdate(query);

            System.out.println("Message sent.");

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    public static void EditMessage(Messenger esql, String user, String chatID)
    {
        try {
            System.out.print("\t\"Edit\" or \"delete\" a message: ");
            String choice = in.readLine();

            if(choice.equalsIgnoreCase("edit")){
                System.out.print("\tEnter message id to edit: ");
                int msg_id = Integer.parseInt(in.readLine());

                String query = String.format("SELECT * FROM Message M WHERE M.sender_login='%s' AND M.msg_id='%s' AND M.chat_id='%s'", user, msg_id, chatID);
                List<List<String>> list = esql.executeQueryAndReturnResult(query);
                int msgExists = list.size();
                //int msgExists = esql.executeQuery(query);
                //int msgExists = executeQuery(query);

                if(msgExists > 0){
                    System.out.print("\tEnter new text: ");
                    String text = in.readLine();

                    //Delete old message
                    query = String.format("DELETE FROM MESSAGE WHERE sender_login='%s' AND msg_id='%s' AND chat_id='%s'", user, msg_id, chatID);
                    esql.executeUpdate(query);

                    //Insert modified message
                    query = String.format("INSERT INTO MESSAGE (msg_id, msg_text, msg_timestamp, sender_login, chat_id) VALUES ('%s', '%s', '%s', '%s', '%s');", msg_id, text, list.get(0).get(2), user, chatID);
                    esql.executeUpdate(query);

                    System.out.println("Message updated!");

                }else{
                    System.out.println("Invalid message id.");
                }


            }else if(choice.equalsIgnoreCase("delete")){
                System.out.print("\tEnter message id to delete: ");
                int msg_id = Integer.parseInt(in.readLine());

                String query = String.format("SELECT * FROM Message M WHERE M.sender_login='%s' AND M.msg_id='%s' AND M.chat_id='%s'", user, msg_id, chatID);
                List<List<String>> list = esql.executeQueryAndReturnResult(query);
                int msgExists = list.size();

                if(msgExists > 0){
                    query = String.format("DELETE FROM MESSAGE WHERE sender_login='%s' AND msg_id='%s' AND chat_id='%s'", user, msg_id, chatID);
                    esql.executeUpdate(query);
                    System.out.println("Message deleted!");

                }else{
                    System.out.println("Invalid message id.");
                }


            }else{
                System.out.println("Invalid choice.");
            }

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    public static void NewMessage(Messenger esql)
    {
      // Your code goes here.
      // ...
      // ...
    }//end 

    public static void Query6(Messenger esql)
    {
      // Your code goes here.
      // ...
      // ...
    }//end Query6
}//end Messenger
