package owlAPI;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
//import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import com.google.common.base.Joiner;
import com.opencsv.CSVReader;

import dict.defaultDict;
import owlAPI.OWLmap.owlRuleSet;
import rlpUtils.RLPUtils;
import test.testReasoner;
import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

public class OntologyCreator {
	/*
	 * 
	 * Creates ontology with onotology IRI and saves it to input OWL File
	 */
	private OWLOntologyManager manager;
	OWLOntology ontology;
	private IRI ontologyIRI;
	private OWLOntologyID newOntologyID;
	private IRI versionIRI;
	private IRI documentIRI;
	private OWLmap owlRulesMap = new OWLmap();
	private Set<OWLClass> definedOWLClass = new HashSet<OWLClass>();
	private Connection dbConn;
	private String tableName;
	private File ruleDir;
	private String algorithm;
	private File owlOut;
	private ArrayList<String> rulesList;
	private int numRules;
	private String classesFile;

	public OntologyCreator(String url, String tableName, File ruleDir, int numRules, String algorithm,
			ArrayList<String> rulesList, File owlOut, String csvClasses) throws SQLException {
		this.dbConn = DriverManager.getConnection(url);
		this.tableName = tableName;
		this.ruleDir = ruleDir;
		this.numRules = numRules;
		this.algorithm = algorithm;
		this.rulesList = rulesList;
		this.owlOut = owlOut;
		this.classesFile = csvClasses;
	}

