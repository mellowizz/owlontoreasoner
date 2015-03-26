package equality;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.OWLReasoner;



import ontology.ClassificationComponentOwlAPI;

public class EqualityReasoner {
	Collection<ClassificationComponentOwlAPI> cClregion1;
	Collection<ClassificationComponentOwlAPI> cClregion2;
	
	
	EqualityReasoner(Collection<ClassificationComponentOwlAPI> list){
		this.cClregion1= list;
		//this.cClregion2= cClLists[1];

	}
	
	public HashMap<String, ArrayList<String>> testEquality(Collection<ClassificationComponentOwlAPI> cCLregion, String destinationRegion, OWLReasoner reasoner) throws SQLException, ClassNotFoundException{
		
		HashMap<String, ArrayList<String>> transferValues = new HashMap<String,ArrayList<String>>();
		
		

		for (Iterator<ClassificationComponentOwlAPI> jt = cCLregion.iterator();jt.hasNext();) {
			ClassificationComponentOwlAPI cl = jt.next();
			OWLClass owlClassOrig = cl.getComponent();
			String originClass = owlClassOrig.getIRI().toString();
			System.out.println(originClass);
			
			ArrayList<String> transferClass = this.testSuperClasses(cl, destinationRegion, reasoner);
			transferValues.put(originClass, transferClass);
			
		}
		return transferValues;
		
	}
	

