package databaseToOWL;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import owlAPI.Individual;
import owlAPI.OntologyClass;
import owlAPI.OntologyCreator;
import owlAPI.OntologyWriter;
import csvToOWLRules.CSVToOWLRulesConverter;
import dict.defaultDict;

public class DBToOWLIndividualConverter {

	public File convertDB(String tableName, String rulesDir, String colName, Integer numRules) // List<String> colNames)
			throws SQLException, IOException {
		LinkedHashSet<Individual> individuals;
		LinkedHashSet<OntologyClass> classes;
		//LinkedHashSet<OWLDataRangeFacetRestriction> rules;
		File owlFile = new File("C:/Users/Moran/ontologies/" + tableName
				+ "_" + numRules + "_rules.owl");
		try {
			// create ontology 
			OntologyCreator ontCreate = new OntologyCreator();
			String ontologyIRI = "http://www.user.tu-berlin.de/niklasmoran/EUNIS/"
					+ owlFile.getName().trim();
			ontCreate.createOntology(ontologyIRI, "version_1_0", owlFile);
			/* get classes and individuals */
			classes = createClassesfromDB(tableName, colName);
			individuals = createIndividualsFromDB(tableName);
			OntologyWriter ontWrite = new OntologyWriter(); //IRI.create(owlFile.toURI()));
			ontWrite.writeClasses(classes, IRI.create(owlFile.toURI()),
					IRI.create(ontologyIRI));
			ontWrite.writeIndividuals(individuals, IRI.create(owlFile.toURI()));
			//ontWrite.writeRules(rules, IRI.create(owlFile.toURI()));
			CSVToOWLRulesConverter therules = new CSVToOWLRulesConverter(rulesDir, IRI.create(owlFile.toURI()), numRules); // 3 rules
			defaultDict<String, List<OWLClassExpression>> rules = therules.CSVRulesConverter();
			ontWrite.writeAll(classes, individuals, rules, IRI.create(owlFile.toURI()), IRI.create(ontologyIRI));
		}

		catch (OWLOntologyStorageException e2) {
			throw new RuntimeException(e2.getMessage(), e2);
		}

		catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		finally{
			System.out.println("leaving convertDB");
		}
		return owlFile;
	}

	public LinkedHashSet<OntologyClass> createClassesfromDB(String tableName) throws IOException, SQLException{
		return createClassesfromDB(tableName, "wetness");
	}

	public LinkedHashSet<OntologyClass> createClassesfromDB(String tableName, String colName)
			throws IOException, SQLException {
		/* Read from DB */
		String url = "jdbc:postgresql://localhost/RLP?user=postgres&password=BobtheBuilder";
		Statement st = null;
		Connection db = null;
		LinkedHashSet<OntologyClass> eunisClasses = new LinkedHashSet<OntologyClass>();
		try {
			db = DriverManager.getConnection(url);
			st = db.createStatement();
			ResultSet rs = st.executeQuery("SELECT DISTINCT(" + colName + ") FROM " + tableName + " where " + colName + "!= ''"); // rlp_all_small
			while (rs.next()) {
				String parameter = rs.getString(colName);
				if (parameter == null){ continue;}
				OntologyClass eunisObj = new OntologyClass();
				System.out.println(colName);
				System.out.println(parameter);
				if (parameter.contains("/")){
						parameter = parameter.split("/")[1];
				}
				if (eunisClasses.contains(eunisObj.getName()) == false) {
					eunisObj.setName(parameter);
					eunisClasses.add(eunisObj);
					System.out.println("Added: " + parameter);
				}
			}
			String entries = "";
			for (OntologyClass c: eunisClasses){
				entries += c.getName() + " ";
			}
			System.out.println(entries);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException f){
			f.printStackTrace();
		} finally {
			if (st != null) {
				st.close();
			}
		}

		return eunisClasses;
	}

	private LinkedHashSet<Individual> createIndividualsFromDB(String tableName)
			throws SQLException {
		/* Read from DB */
		System.out.println("getting object values from DB!");
		String url = "jdbc:postgresql://localhost/RLP?user=postgres&password=BobtheBuilder";
		LinkedHashSet<Individual> individuals = new LinkedHashSet<Individual>();
		Statement st = null;
		Connection db = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;

		try {
			db = DriverManager.getConnection(url);
			st = db.createStatement();
			System.out.println("tableName: " + tableName);
			rs = st.executeQuery("SELECT * FROM " + tableName); // rlp_all_small
			rsmd = rs.getMetaData();
			int colCount = rsmd.getColumnCount();
			if (rsmd == null || colCount == 0 || rs == null) {
				System.out.println("ERROR: too few columns!");
			}
			// System.out.println("RS size: ");
			while (rs.next()) {
				// System.out.println("RS");
				ArrayList<Number> values = new ArrayList<Number>();
				ArrayList<String> DataPropertyNames = new ArrayList<String>();
				Individual individual = new Individual();
				for (int i = 1; i <= colCount; i++) {
					String colName = rsmd.getColumnName(i);
					if (rsmd.getColumnType(i) != java.sql.Types.DOUBLE
							|| colName.endsWith("id")) {
						// System.out.println("Skipping column: "+ colName);
						continue;
					}
					values.add(rs.getDouble(colName));
					DataPropertyNames.add("has_" + colName);
				}
				individual.setFID(rs.getInt("ogc_fid"));
				individual.setValues(values);
				individual.setDataPropertyNames(DataPropertyNames);
				// add to individuals
				individuals.add(individual);
				// System.out.println(individual.getDataPropertyNames() + " : "
				// + individual.getValues());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (st != null) {
				st.close();
			}
		}
		return individuals;
	}

	public static void main(String[] args) throws SQLException, IOException {
		DBToOWLIndividualConverter test = new DBToOWLIndividualConverter();

		String tableName = args[0];
		File existingOWLFile = new File(args[1]);
		// System.out.println(fields.toString());
		System.out.println("About to add: " + tableName + " to "
				+ existingOWLFile);
		test.convertDB(tableName, "c:/Users/Moran/test-rlp/SEaTH/wetness_10_noaquatic", "wetness", 3); // , fields);
		
	}
}
