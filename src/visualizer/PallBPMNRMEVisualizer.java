package visualizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import db.SQLiteJDBCDriverConnection;
import helper.CSVUtils;

public class PallBPMNRMEVisualizer {

	// SQLite connection string
	String url = "jdbc:sqlite:ExtractedInformations.db";
	String projectPath;

	// init DB
	SQLiteJDBCDriverConnection db = new SQLiteJDBCDriverConnection();

	// Constructor with rpoject path
	public PallBPMNRMEVisualizer(String projectPath) {
		this.projectPath = projectPath;
		createDirectory();
	}

	/**
	 * visualize all the data from the database and writes HTML and PDF files into
	 * the project director
	 */
	public void visualizeDB() {

		HTMLVisualizer htmlVisualizer = new HTMLVisualizer(projectPath);
		// creates all html files with the extracted data
		htmlVisualizer.writeToHTML("Role Model", getRoleModelFromDb());
		htmlVisualizer.writeToHTML("Role Model With ADR", getRoleModelADRFromDb());
		htmlVisualizer.writeToHTML("Acquire Device Resources", getADRAndRDRFromDb());
		htmlVisualizer.writeToHTML("Unique IS Permissions", getUniqueISPermissionsFromDb());
		htmlVisualizer.writeToHTML("Processes And Roles", getProcessesAndRolesFromDb());
		htmlVisualizer.writeToHTML("Access Control Requirement Mapping Model", getTraceabilityModelFromDb());
		htmlVisualizer.writeToHTML("Unique Business Permissions", getUniqueBusinessPermissionsFromDb());
		htmlVisualizer.writeToHTML("Counting", getCountingArrayList());
		htmlVisualizer.writeToHTML("ACRBreach", getACRBreachFromDb());

		// writes csv test files
		writeTraceabilityToCSV(getTraceabilityModelFromDb(), "AccessControlRequirementMappingModelTestTable");
		writeTraceabilityToCSV(getRoleModelADRFromDb(), "RoleModelWithADRTestTable");
	}

	/**
	 * Gets the role model from the database and returns an ArrayList of String[]
	 * 
	 * @return an list with all the values and added header for the visualization
	 */
	private ArrayList<String[]> getRoleModelFromDb() {
		ArrayList<String[]> roleModel = new ArrayList<String[]>();
		String[] roleModelHeader = { "Id", "Role", "BusinessPermission", "ISDataType" };
		roleModel.add(roleModelHeader);

		try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
			ResultSet rsRoleModel = stmt.executeQuery(
					"Select DISTINCT Role, BusinessPermission, ISDataType from ACRBP WHERE BusinessPermission != ''\n"
							+ "ORDER BY Role COLLATE NOCASE;");
			int tempi3 = 1;
			// extracts data from result set and adds it to array
			while (rsRoleModel.next()) {
				String[] roleModelArray = new String[] { String.valueOf(tempi3), rsRoleModel.getString("Role"),
						rsRoleModel.getString("BusinessPermission"), rsRoleModel.getString("ISDataType") };
				roleModel.add(roleModelArray);
				tempi3++;
			}
			System.out.println("#### Remove duplicate READ/WRITE permissions role model: ####");
			roleModel = removeDuplicateREADAndWRITEPermissions(roleModel);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return roleModel;
	}

