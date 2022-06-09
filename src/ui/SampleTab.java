package ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class SampleTab extends AbstractLaunchConfigurationTab {

	private MultiCheckSelectionCombo ddUsage;
	private Combo ddOrga;
	private Combo ddData;
	private Combo ddSys;
	private Combo ddRepo;
	private Combo acrAlignJsonSelector;
	private MultiCheckSelectionCombo ddPool_Lp;
	private MultiCheckSelectionCombo ddElscToAS_Lp;
//	private MultiCheckSelectionCombo selectedACA;
	private Text selectedProjectTextfield;
	private Button selectedDFDCheckbox;
	private Button selectedAcsAlignCheckbox;
	private String projectPath = "";
	private String rootFolder = "";
	private String selectedTraceabilityReferenceFilePath = "";
	private String selectedRoleModelWithADRReferenceFilePath = "";
	private String selectedADRWithSurroundingReferenceFilePath = "";

	String defaultText = "Choose";
	/**
	 * Contains FileNames as Key and FilePathes as Value
	 */
	HashMap<String, String> fileToPathMap = new HashMap<String, String>();
	/**
	 * Contains all related pathes to each kind of file extension.
	 */
	private HashMap<String, ArrayList<String>> filetype2FilesMap = new HashMap<String, ArrayList<String>>();

	@Override
	public void createControl(Composite parent) {
		
		Display display = Display.getCurrent();
		Color black = display.getSystemColor(SWT.COLOR_BLACK);

		projectPath = getProjectPath();
		if (projectPath.isEmpty()) {
			System.out.println("ProjectPath: is empty/was not yet selected");
		} else {
			System.out.println("ProjectPath: " + projectPath);
			rootFolder = getRootFolder(projectPath);
			scanProjectForFiles(projectPath);
		}

		// ################################## Here starts the FileSelection of a
		// Selected Project #############################################
		Group fileSelectionGroup = new Group(parent, SWT.BORDER);
		fileSelectionGroup.setText("Target File Selection");
		setControl(fileSelectionGroup);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(fileSelectionGroup);

		// ersteZeile
		Label browseLabel = new Label(fileSelectionGroup, SWT.NONE);
		browseLabel.setText("Click to search for project:");

		Button browseButton = new Button(fileSelectionGroup, SWT.PUSH);
		browseButton.setText("Browse");
		browseButton.addSelectionListener(new SelectionListener() {
			/**
			 * The DirectoryBrowser gets opened and a Folder is selecteable. Afterwards the
			 * path gets isolated and passed to processSelectedProject(String);
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = new Shell();
				DirectoryDialog directoryBrowse = new DirectoryDialog(shell, SWT.OPEN);
				directoryBrowse.setText("Choose Your Project");
				directoryBrowse.setFilterPath(projectPath);
				String selected = directoryBrowse.open();
				if (selected != null) {
					fileToPathMap.clear();
					selectedProjectTextfield.setText(selected);
					System.out.println("Selected via button: " + selected);
					processSelectedProject(selected);
				} else {
					System.out.println("ERROR: es muss ein Projekt ausgew�hlt werden");
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		GridDataFactory.fillDefaults().grab(false, false).applyTo(browseButton);

		// zweite Zeile
		Label selectedProjLabel = new Label(fileSelectionGroup, SWT.NONE);
		selectedProjLabel.setText("Selected project:");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(selectedProjLabel);

		selectedProjectTextfield = new Text(fileSelectionGroup, SWT.NONE);
		selectedProjectTextfield.setText(projectPath);

		GridDataFactory.fillDefaults().grab(true, false).applyTo(selectedProjectTextfield);

		// Generate DFD Information Checkbox
		Label selectedDFDLabel = new Label(fileSelectionGroup, SWT.NONE);
		selectedDFDLabel.setText("Generate DFD Information:");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(selectedDFDLabel);

		selectedDFDCheckbox = new Button(fileSelectionGroup, SWT.CHECK);
		selectedDFDCheckbox.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				Button btn = (Button) e.getSource();
	            if (btn.getSelection()) {
	            	ddSys.clearSelection();
	            	ddSys.setEnabled(false);
	            	ddRepo.clearSelection();
	            	ddRepo.setEnabled(false);
//	            	selectedACA.removeAll();
//	            	selectedACA.setVisible(false);
	            	selectedAcsAlignCheckbox.setEnabled(false);
	            } else {
	            	ddSys.setEnabled(true);
	            	ddRepo.setEnabled(true);
//	            	selectedACA.setVisible(true);
	            	selectedAcsAlignCheckbox.setEnabled(true);
	            	processSelectedProject(projectPath);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(selectedDFDCheckbox);
		
		
		Label separator = new Label(fileSelectionGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setBackground(black);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label separator1 = new Label(fileSelectionGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator1.setBackground(black);
		separator1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
	

		// Erste Zeile
		Label labelUsage = new Label(fileSelectionGroup, SWT.NONE);
		labelUsage.setText("Select \".bpusagemodel\" :");
		GridDataFactory.swtDefaults().applyTo(labelUsage);

		ddUsage = new MultiCheckSelectionCombo(fileSelectionGroup, SWT.NONE);
		String[] inputs = listToArray(filetype2FilesMap.get(".bpusagemodel"));
		for (String s : inputs) {
			ddUsage.add(s);
		}
		// pack shell when combo selection display changes and resizes
		ddUsage.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (ddUsage.getSelections().length == 0) {
					System.out.println("Es wurde kein BPUsageModel ausgew�hlt");
				}
				System.out.println("Selected BPUsagemodel: " + Arrays.toString(ddUsage.getSelections()));
				scheduleUpdateJob();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});

		GridDataFactory.fillDefaults().grab(true, false).applyTo(ddUsage);

		// Zweite Zeile
		Label labelOrga = new Label(fileSelectionGroup, SWT.NONE);
		labelOrga.setText("Select \".organizationenvironmentmodel\" :");
		GridDataFactory.swtDefaults().applyTo(labelOrga);

		// Dropdown mit Liste der entsprechenden Datein innerhalb des Projektes
		ddOrga = new Combo(fileSelectionGroup, SWT.DROP_DOWN);
		ddOrga.setItems(listToArray(filetype2FilesMap.get(".organizationenvironmentmodel")));
		ddOrga.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// System.out.println("Selected Organizationenvironmentmodel: " +
				// ddOrga.getText());
				scheduleUpdateJob();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (ddOrga.getSelectionIndex() == 0) {
					System.out.println("Es wurde kein organizationenvironmentmodel ausgew�hlt");
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(ddOrga);

		// Dritte Zeile
		Label labelData = new Label(fileSelectionGroup, SWT.NONE);
		labelData.setText("Select \".datamodel\" :");
		GridDataFactory.swtDefaults().applyTo(labelData);

		// Dropdown mit Liste der entsprechenden Datein innerhalb des Projektes
		ddData = new Combo(fileSelectionGroup, SWT.DROP_DOWN);
		ddData.setItems(listToArray(filetype2FilesMap.get(".datamodel")));
		ddData.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// System.out.println("Selected Datamodel: " + ddData.getText());
				scheduleUpdateJob();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (ddData.getSelectionIndex() == 0) {
					System.out.println("Es wurde kein Datamodel ausgew�hlt");
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(ddData);

		// Vierte Zeile
		Label labelSys = new Label(fileSelectionGroup, SWT.NONE);
		labelSys.setText("Select \".system\" :");
		GridDataFactory.swtDefaults().applyTo(labelSys);

		// Dropdown mit Liste der entsprechenden Datein innerhalb des Projektes
		ddSys = new Combo(fileSelectionGroup, SWT.DROP_DOWN);
		ddSys.setItems(listToArray(filetype2FilesMap.get(".system")));
		ddSys.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// System.out.println("Selected System: " + ddSys.getText());
				scheduleUpdateJob();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (ddSys.getSelectionIndex() == 0) {
					System.out.println("Es wurde kein System ausgewählt");
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(ddSys);

		// f�nfte Zeile
		Label labelRepo = new Label(fileSelectionGroup, SWT.NONE);
		labelRepo.setText("Select \".repository\" :");
		GridDataFactory.swtDefaults().applyTo(labelRepo);

		// Dropdown mit Liste der entsprechenden Datein innerhalb des Projektes
		ddRepo = new Combo(fileSelectionGroup, SWT.DROP_DOWN);
		ddRepo.setItems(listToArray(filetype2FilesMap.get(".repository")));
		ddRepo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// System.out.println("Selected Repository: " + ddRepo.getText());
				scheduleUpdateJob();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (ddRepo.getSelectionIndex() == 0) {
					System.out.println("Es wurde kein Repository ausgew�hlt");
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(ddRepo);

		// sechste zeile
		Label label_PoolLp = new Label(fileSelectionGroup, SWT.NONE);
		label_PoolLp.setText("Select PoolExtension\".intbiis_lp\" :");
		GridDataFactory.swtDefaults().applyTo(label_PoolLp);

		ddPool_Lp = new MultiCheckSelectionCombo(fileSelectionGroup, SWT.NONE);
		String[] inputs_poolLP = listToArray(filetype2FilesMap.get(".intbiis_lp_pool"));
		for (String s : inputs_poolLP) {
			ddPool_Lp.add(s);
		}
		// pack shell when combo selection display changes and resizes
		ddPool_Lp.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (ddPool_Lp.getSelections().length == 0) {
					System.out.println("Es wurde keine PoolExtension ausgewählt");
				}
				System.out.println("Selected PoolExtension: " + Arrays.toString(ddPool_Lp.getSelections()));
				scheduleUpdateJob();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		GridDataFactory.fillDefaults().grab(true, false).applyTo(ddPool_Lp);

		// siebte Zeile
		Label labelElscToAS_Lp = new Label(fileSelectionGroup, SWT.NONE);
		labelElscToAS_Lp.setText("Select ELSCToAS\".intbiis_lp: ");
		GridDataFactory.swtDefaults().applyTo(labelElscToAS_Lp);

		ddElscToAS_Lp = new MultiCheckSelectionCombo(fileSelectionGroup, SWT.NONE);
		String[] inputs_elscToASLP = listToArray(filetype2FilesMap.get(".intbiis_lp_elsctoas"));
		for (String s : inputs_elscToASLP) {
			ddElscToAS_Lp.add(s);
		}
		ddElscToAS_Lp.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				if (ddElscToAS_Lp.getSelections().length == 0) {
					System.out.println("Es wurde keine ELSCToAS ausgewählt");
				}
				System.out.println("Selected ELSCToAS: " + Arrays.toString(ddElscToAS_Lp.getSelections()));
				scheduleUpdateJob();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(ddElscToAS_Lp);

		// selection for Access Control Annotations
//		Label labelACA = new Label(fileSelectionGroup, SWT.NONE);
//		labelACA.setText("Access Control Annotations (Optional):");
//		GridDataFactory.swtDefaults().applyTo(labelACA);
//
//		selectedACA = new MultiCheckSelectionCombo(fileSelectionGroup, SWT.NONE);
//		String[] inputs_selectedACA = listToArray(filetype2FilesMap.get(".repository"));
//		for (String s : inputs_selectedACA) {
//			selectedACA.add(s);
//		}
//		GridDataFactory.fillDefaults().grab(false, false).applyTo(selectedACA);

		
		//ui separation lines
		Label temp = new Label(fileSelectionGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		temp.setBackground(black);
		temp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label temp1 = new Label(fileSelectionGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		temp1.setBackground(black);
		temp1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridDataFactory.fillDefaults().grab(false, false).applyTo(temp);

		
		// traceability reference file browsing
		Label browseTraceabilityReferenceLabel = new Label(fileSelectionGroup, SWT.NONE);
		browseTraceabilityReferenceLabel.setText("Select Reference TraceabilityModel:");

		Button browseTraceabilityReferenceButton = new Button(fileSelectionGroup, SWT.PUSH);
		browseTraceabilityReferenceButton.setText("Browse");
		browseTraceabilityReferenceButton.addSelectionListener(new SelectionListener() {
			/**
			 * The DirectoryBrowser gets opened and a Folder is selecteable. Afterwards the
			 * path gets isolated and passed to processSelectedProject(String);
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = new Shell();
				FileDialog directoryBrowse = new FileDialog(shell, SWT.OPEN);
				directoryBrowse.setFilterPath(projectPath);
				directoryBrowse.setFilterExtensions(new String[]{"*.csv"});
				String selected = directoryBrowse.open();
				if (selected != null) {
					browseTraceabilityReferenceButton.setText(directoryBrowse.getFileName());
					System.out.println("Selected via button: " + selected);
					selectedTraceabilityReferenceFilePath = selected;
				} else {
					System.out.println("No traceability reference file chosen");
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {				
			}
		});
		GridDataFactory.fillDefaults().grab(false, false).applyTo(browseTraceabilityReferenceButton);
		
		
		// rolemodelwithadr reference file browsing
				Label browseRoleModelWithADRReferenceLabel = new Label(fileSelectionGroup, SWT.NONE);
				browseRoleModelWithADRReferenceLabel.setText("Select Reference RoleModelWithADR:");

				Button browseRoleModelWithADRReferenceButton = new Button(fileSelectionGroup, SWT.PUSH);
				browseRoleModelWithADRReferenceButton.setText("Browse");
				browseRoleModelWithADRReferenceButton.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						Shell shell = new Shell();
						FileDialog directoryBrowse = new FileDialog(shell, SWT.OPEN);
						directoryBrowse.setFilterPath(projectPath);
						directoryBrowse.setFilterExtensions(new String[]{"*.csv"});
						String selected = directoryBrowse.open();
						if (selected != null) {
							browseRoleModelWithADRReferenceButton.setText(directoryBrowse.getFileName());
							System.out.println("Selected via button: " + selected);
							selectedRoleModelWithADRReferenceFilePath = selected;
						} else {
							System.out.println("No roleModelWithADR reference file chosen");
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {				
					}
				});
				GridDataFactory.fillDefaults().grab(false, false).applyTo(browseRoleModelWithADRReferenceButton);
				
				
				// rolemodelwithadr reference file browsing
				Label browseADRWithSurroundingReferenceLabel = new Label(fileSelectionGroup, SWT.NONE);
				browseADRWithSurroundingReferenceLabel.setText("Select Reference ADRWithSurrounding:");

				Button browseADRWithSurroundingReferenceButton = new Button(fileSelectionGroup, SWT.PUSH);
				browseADRWithSurroundingReferenceButton.setText("Browse");
				browseADRWithSurroundingReferenceButton.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						Shell shell = new Shell();
						FileDialog directoryBrowse = new FileDialog(shell, SWT.OPEN);
						directoryBrowse.setFilterPath(projectPath);
						directoryBrowse.setFilterExtensions(new String[]{"*.csv"});
						String selected = directoryBrowse.open();
						if (selected != null) {
							browseADRWithSurroundingReferenceButton.setText(directoryBrowse.getFileName());
							System.out.println("Selected via button: " + selected);
							selectedADRWithSurroundingReferenceFilePath = selected;
						} else {
							System.out.println("No ADRWithSurrounding reference file chosen");
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {				
					}
				});
				GridDataFactory.fillDefaults().grab(false, false).applyTo(browseADRWithSurroundingReferenceButton);
				
				//Separator
				Label separator3 = new Label(fileSelectionGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
				separator.setBackground(black);
				separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				Label separator4 = new Label(fileSelectionGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
				separator1.setBackground(black);
				separator1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
				GridDataFactory.fillDefaults().grab(false, false).applyTo(separator3);
				GridDataFactory.fillDefaults().grab(false, false).applyTo(separator4);

				
				
				// AcsAlign Checkbox
				Label selectedAcsAlignLabel = new Label(fileSelectionGroup, SWT.NONE);
				selectedAcsAlignLabel.setText("Activate AcsAlign");
				GridDataFactory.fillDefaults().grab(true, false).applyTo(selectedAcsAlignLabel);

				selectedAcsAlignCheckbox = new Button(fileSelectionGroup, SWT.CHECK);
				selectedAcsAlignCheckbox.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						Button btn = (Button) e.getSource();
			            if (btn.getSelection()) {
			            	selectedDFDCheckbox.setEnabled(false);
			            	acrAlignJsonSelector.setEnabled(true);
			            } else {
			            	selectedDFDCheckbox.setEnabled(true);
			            	acrAlignJsonSelector.setEnabled(false);
			            	processSelectedProject(projectPath);
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
				GridDataFactory.fillDefaults().grab(true, false).applyTo(selectedDFDCheckbox);
				
				
				// Zweite Zeile
				Label selectReadWriteDatatypesLabel = new Label(fileSelectionGroup, SWT.NONE);
				selectReadWriteDatatypesLabel.setText("Select \"dataTypeUsage.json\" :");
				GridDataFactory.swtDefaults().applyTo(selectReadWriteDatatypesLabel);

				// Dropdown mit Liste der entsprechenden Datein innerhalb des Projektes
				acrAlignJsonSelector = new Combo(fileSelectionGroup, SWT.DROP_DOWN);
				acrAlignJsonSelector.setItems(listToArray(filetype2FilesMap.get(".json")));
            	acrAlignJsonSelector.setEnabled(false);
				acrAlignJsonSelector.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						// System.out.println("Selected Organizationenvironmentmodel: " +
						// ddOrga.getText());
						scheduleUpdateJob();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						if (ddOrga.getSelectionIndex() == 0) {
							System.out.println("Es wurde kein dataTypeUsage.json ausgew�hlt");
						}
					}
				});
				GridDataFactory.fillDefaults().grab(true, false).applyTo(acrAlignJsonSelector);
				
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConfigurationAttributes.projectPath,
				"the selected project path will be displayed here");
		configuration.setAttribute(LaunchConfigurationAttributes.selectedBpusageModel,
				"Here you have to select the .bpusagemodel from your project");
		configuration.setAttribute(LaunchConfigurationAttributes.selectedOrgaModel,
				"Here you have to select the .organizationenvironmentmodel from your project");
		configuration.setAttribute(LaunchConfigurationAttributes.selectedDataModel,
				"Here you have to select the .datamodel from your project");
		configuration.setAttribute(LaunchConfigurationAttributes.selectedSystem,
				"Here you have to select the .system from your project");
		configuration.setAttribute(LaunchConfigurationAttributes.selectedRepo,
				"Here you have to select the .repository from your project");
		configuration.setAttribute(LaunchConfigurationAttributes.selectedPoolExtIntbiis_lp,
				"Here you have to select a PoolExtension.inbiis_lp from your project");
		configuration.setAttribute(LaunchConfigurationAttributes.acrAlignJsonSelector,
				"Here you have to select a RoleExtension.inbiis_lp from your project");
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			if (projectPath != null) {
				selectedProjectTextfield.setText(projectPath);
			} else {
				selectedProjectTextfield.setText(configuration.getAttribute(LaunchConfigurationAttributes.projectPath,
						"the selected project path will be displayed here"));
			}
			// selectedProjectTextfield.setText(configuration.getAttribute(LaunchConfigurationAttributes.projectPath,
			// "alternative default from initializeFrom()"));
//			ddUsage.setDefaultText(configuration.getAttribute(LaunchConfigurationAttributes.selectedBpusageModel,
//					"alternative default from initializeFrom()"));
			ddOrga.setText(configuration.getAttribute(LaunchConfigurationAttributes.selectedOrgaModel,
					"alternative default from initializeFrom()"));
			ddData.setText(configuration.getAttribute(LaunchConfigurationAttributes.selectedDataModel,
					"alternative default from initializeFrom()"));
			ddSys.setText(configuration.getAttribute(LaunchConfigurationAttributes.selectedSystem,
					"alternative default from initializeFrom()"));
			ddRepo.setText(configuration.getAttribute(LaunchConfigurationAttributes.selectedRepo,
					"alternative default from initializeFrom()"));
			acrAlignJsonSelector.setText(configuration.getAttribute(LaunchConfigurationAttributes.acrAlignJsonSelector,
					"alternative default from initializeFrom()"));
//			ddPool_Lp.setDefaultText(configuration.getAttribute(LaunchConfigurationAttributes.selectedPoolExtIntbiis_lp,
//					"alternative default from initializeFrom()"));
//			ddElscToAS_Lp.setDefaultText(
//					configuration.getAttribute(LaunchConfigurationAttributes.selectedElscToASExtIntbiis_lp,
//							"alternative default from initializeFrom()"));
			selectedTraceabilityReferenceFilePath = "";
			selectedADRWithSurroundingReferenceFilePath = "";
			selectedRoleModelWithADRReferenceFilePath = "";
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// hier werden die Werte in die config geschrieben.
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConfigurationAttributes.projectPath, selectedProjectTextfield.getText());
		configuration.setAttribute(LaunchConfigurationAttributes.selectedBpusageModel, ddUsage.getText());
		configuration.setAttribute(LaunchConfigurationAttributes.selectedOrgaModel, ddOrga.getText());
		configuration.setAttribute(LaunchConfigurationAttributes.selectedDataModel, ddData.getText());
		configuration.setAttribute(LaunchConfigurationAttributes.selectedSystem, ddSys.getText());
		configuration.setAttribute(LaunchConfigurationAttributes.selectedRepo, ddRepo.getText());
		configuration.setAttribute(LaunchConfigurationAttributes.selectedPoolExtIntbiis_lp, ddPool_Lp.getText());
		configuration.setAttribute(LaunchConfigurationAttributes.selectedElscToASExtIntbiis_lp,
				ddElscToAS_Lp.getText());
		configuration.setAttribute(LaunchConfigurationAttributes.generateDFDInformation,
				selectedDFDCheckbox.getSelection());
//		configuration.setAttribute(LaunchConfigurationAttributes.selectedACA,
//				Arrays.asList(selectedACA.getText().split("\\s*,\\s*")));
		configuration.setAttribute(LaunchConfigurationAttributes.selectedTraceabilityReferenceFilePath,
				selectedTraceabilityReferenceFilePath);
		configuration.setAttribute(LaunchConfigurationAttributes.selectedRoleModelWithADRReferenceFilePath,
				selectedRoleModelWithADRReferenceFilePath);
		configuration.setAttribute(LaunchConfigurationAttributes.selectedADRWithSurroundingReferenceFilePath,
				selectedADRWithSurroundingReferenceFilePath);
		configuration.setAttribute(LaunchConfigurationAttributes.selectedAcsAlignCheckbox,
				selectedAcsAlignCheckbox.getSelection());
		configuration.setAttribute(LaunchConfigurationAttributes.acrAlignJsonSelector,
				acrAlignJsonSelector.getText());
	}

	@Override
	public String getName() {
		return "Target Files";
	}

	/**
	 * In dieser Klasse wird der Projekt-Pfad des AUSGEW�HLTEN/SELECTED Projektes
	 * ermittelt. Es wird ein String zur�ckgegeben. Dieser enth�lt den Pfad bis zum
	 * Projekt-Ordner.
	 * 
	 * @return Returns the path to the selected project in your workspace as a
	 *         string
	 */
	private String getProjectPath() {
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				if (window.getSelectionService().getSelection() != null) {
					IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
					Object firstElement = selection.getFirstElement();
					if (firstElement instanceof IAdaptable) {
						IProject project = (IProject) ((IAdaptable) firstElement).getAdapter(IProject.class);
						IPath path = project.getLocation();
						String pathString = path.toString().replace("/", File.separator);
						return pathString;
					}
				}
			}

		} catch (IllegalArgumentException e) {
			System.out.println("Fehler in getProjectPath();");
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Scans the project and nested folders located at the given path for contained
	 * files. The mentioned files are: *.datamodel *.bpusagemodel *.system
	 * *.organizationenvironmentmodel *.intbiis_lp The names and pathes of the files
	 * where stored into a hashMap<String, String> called "fileToPathMap"
	 */
	private void scanProjectForFiles(String path) {

		if (path != null) {
			// scans the path of the selected project
			File project = new File(path);
			// get all the contained files of the projectPath
			String[] fileList = project.list();
			// iterate through the list of all files contained
			for (String fileName : fileList) {
				// Check if the file is a directory or not
				File file = new File(path + File.separator + fileName);
				if (file.isDirectory()) {
					// if it is a directory get its name and scan this folder for files
					scanProjectForFiles(file.getPath());
				} else {
					if (file.getName().contains(".datamodel") || file.getName().contains(".bpusagemodel")
							|| file.getName().contains(".system") || file.getName().contains(".repository")
							|| file.getName().contains(".organizationenvironmentmodel")
							|| file.getName().contains(".intbiis_lp") || file.getName().contains(".json")) {
						// Pattern.quote(File.separator) ist zum einfachen trennen anhand des
						// Betriebssystem-Trenners
						// File.seprator ist gleich zusetzten mit "\\". Das ist im Pfad der von
						// getParent zur�ck gegeben wird auch so.
						String[] splittedPath = file.getParent().split(Pattern.quote(File.separator));
						int counter = 1;
						String parentFolder = splittedPath[splittedPath.length - 1];
						while (!parentFolder.contains(rootFolder)) {
							counter++;
							parentFolder = splittedPath[splittedPath.length - counter] + File.separator + parentFolder;
						}
						fileToPathMap.put(parentFolder + File.separator + file.getName(), file.getPath());
					}
				}
			}
		}
		categoriseFile(fileToPathMap);
	}

	/**
	 * Takes the given hashMap and splits it into the different kinds for
	 * filename-extensions. Each extension get its own ArrayList<String>. They are
	 * used at the UI as the Combo-DropDown-Itemlist
	 * 
	 * @param fileToPathMap
	 *            is the hashMap that contains the filenames and the related pathes.
	 *            The names are the key and the pathes are the value
	 */
	private void categoriseFile(HashMap<String, String> fileToPathMap) {

		// Listen f�r die dropdown-Men�s
		ArrayList<String> bpusagemodelList = new ArrayList<>();
		ArrayList<String> organizationalList = new ArrayList<>();
		ArrayList<String> datamodelList = new ArrayList<>();
		ArrayList<String> systemList = new ArrayList<>();
		ArrayList<String> repositoryList = new ArrayList<>();
		ArrayList<String> intbiis_lpList_pools = new ArrayList<>();
		ArrayList<String> intbiis_lpList_elsctoas = new ArrayList<>();
		ArrayList<String> dataTypeUsageJson = new ArrayList<>();

		for (String key : fileToPathMap.keySet()) {
			if (key.substring(key.lastIndexOf(".")).equals(".bpusagemodel")) {
				bpusagemodelList.add(key);
			} else if (key.substring(key.lastIndexOf(".")).equals(".organizationenvironmentmodel")) {
				organizationalList.add(key);
			} else if (key.substring(key.lastIndexOf(".")).equals(".datamodel")) {
				datamodelList.add(key);
			} else if (key.substring(key.lastIndexOf(".")).equals(".system")) {
				systemList.add(key);
			} else if (key.substring(key.lastIndexOf(".")).equals(".repository")) {
				repositoryList.add(key);
			} else if (key.substring(key.lastIndexOf(".")).equals(".intbiis_lp") && key.toLowerCase().contains("elsctoas")) {
				intbiis_lpList_elsctoas.add(key);
			} else if (key.substring(key.lastIndexOf(".")).equals(".intbiis_lp") && key.toLowerCase().contains("pools")) {
				intbiis_lpList_pools.add(key);
			} else if (key.substring(key.lastIndexOf(".")).equals(".json") && key.toLowerCase().contains("datatypeusage")) {
				dataTypeUsageJson.add(key);	
			} else {
				continue;
			}
		}

		if (bpusagemodelList.size() > 1) {
			Collections.sort(bpusagemodelList, new Comparator<String>() {
				@Override
				public int compare(String first, String second) {
					return first.toLowerCase().compareTo(second.toLowerCase());
				}
			});
		}

		if (organizationalList.size() > 1) {
			Collections.sort(organizationalList);
		}

		if (datamodelList.size() > 1) {
			Collections.sort(datamodelList);
		}

		if (systemList.size() > 1) {
			Collections.sort(systemList);
		}

		if (repositoryList.size() > 1) {
			Collections.sort(repositoryList);
		}

		if (intbiis_lpList_pools.size() > 1) {
			Collections.sort(intbiis_lpList_pools);
		}

		if (intbiis_lpList_elsctoas.size() > 1) {
			Collections.sort(intbiis_lpList_elsctoas);
		}
		
		if (dataTypeUsageJson.size() > 1) {
			Collections.sort(dataTypeUsageJson);
		}

		filetype2FilesMap.put(".bpusagemodel", bpusagemodelList);
		filetype2FilesMap.put(".organizationenvironmentmodel", organizationalList);
		filetype2FilesMap.put(".datamodel", datamodelList);
		filetype2FilesMap.put(".system", systemList);
		filetype2FilesMap.put(".repository", repositoryList);
		filetype2FilesMap.put(".intbiis_lp_pool", intbiis_lpList_pools);
		filetype2FilesMap.put(".intbiis_lp_elsctoas", intbiis_lpList_elsctoas);
		filetype2FilesMap.put(".json", dataTypeUsageJson);

	}

	/**
	 * Casts the given ArrayList into a StringArray
	 * 
	 * @param arrayList
	 * @return String[] so that it can be used as a the Itemlist for the
	 *         DropDown-Menus
	 */
	private String[] listToArray(ArrayList<String> arrayList) {

		if (arrayList != null && !arrayList.isEmpty()) {
			String[] itemArray = new String[arrayList.size()];
			itemArray = arrayList.toArray(itemArray);
			return itemArray;
		} else {
			System.out.println("array mit Dateinamen ist leer");
			String[] emptyArray = new String[1];
			emptyArray[0] = "";
			return emptyArray;
		}
	}

	/**
	 * Evaluates and isolates the rootfolder of the project of the given path
	 * 
	 * @param path
	 * @return string wich contains the rootfolder of the given path
	 */
	private String getRootFolder(String path) {
		// Pattern.quote(File.separator) ist zum einfachen trennen anhand des
		// Betriebssystem-Trenners
		String[] rootPathSplitted = path.split(Pattern.quote(File.separator));
		String rootFolder = rootPathSplitted[rootPathSplitted.length - 1];
		System.out.println("RootFolder: " + rootFolder);
		return rootFolder;
	}

	/**
	 * The given Path is processed and evaluates the rootFolder of the Path (that
	 * means the selected folder from the browser). Afterwards all contained files
	 * and folder where read and the dropdowns from the UI going to be loaded with
	 * the found files
	 * 
	 * @param path
	 *            is the path to the selected project from the directoryBrowser
	 */
	private void processSelectedProject(String path) {

		rootFolder = getRootFolder(path);
		scanProjectForFiles(path);
		ddUsage.removeAll();
		ddUsage.setItems(listToArray(filetype2FilesMap.get(".bpusagemodel")));
		ddOrga.setItems(listToArray(filetype2FilesMap.get(".organizationenvironmentmodel")));
		ddOrga.select(0);
		ddData.setItems(listToArray(filetype2FilesMap.get(".datamodel")));
		ddData.select(0);
		ddSys.setItems(listToArray(filetype2FilesMap.get(".system")));
		ddSys.select(0);
		ddRepo.setItems(listToArray(filetype2FilesMap.get(".repository")));
		ddRepo.select(0);
		ddPool_Lp.removeAll();
		
		ddPool_Lp.setItems(listToArray(filetype2FilesMap.get(".intbiis_lp_pool")));
		ddElscToAS_Lp.removeAll();
		ddElscToAS_Lp.setItems(listToArray(filetype2FilesMap.get(".intbiis_lp_elsctoas")));
		ddElscToAS_Lp.select(listToArray(filetype2FilesMap.get(".intbiis_lp_elsctoas")).length);
		
		acrAlignJsonSelector.removeAll();
		acrAlignJsonSelector.setItems(listToArray(filetype2FilesMap.get(".json")));
		acrAlignJsonSelector.select(0);

		selectedTraceabilityReferenceFilePath = "";
		selectedADRWithSurroundingReferenceFilePath = "";
		selectedRoleModelWithADRReferenceFilePath = "";
	}
}
