package extractor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.pcm.repository.CollectionDataType;
import org.palladiosimulator.pcm.repository.CompositeDataType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.PrimitiveDataType;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.Loop;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;

import db.SQLiteJDBCDriverConnection;
import de.uhd.ifi.se.pcm.bppcm.bpusagemodel.AcquireDeviceResourceAction;
import de.uhd.ifi.se.pcm.bppcm.bpusagemodel.ActorStep;
import de.uhd.ifi.se.pcm.bppcm.bpusagemodel.ReleaseDeviceResourceAction;
import de.uhd.ifi.se.pcm.bppcm.datamodel.CollectionDataObject;
import de.uhd.ifi.se.pcm.bppcm.datamodel.CompositeDataObject;
import de.uhd.ifi.se.pcm.bppcm.datamodel.DataModel;
import de.uhd.ifi.se.pcm.bppcm.datamodel.DataObject;
import de.uhd.ifi.se.pcm.bppcm.organizationenvironmentmodel.Role;
import intBIIS_LP.model.intBIIS_LP.ADRMatchASExt;
import intBIIS_LP.model.intBIIS_LP.ELSCMatchASExt;
import intBIIS_LP.model.intBIIS_LP.ELSCMatchASExtContainer;
import intBIIS_LP.model.intBIIS_LP.PoolExtContainer;
import intBIIS_LP.model.intBIIS_LP.RDRMatchASExt;
import intBIIS_LP.model.intBIIS_LP.Role_PoolExt;

public class PallBPMNRoleModelAnalyzer {

	ILaunchConfiguration configuration;

	// init Strings for db
	String dbRole = "";
	String dbProcess = "";
	String dbBusinessActivity = "";
	ArrayList<String> dbBusinessPermission = new ArrayList<>();
	private ArrayList<String> dbBusinessPermissionIds;
	ArrayList<String> dbISDataType = new ArrayList<>();
	String dbISDataTypeId = "";
	String dbISActivity = "";
	String dbSystem = "";
	String dbServiceCall = "";
	String dbISPermission = "";
	String dbELSC_Id = "";

	String matchingRole;

	// inits the EMF reader for reading all models
	EMFModelReader emfReader = new EMFModelReader();

	// the hash map for keeping the data for the ADRMatchingASAndELSC database
	HashMap<String, ArrayList<String>> adrHashMap = new HashMap<String, ArrayList<String>>();

	// keeps the paths of all model files
	private Map<String, String> paths;


	// constructor for the class and getting all required paths for the models
	public PallBPMNRoleModelAnalyzer(Map<String, String> requiredPathsForExtractionList) {
		super();
		this.paths = requiredPathsForExtractionList;
	}

