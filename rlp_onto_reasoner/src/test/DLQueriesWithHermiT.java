package test;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
public class DLQueriesWithHermiT {

    public static void main(String[] args) throws Exception {
        // Load an example ontology.
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager
                .loadOntologyFromOntologyDocument(new StringDocumentSource(koala));
        // We need a reasoner to do our query answering


        // These two lines are the only relevant difference between this code and the original example
        // This example uses HermiT: http://hermit-reasoner.com/
        OWLReasoner reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);



        ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
        // Create the DLQueryPrinter helper class. This will manage the
        // parsing of input and printing of results
        DLQueryPrinter dlQueryPrinter = new DLQueryPrinter(new DLQueryEngine(reasoner,
                shortFormProvider), shortFormProvider);
        // Enter the query loop. A user is expected to enter class
        // expression on the command line.
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        while (true) {
            System.out
                    .println("Type a class expression in Manchester Syntax and press Enter (or press x to exit):");
            String classExpression = br.readLine();
            // Check for exit condition
            if (classExpression == null || classExpression.equalsIgnoreCase("x")) {
                break;
            }
            dlQueryPrinter.askQuery(classExpression.trim());
            System.out.println();
            }
        }


    // for convenience, the Koala ontology is stored in this string
    private final static String koala = "<?xml version=\\"1.0\\"?>\\n"
            + "<rdf:RDF xmlns:rdf=\\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\\" xmlns:rdfs=\\"http://www.w3.org/2000/01/rdf-schema#\\" xmlns:owl=\\"http://www.w3.org/2002/07/owl#\\" xmlns=\\"http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#\\" xml:base=\\"http://protege.stanford.edu/plugins/owl/owl-library/koala.owl\\">\\n"
            + "  <owl:Ontology rdf:about=\\"\\"/>\\n"
            + "  <owl:Class rdf:ID=\\"Female\\"><owl:equivalentClass><owl:Restriction><owl:onProperty><owl:FunctionalProperty rdf:about=\\"#hasGender\\"/></owl:onProperty><owl:hasValue><Gender rdf:ID=\\"female\\"/></owl:hasValue></owl:Restriction></owl:equivalentClass></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"Marsupials\\"><owl:disjointWith><owl:Class rdf:about=\\"#Person\\"/></owl:disjointWith><rdfs:subClassOf><owl:Class rdf:about=\\"#Animal\\"/></rdfs:subClassOf></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"Student\\"><owl:equivalentClass><owl:Class><owl:intersectionOf rdf:parseType=\\"Collection\\"><owl:Class rdf:about=\\"#Person\\"/><owl:Restriction><owl:onProperty><owl:FunctionalProperty rdf:about=\\"#isHardWorking\\"/></owl:onProperty><owl:hasValue rdf:datatype=\\"http://www.w3.org/2001/XMLSchema#boolean\\">true</owl:hasValue></owl:Restriction><owl:Restriction><owl:someValuesFrom><owl:Class rdf:about=\\"#University\\"/></owl:someValuesFrom><owl:onProperty><owl:ObjectProperty rdf:about=\\"#hasHabitat\\"/></owl:onProperty></owl:Restriction></owl:intersectionOf></owl:Class></owl:equivalentClass></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"KoalaWithPhD\\"><owl:versionInfo>1.2</owl:versionInfo><owl:equivalentClass><owl:Class><owl:intersectionOf rdf:parseType=\\"Collection\\"><owl:Restriction><owl:hasValue><Degree rdf:ID=\\"PhD\\"/></owl:hasValue><owl:onProperty><owl:ObjectProperty rdf:about=\\"#hasDegree\\"/></owl:onProperty></owl:Restriction><owl:Class rdf:about=\\"#Koala\\"/></owl:intersectionOf></owl:Class></owl:equivalentClass></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"University\\"><rdfs:subClassOf><owl:Class rdf:ID=\\"Habitat\\"/></rdfs:subClassOf></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"Koala\\"><rdfs:subClassOf><owl:Restriction><owl:hasValue rdf:datatype=\\"http://www.w3.org/2001/XMLSchema#boolean\\">false</owl:hasValue><owl:onProperty><owl:FunctionalProperty rdf:about=\\"#isHardWorking\\"/></owl:onProperty></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf><owl:Restriction><owl:someValuesFrom><owl:Class rdf:about=\\"#DryEucalyptForest\\"/></owl:someValuesFrom><owl:onProperty><owl:ObjectProperty rdf:about=\\"#hasHabitat\\"/></owl:onProperty></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf rdf:resource=\\"#Marsupials\\"/></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"Animal\\"><rdfs:seeAlso>Male</rdfs:seeAlso><rdfs:subClassOf><owl:Restriction><owl:onProperty><owl:ObjectProperty rdf:about=\\"#hasHabitat\\"/></owl:onProperty><owl:minCardinality rdf:datatype=\\"http://www.w3.org/2001/XMLSchema#int\\">1</owl:minCardinality></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf><owl:Restriction><owl:cardinality rdf:datatype=\\"http://www.w3.org/2001/XMLSchema#int\\">1</owl:cardinality><owl:onProperty><owl:FunctionalProperty rdf:about=\\"#hasGender\\"/></owl:onProperty></owl:Restriction></rdfs:subClassOf><owl:versionInfo>1.1</owl:versionInfo></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"Forest\\"><rdfs:subClassOf rdf:resource=\\"#Habitat\\"/></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"Rainforest\\"><rdfs:subClassOf rdf:resource=\\"#Forest\\"/></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"GraduateStudent\\"><rdfs:subClassOf><owl:Restriction><owl:onProperty><owl:ObjectProperty rdf:about=\\"#hasDegree\\"/></owl:onProperty><owl:someValuesFrom><owl:Class><owl:oneOf rdf:parseType=\\"Collection\\"><Degree rdf:ID=\\"BA\\"/><Degree rdf:ID=\\"BS\\"/></owl:oneOf></owl:Class></owl:someValuesFrom></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf rdf:resource=\\"#Student\\"/></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"Parent\\"><owl:equivalentClass><owl:Class><owl:intersectionOf rdf:parseType=\\"Collection\\"><owl:Class rdf:about=\\"#Animal\\"/><owl:Restriction><owl:onProperty><owl:ObjectProperty rdf:about=\\"#hasChildren\\"/></owl:onProperty><owl:minCardinality rdf:datatype=\\"http://www.w3.org/2001/XMLSchema#int\\">1</owl:minCardinality></owl:Restriction></owl:intersectionOf></owl:Class></owl:equivalentClass><rdfs:subClassOf rdf:resource=\\"#Animal\\"/></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"DryEucalyptForest\\"><rdfs:subClassOf rdf:resource=\\"#Forest\\"/></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"Quokka\\"><rdfs:subClassOf><owl:Restriction><owl:hasValue rdf:datatype=\\"http://www.w3.org/2001/XMLSchema#boolean\\">true</owl:hasValue><owl:onProperty><owl:FunctionalProperty rdf:about=\\"#isHardWorking\\"/></owl:onProperty></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf rdf:resource=\\"#Marsupials\\"/></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"TasmanianDevil\\"><rdfs:subClassOf rdf:resource=\\"#Marsupials\\"/></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"MaleStudentWith3Daughters\\"><owl:equivalentClass><owl:Class><owl:intersectionOf rdf:parseType=\\"Collection\\"><owl:Class rdf:about=\\"#Student\\"/><owl:Restriction><owl:onProperty><owl:FunctionalProperty rdf:about=\\"#hasGender\\"/></owl:onProperty><owl:hasValue><Gender rdf:ID=\\"male\\"/></owl:hasValue></owl:Restriction><owl:Restriction><owl:onProperty><owl:ObjectProperty rdf:about=\\"#hasChildren\\"/></owl:onProperty><owl:cardinality rdf:datatype=\\"http://www.w3.org/2001/XMLSchema#int\\">3</owl:cardinality></owl:Restriction><owl:Restriction><owl:allValuesFrom rdf:resource=\\"#Female\\"/><owl:onProperty><owl:ObjectProperty rdf:about=\\"#hasChildren\\"/></owl:onProperty></owl:Restriction></owl:intersectionOf></owl:Class></owl:equivalentClass></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"Degree\\"/>\\n"
            + "  <owl:Class rdf:ID=\\"Male\\"><owl:equivalentClass><owl:Restriction><owl:hasValue rdf:resource=\\"#male\\"/><owl:onProperty><owl:FunctionalProperty rdf:about=\\"#hasGender\\"/></owl:onProperty></owl:Restriction></owl:equivalentClass></owl:Class>\\n"
            + "  <owl:Class rdf:ID=\\"Gender\\"/>\\n"
            + "  <owl:Class rdf:ID=\\"Person\\"><rdfs:subClassOf rdf:resource=\\"#Animal\\"/><owl:disjointWith rdf:resource=\\"#Marsupials\\"/></owl:Class>\\n"
            + "  <owl:ObjectProperty rdf:ID=\\"hasHabitat\\"><rdfs:range rdf:resource=\\"#Habitat\\"/><rdfs:domain rdf:resource=\\"#Animal\\"/></owl:ObjectProperty>\\n"
            + "  <owl:ObjectProperty rdf:ID=\\"hasDegree\\"><rdfs:domain rdf:resource=\\"#Person\\"/><rdfs:range rdf:resource=\\"#Degree\\"/></owl:ObjectProperty>\\n"
            + "  <owl:ObjectProperty rdf:ID=\\"hasChildren\\"><rdfs:range rdf:resource=\\"#Animal\\"/><rdfs:domain rdf:resource=\\"#Animal\\"/></owl:ObjectProperty>\\n"
            + "  <owl:FunctionalProperty rdf:ID=\\"hasGender\\"><rdfs:range rdf:resource=\\"#Gender\\"/><rdf:type rdf:resource=\\"http://www.w3.org/2002/07/owl#ObjectProperty\\"/><rdfs:domain rdf:resource=\\"#Animal\\"/></owl:FunctionalProperty>\\n"
            + "  <owl:FunctionalProperty rdf:ID=\\"isHardWorking\\"><rdfs:range rdf:resource=\\"http://www.w3.org/2001/XMLSchema#boolean\\"/><rdfs:domain rdf:resource=\\"#Person\\"/><rdf:type rdf:resource=\\"http://www.w3.org/2002/07/owl#DatatypeProperty\\"/></owl:FunctionalProperty>\\n"
            + "  <Degree rdf:ID=\\"MA\\"/>\\n" + "</rdf:RDF>";

    }


