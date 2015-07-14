package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import owlAPI.OWLmap;
import owlAPI.OWLmap.owlRuleSet;

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

	//public defaultDict<String, List<OWLClassExpression>> CSVRules()
	public OWLmap CSVRules() 
			throws OWLOntologyCreationException, NumberFormatException,
			IOException, OWLOntologyStorageException {
		final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
				"csv", "CSV");
		
		CSVReader reader;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(this.docIRI);
		OWLDataFactory factory = manager.getOWLDataFactory();
		/* TODO: Bundle rules so we can introduce OR! */
		//HashMap<String, List<HashSet<OWLClassExpression>>> classesExpressions = new HashMap<String, List<HashSet<OWLClassExpression>>>();
		/*
		defaultDict<String, List<HashSet<OWLClassExpression>>> classesExpressions = new defaultDict<String, List<Hashset<OWLClassExpression>>>(
				ArrayList.class);
		*/
		
		final File csvFile = new File(this.directory);
		if (!extensionFilter.accept(csvFile)) {
			System.err.println("error: file doesn't end in .csv");
		}
        reader = new CSVReader(new FileReader(csvFile));
        List<String> classNames = Arrays.asList("aquatic", "dry", "mesic", "very_wet"); 
		//List<owlRuleSet>rulesSet = new List<OWLClassExpression>();
        String[] nextLine;
        int lineNum = 1;
        OWLDatatypeRestriction newRestriction = null;
        OWLDataProperty hasParameter = null;
        /* could make MAX_EXCLUSIVE set by if/else than emit right code */
        OWLmap owlRulesMap = new OWLmap();
        /* parse and add rule at same time?!*/
        Set<OWLClassExpression> ruleSet = new HashSet<OWLClassExpression>();
        int ruleCounter = 0;
        while ((nextLine = reader.readNext()) != null){
                //&& lineNum <= this.numRules) {
            String parameter = nextLine[0]; /* why has_? */
            if (classNames.contains(parameter)){
                /* add collected rules to class and clear rulesList */
            	try{
                    OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet (parameter, ruleCounter);
                    rule.addAll(ruleSet);
                    ruleSet.clear();
                    if (owlRulesMap.get(parameter) == null){
                        ArrayList <owlRuleSet> newRules = new ArrayList<owlRuleSet>();
                        newRules.add(rule);
                        owlRulesMap.put(parameter, newRules);
                        continue;
                    }
                    else{
                    	ruleCounter = 0;
                    	/* already seen this class! --update by or'ing the rules! */
                    	owlRulesMap.get(parameter).add(rule);
                    }
            	}catch (NullPointerException e){
            		e.printStackTrace();
            	} catch ( ArrayIndexOutOfBoundsException e){
            		e.printStackTrace();
            	}
            }
            try {
            	ruleCounter++;
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
			    ruleSet.add(newWetnessRestriction); 
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
		manager.saveOntology(ontology);
        return owlRulesMap;
	}
}
