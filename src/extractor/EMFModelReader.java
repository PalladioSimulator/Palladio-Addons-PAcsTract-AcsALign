package extractor;

import java.io.File;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.pcm.repository.RepositoryPackage;
import org.palladiosimulator.pcm.system.SystemPackage;

import de.uhd.ifi.se.pcm.bppcm.bpusagemodel.BpusagemodelPackage;
import de.uhd.ifi.se.pcm.bppcm.datamodel.DatamodelPackage;
import de.uhd.ifi.se.pcm.bppcm.organizationenvironmentmodel.OrganizationenvironmentmodelPackage;
import intBIIS_LP.model.intBIIS_LP.IntBIIS_LPPackage;

public class EMFModelReader {

	/**
	 * Reads an EMF model from a given path and return an object with its content
	 * @param path the fiel path to the model
	 * @return an object containing the content of the model
	 */
	public EObject getRessourceFromModel(String path) {

		//inits the meta classes from the external packages
		BpusagemodelPackage.eINSTANCE.eClass();
		OrganizationenvironmentmodelPackage.eINSTANCE.eClass();
		DatamodelPackage.eINSTANCE.eClass();
		SystemPackage.eINSTANCE.eClass();
		RepositoryPackage.eINSTANCE.eClass();
		IntBIIS_LPPackage.eINSTANCE.eClass();

		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;

		//adds the given models as a XMIRessourceFactoryImpl
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("bpusagemodel", new XMIResourceFactoryImpl());
		m.put("usagemodel", new XMIResourceFactoryImpl());
		m.put("organizationenvironmentmodel", new XMIResourceFactoryImpl());
		m.put("datamodel", new XMIResourceFactoryImpl());
		m.put("system", new XMIResourceFactoryImpl());
		m.put("repository", new XMIResourceFactoryImpl());
		m.put("intbiis_lp", new XMIResourceFactoryImpl());

		// Obtain a new resource set
		ResourceSet resSet = new ResourceSetImpl();
		// Get the resource
		URI uri = URI.createFileURI(new File(path).getAbsolutePath());
		Resource resource = resSet.getResource(uri, true);
		// Get the first model element and cast it to the right type, in my
		// example everything is hierarchical included in this first node
		EObject object = resource.getContents().get(0);
		return object;
	}
	

}
