package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.opencsv.CSVReader;

import owlAPI.OWLmap;
import owlAPI.OWLmap.owlRuleSet;

public class CSVToOWLRules {
	File directory;
	IRI docIRI;
	int numRules;

	public CSVToOWLRules(File csvDir, IRI documentIRI,
			int numberOfRules) {
		this.directory = csvDir;
		this.docIRI = documentIRI;
		this.numRules = numberOfRules;
	}

	public OWLmap CSVRules(File csvFile) 
			throws OWLOntologyCreationException, NumberFormatException,
			IOException, OWLOntologyStorageException {
		CSVReader reader=null;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(this.docIRI);
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLmap owlRulesMap  = null;
		reader = new CSVReader(new FileReader(csvFile));
		String[] nextLine;
		OWLDatatypeRestriction newRestriction = null;
		OWLDataProperty hasParameter = null;
		owlRulesMap = new OWLmap();
		Set<OWLClassExpression> ruleSet = new HashSet<OWLClassExpression>();
		//int ruleCounter = 0;
		/* only have rules that have more than 2 parameters? */
		while ((nextLine = reader.readNext()) != null){
			String parameter = nextLine[0]; /* why has_? */
				/* add collected rules to class and clear rulesList */
				try{
					OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet (parameter); //, ruleCounter);
					rule.addAll(ruleSet);
					ruleSet.clear();
					if (parameter.startsWith("0")){
						parameter = "true";
					} else if(parameter.startsWith("1")){
						parameter = "false";
					}
					if (owlRulesMap.get(parameter) == null){
						ArrayList <owlRuleSet> newRules = new ArrayList<owlRuleSet>();
						newRules.add(rule);
						owlRulesMap.put(parameter, newRules);
						continue;
					} else{
						//ruleCounter = 0;
						/* already seen this class! --update by or'ing the rules! */
						System.out.println("adding : " + parameter + " in rules");
						owlRulesMap.get(parameter).add(rule);
					}
				}catch (NullPointerException e){
					e.printStackTrace();
				} catch ( ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
			}
			try {
				//ruleCounter++;
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
			} catch (NullPointerException e) {
				e.printStackTrace();
			} catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
			}
		}
	manager.saveOntology(ontology);
	reader.close();
	//owlRulesMap = null;
    return owlRulesMap;
	}
}
