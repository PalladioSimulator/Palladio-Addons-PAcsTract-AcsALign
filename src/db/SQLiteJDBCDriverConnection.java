package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLiteJDBCDriverConnection {
	
    // SQLite connection string
    String url = "jdbc:sqlite:ExtractedInformations.db";
	/**
     * Connect to the database
     */
    public void connectToTable() {
        Connection conn = null;
        try {
            // db parameters
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            
            System.out.println("Connection to ExtractedInformations.db has been established.");
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
    /**
     * Creates all necessary tables in the database for the extraction
     *
     */
    public void createTables() {
        
        
     // SQL statement for creating a new table
        String consolidatedPallBPMNRME = "CREATE TABLE ADRWithSurroundingASAndELSC (\n"
                + "	Id integer PRIMARY KEY,\n"
                + "	Role text,\n"
                + "	Process text,\n"
                + "	ADR text,\n"
                + "	BusinessActivity text,\n"
                + "	BusinessPermission text,\n"
                + "	ISDataType text,\n"
                + "	ISActivity text,\n"
                + "	System text,\n"
                + "	ServiceCall text,\n"
                + "	ISPermission text"
                + ");";
        
        // SQL statement for creating a new table
        String ACRBP = "CREATE TABLE ACRBP (\n"
                + "	Id integer PRIMARY KEY,\n"
                + "	Role text,\n"
                + "	Process text,\n"
                + "	BusinessActivity text,\n"
                + "	BusinessPermission text,\n"
                + "	ISDataType text,\n"
                + "	ISDataTypeId text"
                + ");";
        

        // SQL statement for creating a new table
        String ACRWorkflow = "CREATE TABLE ACRWorkflow (\n"
                + "	Id integer PRIMARY KEY,\n"
                + "	ISActivityId integer,\n"
                + "	ISActivity text,\n"
                + "	System text,\n"
                + "	ServiceCall text,\n"
                + "	ISPermission text,\n"
                + "	ELSC_Id text,\n"
                + " FOREIGN KEY (ISActivityId) REFERENCES ACRBP (Id)"
                + ");";
        
        
        // SQL statement for creating a new table
        String ACRAcquireRelease = "CREATE TABLE ACRAcquireRelease (\n"
                + "	Id integer PRIMARY KEY,\n"
                + "	ISActivityId integer,\n"
                + "	ADR_RDR_Name text,\n"
                + "	ADR_RDR_Permission text,\n"
                + " FOREIGN KEY (ISActivityId) REFERENCES ACRBP (Id)"
                + ");";
        
        
     // SQL statement for creating a new table
        String ACRBreach = "CREATE TABLE ACRBreach (\n"
                + "	Id integer PRIMARY KEY,\n"
                + "	ELSC_Id text,\n"
                + "	READ_WRITTEN_DataType text,\n"
                + "	ACRBreach text\n"
                + ");";
        
      
        //creates and replace existing databases
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute("DROP TABLE IF EXISTS ADRWithSurroundingASAndELSC");
            stmt.execute(consolidatedPallBPMNRME);
        	stmt.execute("DROP TABLE IF EXISTS ACRBP");
            stmt.execute(ACRBP);
        	stmt.execute("DROP TABLE IF EXISTS ACRWorkflow");
            stmt.execute(ACRWorkflow);
        	stmt.execute("DROP TABLE IF EXISTS ACRAcquireRelease");
            stmt.execute(ACRAcquireRelease);
            stmt.execute("DROP TABLE IF EXISTS ACRBreach");
            stmt.execute(ACRBreach);
            System.out.println("databases succesful created");
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
   
    
    /**
     * Inserts all given data into the ADRWithSurroundingASAndELSC db
     * @param key The name of the ADRA/RDRA
     * @param data The List of all necessary values for the db
     */
    public void insertADRWithSurroundingASAndELSCTable(String key, ArrayList<String> data) {
        String sql = "INSERT INTO ADRWithSurroundingASAndELSC(Role,Process,ADR,BusinessActivity,BusinessPermission,ISDataType,ISActivity,System,ServiceCall,ISPermission) VALUES(?,?,?,?,?,?,?,?,?,?)";
 
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, data.get(0));
            pstmt.setString(2, data.get(1));
            pstmt.setString(3, "ACQUIRE " + key);
            pstmt.setString(4, data.get(2));
            pstmt.setString(5, data.get(3));
            pstmt.setString(6, data.get(4));
            pstmt.setString(7, data.get(5));
            pstmt.setString(8, data.get(6));
            pstmt.setString(9, data.get(7));
            pstmt.setString(10, data.get(8));
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Inserts a given ArrayList into the ACRBP table
     * @param data the data to insert into the db
     */
    public void insertACRBPDataToTable(ArrayList<String> data) {
        String sql = "INSERT INTO ACRBP(Role,Process,BusinessActivity,BusinessPermission,ISDataType,ISDataTypeId) VALUES(?,?,?,?,?,?)";
 
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, data.get(0));
            pstmt.setString(2, data.get(1));
            pstmt.setString(3, data.get(2));
            pstmt.setString(4, data.get(3));
            pstmt.setString(5, data.get(4));
            pstmt.setString(6, data.get(5));
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Inserts a given ArrayList into the ACRWorkflow table
     * @param data the data to insert into the db
     */
    public void insertACRWorkflowDataToTable(ArrayList<String> data) {
        String sql = "INSERT INTO ACRWorkflow(ISActivityId,ISActivity,System,ServiceCall,ISPermission, ELSC_Id) VALUES(?,?,?,?,?,?)";
 
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, data.get(0));
            pstmt.setString(2, data.get(1));
            pstmt.setString(3, data.get(2));
            pstmt.setString(4, data.get(3));
            pstmt.setString(5, data.get(5));
            pstmt.setString(6, data.get(4));
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Inserts a given ArrayList into the ACRAcquireRelease table
     * @param data the data to insert into the db
     */
    public void insertACRAcquireReleaseDataToTable(ArrayList<String> data) {
        String sql = "INSERT INTO ACRAcquireRelease(ISActivityId,ADR_RDR_Name,ADR_RDR_Permission) VALUES(?,?,?)";
 
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, data.get(0));
            pstmt.setString(2, data.get(1));
            pstmt.setString(3, data.get(2));
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    
    /**
     * Inserts the given values into the ACRBreach table
     * @param ELSC_Id the id of the ELSC
     * @param DataType the Write or Read DataType
     * @param ACRBreach 
     */
    public void insertACRBreachDataToTable(String ELSC_Id, String DataType, String ACRBreach) {
        String sql = "INSERT INTO ACRBreach(ELSC_Id,READ_WRITTEN_DataType,ACRBreach) VALUES(?,?,?)";
 
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ELSC_Id);
            pstmt.setString(2, DataType);
            pstmt.setString(3, ACRBreach);
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    
    
    /**
     * Gets the Id for a Activity already in the db
     * @param ASName the name of the activity/Actor step
     * @return the id or 0 if not found
     */
    public Integer getISActivityId(String ASName, String process) {
        String sql = "Select Id from ACRBP Where BusinessActivity Like '" + ASName + "' AND Process LIKE '" + process + "'";
 
        try (Connection conn = DriverManager.getConnection(url);
        		Statement pstmt = conn.createStatement();
        		ResultSet rs = pstmt.executeQuery(sql)) {
        	while (rs.next()) {
        		return rs.getInt("Id");
        	}
            conn.close();
        } catch (SQLException e) {
        	System.out.println("Error getISActivityId: " + e.getStackTrace());
		}
		return 0;      		
    }
    
    /**
     * Builds a table from a given list of list of strings
     * @param rows the given table as a list of rows
     * @return The formated table
     */
    public static String formatAsTable(List<List<String>> rows)
    {
        int[] maxLengths = new int[rows.get(0).size()];
        for (List<String> row : rows)
        {
            for (int i = 0; i < row.size(); i++)
            {
                maxLengths[i] = Math.max(maxLengths[i], row.get(i).length());
            }
        }

        StringBuilder formatBuilder = new StringBuilder();
        for (int maxLength : maxLengths)
        {
            formatBuilder.append("%-").append(maxLength + 2).append("s");
        }
        String format = formatBuilder.toString();

        StringBuilder result = new StringBuilder();
        for (List<String> row : rows)
        {
            result.append(String.format(format, (Object[]) row.toArray(new String[0]))).append("\n");
        }
        return result.toString();
    }

}
