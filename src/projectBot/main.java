package projectBot;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.util.iterator.ExtendedIterator;

public class main {
	public static void output(ExtendedIterator<OntClass> superClasses, String uri) {
		while(superClasses.hasNext()) {
	        OntResource sc = (OntResource)superClasses.next();	        	       
	        
	        if (sc.getURI() != null) {	        		        
	        	String superClass = sc.getURI().toString().replaceAll(uri, "");
	        	System.out.print(superClass);
	        	//String firstSuperClass = question.getSuperClass().getURI().toString().replaceAll(uri, "");	 
	        	switch (superClass) {
	        		case "OpenQuestion": 
	        			System.out.print("Es ist eine offene Frage (W-)\n");	        			
	        			return;	        			
	        		case "ClosedQuestion":
	        			System.out.print("Es ist eine geschlossene Frage (Yes/No)\n");
	        			return;
	        		default:	        			
	        			break;
	        	}
	        }	        	                	        	
	        	        	        	       
	    }
		return;
    }
	
	public static OntClass createHasValue(String uri, String propertyName, String individualName, OntModel m) {
		Property p = m.getProperty(uri + propertyName);				 
		try {
			Individual i = m.getIndividual(uri + individualName.toLowerCase());
			 return(m.createHasValueRestriction(null, p, i));
		}
		catch (Exception e) {
			return null;
		}					
	}
	
	public static void saveModel(OntModel m, String file, String type) {				
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			//m.writeAll(System.out, type);
			m.writeAll(outputStream, type);
		} catch (FileNotFoundException e) {			
			e.printStackTrace(System.out);
		} finally {
			m.close();
		}
	}	
	public static void main (String[] args) {		
		String state = "learn";
		
		SearchWiki a = new SearchWiki();
		try {
			a.searchWiki(state);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.print(e.getMessage());
		} 
		
		//inf.write(System.out, "JSON-LD");																						
		
		/*while(0 == 0) {
		
			OntModel m1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			m1.read("src/projectBot/new.xml", "RDF/XML");
			m1.setStrictMode(false);
			String uri = "http://www.semanticweb.org/z003da4t/ontologies/2017/7/untitled-ontology-3#";
			
			Scanner scan = new Scanner(System.in);
			System.out.println("Enter a question:");
			String input = scan.nextLine();
			if (input.equals("exit")) {
				break;
			}
			
			String[] words = input.split("\\s+");		
			for (int i = 0; i < words.length; i++) {			    
			    words[i] = words[i].replaceAll("[^\\w]", "");
			}
			
			//mit einem Question							
			//create new question
			OntClass sentenceFromUser = m1.getOntClass(uri + "SentenceFromUser");	
			String questionName = "Question" + ThreadLocalRandom.current().nextInt(1, 100000);
			Resource newResQuestion = m1.createResource(uri + questionName);																	
			sentenceFromUser.addSubClass(newResQuestion);					
			
			OntClass savedQuestion = m1.getOntClass(uri + questionName);																
			
			savedQuestion.addSuperClass(createHasValue(uri, "hasWordOnFirst", words[0], m1));
			for (int counter = 1; counter < words.length; counter++) {				
				OntClass newSuperClass = createHasValue(uri, "hasWord", words[counter], m1);
				if(newSuperClass != null) {
					savedQuestion.addSuperClass(newSuperClass);
				}
				else {
					if(state.equals("learn")) {												
						OntClass word = m1.getOntClass(uri + "Word");
						
						ExtendedIterator<OntClass> subClasses = word.listSubClasses();
						
						while(subClasses.hasNext()) {
					        OntClass sc = subClasses.next();
					        String subClass = sc.getURI().toString().replaceAll(uri, "");	
					        System.out.print(subClass + "\n");
					        ExtendedIterator<OntClass> subClassesOfSC = sc.listSubClasses();
					        while(subClassesOfSC.hasNext()) {
					        	OntClass scosc = subClassesOfSC.next();
					        	String subClassOfSubClass = scosc.getURI().toString().replaceAll(uri, "");
					        	System.out.print("-" + subClassOfSubClass + "\n");
					        }					        
						}    
												
						scan = new Scanner(System.in);
						System.out.println("In which Category would you put '" + words[counter] + "'");																			
						input = scan.nextLine();
						
						OntClass category = m1.getOntClass(uri + input);
						
						Individual newWord = m1.createIndividual( uri + words[counter], category );
						
						counter--;
					}
				}
			}
			
			//mit mehr als einem Question
			/*OntClass sentenceFromUser = m1.getOntClass(uri + "SentenceFromUser");
			ExtendedIterator subClasses = sentenceFromUser.listSubClasses();				
			
			Integer counter = 0;			
			while (subClasses.hasNext()) {
				counter++;
				subClasses.next();			
			}
			
			String questionName = "Question" + counter++;
			
			Resource newResQuestion = m1.createResource(uri + questionName);																	
			sentenceFromUser.addSubClass(newResQuestion);	
			
			OntClass savedQuestion = m1.getOntClass(uri + questionName);		
			Property hasWordOnFirst = m1.getProperty(uri + "hasWordOnFirst");						
			Property hasWord = m1.getProperty(uri + "hasWord");	
					
			Individual inputFirstWord = m1.getIndividual(uri + words[0].toLowerCase());
			Individual inputSecondWord = m1.getIndividual(uri + words[1].toLowerCase());				
			
			savedQuestion.addSuperClass(m1.createHasValueRestriction(null, hasWordOnFirst, inputFirstWord));
			savedQuestion.addSuperClass(m1.createHasValueRestriction(null, hasWord, inputSecondWord));	
			
			m1.write(System.out);*/					    			
						
			
			/*saveModel(m1, "src/projectBot/new.xml", "RDF/XML");
			
			OntModel m2 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			m2.read("src/projectBot/new.xml", "RDF/XML");											
			
			Reasoner r = ReasonerRegistry.getOWLReasoner();
			r = r.bindSchema(m2);
			
			OntModelSpec ontModelSpec = OntModelSpec.OWL_DL_MEM;
		    ontModelSpec.setReasoner(r);
		    
		    OntModel model = ModelFactory.createOntologyModel(ontModelSpec, m2);			
		    
		    OntClass question = model.getOntClass(uri + questionName);
		    		    
		    
		    ExtendedIterator<OntClass> superClasses = question.listSuperClasses();
		     	   
		    System.out.print(question.getURI());
		    
		    output(superClasses, uri);		    
		    
		    //delete question
			OntClass oldResQuestion = m2.getOntClass(uri + questionName);
			if(oldResQuestion != null) {
				oldResQuestion.remove();
			}			
		    
			saveModel(m2, "src/projectBot/new.xml", "RDF/XML");
		}*/
	}	
}
