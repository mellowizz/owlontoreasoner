package main;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import test.testReasoner;
import databaseToOWL.DBToOWLIndividualConverter;

public class Main {
	
	public static void main(String[] args) throws SQLException, IOException{
		
		DBToOWLIndividualConverter test = new DBToOWLIndividualConverter();
		// table to classify on
		String tableName = "wetness_gt0_80pct_validation"; //args[0];
		// SEaTH rules to use
		String rulesDir = "c:/Users/Moran/test-rlp/SEaTH/wetness_gt20_20_train";
		// number of rules to use
		int numRules = 3;	
		//System.out.println("About to create OWL file from table: " + tableName);
		File owlFile = test.convertDB(tableName, rulesDir, "wetness", numRules); // , fields);
		//File owlFile = new File("C:/Users/Moran/ontologies/wetness_gt20_450_validation_1_rules.owl");
		String resultsTbl = owlFile.getName();
		resultsTbl = resultsTbl.substring(0, resultsTbl.lastIndexOf("."));
		resultsTbl = resultsTbl + "_results_gt20";
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
