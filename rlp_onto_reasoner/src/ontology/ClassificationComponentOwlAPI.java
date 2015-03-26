package ontology;

import java.util.Set;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;



public class ClassificationComponentOwlAPI {

	
	//private NodeSet<OWLClass> superClassesString;
	
	private Set<OWLClass> inferredSuperClasses;

	private Set<OWLClass> inferredEquivalentClasses;
	
	private Set<OWLClass> inferredSubClasses;
	
	//private ArrayList<String> transferClasses;
	
	//private Number Key;
	
	//private int superClassesCount;
	
	//private Set<OWLClass> subClasses;
	
	//private NodeSet<OWLClass> superClasses;
	
	private OWLReasoner reasoner;
	
	private OWLClass component;
	
	OWLOntologyManager manager;
	
	public static ClassificationComponentOwlAPI createClassificationComponent(OWLClass Component, OWLReasoner reasoner){
		
		ClassificationComponentOwlAPI ClCompOwlAPI = new ClassificationComponentOwlAPI();
		ClCompOwlAPI.reasoner = reasoner;
		ClCompOwlAPI.component = Component;
		return ClCompOwlAPI;		
	}

	
	public void inferSuperClasses(){
		NodeSet<OWLClass> subClses = reasoner.getSuperClasses(component, true);
		this.inferredSuperClasses = subClses.getFlattened();
	}

	
	public void inferEquivalentClasses(){
		Node<OWLClass> equivalentClses = reasoner.getEquivalentClasses(component);
		this.inferredEquivalentClasses = equivalentClses.getEntities();
	}
	

	public void inferSubClasses(){
		NodeSet<OWLClass> subClses = reasoner.getSubClasses(component, true);
		this.inferredSubClasses = subClses.getFlattened();		
	}
		
	public void setComponent(OWLClass ClassComponent){
		component = ClassComponent;
	}
		
	public OWLClass getComponent(){
		return component;
	}
		
	public  Set<OWLClass> getInferredSuperClasses(){
		return this.inferredSuperClasses;
	}
		
	public  Set<OWLClass> getInferredEquivalentClasses(){
		return this.inferredEquivalentClasses;
	}
		
