package ontology;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class ClassificationComponentList<ClassifiactionComponentOwlAPI> {
	
	public HashMap<Number, ArrayList<String>> attributes;
	public ArrayList<String> superAttributes;
	public String database;
;

	public Collection<ClassificationComponentOwlAPI> getClassificationComponentList(String region, IRI regionIRI, OWLReasoner reasoner, OWLOntologyManager manager) throws SQLException{
			
		Collection<ClassificationComponentOwlAPI> classificationComponents = new LinkedList<ClassificationComponentOwlAPI>();
		dataBase.PostGIS_Database database = new dataBase.PostGIS_Database();
		ResultSet rs =database.loadAttributes(region);
			
		while(rs.next()){
			//System.out.println(rs.getString("primitiveName").trim());
			IRI primitiveIRI = IRI.create(regionIRI+"#"+rs.getString("primitiveName").trim());
			OWLDataFactory fac = manager.getOWLDataFactory();
			OWLClass component =fac.getOWLClass(primitiveIRI);
			ClassificationComponentOwlAPI clComp = ClassificationComponentOwlAPI.createClassificationComponent(component, reasoner);
			//System.out.println("/"+primitiveIRI+"/");
			clComp.inferSubClasses();
			clComp.inferEquivalentClasses();
			clComp.inferSuperClasses();
				//clComp.superClasses();
				//clComp.superClassesString();
			classificationComponents.add(clComp);
		}
			
		return classificationComponents;
	}
		public static void main(String[] args) throws SQLException {
			//ClassificationComponentList cCL = new ClassificationComponentList();
			//Collection<ClassificationComponentOwlAPI> classCompList = cCL.getClassificationComponentList("Flandern", null, null, null);
			//System.out.println(classCompList);
		}
}
