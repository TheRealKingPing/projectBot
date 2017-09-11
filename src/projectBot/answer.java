package projectBot;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;

public class answer {
	public final static String uri = "http://www.semanticweb.org/z003da4t/ontologies/2017/7/untitled-ontology-3#";
	
	public static OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	
	//todo: not only infinitive => every verb
	//get subject name by infinitive
	private static String getPropertyName(String infinitive) { 
		switch(infinitive) {
			case "be":
				return "is";
			case "have":
				return "has";
			default:
				return null;
		}		
	}
	
	//get subject name by pronoun
	private static String getSubjectName(String pronoun) {
		switch(pronoun) {
			case "you":
				return "me";				
			default:
				return null;
		}
	}
	
	//Bind pronoun and noun
	private static String bindPronounAndNoun(String userName, String nounName) {						
		String propertyName = getPropertyName("have");
		Property property = m.getProperty(uri + propertyName);			
		
		OntClass pronoun = m.getOntClass(uri + userName);				
		
		ExtendedIterator<OntClass> pronounSuperC = pronoun.listSuperClasses();									
		
		while(pronounSuperC.hasNext()) {	
			OntClass sc = pronounSuperC.next();
			if (sc.isRestriction()) {
				Restriction r = sc.asRestriction();					
				if(property.equals(r.getOnProperty())) {													
					String scNounName = r.asSomeValuesFromRestriction().getSomeValuesFrom().getURI().toString().replaceAll(uri, "");							
					if(nounName.equals(nounName)) {
						return scNounName;						
					}
				}
			}
		}												
		return null;
	}
	
	public static String answer(String[][] questionWords) {
		String propertyName = "";
		String subjectName = "";
		String questionPointName = "";
				
		for(int counter = 0; counter < questionWords.length; counter++) {			
			String word = questionWords[counter][0];
			switch (questionWords[counter][1]) {
				case "verb":
					propertyName = getPropertyName(questionWords[counter][2]);
					break;
				case "pronoun":
					//todo: convert pronoun to user... for example: his = User1 (now in count 2 [2])									
					String userName = "";
					if (word.equals("his")) { userName = "User1"; }
					
					String nextWord = questionWords[counter + 1][1];					
					if (nextWord. equals("noun")) {
						subjectName = bindPronounAndNoun(userName, nextWord);
					}
					else {
						subjectName = getSubjectName(word);			
					}												
					break;
				case "noun":					
					break;
				case "adjective":
					questionPointName = word;
					break;
				default:
					break;
			}				
		}									
		//todo: check if subject is in db
		OntClass subject = m.getOntClass(uri + subjectName);
		ExtendedIterator<OntClass> subjectSuperC = subject.listSuperClasses();
		
		//todo: check if property is in db
		Property property = m.getProperty(uri + propertyName);							
		
		while(subjectSuperC.hasNext()) {
			OntClass sc = subjectSuperC.next();
			//das gleiche wie oben (Mach ne funktion)
			if (sc.isRestriction()) {
				Restriction r = sc.asRestriction();					
				if(property.equals(r.getOnProperty())) {													
					String adjectiveName = r.asHasValueRestriction().getHasValue().asResource().getURI().toString().replaceAll(uri, "");
					if(adjectiveName.equals(questionPointName)) {
						return "Yes";
					}
				}
			}					
		}		
		return "No";														
	}
	
	public static void main (String[] args) {
		QuestionType type = QuestionType.closed;
		String[][] questionWords1 = {
				{"was", "verb", "be", "past"},
				{"his", "pronoun", "User1", "possesive"},
				{"idea", "noun"},
				{"interesting", "adjective"}
		};		
		
		//Are you hungry?
		String[][] questionWords = {
				{"are", "verb", "be", "plural"},
				{"you", "pronoun", "me", "possesive"},
				{"hungry", "adjective"}
		};
		
		//Are Spanish and German different languages?
		String[][] questionWords2 = {
				{"are", "verb", "be", "present"}, 
				{"spanish", "noun", "subject"}, 
				{"and", "conjunction", "subject"},
				{"german", "noun", "subject"},
				{"different", "adjective", "languages"},
				{"languages", "noun", "language"}
		};
		
		m.read("src/projectBot/new.xml", "RDF/XML");
		m.setStrictMode(false);		
				
		if(type == QuestionType.open) {
			
		}
		else if(type == QuestionType.closed) {						
			System.out.print(answer(questionWords));					
			
			//OntClass user = m1.getOntClass(uri + questionWords[1][1]);	
			
			/*while ( it.hasNext() ) {				
			  System.out.print( it.next());
			}
			
			/*Statement statement = user.getRequiredProperty(has);			
			System.out.print(statement.getResource().getURI());
			
			/*
			// Get the property and the subject
			Property driverOf = m1.getProperty(uri + "has");
			Resource bus = m1.getResource(uri + questionWords[1][1]);

			// Get all statements/triples of the form (****, driverOf, bus)
			StmtIterator stmtIterator = m1.listStatements(null, driverOf, bus);
			//StmtIterator stmtIterator = m1.listStatements();
			while (stmtIterator.hasNext()){
			    Statement s = stmtIterator.nextStatement();
			    Resource busDriver = s.getResource();
			    // do something to the busdriver (only nice things, everybody likes busdrivers)
			    System.out.print(busDriver.getURI() + "\n");
			}*/
		}
	}	
}