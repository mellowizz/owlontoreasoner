package main;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import owlAPI.OntologyCreator;
import rlpUtils.RLPUtils;

public class Main {

	public static ArrayList<String> getRulesList (File rulesDir){
		final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
				"csv", "CSV");
		int size = rulesDir.listFiles().length;
		ArrayList<String> rulesList = new ArrayList<String>(size);
		for (File ruleFile : rulesDir.listFiles()){
			String fileNameNoExt = FilenameUtils.removeExtension(ruleFile
					.getName());	
			rulesList.add(fileNameNoExt);
		}
		return rulesList;
	}
	public static void main(String[] args) throws SQLException, IOException, OWLOntologyCreationException{
		RLPUtils.getOsName();
		String tableName = "test";
		String homeDir = System.getProperty("user.home");
        File rulesDir = new File(homeDir +"/test-rlp/sci-kit_rules/");
        ArrayList<String> rulesList = getRulesList(rulesDir);
        System.out.println("Rules: " + rulesList.toString());
		int numRules = 8;
		String algorithm = "dt";
		File outFile = new File(homeDir+"/test-rlp/grassland_smallest.owl");
		//AddIndividuals 
		String csvClasses = homeDir+"/git/owlontoreasoner/rlp_onto_reasoner/data/rlp_eunis_key.csv";
		OntologyCreator ontCreate = new OntologyCreator(
		"jdbc:postgresql://localhost:5432/rlp_spatial?user=postgres&password=BobtheBuilder",
		tableName, rulesDir, numRules, algorithm, rulesList,
		outFile, csvClasses);
		String ontologyIRI = "http://www.user.tu-berlin.de/niklasmoran/" + outFile.getName().trim();
		try {
			ontCreate.createOntology(ontologyIRI, "version_1_0", outFile);
			ontCreate.loadOntology(ontologyIRI, "version_1_0", outFile);
		 } catch (OWLOntologyCreationException e) {
			 e.printStackTrace();
		 } catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		 }
		ontCreate.convertDB(); 
		//String resultsTbl = outFile.getName();
		//resultsTbl = resultsTbl.substring(0, resultsTbl.lastIndexOf("."));
		//resultsTbl = resultsTbl + "_results";
		//String returnTbl = ontCreate.classifyOWL(outFile); //outFile, tableName, resultsTbl, parameter);
		System.out.println("successfully created");
		/*
		for (String parameter : rulesList){
			System.out.println("results located in: " + returnTbl);
			File file = new File(".");
			String currLocation = file.getCanonicalPath();
			String workingDirectory = null;
			String OS = (System.getProperty("os.name")).toUpperCase();
			//to determine what the workingDirectory is.
			//if it is some version of Windows
			String pythonLoc = null;
			if (OS.contains("WIN")){
				workingDirectory = System.getenv("AppData");
				pythonLoc = "C:/Python27_64/WinPython-64bit-2.7.9.3/python-2.7.9.amd64/python.exe";
			} //Otherwise, we assume Linux or Mac
			else {
					workingDirectory = System.getProperty("user.home");
					pythonLoc = "python2";
				}
				System.out.println("Executing: " + 
				currLocation + "/sql_utils/confusion_matrix.py");
				
				Process process = new ProcessBuilder(pythonLoc,
							currLocation + "/sql_utils/confusion_matrix.py ", returnTbl).start();
		}*/
	}
}