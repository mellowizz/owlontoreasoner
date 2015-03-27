package ontology;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.HermiT.Reasoner;

public class ReasonerHermit{

	/**
	 * @param args
	 */
	
	OWLReasoner reasoner;
	
	public static OWLReasoner createOWLReasoner(OWLOntology ont){

		ReasonerHermit reasHer = new ReasonerHermit();
		OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);

		reasHer.reasoner = reasonerFactory.createReasoner(ont, config);
		reasHer.checkConsistency();
		
		return reasHer.reasoner;
		
		}
	
	public void checkConsistency(){
		
		reasoner.precomputeInferences();
		boolean consistent = reasoner.isConsistent();
		System.out.println("Consistent: " + consistent);
		System.out.println("\n");
		
	}
/*
	private void classify(){	
		try {
			this.reasoner.classifyTaxonomy();
			if (reasoner == null) {
				System.out.println("Skipping.. reasoner is null.");
			}
			
		} catch (ProtegeReasonerException e) {
			e.printStackTrace();
		}
	}
	
	private static ProtegeReasoner createPelletJenaReasoner(OWLModel owlModel) {

		// Get the reasoner manager and obtain a reasoner for the OWL model. 
		ReasonerManager reasonerManager = ReasonerManager.getInstance();

		//Get an instance of the Protege Pellet reasoner
		ProtegeReasoner reasoner = reasonerManager.createProtegeReasoner(owlModel, ProtegePelletJenaReasoner.class);

		return reasoner;
	}
	
	
	private static DefaultProtegeDIGReasoner createDIGReasoner(OWLModel owlModel) {
		final String REASONER_URL = "http://localhost:8085";

		// Get the reasoner manager and obtain a reasoner for the OWL model. 
		ReasonerManager reasonerManager = ReasonerManager.getInstance();

		DefaultProtegeDIGReasoner reasoner = (DefaultProtegeDIGReasoner) reasonerManager.createProtegeReasoner(owlModel, reasonerManager.getDefaultDIGReasonerClass());			 
		// Set the reasoner URL and test the connection - only for DIG 
		reasoner.setURL(REASONER_URL);

		if (!reasoner.isConnected()) {
			System.out.println("Reasoner not connected!");		
		}

		return reasoner;
	}
	
	private static ProtegeReasoner createPelletOWLAPIReasoner(OWLModel owlModel) {
	// Get the reasoner manager and obtain a reasoner for the OWL model. 
	ReasonerManager reasonerManager = ReasonerManager.getInstance();

	//Get an instance of the Protege Pellet reasoner
	ProtegeReasoner reasoner = reasonerManager.createProtegeReasoner(owlModel, ProtegePelletOWLAPIReasoner.class);

	return reasoner;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
*/
}