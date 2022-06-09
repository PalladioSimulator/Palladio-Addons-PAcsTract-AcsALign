package extractor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.pcm.repository.CollectionDataType;
import org.palladiosimulator.pcm.repository.CompositeDataType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.InnerDeclaration;
import org.palladiosimulator.pcm.repository.PrimitiveDataType;
import org.palladiosimulator.pcm.repository.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import db.SQLiteJDBCDriverConnection;
import de.uhd.ifi.se.pcm.bppcm.datamodel.CollectionDataObject;
import de.uhd.ifi.se.pcm.bppcm.datamodel.CompositeDataObject;
import de.uhd.ifi.se.pcm.bppcm.datamodel.DataModel;
import de.uhd.ifi.se.pcm.bppcm.datamodel.DataObject;
import model.jsonSchema.DataTypeUsage;
import model.jsonSchema.DataTypeUsage_;
import model.jsonSchema.ReadDataType;
import model.jsonSchema.WriteDataType;

public class AcsAlign {

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");
	}

	// inits the db, establishes a connection and creates the necessary tables
	SQLiteJDBCDriverConnection db = new SQLiteJDBCDriverConnection();

	String projectPath;
	String dataPath;
	String repoPath;
	String selectedReadWriteDatatypesFilePath;
	// init logging
	Logger logger = Logger.getLogger("MyLog");
	FileHandler fh;
	

	public AcsAlign(String projectPath, String dataPath, String repoPath, String selectedReadWriteDatatypesFilePath) {
		this.projectPath = projectPath;
		this.dataPath = dataPath;
		this.repoPath = repoPath;
		this.selectedReadWriteDatatypesFilePath = selectedReadWriteDatatypesFilePath;
		
		try {
			// This block configure the logger with handler and formatter
			fh = new FileHandler(projectPath + File.separator + "DataOutput" + File.separator + "AcsAlignAnalysis.log");
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			fh.setEncoding(StandardCharsets.UTF_8.toString());
			logger.addHandler(fh);

			// to remove the console output toggle comment
			// logger.setUseParentHandlers(false);

			// the following statement is used to log any messages
			logger.info("### Logger init ###");

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startAcsAlign() {
		// builds the hashMap for dKnown (<DataTypeName, dataType>)
		HashMap<String, DataType> dKnown = getAllDataTypesFromDataModel();

		// builds the hashMap for allDAllowed (<ELSCId.Process, dAllowed>)
		HashMap<String, Set<String>> allDAllowed = getAllowedDataTypeStringsFromDB();

		// iterates over all dAllowed sets
		for (String key : allDAllowed.keySet()) {
			String elscId = key.substring(0, key.indexOf("."));
			String processNameString = key.substring(key.indexOf(".") + 1, key.length());
			Set<String> dAllowed = allDAllowed.get(key);
			logger.info("");
			logger.info("########## " + processNameString + " ##########");

			HashMap<String, Set<DataType>> combinedReadAndWriteDatatTypes = new HashMap<String, Set<DataType>>();
			combinedReadAndWriteDatatTypes = getReadWriteDataTypesFromJSON(elscId);

			// iterates over all extracted read and write data types from the json file
			for (String dataTypekey : combinedReadAndWriteDatatTypes.keySet()) {
				for (DataType readWriteDataType : combinedReadAndWriteDatatTypes.get(dataTypekey)) {
					if (readWriteDataType != null) {
						// runs the acr breach algorithm for each data type and sets its permission
						boolean acrBreach = acsAlignAlgorithm(readWriteDataType, dKnown, dAllowed, dataTypekey, elscId);

						// gets the name of the data type for writing it into the db
						String readWriteName = null;
						if (readWriteDataType instanceof CompositeDataType) {
							CompositeDataType compositeDataType = (CompositeDataType) readWriteDataType;
							readWriteName = compositeDataType.getEntityName();
						} else if (readWriteDataType instanceof CollectionDataType) {
							CollectionDataType collectionDataType = (CollectionDataType) readWriteDataType;
							readWriteName = collectionDataType.getEntityName();
						}
						// sets the final permission string
						String acrAllowed = "FORBIDDEN";
						if (acrBreach) {
							acrAllowed = "ALLOWED";
						}
						// writes acrBreach data to db
						db.insertACRBreachDataToTable(elscId, dataTypekey + " " + readWriteName, acrAllowed);
					}
				}
			}
		}
	}

	/**
	 * acs align algorithm extracts a boolean if a data type is allowed or not
	 * 
	 * @param dataType    the data type to check if it is allowed
	 * @param dKnown      all known data types from the repository
	 * @param dAllowed    all allowed data types from the traceability db
	 * @param readOrWrite string for READ or WRITE for the given data type
	 * @param elscId      the elsc id for the data type
	 * @return boolean if data type is allowed or not
	 */
	private boolean acsAlignAlgorithm(DataType dataType, HashMap<String, DataType> dKnown, Set<String> dAllowed,
			String readOrWrite, String elscId) {
		String primitiveDataTypeName = "";

		boolean fallback = true;
		boolean allowed = false;
		String dataTypeName = "";
		String dataTypeId = "";
		String dataTypeShort = "";

		// checks for dataType of dataType, casts it to it and sets the string values
		// for id and name
		if (dataType instanceof CompositeDataType) {
			CompositeDataType compositeDataType = (CompositeDataType) dataType;
			dataTypeName = compositeDataType.getEntityName();
			dataTypeId = compositeDataType.getId();
			dataTypeShort = "(Comp)";
		} else if (dataType instanceof CollectionDataType) {
			CollectionDataType collectionDataType = (CollectionDataType) dataType;
			dataTypeName = collectionDataType.getEntityName();
			dataTypeId = collectionDataType.getId();
			dataTypeShort = "(Coll)";
		} else if (dataType instanceof PrimitiveDataType) {
			PrimitiveDataType primitiveDataType = (PrimitiveDataType) dataType;
			dataTypeId = primitiveDataType.getType().getName();
			primitiveDataType.getRepository__DataType().getDataTypes__Repository();
			dataTypeName = "(" + dataTypeId + ") " + primitiveDataTypeName;
		}

		String dAllowedString = readOrWrite + " " + dataTypeId + " " + dataTypeShort;

		// checks if in dAllowed
		boolean isInDAllowed = dAllowed.contains(dAllowedString);

		// first part of algorithm, checks if not in dKnown or in dAllowed and in dKNown
		// or fallback
		allowed = (!dKnown.containsKey(dataTypeName) || isInDAllowed) && (dKnown.containsKey(dataTypeName) || fallback);

		// logs result of first algorithm part
		logger.info("R/W DataType " + dataTypeName + ": dAllowed= " + dAllowed.contains(dAllowedString) + "; dKnown= "
				+ dKnown.containsKey(dataTypeName) + "; allowed= " + allowed);

		// if allowed and composite get inner data type and call function recursive
		if (allowed && dataType instanceof CompositeDataType) {
			CompositeDataType compositeDataType = (CompositeDataType) dataType;

			// iterates over all inner composite data types
			for (InnerDeclaration innerCompositeDataype : compositeDataType.getInnerDeclaration_CompositeDataType()) {
				logger.info("------------------------------------ Analyze inner composite datatype");
				DataType InnerDeclaration = innerCompositeDataype.getDatatype_InnerDeclaration();
				if (InnerDeclaration instanceof PrimitiveDataType) {
					primitiveDataTypeName = innerCompositeDataype.getEntityName();
					logger.info("Primitive DataType Name: " + primitiveDataTypeName);
				}
				// recursive call
				boolean recursiveResult = acsAlignAlgorithm(InnerDeclaration, dKnown, dAllowed, readOrWrite, elscId);
				// sets allowed to allowed from frist part and result from recursive call
				allowed = allowed && recursiveResult;
			}
		}
		// if allowed and collection and not in dKnown -> allowed depends if inner
		// collection is allowed
		if (allowed && dataType instanceof CollectionDataType && !dKnown.containsKey(dataTypeName)) {
			CollectionDataType collectionDataType = (CollectionDataType) dataType;
			String innerCollectionTypeString = ((CompositeDataType) collectionDataType
					.getInnerType_CollectionDataType()).getEntityName();

			// allowed if allowed from first part and inner collection not in dKnown
			allowed = allowed && !dKnown.containsKey(innerCollectionTypeString);
			logger.info("Analyze inner composite datatype - !dKnown(Collection inner type): "
					+ !dKnown.containsKey(innerCollectionTypeString) + "; Inner type allowed: " + allowed);
		}
		// sets the final result string
		String acrAllowed = "FORBIDDEN";
		if (allowed) {
			acrAllowed = "ALLOWED";
		}

		logger.info("R/W DataType " + dataTypeName + ": Final allowed= " + acrAllowed);
		return allowed;
	}


	/**
	 * Gets the read and write data types from the json file for an elsc id
	 * 
	 * @param elscId
	 * @return a hash map with the elsc id as key and a set with the containing data
	 *         types
	 */
	private HashMap<String, Set<DataType>> getReadWriteDataTypesFromJSON(String elscId) {

		ObjectMapper objectMapper = new ObjectMapper();
		HashMap<String, Set<DataType>> combinedReadAndWriteDatatTypes = new HashMap<String, Set<DataType>>();

		try {
			// maps the json file to the objects and reads its data
			DataTypeUsage example = objectMapper.readValue(
					new FileReader(projectPath + selectedReadWriteDatatypesFilePath
							.substring(selectedReadWriteDatatypesFilePath.indexOf(File.separator))),
					DataTypeUsage.class);

			// get list of all data type usages
			List<DataTypeUsage_> readWriteDataTypes = example.getDataTypeUsage();

			Set<DataType> readDataTypeList = new HashSet<>();
			Set<DataType> writeDataTypeList = new HashSet<>();

			String elscNameString = "";
			// iterates over all read and write data types from the json and extracts their
			// entries
			for (DataTypeUsage_ readWriteDataType : readWriteDataTypes) {

				String elscIdString = readWriteDataType.getEntryLevelSystemCall().getId();

				// if the given elsc id matches the one from the json file extract the inner
				// data
				if (elscId.equals(elscIdString)) {
					if (!elscNameString.equals(readWriteDataType.getEntryLevelSystemCall().getName())) {
						logger.info("### EntryLevelSystemCall: " + readWriteDataType.getEntryLevelSystemCall().getName()
								+ " ###");
						elscIdString = readWriteDataType.getEntryLevelSystemCall().getName();
					}
					List<ReadDataType> readDataTypesList = readWriteDataType.getReadDataTypes();
					for (ReadDataType innerReadDataType : readDataTypesList) {
						readDataTypeList.add(getDataTypeFromId(innerReadDataType.getId()));
					}
					List<WriteDataType> writeDataTypesList = readWriteDataType.getWriteDataTypes();
					for (WriteDataType innerWriteDataType : writeDataTypesList) {
						writeDataTypeList.add(getDataTypeFromId(innerWriteDataType.getId()));
					}
				}
			}
			// combines read an writes lists together
			combinedReadAndWriteDatatTypes.put("READ", readDataTypeList);
			combinedReadAndWriteDatatTypes.put("WRITE", writeDataTypeList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return combinedReadAndWriteDatatTypes;
	}

	/**
	 * get all allowed data types ids from the ACRBP table (ISDataTypeId)
	 * 
	 * @return a hash map with the elsc id and their corresponding data types as ids
	 */
	private HashMap<String, Set<String>> getAllowedDataTypeStringsFromDB() {
		HashMap<String, Set<String>> extractedDataTypesFromDb = new HashMap<>();

		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:ExtractedInformations.db");
				Statement stmt = conn.createStatement()) {
			ResultSet dataTypesFromDB = stmt.executeQuery("SELECT Process,\n" + "       Role,\n"
					+ "       BusinessActivity,\n" + "       BusinessPermission,\n" + "       ISDataType,\n"
					+ "       ISDataTypeId,\n" + "       ISActivity,\n" + "       ELSC_Id\n" + "from ACRWorkflow\n"
					+ "         JOIN (SELECT Id,\n" + "                           Role,\n"
					+ "                           Process,\n" + "                           BusinessActivity,\n"
					+ "                           GROUP_CONCAT(BusinessPermission) AS BusinessPermission,\n"
					+ "                           GROUP_CONCAT(ISDataType) AS ISDataType,\n"
					+ "                           GROUP_CONCAT(ISDataTypeId) AS ISDataTypeId\n"
					+ "                    FROM ACRBP\n" + "                    WHERE ISDataType != ''\n"
					+ "                    GROUP BY BusinessActivity) A on ACRWorkflow.ISActivityId = A.Id\n"
					+ "GROUP BY Process, Role, BusinessActivity, ELSC_Id;");
			while (dataTypesFromDB.next()) {
				System.out.println(
						dataTypesFromDB.getString("ELSC_Id") + ": " + dataTypesFromDB.getString("ISDataTypeId"));
				extractedDataTypesFromDb.put(
						dataTypesFromDB.getString("ELSC_Id") + "." + dataTypesFromDB.getString("Process"),
						new HashSet<>(Arrays.asList(dataTypesFromDB.getString("ISDataTypeId").split(","))));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return extractedDataTypesFromDb;
	}

	/**
	 * Extracts the first dataType from EMF data model and puts them to a hash map
	 * with their name as key
	 * 
	 * @return
	 */
	private HashMap<String, DataType> getAllDataTypesFromDataModel() {
		HashMap<String, DataType> extractetDataTypeSet = new HashMap<>();
		// inits the EMF reader for reading all models
		EMFModelReader emfReader = new EMFModelReader();
		DataModel dataModel = (DataModel) emfReader
				.getRessourceFromModel(projectPath + dataPath.substring(dataPath.indexOf(File.separator)));
		EList<DataObject<?>> dataModels = dataModel.getDataObjects();
		for (DataObject<?> dataObject : dataModels) {
			if (dataObject instanceof CollectionDataObject) {
				CollectionDataObject collectionDataObject = (CollectionDataObject) dataObject;
				extractetDataTypeSet.put(collectionDataObject.getEntityName(),
						collectionDataObject.getDataTypes().get(0));
			} else if (dataObject instanceof CompositeDataObject) {
				CompositeDataObject compositeDataObject = (CompositeDataObject) dataObject;
				extractetDataTypeSet.put(compositeDataObject.getEntityName(),
						compositeDataObject.getDataTypes().get(0));
			}
		}
		return extractetDataTypeSet;
	}

	/**
	 * gets the data type to a given id from the repository
	 * @param id the data type string
	 * @return the data type extracted from the repository model
	 */
	private DataType getDataTypeFromId(String id) {
		EMFModelReader emfReader = new EMFModelReader();
		Repository repository = (Repository) emfReader
				.getRessourceFromModel(projectPath + repoPath.substring(repoPath.indexOf(File.separator)));
		EList<DataType> dataTypes = repository.getDataTypes__Repository();

		for (DataType dataType : dataTypes) {
			String idString;
			if (dataType instanceof CompositeDataType) {
				CompositeDataType castedDataType = (CompositeDataType) dataType;
				idString = castedDataType.getId();
			} else {
				CollectionDataType castedDataType = (CollectionDataType) dataType;
				idString = castedDataType.getId();
			}
			if (idString.equals(id)) {
				return dataType;
			}
		}
		return null;
	}
	
}
