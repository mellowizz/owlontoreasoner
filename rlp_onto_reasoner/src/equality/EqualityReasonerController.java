package equality;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import ontology.ClassificationComponentOwlAPI;
import ontology.OntologyController;
import ontology.ReasonerHermit;

public class EqualityReasonerController {
	String URI;
	String originRegion;
	String destinationRegion;
	IRI originRegionIRI;
	IRI destinationRegionIRI;

	OWLOntology ont;
	
	
	

	public EqualityReasonerController(String uri, String originRegion, String originRegionIRI, String destinationRegion, String destinationRegionIRI){
		this.URI = uri;
		this.originRegion = originRegion;
		this.destinationRegion = destinationRegion;
		this.destinationRegionIRI = IRI.create(destinationRegionIRI);
		this.originRegionIRI = IRI.create(originRegionIRI);
		
	}
	
	public void run() throws SQLException, ClassNotFoundException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		IRI docIRI = IRI.create(URI);
		
		try {
			this.ont = manager.loadOntologyFromOntologyDocument(docIRI);
			System.out.println("Loaded " + ont.getOntologyID());
		} 
		catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		OWLReasoner reasoner = ReasonerHermit.createOWLReasoner(this.ont);
		
		OntologyController ontCont =new OntologyController();
		Collection<ClassificationComponentOwlAPI> list = ontCont.run(this.originRegion, this.originRegionIRI, this.destinationRegion, this.destinationRegionIRI, reasoner, manager);
	
		EqualityReasoner equ = new EqualityReasoner(list);
		HashMap<String, ArrayList<String>> transferClasses = equ.testEquality(equ.cClregion1, this.destinationRegion, reasoner);

		dataBase.PostGIS_Database db = new dataBase.PostGIS_Database();
		db.saveTransferValue(transferClasses, test.TestData.database);
	}
	
	public static void main(String[] args) throws SQLException, ClassNotFoundException{
		EqualityReasonerController equCont = new EqualityReasonerController(test.TestData.URI, test.TestData.originRegion, test.TestData.originRegionIRI, test.TestData.destinationRegion, test.TestData.destinationRegionIRI);
		equCont.run();
	}

}
