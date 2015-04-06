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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
import ontology.ReasonerHermit;

public class testReasoner {
	public static void createTable(HashMap<String, ArrayList<String>> myMap) {
		// TODO Auto-generated method stub
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
			
			//System.out.println("Commited: "+ counts.lenth + " successfully!");

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
				"C:\\Users\\Moran\\ontologies\\EUNIS_ext_small.owl");
		OWLOntology onto = null;
		if (mgr == null || file == null) {
			System.out.println("ERROR!!");
		}
		System.out.println("Before try");
		try {
			onto = mgr.loadOntologyFromOntologyDocument(file);
			OWLReasoner factplusplus = new FaCTPlusPlusReasonerFactory()
					.createReasoner(onto);
			System.out.println(factplusplus.getReasonerVersion());
			HashMap<String, ArrayList<String>> classesHash = new HashMap<String, ArrayList<String>>();
			classesHash.put("waterlogged", new ArrayList<String>());
			classesHash.put("periodic_flooding", new ArrayList<String>());
			classesHash.put("riparian", new ArrayList<String>());
			classesHash.put("dry_or_seasonally_wet", new ArrayList<String>());
			// convert nodeset to lis?
			ArrayList<String> classList = new ArrayList<String>();
			classList.add("waterlogged");
			classList.add("periodic_flooding");
			classList.add("riparian");
			classList.add("dry_or_seasonally_wet");
			for (OWLClass c : onto.getClassesInSignature()) {
				if (classList.isEmpty())
					break;
				if (c.getIRI().getFragment().equals(classList.remove(0))) {
					String currClass = c.getIRI().getFragment();
					System.out.println("CurrClass:" + currClass);
					NodeSet<OWLNamedIndividual> instances = factplusplus
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
			System.out.println("STUB:");
		}
	}
}