	/**
	 * Main method which reads the model objects and writes the extracted data into the
	 * database
	 * 
	 * @param scenarioBehaviour a scenario behaviour object 
	 * @param db the db where to write the extracted data to
	 * @param defaultProcess a name for the process (usually extracted from the object itself)
	 */
	public void extractModelsFromScenarioBehaviour(ScenarioBehaviour scenarioBehaviour, SQLiteJDBCDriverConnection db,
			String defaultProcess) {

		// sets the process to the scenario behaviour name if there is no default
		// process given
		if (defaultProcess == null) {
			dbProcess = scenarioBehaviour.getEntityName();
		} else {
			dbProcess = defaultProcess;
		}

		// gets all actions inside a scenario behaviour
		EList<AbstractUserAction> actions = scenarioBehaviour.getActions_ScenarioBehaviour();
		// iterates trough all actions and checks which type the action is
		for (AbstractUserAction a : actions) {

			// clears all strings for inserting into the database
			dbBusinessActivity = "";
			dbBusinessPermission.clear();
			dbISDataType.clear();
			dbISActivity = "";
			dbSystem = "";
			dbServiceCall = "";
			dbISPermission = "";
			dbELSC_Id = "";
			
			// if the action is from the type ActorStep
			if (a instanceof ActorStep) {
				dbRole = "";
				ArrayList<String> acrbData = new ArrayList<>();

				// extracts all necessary data an actor step consists of
				ActorStep as = (ActorStep) a;
				dbBusinessActivity = as.getEntityName();
				Role role = as.getResponsibleRole();
				if (role != null) {
					// calls the function for extracting the organizational unit and concat it with
					// the role name
					dbRole = getOrganizationalUnit(role.getEntityName()) + ":" + role.getEntityName();
				}
				// calls the method to get all data objects from the actor step and writes it to
				// the array list
				dbBusinessPermission = getDataObjects(as);
				dbBusinessPermissionIds = getDataObjectsIds(as);
				// calls the method to get all the data types of the repository and writes it to
				// the array list
				dbISDataType = getRepositoryDataTypes(as);
				// if the ADR hash map is initialized add the Actor Step data for the
				// consolidated database
				if (adrHashMap.size() > 0) {
					adrHashMap.forEach((k, v) -> {
						System.out.println("Key: " + k + " Value: " + v);
						String tempBusinessActivity = null;
						String tempbusinessPermission = null;
						String tempISDataType = null;

						tempBusinessActivity = v.get(2);
						tempbusinessPermission = v.get(3);
						tempISDataType = v.get(4);
						// depending if there are already values in the hash map, new values will be
						// append or created
						// checks is the Actor Step is matching the role
						if (!v.get(0).isEmpty()
								&& v.get(0).substring(v.get(0).lastIndexOf(":") + 1).equals(role.getEntityName())) {
							if (tempBusinessActivity == "" && tempbusinessPermission == "" && tempISDataType == "") {
								v.set(2, dbBusinessActivity);
								v.set(3, String.join(",", dbBusinessPermission));
								v.set(4, String.join(",", dbISDataType));
							} else {
								v.set(2, tempBusinessActivity + "," + dbBusinessActivity);
								v.set(3, tempbusinessPermission + "," + String.join(";", dbBusinessPermission));
								v.set(4, tempISDataType + "," + String.join(";", dbISDataType));
							}
						}
					});
				}

				// combines READ and WRITE objects from permission lists
				dbBusinessPermission = combineReadWriteElements(dbBusinessPermission);
				dbBusinessPermissionIds = combineReadWriteElements(dbBusinessPermissionIds);

				// dbISDataType = combineReadWriteElements(dbISDataType); // to combine
				// Read/Write in DataTypes

				acrbData.add(0, dbRole);
				acrbData.add(1, dbProcess);
				acrbData.add(2, dbBusinessActivity.replace("\'", ""));
				acrbData.add(3, "");
				acrbData.add(4, "");
				acrbData.add(5, "");

				if (dbBusinessPermission.size() > 0) {
					// iterates over all business permissions
					for (int i1 = 0; i1 < dbBusinessPermission.size(); i1++) {
						// if there are business and It permissions, add both to the database
						if (dbISDataType.size() > 0 && i1 < dbISDataType.size()) {
							acrbData.set(3, dbBusinessPermission.get(i1));
							acrbData.set(4, dbISDataType.get(i1));
							if (i1 < dbBusinessPermissionIds.size()) {
								acrbData.set(5, dbBusinessPermissionIds.get(i1));
							}
							db.insertACRBPDataToTable(acrbData);
							// if there are no It permission just and the business permission and remain the
							// It permission empty
						} else {
							acrbData.set(3, dbBusinessPermission.get(i1));
							acrbData.set(5, dbBusinessPermissionIds.get(i1));
							db.insertACRBPDataToTable(acrbData);
						}
					}
				} else {
					db.insertACRBPDataToTable(acrbData);
				}

				// if the action is an entry level system call
			} else if (a instanceof EntryLevelSystemCall) {
				// inits the array list for the type of the parameters
				ArrayList<String> operationParameter = new ArrayList<>();

				EntryLevelSystemCall elsc = (EntryLevelSystemCall) a;
				dbISActivity = elsc.getEntityName();
				dbELSC_Id = elsc.getId();
				
				try {
					dbSystem = elsc.getProvidedRole_EntryLevelSystemCall().getProvidingEntity_ProvidedRole()
							.getEntityName(); // = component zukünftig ist auch das subsystem interessant
				} catch (NullPointerException e) {
					System.out.println("no Entry Level System Call " + dbISActivity + " provided role (System) Error:"
							+ e.getMessage());
					dbSystem = "";
				}
				// the system can be directly extracted from functions the elsc provide

				// extraction of the operation signature of the elsc
				OperationSignature operat = elsc.getOperationSignature__EntryLevelSystemCall();

				// if there is a operation signature, the values will be extracted and added to
				// the db
				if (operat != null) {
					dbServiceCall = elsc.getProvidedRole_EntryLevelSystemCall().getEntityName() + "."
							+ operat.getEntityName() + "()";
					EList<Parameter> parameter = operat.getParameters__OperationSignature();
					// gets the return types of the called functions
					DataType returns = operat.getReturnType__OperationSignature();
					// for all parameter add a read permission to the array list
					for (Parameter param : parameter) {
						if (param.getDataType__Parameter() instanceof PrimitiveDataType) {
							operationParameter.add("READ " + param.getParameterName() + ": "
									+ ((PrimitiveDataType) param.getDataType__Parameter()).getType().getName());
						} else if (param.getDataType__Parameter() instanceof CollectionDataType) {
							operationParameter.add("READ " + param.getParameterName() + ": "
									+ ((CollectionDataType) param.getDataType__Parameter()).getEntityName());
						} else if (param.getDataType__Parameter() instanceof CompositeDataType) {
							operationParameter.add("READ " + param.getParameterName() + ": "
									+ ((CompositeDataType) param.getDataType__Parameter()).getEntityName());
						}
						// operationParameter.add("READ " + param.getParameterName() + ":" +
						// param.getDataType__Parameter());
					}
					// checks which kind of data type the operation signature matches and casts it
					// to it
					if (returns != null) {
						// for all return parameter add a write permission to the array list
						if (returns instanceof PrimitiveDataType) {
							operationParameter.add("WRITE " + ((PrimitiveDataType) returns).getType().getName());
						} else if (returns instanceof CollectionDataType) {
							operationParameter.add("WRITE " + ((CollectionDataType) returns).getEntityName());
						} else if (returns instanceof CompositeDataType) {
							operationParameter.add("WRITE " + ((CompositeDataType) returns).getEntityName());
						}
					}
					// if there is no operation signature the values will be null/empty
				} else {
					try {
						dbServiceCall = elsc.getProvidedRole_EntryLevelSystemCall().getEntityName() + "." + null;
					} catch (NullPointerException e) {
						System.out.println("no Entry Level System Call " + dbISActivity
								+ " provided role (Service Call). Error:" + e.getMessage());
						dbServiceCall = "";
					}
				}

				ActorStep matchingActorStepForELSC = getMatchingActorStepForELSC(dbISActivity);
				try {
					matchingRole = matchingActorStepForELSC.getResponsibleRole().getEntityName();
				} catch (NullPointerException e) {
					System.out.println("no Entry Level System Call " + dbISActivity
							+ " no matching role. Error:" + e.getMessage());
					matchingRole = "";
				}
				
				// if ADR HashMap is not empty, add the values of the ELSC to the map
				if (adrHashMap.size() > 0) {
					adrHashMap.forEach((k, v) -> {
						// System.out.println("Key: " + k + " Value: " + v);
						String tempIsActivity = null;
						String tempSystem = null;
						String tempServiceCall = null;
						String tempIsPermission = null;

						// checks if ELSC is matching the role
						if (!v.get(0).isEmpty()
								&& v.get(0).substring(v.get(0).lastIndexOf(":") + 1).equals(matchingRole)) {
							tempIsActivity = v.get(5);
							tempSystem = v.get(6);
							tempServiceCall = v.get(7);
							tempIsPermission = v.get(8);

							// adds the data depending on if the values are already set or not
							if (tempIsActivity == "" && tempSystem == "" && tempServiceCall == ""
									&& tempIsPermission == "") {
								v.set(5, dbISActivity);
								v.set(6, dbSystem);
								v.set(7, dbServiceCall);
								v.set(8, String.join(";", operationParameter));
							} else {
								v.set(5, tempIsActivity + "," + dbISActivity);
								v.set(6, tempSystem + "," + dbSystem);
								v.set(7, tempServiceCall + "," + dbServiceCall);
								v.set(8, tempIsPermission + "," + String.join(";", operationParameter));
							}
						}
					});
				}

				ArrayList<String> elscMatchingASData = new ArrayList<>();
				ArrayList<Integer> elscMatchingIds = getMatchingId("elsc", dbProcess, dbISActivity);
				if (elscMatchingIds != null) {
					for (int elscMatchingId : elscMatchingIds) {
						if (elscMatchingId != 0) {
							elscMatchingASData.add(0, String.valueOf(elscMatchingId));
							elscMatchingASData.add(1, dbISActivity);
							elscMatchingASData.add(2, dbSystem);
							elscMatchingASData.add(3, dbServiceCall);
							elscMatchingASData.add(4, dbELSC_Id);
							if (operationParameter.size() > 0) {
								for (String opPara : operationParameter) {
									elscMatchingASData.add(5, opPara);
									db.insertACRWorkflowDataToTable(elscMatchingASData);
								}
							} else {
								elscMatchingASData.add(5, "");
								db.insertACRWorkflowDataToTable(elscMatchingASData);
							}
						}
					}
				}
				// if the action is a adra
			} else if (a instanceof AcquireDeviceResourceAction) {
				AcquireDeviceResourceAction adra = (AcquireDeviceResourceAction) a;
				dbBusinessActivity = adra.getEntityName();
				dbBusinessPermission.add("ACQUIRE " + adra.getPassiveresource_AcquireAction().getEntityName());

				ActorStep matchingActorStep = getMatchingActorStepForADR(dbBusinessActivity);
				String matchingCombinedRole = "";
				if (matchingActorStep != null) {
					String matchingRole = matchingActorStep.getResponsibleRole().getEntityName();
					matchingCombinedRole = getOrganizationalUnit(matchingRole) + ":" + matchingRole;
				}
				

				// adds the ADR, the role and the process to the HashMap for the
				// consolidatedData
				// inits the hashmap with the adra key and and array list of strings for the
				// values
				ArrayList<String> list = new ArrayList<String>();

				adrHashMap.put(adra.getPassiveresource_AcquireAction().getEntityName(),
						initializeAdrdHashMapValue(list));
				// adds the first two values (role,process) to the hash map
				adrHashMap.get(adra.getPassiveresource_AcquireAction().getEntityName()).set(0, matchingCombinedRole);
				adrHashMap.get(adra.getPassiveresource_AcquireAction().getEntityName()).set(1, dbProcess);
				// if the action is a rdra
			} else if (a instanceof ReleaseDeviceResourceAction) {
				ReleaseDeviceResourceAction rdra = (ReleaseDeviceResourceAction) a;
				dbBusinessActivity = rdra.getEntityName();
				String realeaseActionName = rdra.getPassiveresource_ReleaseAction().getEntityName();
				dbBusinessPermission.add("RELEASE " + realeaseActionName);

				// in case of a RDR insert the hashMap data to the database and remove the ADR
				// data from the hashMap
				ArrayList<String> tempMap = adrHashMap.get(realeaseActionName);
				// insert into db with the name of the rdra/adra and the array list from the
				// hash map
				db.insertADRWithSurroundingASAndELSCTable(realeaseActionName, tempMap);
				// removes the data with the matching key from the hash map
				adrHashMap.remove(realeaseActionName);

				// if the action is a loop get the scenario behaviour of the loop and call the
				// whole function recursive with that scenario behavior
			} else if (a instanceof Loop) {
				Loop loop = (Loop) a;
				ScenarioBehaviour loopScenarioBehaviour = loop.getBodyBehaviour_Loop();
				extractModelsFromScenarioBehaviour(loopScenarioBehaviour, db, dbProcess);
				// if the action is a branch get all scenario behaviours of the branch and call
				// the whole function recursive with all scenario behaviors
			} else if (a instanceof Branch) {
				Branch branch = (Branch) a;
				EList<BranchTransition> branchTransitions = branch.getBranchTransitions_Branch();
				ScenarioBehaviour branchScenarioBehaviour;
				// iterate over all branch transitions and get the scenario behavior of it and
				// call the function recursive
				for (BranchTransition branchTransition : branchTransitions) {
					branchScenarioBehaviour = branchTransition.getBranchedBehaviour_BranchTransition();
					extractModelsFromScenarioBehaviour(branchScenarioBehaviour, db, dbProcess);
				}
			}
		}

		//iterates over all user actions again to find matching ADR/RDR 
		for (AbstractUserAction a : actions) {
			if (a instanceof ReleaseDeviceResourceAction) {
				ReleaseDeviceResourceAction rdra = (ReleaseDeviceResourceAction) a;
				ArrayList<String> rdrMatchingASData = new ArrayList<>();
				ArrayList<Integer> rdrMatchingIds = getMatchingId("rdr", dbProcess, rdra.getEntityName());
				if (rdrMatchingIds != null) {
					for (Integer rdrMatchingId : rdrMatchingIds) {
						rdrMatchingASData.add(0, String.valueOf(rdrMatchingId));
						rdrMatchingASData.add(1, rdra.getEntityName());
						rdrMatchingASData.add(2, "RELEASE " + rdra.getPassiveresource_ReleaseAction().getEntityName());
						db.insertACRAcquireReleaseDataToTable(rdrMatchingASData);
					}
				}
			} else if (a instanceof AcquireDeviceResourceAction) {
				AcquireDeviceResourceAction adra = (AcquireDeviceResourceAction) a;
				ArrayList<String> adrMatchingASData = new ArrayList<>();
				ArrayList<Integer> adrMatchingIds = getMatchingId("adr", dbProcess, adra.getEntityName());
				if (adrMatchingIds != null) {
					for (int adrMatchingId : adrMatchingIds) {
						adrMatchingASData.add(0, String.valueOf(adrMatchingId));
						adrMatchingASData.add(1, adra.getEntityName());
						adrMatchingASData.add(2, "ACQUIRE " + adra.getPassiveresource_AcquireAction().getEntityName());
						db.insertACRAcquireReleaseDataToTable(adrMatchingASData);
					}
				}
			}
		}
	}