class DLQueryParser {
    private final OWLOntology rootOntology;
    private final BidirectionalShortFormProvider bidiShortFormProvider;

    public DLQueryParser(OWLOntology rootOntology, ShortFormProvider shortFormProvider) {
        this.rootOntology = rootOntology;
        OWLOntologyManager manager = rootOntology.getOWLOntologyManager();
        Set<OWLOntology> importsClosure = rootOntology.getImportsClosure();
        // Create a bidirectional short form provider to do the actual mapping.
        // It will generate names using the input
        // short form provider.
        bidiShortFormProvider = new BidirectionalShortFormProviderAdapter(manager,
                importsClosure, shortFormProvider);
    }

    public OWLClassExpression parseClassExpression(String classExpressionString) {
        OWLDataFactory dataFactory = rootOntology.getOWLOntologyManager()
                .getOWLDataFactory();
        ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(
                dataFactory, classExpressionString);
        parser.setDefaultOntology(rootOntology);
        OWLEntityChecker entityChecker = new ShortFormEntityChecker(bidiShortFormProvider);
        parser.setOWLEntityChecker(entityChecker);
        return parser.parseClassExpression();
        }
    }

class DLQueryPrinter {
    private final DLQueryEngine dlQueryEngine;
    private final ShortFormProvider shortFormProvider;