	public Set<OWLClass> getInferredSubClasses(){
		return this.inferredSubClasses;
	}
		
}
		
		/*		
		public Collection<String> getSuperClassesString(){
			return this.superClassesString;
		}
		
		
		public Collection<OWLNamedClass> getInferredSuperClasses(){
			return this.inferredSuperClasses;
		}
		
		public Collection<OWLNamedClass> getInferredEquivalentClasses(){
			return this.inferredEquivalentClasses;
		}
		
		public Collection<OWLNamedClass> getInferredSubClasses(){
			return this.inferredSubClasses;
		}
		
		public  Collection<OWLNamedClass> getSuperClasses(){
			//System.out.println(this.superClasses);
			return this.superClasses;
		}
		
		public  Collection<OWLNamedClass> getSubClasses(){
			//System.out.println(this.superClasses);
			return this.subClasses;
		}
		
		
		public int  superClassesCount(){
			return this.superClassesCount;
		}
		
		public void superClasses(){
			RDFSNamedClass cls = owlModel.getRDFSNamedClass(this.CComponent);
			if(cls!=null){
				//this.superClasses =cls.getSuperclasses();
				superClasses = new ArrayList<OWLNamedClass>();
				for (Iterator<ProtegeCls> it = cls.getSuperclasses().iterator(); it.hasNext();){
					ProtegeCls protegeCls = it.next();
					if(protegeCls.getBrowserText().contains("has")==false){
						OWLNamedClass owlClass = owlModel.getOWLNamedClass(protegeCls.getBrowserText());
						this.superClasses.add(owlClass);
					}
				}
					
			}
		}
		public void subClasses(){
			System.out.println(this.CComponent.trim());
			//System.out.println(owlModel.getR);
			
			RDFSNamedClass cls = owlModel.getRDFSNamedClass(this.CComponent);
			System.out.println(cls);
			if(cls!=null){
				System.out.println(cls);
				//this.superClasses =cls.getSuperclasses();
				subClasses = new ArrayList<OWLNamedClass>();
				for (Iterator<ProtegeCls> it = cls.getSubclasses().iterator(); it.hasNext();){
					ProtegeCls protegeCls = it.next();
					if(protegeCls.getBrowserText().contains("has")==false){
						OWLNamedClass owlClass = owlModel.getOWLNamedClass(protegeCls.getBrowserText());
						this.subClasses.add(owlClass);
					}
				}
					
			}
		}
		
		
		
		
		public void superClassesString(){
			RDFSNamedClass cls = owlModel.getRDFSNamedClass(this.CComponent);
			if(cls!=null){
				//this.superClasses =cls.getSuperclasses();
				superClassesString = new ArrayList<String>();
				for (Iterator<ProtegeCls> it = cls.getSuperclasses().iterator(); it.hasNext();){
					ProtegeCls protegeCls = it.next();
					if(protegeCls.getBrowserText().contains("has")==false){
						OWLNamedClass owlClass = owlModel.getOWLNamedClass(protegeCls.getBrowserText());
						this.superClassesString.add(protegeCls.getBrowserText());
					}
				}
					
			}
		}
		
		
		@SuppressWarnings("unchecked")
		public void inferredSuperClasses(){
		
			OWLNamedClass cls = owlModel.getOWLNamedClass(this.CComponent); 
			if(cls!=null){
					this.inferredSuperClasses = cls.getInferredSuperclasses();
				}
			}
		
		@SuppressWarnings("unchecked")
		public void inferredSubClasses(){
			OWLNamedClass cls = owlModel.getOWLNamedClass(this.CComponent);
			if(cls!=null){
				this.inferredSubClasses = cls.getInferredSubclasses();
				
			}
		}

			
		@SuppressWarnings("unchecked")
		public void inferredEquivalentClasses(){
		
			OWLNamedClass cls = owlModel.getOWLNamedClass(this.CComponent); 
			if(cls!=null){
			
				this.inferredEquivalentClasses = cls.getInferredEquivalentClasses();
			}
		}


		public void setKey(Number number) {
			this.Key = number;
			
		}
		
		public Number getKey(){
			return Key;
		}


		public void setTransferClasses(ArrayList<String> arrayList) {
			this.transferClasses= arrayList;
			
		}
		
		public ArrayList<String> getTransferClasses() {
			return transferClasses;
			
		}
		
		
	/*	
	 * 
	 * 
	 * public Collection<OWLRestriction> getRestrictions(){
			return this.restrictions;
			
		}
		
		public Collection<String> getSuperCl(){
			return this.superClasses;
		}
		
		public void restictions(){
			Collection classes = this.owlModel.getUserDefinedOWLNamedClasses();
			Collection<OWLRestriction> restrictions = new LinkedList<OWLRestriction>();
			for (Iterator it = classes.iterator(); it.hasNext();) {
				
			    OWLNamedClass cls = (OWLNamedClass) it.next();
			    //if (cls.getDisjointClasses().isEmpty()==false)
			    //	{System.out.println(cls.getBrowserText());
			    //}
			    
			    Collection hasValues = cls.getRestrictions(true);
			    Collection<ProtegeCls> superClasses = cls.getSuperclasses();
			    if(cls.getBrowserText().isEmpty() == false){
			    	if (cls.getBrowserText().equals(this.CComponent)){
			    	
			    		for (Iterator jt = hasValues.iterator(); jt.hasNext();) {
			    			OWLRestriction rest = (OWLRestriction) jt.next();
			    		 	if (rest.getBrowserText().startsWith("hasAttribute")){
				    			restrictions.add(rest);
				    			}
			    		}
			    	}	
			    	}
			}	
			this.restrictions = restrictions;
		}
		
		
		
		@SuppressWarnings("unchecked")
		public void SuperCl(){
			Collection<OWLNamedClass> classes = this.owlModel.getUserDefinedOWLNamedClasses();
			Collection<String> SuperClasses = new LinkedList<String>();
			for (Iterator<OWLNamedClass> it = classes.iterator(); it.hasNext();) {
				
			    OWLNamedClass cls = (OWLNamedClass) it.next();

			    @SuppressWarnings("deprecation")
				Collection<ProtegeCls> superClasses = cls.getSuperclasses();
			    if (cls.getBrowserText().equals(this.CComponent)){
			    	int z=0;
			    	for (Iterator<ProtegeCls> jt = superClasses.iterator(); jt.hasNext();) {
			    		ProtegeCls rest = jt.next();
			    		 if (rest.getBrowserText().startsWith("has")){
				    			;
				    			}
			    		 else{
			    			 if (z!=0){
			    				 SuperClasses.add(rest.getBrowserText());
				    			}
				    			z=z+1;	
			    		 	}	
			    		}	
			    	}	
				}
			this.superClasses = SuperClasses;
			}
		
	*/
		
		



