package uk.co.v2systems.framework.database;

/**
 * Created by Pankaj Buchade on 23/06/2015.
 */
import uk.co.v2systems.framework.utils.Methods;
import java.sql.*;
import java.util.Properties;

//Supports both Oracle and SqLite connections
public class CustomSqlClient {
//oracle specific variables
    static String serverName;
    static String portNumber = "1525";
//sqLite specific variables
    static String dbFileName;
//Common variable
    static Connection conn = null;
    static String dbms;
    static String dbName;
    static String userName;
    static String password;
    static String queryString;
    static Statement statement;
    static ResultSet resultSet;
    static int rowCount=0;
    static ResultSetMetaData resultSetMetadata;

//Set Connection properties for Oracle DB Connection
    public void setConnectionDetails(String dbms, String serverName, String portNumber, String dbName, String userName, String password){
        this.dbms=dbms;
        this.serverName=serverName;
        this.portNumber = portNumber;
        this.dbName=dbName;
        this.userName=userName;
        this.password=password;
    }
//Set Connection properties for SQLite Connection
    public void setConnectionDetails(String dbms, String dbFileName){
        this.dbFileName =dbFileName;
        this.dbms=dbms;
    }
    public void getConnectionDetails(){
        //Common Details
        Methods.printConditional("\nDatabase: "+dbms);
        if(dbms.equalsIgnoreCase("oracle")){
            Methods.printConditional("\nHostName: "+serverName);
            Methods.printConditional("\nDB Port: "+portNumber);
        }
        if(dbms.equalsIgnoreCase("sqlite")){
            Methods.printConditional("\nDB File Name: "+dbFileName);
        }
            Methods.printConditional("\nDB Name: "+dbName);
            Methods.printConditional("\nDB UserName: "+userName);
            Methods.printConditional("\nPassword: "+password);
    }
//Establish Connection to Database
    public Connection connect(){
        try{
            if(dbms.equalsIgnoreCase("oracle")) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                Properties connectionProps = new Properties();
                connectionProps.put("user", this.userName);
                connectionProps.put("password", this.password);
                    conn = DriverManager.getConnection(
                            "jdbc:" + this.dbms + ":thin:@" +
                                    this.serverName +
                                    ":" + this.portNumber + ":" +
                                    this.dbName, connectionProps);
                Methods.printConditional("Connected to database: " + this.serverName + "::" + this.dbName);
            }
            if(dbms.equalsIgnoreCase("sqlite")){
                Class.forName("org.sqlite.JDBC");
                Properties connectionProps = new Properties();
                    conn = DriverManager.getConnection(
                            "jdbc:" + this.dbms + ":" +
                                    this.dbFileName, connectionProps );
                Methods.printConditional("Connected to sqlLite database File: " + this.dbFileName);
            }
            return conn;
        }catch(Exception e){
            System.out.println("Error While establishing sql connection...\n" +e);
            return null;
        }
    }
//Execute SQL query to connected Oracle Database
    public void executeQuery(String queryString){
        try {
            this.queryString=queryString;
            statement = conn.createStatement();
            boolean result= statement.execute(queryString);
            if(result){
               resultSet=statement.executeQuery(queryString);
               resultSetMetadata=resultSet.getMetaData();
            }
        }catch(SQLException e){
            System.out.println("Exception "+e+" in SqlClient.executeQuery");
        }
    }
//Print as well as return SQL query result
    public ResultSet getResultSet(){
        return getResultSet(true);
    }
//getResultSet can be controlled as verbose or non verbose also used by getRowCount
    public ResultSet getResultSet(boolean verbose){
        try{
            Methods.printConditional("\nSQL: " + this.queryString + "\n");
            int numberOfRows = 0;
            rowCount=0;
            //printing column Headers
            for(int i=0; i< resultSetMetadata.getColumnCount();i++) {
                Methods.printConditional(resultSetMetadata.getColumnLabel(i + 1) + "\t",verbose);
            }
            System.out.print("\n");
            //printing Result set rows
            while(resultSet.next()) {
                for(int i=0; i< resultSetMetadata.getColumnCount();i++) {
                    Methods.printConditional(resultSet.getString(resultSetMetadata.getColumnLabel(i + 1)) + "\t",verbose);
                }
                Methods.printConditional("\n\n",verbose); numberOfRows++;
            }
            System.out.println("Total rows returned: " + numberOfRows);
            this.rowCount = numberOfRows;
            this.executeQuery(queryString);
            return this.resultSet;
        }catch(SQLException e){
            System.out.println("Exception in SqlClient.getResultSet");
            return null;
        }
    }
//Close DB connection
    public void close(){
        try{
            statement.close();
            resultSet.close();
            conn.close();
        }catch(Exception e){
            System.out.println("Exception in SqlClient.close");
        }
    }
//Get row count, please note that you should execute show result before you use this function.
    public int getRowCount() {
        if(rowCount==0){
            getResultSet(false);
        }
        //Methods.printConditional(Integer.toString(rowCount));
        return rowCount;
    }

}