	// private void matchingAndInsertInDBOfADRRDR(EList<AbstractUserAction> actions)
	// {
	// for (AbstractUserAction a : actions) {
	// if (a instanceof ReleaseDeviceResourceAction) {
	// ReleaseDeviceResourceAction rdra = (ReleaseDeviceResourceAction) a;
	//
	// ArrayList<String> rdrMatchingASData = new ArrayList<>();
	// int rdrMatchingId = getMatchingId("rdr", rdra.getEntityName());
	// rdrMatchingASData.add(0, String.valueOf(rdrMatchingId));
	// rdrMatchingASData.add(1, rdra.getEntityName());
	// rdrMatchingASData.add(2, "RELEASE " +
	// rdra.getPassiveresource_ReleaseAction().getEntityName());
	// db.insertACRAcquireReleaseDataToTable(rdrMatchingASData);
	// } else if (a instanceof AcquireDeviceResourceAction) {
	// AcquireDeviceResourceAction adra = (AcquireDeviceResourceAction) a;
	// ArrayList<String> adrMatchingASData = new ArrayList<>();
	// int adrMatchingId = getMatchingId("adr", adra.getEntityName());
	// adrMatchingASData.add(0, String.valueOf(adrMatchingId));
	// adrMatchingASData.add(1, adra.getEntityName());
	// adrMatchingASData.add(2, "ACQUIRE " +
	// adra.getPassiveresource_AcquireAction().getEntityName());
	// db.insertACRAcquireReleaseDataToTable(adrMatchingASData);
	// }
	// }
	// }

	
	private ArrayList<String> initializeAdrdHashMapValue(ArrayList<String> list) {
		list.add(0, "");
		list.add(1, "");
		list.add(2, "");
		list.add(3, "");
		list.add(4, "");
		list.add(5, "");
		list.add(6, "");
		list.add(7, "");
		list.add(8, "");
		return list;
	}