	/**
	 * Gets the role model with ADR from the database and returns an ArrayList of
	 * String[]
	 * 
	 * @return an list with all the values and added header for the visualization
	 */
	private ArrayList<String[]> getRoleModelADRFromDb() {
		ArrayList<String[]> roleModelADR = new ArrayList<String[]>();
		String[] roleModelADRHeader = { "Id", "Role", "BusinessPermission", "ISDataType" };
		roleModelADR.add(roleModelADRHeader);

		try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
			ResultSet rsRoleModelADR = stmt.executeQuery(
					"Select DISTINCT Role, BusinessPermission, ISDataType from ACRBP WHERE BusinessPermission != ''\n"
							+ "UNION\n" + "SELECT DISTINCT ACRBP.Role, ADR_RDR_Permission, '' from ACRBP\n"
							+ "INNER JOIN ACRAcquireRelease on ACRAcquireRelease.ISActivityId = ACRBP.Id\n"
							+ "WHERE ADR_RDR_Permission LIKE '%Acquire%'\n" + "order by Role COLLATE NOCASE;");
			int tempi4 = 1;
			// extracts data from result set and adds it to array
			while (rsRoleModelADR.next()) {
				String[] roleModelADRArray = new String[] { String.valueOf(tempi4), rsRoleModelADR.getString("Role"),
						rsRoleModelADR.getString("BusinessPermission"), rsRoleModelADR.getString("ISDataType") };
				roleModelADR.add(roleModelADRArray);
				tempi4++;
			}
			System.out.println("#### Remove duplicate READ/WRITE permissions role model with ADR: ####");
			roleModelADR = removeDuplicateREADAndWRITEPermissions(roleModelADR);
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return roleModelADR;
	}

