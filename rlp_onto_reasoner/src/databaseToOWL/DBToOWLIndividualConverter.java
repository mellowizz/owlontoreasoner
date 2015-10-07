package databaseToOWL;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashSet;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import owlAPI.Individual;
import owlAPI.OWLmap;
import owlAPI.OntologyClass;
import owlAPI.OntologyCreator;
import owlAPI.OntologyWriter;
import csvToOWLRules.CSVToOWLRules;
import csvToOWLRules.CSVToOWLRulesConverter;

public class DBToOWLIndividualConverter {
	private Connection dbConn = null;
	private String tableName = null;
	private String parameter;
	private String rule;
	private String colName;
	private int numRules;
	private String algorithm;
	
	public DBToOWLIndividualConverter(String url, String tableName, String parameter, String rule, String colName, int numRules, String algorithm) throws SQLException{
		this.dbConn = DriverManager.getConnection(url);
		this.tableName = tableName;
		this.parameter = parameter;
		this.rule = rule;
		this.colName = colName;
		this.numRules = numRules;
		this.algorithm = algorithm;
	}
	public boolean IsPathDirectory(String myPath) {
	    File test = new File(myPath);

	    // check if the file/directory is already there
	    if (!test.exists()) {
	        // see if the file portion it doesn't have an extension
	        return test.getName().lastIndexOf('.') == -1;
	    } else {
	        // see if the path that's already in place is a file or directory
	        return test.isDirectory();
	    }
	}


	public File convertDB() throws NumberFormatException, IOException, SQLException{ 
		LinkedHashSet<Individual> individuals;
		LinkedHashSet<OntologyClass> classes;
		File owlFile = new File("/home/niklasmoran/ontologies/" + this.tableName + "_"
				+ numRules + this.algorithm + "_rules.owl");
		try {
			// create ontology
			OntologyCreator ontCreate = new OntologyCreator();
			String ontologyIRI = "http://www.user.tu-berlin.de/niklasmoran/EUNIS/"
					+ owlFile.getName().trim();
			ontCreate.createOntology(ontologyIRI, "version_1_0", owlFile);
			/* get classes and individuals */
			classes = createClassesfromDB(); //this.tableName, colName);
			individuals = createIndividualsFromDB(); //tableName);
			System.out.println("# of classes: " + classes.size()
					+ " # of individuals : " + individuals.size());
			OntologyWriter ontWrite = new OntologyWriter(); // IRI.create(owlFile.toURI()));
			File file = new File(this.rule);
			OWLmap rulesMap = null;
			if (file.isDirectory()) {
				System.out.println("directory!");
				CSVToOWLRulesConverter therules = new CSVToOWLRulesConverter(
						this.rule, IRI.create(owlFile.toURI()), this.numRules);
				rulesMap = therules.CSVRulesConverter();
			} else if (file.isFile()) {
				CSVToOWLRules therules = new CSVToOWLRules(this.rule,
						IRI.create(owlFile.toURI()), numRules);
				rulesMap = therules.CSVRules();
			} else{
				System.out.println("can't determine if file or directory!");
			}
			/* if another parameter? */
			ontWrite.writeAll(classes, individuals, rulesMap, colName,
					IRI.create(owlFile.toURI()), IRI.create(ontologyIRI));
		}
		catch (NullPointerException mye){
			throw new NullPointerException(mye.getMessage());
		}
		catch (OWLOntologyStorageException e2) {
			throw new RuntimeException(e2.getMessage(), e2);
		}
		catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			System.out.println("leaving convertDB");
		}
		return owlFile;
	}

	public LinkedHashSet<OntologyClass> createClassesfromDB() throws SQLException{ //String tableName,
			//String colName) throws IOException, SQLException {
		/* Read from DB */
		Statement st = null;
		LinkedHashSet<OntologyClass> eunisClasses = new LinkedHashSet<OntologyClass>();
		try {
			st = this.dbConn.createStatement();
			ResultSet rs = st.executeQuery("SELECT DISTINCT( \"" + this.colName
					+ "\") FROM " + tableName); 
			while (rs.next()) {
				String parameter = rs.getString(this.colName);
				if (parameter == null) {
					continue;
				}
				OntologyClass eunisObj = new OntologyClass();
				System.out.println(this.colName);
				//System.out.println(parameter);
				if (parameter.contains(" ")) {
					parameter = parameter.replace(" ", "_");
				} 
				if (eunisClasses.contains(eunisObj.getName()) == false) {
					eunisObj.setName(parameter);
					eunisClasses.add(eunisObj);
					System.out.println("Added: " + parameter);
				}
			}
			String entries = "";
			for (OntologyClass c : eunisClasses) {
				entries += c.getName() + " ";
			}
			System.out.println(entries);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException f) {
			f.printStackTrace();
		} finally {
			if (st != null) {
				st.close();
			}
		}

		return eunisClasses;
	}

	private LinkedHashSet<Individual> createIndividualsFromDB() throws SQLException{ //String tableName)
			//throws SQLException {
		/* Read from DB */
		System.out.println("getting object values from DB!");
		LinkedHashSet<Individual> individuals = new LinkedHashSet<Individual>();
		Statement st = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;

		try {
			st = dbConn.createStatement();
			System.out.println("tableName: " + this.tableName);
			rs = st.executeQuery("SELECT * FROM \"" + this.tableName + "\""); // rlp_all_small
			rsmd = rs.getMetaData();
			int colCount = rsmd.getColumnCount();
			if (rsmd == null || colCount == 0 || rs == null) {
				System.out.println("ERROR: too few columns!");
			}
			// System.out.println("RS size: ");
			while (rs.next()) {
				HashMap<String, String> stringValues = new HashMap<String, String>();
				HashMap<String, Number> values = new HashMap<String, Number>();
				Individual individual = new Individual();
				for (int i = 1; i <= colCount; i++) {
					String colName = rsmd.getColumnName(i);
					if (colName.endsWith("id")){
						continue;
					} else if (colName.startsWith("NATFLO") || colName.startsWith("EUNIS") || colName.startsWith("EAGLE") || colName.equals("Class_name")){
						//if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR){
						String myValue = rs.getString(colName);
						if (myValue == null){
							myValue = "";
						}
						stringValues.put("has_"+ colName, myValue);
					} else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE){
						values.put("has_"+ colName, rs.getDouble(colName));
					}
				}
				individual.setFID(rs.getInt("ogc_fid"));
				individual.setValues(values);
				individual.setValueString(stringValues);
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
}
