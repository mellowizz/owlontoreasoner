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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import rlpUtils.RLPUtils;
import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;


import com.google.common.base.Joiner;

import dict.defaultDict;

public class testReasoner {
	
	public static void createTable(defaultDict<Integer, List<String>> myDict, String tableName, String validationTable) {
		createTable(myDict, tableName, validationTable, "NATFLO_wetness");
	}

	public static void createTable(defaultDict<Integer, List<String>> myDict, String tableName, String validationTable, String parameter) {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String url = "jdbc:postgresql://localhost:5432/rlp_spatial?user=postgres&password=BobtheBuilder";
	    ResultSet validClass = null;	
		try {
			con = DriverManager.getConnection(url);
			con.setAutoCommit(false);
			st = con.createStatement();
			//String validationTable = tableName + "_results";
			System.out.println("going to create tableName: " + tableName + " from validation table: "+ validationTable);
			st.execute("drop TABLE if exists " + tableName + ";");
			String createSql = "CREATE TABLE " +  tableName + "( id integer, \"" + parameter + "\" VARCHAR(25), classified VARCHAR(25), PRIMARY KEY(id));";
			System.out.println(createSql);
			st.executeUpdate(createSql);
			validClass = st.executeQuery("select id, \"" + parameter + "\" from " + validationTable);
			HashMap<Integer, String> validClasses = new HashMap <Integer, String>();
			while (validClass.next()){
				validClasses.put(validClass.getInt("id"), validClass.getString(parameter));
			}
			 for (Entry<Integer, List<String>> ee : myDict.entrySet()) {
				Integer key = ee.getKey();
				List<String> values = ee.getValue();
				System.out.println();
				System.out.println(key + ":");
				String new_value = Joiner.on("_").skipNulls().join(values);
				String query = "insert into " + tableName + "(id, \"" + parameter + "\", classified) values(" + key +
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

	public String classifyOWL() throws SQLException{
		//OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		mgr = this.manager;
		//OWLOntology onto = null;
		if (mgr == null) {
			System.out.println("ERROR!!");
		}
		

		System.out.println("Before try");
		try {
			System.out.println("reasoner about to load ontology");
			onto = mgr.loadOntologyFromOntologyDocument(fileName);
			OWLReasoner factplusplus = new FaCTPlusPlusReasonerFactory()
					.createReasoner(onto); 
			System.out.println(factplusplus.getReasonerVersion());
			HashMap<String, ArrayList<String>> classesHash = new HashMap<String, ArrayList<String>>();
			
			ArrayList<String> classList = RLPUtils.getDistinctValuesFromTbl(tableName, parameter);
			defaultDict<Integer, List<String>> dict = new defaultDict<Integer, List<String>>(ArrayList.class);
			for (OWLClass c : onto.getClassesInSignature())
			{
				if (classList.isEmpty()){ System.out.println("class list empty!"); break;}
				String currClass = c.getIRI().getFragment();
				if (currClass.contains("/")) {
					currClass = currClass.split("/")[1];
				}
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
				}
				else{
					continue;
				}
			}
			for (ArrayList<String> clazz: classesHash.values())
			{
				System.out.println(clazz.toString());
				System.out.println("Class size: " + clazz.size());
			}
			/* write results to DB */
			//String originalDataTable = "rlp_eunis_all_parameters";
			createTable(dict, resultsTbl, tableName, parameter); 
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} finally {
			System.out.println("ALL DONE!");
		}
		return resultsTbl; 
	}
}
