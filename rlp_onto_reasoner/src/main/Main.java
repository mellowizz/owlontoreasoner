package main;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import test.testReasoner;
import databaseToOWL.DBToOWLIndividualConverter;

public class Main {
	
	public static void main(String[] args) throws SQLException, IOException{
		
		DBToOWLIndividualConverter test = new DBToOWLIndividualConverter();
		// table to classify on
		String tableName = "saarburg_200_testing_300_zstats"; //args[0];
		// SEaTH rules to use
		String rulesDir = "c:/Users/Moran/test-rlp/SEaTH/saarburg_seath_zstats"; // names are reversed
		// DT rule to use.
        String rule = "c:/Users/Moran/test-rlp/WEKA/PART_reduced_51.csv";
        //argmin_DT_3_rules.csv";
		// number of rules to use
		int numRules = 5;	
		//System.out.println("About to create OWL file from table: " + tableName);
		File owlFile = test.convertDB(tableName, rulesDir, "seath", numRules); // , fields);
		//File owlFile = new File("C:/Users/Moran/ontologies/saarburg_200_testing_300_zstats_1seath_rules.owl");
		String resultsTbl = owlFile.getName();
		resultsTbl = resultsTbl.substring(0, resultsTbl.lastIndexOf("."));
		resultsTbl = resultsTbl + "_results";
		System.out.println("successfully created: " + owlFile + " from " + tableName);
		String returnTbl = testReasoner.classifyOWL(owlFile, tableName, resultsTbl);
		if (returnTbl != resultsTbl){
			System.out.println("returned table and results table mismatch!");
		}
		System.out.println("results located in: " + returnTbl);
		
		
		/*
		List<String> command = new ArrayList<String>();
	
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		
		"C:/Users/Moran/git/rlp/dataPreprocessingTool/database/confusionMatrix.py"
		*/
	}
}
