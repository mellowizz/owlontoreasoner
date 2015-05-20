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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import owlAPI.Individual;
import owlAPI.OntologyClass;
import owlAPI.OntologyCreator;
import owlAPI.OntologyWriter;

public class DBToOWLIndividualConverter {

	public void convertDB(String tableName) //List<String> colNames)
			throws SQLException, IOException {
		LinkedHashSet<Individual> individuals;
		LinkedHashSet<OntologyClass> classes;
		File owlFile = new File("C:/Users/Moran/ontologies/" +tableName + ".owl");
		try {
			classes = createClassesfromDB(tableName);
			individuals = createIndividualsFromDB(tableName);
			OntologyWriter ontWrite = new OntologyWriter();
			OntologyCreator ontCreate = new OntologyCreator();
			String ontologyIRI = "http://www.user.tu-berlin.de/niklasmoran/EUNIS/" + owlFile.getName().trim();
			ontCreate.createOntology(ontologyIRI, "version_1_0", owlFile);
			ontWrite.writeClasses(classes, IRI.create(owlFile.toURI()), IRI.create(ontologyIRI));
			ontWrite.writeIndividuals(individuals, IRI.create(owlFile.toURI()));
		}

		catch (OWLOntologyStorageException e2) {
			throw new RuntimeException(e2.getMessage(), e2);
		}

		catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	public LinkedHashSet<OntologyClass> createClassesfromDB(String tableName) throws IOException, SQLException {
		/* Read from DB */
		String url = "jdbc:postgresql://localhost/RLP?user=postgres&password=BobtheBuilder";
		Statement st = null;
		Connection db = null;
		LinkedHashSet<OntologyClass> eunisClasses = new LinkedHashSet<OntologyClass>();
		try {
			db = DriverManager.getConnection(url);
			st = db.createStatement();
			ResultSet rs = st.executeQuery("SELECT wetness FROM " + tableName); // rlp_all_small
			while (rs.next()) {
				OntologyClass eunisObj = new OntologyClass();
				String parameter = rs.getString("wetness");
				if (eunisClasses.contains(eunisObj.getName()) == false) {
					eunisObj.setName(parameter);
					eunisClasses.add(eunisObj);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (st != null) {
				st.close();
			}
		}

		return eunisClasses;
	}

	private LinkedHashSet<Individual> createIndividualsFromDB(String tableName) throws SQLException {
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
			if (rsmd == null || colCount == 0) {
				System.out.println("ERROR: too few columns!");
			}
			if (rs == null) {
				System.out.println(tableName + " is empty!!");
			}

			System.out.println("RS size: ");
			while (rs.next()) {
				System.out.println("RS");
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
				System.out.println(individual.getDataPropertyNames() + " : "
						+ individual.getValues());
			}
		} catch (SQLException e) {
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
		//System.out.println(fields.toString());
		System.out.println("About to add: " + tableName + " to "
				+ existingOWLFile);
		test.convertDB(tableName); //, fields);
	}
}
