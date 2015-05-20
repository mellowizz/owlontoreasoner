package csvToOWLRules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import static org.semanticweb.owlapi.vocab.OWLFacet.MAX_EXCLUSIVE;
import static org.semanticweb.owlapi.vocab.OWLFacet.MIN_EXCLUSIVE;

import com.opencsv.CSVReader;

public class CSVToOWLRulesConverter {

	public CSVToOWLRulesConverter(String csvDir, IRI documentIRI) throws OWLOntologyCreationException, NumberFormatException, IOException, OWLOntologyStorageException{
		final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("csv", "CSV");
		final File file = new File(csvDir);
		CSVReader reader; // new CSVReader(new FileReader(csvFile));
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(documentIRI);	
		OWLDataFactory factory = manager.getOWLDataFactory();
		//Set<OWLDatatypeRestriction> rules = null;
		OWLDatatype doubleDatatype = factory.getDoubleOWLDatatype();
		for (final File csvFile: file.listFiles()){
			Set dataRanges = new HashSet();
			Set classExpressions = new HashSet();
			//Set<OWLAxiom> newAxioms = null;
			Set<OWLObjectPropertyExpression> newAxioms = null;
			if (extensionFilter.accept(csvFile)){
				reader = new CSVReader(new FileReader(csvFile));
				String fileNameNoExt = FilenameUtils.removeExtension(csvFile.getName());
				String[] classNames = fileNameNoExt.split("_");
				System.out.println("class: " + classNames[1] + " target: " + classNames[2]); 
				String [] nextLine;
				int lineNum = 1;
				OWLDatatypeRestriction newRestriction = null;
	            OWLDataProperty hasParameter = null; 
				while ((nextLine = reader.readNext())!=null && lineNum<=4){
					String parameter = "has_" + nextLine[0];
					String direction = nextLine[2];
					String threshold = nextLine[3];
					System.out.println(parameter + " direction: " + direction + " threshold: " + threshold);
					//OWLDatatype owlDouble = factory.getOWLDatat
					hasParameter = factory.getOWLDataProperty(IRI.create(documentIRI + "#" + parameter));
					
					try{
						if (direction.equals("<")){
							newRestriction = factory.getOWLDatatypeRestriction(doubleDatatype, factory.getOWLFacetRestriction(MAX_EXCLUSIVE, Double.parseDouble(threshold)));
							//rules.add(factory.getOWLDatatypeMaxExclusiveRestriction(Double.parseDouble(threshold)));
							//rules.add(factory.getOWLDatatypeMaxExclusiveRestriction(Double.parseDouble(threshold)));
						} else if (direction.equals(">")){
							//rules.add(factory.getOWLDatatypeMinExclusiveRestriction(Double.parseDouble(threshold)));
							newRestriction = factory.getOWLDatatypeRestriction(doubleDatatype, factory.getOWLFacetRestriction(MIN_EXCLUSIVE, Double.parseDouble(threshold)));
						}else{
							System.out.println("DID NOT FIND > or < in CSV file: " + csvFile.getName());
						}
						if (newRestriction == null || hasParameter == null){
							System.out.println("Something went wrong!!");
							continue;
						}
						// add new data restriction!
						OWLClassExpression newWetnessRestriction = factory.getOWLDataSomeValuesFrom(hasParameter, newRestriction);
						classExpressions.add(newWetnessRestriction);
						// OWLClassExpression wetnessQuality = factory.getOWLObjectIntersectionOf(currClass, newWetnessRestriction);
			            
						lineNum++;
					}
					catch (NullPointerException e){
						e.printStackTrace();
					}
				}
				OWLClass currClass = factory.getOWLClass(IRI.create(documentIRI + "#" + classNames[1]));
				
						OWLObjectIntersectionOf intersection = factory.getOWLObjectIntersectionOf(classExpressions);// IntersectionOf( dataRanges);
			            //newAxioms.add(wetnessQuality);
			            //OWLAxiom newAx = factory.getOWLEquivalentObjectPropertiesAxiom(wetnessQuality);
			            //OWLAxiom newDefinition = factory.getOWLEquivalentClassesAxiom(currClass, wetnessQuality);
			            //manager.addAxiom(ontology, newDefinition);
						manager.addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(currClass, intersection));
						//manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(currClass, intersection));
			}
		}
		//if (rules.isEmpty()){ System.out.println("!!!! rules are empty!!!"); }
		manager.saveOntology(ontology);
	}
}
