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
	
	private static void wordNotFound(String word) {
		System.out.print("'" + word + "' not found");
	}
	
	private static String openAnswer(List<Word> questionWords) {
		for(int counter = 0; counter < questionWords.size(); counter++) {			
			String word = questionWords.get(counter).getValue();	
			switch (questionWords.get(counter).getType()) {
				case questionWord:					
						
					break;
				default:
					break;
			}
		}
		return null;
	}
	
	private static String closedAnswer(List<Word> questionWords) {		
		String propertyName = "";
		String subjectName = "";
		Word objectName = null;		
		
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
					objectName = new Word(word, WordType.noun);					
					break;
				case properNoun:
					//firstname and surname to id
					//is next word also a proper noun?					
					if (questionWords.size() > counter + 1 && questionWords.get(counter + 1).getType().equals(WordType.properNoun)) {						
						String personID = dataInstance.getPersonByName(questionWords.get(counter).getValue(), questionWords.get(counter + 1).getValue());
						//object or subject
						//is next word a pronoun (f.E. "your")
						if(questionWords.size() > counter + 2 && questionWords.get(counter + 2).getType().equals(WordType.pronoun)) {
							objectName = new Word(personID, null);
						}
						else {
							subjectName = personID;													
						}	
						//skip next word
						counter++;
					}
					break;
				case adjective:							
					if(questionWords.size() > counter + 1 && questionWords.get(counter + 1).getType() == WordType.noun) {
						if(dataInstance.searchRestrictionExist(objectName.getValue(), propertyName, questionWords.get(counter + 1)) == false) {
							return "No";
						}										
						propertyName = propertyName + word.substring(0, 1).toUpperCase() + word.substring(1);							
						
						counter++;
					}
					else {
						objectName = new Word(word, WordType.adjective);
					}					
					break;
				default:
					break;
			}			
		}
		
		if(dataInstance.searchRestrictionExist(subjectName, propertyName, objectName) == true) {
			return "Yes";
		}
		else {
			return "No";
		}
	}
	
	public static void main (String[] args) {
		QuestionType type = QuestionType.closed;
		
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
			System.out.print(openAnswer(wordList));
		}
		else if(type == QuestionType.closed) {										
			System.out.print(closedAnswer(wordList));
		}
		
		dataInstance.closeData();
	}	
}
