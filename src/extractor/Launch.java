package extractor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.pcm.repository.CollectionDataType;
import org.palladiosimulator.pcm.repository.CompositeDataType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.InnerDeclaration;
import org.palladiosimulator.pcm.repository.PrimitiveDataType;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import com.fasterxml.jackson.databind.ObjectMapper;

import db.SQLiteJDBCDriverConnection;
import de.uhd.ifi.se.pcm.bppcm.datamodel.CollectionDataObject;
import de.uhd.ifi.se.pcm.bppcm.datamodel.CompositeDataObject;
import de.uhd.ifi.se.pcm.bppcm.datamodel.DataModel;
import de.uhd.ifi.se.pcm.bppcm.datamodel.DataObject;
import helper.CSVUtils;
import model.RoleModelPermissionList;
import model.jsonSchema.DataTypeUsage;
import model.jsonSchema.DataTypeUsage_;
import model.jsonSchema.ReadDataType;
import model.jsonSchema.WriteDataType;
import visualizer.GraphVisualizer;
import visualizer.PallBPMNRMEVisualizer;
import test.CSVComparator;
import ui.LaunchConfigurationAttributes;

public class Launch extends LaunchConfigurationDelegate {


	List<String> listPoolIntbisLpPaths;

	String projectPath;
	String bpusagePaths;
	String orgaPath;
	String dataPath;
	String systemPath;
	String repoPath;
	String selectedTraceabilityReferenceFilePath = "";
	String selectedRoleModelWithADRReferenceFilePath = "";
	String selectedADRWithSurroundingReferenceFilePath = "";
	private String poolIntbisLpPaths;
	private String elscToAsPaths;
	private List<String> selectedACAPaths;
	private boolean generateDFDInformationSelection;
	private boolean selectedAcsAlignCheckbox;
	private String selectedReadWriteDatatypesFilePath;

	// inits the db, establishes a connection and creates the necessary tables
	SQLiteJDBCDriverConnection db = new SQLiteJDBCDriverConnection();


	/**
	 * The function that is executed after running the configuration to init the
	 * extraction process
	 */
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		projectPath = configuration.getAttribute(LaunchConfigurationAttributes.projectPath, "kein Pfad");


		// reads all necessary paths from the configuration
		bpusagePaths = configuration.getAttribute(LaunchConfigurationAttributes.selectedBpusageModel,
				"alternative default from initializeFrom()");
		orgaPath = configuration.getAttribute(LaunchConfigurationAttributes.selectedOrgaModel,
				"alternative default from initializeFrom()");
		dataPath = configuration.getAttribute(LaunchConfigurationAttributes.selectedDataModel,
				"alternative default from initializeFrom()");
		systemPath = configuration.getAttribute(LaunchConfigurationAttributes.selectedSystem,
				"alternative default from initializeFrom()");
		repoPath = configuration.getAttribute(LaunchConfigurationAttributes.selectedRepo,
				"alternative default from initializeFrom()");
		poolIntbisLpPaths = configuration.getAttribute(LaunchConfigurationAttributes.selectedPoolExtIntbiis_lp,
				"alternative default from initializeFrom()");
		elscToAsPaths = configuration.getAttribute(LaunchConfigurationAttributes.selectedElscToASExtIntbiis_lp,
				"alternative default from initializeFrom()");
		generateDFDInformationSelection = configuration
				.getAttribute(LaunchConfigurationAttributes.generateDFDInformation, false);
//		selectedACAPaths = configuration.getAttribute(LaunchConfigurationAttributes.selectedACA, selectedACAPaths);
		selectedTraceabilityReferenceFilePath = configuration
				.getAttribute(LaunchConfigurationAttributes.selectedTraceabilityReferenceFilePath, "");
		selectedRoleModelWithADRReferenceFilePath = configuration
				.getAttribute(LaunchConfigurationAttributes.selectedRoleModelWithADRReferenceFilePath, "");
		selectedADRWithSurroundingReferenceFilePath = configuration
				.getAttribute(LaunchConfigurationAttributes.selectedADRWithSurroundingReferenceFilePath, "");

		selectedAcsAlignCheckbox = configuration.getAttribute(LaunchConfigurationAttributes.selectedAcsAlignCheckbox,
				false);
		selectedReadWriteDatatypesFilePath = configuration
				.getAttribute(LaunchConfigurationAttributes.acrAlignJsonSelector, "");

		db.connectToTable();
		db.createTables();

		// inits the EMF reader
		EMFModelReader emfReader = new EMFModelReader();

