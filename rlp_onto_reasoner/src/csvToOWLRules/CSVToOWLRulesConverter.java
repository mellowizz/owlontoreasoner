package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.opencsv.CSVReader;

import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import dict.defaultDict;

public class CSVToOWLRulesConverter {
	String directory;
	IRI docIRI;
	int numRules;

	public CSVToOWLRulesConverter(String csvDir, IRI documentIRI,
			int numberOfRules) {
		this.directory = csvDir;
		this.docIRI = documentIRI;
		this.numRules = numberOfRules;
	}

	public defaultDict<String, List<OWLClassExpression>> CSVRulesConverter()
			throws OWLOntologyCreationException, NumberFormatException,
			IOException {
		final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
				"csv", "CSV");
		final File file = new File(this.directory);
		CSVReader reader;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		defaultDict<String, List<OWLClassExpression>> classesExpressions = new defaultDict<String, List<OWLClassExpression>>(
				ArrayList.class);
		for (final File csvFile : file.listFiles()) {
			if (extensionFilter.accept(csvFile)) {
				reader = new CSVReader(new FileReader(csvFile));
				//List<String> classNames = Arrays.asList("aquatic", "dry", "mesic", "very_wet"); 
				String fileNameNoExt = FilenameUtils.removeExtension(csvFile
						.getName());
				String[] classNames = fileNameNoExt.split("-");
				System.out.println("class: " + classNames[0] + " target: "
						+ classNames[1]);
				String[] nextLine;
				int lineNum = 1;
				OWLDatatypeRestriction newRestriction = null;
				OWLDataProperty hasParameter = null;
				OWLDatatypeRestriction newRestrictionOpp = null;
				/* could make MAX_EXCLUSIVE set by if/else than emit right code */
				while ((nextLine = reader.readNext()) != null
						&& lineNum <= this.numRules) {
					String parameter = nextLine[0]; /* why has_? */
					if (parameter.equals("aquatic") || parameter.equals("dry")
							|| parameter.equals("mesic")
							|| parameter.equals("very_wet")) {
						System.out.println("saw wetness");
						// do something
						//System.out.println("adding: " + parameter + " rules");
						//classesExpressions.get(parameter).addAll(rulesList);
						continue;
					}
					try {
                        parameter = "has_" + parameter;
                        String direction = nextLine[2];
                        String threshold = nextLine[3];
                        System.out.println(parameter + " direction: " + direction
                                + " threshold: " + threshold);
                        hasParameter = factory.getOWLDataProperty(IRI.create("#"
                                + parameter));
						switch (direction) {
                            case ">=":
                                newRestriction = factory
                                        .getOWLDatatypeMinInclusiveRestriction(Double
                                                .parseDouble(threshold));
                                newRestrictionOpp = factory
                                        .getOWLDatatypeMaxExclusiveRestriction(Double
                                                .parseDouble(threshold));
                                break;
                            case "<=":
                                newRestriction = factory
                                        .getOWLDatatypeMaxInclusiveRestriction(Double
                                                .parseDouble(threshold));
                                
                                newRestrictionOpp = factory
                                        .getOWLDatatypeMinExclusiveRestriction(Double
                                                .parseDouble(threshold));
                                break;

                            case "<":
                                newRestriction = factory
                                        .getOWLDatatypeMaxExclusiveRestriction(Double
                                                .parseDouble(threshold));
                                
                                newRestrictionOpp = factory
                                        .getOWLDatatypeMinExclusiveRestriction(Double
                                                .parseDouble(threshold));
                                break;
                            case ">":
                                newRestriction = factory
                                        .getOWLDatatypeMinExclusiveRestriction(Double
                                                .parseDouble(threshold));
                                
                                newRestrictionOpp = factory
                                        .getOWLDatatypeMaxExclusiveRestriction(Double
                                                .parseDouble(threshold));
                                break;
                            default:
                                System.out
                                        .println("DID NOT FIND > or < in CSV file: "
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
						//
						OWLClassExpression newWetnessRestrictionOpp = factory
								.getOWLDataSomeValuesFrom(hasParameter,
										newRestrictionOpp);

						classesExpressions.get(classNames[0]).add(
								newWetnessRestriction);
						// add opposite rule:
						//classesExpressions.get(classNames[1]).add(
						//		newWetnessRestrictionOpp);
						lineNum++;
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("There are: " + classesExpressions.size()
				+ " keys: " + classesExpressions.keySet().size());
		return classesExpressions;
	}
}