	/**
	 * Gets all data objects of an actor step and write it to an ArrayList
	 * 
	 * @param actorStep The given actor step
	 * @return A Array List containing all the data objects with their read or write
	 *         rights in an array
	 */
	private ArrayList<String> getDataObjects(ActorStep actorStep) {
		DataModel dataModel = (DataModel) emfReader.getRessourceFromModel(paths.get("projectPath")
				+ paths.get("dataPath").substring(paths.get("dataPath").indexOf(File.separator)));
		EList<DataObject<?>> dataModels = dataModel.getDataObjects();
		ArrayList<String> dataObjects = new ArrayList<String>();

		// extracts all input and output objects from the Actor Step
		EList<DataObject<?>> inputDataObjects = actorStep.getInputDataObjects();
		EList<DataObject<?>> outputDataObjects = actorStep.getOutputDataObjects();

		// iterates over all input objects and checks if there are matches with data
		// objects from the imported data model
		for (DataObject<?> inputDataObject : inputDataObjects) {
			for (DataObject<?> model : dataModels) {
				if (model.getEntityName().equals(inputDataObject.getEntityName())) {
					dataObjects.add("READ " + inputDataObject.getEntityName() + getDataType(inputDataObject));
				}
			}
		}

		// iterates over all output objects and checks if there are matches with data
		// objects from the imported data model
		for (DataObject<?> outputDataObject : outputDataObjects) {
			for (DataObject<?> model : dataModels) {
				if (model.getEntityName().equals(outputDataObject.getEntityName())) {
					dataObjects.add("WRITE " + outputDataObject.getEntityName() + getDataType(outputDataObject));
				}
			}
		}
		return dataObjects;
	}
	
	
	