		// Read all BpusagModels and extensions
		List<String> listBpusageModelPaths = Arrays.asList(bpusagePaths.split("\\s*,\\s*"));
		List<String> listPoolIntbisLpPaths = Arrays.asList(poolIntbisLpPaths.split("\\s*,\\s*"));
		List<String> listelscToAsPaths = Arrays.asList(elscToAsPaths.split("\\s*,\\s*"));

		// iterate through all BpusageModels
		for (int i = 0; i < listBpusageModelPaths.size(); i++) {

			Map<String, String> requiredPathsForExtractionList = new HashMap<String, String>();

			// add all paths to the map
			requiredPathsForExtractionList.put("projectPath", projectPath);
			requiredPathsForExtractionList.put("dataPath", dataPath);
			requiredPathsForExtractionList.put("systemPath", systemPath);
			if (!listelscToAsPaths.get(0).equals("Choose")) {
				requiredPathsForExtractionList.put("elscToAsPath", listelscToAsPaths.get(i));
			}
			requiredPathsForExtractionList.put("poolIntbisLpPath", listPoolIntbisLpPaths.get(i));

			// gets the Path of the BpusageModel and the projectPath and reads an UsageModel
			// with EMF reader
			String temp = listBpusageModelPaths.get(i).substring(listBpusageModelPaths.get(i).indexOf(File.separator),
					listBpusageModelPaths.get(i).length());
			UsageModel usageModel = (UsageModel) emfReader.getRessourceFromModel(projectPath + temp);

			System.out.println("####### BpusageModel #######");
			System.out.println("Usage Scenario: " + usageModel.getUsageScenario_UsageModel().get(0).getEntityName());
			System.out.println("Scenario Behavior: " + usageModel.getUsageScenario_UsageModel().get(0)
					.getScenarioBehaviour_UsageScenario().getEntityName());

			// extract the scenario behavior from usage model
			ScenarioBehaviour scenarioBehaviour = usageModel.getUsageScenario_UsageModel().get(0)
					.getScenarioBehaviour_UsageScenario();

			// init the PallBPMNRoleModelAnalyzer with the given paths
			PallBPMNRoleModelAnalyzer analyzer = new PallBPMNRoleModelAnalyzer(requiredPathsForExtractionList);

			// extract with the current scenario behavior and the database
			analyzer.extractModelsFromScenarioBehaviour(scenarioBehaviour, db, null);
		}

		// print the database in the console
		// db.printTable();

		// creates a copy of the original db in the DataOutput folder
		createBackupOfOriginalDb("TraceModel");
		// deletes unnecessary table
		deleteADRWithSuttoundingdTable();

		// if the checkbox for generating DFD information is selected the method is
		// called
		if (generateDFDInformationSelection) {
			generateDFDInformation();
		}

		// creates the permission graph and a csv file and all necessary data
		createPermissionGraph();

		// run all tests with the given reference files
		runTestsWithReferenceFiles();

		// runs the Palladio ACR Annotator and changes data in the given files
//		runPalladioACRAnnotator();

		//runs the acsAlignAlgorithm if the checkbox is selected
		if (selectedAcsAlignCheckbox) {
			AcsAlign acsAlign = new AcsAlign(projectPath, dataPath, repoPath, selectedReadWriteDatatypesFilePath);
			acsAlign.startAcsAlign();
		}

