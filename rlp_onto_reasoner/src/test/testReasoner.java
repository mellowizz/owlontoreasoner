package test;

import java.io.File;

import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;

import ontology.ReasonerHermit;

public class testReasoner {
	public static void main(String[] args){
		OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		File file = new File("C:\\Users\\Moran\\ontologies\\EUNIS_ext_small.owl");
		OWLOntology onto = null;
		if( mgr == null || file == null){
			System.out.println("ERROR!!");
		}
		System.out.println("Before try");
		try{
			onto = mgr.loadOntologyFromOntologyDocument(file);
			Reasoner hermit = new Reasoner(onto);
			//OWLReasoner hermit = ReasonerHermit.createOWLReasoner(onto);
			System.out.println(hermit.getReasonerVersion());
			
			
			for (OWLClass c : onto.getClassesInSignature()){
				if (c.getIRI().getFragment().equals("waterlogged")){
					NodeSet<OWLNamedIndividual>instances = hermit.getInstances(c, false);
					System.out.println(c.getIRI().getFragment());
					for (OWLNamedIndividual i : instances.getFlattened()) {
						System.out.println(i.getIRI().getFragment());
					}
				}
			}
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{ 
			System.out.println("STUB:");
		}
	}
}