	public ArrayList<String> testSuperClasses(ClassificationComponentOwlAPI cl, String destinationRegion, OWLReasoner reasoner) throws SQLException, ClassNotFoundException{
		ArrayList<String> transferClass = new ArrayList<String>();
		
		System.out.println("	superClasses: "+cl.getComponent()+"\n");
		System.out.println("	superClasses: "+cl.getInferredSuperClasses()+"\n");
		if(cl.getInferredSuperClasses()!=null){
			//System.out.println("	inferred superClasses: "+cl.getInferredSuperClasses()+"\n");
			
			for (OWLClass cls : cl.getInferredSuperClasses()) {
				System.out.println("	superClass: "+cls.getIRI().toString()+"\n");
				System.out.println("		check if "+cls.getIRI().toString().split("#")[1]+" in "+destinationRegion+": "+dataBase.PostGIS_Database.regionCheck(destinationRegion, cls.getIRI().toString().split("#")[1])+"\n");
				if (dataBase.PostGIS_Database.regionCheck(destinationRegion,cls.getIRI().toString().split("#")[1])==true){
					if(transferClass.contains(cls.getIRI().toString().split("#")[1])==false){
						transferClass.add(cls.getIRI().toString().split("#")[1]);
						System.out.println("	"+transferClass);
					}
				}
				
				else{
					for (OWLClass superCls : reasoner.getSuperClasses(cls, true).getFlattened()){
						System.out.println("		supersuperClass: "+superCls.getIRI().toString().split("#")[1]+"\n");
						System.out.println("			check if "+superCls.getIRI().toString().split("#")[1]+" in "+destinationRegion+": "+dataBase.PostGIS_Database.regionCheck(destinationRegion, superCls.getIRI().toString().split("#")[1])+"\n");
						if (dataBase.PostGIS_Database.regionCheck(destinationRegion, superCls.getIRI().toString().split("#")[1])==true){
							if(transferClass.contains(superCls.getIRI().toString().split("#")[1])==false){
								transferClass.add(superCls.getIRI().toString().split("#")[1]);
								System.out.println("	"+transferClass);
							}
						}
						else{
							for (OWLClass superSuperCls : reasoner.getSuperClasses(superCls, true).getFlattened()){
								System.out.println("			supersupersuperClass: "+superSuperCls.getIRI().toString().split("#")[1]+"\n");
								System.out.println("				check if "+superSuperCls.getIRI().toString().split("#")[1]+" in "+destinationRegion+": "+dataBase.PostGIS_Database.regionCheck(destinationRegion, superSuperCls.getIRI().toString().split("#")[1])+"\n");
								if (dataBase.PostGIS_Database.regionCheck(destinationRegion, superSuperCls.getIRI().toString().split("#")[1])==true){
									if(transferClass.contains(superSuperCls.getIRI().toString().split("#")[1])==false){
										transferClass.add(superSuperCls.getIRI().toString().split("#")[1]);
										System.out.println("	"+transferClass);
									}
								}
								
							}
						}
						
					}
				}
			}
		
			/*
			
			for (Iterator<OWLNamedClass> infIt = cl.getInferredSuperClasses().iterator(); infIt.hasNext();){
				OWLNamedClass owlNamedClass = (OWLNamedClass) infIt.next();
				System.out.println("	superClass: "+owlNamedClass.getBrowserText()+"\n");
				System.out.println("	superClass: "+owlNamedClass.getBrowserText().split(":")[1]+"\n");
				System.out.println("	check if "+owlNamedClass.getBrowserText().split(":")[1]+" in "+destinationRegion+": "+dataBase.PostGIS_Database.regionCheck(destinationRegion, owlNamedClass.getBrowserText().split(":")[1])+"\n");
				if (dataBase.PostGIS_Database.regionCheck(destinationRegion, owlNamedClass.getBrowserText().split(":")[1])==true){
					if(transferClass.contains(owlNamedClass.getBrowserText().split(":")[1])==false){
						transferClass.add(owlNamedClass.getBrowserText().split(":")[1]);
						System.out.println("	"+transferClass);
					}
				}
				
				else{
					for (Iterator<OWLNamedClass> It = owlNamedClass.getInferredSuperclasses().iterator(); It.hasNext();){
						OWLNamedClass owlNamedCl = (OWLNamedClass) It.next();
						if(owlNamedCl.getBrowserText().contains(":")){
							System.out.println("		"+owlNamedClass.getBrowserText().split(":")[1]+"\n");
							System.out.println("			superClass: "+owlNamedCl.getBrowserText().split(":")[1]+"\n");
							System.out.println("			check if "+owlNamedCl.getBrowserText().split(":")[1]+" in "+destinationRegion+": "+dataBase.PostGIS_Database.regionCheck(destinationRegion, owlNamedCl.getBrowserText().split(":")[1])+"\n");
						
							if (dataBase.PostGIS_Database.regionCheck(destinationRegion, owlNamedCl.getBrowserText().split(":")[1])==true){
								if(transferClass.contains(owlNamedCl.getBrowserText().split(":")[1])==false){
									transferClass.add(owlNamedCl.getBrowserText().split(":")[1]);
									System.out.println("	"+transferClass);
								}
							}
						}
						else{
							for (Iterator<OWLNamedClass> Iter = owlNamedCl.getInferredSuperclasses().iterator(); Iter.hasNext();){
								OWLNamedClass owlNamedC = (OWLNamedClass) Iter.next();
								if(owlNamedC.getBrowserText().contains(":")){
									System.out.println("			Das will ich splitten: "+owlNamedC.getBrowserText()+"\n");
									//System.out.println("				superClass: "+owlNamedC.getBrowserText().split(":")[1]+"\n");
									//System.out.println("				check if "+owlNamedC.getBrowserText().split(":")[1]+" in "+destinationRegion+": "+dataBase.PostGIS_Database.regionCheck(destinationRegion, owlNamedC.getBrowserText().split(":")[1])+"\n");
								
									if (dataBase.PostGIS_Database.regionCheck(destinationRegion, owlNamedC.getBrowserText().split(":")[1])==true){
										if(transferClass.contains(owlNamedC.getBrowserText().split(":")[1])==false){
											transferClass.add(owlNamedC.getBrowserText().split(":")[1]);
											System.out.println("		"+transferClass);
										}
									}		
								}
							
							}
						}
					}
				}
			}
			*/
			
		}
		return transferClass;
		
		
	}

}