		// inits the visualizer and calls the functions to visualize from the 3
		// databases
		PallBPMNRMEVisualizer visualizer = new PallBPMNRMEVisualizer(projectPath);
		visualizer.visualizeDB();
		visualizer.visualizeConsolidatedDB();
	}
	
	
	

	





	@Deprecated
	private DataType getDataTypeFromName(String name) {
		EMFModelReader emfReader = new EMFModelReader();
		Repository repository = (Repository) emfReader
				.getRessourceFromModel(projectPath + repoPath.substring(repoPath.indexOf(File.separator)));
		EList<DataType> dataTypes = repository.getDataTypes__Repository();

		for (DataType dataType : dataTypes) {
			String dataTypeNameString = dataType.getRepository__DataType().getEntityName();
			if (dataTypeNameString.equals(name)) {
				return dataType;
			}
		}
		return null;
	}



	@Deprecated
	@SuppressWarnings("unchecked")
	private <T extends DataType> T castDataType(DataType type) {
		if (type instanceof CompositeDataType) {
			return (T) (CompositeDataType) type;
		} else {
			return (T) (CollectionDataType) type;
		}
	}

	/**
	 * generates the DFD Information in a new db
	 */
	private void generateDFDInformation() {
		createBackupOfOriginalDb("DFDInformation");
		deleteUnnecessaryTables();
	}

	/**
	 * runs the palladio ACR Annotator to write data persistent to the models
	 */
	private void runPalladioACRAnnotator() {
		if (!selectedACAPaths.get(0).equals("Choose")) {
			PalladioACRAnnotator palladioACRAnnotator = new PalladioACRAnnotator(projectPath, selectedACAPaths);
			palladioACRAnnotator.writeACA();
		}
	}

	/**
	 * Runs tests to compare the newly created files against reference files
	 */
	private void runTestsWithReferenceFiles() {
		// run Test if reference file was selected
		if (!selectedTraceabilityReferenceFilePath.isEmpty()) {
			CSVComparator ModelCSVComparison = new CSVComparator(projectPath + File.separator + "DataOutput"
					+ File.separator + "AccessControlRequirementMappingModelTestTable.csv",
					selectedTraceabilityReferenceFilePath);
			System.out.println("####################################");
			System.out.println("Test results for traceability model:");
			ModelCSVComparison.test();
		}
		// run Test if reference file was selected
		if (!selectedRoleModelWithADRReferenceFilePath.isEmpty()) {
			CSVComparator ModelCSVComparison = new CSVComparator(
					projectPath + File.separator + "DataOutput" + File.separator + "RoleModelWithADRTestTable.csv",
					selectedRoleModelWithADRReferenceFilePath);
			System.out.println("########################################");
			System.out.println("Test results for roleModelWithADR model:");
			ModelCSVComparison.test();
		}
		// run Test if reference file was selected
		if (!selectedADRWithSurroundingReferenceFilePath.isEmpty()) {
			CSVComparator ModelCSVComparison = new CSVComparator(
					projectPath + File.separator + "DataOutput" + File.separator + "ADRWithSurroundingASTestTable.csv",
					selectedADRWithSurroundingReferenceFilePath);
			System.out.println("############################################");
			System.out.println("Test results for ADRWithSurroundingAS model:");
			ModelCSVComparison.test();
		}
	}

	/**
	 * Creates the hierarchy graph (html file) for roles and their permissions and
	 * creates a csv file with the data
	 */
	private void createPermissionGraph() {
		// init for permission List graph
		ArrayList<RoleModelPermissionList> permissionList = new ArrayList<RoleModelPermissionList>();

		// connects to the database and gets all the necessary information for the
		// hierarchy graph
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:ExtractedInformations.db");
				Statement stmt = conn.createStatement()) {
			ResultSet combinedPermissions = stmt
					.executeQuery("Select DISTINCT Role, GROUP_CONCAT(DISTINCT BusinessPermission) from ACRBP\n"
							+ "							GROUP BY Role\n"
							+ "							ORDER BY Role;");
			while (combinedPermissions.next()) {
				if (!combinedPermissions.getString(2).isEmpty()) {
					String[] permissions = combinedPermissions.getString(2).split(",");
					permissionList.add(new RoleModelPermissionList(combinedPermissions.getString(1), permissions));
				} else {
					permissionList.add(new RoleModelPermissionList(combinedPermissions.getString(1), new String[0]));
				}
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		permissionList = removeDuplicateREADAndWRITEPermissions(permissionList);

		// gets all the inheritance inside the extracted roles and their permissions and
		// writes it to the RoleModelPermissionList
		for (int i = 0; i < permissionList.size(); i++) {
			String role = permissionList.get(i).getRole();
			String[] permissions = permissionList.get(i).getPermissions();
			ArrayList<String> inheritFrom = new ArrayList<>();
			System.out.println(role + " " + String.join(",", permissions));
			for (int j = 0; j < permissionList.size(); j++) {
				if (i != j) {
					String tempRole = permissionList.get(j).getRole();
					String[] temppermissions = permissionList.get(j).getPermissions();
					if (temppermissions.length == 0) {
						inheritFrom.add(tempRole);
					} else {
						if (subset_arrays(permissions, temppermissions)) {
							inheritFrom.add(tempRole);
						}
					}
				}
			}
			permissionList.get(i).setInheritFrom(inheritFrom);
		}

		// prints all permissions and inheritances for debugging
		for (int i = 0; i < permissionList.size(); i++) {
			System.out.println(permissionList.get(i).print());
		}

		// writes the data into the permission graph
		GraphVisualizer gv = new GraphVisualizer(projectPath);
		gv.writeToHTML("RolePermissionsGraph", permissionList);

		// writes the permission data into a csv file
		String csvFile = projectPath + File.separator + "DataOutput" + File.separator + "roleInheritFrom.csv";
		FileWriter writer;
		try {
			writer = new FileWriter(csvFile);

			// for header
			CSVUtils.writeLine(writer, Arrays.asList("Rolle", "Berechtigungen", "Erbt_von"), ';');

			for (int i = 0; i < permissionList.size(); i++) {
				List<String> list = new ArrayList<>();
				list.add(permissionList.get(i).getRole());
				list.add(Arrays.toString(permissionList.get(i).getPermissions()));
				list.add(String.join(",", permissionList.get(i).getInheritFrom()));
				CSVUtils.writeLine(writer, list, ';');
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the arr2 is a subset of arr1
	 * 
	 * @param arr1
	 * @param arr2
	 * @return if the arr2 is a subset of arr1
	 */
	static boolean subset_arrays(String arr1[], String arr2[]) {
		int i, j = 0;
		for (i = 0; i < arr2.length; i++) {
			for (j = 0; j < arr1.length; j++) {
				if (arr2[i].equals(arr1[j]))
					break;
			}
			if (j == arr1.length)
				return false;
		}
		return true;
	}

	/**
	 * Creates a Backup of the in memory database in the DataOutput folder
	 */
	private void createBackupOfOriginalDb(String fileName) {
		try {
			Connection conn = DriverManager.getConnection("jdbc:sqlite:ExtractedInformations.db");
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(
					"backup to '" + projectPath + File.separator + "DataOutput" + File.separator + fileName + ".db'");
			stmt.close();
			conn.close();
			System.out.println("Backup erstellt");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes all unnecessary Tables (ACRWorkflow, ADRWithSurroundingASAndELSC) for
	 * the DFD Information
	 */
	private void deleteUnnecessaryTables() {
		try {
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + projectPath + File.separator + "DataOutput"
					+ File.separator + "DFDInformation.db");
			Statement stmt = conn.createStatement();
			stmt.execute("DROP TABLE IF EXISTS ACRWorkflow");
			stmt.execute("DROP TABLE IF EXISTS ADRWithSurroundingASAndELSC");
			stmt.close();
			conn.close();
			System.out.println("Tables ACRWorkflow and ADRWithSurroundingASAndELSC deleted");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes all unnecessary Tables (ACRWorkflow, ADRWithSurroundingASAndELSC) for
	 * the DFD Information
	 */
	private void deleteADRWithSuttoundingdTable() {
		try {
			Connection conn = DriverManager.getConnection(
					"jdbc:sqlite:" + projectPath + File.separator + "DataOutput" + File.separator + "TraceModel.db");
			Statement stmt = conn.createStatement();
			stmt.execute("DROP TABLE IF EXISTS ADRWithSurroundingASAndELSC");
			stmt.close();
			conn.close();
			System.out.println("Table ADRWithSurroundingASAndELSC deleted");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes duplicate READ or WRITE permissions and combines them to READ/WRITE
	 * permissions
	 * 
	 * @param permissionList an ArrayList with the type RoleModelPermissionList
	 *                       containing the permissions
	 * @return the cleaned list with the combined elements
	 */
	private ArrayList<RoleModelPermissionList> removeDuplicateREADAndWRITEPermissions(
			ArrayList<RoleModelPermissionList> permissionList) {
		for (RoleModelPermissionList list : permissionList) {
			ArrayList<String> tempArrayList = new ArrayList<String>();

			ArrayList<String> permissions = new ArrayList<String>(Arrays.asList(list.getPermissions()));
			for (int i = 0; i < permissions.size(); i++) {
				boolean foundInTempArray = false;
				for (int j = 0; j < tempArrayList.size(); j++) {
					if (tempArrayList.get(j).contains(" ")) {
						String secondWordOfPermissionOriginal = permissions.get(i)
								.substring(permissions.get(i).indexOf(" ") + 1);
						String secondWordOfPermissionTemp = tempArrayList.get(j)
								.substring(tempArrayList.get(j).indexOf(" ") + 1);
						if (secondWordOfPermissionOriginal.equals(secondWordOfPermissionTemp)) {
							tempArrayList.set(j, "READ/WRITE " + secondWordOfPermissionTemp);
							foundInTempArray = true;
						}
					}
				}
				if (!foundInTempArray) {
					tempArrayList.add(permissions.get(i));
				}
			}
			list.setPermissions(tempArrayList.toArray(new String[tempArrayList.size()]));
		}
		return permissionList;
	}

}