	private ArrayList<String> getDataObjectsIds(ActorStep actorStep) {
		DataModel dataModel = (DataModel) emfReader.getRessourceFromModel(paths.get("projectPath")
				+ paths.get("dataPath").substring(paths.get("dataPath").indexOf(File.separator)));
		EList<DataObject<?>> dataModels = dataModel.getDataObjects();
		ArrayList<String> dataObjects = new ArrayList<String>();

		// extracts all input and output objects from the Actor Step
		EList<DataObject<?>> inputDataObjects = actorStep.getInputDataObjects();
		EList<DataObject<?>> outputDataObjects = actorStep.getOutputDataObjects();

		// iterates over all input objects and checks if there are matches with data
		// objects from the imported data model
		for (DataObject<?> inputDataObject : inputDataObjects) {
			for (DataObject<?> model : dataModels) {
				if (model.getEntityName().equals(inputDataObject.getEntityName())) {
					DataType dataType = inputDataObject.getDataTypes().get(0);			
					dataObjects.add("READ " + getDataTypeId(dataType) + getDataType(inputDataObject));
				}
			}
		}

		// iterates over all output objects and checks if there are matches with data
		// objects from the imported data model
		for (DataObject<?> outputDataObject : outputDataObjects) {
			for (DataObject<?> model : dataModels) {
				if (model.getEntityName().equals(outputDataObject.getEntityName())) {
					dataObjects.add("WRITE " + getDataTypeId(outputDataObject.getDataTypes().get(0)) + getDataType(outputDataObject));
				}
			}
		}
		return dataObjects;
	}
	
	
	
