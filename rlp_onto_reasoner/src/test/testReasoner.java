package test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLObjectVisitorExAdapter;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

import com.google.common.base.Joiner;

//import ontology.ReasonerHermit;
import dict.defaultDict;

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
	public static void createTable(defaultDict<Integer, List<String>> myDict) {//HashMap<String, ArrayList<String>> myMap) {
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
			st.addBatch("CREATE TABLE theresult(ogc_fid integer, classified VARCHAR(25));");
			//st.addBatch("SELECT AddGeometry Column 'the_results', the_geom, 25832, 'MultiPolygon', ")
			for (Entry<Integer, List<String>> ee : myDict.entrySet()) {
				Integer key = ee.getKey();
				List<String> values = ee.getValue();
				System.out.println();
				System.out.println(key + ":");
				String new_value = Joiner.on("_").skipNulls().join(values);
				/*
				for (String i : values) {
					new_value += i;
					//
				}*/
				String query = "insert into theresult values(" + key
						+ ",'" + new_value + "')";
				System.out.println(query);
				st.addBatch(query);
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
			//OWLReasoner hermit = new Reasoner.ReasonerFactory().createReasoner(onto);
			OWLReasoner factplusplus = new FaCTPlusPlusReasonerFactory()
					.createReasoner(onto); 
			System.out.println(factplusplus.getReasonerVersion());
			factplusplus.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			//System.out.println(hermit.getReasonerVersion());
			HashMap<String, ArrayList<String>> classesHash = new HashMap<String, ArrayList<String>>();
			/*classesHash.put("waterlogged", new ArrayList<String>());
			classesHash.put("periodic_flooding", new ArrayList<String>());
			classesHash.put("riparian", new ArrayList<String>());
			classesHash.put("dry_or_seasonally_wet", new ArrayList<String>());
			classesHash.put("aquatic", new ArrayList<String>());
			classesHash.put("dry", new ArrayList<String>());
			classesHash.put("moist", new ArrayList<String>());
			classesHash.put("mesic", new ArrayList<String>());
			classesHash.put("wet", new ArrayList<String>());
			*/
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
			defaultDict<Integer, List<String>> dict = new defaultDict<Integer, List<String>>(ArrayList.class);
			for (OWLClass c : onto.getClassesInSignature()) {
				if (classList.isEmpty()){
					System.out.println("class list empty!");
					break;
				}
				
				String currClass = c.getIRI().getFragment();			
				System.out.println("current class: " + currClass);
				if (classList.contains(currClass)) {
					NodeSet<OWLNamedIndividual> instances = factplusplus
							.getInstances(c, false);
					System.out.println("current class: " + currClass + " isEmpty? " + instances.isEmpty());
					for (OWLNamedIndividual i : instances.getFlattened()) {
						//classesHash.get(currClass)
						//		.add(i.getIRI().getFragment());
						dict.get(Integer.parseInt(i.getIRI().getFragment())).add(currClass);
						System.out.println(i.getIRI().getFragment());
					}
					System.out.println("Total: "
							+ instances.getFlattened().size());
				} else{
					continue;
				}
			}
			for (ArrayList<String> clazz: classesHash.values()){
				System.out.println(clazz.toString());
				System.out.println("Class size: " + clazz.size());
			}
			/* write results to DB */
			//createTable(classesHash);
			createTable(dict);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} finally {
			System.out.println("ALL DONE!");
		}
	}
}