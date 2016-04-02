package main;

import java.io.File;
import java.io.FilenameFilter;
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
        File[] files = rulesDir.listFiles(new FilenameFilter() {
            public boolean accept(File rulesDir, String name){
                return name.toLowerCase().endsWith(".csv");
            }
        });
        for (File ruleFile : files){
            String fileNameNoExt = FilenameUtils.removeExtension(ruleFile
                    .getName());    
            rulesList.add(fileNameNoExt);
        }
        return rulesList;
    }
    public static void main(String[] args) throws SQLException, IOException, OWLOntologyCreationException{
        RLPUtils.getOsName();
        String tableName = "test_may_grasslands";
        String homeDir = System.getProperty("user.home");
        File rulesDir = new File("/home/niklasmoran/git/data_mining_module/rules/");
        ArrayList<String> rulesList = getRulesList(rulesDir);
        System.out.println("Rules: " + rulesList.toString());
        int numRules = 20;
        String algorithm = "dt";
        File outFile = new File(homeDir+"/test-rlp/grassland_may.owl");
        //AddIndividuals 
        String csvClasses = homeDir+"/git/owlontoreasoner/rlp_onto_reasoner/data/rlp_eunis_key.csv";
        OntologyCreator ontCreate = new OntologyCreator(
        "jdbc:postgresql://localhost:5432/rlp_saarburg?user=postgres&password=BobtheBuilder",
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
        // create OWL file from DB
        ontCreate.convertDB(); 
        // classify create OWL file
        String returnTbl = ontCreate.classifyOWL(outFile); //outFile, tableName, resultsTbl, parameter);
    }
}
