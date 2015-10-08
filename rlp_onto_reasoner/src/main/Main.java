package main;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import test.testReasoner;
import databaseToOWL.DBToOWLIndividualConverter;
import rlpUtils.RLPUtils;

public class Main {
	
	public static void main(String[] args) throws SQLException, IOException{
		RLPUtils.getOsName();
		// table to classify on
		String parameter = "NATFLO_homogeneity";
		String tableName = "testing_natflo_homogeneity_600"; //args[0];
        String rule = "c:/Users/Moran/test-rlp/scikit-learn_rules/training_natflo_homogeneity_200_cv.csv";
		int numRules = 8;
		String algorithm = "dt";
		String colName = parameter;
		DBToOWLIndividualConverter test = new DBToOWLIndividualConverter("jdbc:postgresql://localhost:5432/RLP?user=postgres&password=BobtheBuilder",
				tableName, parameter, rule, colName, numRules, algorithm);
		// SEaTH rules to use
		//String rulesDir = "C:/Users/Moran/test-rlp/SEaTH/natflo_wetness_eagle_vegetation_type_1_200";
		//String rule = "/home/niklasmoran/test-rlp/scikit-learn/training_natflo_usage_intensity_rules.csv";
		// DT rule to use.
        //String rule = "c:/Users/Moran/test-rlp/WEKA/testing_natflo_wetness_vegetation_type_1_CART.csv";
        //String rule = "c:/Users/Moran/tubCloud/Thesis/dt_natflo_slope_rules.csv";
        //argmin_DT_3_rules.csv";
		// number of rules to use
		//System.out.println("About to create OWL file from table: " + tableName);
		File owlFile = test.convertDB(); //tableName, rule, "dt", numRules, parameter); // , fields);
		// File owlFile = new File("/home/niklasmoran/ontologies/testing_natflo_wetness_class_name_600_8dt_rules.owl");
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