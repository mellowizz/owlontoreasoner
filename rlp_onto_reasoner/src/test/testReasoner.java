package test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
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
			//Reasoner hermit = new Reasoner(onto);
			//OWLReasoner hermit = ReasonerHermit.createOWLReasoner(onto);
			OWLReasoner factplusplus = new FaCTPlusPlusReasonerFactory().createReasoner(onto);
			//System.out.println(hermit.getReasonerVersion());
			System.out.println(factplusplus.getReasonerVersion());
			HashMap<String, ArrayList<String>> classesHash = new HashMap<String, ArrayList<String>>();
			classesHash.put("waterlogged", new ArrayList<String>());
			classesHash.put("periodic_flooding", new ArrayList<String>());
			classesHash.put("riparian", new ArrayList<String>());
			classesHash.put("dry_or_seasonally_wet", new ArrayList<String>());
			// convert nodeset to lis?
			ArrayList<String> classList = new ArrayList<String>();
			classList.add("waterlogged");
			classList.add("periodic_flooding");
			classList.add("riparian");
			classList.add("dry_or_seasonally_wet");
			for (OWLClass c : onto.getClassesInSignature()){
				// dry_or_seasonally wet=0, periodific_flooding=0?
				/*String currClass = c.getIRI().getFragment();
				if (classesHash.get(currClass) == null){
					classesHash.put(c.getIRI().getFragment(), new ArrayList<Integer>());
				}
				*/
				if (classList.isEmpty()) break;
				if (c.getIRI().getFragment().equals(classList.remove(0))){
					String currClass = c.getIRI().getFragment();
					System.out.println("CurrClass:" + currClass);
					//NodeSet<OWLNamedIndividual>instances = hermit.getInstances(c, false);
					NodeSet<OWLNamedIndividual>instances = factplusplus.getInstances(c, false);
					//System.out.println(instances.toString());
					//System.out.println(c.getIRI().getFragment());
					//instances.
					for (OWLNamedIndividual i : instances.getFlattened()) {
						//classesHash.get(currClass, )	
						classesHash.get(currClass).add(i.getIRI().getFragment());
						System.out.println(i.getIRI().getFragment());
					}
					System.out.println("Total: " + instances.getFlattened().size());
				}
			}
			for (Entry<String, ArrayList<String>> ee: classesHash.entrySet()){
				String key = ee.getKey();
				ArrayList<String> values = ee.getValue();
				System.out.println();
				System.out.println(key + ":");
				for (String i : values){
					System.out.println(i);
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