    public DLQueryPrinter(DLQueryEngine engine, ShortFormProvider shortFormProvider) {
        this.shortFormProvider = shortFormProvider;
        dlQueryEngine = engine;
        }

    public void askQuery(String classExpression) {
        if (classExpression.length() == 0) {
            System.out.println("No class expression specified");
        } else {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("\\nQUERY:   ").append(classExpression).append("\\n\\n");
                Set<OWLClass> superClasses = dlQueryEngine.getSuperClasses(
                        classExpression, false);
                printEntities("SuperClasses", superClasses, sb);
                Set<OWLClass> equivalentClasses = dlQueryEngine
                        .getEquivalentClasses(classExpression);
                printEntities("EquivalentClasses", equivalentClasses, sb);
                Set<OWLClass> subClasses = dlQueryEngine.getSubClasses(classExpression,
                        true);
                printEntities("SubClasses", subClasses, sb);
                Set<OWLNamedIndividual> individuals = dlQueryEngine.getInstances(
                        classExpression, true);
                printEntities("Instances", individuals, sb);
                System.out.println(sb.toString());
            } catch (ParserException e) {
                System.out.println(e.getMessage());
            }
            }
        }

    private void printEntities(String name, Set<? extends OWLEntity> entities,
            StringBuilder sb) {
        sb.append(name);
        int length = 50 - name.length();
        for (int i = 0; i < length; i++) {
            sb.append(".");
        }
        sb.append("\\n\\n");
        if (!entities.isEmpty()) {
            for (OWLEntity entity : entities) {
                sb.append("\\t").append(shortFormProvider.getShortForm(entity))
                        .append("\\n");
            }
        } else {
            sb.append("\\t[NONE]\\n");
            }
        sb.append("\\n");
        }
    }