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

public class testReasoner {
	public static void createTable(defaultDict<Integer, List<String>> myDict, String tableName, String validationTable) {//HashMap<String, ArrayList<String>> myMap) {
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
			//String validationTable = tableName + "_results";
			System.out.println("going to create tableName: " + tableName + " with results: "+ validationTable);
			st.addBatch("CREATE TABLE " +  tableName + "as ("
					+ "select ogc_fid, wetness from " + validationTable); //(ogc_fid integer, classified VARCHAR(25));");
			st.addBatch("ALTER TABLE " + tableName + " ADD classified VARCHAR(25)");
			//st.addBatch("CREATE TABLE + " +  tableName + "(ogc_fid integer, classified VARCHAR(25));");
			st.executeBatch();
			for (Entry<Integer, List<String>> ee : myDict.entrySet()) {
				Integer key = ee.getKey();
				List<String> values = ee.getValue();
				System.out.println();
				System.out.println(key + ":");
				String new_value = Joiner.on("_").skipNulls().join(values);
				String query = "insert into " + tableName + "(ogc_fid, classified) values(" + key +
						",'" + new_value + "')";
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

	
	public static void main(String[] args) {
		OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		File file = new File(
				"C:\\Users\\Moran\\ontologies\\without_moist_mesic.owl");
		String tableName;
		OWLOntology onto = null;
		if (mgr == null || file == null) {
			System.out.println("ERROR!!");
		}
		HashMap<OWLClass,Set<OWLClass>> someValueFromAxioms = new HashMap<OWLClass,Set<OWLClass>>();

		System.out.println("Before try");
		try {
			tableName = file.getName();
			tableName = tableName.substring(0, tableName.lastIndexOf("."));
			onto = mgr.loadOntologyFromOntologyDocument(file);
			//OWLReasoner hermit = new Reasoner.ReasonerFactory().createReasoner(onto);
			OWLReasoner factplusplus = new FaCTPlusPlusReasonerFactory()
					.createReasoner(onto); 
			System.out.println(factplusplus.getReasonerVersion());
			factplusplus.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			HashMap<String, ArrayList<String>> classesHash = new HashMap<String, ArrayList<String>>();
			ArrayList<String> classList = new ArrayList<String>();
			classList.add("aquatic");
			classList.add("dry");
			/*classList.add("moist");*/
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
			
			createTable(dict, tableName, "validation_without_mesic_moist");
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} finally {
			System.out.println("ALL DONE!");
		}
	}
}