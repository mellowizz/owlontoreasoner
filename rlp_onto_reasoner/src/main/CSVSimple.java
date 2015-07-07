package main;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import test.testReasoner;
import databaseToOWL.DBToOWLIndividualConverter;

public class CSVSimple {
    public static void main(String[] args) throws SQLException, IOException{
        DBToOWLIndividualConverter test = new DBToOWLIndividualConverter();
        // table to classify on
        String tableName = "saarburg_wetness_all_gt20_300testing"; //args[0];
        // SEaTH rules to use
        String rule = "c:/Users/Moran/test-rlp/scikit-learn_rules/short_rules.csv";
        // number of rules to use
        int numRules = 3;	
        //System.out.println("About to create OWL file from table: " + tableName);
        File owlFile = test.convertDB(tableName, rule, "wetness", numRules); // , fields);
        //File owlFile = new File("C:/Users/Moran/ontologies/wetness_gt0_80pct_validation_8_DT_rules.owl");
        String resultsTbl = owlFile.getName();
        System.out.println(resultsTbl);
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
