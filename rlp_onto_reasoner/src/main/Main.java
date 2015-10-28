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
		String parameter = "NATFLO_usage_intensity";
		String tableName = "testing_" + parameter.toLowerCase() + "_600";
		//String tableName = "testing_natflo_depression"; //args[0];
        String rule = "c:/Users/Moran/test-rlp/scikit-learn_rules/training_natflo_usage_intensity_200.csv";
        //String ruleDir = "c:/Users/Moran/test-rlp/SEaTH/natflo_slope_position_200_seath";
		int numRules = 10;
		String algorithm = "dt";
		String colName = parameter;
		DBToOWLIndividualConverter test = new DBToOWLIndividualConverter("jdbc:postgresql://localhost:5432/RLP?user=postgres&password=BobtheBuilder",
				tableName, parameter, rule, colName, numRules, algorithm);
		File owlFile = test.convertDB(); //tableName, rule, "dt", numRules, parameter); // , fields);
		String resultsTbl = owlFile.getName();
		resultsTbl = resultsTbl.substring(0, resultsTbl.lastIndexOf("."));
		resultsTbl = resultsTbl + "_results";
		System.out.println("successfully created: " + owlFile + " from " + tableName);
		String returnTbl = testReasoner.classifyOWL(owlFile, tableName, resultsTbl, parameter);
		if (returnTbl != resultsTbl){
			System.out.println("returned table and results table mismatch!");
		}
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
			System.out.println("Executing: " + currLocation);
			
	        Process process = new ProcessBuilder(pythonLoc,
	                    currLocation + "/sql_utils/confusion_matrix.py", returnTbl).start();
		
	}
}