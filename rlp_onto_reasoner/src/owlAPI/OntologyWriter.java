package owlAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import dict.defaultDict;
import owlAPI.OWLmap.owlRuleSet;
//import conversion.OntologyClass;
import owlAPI.OntologyClass;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;

public class OntologyWriter {
	/*
	 * 
	 * Writes properties and classes to ontology
	 */

	/*
	 * OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	 * OWLOntology ontology; OWLDataFactory factory; // =
	 * manager.getOWLDataFactory(); public OntologyWriter(IRI documentIRI)
	 * throws OWLOntologyCreationException{
	 * manager.loadOntologyFromOntologyDocument(documentIRI); factory =
	 * manager.getOWLDataFactory(); }
	 */

	public void writeAll(LinkedHashSet<OntologyClass> classes,
			LinkedHashSet<Individual> individuals, OWLmap rules, String colName, IRI documentIRI,
			IRI ontologyIRI) throws OWLOntologyCreationException,
			OWLOntologyStorageException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(documentIRI);
		OWLDataFactory factory = manager.getOWLDataFactory();
		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
		manager.addIRIMapper(mapper);
		PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString()); // +
																				// '#'

		OWLClass parameter = factory.getOWLClass(IRI.create(ontologyIRI
				+ "#" + colName));
		
		for (OntologyClass EUClass : classes) {
			OWLClass cls = factory.getOWLClass(IRI.create(ontologyIRI + "#"
					+ EUClass.getName()));
			OWLClass thing = factory.getOWLThing();
			OWLAxiom classAx = factory.getOWLSubClassOfAxiom(cls, parameter);
			OWLAxiom parameterAx = factory.getOWLSubClassOfAxiom(parameter, thing);
			manager.applyChange(new AddAxiom(ontology, classAx));
			manager.applyChange(new AddAxiom(ontology, parameterAx));
		}
		
		System.out.println("# of individuals: " + individuals.size());

		for (Individual ind : individuals) {
			Integer index = 0;

			OWLNamedIndividual obj = factory.getOWLNamedIndividual(
					"#" + ind.getFID(), pm);

			for (Number value : ind.getValues()) {
				OWLDataProperty dataProp = factory.getOWLDataProperty("#"
						+ ind.getDataPropertyNames().get(index), pm);

				OWLDatatype doubleDatatype = factory
						.getOWLDatatype(OWL2Datatype.XSD_DOUBLE.getIRI());

				OWLLiteral literal = factory.getOWLLiteral(value.toString(),
						doubleDatatype);

				OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory
						.getOWLDataPropertyAssertionAxiom(dataProp, obj,
								literal);
				manager.applyChange(new AddAxiom(ontology, dataPropertyAssertion));
				index = index + 1;
			}
			index = 0;
			for (String value : ind.getStringValues()) {
				OWLDataProperty dataProp = factory.getOWLDataProperty("#"
						+ ind.getDataPropertyNames().get(index), pm);

				OWLDatatype stringDatatype = factory
						.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
				//System.out.println("about to write: " + value);
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

	public void writeClasses(LinkedHashSet<OntologyClass> classes,
			IRI documentIRI, IRI ontologyIRI)
			throws OWLOntologyCreationException, OWLOntologyStorageException {
		/*
		 * 
		 * Writes EUNIS classes to ontology (as a subclass of owl:thing)
		 */

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(documentIRI);
		OWLDataFactory factory = manager.getOWLDataFactory();

		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
		manager.addIRIMapper(mapper);
		OWLClass wetness = factory.getOWLClass(IRI.create(ontologyIRI
				+ "#wetness"));
		// manager.applyChange(new AddAxiom(ontology, wetnessThing));
		for (OntologyClass EUClass : classes) {
			OWLClass cls = factory.getOWLClass(IRI.create(ontologyIRI + "#"
					+ EUClass.getName()));
			OWLClass thing = factory.getOWLThing();
			OWLAxiom classAx = factory.getOWLSubClassOfAxiom(cls, wetness);
			manager.applyChange(new AddAxiom(ontology, classAx));

		}

		manager.saveOntology(ontology);
	}

	public void writeIndividuals(LinkedHashSet<Individual> individuals,
			IRI documentIRI) throws OWLOntologyCreationException,
			OWLOntologyStorageException {
		/*
		 * Writes Individuals (and corresponding DataProperties) to an existing
		 * ontology
		 */
		System.out.println("# of individuals: " + individuals.size());
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(documentIRI);
		OWLDataFactory factory = manager.getOWLDataFactory();
		IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI();
		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);

		manager.addIRIMapper(mapper);

		PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString()); // +
																				// '#'
		// OWLClass eunis = factory.getOWLClass(":")
		// manager.addAxiom(ontology, eunis);

		for (Individual ind : individuals) {
			Integer index = 0;

			OWLNamedIndividual obj = factory.getOWLNamedIndividual(
					"#" + ind.getFID(), pm);

			for (Number value : ind.getValues()) {
				OWLDataProperty dataProp = factory.getOWLDataProperty("#"
						+ ind.getDataPropertyNames().get(index), pm);

				OWLDatatype integerDatatype = factory
						.getOWLDatatype(OWL2Datatype.XSD_DOUBLE.getIRI());

				OWLLiteral literal = factory.getOWLLiteral(value.toString(),
						integerDatatype);

				OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory
						.getOWLDataPropertyAssertionAxiom(dataProp, obj,
								literal);

				manager.addAxiom(ontology, dataPropertyAssertion);

				index = index + 1;
			}
		}
		manager.saveOntology(ontology);
	}
}
