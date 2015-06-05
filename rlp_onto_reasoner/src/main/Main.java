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
		String tableName = "wetness_10_noaquatic_validation_centroid"; //args[0];
		// SEaTH rules to use
		String rulesDir = "c:/Users/Moran/test-rlp/SEaTH/wetness_10_noaquatic_centroid";
		// number of rules to use
		int numRules = 4;	
		System.out.println("About to create OWL file from table: " + tableName);
		File owlFile = test.convertDB(tableName, rulesDir, "wetness", numRules); // , fields);
		String resultsTbl = owlFile.getName();
		resultsTbl = resultsTbl.substring(0, resultsTbl.lastIndexOf("."));
		resultsTbl = resultsTbl + "_results";
		System.out.println("successfully created: " + owlFile + " from " + tableName);
		String returnTbl = testReasoner.classifyOWL(owlFile, tableName, resultsTbl);
		if (returnTbl != resultsTbl){
			System.out.println("returned table and results table mismatch!");
		}
		System.out.println("results located in: " + returnTbl);
	}
}
