package ui;

/**
 * Contains the Attributes which should be set when selecting the files to be processed from the project
 * @author Yuri
 *
 */
public interface LaunchConfigurationAttributes {
	
	public static final String projectPath = "the selected project path will be displayed here";
	public static final String selectedBpusageModel= "Here you have to select the .bpusagemodel from your project";
	public static final String selectedOrgaModel= "Here you have to select the .organizationenvironmentmodel from your project";
	public static final String selectedDataModel= "Here you have to select the .datamodel from your project";
	public static final String selectedSystem= "Here you have to select the .system from your project";
	public static final String selectedRepo= "Here you have to select the .repository from your project";
	public static final String selectedPoolExtIntbiis_lp= "Here you have to select a PoolExtension.inbiis_lp from your project";
	public static final String selectedElscToASExtIntbiis_lp= "Here you have to select a RoleExtension.inbiis_lp from your project";
	public static final String generateDFDInformation = "Select if the DFD Information should be generated";
	public static final String selectedACA = "Select the repository files for Access Control Annotations";
	public static final String selectedADRWithSurroundingReferenceFilePath = "Select Reference ADRWithSurrounding";
	public static final String selectedTraceabilityReferenceFilePath = "Select Reference TraceabilityModel";
	public static final String selectedRoleModelWithADRReferenceFilePath = "Select Reference RoleModelWithADR";
	public static final String selectedAcsAlignCheckbox = "Checkbox for selecting ACS Align";
	public static final String acrAlignJsonSelector = "Choose dataTypeUsage file";
}