	private String getDataTypeId(DataType dataType) {
		String dataTypeId = "";
		if (dataType instanceof CompositeDataType) {
			CompositeDataType compositeDataType = (CompositeDataType) dataType;
			dataTypeId = compositeDataType.getId();
		} else if (dataType instanceof CollectionDataType) {
			CollectionDataType collectionDataType = (CollectionDataType) dataType;
			dataTypeId = collectionDataType.getId();
		} else if (dataType instanceof PrimitiveDataType) {
			PrimitiveDataType primitiveDataType = (PrimitiveDataType) dataType;
			dataTypeId = primitiveDataType.getType().getName();
		}
		return dataTypeId;
	}
	
	

	/**
	 * Adds a string to a DataObject to indicate either it's a Collection or Composite DataType
	 * @param dataObject
	 * @return
	 */
	private String getDataType(DataObject<?> dataObject) {
		String dataTypes = "";
			if (dataObject instanceof CollectionDataObject) {
				dataTypes += " (Coll)";
			} else if (dataObject instanceof CompositeDataObject) {
				dataTypes += " (Comp)";
			} 
		return dataTypes;
	}

	/**
	 * Gets all the data types of the data objects
	 * 
	 * @param actorStep The given actor step
	 * @return An ArrayList containing all data types with their read or write
	 *         rights
	 */
	private ArrayList<String> getRepositoryDataTypes(ActorStep actorStep) {
		ArrayList<String> repositoryDataTypesList = new ArrayList<String>();

		EList<DataObject<?>> inputDataObjects = actorStep.getInputDataObjects();
		EList<DataObject<?>> outputDataObjects = actorStep.getOutputDataObjects();

		// gets input data types
		for (DataObject<?> inputDataObject : inputDataObjects) {
			EList<?> dataTypeList = inputDataObject.getDataTypes();
			if (dataTypeList.isEmpty()) {
				repositoryDataTypesList.add("");
			}
			// checks with data type the input data objects are and casts them to the
			// matching one
			for (Object dataType : dataTypeList) {
				
				if (dataType instanceof CollectionDataType) {					
					repositoryDataTypesList.add(((CollectionDataType) dataType).getEntityName() + " (Coll)"); // add
																												// "Read"
																												// to
																												// be
					// shown in output
				} else if (dataType instanceof CompositeDataType)
					repositoryDataTypesList.add(((CompositeDataType) dataType).getEntityName() + " (Comp)"); // add
																												// "Read"
																												// to be
				// shown in output
			}
		}

		// gets output data types
		for (DataObject<?> outputDataObject : outputDataObjects) {
			EList<?> dataTypeList = outputDataObject.getDataTypes();
			if (dataTypeList.isEmpty()) {
				repositoryDataTypesList.add("");
			}
			// checks with data type the output data objects are and casts them to the
			// matching one
			for (Object dataType : dataTypeList) {
				if (dataType instanceof CollectionDataType) {
					repositoryDataTypesList.add(((CollectionDataType) dataType).getEntityName() + " (Coll)"); // add
																												// "Write"
																												// to be
					// shown in output
				} else if (dataType instanceof CompositeDataType)
					repositoryDataTypesList.add(((CompositeDataType) dataType).getEntityName() + " (Comp)"); // add
																												// "Write"
																												// to be
				// shown in output
			}
		}
		return repositoryDataTypesList;
	}

