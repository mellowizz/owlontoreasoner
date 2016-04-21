package owlAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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

import javax.swing.filechooser.FileNameExtensionFilter;

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
//import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
//import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
//import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SetOntologyID;
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
    private SetOntologyID setOntologyID;
    private HashSet<String> parameterClasses = new HashSet<String>();

    public OntologyCreator(String url, String tableName, File ruleDir,
            int numRules, String algorithm, ArrayList<String> rulesList,
            File owlOut, String csvClasses) throws SQLException {
        this.dbConn = DriverManager.getConnection(url);
        this.tableName = tableName;
        this.ruleDir = ruleDir;
        this.numRules = numRules;
        this.algorithm = algorithm;
        this.rulesList = rulesList;
        this.owlOut = owlOut;
        this.classesFile = csvClasses;
    }

    public void loadOntology(String ontologyIRIasString, String version,
            File owlFile) throws OWLOntologyCreationException,
    OWLOntologyStorageException {
        try {
            this.manager = OWLManager.createOWLOntologyManager();
            this.ontologyIRI = IRI.create(ontologyIRIasString);

            this.documentIRI = IRI.create(owlFile.toURI());

            this.ontology = manager.loadOntologyFromOntologyDocument(owlFile);

            this.versionIRI = IRI.create(ontologyIRI + "/version1");

            // this.newOntologyID = new OWLOntologyID(ontologyIRI, versionIRI);

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

    public void createOntology(String ontologyIRIasString, String version,
            File owlFile) throws OWLOntologyCreationException,
    OWLOntologyStorageException {

        try {
            this.manager = OWLManager.createOWLOntologyManager();
            // PriorityCollection<OWLOntologyIRIMapper> iriMappers =
            // manager.getIRIMappers();
            this.ontologyIRI = IRI.create(ontologyIRIasString);

            this.documentIRI = IRI.create(owlFile.toURI());

            this.ontology = manager.createOntology(ontologyIRI);

            this.versionIRI = IRI.create(ontologyIRI + "/version1");

            this.newOntologyID = new OWLOntologyID(ontologyIRI, versionIRI);

            this.setOntologyID = new SetOntologyID(ontology, newOntologyID);
            manager.applyChange(setOntologyID);

            save(ontologyIRI, ontology, owlFile);
            System.out.println("Ontology created: " + ontology);
        } catch (OWLOntologyCreationException e) {
            System.out.println("Could not load ontology: " + e.getMessage());
        } catch(NullPointerException e){
            System.out.println("NULL POINTER");
            e.printStackTrace();
        }
    }

    public void save(IRI ontologyIRI, OWLOntology ontology, File owlFile)
            throws OWLOntologyCreationException, OWLOntologyStorageException {

        OWLXMLDocumentFormat owlxmlFormat = new OWLXMLDocumentFormat();
        /* Save OWL to file */
        this.manager.saveOntology(ontology, owlxmlFormat,
                IRI.create(owlFile.toURI()));
    }

    public void writeRules(OWLmap rules) // , File owlFile)
            throws OWLOntologyCreationException, OWLOntologyStorageException {
        // OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // OWLOntology ontology =
        // ontology =
        // this.manager.loadOntologyFromOntologyDocument(this.documentIRI);
        OWLDataFactory factory = this.manager.getOWLDataFactory();
        SimpleIRIMapper mapper = new SimpleIRIMapper(this.ontologyIRI,
                this.documentIRI);
        manager.addIRIMapper(mapper);
        // PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString());

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
            ArrayList<owlRuleSet> currRuleset = (ArrayList<owlRuleSet>) pair
                    .getValue();
            for (int i = 0; i < currRuleset.size(); i++) {
                firstRuleSet = factory.getOWLObjectIntersectionOf(
                        currRuleset.get(i).getRuleList(currCls));
                unionSet.add(firstRuleSet);
            }
            totalunion = factory.getOWLObjectUnionOf(unionSet);
            unionSet.clear();
            manager.addAxiom(ontology,
                    factory.getOWLEquivalentClassesAxiom(owlCls, totalunion));
        }
        manager.saveOntology(ontology);
    }

    public void writeAll(LinkedHashSet<OntologyClass> classes,
            LinkedHashSet<Individual> individuals) //, OWLmap rules)
                    throws OWLOntologyCreationException,
                    OWLOntologyStorageException, SQLException {

        OWLDataFactory factory = this.manager.getOWLDataFactory();
        SimpleIRIMapper mapper = new SimpleIRIMapper(this.ontologyIRI,
         this.documentIRI);
        manager.addIRIMapper(mapper);
        PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString());
        System.out.println("# of individuals: " + individuals.size());
        for (Individual ind : individuals) {
            Integer index = 0;

            OWLNamedIndividual obj = factory
                    .getOWLNamedIndividual("#" + ind.getFID(), pm);

            for (Entry<String, Number> entry : ind.getValues().entrySet()) {
                OWLDataProperty dataProp = factory
                        .getOWLDataProperty("#" + entry.getKey(), pm);

                OWLDatatype doubleDatatype = factory
                        .getOWLDatatype(OWL2Datatype.XSD_DOUBLE.getIRI());

                OWLLiteral literal = factory.getOWLLiteral(
                        entry.getValue().toString(), doubleDatatype);

                OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory
                        .getOWLDataPropertyAssertionAxiom(dataProp, obj,
                                literal);
                manager.applyChange(
                        new AddAxiom(ontology, dataPropertyAssertion));
                index = index + 1;
            }
        }
        /* write rules */
        OWLClassExpression firstRuleSet = null;
        OWLClass owlCls = null;
        OWLObjectUnionOf totalunion = null;
        Iterator it = this.owlRulesMap.map.entrySet().iterator();
        Set<OWLClassExpression> unionSet = new HashSet<OWLClassExpression>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String currCls = (String) pair.getKey();
            /*this.ontologyIRI */
            owlCls = factory.getOWLClass(IRI.create("#" + currCls));
            System.out.println("CurrCls for rule: " + currCls);
            ArrayList<owlRuleSet> currRuleset = (ArrayList<owlRuleSet>) pair
                    .getValue();
            for (int i = 0; i < currRuleset.size(); i++) {
                firstRuleSet = factory.getOWLObjectIntersectionOf(
                        currRuleset.get(i).getRuleList(currCls));
                unionSet.add(firstRuleSet);
            }
            totalunion = factory.getOWLObjectUnionOf(unionSet);
            unionSet.clear();
            manager.addAxiom(ontology,
                    factory.getOWLEquivalentClassesAxiom(owlCls, totalunion));
        }
        manager.saveOntology(ontology);
    }
    //LinkedHashSet<OntologyClass> classes, OWLmap rulesMap)
    public void writeAll(
            LinkedHashSet<Individual> individuals) 
                    throws OWLOntologyCreationException,
                    OWLOntologyStorageException, SQLException {

        OWLDataFactory factory = this.manager.getOWLDataFactory();
        SimpleIRIMapper mapper = new SimpleIRIMapper(this.ontologyIRI,
         this.documentIRI);
        manager.addIRIMapper(mapper);
        PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString());
        System.out.println("# of individuals: " + individuals.size());
        //for (String colName : this.rulesList) {
        //    System.out.println("colName: " + colName);
        //}
        for (Individual ind : individuals) {
            Integer index = 0;

            OWLNamedIndividual obj = factory
                    .getOWLNamedIndividual("#" + ind.getFID(), pm);

            for (Entry<String, Number> entry : ind.getValues().entrySet()) {
                OWLDataProperty dataProp = factory
                        .getOWLDataProperty("#" + entry.getKey(), pm);

                OWLDatatype doubleDatatype = factory
                        .getOWLDatatype(OWL2Datatype.XSD_DOUBLE.getIRI());

                OWLLiteral literal = factory.getOWLLiteral(
                        entry.getValue().toString(), doubleDatatype);

                OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory
                        .getOWLDataPropertyAssertionAxiom(dataProp, obj,
                                literal);
                manager.applyChange(
                        new AddAxiom(ontology, dataPropertyAssertion));
                index = index + 1;
            }
            index = 0;
        } 
        /* write rules */
        OWLClassExpression firstRuleSet = null;
        OWLClass owlCls = null;
        OWLObjectUnionOf totalunion = null;
        Iterator it = this.owlRulesMap.map.entrySet().iterator();
        Set<OWLClassExpression> unionSet = new HashSet<OWLClassExpression>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String currCls = (String) pair.getKey();
            /*this.ontologyIRI */
            owlCls = factory.getOWLClass(IRI.create("#" + currCls));
            System.out.println("CurrCls for rule: " + currCls);
            ArrayList<owlRuleSet> currRuleset = (ArrayList<owlRuleSet>) pair
                    .getValue();
            for (int i = 0; i < currRuleset.size(); i++) {
                firstRuleSet = factory.getOWLObjectIntersectionOf(
                        currRuleset.get(i).getRuleList(currCls));
                unionSet.add(firstRuleSet);
            }
            totalunion = factory.getOWLObjectUnionOf(unionSet);
            unionSet.clear();
            manager.addAxiom(ontology,
                    factory.getOWLEquivalentClassesAxiom(owlCls, totalunion));
        }
        manager.saveOntology(ontology);
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
        //ontologyIRI
        OWLClass ancestor = dataFactory
                .getOWLClass(IRI.create("#" + parents.remove(0)));
        /* loop over children */
        cls = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#" + clazz));
        if (this.definedOWLClass.contains(cls)) {
            return cls;
        }
        if (parents.isEmpty()) {
            axioms.add(dataFactory.getOWLSubClassOfAxiom(cls, ancestor));
        } else if (parents.size() == 1) {
            parentCls = dataFactory.getOWLClass(
                    IRI.create("#" + parents.remove(0)));
            axioms.add(dataFactory.getOWLSubClassOfAxiom(cls, parentCls));
            axioms.add(dataFactory.getOWLSubClassOfAxiom(parentCls, ancestor));

        } else {
            // System.out.println("more than two parents!");
            topParentCls = dataFactory.getOWLClass(
                    IRI.create("#" + parents.remove(0)));
            parentCls = dataFactory.getOWLClass(
                    IRI.create("#" + parents.remove(0)));
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

    public LinkedHashSet<OntologyClass> createClassesfromDB()
            throws SQLException { // String
        Statement st = null;
        LinkedHashSet<OntologyClass> eunisClasses = new LinkedHashSet<OntologyClass>();
        try {
            st = this.dbConn.createStatement();
            for (File colName : this.ruleDir.listFiles()) {
                String fileNameNoExt = FilenameUtils
                        .removeExtension(colName.getName());
                String currCol = fileNameNoExt;
                System.out.println("before query " + this.tableName);
                ResultSet rs = st.executeQuery("SELECT DISTINCT( \"" + currCol
                        + "\") FROM " + this.tableName);
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
                    if (parameter.startsWith("0")) {
                        parameter = fileNameNoExt + "_false";
                    } else if (parameter.startsWith("1")) {
                        parameter = fileNameNoExt + "_true";
                    }
                    if (eunisClasses.contains(eunisObj.getName()) == false) {
                        eunisObj.setName(parameter);
                        eunisClasses.add(eunisObj);
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
        LinkedHashMap<String, Integer> myHash = new LinkedHashMap<String, Integer>();
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

    public void convertDB()
            throws NumberFormatException, IOException, SQLException {
        LinkedHashSet<Individual> individuals;
        //LinkedHashSet<OntologyClass> classes;
        String ontoFolder = null;
        if (RLPUtils.isLinux()) {
            ontoFolder = "/home/niklasmoran/ontologies/";
        } else if (RLPUtils.isWindows()) {
            ontoFolder = "C:/Users/Moran/ontologies/";
        } else {
            System.out.println("Sorry, unsupported OS!");
        }
        CSVReader reader = null;
        
        try {
            //this.OWLmap rulesMap = CSVRules();
            //CSVRulesToSQL();
            individuals = createIndividualsFromDB("-1"); // tableName);
            this.owlRulesMap = CSVRules();
            LinkedHashSet<OntologyClass> eunis = createClassesfromDB();
            //addEUNISClasses();
            writeAll(eunis, individuals);// rulesMap);
            this.owlRulesMap = null;
        } catch (NullPointerException mye) {
            throw new NullPointerException(mye.getMessage());
        } catch (OWLOntologyStorageException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (reader !=null){
                reader.close();
                reader = null;
            }
            System.out.println("leaving convertDB");
            individuals = null;
        }
        // return this.owlOut; //owlFile;
    }


    public OWLmap CSVRulesConverter() throws OWLOntologyCreationException,
    NumberFormatException, IOException, OWLOntologyStorageException {
        CSVReader reader = null;
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager
                .loadOntologyFromOntologyDocument(this.documentIRI);
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLmap owlRulesMap = new OWLmap();
        for (final File csvFile : this.ruleDir.listFiles()) {
            final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
                    "csv", "CSV");
            if (extensionFilter.accept(csvFile)) {
                reader = new CSVReader(new FileReader(csvFile));
                String fileNameNoExt = FilenameUtils
                        .removeExtension(csvFile.getName());
                String[] classNames = fileNameNoExt.split("-");
                if (classNames[0].contains(" ")) {
                    classNames[0] = classNames[0].replace(" ", "_");
                }
                if (classNames[1].contains(" ")) {
                    classNames[1] = classNames[1].replace(" ", "_");
                }
                System.out.println("class: " + classNames[0] + " target: "
                        + classNames[1]);
                String[] nextLine;
                int lineNum = 1;
                OWLDatatypeRestriction newRestriction = null;
                OWLClassExpression myRestriction = null;
                //OWLDataProperty hasParameter = null;
                OWLDataProperty hasParameter = null;
                // OWLDatatypeRestriction newRestrictionOpp = null;
                Set<OWLClassExpression> ruleSet = new HashSet<OWLClassExpression>();
                Set<OWLClassExpression> objSet = new HashSet<OWLClassExpression>();
                // Set<OWLClassExpression> ruleSetOpp = new
                // HashSet<OWLClassExpression>();
                int ruleCounter = 0;
                /*
                 * could make MAX_EXCLUSIVE set by if/else than emit right code
                 */
                while ((nextLine = reader.readNext()) != null
                        && lineNum <= this.numRules) {
                    String parameter = nextLine[0]; /* why has_? */
                    /* if in distinct values */
                    try {
                        ruleCounter++;
                        /* try to create a rule out of line */
                        parameter = "has_" + parameter;
                        String direction = nextLine[2];
                        String threshold = nextLine[3];
                        System.out.println(parameter + " direction: "
                                + direction + " threshold: " + threshold);
                        hasParameter = factory.getOWLDataProperty(
                                IRI.create("#" + parameter));
                        switch (direction) {
                        case ">=":
                            newRestriction = factory
                            .getOWLDatatypeMinInclusiveRestriction(
                                    Double.parseDouble(threshold));
                            break;
                        case "<=":
                            newRestriction = factory
                            .getOWLDatatypeMaxInclusiveRestriction(
                                    Double.parseDouble(threshold));

                            break;

                        case "<":
                            newRestriction = factory
                            .getOWLDatatypeMaxExclusiveRestriction(
                                    Double.parseDouble(threshold));

                            break;
                        case ">":
                            newRestriction = factory
                            .getOWLDatatypeMinExclusiveRestriction(
                                    Double.parseDouble(threshold));

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
                        ruleSet.add(newWetnessRestriction);
                        //myRestriction = factory.getOWLEquivalentClassesAxiom()
                        //OWLClassExpression newObjRestriction = factory
                        //.getOWLObjectSomeValuesFrom(hasParameter, )
                       
                        ruleSet.add(newWetnessRestriction);
                        //objSet.add(newObjRestriction);
                        // ruleSetOpp.add(newWetnessRestrictionOpp);
                        lineNum++;
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                /* done with CSV, add rules */
                if (owlRulesMap.get(classNames[0]) == null) {
                    OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet(
                            classNames[0]);
                    // OWLmap.owlRuleSet rule_target = new OWLmap.owlRuleSet
                    // (classNames[1], ruleCounter);
                    rule.addAll(ruleSet);
                    // rule_target.addAll(ruleSetOpp);
                    // ruleSetOpp.clear();
                    ArrayList<owlRuleSet> newRules = new ArrayList<owlRuleSet>();
                    newRules.add(rule);
                    owlRulesMap.put(classNames[0], newRules);
                    // continue;
                } else {
                    ArrayList<owlRuleSet> existingRules = owlRulesMap
                            .pop(classNames[0]);
                    owlRuleSet the_rules = existingRules.remove(0);
                    ruleSet.addAll(the_rules.getRuleList(classNames[0]));
                    OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet(
                            classNames[0]);
                    ArrayList<owlRuleSet> newRules = new ArrayList<owlRuleSet>();
                    rule.addAll(ruleSet);
                    newRules.add(rule);
                    owlRulesMap.put(classNames[0], newRules);
                }
                ruleSet.clear();
            }
        }
        manager.saveOntology(ontology);
        reader.close();
        // System.out.println("There are: " + classesExpressions.size()
        // + " keys: " + classesExpressions.keySet().size());
        return owlRulesMap; // classesExpressions;
    }

    public void CSVRulesToSQL() throws IOException{
        /* loop over files */
        ArrayList classList = null;
        CSVReader reader = null;
        for (File csvFile : this.ruleDir.listFiles()) {
            Map<String, ArrayList<String>> sqlMap = new java.util.HashMap<String, ArrayList<String>>();
            String fileNameNoExt = FilenameUtils
                    .removeExtension(csvFile.getName());
            System.out.println("loading rule: " + fileNameNoExt);
            try {
                classList = RLPUtils.getDistinctValuesFromTbl(this.tableName,
                        fileNameNoExt);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            reader = new CSVReader(new FileReader(csvFile));
            String[] nextLine;
            /* write rules in SQL */
            ArrayList<String> ruleSet = new ArrayList<String>();
            ArrayList<String> rule = new ArrayList<String>();
            while ((nextLine = reader.readNext()) != null) {
                String parameter = nextLine[0]; 
                if (parameter.contains(" ")) {
                    parameter = parameter.replace(" ", "_");
                }
                if (classList.contains(parameter)) {
                    /* add collected rules to class and clear rulesList */
                    try {
                        /* fileNameNoExt is the class Names so has_wetness */ 
                        rule.addAll(ruleSet);
                        ruleSet.clear();
                        if (sqlMap.get(parameter) == null) {
                            //ArrayList<String> myRules = new ArrayList<String>();
                            //myRules.add();
                            sqlMap.put(parameter, rule);
                            continue;
                        } else {
                            /*
                             * already seen this class! --update by or'ing the
                             * rules!
                             */
                            sqlMap.get(parameter).addAll(rule);
                            rule.clear();
                       } 
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                /* gather rules */
                try {
                    String direction = nextLine[1];
                    String threshold = nextLine[2];
                    System.out.println(parameter + " direction: " + direction
                    + " threshold: " + threshold);
                    ruleSet.add(direction + threshold);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("***** ALL DONE ****");
            Iterator it = sqlMap.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry pair = (Map.Entry) it.next();
                String currCls = (String) pair.getKey();
                ArrayList<String> currRuleset = (ArrayList<String>) pair
                        .getValue();
                System.out.println("class: " + currCls + " size: " + currRuleset.size());
                for (int i = 0; i < currRuleset.size(); i++) {
                    System.out.println("curr: " + currRuleset.get(i));
                }
            }
        }
    }

    public OWLmap CSVRules() throws OWLOntologyCreationException,
    NumberFormatException, IOException, OWLOntologyStorageException {
        CSVReader reader = null;
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager
                .loadOntologyFromOntologyDocument(this.documentIRI);
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLmap owlRulesMap = new OWLmap();
        ArrayList classList = null;
        OWLDatatype booleanDataType = factory.getBooleanOWLDatatype();
        OWLClass paramValue = null;
        for (File csvFile : this.ruleDir.listFiles()) {
            String fileNameNoExt = FilenameUtils
                    .removeExtension(csvFile.getName());
            System.out.println("current rule: " + fileNameNoExt);
            if (fileNameNoExt.isEmpty() || fileNameNoExt == null) continue;
            try {
                classList = RLPUtils.getDistinctValuesFromTbl(this.tableName,
                        fileNameNoExt);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            reader = new CSVReader(new FileReader(csvFile));
            String[] nextLine;
            OWLDatatypeRestriction newRestriction = null;
            OWLClassExpression myRestriction = null;
            OWLDataProperty hasParameter = null;
            OWLObjectProperty hasObjProp = null;
            OWLLiteral literal = null;
            
            Set<OWLClassExpression> ruleSet = new HashSet<OWLClassExpression>();
            Set<OWLClassExpression> objSet = new HashSet<OWLClassExpression>();
            while ((nextLine = reader.readNext()) != null) {
                /* has_${parameter} */
                String parameter = nextLine[0]; 
                if (parameter.contains(" ")) {
                    parameter = parameter.replace(" ", "_");
                }
                if (classList.contains(parameter) || nextLine.length == 1) {
                    /* add collected rules to class and clear rulesList */
                    
                    try {
                        if (parameter.matches("0")) {
                            parameter = fileNameNoExt + "_false";
                        } else if (parameter.matches("1")) {
                            parameter = fileNameNoExt + "_true";
                        } else if (parameter.contains("/")){
                            parameter = parameter.replace("/", "_");
                        }
                        
                        paramValue = factory
                            .getOWLClass(IRI.create("#" + fileNameNoExt));
                        OWLClass cls = factory.getOWLClass(
                                IRI.create("#" + parameter));
                        hasObjProp = factory.getOWLObjectProperty(
                                IRI.create("#" + "has_" + fileNameNoExt));
                        OWLClassExpression expression =  factory.getOWLObjectSomeValuesFrom(hasObjProp, cls);
                        OWLClass thing = factory.getOWLThing();
                        OWLAxiom classAx = factory.getOWLSubClassOfAxiom(cls,
                                thing);
                        OWLAxiom parameterAx = factory.getOWLSubClassOfAxiom(paramValue,
                                thing);
                        OWLAxiom objUnionSub = factory.getOWLSubObjectPropertyOfAxiom(hasObjProp, factory.getOWLTopObjectProperty());
                        this.manager.addAxiom(this.ontology, factory.getOWLEquivalentClassesAxiom(factory.getOWLObjectUnionOf(expression)));
                        this.manager.applyChange(new AddAxiom(this.ontology, classAx));
                        this.manager
                        .applyChange(new AddAxiom(this.ontology, parameterAx));
                        this.manager.applyChange(new AddAxiom(this.ontology, objUnionSub));
                        this.parameterClasses.add(parameter);
                        System.out.println("class: " + fileNameNoExt+
                                " value: " + parameter);
                        OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet(
                                parameter); // ,
                        // ruleCounter);
                        rule.addAll(ruleSet);
                        ruleSet.clear();
                        if (owlRulesMap.get(parameter) == null) {
                            ArrayList<owlRuleSet> newRules = new ArrayList<owlRuleSet>();
                            owlRulesMap.put(parameter, newRules);
                            newRules.add(rule);
                            owlRulesMap.put(parameter, newRules);
                            continue;
                        } else {
                            /*
                             * already seen this class! --update by or'ing the
                             * rules!
                             */
                            System.out.println(
                                    "adding : " + parameter + " in rules");
                            /* class*/
                            myRestriction = factory.getOWLObjectUnionOf(cls);
                            objSet.add(myRestriction);
                            owlRulesMap.get(parameter).add(rule);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    /* bug!! */
                    parameter = "has_" + parameter;
                    String direction = nextLine[1];
                    String threshold = nextLine[2];
                    // System.out.println(parameter + " direction: " + direction
                    // + " threshold: " + threshold);
                    hasParameter = factory
                            .getOWLDataProperty(IRI.create("#" + parameter));
                    switch (direction) {
                    case "<=":
                        newRestriction = factory
                        .getOWLDatatypeMaxInclusiveRestriction(
                                Double.parseDouble(threshold));
                        break;

                    case "<":
                        newRestriction = factory
                        .getOWLDatatypeMaxExclusiveRestriction(
                                Double.parseDouble(threshold));
                        break;
                    case ">":
                        newRestriction = factory
                        .getOWLDatatypeMinExclusiveRestriction(
                                Double.parseDouble(threshold));
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
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            OWLObjectUnionOf myUnion = factory.getOWLObjectUnionOf(objSet);
            manager.addAxiom(this.ontology, factory.getOWLEquivalentClassesAxiom(paramValue, myUnion));
            reader.close();
        }
        manager.saveOntology(ontology);
        // owlRulesMap = null;
        return owlRulesMap;
    }

    private LinkedHashSet<Individual> createIndividualsFromDB()
                          throws SQLException {
        return createIndividualsFromDB("-1"); 
    }
    private LinkedHashSet<Individual> createIndividualsFromDB(String limit)
                          throws SQLException {
        /* Read from DB */
        System.out.println("getting individuals from DB!");
        Statement st = null;
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        LinkedHashSet<Individual> individuals = null;
        try {
            st = dbConn.createStatement();
            individuals = new LinkedHashSet<Individual>(
                    getRowCount(this.tableName));
            System.out.println("tableName: " + this.tableName);
            if (limit.equals("-1")){
                rs = st.executeQuery(
                        "SELECT * FROM \"" + this.tableName + "\"");
            } else{
                rs = st.executeQuery(
                        "SELECT * FROM \"" + this.tableName + "\"" +
                        " LIMIT " + limit);
            }
            rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();
            if (rsmd == null || colCount == 0 || rs == null) {
                System.out.println("ERROR: too few columns!");
            }
            // System.out.println("RS size: ");
            while (rs.next()) {
                HashMap<String, String> stringValues = new HashMap<String, String>(
                        colCount);
                HashMap<String, Number> values = new HashMap<String, Number>(
                        colCount);
                Individual individual = new Individual();
                for (int i = 1; i <= colCount; i++) {
                    String colName = rsmd.getColumnName(i);
                    if (colName.endsWith("id")){ 
                        //this.owlRulesMap.get(colName) == null);
                        // skip ids and columns not in parameter
                        continue;
                    } else if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR){
                        String myValue = rs.getString(colName);
                        if (myValue == null) {
                            myValue = "";
                        }
                        stringValues.put("has_" + colName, myValue);
                    } else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE){
                        values.put("has_" + colName, rs.getDouble(colName));
                    }
                }
                individual.setFID(rs.getInt("id"));
                individual.setValues(values);
                individual.setValueString(stringValues);
                // add to individuals
                individuals.add(individual);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (st != null) {
                st.close();
            }
        }
        if (individuals.isEmpty()){
            System.out.println("individuals hash is empty!!");
        }
        return individuals;
    }

    public int getRowCount(String tableName) throws SQLException {
        ResultSet rowQuery = null;
        Statement st = null;
        int rowCount = -1;
        try {
            st = dbConn.createStatement();
            rowQuery = st.executeQuery(
                    "SELECT COUNT(*) FROM \"" + this.tableName + "\"");
            if (rowQuery.next()) {
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

    public String classifyOWL(File outFile)
            throws SQLException, OWLOntologyCreationException {
        OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
        OWLOntology onto = null;
        String resultsTbl = null;
        System.out.println("Before try");
        PrintWriter pw = null;
        String tableName = "test_sburg_grasslands";
        String homeDir = System.getProperty("user.home");
        defaultDict<Integer, List<String>> dict = null;  new defaultDict<Integer, List<String>>(ArrayList.class);
        try {
            //mgr.createOntology(ontologyIRI);
            assert(outFile != null);
            onto = mgr.loadOntologyFromOntologyDocument(IRI.create(outFile));
            assert (onto != null);
            OWLReasoner reasoner = new FaCTPlusPlusReasonerFactory()
                    .createReasoner(onto);
            // System.out.println("Is Consistent: " + reasoner.isConsistent());
            pw = new PrintWriter(new File(homeDir + "/test-rlp/create.sql"));
            System.out.println("reasoner about to load ontology");
            //onto = mgr.loadOntologyFromOntologyDocument(outFile);
            System.out.println(reasoner.getReasonerVersion());
            System.out.println("rulesList: " + this.rulesList);
            for (String parameter : this.rulesList) {
                dict = new defaultDict<Integer, List<String>>(ArrayList.class); 
                System.out.println("current parameter: " + parameter);
                resultsTbl = "results_" + parameter ;
                pw.println("COPY " + resultsTbl + " FROM stdin USING DELIMITERS '|';");
                for (OWLClass c : onto.getClassesInSignature()) {
                    String currClass = c.getIRI().getShortForm();
                    if (currClass.endsWith("false")) {
                        currClass = "0";
                    } else if (currClass.endsWith("true")) {
                        currClass = "1";
                    }
                    if (RLPUtils.getDistinctValuesFromTbl(this.tableName, parameter).contains(currClass)){
                    //if ( this.parameterClasses.contains(currClass)){
                    	NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(c, false);
                        int numIndividuals = 0;
                        if (instances.isEmpty()){
                            System.out.println("class: " + " is empty!!");
                        } else{
                            numIndividuals = instances.getFlattened().size();
                        }
                        System.out.println("currcls: " + currClass + " has: " + 
                                numIndividuals + " individuals");
                        for (OWLNamedIndividual i : instances.getFlattened()){
                            String individual = i.getIRI().getShortForm();
                            int currId = Integer.parseInt(individual.substring(individual.lastIndexOf("#")+1));
                            dict.get(currId).add(currClass);
                            pw.println(currId + "|"+ currClass);
                            // System.out.println(currId + ", " + currClass);
                        }
                    }else {
                        System.out.println("class: " + currClass + " not in: " + tableName);
                        continue;
                    }
                /* write results to DB */
                // String originalDataTable = "rlp_eunis_all_parameters";
                }
                createTable(dict, resultsTbl, tableName, parameter);
                dict = null;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NumberFormatException e){
            e.printStackTrace();
        } finally {
            System.out.println("ALL DONE!");
            if (pw != null){
                pw.close();
            }
        }
        return resultsTbl;
    }

    public static void createTable(defaultDict<Integer, List<String>> myDict,
            String tableName, String validationTable) {
        createTable(myDict, tableName, validationTable, "natflo_wetness");
    }

    public static void createTable(defaultDict<Integer, List<String>> myDict,
            String tableName, String validationTable, String parameter) {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        String url = "jdbc:postgresql://localhost:5432/rlp_saarburg?user=postgres&password=BobtheBuilder";
        ResultSet validClass = null;
        try {
            con = DriverManager.getConnection(url);
            con.setAutoCommit(false);
            st = con.createStatement();
            System.out.println("going to create tableName: " + tableName + "_sburg"
                    + " from validation table: " + validationTable);
            /* reduced */
            st.execute("drop TABLE if exists " + tableName + "_sburg" + ";");
            String createSql = "CREATE TABLE " + tableName +"_sburg" + "( id integer, \""
                    + parameter
                    + "\" VARCHAR(25), classified VARCHAR(25), PRIMARY KEY(id));";
            System.out.println(createSql);
            st.executeUpdate(createSql);
            validClass = st.executeQuery(
                    "select id, \"" + parameter + "\" from " + validationTable);
            HashMap<Integer, String> validClasses = new HashMap<Integer, String>();
            while (validClass.next()) {
                validClasses.put(validClass.getInt("id"),
                        validClass.getString(parameter));
            }
            for (Entry<Integer, List<String>> ee : myDict.entrySet()) {
                Integer key = ee.getKey();
                List<String> values = ee.getValue();
                String new_value = Joiner.on("_").skipNulls().join(values);
                /*reduced */
                String query = "insert into " + tableName + "_sburg" + "(id, \""
                        + parameter + "\", classified) values(" + key + ",'"
                        + validClasses.get(key) + "','" + new_value + "');";

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
                if (validClass != null)
                    validClass.close();
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (con != null)
                    con.close();
            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(testReasoner.class.getName());
                lgr.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
}