package extractor;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.Interface;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;

/**
 * Writes programmatically changes to the selected models
 * @author saubaer
 *
 */
public class PalladioACRAnnotator {

	
	List<String> paths;
	private String projectPath;

	/**
	 * Contructor for PalladioACRAnnotator with project path initialization and the given paths for the repository files	
	 * @param projectPath top level project path of the plugin
	 * @param paths list of all selected repository files
	 */
	public PalladioACRAnnotator(String projectPath, List<String> paths) {
		super();
		this.projectPath = projectPath;
		this.paths = paths;
		
		
	}
	
	/**
	 * Writes some test changes to the given repository files
	 */
	public void writeACA() {
		
		for (String path : paths) {
			//combines the project path with the file path to get one absolute path for the file
			String filePath = projectPath + path.substring(path.indexOf(File.separator));
			EMFModelReader emfReader = new EMFModelReader();
			//reading the repository model from the resource file
			Repository aca = (Repository) emfReader.getRessourceFromModel(filePath);
			EList<Interface> interfaces = aca.getInterfaces__Repository();
			
			//gets a datatype from the repository
			DataType dataType = aca.getDataTypes__Repository().get(3);
			
			
			//iterates through the elements of the repository and changes its values
			for (Interface i : interfaces) {
				System.out.println(i.getEntityName());
				if (i instanceof OperationInterface) {
					OperationInterface oI = (OperationInterface) i;
					EList<OperationSignature> signatures = oI.getSignatures__OperationInterface();
					for (OperationSignature signature : signatures) {
						signature.setEntityName("setToCreditCard");
						System.out.println(signature.getParameters__OperationSignature());
						System.out.println(signature.getReturnType__OperationSignature());
						signature.setReturnType__OperationSignature(dataType);
					}
				}
			}
	
			//gets the resource of the model And adds the changed repository
			ResourceSet resSet = new ResourceSetImpl();
			URI uri = URI.createFileURI(new File(filePath).getAbsolutePath());
			Resource resource = resSet.getResource(uri, true);
	        resource.getContents().add(0,aca);
	        
	        //saves the changes to the model file
			try {
				resource.save(Collections.EMPTY_MAP);
				System.out.println("model successful saved");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
		
		
	}
	
	
	

}
