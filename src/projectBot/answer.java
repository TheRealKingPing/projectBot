package projectBot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.util.iterator.ExtendedIterator;
import data.dataFunctions;

public class answer {	
	private static dataFunctions dataInstance = new dataFunctions();		
	
	public static String sparqlAnswer(List<Word> questionWords) {		
		String propertyName = "";
		String subjectName = "";
		String questionPointName = "";
		
		WordType type;
		for(int counter = 0; counter < questionWords.size(); counter++) {			
			String word = questionWords.get(counter).getValue();	
			switch (questionWords.get(counter).getType()) {
				case auxiliaryVerb:
					//todo: check present, past and future of verb
					propertyName = dataInstance.getPropertyName(dataInstance.getInfinitive(questionWords.get(counter)));					
					break;
				case verb:
					break;
				case pronoun:
					//todo: convert pronoun to user... for example: his = User1 (now in count 2 [2])									
					String userName = "";
					if (word.equals("his")) { userName = "User1"; }
					if (word.equals("him")) { userName = "User1"; }
																	
					if (questionWords.get(counter + 1).getType().equals(WordType.noun)) {
						subjectName = dataInstance.bindPronounAndNoun(userName, questionWords.get(counter + 1).getValue());	
						if(subjectName == null) {
							return "Which " + questionWords.get(counter + 1).getValue() + "?";
						}						
					}
					else {
						subjectName = dataInstance.getSubjectName(word);			
					}												
					break;
				case noun:					
					break;
				case adjective:
					questionPointName = word;
					break;
				default:
					break;
			}			
		}
		
		if(dataInstance.searchRestrictionExist(subjectName, propertyName, questionPointName) == true) {
			return "Yes";
		}
		else {
			return "No";
		}
	}
	
	public static void main (String[] args) {
		QuestionType type = QuestionType.closed;
		String[][] questionWords1 = {
				{"is", "auxiliary verb", "be", "present"},
				{"he", "pronoun", "User1", "possesive"},
				{"working", "verb", "work"},
				{"very"},
				{"hard"}
		};
		
		//Do you usually walk to work
		String[][] questionWords2 = {
				{"do", "auxiliary verb", "do", "present"},
				{"you", "pronoun", "me", "possesive"},
				{"usually", "adverb"},
				{"walk", "verb"},
				{"to", "preposition"},
				{"work", "noun"}
		};
		
		String[][] questionWords3 = {
				{"was", "auxiliary verb", "be", "past"},
				{"his", "pronoun", "User1", "possesive"},
				{"idea", "noun"},
				{"interesting", "adjective"}
		};		
		
		//Are you hungry?
		String[][] questionWords4 = {
				{"are", "auxiliary verb", "be", "plural"},
				{"you", "pronoun", "me", "possesive"},
				{"hungry", "adjective"}
		};
		
		//Are Spanish and German different languages?
		String[][] questionWords5 = {
				{"are", "auxiliary verb", "be", "present"}, 
				{"spanish", "noun", "subject"}, 
				{"and", "conjunction", "subject"},
				{"german", "noun", "subject"},
				{"different", "adjective", "languages"},
				{"languages", "noun", "language"}
		};
		
		//was it hot outside?
		String[][] questionWords6 = {
				{"was", "auxiliary verb", "be", "past"}, 
				{"it", "pronoun"}, 
				{"hot"},
				{"outside"}			
		};
		
		//Have you seen Greg
		String[][] questionWords7 = {
				{"have", "auxiliary verb", "have", "present"},
				{"you", "pronoun", "me", "possesive"},
				{"seen", "verb", "see", "past participle"},
				{"greg", "noun", "human"}
		};
		
		//have you talked with him
		String[][] questionWords8 = {
				{"have", "auxiliary verb", "have", "present"},
				{"you", "pronoun", "me", "possesive"},
				{"talked", "verb", "talk"},
				{"with", "preposition"},
				{"him", "noun", "User1"}
		};
		
		//was his story interesting?
		String[][] questionWords9 = {
				{"was", "auxiliary verb", "be", "past"},
				{"his", "pronoun", "User1", "possesive"},
				{"story", "noun"},
				{"interesting", "adjective"}
		};
		
		dataInstance.openData("src/projectBot/new.xml", "RDF/XML");
			
		
		//get question
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter a question:");
		String input = scan.nextLine();		
		
		List<Word> wordList = new ArrayList();
		String[] words = input.split("\\s+");		
		for (int i = 0; i < words.length; i++) {			    
			words[i] = words[i].replaceAll("[^\\w]", "");		    
		    wordList.add(new Word(words[i], dataInstance.getType(words[i])));
		}
		
		if(type == QuestionType.open) {
			
		}
		else if(type == QuestionType.closed) {						
			//System.out.print(answer(questionWords));					
			System.out.print(sparqlAnswer(wordList));
			
			
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
		
		dataInstance.closeData();
	}	
}
