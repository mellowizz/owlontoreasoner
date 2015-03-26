package ontology;

import java.sql.SQLException;
import java.util.Collection;


import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;



public class OntologyController{
	
	
	
	public Collection<ClassificationComponentOwlAPI> run(String region1, IRI destinationRegionIRI, String region2, IRI originRegionIRI, OWLReasoner reasoner, OWLOntologyManager manager) throws SQLException{
		
		@SuppressWarnings("rawtypes")
		ClassificationComponentList cCL = new ClassificationComponentList();
		@SuppressWarnings("unchecked")
		Collection<ClassificationComponentOwlAPI> classCompList = cCL.getClassificationComponentList(region1, destinationRegionIRI, reasoner, manager);

		return classCompList;
	}

}