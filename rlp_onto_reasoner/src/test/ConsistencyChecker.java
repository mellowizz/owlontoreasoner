package test;

import java.util.Set;

import org.semanticweb.HermiT.Reasoner;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.ExplanationGenerator;

import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrdererImpl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
public class ConsistencyChecker {
	public static void main(String[] args) throws Exception {
	    OWLOntologyManager m=OWLManager.createOWLOntologyManager();

	   OWLOntology o=m.loadOntologyFromOntologyDocument(IRI.create("http://www.cs.ox.ac.uk/isg/ontologies/UID/00793.owl"));

	      // Reasoner hermit=new Reasoner(o);
	        OWLReasoner owlreasoner=new Reasoner.ReasonerFactory().createReasoner(o);
	        System.out.println(owlreasoner.isConsistent());

	      //System.out.println(hermit.isConsistent());

	        //---------------------------- Copied from example--------- 
	        OWLDataFactory df = m.getOWLDataFactory();
	        OWLClass testClass = df.getOWLClass(IRI.create("urn:test#testclass"));
	        m.addAxiom(o, df.getOWLSubClassOfAxiom(testClass, df.getOWLNothing()));
	        OWLNamedIndividual individual = df.getOWLNamedIndividual(IRI
	                .create("urn:test#testindividual"));
	        m.addAxiom(o, df.getOWLClassAssertionAxiom(testClass, individual));

	        //----------------------------------------------------------

	        Node<OWLClass> unsatisfiableClasses = owlreasoner.getUnsatisfiableClasses();
	        //Node<OWLClass> unsatisfiableClasses = hermit.getUnsatisfiableClasses();
	        for (OWLClass owlClass : unsatisfiableClasses) {
	            System.out.println(owlClass.getIRI());
	        }
	        //-----------------------------
	        ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager.createExplanationGeneratorFactory((OWLReasonerFactory) owlreasoner);
	        ExplanationGenerator<OWLAxiom> gen = genFac.createExplanationGenerator(o);

	        //-------------------------

	        InconsistentOntologyExplanationGeneratorFactory igf = new InconsistentOntologyExplanationGeneratorFactory((OWLReasonerFactory) owlreasoner, 10000);
	        //InconsistentOntologyExplanationGeneratorFactory igf = new InconsistentOntologyExplanationGeneratorFactory((OWLReasonerFactory) hermit, 10000);
	        ExplanationGenerator<OWLAxiom> generator = igf.createExplanationGenerator(o);

	        OWLAxiom entailment = df.getOWLClassAssertionAxiom(df.getOWLNothing(),
	                individual);

	        //-------------
	        Set<Explanation<OWLAxiom>> expl = gen.getExplanations(entailment, 5);
	        //------------

	        System.out.println("Explanation "
	                + generator.getExplanations(entailment, 5));
	    }
}