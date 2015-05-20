package test;

import dict.defaultDict;
import com.google.common.base.Joiner;

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

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class testReasoner {
	public static void createTable(defaultDict<Integer, List<String>> myDict, String tableName, String validationTable) {//HashMap<String, ArrayList<String>> myMap) {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String url = "jdbc:postgresql://localhost/RLP";
		String user = "postgres";
		String password = "BobtheBuilder";
	    ResultSet validClass = null;	
		try {
			con = DriverManager.getConnection(url, user, password);
			con.setAutoCommit(false);
			st = con.createStatement();
			//String validationTable = tableName + "_results";
			System.out.println("going to create tableName: " + tableName + " from validation table: "+ validationTable);
			String createSql = "CREATE TABLE " +  tableName + "( ogc_fid integer, wetness VARCHAR(25), classified VARCHAR(25), PRIMARY KEY(ogc_fid));";
			System.out.println(createSql);
			st.executeUpdate(createSql);
			validClass = st.executeQuery("select ogc_fid, wetness from " + validationTable);
			HashMap<Integer, String> validClasses = new HashMap <Integer, String>();
			while (validClass.next()){
				validClasses.put(validClass.getInt("ogc_fid"), validClass.getString("wetness"));
			}
			 for (Entry<Integer, List<String>> ee : myDict.entrySet()) {
				Integer key = ee.getKey();
				List<String> values = ee.getValue();
				System.out.println();
				System.out.println(key + ":");
				String new_value = Joiner.on("_").skipNulls().join(values);
				String query = "insert into " + tableName + "(ogc_fid, wetness, classified) values(" + key +
						",'" + validClasses.get(key) +"','" + new_value + "');";
				
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
				if (validClass != null) validClass.close();
				if (rs != null) rs.close();
				if (st != null) st.close();
				if (con != null) con.close();
			} catch (SQLException ex) {
				Logger lgr = Logger.getLogger(testReasoner.class.getName());
				lgr.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
	}

	
	public static void main(String[] args) {
		OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		String fileName = "validation_wetness_10_gt50_new.owl";
		File file = new File(
				"C:\\Users\\Moran\\ontologies\\" + fileName);
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
				//if (classList.contains(currClass)) {
					NodeSet<OWLNamedIndividual> instances = factplusplus
							.getInstances(c, false);
					System.out.println("current class: " + currClass + " isEmpty? " + instances.isEmpty());
					for (OWLNamedIndividual i : instances.getFlattened()) {
						dict.get(Integer.parseInt(i.getIRI().getFragment())).add(currClass);
						System.out.println(i.getIRI().getFragment());
					}
					System.out.println("Total: "
							+ instances.getFlattened().size());
				//} else{
				//	continue;
				//}
			}
			for (ArrayList<String> clazz: classesHash.values()){
				System.out.println(clazz.toString());
				System.out.println("Class size: " + clazz.size());
			}
			/* write results to DB */
			//String originalDataTable = "rlp_eunis_all_parameters";
			String validationTable = "validation_wetness_10_gt50";
			String resultsTable = validationTable + "_results";
			createTable(dict, resultsTable, validationTable); 
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} finally {
			System.out.println("ALL DONE!");
		}
	}
}