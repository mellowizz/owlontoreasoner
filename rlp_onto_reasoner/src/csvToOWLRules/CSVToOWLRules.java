package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.opencsv.CSVReader;

import dict.defaultDict;

public class CSVToOWLRules {
	String directory;
	IRI docIRI;
	int numRules;

	public CSVToOWLRules(String csvDir, IRI documentIRI,
			int numberOfRules) {
		this.directory = csvDir;
		this.docIRI = documentIRI;
		this.numRules = numberOfRules;
	}

	public defaultDict<String, List<OWLClassExpression>> CSVRules()
			throws OWLOntologyCreationException, NumberFormatException,
			IOException {
		final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
				"csv", "CSV");
		
		CSVReader reader;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		defaultDict<String, List<OWLClassExpression>> classesExpressions = new defaultDict<String, List<OWLClassExpression>>(
				ArrayList.class);
		HashSet<OWLClassExpression>rulesList = new HashSet<OWLClassExpression>();
		
		final File csvFile = new File(this.directory);
		if (!extensionFilter.accept(csvFile)) {
			System.err.println("error: file doesn't end in .csv");
		}
        reader = new CSVReader(new FileReader(csvFile));
        List<String> classNames = Arrays.asList("aquatic", "dry", "mesic", "very_wet"); 
        String[] nextLine;
        int lineNum = 1;
        OWLDatatypeRestriction newRestriction = null;
        OWLDataProperty hasParameter = null;
        /* could make MAX_EXCLUSIVE set by if/else than emit right code */
        while ((nextLine = reader.readNext()) != null){
                //&& lineNum <= this.numRules) {
            String parameter = nextLine[0]; /* why has_? */
            if (classNames.contains(parameter)){
                /* add collected rules to class and clear rulesList */
            	System.out.println("adding: " + rulesList.size() + " rules");
                classesExpressions.get(parameter).addAll(rulesList);
                rulesList.clear();
                continue;
            }
            try {
            	/* bug!! */
            	parameter = "has_" + parameter;
                String direction = nextLine[1];
                String threshold = nextLine[2];
                System.out.println(parameter + " direction: " + direction
                        + " threshold: " + threshold);
                hasParameter = factory.getOWLDataProperty(IRI.create("#"
                        + parameter));
                switch(direction){
                    case "<=":
                        newRestriction = factory
                        .getOWLDatatypeMaxInclusiveRestriction(Double
                                .parseDouble(threshold));
                        break;

                    case "<":
                        newRestriction = factory
                        .getOWLDatatypeMaxExclusiveRestriction(Double
                                .parseDouble(threshold));
                        break;
                    case ">":
                        newRestriction = factory
                        .getOWLDatatypeMinExclusiveRestriction(Double
                                .parseDouble(threshold));
                        break;
                    default:
                        System.out.println("DID NOT FIND > or < in CSV file: "
                                + csvFile.getName());
                        break;
                }

                if (newRestriction == null || hasParameter == null) {
                    System.out.println("Something went wrong!!");
                    continue;
                }
                // add new data restriction!
                OWLClassExpression newWetnessRestriction = factory
                        .getOWLDataSomeValuesFrom(hasParameter,
                                newRestriction);
               rulesList.add(newWetnessRestriction); 
                // add opposite rule:
                // classesExpressions.get(classNames[1]).add(
                // newWetnessRestrictionOpp);
                lineNum++;
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch(ArrayIndexOutOfBoundsException e){
            	e.printStackTrace();
            }
        }
        System.out.println("There are: " + classesExpressions.size()
                + " keys: " + classesExpressions.keySet().size());
        return classesExpressions;
	}
}
