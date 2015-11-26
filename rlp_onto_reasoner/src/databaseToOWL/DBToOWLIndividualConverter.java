package databaseToOWL;

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

import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import csvToOWLRules.CSVToOWLRules;
import csvToOWLRules.CSVToOWLRulesConverter;
import owlAPI.Individual;
import owlAPI.OWLmap;
import owlAPI.OntologyClass;
import owlAPI.OntologyCreator;
import owlAPI.OntologyWriter;
import rlpUtils.RLPUtils;

public class DBToOWLIndividualConverter {
	private Connection dbConn = null;
	private String tableName = null;
	//private String parameter;
	private String rule;
	//private String colName;
	private int numRules;
	private String algorithm;
	private File ruleDir;
	private File owlOut;
	private ArrayList<String> rulesList;

	//String parameter, 
	public DBToOWLIndividualConverter(String url, String tableName, 
			File ruleDir, int numRules, String algorithm,
			ArrayList<String> rulesList, File owlOut) throws SQLException {
		this.dbConn = DriverManager.getConnection(url);
		this.tableName = tableName;
		this.ruleDir = ruleDir;
		this.numRules = numRules;
		this.algorithm = algorithm;
		this.rulesList = rulesList;
		this.owlOut = owlOut;
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
	
	public File convertDB() throws NumberFormatException, IOException, SQLException {
		LinkedHashSet<Individual> individuals;
		LinkedHashSet<OntologyClass> classes;
		String ontoFolder = null;
		if (RLPUtils.isLinux()) {
			ontoFolder = "/home/niklasmoran/ontologies/";
		} else if (RLPUtils.isWindows()) {
			ontoFolder = "C:/Users/Moran/ontologies/";
		} else {
			System.out.println("Sorry, unsupported OS!");
		}
		try {
			// create ontology
			OntologyCreator ontCreate = new OntologyCreator();
			String ontologyIRI = "http://www.user.tu-berlin.de/niklasmoran/grassland.owl";
			ontCreate.loadOntology(ontologyIRI, "version_1_0", this.owlOut);
			//ontologyIRI, "version_1_0", owlFile);
			/* get classes and individuals */
			classes = createClassesfromDB(); 
			individuals = createIndividualsFromDB("20000"); // tableName);
			//System.out.println("# of classes: " + classes.size() + " # of individuals : " + individuals.size());
			//OntologyWriter ontWrite = new OntologyWriter(); // IRI.create(owlFile.toURI()));
			//File owlFiles = new File(this.ruleDir);
			OWLmap rulesMap = null;
			CSVToOWLRules therules = null;
			ontCreate.writeAll(classes, individuals, this.rulesList);
			
			for (File ruleFile: this.ruleDir.listFiles()){
				String fileNameNoExt = FilenameUtils.removeExtension(ruleFile.getName());
				System.out.println("loading rule: " + fileNameNoExt);
				therules = new CSVToOWLRules(ruleFile, IRI.create(this.owlOut.toURI()), numRules);
				rulesMap = therules.CSVRules(ruleFile);
				ontCreate.writeRules(rulesMap, this.owlOut);
				//RLPUtils.getDistinctValuesFromTbl(this.tableName, ruleFile.getName().split(".")[0]));
				//ontWrite.writeAll(individuals, rulesMap, fileNameNoExt, IRI.create(this.owlOut.toURI()),
				//		IRI.create(ontologyIRI));
				rulesMap = null;
			}
			
		} catch (NullPointerException mye) {
			throw new NullPointerException(mye.getMessage());
		} catch (OWLOntologyStorageException e2) {
			throw new RuntimeException(e2.getMessage(), e2);
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			System.out.println("leaving convertDB");
		}
		return this.owlOut; //owlFile;
	}

	public File convertDB(ArrayList<String> rulesList) throws NumberFormatException, IOException, SQLException {
		LinkedHashSet<Individual> individuals;
		LinkedHashSet<OntologyClass> classes;
		String ontoFolder = null;
		if (RLPUtils.isLinux()) {
			ontoFolder = "/home/niklasmoran/ontologies/";
		} else if (RLPUtils.isWindows()) {
			ontoFolder = "C:/Users/Moran/ontologies/";
		} else {
			System.out.println("Sorry, unsupported OS!");
		}
		File owlFile = new File(ontoFolder + "grassland.owl");
		try {
			// create ontology
			OntologyCreator ontCreate = new OntologyCreator();
			String ontologyIRI = "http://www.user.tu-berlin.de/niklasmoran/" + owlFile.getName().trim();
			ontCreate.createOntology(ontologyIRI, "version_1_0", owlFile);
			/* get classes and individuals */
			classes = createClassesfromDB(); 
			individuals = createIndividualsFromDB("80000"); 
			System.out.println("# of classes: " + classes.size() + " # of individuals : " + individuals.size());
			OntologyWriter ontWrite = new OntologyWriter(); // IRI.create(owlFile.toURI()));
			OWLmap rulesMap = null;
			CSVToOWLRules therules = new CSVToOWLRules(rulesList, IRI.create(owlFile.toURI()), numRules);
			rulesMap = therules.CSVRules(RLPUtils.getDistinctValuesFromTbl(this.tableName, this.colName));
			/* if another parameter? */
			ontWrite.writeAll(classes, individuals, rulesMap, IRI.create(owlFile.toURI()),
					IRI.create(ontologyIRI));
		} catch (NullPointerException mye) {
			throw new NullPointerException(mye.getMessage());
		} catch (OWLOntologyStorageException e2) {
			throw new RuntimeException(e2.getMessage(), e2);
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			System.out.println("leaving convertDB");
		}
		return owlFile;
	}

	public LinkedHashSet<OntologyClass> createClassesfromDB() throws SQLException { // String
																					// tableName,
		// String colName) throws IOException, SQLException {
		/* Read from DB */
		Statement st = null;
		LinkedHashSet<OntologyClass> eunisClasses = new LinkedHashSet<OntologyClass>();
		try {
			st = this.dbConn.createStatement();
			for (File colName : this.ruleDir.listFiles()){
				String fileNameNoExt = FilenameUtils.removeExtension(colName.
						getName());
				String currCol = fileNameNoExt;
				ResultSet rs = st.executeQuery("SELECT DISTINCT( \"" + currCol
						+ "\") FROM " + tableName);
				while (rs.next()) {
					String parameter = rs.getString(currCol);
					if (parameter == null) {
						continue;
					}
					OntologyClass eunisObj = new OntologyClass();
					System.out.println(colName);
					// System.out.println(parameter);
					if (parameter.contains(" ")) {
						parameter = parameter.replace(" ", "_");
					}
					if (eunisClasses.contains(eunisObj.getName()) == false) {
						eunisObj.setName(parameter);
						eunisClasses.add(eunisObj);
						if (parameter == "0"){
							parameter = "false";
						} else if (parameter == "1"){
							parameter = "true";
						}
						System.out.println("Added: " + parameter);
					}
				}
				String entries = "";
				for (OntologyClass c : eunisClasses) {
					entries += c.getName() + " ";
				}
				System.out.println(entries);
			}
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
	
	public int getRowCount(String tableName) throws SQLException{
		ResultSet rowQuery = null;
		Statement st = null;
		int rowCount = -1;
		try {
			st = dbConn.createStatement();
			rowQuery = st.executeQuery("SELECT COUNT(*) FROM \"" + this.tableName + "\"");
			if (rowQuery.next()){
				rowCount = rowQuery.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (st != null) {
				st.close();
			}
		}
		return rowCount;
	}

	private LinkedHashSet<Individual> createIndividualsFromDB(String limit) throws SQLException { // String
		/* Read from DB */
		System.out.println("getting individuals from DB!");
		Statement st = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		LinkedHashSet<Individual> individuals = null;
		try {
			st = dbConn.createStatement();
			individuals = new LinkedHashSet<Individual>(getRowCount(this.tableName));
			System.out.println("tableName: " + this.tableName);
			rs = st.executeQuery("SELECT * FROM \"" + this.tableName + "\" LIMIT " + limit);
			rsmd = rs.getMetaData();
			int colCount = rsmd.getColumnCount();
			if (rsmd == null || colCount == 0 || rs == null) {
				System.out.println("ERROR: too few columns!");
			}
			// System.out.println("RS size: ");
			while (rs.next()) {
				HashMap<String, String> stringValues = new HashMap<String, String>(colCount);
				HashMap<String, Number> values = new HashMap<String, Number>(colCount);
				Individual individual = new Individual();
				for (int i = 1; i <= colCount; i++) {
					String colName = rsmd.getColumnName(i);
					if (colName.endsWith("id")) {
						continue;
					} else if (colName.startsWith("natflo") || colName.startsWith("eunis")
							|| colName.startsWith("eagle") || colName.equals("class_name")) {
						// if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR){
						String myValue = rs.getString(colName);
						if (myValue == null) {
							myValue = "";
						}
						stringValues.put("has_" + colName, myValue);
					} else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
						values.put("has_" + colName, rs.getDouble(colName));
					}
				}
				individual.setFID(rs.getInt("id"));
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