	/**
	 * Gets the unique IS permissions from the database and returns an ArrayList of
	 * String[]
	 * 
	 * @return an list with all the values and added header for the visualization
	 */
	private ArrayList<String[]> getUniqueISPermissionsFromDb() {
		ArrayList<String[]> uniqueISPermissions = new ArrayList<String[]>();
		String[] uniqueISPermissionsHeader = { "Id", "System", "ServiceCall", "ISPermission" };
		uniqueISPermissions.add(uniqueISPermissionsHeader);

		try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
			// gets the unique permissions from the db
			ResultSet rsuniqueISPermissions = stmt.executeQuery(
					"SELECT DISTINCT System, ServiceCall, ISPermission FROM ACRWorkflow WHERE System != '' AND ServiceCall != '' ORDER BY System COLLATE NOCASE;");
			int tempi1 = 1;
			// extracts data from result set and adds it to array
			while (rsuniqueISPermissions.next()) {
				String[] uniqueISPermissionsArray = new String[] { String.valueOf(tempi1),
						rsuniqueISPermissions.getString("System"), rsuniqueISPermissions.getString("ServiceCall"),
						rsuniqueISPermissions.getString("ISPermission") };
				uniqueISPermissions.add(uniqueISPermissionsArray);
				tempi1++;
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return uniqueISPermissions;
	}

	/**
	 * Gets the processes and roles from the database and returns an ArrayList of
	 * String[]
	 * 
	 * @return an list with all the values and added header for the visualization
	 */
	private ArrayList<String[]> getProcessesAndRolesFromDb() {
		ArrayList<String[]> processesAndRoles = new ArrayList<String[]>();
		String[] processesAndRolesHeader = { "Id", "Process", "Role" };
		processesAndRoles.add(processesAndRolesHeader);

		try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
			// gets the unique roles and processes from the db
			ResultSet rsprocessesAndRoles = stmt
					.executeQuery("SELECT DISTINCT Process, Role FROM ACRBP ORDER BY Process COLLATE NOCASE;");
			int tempi2 = 1;
			// extracts data from result set and adds it to array
			while (rsprocessesAndRoles.next()) {
				String[] processesAndRolesArray = new String[] { String.valueOf(tempi2),
						rsprocessesAndRoles.getString(1), rsprocessesAndRoles.getString(2) };
				processesAndRoles.add(processesAndRolesArray);
				tempi2++;
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return processesAndRoles;
	}

	/**
	 * Gets the ADR/RDR from the database and returns an ArrayList of String[]
	 * 
	 * @return an list with all the values and added header for the visualization
	 */
	private ArrayList<String[]> getADRAndRDRFromDb() {
		ArrayList<String[]> acquireDeviceResources = new ArrayList<String[]>();
		String[] acquireDeviceResourcesHeader = { "Id", "Role", "Acquire_Release_Name", "Acquire_Release_Permission" };
		acquireDeviceResources.add(acquireDeviceResourcesHeader);

		try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
			// gets the ADR/RDR
			ResultSet rsadr = stmt
					.executeQuery("SELECT DISTINCT Role, ADR_RDR_Name, ADR_RDR_Permission FROM ACRAcquireRelease\n"
							+ "INNER JOIN ACRBP A on ACRAcquireRelease.ISActivityId = A.Id ORDER BY Role COLLATE NOCASE");
			int i = 1;
			// extracts data from result set and adds it to array
			while (rsadr.next()) {
				acquireDeviceResources.add(new String[] { String.valueOf(i), rsadr.getString("Role"),
						rsadr.getString("ADR_RDR_Name"), rsadr.getString("ADR_RDR_Permission") });
				i++;
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return acquireDeviceResources;
	}

	/**
	 * Gets the traceability model from the database and returns an ArrayList of
	 * String[]
	 * 
	 * @return an list with all the values and added header for the visualization
	 */
	private ArrayList<String[]> getTraceabilityModelFromDb() {
		ArrayList<String[]> traceabilityDB = new ArrayList<String[]>();
		String[] traceabilityDBHeader = { "Id","IDBA", "Role", "Process", "BusinessActivity", "BusinessPermission",
				"ISDataType", "ISActivity", "System", "ServiceCall", "ISPermission" };
		traceabilityDB.add(traceabilityDBHeader);

		try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
			// gets the traceability model
			ResultSet rsTraceability = stmt.executeQuery("Select Id,\n" + "       Role,\n" + "       Process,\n"
					+ "       BusinessActivity,\n" + "       GroupedBusinessPermission as BusinessPermission,\n"
					+ "       GroupedISDataType as ISDataType,\n" + "       ifnull(ISActivity, '') as ISActivity,\n"
					+ "       ifnull(System, '') as System,\n" + "       ifnull(ServiceCall, '') as ServiceCall,\n"
					+ "       ifnull(ISPermission, '') as ISPermission\n" + "from (Select Id,\n"
					+ "             Role,\n" + "             Process,\n" + "             BusinessActivity,\n"
					+ "             GROUP_CONCAT(BusinessPermission) as GroupedBusinessPermission,\n"
					+ "             GROUP_CONCAT(ISDataType)         as GroupedISDataType\n" + "      from ACRBP\n"
					+ "      GROUP BY BusinessActivity, Process)\n"
					+ "         LEFT JOIN (Select DISTINCT ISActivityId,\n"
					+ "                                    ISActivity,\n"
					+ "                                    System,\n"
					+ "                                    ServiceCall,\n"
					+ "                                    ifnull(GROUP_CONCAT(ISPermission), '') as ISPermission\n"
					+ "                    from ACRWorkflow\n"
					+ "                    GROUP BY ACRWorkflow.ISActivityId, ISActivity) as A on A.ISActivityId = Id\n"
					+ "UNION ALL\n" + "Select ACRBP.Id,\n" + "       Role,\n" + "       Process,\n"
					+ "       BusinessActivity,\n" + "       ifnull(null, ''),\n" + "       ifnull(null, ''),\n"
					+ "       ifnull(null, ''),\n" + "       ifnull(null, ''),\n"
					+ "       GROUP_CONCAT(ADR_RDR_Name),\n" + "       GROUP_CONCAT(ADR_RDR_Permission)\n"
					+ "from ACRBP\n"
					+ "         INNER JOIN ACRAcquireRelease on ACRAcquireRelease.ISActivityId = ACRBP.Id\n"
					+ "GROUP BY ACRBP.Id\n" + "ORDER BY Id;");
			String m = "", k = "", l = "";
			int z = 0;
			int i = 0;
			// extracts data from result set and adds it to array
			while (rsTraceability.next()) {
				// changes from comma separated values to ordered list
				// if (temp.contains("Acquire") || temp.contains("Release")) {
				// temp = visualizeStringForDb(rsTraceability.getString(9));
				// }

				if (m.equals(rsTraceability.getString("Role")) && k.equals(rsTraceability.getString("Process"))
						&& l.equals(rsTraceability.getString("BusinessActivity"))) {

				} else {
					z++;
				}

				m = rsTraceability.getString("Role");
				k = rsTraceability.getString("Process");
				l = rsTraceability.getString("BusinessActivity");
				i++;
				String visualizedbP = visualizeStringForDb(rsTraceability.getString("BusinessPermission"));
				String visualizedISDataType = visualizeStringForDb(rsTraceability.getString("ISDataType"));
				String visualizedServiceCall = visualizeStringForDb(rsTraceability.getString("ServiceCall"));
				String visualizedISPermission = visualizeStringForDb(rsTraceability.getString("ISPermission"));
				traceabilityDB.add(new String[] {String.valueOf(i), String.valueOf(z), rsTraceability.getString("Role"),
						rsTraceability.getString("Process"), rsTraceability.getString("BusinessActivity"), visualizedbP,
						visualizedISDataType, rsTraceability.getString("ISActivity"),
						rsTraceability.getString("System"), visualizedServiceCall, visualizedISPermission });
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return traceabilityDB;
	}

	
	
	
	/**
	 * Gets the ACRBreach model from the database and returns an ArrayList of
	 * String[]
	 * 
	 * @return an list with all the values and added header for the visualization
	 */
	private ArrayList<String[]> getACRBreachFromDb() {
		ArrayList<String[]> acrBreachDB = new ArrayList<String[]>();
		String[] acrBreachDBHeader = { "Id","IDBA", "Role", "Process", "BusinessActivity", "BusinessPermission",
				"ISDataType", "ISActivity", "System", "ServiceCall", "READ_WRITTEN_DataType", "ACRBreach" };
		acrBreachDB.add(acrBreachDBHeader);

		try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
			// gets the traceability model
			ResultSet rsACRBreach = stmt.executeQuery("Select Id,\n" + 
					"       Role,\n" + 
					"       Process,\n" + 
					"       BusinessActivity,\n" + 
					"       GroupedBusinessPermission         as BusinessPermission,\n" + 
					"       GroupedISDataType                 as ISDataType,\n" + 
					"       ifnull(ISActivity, '')            as ISActivity,\n" + 
					"       ifnull(System, '')                as System,\n" + 
					"       ifnull(ServiceCall, '')           as ServiceCall,\n" + 
					"       ifnull(ISPermission, '')          as ISPermission,\n" + 
					"       ifnull(READ_WRITTEN_DataType, '') as READ_WRITTEN_DataType,\n" + 
					"       ifnull(ACRBreach, '')             as ACRBreach\n" + 
					"from (Select Id,\n" + 
					"             Role,\n" + 
					"             Process,\n" + 
					"             BusinessActivity,\n" + 
					"             GROUP_CONCAT(BusinessPermission) as GroupedBusinessPermission,\n" + 
					"             GROUP_CONCAT(ISDataType)         as GroupedISDataType\n" + 
					"      from ACRBP\n" + 
					"      GROUP BY BusinessActivity, Process)\n" + 
					"         LEFT JOIN (Select DISTINCT ISActivityId,\n" + 
					"                                    ISActivity,\n" + 
					"                                    System,\n" + 
					"                                    ServiceCall,\n" + 
					"                                    ifnull(GROUP_CONCAT(ISPermission), '') as ISPermission,\n" + 
					"                                    ELSC_Id\n" + 
					"                    from ACRWorkflow\n" + 
					"                    GROUP BY ACRWorkflow.ISActivityId, ISActivity) as A on A.ISActivityId = Id\n" + 
					"         LEFT JOIN (SELECT ELSC_Id,\n" + 
					"                           GROUP_CONCAT(READ_WRITTEN_DataType) as READ_WRITTEN_DataType,\n" + 
					"                           GROUP_CONCAT(ACRBreach)             as ACRBreach\n" + 
					"                    from (SELECT DISTINCT ELSC_Id, READ_WRITTEN_DataType, ACRBreach from ACRBreach)\n" + 
					"                    group by ELSC_Id) as E on E.ELSC_Id = A.ELSC_Id\n ORDER BY Id");
			String m = "", k = "", l = "";
			int z = 0;
			int i = 0;
			// extracts data from result set and adds it to array
			while (rsACRBreach.next()) {
				i++;
				if (m.equals(rsACRBreach.getString("Role")) && k.equals(rsACRBreach.getString("Process"))
						&& l.equals(rsACRBreach.getString("BusinessActivity"))) {

				} else {
					z++;
				}

				m = rsACRBreach.getString("Role");
				k = rsACRBreach.getString("Process");
				l = rsACRBreach.getString("BusinessActivity");

				String visualizedbP = visualizeStringForDb(rsACRBreach.getString("BusinessPermission"));
				String visualizedISDataType = visualizeStringForDb(rsACRBreach.getString("ISDataType"));
				String visualizedServiceCall = visualizeStringForDb(rsACRBreach.getString("ServiceCall"));
				String visualizedDataTypePermission = visualizeStringWithCharsForDb(rsACRBreach.getString("READ_WRITTEN_DataType"));
				String visualizedACRBreachPermission = visualizeStringWithCharsForDb(rsACRBreach.getString("ACRBreach"));

				acrBreachDB.add(new String[] {String.valueOf(i), String.valueOf(z), rsACRBreach.getString("Role"),
						rsACRBreach.getString("Process"), rsACRBreach.getString("BusinessActivity"), visualizedbP,
						visualizedISDataType, rsACRBreach.getString("ISActivity"),
						rsACRBreach.getString("System"), visualizedServiceCall, visualizedDataTypePermission, visualizedACRBreachPermission });
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return acrBreachDB;
	}
	
	
	
	
	
	/**
	 * Gets the unique business permissions from the database and returns an
	 * ArrayList of String[]
	 * 
	 * @return an list with all the values and added header for the visualization
	 */
	private ArrayList<String[]> getUniqueBusinessPermissionsFromDb() {
		ArrayList<String[]> uniqueBusinessPermissions = new ArrayList<String[]>();
		String[] uniqueBusinessPermissionsHeader = { "Id", "BusinessPermission", "ISDataType" };
		uniqueBusinessPermissions.add(uniqueBusinessPermissionsHeader);

		try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
			ResultSet rsuniqueBusinessPermissions = stmt.executeQuery(
					"SELECT DISTINCT BusinessPermission, ISDataType FROM ACRBP WHERE BusinessPermission != '' ORDER BY ISDataType COLLATE NOCASE;");
			int j = 1;
			// extracts data from result set and adds it to array
			while (rsuniqueBusinessPermissions.next()) {
				String[] uniqueBusinessPermissionsArray = new String[] { String.valueOf(j),
						rsuniqueBusinessPermissions.getString(1), rsuniqueBusinessPermissions.getString(2) };
				uniqueBusinessPermissions.add(uniqueBusinessPermissionsArray);
				j++;
			}
			System.out.println("#### Remove duplicate READ/WRITE permissions uniqueBusinessPermissions: ####");
			uniqueBusinessPermissions = removeDuplicateREADAndWRITEForUniqueBusinessPermissions(
					uniqueBusinessPermissions);
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return uniqueBusinessPermissions;
	}

	/**
	 * Gets counting for all relevant columns from the database and returns an
	 * ArrayList of String[]
	 * 
	 * @return an list with all the values and added header for the visualization
	 */
	private ArrayList<String[]> getCountingArrayList () {
		ArrayList<String[]> countingArrayList = new ArrayList<String[]>();
		String[] countingArrayListHeader = { "Anzahl Prozesse", "Anzahl Rollen", "Anzahl ActorSteps", "Anzahl ActorSteps mit Permissions", "Anzahl Business Permissions READ","Anzahl Business Permissions WRITE", "Anzahl ELSC", "Anzahl ADR", "Anzahl RDR" };
		countingArrayList.add(countingArrayListHeader);

		try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
			ResultSet countingResultSet = stmt.executeQuery(
					"SELECT COUNT(DISTINCT Process)                                                                 as Anzahl_Prozesse,\n" + 
					"       COUNT(DISTINCT Role)                                                                    as Anzahl_Rollen,\n" + 
					"       (SELECT COUNT(BusinessActivity)\n" + 
					"        from (SELECT DISTINCT Role, Process, BusinessActivity from ACRBP))                     as Anzahl_ActorSteps,\n" + 
					"       (SELECT COUNT(BusinessActivity)\n" + 
					"        FROM (SELECT DISTINCT Role, Process, BusinessActivity from ACRBP WHERE BusinessPermission != '')\n" + 
					"       )                                                                                       AS Anzahl_ActorSteps_mit_Permissions,\n" + 
					"       (SELECT COUNT(BusinessPermission)\n" + 
					"        FROM ACRBP\n" + 
					"        WHERE BusinessPermission LIKE '%READ%'\n" + 
					"       )                                                                                       AS Anzahl_BusinessPermissions_READ,\n" + 
					"       (SELECT COUNT(BusinessPermission)\n" + 
					"        FROM ACRBP\n" + 
					"        WHERE BusinessPermission LIKE '%WRITE%'\n" + 
					"       )                                                                                       AS Anzahl_BusinessPermissions_WRITE,\n" + 
					"       (SELECT COUNT(ISActivity)\n" + 
					"        FROM (SELECT DISTINCT ISActivityId, ISActivity FROM ACRWorkflow))\n" + 
					"                                                                                               AS Anzahl_ELSC,\n" + 
					"       (SELECT COUNT(ADR_RDR_Name) from ACRAcquireRelease WHERE ADR_RDR_Name LIKE '%Acquire%') as Anzahl_ADR,\n" + 
					"       (SELECT COUNT(ADR_RDR_Name)\n" + 
					"        from ACRAcquireRelease\n" + 
					"        WHERE ADR_RDR_Name LIKE '%Release%')                                                   as Anzahl_RDR\n" + 
					"from ACRBP;");
			// extracts data from result set and adds it to array
			while (countingResultSet.next()) {
				String[] countingArray = new String[] { 
						countingResultSet.getString("Anzahl_Prozesse"), countingResultSet.getString("Anzahl_Rollen"), countingResultSet.getString("Anzahl_ActorSteps"),
						countingResultSet.getString("Anzahl_ActorSteps_mit_Permissions"),countingResultSet.getString("Anzahl_BusinessPermissions_READ"), countingResultSet.getString("Anzahl_BusinessPermissions_WRITE"), countingResultSet.getString("Anzahl_ELSC"),countingResultSet.getString("Anzahl_ADR"),countingResultSet.getString("Anzahl_RDR")
				};
				countingArrayList.add(countingArray);
			}
			
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return countingArrayList;
	}

	/**
	 * Goes trough all permissions of roleModel and roleModelADR in the given list
	 * and checks for READ or WRITE or READ/WRITE elements and combines them and
	 * removes redundancies so that in the end there will be just 1 READ/WRITE or
	 * either a READ or WRITE element
	 * 
	 * @param resultList the list with the given permissions in the form: READ
	 *                   element, WRITE element, READ/WRITE element
	 * @return the new list with combined and removed duplicated elements
	 */
	private ArrayList<String[]> removeDuplicateREADAndWRITEPermissions(ArrayList<String[]> resultList) {
		ArrayList<String[]> tempArrayList = new ArrayList<String[]>();

		for (int i = 0; i < resultList.size(); i++) {
			boolean foundInTempArray = false;
			// goes through the new created temporary list and checks if element is already
			// in
			for (int j = 0; j < tempArrayList.size(); j++) {
				if (resultList.get(i)[1].equals(tempArrayList.get(j)[1])
						&& resultList.get(i)[3].equals(tempArrayList.get(j)[3])) {
					// checks if there is an empty space in the string
					if (tempArrayList.get(j)[2].contains(" ")) {
						String secondWordOfPermissionOriginal = resultList.get(i)[2]
								.substring(resultList.get(i)[2].indexOf(" ") + 1);
						String secondWordOfPermissionTemp = tempArrayList.get(j)[2]
								.substring(tempArrayList.get(j)[2].indexOf(" ") + 1);
						if (secondWordOfPermissionOriginal.equals(secondWordOfPermissionTemp)) {
							tempArrayList.get(j)[2] = "READ/WRITE " + secondWordOfPermissionTemp;
							foundInTempArray = true;
						}
					}
				}
			}
			if (!foundInTempArray) {
				tempArrayList.add(resultList.get(i));
			}
		}
		return tempArrayList;
	}

	/**
	 * Goes trough all permissions for uniqueBusinessPermissions in the given list
	 * and checks for READ or WRITE or READ/WRITE elements and combines them and
	 * removes redundancies so that in the end there will be just 1 READ/WRITE or
	 * either a READ or WRITE element
	 * 
	 * @param resultList the list with the given permissions in the form: READ
	 *                   element, WRITE element, READ/WRITE element
	 * @return the new list with combined and removed duplicated elements
	 */
	private ArrayList<String[]> removeDuplicateREADAndWRITEForUniqueBusinessPermissions(
			ArrayList<String[]> resultList) {
		ArrayList<String[]> tempArrayList = new ArrayList<String[]>();

		for (int i = 0; i < resultList.size(); i++) {
			boolean foundInTempArray = false;
			// goes through the new created temporary list and checks if element is already
			// in
			for (int j = 0; j < tempArrayList.size(); j++) {
				if (resultList.get(i)[2].equals(tempArrayList.get(j)[2])) {
					// checks if there is an empty space in the string
					if (tempArrayList.get(j)[1].contains(" ")) {
						String secondWordOfPermissionOriginal = resultList.get(i)[1]
								.substring(resultList.get(i)[1].indexOf(" ") + 1);
						String secondWordOfPermissionTemp = tempArrayList.get(j)[1]
								.substring(tempArrayList.get(j)[1].indexOf(" ") + 1);
						if (secondWordOfPermissionOriginal.equals(secondWordOfPermissionTemp)) {
							tempArrayList.get(j)[1] = "READ/WRITE " + secondWordOfPermissionTemp;
							foundInTempArray = true;
						}
					}
				}
			}
			if (!foundInTempArray) {
				tempArrayList.add(resultList.get(i));
			}
		}
		return tempArrayList;
	}

	/**
	 * Writes the traceability model from a ArrayList to a csv file in DataOutput
	 * folder
	 * 
	 * @param traceabilityList the traceability model ArrayList
	 * @param fileName         the name for the new file
	 */
	private void writeTraceabilityToCSV(ArrayList<String[]> traceabilityList, String fileName) {

		String csvFile = projectPath + "/DataOutput/" + fileName + ".csv";
		FileWriter writer;
		try {
			writer = new FileWriter(csvFile);
			for (int i = 0; i < traceabilityList.size(); i++) {
				List<String> list = new ArrayList<>();
				for (int j = 0; j < traceabilityList.get(i).length; j++) {
					list.add(traceabilityList.get(i)[j]);
				}
				CSVUtils.writeLine(writer, list, ',');
			}

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * gets and prints out the data for the ADRWithSurroundingASAndELSC
	 */
	public void visualizeConsolidatedDB() {

		HTMLVisualizer htmlConsolidateVisualizer = new HTMLVisualizer(projectPath);

		ArrayList<String[]> wholeConsolidatedDB = new ArrayList<String[]>();

		String[] wholeConsolidatedDBHeader = { "Id:", "Role:", "Process:", "ADR:", "BusinessActivity:",
				"BusinessPermission:", "ISDataType:", "ISActivity:", "System:", "ServiceCall:", "ISPermission:" };

		wholeConsolidatedDB.add(wholeConsolidatedDBHeader);

		String sql = "SELECT DISTINCT * FROM ADRWithSurroundingASAndELSC";

		try (Connection conn = DriverManager.getConnection(url);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			// loop through the result set
			int i = 1;
			while (rs.next()) {
				String id = String.valueOf(i);
				String role = rs.getString("Role");
				String process = rs.getString("Process");
				String adr = rs.getString("ADR");
				String businessActivity = rs.getString("BusinessActivity");
				String businessPermission = rs.getString("BusinessPermission");
				String isDataType = rs.getString("ISDataType");
				String isActivity = rs.getString("ISActivity");
				String system = rs.getString("System");
				String serviceCall = rs.getString("ServiceCall");
				String isPermission = rs.getString("ISPermission");

				String[] consolidatedArray = new String[] { id, role, process, adr,
						visualizeStringForDb(businessActivity), visualizeStringForDb(businessPermission),
						visualizeStringForDb(isDataType), visualizeStringForDb(isActivity),
						visualizeStringForDb(system), visualizeStringForDb(serviceCall),
						visualizeStringForDb(isPermission) };

				wholeConsolidatedDB.add(consolidatedArray);

				i++;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		htmlConsolidateVisualizer.writeToHTML("ADRWithSurroundingASAndELSC", wholeConsolidatedDB);
		writeTraceabilityToCSV(wholeConsolidatedDB, "ADRWithSurroundingASTestTable");

	}

	/**
	 * Converts a comma separated string to a ordered list with HTML breaks string
	 * 
	 * @param list a string with comma separated values
	 * @return the concatenated string containing ordered elements with numbers and
	 *         HTML breaks
	 */
	private String visualizeStringForDb(String list) {
		if (list != null && !list.isEmpty()) {
			String newList = "";
			String[] tempList = list.split(",");
			if (tempList.length > 1) {
				for (int i1 = 0; i1 < tempList.length; i1++) {
					newList += String.valueOf(i1 + 1) + ". " + tempList[i1] + "<br>";
				}
				return newList;
			}
		}
		return list;
	}
	
	
	private String visualizeStringWithCharsForDb(String list) {
		if (list != null && !list.isEmpty()) {
			String newList = "";
			String[] tempList = list.split(",");
			if (tempList.length > 1) {
				for (int i1 = 0; i1 < tempList.length; i1++) {
					newList += getCharForNumber(i1+1) + ": " + tempList[i1] + "<br>";
				}
				return newList;
			}
		}
		return list;
	}
	
	
	
	private String getCharForNumber(int i) {
	    return i > 0 && i < 27 ? String.valueOf((char)(i + 'A' - 1)) : null;
	}


	/**
	 * Creates the directory for the output files in the project directory
	 */
	private void createDirectory() {

		File rootDir = new File(projectPath);
		String directoryName = "DataOutput";
		File[] fileArray = rootDir.listFiles();
		Boolean dirExists = false;

		for (int i = 0; i < fileArray.length; i++) {
			File fileTemp = fileArray[i];
			if (fileTemp.isDirectory() == true) {
				if (fileTemp.getName().equals(directoryName) == true) {
					dirExists = true;
				}
			} else {
				continue;
			}
		}

		if (dirExists == false) {
			File dir = new File(rootDir + "/" + directoryName);
			if (dir.mkdir() == true) {
				System.out.println("Verzeichnis \"" + directoryName + "\" erstellt!");
			} else {
				System.out.println("Fehler beim erstellen des Verzeichnisses Aufgetreten!");
			}
		} else {
			System.out.println("Das Verzeichnis \"" + directoryName + "\"existiert bereits");
		}
	}

}
