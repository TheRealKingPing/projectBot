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
			switch (questionWords.get(counter).getWordType()) {
				case questionWord:					
						
					break;
				default:
					break;
			}
		}
		return null;
	}
	
	public static UsableStatement getUsableStatement(List<Word> sentence) {
		//cut unnecessary parts
		List<Word> cuttedSentence = sentence;
		
		//find verb with "to" in front (infinitives)
		List<Word> infinitives = new ArrayList<Word>();
		List<Word> removableWords = new ArrayList<Word>();		 				
		
		for(Word w : cuttedSentence ) {	
			String test = w.getValue();
			WordType test2 = w.getWordType();
			if(w.getValue().equals("to") && w.getWordType() == WordType.preposition) {
				int indexOfW = cuttedSentence.indexOf(w);
				Word nextWord = cuttedSentence.get(indexOfW + 1);
				if (nextWord.getWordType() == WordType.verb) {
					infinitives.add(nextWord);
					removableWords.add(w);
					removableWords.add(nextWord);
				}							
			}
		}
		
		//remove "to" and verb from cuttedSentence 
		for(Word w : removableWords) {
			cuttedSentence.remove(w);
		}
		
		//find auxiliary verb
		List<Word> auxiliaryVerbs = new ArrayList<Word>();
		int firstAVIndex = 0; 
		
		//add auxiliary verbs
		for (Word w : cuttedSentence) {
			if(w.getWordType() == WordType.auxiliaryVerb) {
				auxiliaryVerbs.add(w);	
				if(firstAVIndex == 0) {
					firstAVIndex = cuttedSentence.indexOf(w);
				}
			}
		}
		
		//remove auxiliary verbs
		for (Word w : auxiliaryVerbs) {
			cuttedSentence.remove(w);
		}
		
		//find main verb (last verb)
		Word inFrontOfMainVerb = null;
		Word mainVerb = null;
		Word afterMainVerb = null;
		int mVIndex = 0;
		
		for (Word w : cuttedSentence) {
			if (w.getWordType() == WordType.verb) {
				mainVerb = w;
				mVIndex = cuttedSentence.indexOf(w);					
				
				//does it have a preposition behind it
				if(mVIndex + 1 < cuttedSentence.size()) {					
					Word nextWord = cuttedSentence.get(mVIndex + 1);
					if(nextWord.getWordType() == WordType.preposition) {
						afterMainVerb = nextWord;
						cuttedSentence.remove(nextWord);
					}
				}
				
				cuttedSentence.remove(w);				
				break;
			}
		}
		//the auxiliary verb is the main verb, if no other verb is found
		if(mainVerb == null) {
			mainVerb = auxiliaryVerbs.get(0);
			mVIndex = firstAVIndex;
		}
		
		//find subject (in front of main verb)
		List<Word> subjects = new ArrayList<Word>();
				
		while(subjects.size() == 0) {
			Word wInFOfMVerb = cuttedSentence.get(mVIndex - 1);			
			if (wInFOfMVerb.getWordType() == WordType.adjective
			 || wInFOfMVerb.getWordType() == WordType.preposition
			 || wInFOfMVerb.getWordType() == WordType.adverb			
			) {
				inFrontOfMainVerb = wInFOfMVerb;
				cuttedSentence.remove(wInFOfMVerb);
				mVIndex--;	
			}
			else {
				subjects.add(wInFOfMVerb);
				cuttedSentence.remove(wInFOfMVerb);
				mVIndex--;	
			}
		}		
		
		//is in front of the subject (, and or...) => make new statement
		
		removableWords = new ArrayList<Word>();
		
		//add second subject
		for (Word w : cuttedSentence) {
			if(cuttedSentence.indexOf(w) >= mVIndex) {
				break;
			}
			if(w.getWordType() == WordType.coordinatingConjunction) {
				Word secondSubject = cuttedSentence.get(cuttedSentence.indexOf(w) - 1);
				subjects.add(secondSubject);
				removableWords.add(w);
				removableWords.add(secondSubject);			
			}
		}
							
		//remove coordinatingConjungtion and subject
		for (Word w : removableWords) {
			cuttedSentence.remove(w);
		}
		
		
		//rest is object
		List<Word> objects = new ArrayList<Word>();
		
		for (Word w : cuttedSentence) {
			//todo: adj. !
			//todo: preposition !
			if(w.getWordType() == WordType.noun || w.getWordType() == WordType.properNoun || w.getWordType() == WordType.pronoun) {
				objects.add(w);
			}
		}
		
		//todo: is in front of the object (, and or..) => make new statement
		
		//arange predicate
		String predicate = "";
		if(inFrontOfMainVerb != null) {
			predicate = inFrontOfMainVerb.getValue();			
		}
		if(predicate == "") {
			predicate = mainVerb.getValue();
		}
		else {
			predicate += mainVerb.getValue().substring(0, 1).toUpperCase() + mainVerb.getValue().substring(1);
		}
		if(afterMainVerb != null) {
			predicate += afterMainVerb.getValue().substring(0, 1).toUpperCase() + afterMainVerb.getValue().substring(1);
		}
		
		
		//arange usabale statements
		UsableStatement respond = new UsableStatement();
		respond.subjects = subjects;
		respond.predicate = predicate;												 				
		respond.objects = objects;		
		
		return respond;
	}
	
	/*public static UsableStatement getUsableStatement(List<Word> sentence) {
		//create new Statement
		UsableStatement output = new UsableStatement();			
		
		//passiv voice?
		//verb = add to predicate (or auxiliaryVerb because it is already defined as predicate)
		for(int counter = 0; counter < sentence.size(); counter++) {			
			String word = sentence.get(counter).getValue();	
			switch (sentence.get(counter).getType()) {
				//auxiliaryVerb = instant predicate
				case auxiliaryVerb:
					//todo: check present, past and future of verb
					output.predicate = dataInstance.getPropertyName(dataInstance.getInfinitive(sentence.get(counter)));
					break;
				case verb:
					break;
				case pronoun:
					//todo: convert pronoun to user... for example: his = User1 (now in count 2 [2])									
					String userName = "";
					if (word.equals("his")) { userName = "User1"; }
					if (word.equals("him")) { userName = "User1"; }
																	
					if (sentence.get(counter + 1).getType().equals(WordType.noun)) {
						output.subjects.add(dataInstance.bindPronounAndNoun(userName, sentence.get(counter + 1).getValue()));	
						if(output.subjects == null) {
							//return "Which " + questionWords.get(counter + 1).getValue() + "?";
						}						
					}
					else {
						output.subjects.add(dataInstance.getSubjectName(word));			
					}												
					break;
				//noun = subject (as actor) or object
				case noun:			
					//todo: sentence from passic voice to active voice!
					if(output.subjects.size() == 0) {
						output.subjects.add(word);
					}								
					output.objects.add(new Word(word, WordType.noun));					
					break;
				case properNoun:
					//firstname and surname to id
					//is next word also a proper noun?					
					if (sentence.size() > counter + 1 && sentence.get(counter + 1).getType().equals(WordType.properNoun)) {						
						String personID = dataInstance.getPersonByName(sentence.get(counter).getValue(), sentence.get(counter + 1).getValue());
						//object or subject
						//is next word a pronoun (f.E. "your")
						if(sentence.size() > counter + 2 && sentence.get(counter + 2).getType().equals(WordType.pronoun)) {
							output.objects.add(new Word(personID, null));
						}
						else {
							output.subjects.add(personID);													
						}	
						//skip next word
						counter++;
					}
					break;
				case adjective:							
					if(sentence.size() > counter + 1 && sentence.get(counter + 1).getType() == WordType.noun) {
						if(dataInstance.searchRestrictionExist(output.objects.get(0).getValue(), output.predicate, sentence.get(counter + 1)) == false) {
							//return "No";
						}										
						output.predicate = output.predicate + word.substring(0, 1).toUpperCase() + word.substring(1);							
						
						counter++;
					}
					else {
						output.objects.set(0, new Word(word, WordType.adjective)); 								
					}					
					break;
				case coordinatingConjunction:
					
					break;
				default:
					break;			
			}
		}	
		return output;
	}*/
	
	//doofi idee
	/*//list vo enere list lul kei lust gah irgendöbis gschids mache odr was
	private static List<List<Word>> splitSentence(List<Word> sentence) {
		List<List<Word>> output = null;
		
		for(int counter = 0; counter < sentence.size(); counter++) {			
			String word = sentence.get(counter).getValue();	
			switch (sentence.get(counter).getType()) {
				case coordinatingConjunction:
					List<Word> newSentece = null;
					for(int nScounter = 0; nScounter < sentence.size(); nScounter++) {
						newSentece.add(sentence.get(nScounter));
					}
					output.add(new List<Word>					
					break;
				default:
					break;
			}
		}
		
		return output;	
	}*/
	
	private static String closedAnswer(List<Word> questionWords) {		
		UsableStatement statement = getUsableStatement(questionWords);
		
		if(dataInstance.searchRestrictionExist(statement.subjects.get(0), statement.predicate, statement.objects.get(0)) == true) {
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
