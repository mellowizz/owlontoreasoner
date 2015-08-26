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
		String parameter = "NATFLO_wetness";
		String tableName = "testing_natflo_class_name_600"; //args[0];
		// SEaTH rules to use
		String rulesDir = "c:/Users/Moran/test-rlp/SEaTH/natflo_wetness_class_name_200_seath";
		// DT rule to use.
        //String rule = "c:/Users/Moran/test-rlp/scikit-learn_rules/saarburg_training_14_DT_rules.csv";
        String rule = "c:/Users/Moran/test-rlp/WEKA/testing_natflo_wetness_vegetation_type_1_CART.csv";
        //String rule = "c:/Users/Moran/tubCloud/Thesis/dt_natflo_slope_rules.csv";
        //argmin_DT_3_rules.csv";
		// number of rules to use
		int numRules = 10;	
		//System.out.println("About to create OWL file from table: " + tableName);
		File owlFile = test.convertDB(tableName, rulesDir, "seath", numRules, parameter); // , fields);
		//File owlFile = new File("C:/Users/Moran/ontologies/saarburg_testing_600_16PART_rules.owl");
		String resultsTbl = owlFile.getName();
		resultsTbl = resultsTbl.substring(0, resultsTbl.lastIndexOf("."));
		resultsTbl = resultsTbl + "_results";
		System.out.println("successfully created: " + owlFile + " from " + tableName);
		String returnTbl = testReasoner.classifyOWL(owlFile, tableName, resultsTbl, parameter);
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