	/**
	 * Gets the matching ActorStep Element for a ELSC/ADR/RDR from the ELSCMatchAS model and retrieves the corresponding ID from the database 
	 * @param type the type of the given model element (elsc,adr,rdr)
	 * @param name the name of the model element
	 * @return a list with all the matching IDs from the database
	 */
	private ArrayList<Integer> getMatchingId(String type, String process, String name) {
		if (paths.get("elscToAsPath") != null) {
			ELSCMatchASExtContainer laneExtModel = (ELSCMatchASExtContainer) emfReader
					.getRessourceFromModel(paths.get("projectPath")
							+ paths.get("elscToAsPath").substring(paths.get("elscToAsPath").indexOf(File.separator)));

			SQLiteJDBCDriverConnection db = new SQLiteJDBCDriverConnection();
			ArrayList<Integer> isActvityIds = new ArrayList<Integer>();
			switch (type) {
			case "elsc":
				EList<ELSCMatchASExt> elscMatchAs = laneExtModel.getElscmatchasext();
				for (ELSCMatchASExt elscMatch : elscMatchAs) {
					String elscName = elscMatch.getEntrylevelsystemcall().getEntityName();
					if (elscName.equals(name)) {
						String actorStepName = elscMatch.getActorstep().getEntityName();
						isActvityIds.add(db.getISActivityId(actorStepName.replace("\'", ""), process));
					}
				}
				return isActvityIds;
			case "adr":
				EList<ADRMatchASExt> adrMatchAs = laneExtModel.getAdrmatchasext();
				for (ADRMatchASExt adrMatch : adrMatchAs) {
					String adrName = adrMatch.getAcquiredeviceresourceaction().getEntityName();
					if (adrName.equals(name)) {
						String actorStepName = adrMatch.getActorstep().getEntityName();
						isActvityIds.add(db.getISActivityId(actorStepName.replace("\'", ""), process));
					}
				}
				return isActvityIds;
			case "rdr":
				EList<RDRMatchASExt> rdrMatchAs = laneExtModel.getRdrmatchasext();
				for (RDRMatchASExt rdrMatch : rdrMatchAs) {
					String rdrName = rdrMatch.getReleasedeviceresourceaction().getEntityName();
					if (rdrName.equals(name)) {
						String actorStepName = rdrMatch.getActorstep().getEntityName();
						isActvityIds.add(db.getISActivityId(actorStepName.replace("\'", ""), process));
					}
				}
				return isActvityIds;
			}
		}
		return null;
	}

