package test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.OWLObjectVisitorExAdapter;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
import ontology.ReasonerHermit;

class LabelExtractor extends OWLObjectVisitorExAdapter<String> implements OWLAnnotationObjectVisitorEx<String> {
	public String visit(OWLAnnotation annotation) { 
		if (annotation.getProperty().isLabel()) {
			OWLLiteral c = (OWLLiteral) annotation.getValue();
		return c.getLiteral();
		}
		return null;
	}
}

public class testReasoner {
	public static void createTable(HashMap<String, ArrayList<String>> myMap) {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String url = "jdbc:postgresql://localhost/RLP";
		String user = "postgres";
		String password = "BobtheBuilder";
		try {
			con = DriverManager.getConnection(url, user, password);
			con.setAutoCommit(false);
			st = con.createStatement();
			//st.executeQuery("'rlp_results'");
			st.addBatch("CREATE TABLE rlp_results(ogc_fid VARCHAR(25), water_regime VARCHAR(25), the_geom geometry(MultiPolygon,25832))");
			for (Entry<String, ArrayList<String>> ee : myMap.entrySet()) {
				String key = ee.getKey();
				ArrayList<String> values = ee.getValue();
				System.out.println();
				System.out.println(key + ":");
				for (String i : values) {
					String query = "insert into rlp_results values('" + i
							+ "','" + key + "')";
					System.out.println(query);
					st.addBatch(query);
				}
			}
			st.executeBatch();
			con.commit();

			// System.out.println("Commited: "+ counts.lenth +
			// " successfully!");

		} catch (SQLException ex) {
			Logger lgr = Logger.getLogger(testReasoner.class.getName());
			lgr.log(Level.SEVERE, ex.getMessage(), ex);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException ex) {
				Logger lgr = Logger.getLogger(testReasoner.class.getName());
				lgr.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
	}

	private static LabelExtractor le = new LabelExtractor();

	private static String labelFor(OWLEntity clazz, OWLOntology o){
		Set<OWLAnnotation> annotations = clazz.getAnnotations(o);
		for (OWLAnnotation anno: annotations){
			String result = anno.accept(le);
			if (result != null){
				return result;
			}
		}
		return clazz.getIRI().toString();
	}
	
	public static void main(String[] args) {
		OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		File file = new File(
				"C:\\Users\\Moran\\ontologies\\wetness_base.owl");
		OWLOntology onto = null;
		if (mgr == null || file == null) {
			System.out.println("ERROR!!");
		}
		HashMap<OWLClass,Set<OWLClass>> someValueFromAxioms = new HashMap<OWLClass,Set<OWLClass>>();

		System.out.println("Before try");
		try {
			onto = mgr.loadOntologyFromOntologyDocument(file);
			OWLReasoner hermit = new Reasoner.ReasonerFactory().createReasoner(onto);
			/*OWLReasoner factplusplus = new FaCTPlusPlusReasonerFactory()
					.createReasoner(onto); */
			//System.out.println(factplusplus.getReasonerVersion());
			//factplusplus.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			System.out.println(hermit.getReasonerVersion());
			HashMap<String, ArrayList<String>> classesHash = new HashMap<String, ArrayList<String>>();
			/*classesHash.put("waterlogged", new ArrayList<String>());
			classesHash.put("periodic_flooding", new ArrayList<String>());
			classesHash.put("riparian", new ArrayList<String>());
			classesHash.put("dry_or_seasonally_wet", new ArrayList<String>());
			*/
			classesHash.put("aquatic", new ArrayList<String>());
			classesHash.put("dry", new ArrayList<String>());
			classesHash.put("moist", new ArrayList<String>());
			classesHash.put("mesic", new ArrayList<String>());
			classesHash.put("wet", new ArrayList<String>());
			ArrayList<String> classList = new ArrayList<String>();
			classList.add("aquatic");
			classList.add("dry");
			classList.add("moist");
			classList.add("mesic");
			classList.add("wet");
			/*classList.add("waterlogged");
			classList.add("periodic_flooding");
			classList.add("riparian");
			classList.add("dry_or_seasonally_wet");*/
			
			for (OWLClass c : onto.getClassesInSignature()) {
				if (classList.isEmpty()){
					System.out.println("class list empty!");
					break;
				}
				if (c.getIRI().getFragment().equals(classList.remove(0))) {
					String currClass = c.getIRI().getFragment();
					System.out.println("CurrClass:" + currClass);
					//NodeSet<OWLNamedIndividual> instances = factplusplus
					//		.getInstances(c, false);
					NodeSet<OWLNamedIndividual> instances = hermit
							.getInstances(c, false);
					for (OWLNamedIndividual i : instances.getFlattened()) {
						// classesHash.get(currClass, )
						classesHash.get(currClass)
								.add(i.getIRI().getFragment());
						System.out.println(i.getIRI().getFragment());
					}
					System.out.println("Total: "
							+ instances.getFlattened().size());
				}
			}
			createTable(classesHash);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			System.out.println("ALL DONE!");
		}
	}
}
/*
				for (OWLNamedIndividual i : factplusplus.getInstances(c, false).getFlattened()) {
					System.out.println(labelFor(i, onto) + labelFor(c, onto));
					// get assertions:
					for (OWLObjectProperty op: onto.getObjectPropertiesInSignature()){
						NodeSet<OWLNamedIndividual> valuesNodeSet = factplusplus.getObjectPropertyValues(i, op);
						for (OWLNamedIndividual value : valuesNodeSet.getFlattened()){
							System.out.println(value.getIRI());
							//classesHash.get(currClass).add(i.getIRI().getFragment());
							System.out.println(i.getIRI().getFragment());
						}
					} */