	public void loadOntology(String ontologyIRIasString, String version, File owlFile)
			throws OWLOntologyCreationException, OWLOntologyStorageException {
		 try {
		this.manager = OWLManager.createOWLOntologyManager();
		// PriorityCollection<OWLOntologyIRIMapper> iriMappers =
		// manager.getIRIMappers();
		this.ontologyIRI = IRI.create(ontologyIRIasString);

		this.documentIRI = IRI.create(owlFile.toURI());

		this.ontology = manager.loadOntologyFromOntologyDocument(owlFile);

		this.versionIRI = IRI.create(ontologyIRI + "/version1");

		this.newOntologyID = new OWLOntologyID(ontologyIRI, versionIRI);

		// this.setOntologyID = new SetOntologyID(ontology, newOntologyID);
		// manager.applyChange(setOntologyID);

		save(ontologyIRI, ontology, owlFile);
		this.documentIRI = IRI.create(owlFile.toURI());
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createOntology(String ontologyIRIasString, String version, File owlFile)
			throws OWLOntologyCreationException, OWLOntologyStorageException {

		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

			IRI ontologyIRI = IRI.create(ontologyIRIasString);

			IRI documentIRI = IRI.create(owlFile);

			SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
			manager.addIRIMapper(mapper);

			OWLOntology ontology = manager.createOntology(ontologyIRI);

			IRI versionIRI = IRI.create(ontologyIRI + "/version1");

			//OWLOntologyID newOntologyID = new OWLOntologyID(ontologyIRI, versionIRI);

			//SetOntologyID setOntologyID = new SetOntologyID(ontology, newOntologyID);
			//manager.applyChange(setOntologyID);
			System.out.println("Ontology created: " + ontology);
			save(ontologyIRI, ontology, owlFile);
		} catch (OWLOntologyCreationException e) {
			System.out.println("Could not load ontology: " + e.getMessage());
		 }
	}

	public void save(IRI ontologyIRI, OWLOntology ontology, File owlFile)
			throws OWLOntologyCreationException, OWLOntologyStorageException {
		
		OWLXMLDocumentFormat owlxmlFormat = new OWLXMLDocumentFormat();
        /* Save OWL to file */
        manager.saveOntology(ontology, owlxmlFormat,
                IRI.create(owlFile.toURI()));
	}

	public void writeRules(OWLmap rules) //, File owlFile)
			throws OWLOntologyCreationException, OWLOntologyStorageException {
		// OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		// OWLOntology ontology =
		//ontology = this.manager.loadOntologyFromOntologyDocument(this.documentIRI);
		OWLDataFactory factory = this.manager.getOWLDataFactory();
		SimpleIRIMapper mapper = new SimpleIRIMapper(this.ontologyIRI, this.documentIRI);
		manager.addIRIMapper(mapper);
		PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString());

		/* write rules */
		OWLClassExpression firstRuleSet = null;
		OWLClass owlCls = null;
		OWLObjectUnionOf totalunion = null;
		Iterator it = rules.map.entrySet().iterator();
		Set<OWLClassExpression> unionSet = new HashSet<OWLClassExpression>();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String currCls = (String) pair.getKey();
			owlCls = factory.getOWLClass(IRI.create("#" + currCls));
			ArrayList<owlRuleSet> currRuleset = (ArrayList<owlRuleSet>) pair.getValue();
			for (int i = 0; i < currRuleset.size(); i++) {
				firstRuleSet = factory.getOWLObjectIntersectionOf(currRuleset.get(i).getRuleList(currCls));
				unionSet.add(firstRuleSet);
			}
			totalunion = factory.getOWLObjectUnionOf(unionSet);
			unionSet.clear();
			manager.addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(owlCls, totalunion));
		}
		manager.saveOntology(ontology);
	}

	public void writeAll(//LinkedHashSet<OntologyClass> classes,
			LinkedHashSet<Individual> individuals, OWLmap rules)//, IRI documentIRI,
			throws OWLOntologyCreationException,
			OWLOntologyStorageException {
		
		OWLDataFactory factory = this.manager.getOWLDataFactory();
		//SimpleIRIMapper mapper = new SimpleIRIMapper(this.ontologyIRI, this.documentIRI);
		//manager.addIRIMapper(mapper);
		PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString());
		System.out.println("# of individuals: " + individuals.size());

		for (Individual ind : individuals) {
			Integer index = 0;

			OWLNamedIndividual obj = factory.getOWLNamedIndividual(
					"#" + ind.getFID(), pm);

			for (Entry<String, Number> entry : ind.getValues().entrySet()){
				OWLDataProperty dataProp = factory.getOWLDataProperty("#"
						+ entry.getKey(), pm);

				OWLDatatype doubleDatatype = factory
						.getOWLDatatype(OWL2Datatype.XSD_DOUBLE.getIRI());

				OWLLiteral literal = factory.getOWLLiteral(entry.getValue().toString(),
						doubleDatatype);

				OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory
						.getOWLDataPropertyAssertionAxiom(dataProp, obj,
								literal);
				manager.applyChange(new AddAxiom(ontology, dataPropertyAssertion));
				index = index + 1;
			}
			index = 0;
			for (Entry<String, String> entry : ind.getStringValues().entrySet()){
				OWLDataProperty dataProp = factory.getOWLDataProperty("#"
						+ entry.getKey(), pm);

				OWLDatatype stringDatatype = factory
						.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
				//System.out.println("about to write: " + value);
				String value = entry.getValue();
				if (value == null){
					value = "";
				}
				OWLLiteral literal = factory.getOWLLiteral(value,
						stringDatatype);

				OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory
						.getOWLDataPropertyAssertionAxiom(dataProp, obj,
								literal);
				manager.applyChange(new AddAxiom(ontology, dataPropertyAssertion));
				index = index + 1;
			}
		}
		/* write rules */
		OWLClassExpression firstRuleSet= null;
		OWLClass owlCls = null;
		OWLObjectUnionOf totalunion = null;
		Iterator it = rules.map.entrySet().iterator();
		Set<OWLClassExpression> unionSet = new HashSet<OWLClassExpression>();
		while (it.hasNext()){
			Map.Entry pair = (Map.Entry) it.next();
			String currCls = (String) pair.getKey();
			owlCls = factory.getOWLClass(IRI.create("#" + currCls ));
			System.out.println("CurrCls for rule: " + currCls);
			ArrayList<owlRuleSet> currRuleset = (ArrayList<owlRuleSet>) pair.getValue();
			for (int i=0; i< currRuleset.size(); i++){
				firstRuleSet = factory.getOWLObjectIntersectionOf(currRuleset.get(i).getRuleList(currCls));
				unionSet.add(firstRuleSet);
			}
			totalunion = factory.getOWLObjectUnionOf(unionSet);
			unionSet.clear();
			manager.addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(owlCls, totalunion));
		}
		manager.saveOntology(ontology);
	}
	public void writeAll(LinkedHashSet<OntologyClass> classes, LinkedHashSet<Individual> individuals,
			ArrayList<String> parameters) throws OWLOntologyCreationException, OWLOntologyStorageException {

		//ontology = this.manager.loadOntologyFromOntologyDocument(this.documentIRI);
		OWLDataFactory factory = this.manager.getOWLDataFactory();
		SimpleIRIMapper mapper = new SimpleIRIMapper(this.ontologyIRI, this.documentIRI);
		this.manager.addIRIMapper(mapper);
		PrefixManager pm = new DefaultPrefixManager(this.ontologyIRI.toString()); // +
		// '#'
		for (String colName : parameters) {
			OWLClass parameter = factory.getOWLClass(IRI.create(ontologyIRI + "#" + colName));

			for (OntologyClass EUClass : classes) {
				OWLClass cls = factory.getOWLClass(IRI.create(this.ontologyIRI + "#" + EUClass.getName()));
				OWLClass thing = factory.getOWLThing();
				OWLAxiom classAx = factory.getOWLSubClassOfAxiom(cls, parameter);
				OWLAxiom parameterAx = factory.getOWLSubClassOfAxiom(parameter, thing);
				this.manager.applyChange(new AddAxiom(this.ontology, classAx));
				this.manager.applyChange(new AddAxiom(this.ontology, parameterAx));
			}
		}

		System.out.println("# of individuals: " + individuals.size());

		for (Individual ind : individuals) {
			Integer index = 0;

			OWLNamedIndividual obj = factory.getOWLNamedIndividual("#" + ind.getFID(), pm);

			for (Entry<String, Number> entry : ind.getValues().entrySet()) {
				OWLDataProperty dataProp = factory.getOWLDataProperty("#" + entry.getKey(), pm);

				OWLDatatype doubleDatatype = factory.getOWLDatatype(OWL2Datatype.XSD_DOUBLE.getIRI());

				OWLLiteral literal = factory.getOWLLiteral(entry.getValue().toString(), doubleDatatype);

				OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory.getOWLDataPropertyAssertionAxiom(dataProp,
						obj, literal);
				this.manager.applyChange(new AddAxiom(ontology, dataPropertyAssertion));
				index = index + 1;
			}
			index = 0;
			for (Entry<String, String> entry : ind.getStringValues().entrySet()) {
				OWLDataProperty dataProp = factory.getOWLDataProperty("#" + entry.getKey(), pm);

				OWLDatatype stringDatatype = factory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
				// System.out.println("about to write: " + value);
				String value = entry.getValue();
				if (value == null) {
					value = "";
				}
				OWLLiteral literal = factory.getOWLLiteral(value, stringDatatype);

				OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory.getOWLDataPropertyAssertionAxiom(dataProp,
						obj, literal);
				this.manager.applyChange(new AddAxiom(ontology, dataPropertyAssertion));
				index = index + 1;
			}
		}
	}
	public void createOntologyObject(LinkedHashMap<String, Integer> nameIndex,
    String fileName) throws OWLOntologyStorageException{
	      CSVReader reader = null;
	        String[] nextLine = null;
	        try {
	            reader = new CSVReader(new FileReader(this.classesFile));
	            // skip header
	            nextLine = reader.readNext();
	            while ((nextLine = reader.readNext()) != null) {
	                /* "eunis" */
	                String className = nextLine[2];
	                String description = nextLine[3];
	                String descriptionDE = nextLine[4];
	                List<String> parents = new ArrayList<String>();
	                parents.add("EUNIS");
	                if (className.startsWith("E")){
						createOntoClass(parents, className, description, descriptionDE);
						owlRuleSet owlRules = buildRuleSet(nextLine, nameIndex);
						if (this.owlRulesMap.get(className) == null) {
							ArrayList<owlRuleSet> newRules = new ArrayList<owlRuleSet>();
							newRules.add(owlRules);
							this.owlRulesMap.put(className, newRules);
						} else {
							ArrayList<owlRuleSet> existingRules = this.owlRulesMap
									.pop(className);
							existingRules.add(owlRules);
							this.owlRulesMap.put(className, existingRules);
						}
					}
	            }
	        } catch (NullPointerException f) {
	            f.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        /* done with file */
	        /* write rules */
	        OWLClassExpression firstRuleSet = null;
	        OWLClass owlCls = null;
	        // owlCls.getIRI().getShortForm();
	        OWLObjectUnionOf totalunion = null;
	        Iterator it = this.owlRulesMap.map.entrySet().iterator(); // .itor(); //
	        // entrySet().iterator();
	        Set<OWLClassExpression> unionSet = new HashSet<OWLClassExpression>();
	        OWLDataFactory dataFactory = this.manager.getOWLDataFactory();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry) it.next();
	            String currCls = (String) pair.getKey();
	            owlCls = dataFactory.getOWLClass(IRI.create("#" + currCls));
	            ArrayList<owlRuleSet> currRuleset = (ArrayList<owlRuleSet>) pair
	                    .getValue();
	            for (int i = 0; i < currRuleset.size(); i++) {
	                firstRuleSet = dataFactory.getOWLObjectIntersectionOf(
	                        currRuleset.get(i).getRuleList(currCls));
	                unionSet.add(firstRuleSet);
	            }
	            totalunion = dataFactory.getOWLObjectUnionOf(unionSet);
	            unionSet.clear();

	            manager.addAxiom(ontology, dataFactory
	                    .getOWLEquivalentClassesAxiom(owlCls, totalunion));
	        }
	        manager.saveOntology(ontology, this.documentIRI);
	        try {
	            if (reader != null) {
	                reader.close();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}
	 private owlRuleSet buildRuleSet(String[] nextLine,
	            LinkedHashMap<String, Integer> nameIndex) {
	        OWLDataFactory dataFactory = this.manager.getOWLDataFactory();
	        List<String> parents = new ArrayList<String>(); // "Parameter, "EUNIS";
	        String description = null;
	        String descriptionDE = null;
	        String paramName = "Parameter";
	        String paramValue = null;
	        OWLClassExpression myRestriction = null;
	        OWLClass parameterValue = null;
	        OWLObjectProperty hasParameter = null;
	        OWLDataProperty hasDataProperty = null;
	        OWLLiteral literal = null;
	        OWLDatatype booleanDataType = dataFactory.getBooleanOWLDatatype();
	        OWLDatatypeRestriction newDataRestriction = null;
	        Double myVal = -1.0;
	        String extractedValue = "";
	        parents.add("EUNIS");
	        String className = nextLine[2];
	        description = nextLine[3];
	        descriptionDE = nextLine[4];
	        /* create rules for EUNIS */
	        OWLmap.owlRuleSet owlRules = new OWLmap.owlRuleSet(className, parents,
	                description, descriptionDE);
	        /* loop over parameters and cleanup */
	        for (Map.Entry<String, Integer> headerEntry : nameIndex.entrySet()) {
	            /* paramName is the ObjectProperty */
	            paramName = headerEntry.getKey();
	            paramValue = nextLine[headerEntry.getValue()];
	            if (paramValue.length() == 0 || paramValue == null
	                    || paramValue.contains("?")) {
	                continue;
	            }
	            /* TODO: refactor */
	            paramValue = paramValue.trim();
	            // paramValue = paramValue.replaceAll(" \\", "/");
	            paramValue = paramValue.replaceAll("\\s", "_");
	            if (paramValue.contains("%")) {
	                paramValue = paramValue.replace("%", "");
	            }
	            /* got number hopefully */
	            extractedValue = extractNumber(paramValue);
	            // System.out.println("extractedValue: " + extractedValue);
	            if (!extractedValue.isEmpty()) {
	                hasDataProperty = dataFactory.getOWLDataProperty(
	                        IRI.create("#" + "has_" + paramName));
	                // System.out.println("paramName: " + paramValue);
	                if (paramName.contains("_max") || paramName.contains("_min")) {
	                    /* dataType restriction */
	                    try {
	                        myVal = Double.parseDouble(extractedValue);
	                    } catch (NumberFormatException e) {
	                        e.printStackTrace();
	                    }
	                    if (paramName.contains("max")) {
	                        newDataRestriction = dataFactory
	                                .getOWLDatatypeMaxExclusiveRestriction(myVal);
	                        myRestriction = dataFactory.getOWLDataSomeValuesFrom(
	                                hasDataProperty, newDataRestriction);
	                    } else {
	                        newDataRestriction = dataFactory
	                                .getOWLDatatypeMinExclusiveRestriction(myVal);
	                        myRestriction = dataFactory.getOWLDataSomeValuesFrom(
	                                hasDataProperty, newDataRestriction);
	                    }
	                } else if (paramValue.matches("0")) {
	                    literal = dataFactory.getOWLLiteral("false",
	                            booleanDataType);
	                    OWLDataOneOf boolFalse = dataFactory
	                            .getOWLDataOneOf(literal);
	                    myRestriction = dataFactory.getOWLDataSomeValuesFrom(
	                            hasDataProperty, boolFalse);

	                } else if (paramValue.matches("1")) {
	                    literal = dataFactory.getOWLLiteral("true",
	                            booleanDataType);
	                    OWLDataOneOf boolFalse = dataFactory
	                            .getOWLDataOneOf(literal);
	                    myRestriction = dataFactory.getOWLDataSomeValuesFrom(
	                            hasDataProperty, boolFalse);
	                }
	            } else {
	                /* neither boolean nor starting with a digit */
	                parents.clear();
	                String[] paramList = null;
	                OWLObjectUnionOf totalunion1 = null;
	                Set<OWLClassExpression> unionSet1 = new HashSet<OWLClassExpression>();
	                hasParameter = dataFactory.getOWLObjectProperty(
	                        IRI.create("#" + "has_" + paramName));
	                if (paramValue.contains("/")) {
	                    paramList = paramValue.split("/");
	                    for (String s : paramList) {
	                        parameterValue = dataFactory
	                                .getOWLClass(IRI.create("#" + s));
	                        myRestriction = dataFactory.getOWLObjectSomeValuesFrom(
	                                hasParameter, parameterValue); // parameterValue);
	                        // owlRules.addRule(myRestriction);
	                        unionSet1.add(myRestriction);
	                        parents.add("Parameter");
	                        parents.add(paramName);
	                        // parents.add(paramValue);
	                        description = "A parameter from " + paramName;
	                        descriptionDE = "Ein Parameter von " + paramName;
	                        /* create class */
	                        createOntoClass(parents, s, description, descriptionDE);
	                        parents.clear();
	                    }
	                    totalunion1 = dataFactory.getOWLObjectUnionOf(unionSet1);
	                    owlRules.rules.add(totalunion1);
	                    // owlRules.addAll(unionSet1);
	                    myRestriction = null;
	                } else {
	                    parameterValue = dataFactory
	                            .getOWLClass(IRI.create("#" + paramValue));
	                    /* which restriction */
	                    myRestriction = dataFactory.getOWLObjectSomeValuesFrom(
	                            hasParameter, parameterValue); // parameterValue);
	                    /* before was outside */
	                    parents.add("Parameter");
	                    parents.add(paramName);
	                    parents.add(paramValue);
	                    description = "A parameter from " + paramName;
	                    descriptionDE = "Ein Parameter von " + paramName;
	                    /* create class */
	                    createOntoClass(parents, paramValue, description,
	                            descriptionDE);
	                }
	            }
	            /* create rule */
	            if (myRestriction != null) {
	                owlRules.addRule(myRestriction);
	            }
	        }
	        return owlRules;
	    }
	 public static String extractNumber(final String myStr) {

	        if (myStr == null || myStr.isEmpty())
	            return "";
	        String foundDigit = "";
	        StringBuilder sb = new StringBuilder();
	        boolean found = false;
	        for (char c : myStr.toCharArray()) {
	            if (Character.isDigit(c)) {
	                sb.append(c);
	                found = true;
	            } else if (Character.compare(c, ',') == 0) {
	                sb.append('.');
	            } else if (found) {
	                // If we already found a digit before and this char is not a
	                // digit, stop looping
	                break;
	            }
	        }
	        if (sb != null) {
	            foundDigit = sb.toString();
	        }
	        return foundDigit;
	    }
	
	private OWLClass createOntoClass(List<String> parents, String clazz,
            String description, String descriptionDE) {
		 OWLDataFactory dataFactory = this.manager.getOWLDataFactory();
	        OWLClass topParentCls = null;
	        OWLClass parentCls = null;
	        OWLClass thing = dataFactory.getOWLThing();
	        OWLClass cls = null;
	        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
	        OWLClass ancestor = dataFactory
	                .getOWLClass(IRI.create(ontologyIRI + "#" + parents.remove(0)));
	        /* loop over children */
	        cls = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#" + clazz));
	        if (this.definedOWLClass.contains(cls)) {
	            return cls;
	        }
	        if (parents.isEmpty()) {
	            axioms.add(dataFactory.getOWLSubClassOfAxiom(cls, ancestor));
	        } else if (parents.size() == 1) {
	            parentCls = dataFactory.getOWLClass(
	                    IRI.create(ontologyIRI + "#" + parents.remove(0)));
	            axioms.add(dataFactory.getOWLSubClassOfAxiom(cls, parentCls));
	            axioms.add(dataFactory.getOWLSubClassOfAxiom(parentCls, ancestor));

	        } else {
	            // System.out.println("more than two parents!");
	            topParentCls = dataFactory.getOWLClass(
	                    IRI.create(ontologyIRI + "#" + parents.remove(0)));
	            parentCls = dataFactory.getOWLClass(
	                    IRI.create(ontologyIRI + "#" + parents.remove(0)));
	            axioms.add(
	                    dataFactory.getOWLSubClassOfAxiom(parentCls, topParentCls));
	            axioms.add(
	                    dataFactory.getOWLSubClassOfAxiom(topParentCls, ancestor));
	        }
	        axioms.add(dataFactory.getOWLSubClassOfAxiom(ancestor, thing));
	        manager.addAxioms(ontology, axioms);
	        if (description != null) {
	            OWLAnnotation commentAnno = dataFactory.getOWLAnnotation(
	                    dataFactory.getRDFSComment(),
	                    dataFactory.getOWLLiteral(description, "en"));
	            OWLAxiom ax = dataFactory
	                    .getOWLAnnotationAssertionAxiom(cls.getIRI(), commentAnno);
	            manager.applyChange(new AddAxiom(ontology, ax));
	            OWLAnnotation commentDE = dataFactory.getOWLAnnotation(
	                    dataFactory.getRDFSComment(),
	                    dataFactory.getOWLLiteral(descriptionDE, "de"));
	            OWLAxiom axDE = dataFactory
	                    .getOWLAnnotationAssertionAxiom(cls.getIRI(), commentDE);
	            manager.applyChange(new AddAxiom(ontology, axDE));
	        }
	        this.definedOWLClass.add(cls);
	        return cls;
	}	

	public LinkedHashSet<OntologyClass> createClassesfromDB() throws SQLException { // String
		Statement st = null;
		LinkedHashSet<OntologyClass> eunisClasses = new LinkedHashSet<OntologyClass>();
		try {
			st = this.dbConn.createStatement();
			for (File colName : this.ruleDir.listFiles()) {
				String fileNameNoExt = FilenameUtils.removeExtension(colName.getName());
				String currCol = fileNameNoExt;
				System.out.println("before query " + this.tableName);
				ResultSet rs = st.executeQuery("SELECT DISTINCT( \"" + currCol + "\") FROM " +this.tableName);
				while (rs.next()) {
					String parameter = rs.getString(currCol);
					if (parameter == null) {
						continue;
					}
					OntologyClass eunisObj = new OntologyClass();
					System.out.println(colName);
					// System.out.println(parameter);
					if (parameter.contains(" ")) {
						parameter = parameter.replace(" ", "_");
					}
					if (eunisClasses.contains(eunisObj.getName()) == false) {
						eunisObj.setName(parameter);
						eunisClasses.add(eunisObj);
						if (parameter.startsWith("0")) {
							parameter = "false";
						} else if (parameter.startsWith("1")) {
							parameter = "true";
						}
						System.out.println("Added: " + parameter);
					}
				}
				String entries = "";
				for (OntologyClass c : eunisClasses) {
					entries += c.getName() + " ";
				}
				System.out.println(entries);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException f) {
			f.printStackTrace();
		} finally {
			if (st != null) {
				st.close();
			}
		}
		return eunisClasses;
	}
	 private static LinkedHashMap<String, Integer> getColIndexes(
	            String fileName) {
	        CSVReader reader = null;
	        List<String> headerCols = null;
	        LinkedHashMap<String, Integer> myHash = 
	                new LinkedHashMap<String, Integer>();
	        try {
	            reader = new CSVReader(new FileReader(fileName));
	            headerCols = Arrays.asList(reader.readNext());
	            for (int i = 0; i < headerCols.size(); i++) {
	                String column = headerCols.get(i);
	                if (column.startsWith("EUNIS_") && !column.startsWith("EUNIS_N")
	                        || column.startsWith("NATFLO")
	                        || column.startsWith("EAGLE")) { 
	                    myHash.put(column, i);
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return myHash;
	}
	public void convertDB() throws NumberFormatException, IOException, SQLException {
		LinkedHashSet<Individual> individuals;
		LinkedHashSet<OntologyClass> classes;
		String ontoFolder = null;
		if (RLPUtils.isLinux()) {
			ontoFolder = "/home/niklasmoran/ontologies/";
		} else if (RLPUtils.isWindows()) {
			ontoFolder = "C:/Users/Moran/ontologies/";
		} else {
			System.out.println("Sorry, unsupported OS!");
		}
		try {
			//LinkedHashMap<String, Integer> nameIndex = null;
			 /* open file */
			//File csvClasses = new File(this.classesFile);
			//reader = new CSVReader(new FileReader(csvClasses));
			//nameIndex = getColIndexes(this.classesFile);
			/* create ontology */
			//createOntologyObject(nameIndex, this.classesFile);
			individuals = createIndividualsFromDB("20000"); // tableName);
			//System.out.println("# of classes: " + classes.size() + " # of individuals : " + individuals.size());
			//OntologyWriter ontWrite = new OntologyWriter(); // IRI.create(owlFile.toURI()));
			//File owlFiles = new File(this.ruleDir);
			OWLmap rulesMap = CSVRules();
			
			writeAll(individuals, rulesMap);
			rulesMap = null;
		} catch (NullPointerException mye) {
			throw new NullPointerException(mye.getMessage());
		} catch (OWLOntologyStorageException e2) {
			throw new RuntimeException(e2.getMessage(), e2);
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			System.out.println("leaving convertDB");
			//individuals = null;
		}
		//return this.owlOut; //owlFile;
	}

	public OWLmap CSVRules() 
			throws OWLOntologyCreationException, NumberFormatException,
			IOException, OWLOntologyStorageException {
		CSVReader reader=null;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(this.documentIRI);
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLmap owlRulesMap  = null;
		for (File csvFile: this.ruleDir.listFiles()){
				String fileNameNoExt = FilenameUtils.removeExtension(csvFile.getName());
				System.out.println("loading rule: " + fileNameNoExt);
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
						if (parameter.contains(" ")) {
							parameter = parameter.replace(" ", "_");
						}
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
	}
	//owlRulesMap = null;
    return owlRulesMap;
	}
	
	private LinkedHashSet<Individual> createIndividualsFromDB(String limit) throws SQLException { // String
		/* Read from DB */
		System.out.println("getting individuals from DB!");
		Statement st = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		LinkedHashSet<Individual> individuals = null;
		try {
			st = dbConn.createStatement();
			individuals = new LinkedHashSet<Individual>(getRowCount(this.tableName));
			System.out.println("tableName: " + this.tableName);
			rs = st.executeQuery("SELECT * FROM \"" + this.tableName + "\" LIMIT " + limit);
			rsmd = rs.getMetaData();
			int colCount = rsmd.getColumnCount();
			if (rsmd == null || colCount == 0 || rs == null) {
				System.out.println("ERROR: too few columns!");
			}
			// System.out.println("RS size: ");
			while (rs.next()) {
				HashMap<String, String> stringValues = new HashMap<String, String>(colCount);
				HashMap<String, Number> values = new HashMap<String, Number>(colCount);
				Individual individual = new Individual();
				for (int i = 1; i <= colCount; i++) {
					String colName = rsmd.getColumnName(i);
					if (colName.endsWith("id")) {
						continue;
					} else if (colName.startsWith("natflo") || colName.startsWith("eunis")
							|| colName.startsWith("eagle")) {
						// if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR){
						String myValue = rs.getString(colName);
						if (myValue == null) {
							myValue = "";
						}
						stringValues.put("has_" + colName, myValue);
					} else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
						values.put("has_" + colName, rs.getDouble(colName));
					}
				}
				individual.setFID(rs.getInt("id"));
				individual.setValues(values);
				individual.setValueString(stringValues);
				// add to individuals
				individuals.add(individual);
				// System.out.println(individual.getDataPropertyNames() + " : "
				// + individual.getValues());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (st != null) {
				st.close();
			}
		}
		return individuals;
	}
	public int getRowCount(String tableName) throws SQLException{
		ResultSet rowQuery = null;
		Statement st = null;
		int rowCount = -1;
		try {
			st = dbConn.createStatement();
			rowQuery = st.executeQuery("SELECT COUNT(*) FROM \"" + this.tableName + "\"");
			if (rowQuery.next()){
				rowCount = rowQuery.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (st != null) {
				st.close();
			}
		}
		return rowCount;
	}
	public String classifyOWL() throws SQLException, OWLOntologyCreationException{
		//OWLOntologyManager mgr = this.manager;
		//OWLDataFactory factory = this.manager.getOWLDataFactory();
		//SimpleIRIMapper mapper = new SimpleIRIMapper(this.ontologyIRI, this.documentIRI);
		//manager.addIRIMapper(mapper);
		//PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString());
		OWLOntology onto = this.ontology;
		if (this.manager == null) {
			System.out.println("ERROR!!");
		}
		String resultsTbl = null;
		System.out.println("Before try");
		try {
			System.out.println("reasoner about to load ontology");
			//onto = mgr.loadOntologyFromOntologyDocument(fileName);
			OWLReasoner factplusplus = new FaCTPlusPlusReasonerFactory()
					.createReasoner(onto); 
			System.out.println(factplusplus.getReasonerVersion());
			HashMap<String, ArrayList<String>> classesHash = new HashMap<String, ArrayList<String>>();
			for (String parameter : this.rulesList){
				ArrayList<String> classList = RLPUtils.getDistinctValuesFromTbl(tableName, parameter);
				resultsTbl = "results_" + parameter;
				defaultDict<Integer, List<String>> dict = new defaultDict<Integer, List<String>>(ArrayList.class);
				for (OWLClass c : onto.getClassesInSignature())
				{
					if (classList.isEmpty()){ System.out.println("class list empty!"); break;}
					String currClass = c.getIRI().getFragment();
					if (currClass.contains("/")) {
						currClass = currClass.split("/")[1];
					}
					System.out.println("current class: " + currClass);
					
					if (classList.contains(currClass)) {
						NodeSet<OWLNamedIndividual> instances = factplusplus
								.getInstances(c, false);
						System.out.println("current class: " + currClass + " isEmpty? " + instances.isEmpty());
						for (OWLNamedIndividual i : instances.getFlattened()) {
							dict.get(Integer.parseInt(i.getIRI().getFragment())).add(currClass);
							System.out.println(i.getIRI().getFragment());
						}
						System.out.println("Total: "
								+ instances.getFlattened().size());
					}
					else{
						continue;
					}
				}
				for (ArrayList<String> clazz: classesHash.values())
				{
					System.out.println(clazz.toString());
					System.out.println("Class size: " + clazz.size());
				}
				/* write results to DB */
				//String originalDataTable = "rlp_eunis_all_parameters";
				createTable(dict, resultsTbl, tableName, parameter); 
			}
		} finally {
			System.out.println("ALL DONE!");
		}
		return resultsTbl; 
	}
	public static void createTable(defaultDict<Integer, List<String>> myDict, String tableName, String validationTable) {
		createTable(myDict, tableName, validationTable, "natflo_wetness");
	}

	public static void createTable(defaultDict<Integer, List<String>> myDict, String tableName, String validationTable, String parameter) {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String url = "jdbc:postgresql://localhost:5432/rlp_spatial?user=postgres&password=BobtheBuilder";
	    ResultSet validClass = null;	
		try {
			con = DriverManager.getConnection(url);
			con.setAutoCommit(false);
			st = con.createStatement();
			//String validationTable = tableName + "_results";
			System.out.println("going to create tableName: " + tableName + " from validation table: "+ validationTable);
			st.execute("drop TABLE if exists " + tableName + ";");
			String createSql = "CREATE TABLE " +  tableName + "( id integer, \"" + parameter + "\" VARCHAR(25), classified VARCHAR(25), PRIMARY KEY(id));";
			System.out.println(createSql);
			st.executeUpdate(createSql);
			validClass = st.executeQuery("select id, \"" + parameter + "\" from " + validationTable);
			HashMap<Integer, String> validClasses = new HashMap <Integer, String>();
			while (validClass.next()){
				validClasses.put(validClass.getInt("id"), validClass.getString(parameter));
			}
			 for (Entry<Integer, List<String>> ee : myDict.entrySet()) {
				Integer key = ee.getKey();
				List<String> values = ee.getValue();
				String new_value = Joiner.on("_").skipNulls().join(values);
				String query = "insert into " + tableName + "(id, \"" + parameter + "\", classified) values(" + key +
						",'" + validClasses.get(key) +"','" + new_value + "');";
				
				//System.out.println(query);
				st.addBatch(query);
			}
			st.executeBatch();
			con.commit();

			// System.out.println("Commited: "+ counts.lenth +
			// " successfully!");

		} catch (SQLException ex) {
			Logger lgr = Logger.getLogger(testReasoner.class.getName());
			lgr.log(Level.SEVERE, ex.getMessage(), ex);
		} finally {
			try {
				if (validClass != null) validClass.close();
				if (rs != null) rs.close();
				if (st != null) st.close();
				if (con != null) con.close();
			} catch (SQLException ex) {
				Logger lgr = Logger.getLogger(testReasoner.class.getName());
				lgr.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
	}

}