	/**
	 * Returns the matching ActorStep for an ADR retrieved from ELSCMatchAS model
	 * @param name the name of the ADR element
	 * @return the matching ActorStep object for the ADR retrieved from ELSCMatchAS model
	 */
	private ActorStep getMatchingActorStepForADR(String name) {
		if (paths.get("elscToAsPath") != null) {
			ELSCMatchASExtContainer laneExtModel = (ELSCMatchASExtContainer) emfReader
					.getRessourceFromModel(paths.get("projectPath")
							+ paths.get("elscToAsPath").substring(paths.get("elscToAsPath").indexOf(File.separator)));

			EList<ADRMatchASExt> adrMatchAs = laneExtModel.getAdrmatchasext();
			for (ADRMatchASExt adrMatch : adrMatchAs) {
				String adrName = adrMatch.getAcquiredeviceresourceaction().getEntityName();
				if (adrName.equals(name)) {
					return adrMatch.getActorstep();
				}
			}
		}
		return null;
	}

	/**
	 * Gets a matching Actor step for an ELSC from the ELSCMatchAS model
	 * @param name the name of the given ELSC
	 * @return the matching actor step object or null
	 */
	private ActorStep getMatchingActorStepForELSC(String name) {
		if (paths.get("elscToAsPath") != null) {
			ELSCMatchASExtContainer laneExtModel = (ELSCMatchASExtContainer) emfReader
					.getRessourceFromModel(paths.get("projectPath")
							+ paths.get("elscToAsPath").substring(paths.get("elscToAsPath").indexOf(File.separator)));

			EList<ELSCMatchASExt> elscMatchAs = laneExtModel.getElscmatchasext();
			for (ELSCMatchASExt elscMatch : elscMatchAs) {
				String elscName = elscMatch.getEntrylevelsystemcall().getEntityName();
				if (elscName.equals(name)) {
					return elscMatch.getActorstep();
				}
			}
		}
		return null;
	}

	/**
	 * Extracts the organizational unit from the IntBIIS_LP Pool Extension
	 * 
	 * @param role The name of the given role
	 * @return The organizational unit of the provided role
	 */
	private String getOrganizationalUnit(String role) {
		String path = paths.get("projectPath")
				+ paths.get("poolIntbisLpPath").substring(paths.get("poolIntbisLpPath").indexOf(File.separator));
		PoolExtContainer poolExtModel = (PoolExtContainer) emfReader.getRessourceFromModel(path);
		EList<Role_PoolExt> poolRoles = poolExtModel.getRole_poolext();
		// iterates over all pool roles
		for (Role_PoolExt poolRole : poolRoles) {
			Role poolRoleForCheck = poolRole.getRole();
			// if the pool role is not null and the role name is the same like pool role
			// name return the organizational unit connected to the pool role
			if (poolRoleForCheck != null) {
				if (role.equals(poolRoleForCheck.getEntityName())) {
					return poolRole.getOrganizationalUnit();
				}
			}
		}
		return null;
	}

	/**
	 * Combines separated READ/WRITE elements in a given ArrayList of Strings (READ element, WRITE element goes to READ/WRITE element)
	 * @param list the list with the separated READ and WRITE elements
	 * @return a list with the combined elements
	 */
	private ArrayList<String> combineReadWriteElements(ArrayList<String> list) {
		for (int i1 = 0; i1 < list.size(); i1++) {
			if (!list.get(i1).equals("")) {
				String[] tempStringArray = list.get(i1).split("\\s+");
				for (int j = i1 + 1; j < list.size(); j++) {
					if (!list.get(j).equals("")) {
						if (list.get(j).split("\\s+")[1].equals(tempStringArray[1])) {
							list.remove(j);
							list.set(i1, "READ/WRITE " + tempStringArray[1] + " " + tempStringArray[2]);
						}
					}
				}
			}
		}
		return list;
	}

}
