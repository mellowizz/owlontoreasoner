package csvToOWLRules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import static org.semanticweb.owlapi.vocab.OWLFacet.MAX_EXCLUSIVE;
import static org.semanticweb.owlapi.vocab.OWLFacet.MIN_EXCLUSIVE;

import com.opencsv.CSVReader;

import dict.defaultDict;

public class CSVToOWLRulesConverter {
	String directory;
	IRI docIRI;
	int numRules;
		
	public CSVToOWLRulesConverter(String csvDir, IRI documentIRI, int numberOfRules){
		this.directory = csvDir;
		this.docIRI = documentIRI;
		this.numRules = numberOfRules;
	}
	
	public defaultDict<String, List<OWLClassExpression>> CSVRulesConverter() throws OWLOntologyCreationException, NumberFormatException, IOException{ 
		final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("csv", "CSV");
		final File file = new File(this.directory);
		CSVReader reader; // new CSVReader(new FileReader(csvFile));
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(this.docIRI);	
		OWLDataFactory factory = manager.getOWLDataFactory();
		//Set<OWLDatatypeRestriction> rules = null;
		OWLDatatype doubleDatatype = factory.getDoubleOWLDatatype();
		defaultDict<String, List<OWLClassExpression>> classesExpressions = new defaultDict<String, List<OWLClassExpression>>(ArrayList.class);
		for (final File csvFile: file.listFiles()){
			//Set dataRanges = new HashSet();
			Set<OWLObjectPropertyExpression> newAxioms = null;
			if (extensionFilter.accept(csvFile)){
				reader = new CSVReader(new FileReader(csvFile));
				String fileNameNoExt = FilenameUtils.removeExtension(csvFile.getName());
				String[] classNames = fileNameNoExt.split("_");
				System.out.println("class: " + classNames[1] + " target: " + classNames[2]); 
				String [] nextLine;
				int lineNum = 1;
				OWLDatatypeRestriction newRestriction = null;
				OWLFacetRestriction maxormin= null;
				OWLDataProperty hasParameter = null;
	            /* could make MAX_EXCLUSIVE set by if/else than emit right code */
	            while ((nextLine = reader.readNext())!=null && lineNum<= this.numRules){
					String parameter = "has_" + nextLine[0];
					String direction = nextLine[2];
					String threshold = nextLine[3];
					System.out.println(parameter + " direction: " + direction + " threshold: " + threshold);
					//OWLDatatype owlDouble = factory.getOWLDatat
					hasParameter = factory.getOWLDataProperty(IRI.create("#" + parameter));
					try{
						if (direction.equals("<")) {
							newRestriction = factory.getOWLDatatypeMaxExclusiveRestriction(Double.parseDouble(threshold));
						} else if (direction.equals(">")){
							newRestriction = factory.getOWLDatatypeMaxExclusiveRestriction(Double.parseDouble(threshold));
						}else{
							System.out.println("DID NOT FIND > or < in CSV file: " + csvFile.getName());
						}
						
						if (newRestriction == null || hasParameter == null){
							System.out.println("Something went wrong!!");
							continue;
						}
						// add new data restriction!
						OWLClassExpression newWetnessRestriction = factory.getOWLDataSomeValuesFrom(hasParameter, newRestriction);
						//classExpressions.add(newWetnessRestriction);
						classesExpressions.get(classNames[1]).add(newWetnessRestriction);
						// OWLClassExpression wetnessQuality = factory.getOWLObjectIntersectionOf(currClass, newWetnessRestriction);
						lineNum++;
					}
					catch (NullPointerException e){
						e.printStackTrace();
					}
				}
			}
		}
		//if (rules.isEmpty()){ System.out.println("!!!! rules are empty!!!"); }
			System.out.println("There are: " + classesExpressions.size() + " keys: " + classesExpressions.keySet().size());
			return classesExpressions;
	}
}