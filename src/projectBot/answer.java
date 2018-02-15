package projectBot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
	
	public static List<Word> getSentence(UsableStatement statement) {
		
		
		
		
		return null;
	}		
	
	private static List<Word> transformWQuestionToSentence(List<Word> question) {
		return null;
	}
	
	private static List<Word> transformYNQuestionToSentence(List<Word> question) {
		//You to I
		for(Word w : question) {
			if(w.getValue().toLowerCase().equals("you")) {
				w.setValue("I");
			}
		}		
		
		//todo: eigene funktion machen und auch bei getusablestatement benutzen
		//identify subject				
		int subjectEndIndex = 0;
		
		for(int c = 1; c < question.size(); c++) {
			Word w = question.get(c);
			Word nW = question.get(c + 1);
			if(w.getWordTypes().get(0) == WordType.noun && nW.getWordTypes().get(0) != WordType.coordinatingConjunction) {
				subjectEndIndex = c;
				break;
			}
		}
		
		//switch position verb and subject
		question.add(subjectEndIndex + 1, question.get(0));
		question.remove(0);		
		
		//define index again
		List<Word> respond = new ArrayList<Word>();
		int counter = 0;
		for(Word w : question) {			
			w.setIndex(counter);
			respond.add(w);
			counter++;
		}
		
		return respond;
	}
	
	public static List<UsableStatement> getUsableStatement(List<Word> sentence) {			
		//todo: check if it has a verb
		
		//fill empty type list with "properNoun"	
		int counter = 0;
		List<WordType> typeList = new ArrayList<WordType>();
		typeList.add(WordType.properNoun);
		for(Word w : sentence) {					
			if(w.getWordTypes().size() == 0) {
				w.setWordTypes(typeList);
				sentence.set(counter, w);
			}
			counter++;
		}
		
		//todo: delete me
		for(Word w : sentence) {
			String output = "\"" + w.getValue() + "\" -> | ";
			for(WordType wt : w.getWordTypes()) {
				output = output + wt.toString() + " | ";
			}
			System.out.print(output + "\n");
		}
		System.out.print("---------------------------\n");
						
		List<UsableStatement> respondStatements = new ArrayList<UsableStatement>();
		
		//cut unnecessary parts
		List<Word> cuttedSentence = new ArrayList<Word>(sentence);		
		//cut 'also'
		for(Word w : sentence) {
			if(w.getValue().equals("also")) {
				cuttedSentence.remove(w);
			}
		}		
		
		//name to person id
		for(int c = 0; c < cuttedSentence.size(); c++) {
			Word wFn = cuttedSentence.get(c);
			if(cuttedSentence.size() > c + 1) {
				Word wSn = cuttedSentence.get(c + 1);				
				if(wFn.getWordTypes().isEmpty() == wSn.getWordTypes().isEmpty()) {
					if(wFn.getWordTypes().get(0) == WordType.properNoun && wSn.getWordTypes().get(0) == WordType.properNoun) {
						
							//todo people with middlename
						
							String personID = dataInstance.getPersonByName(wFn.getValue(), wSn.getValue());
							//create new person id
							if(personID == null) {
								personID = "Person" + dataInstance.createNewPersonID(wFn.getValue(), wSn.getValue());
							}
								
							//überprüfen ob id wirklich erstellt!
										
							//todo: is people as WordType rational
							
							List<WordType> pTypeList = new ArrayList<WordType>();
							pTypeList.add(WordType.person);
							Word person = new Word(personID, pTypeList, c);
							cuttedSentence.add(c, person);
							cuttedSentence.remove(wFn);
							cuttedSentence.remove(wSn);
							
							sentence.add(c, person);
							sentence.remove(wFn);
							sentence.remove(wSn);
							//if you want to change the sentence list you have to refresh the index of each item
							int index = 0; 
							for(Word w : sentence) {
								w.setIndex(index);
								index++;
							}
							
						}
				}				
			}			
		}						
		
		//find adjective-noun constructions, create new statement and cut adjective
		List<Word> removableAdj = new ArrayList<Word>();
		
		for(int c = 1; c < cuttedSentence.size(); c++) {
			Word w = cuttedSentence.get(c);
			Word lW = cuttedSentence.get(c - 1);
			if(lW.getWordTypes().contains(WordType.adjective) && w.getWordTypes().contains(WordType.noun)) {
				//create this construct in database
				//dataInstance.insertClass(w.getValue(), null);
				//dataInstance.insertClass(lW.getValue() + "_" + w.getValue(), w.getValue());
				
				UsableStatement adjNounStatement = new UsableStatement();				
				adjNounStatement.subjects.add(new Word(lW.getValue(), lW.getWordTypes()));
				adjNounStatement.predicate = "is";
				adjNounStatement.objects.add(new Word(w.getValue(), w.getWordTypes()));
				
				respondStatements.add(adjNounStatement);
				removableAdj.add(lW);
				
				//define potential noun as noun
				List<WordType> nounType = new ArrayList<WordType>();
				nounType.add(WordType.noun);
				w.setWordTypes(nounType);
				w.setValue(lW.getValue());
			}
		}
		
		//remove adj.
		for(Word w : removableAdj) {
			cuttedSentence.remove(w);
		}	
		
		//todo: create findconstruction method
		//find article-noun construction and cut article
		List<Word> removableArt = new ArrayList<Word>();	
		
		for(int c = 1; c < cuttedSentence.size(); c++) {
			Word w = cuttedSentence.get(c);
			Word lW = cuttedSentence.get(c - 1);
			if(lW.getWordTypes().contains(WordType.article) && w.getWordTypes().contains(WordType.noun)) {
				removableArt.add(lW);
				
				//define potential noun as noun
				List<WordType> nounType = new ArrayList<WordType>();
				nounType.add(WordType.noun);
				w.setWordTypes(nounType);
			}			
		}
		
		//remove article
		for(Word w : removableArt) {
			cuttedSentence.remove(w);
		}				
		
		//todo: find better solution for every preposition
		//preposition
		List<Word> removablePrep = new ArrayList<Word>();
		
		for(int c = 1; c < cuttedSentence.size(); c++) {
			Word w = cuttedSentence.get(c);
			if(w.getWordTypes().contains(WordType.preposition)) {
				if(
						cuttedSentence.get(c - 1) != null &&
						cuttedSentence.get(c + 1) != null					
						) {
					Word lW = cuttedSentence.get(c - 1);
					Word nW = cuttedSentence.get(c + 1);
					if(						
							lW.getWordTypes().contains(WordType.noun) &&
							nW.getWordTypes().contains(WordType.noun)
							) {
						UsableStatement prepStatement = new UsableStatement();				
						prepStatement.subjects.add(new Word(lW.getValue(), lW.getWordTypes()));
						prepStatement.predicate = "is";
						prepStatement.objects.add(new Word(nW.getValue(), nW.getWordTypes()));
						
						respondStatements.add(prepStatement);
						removablePrep.add(w);
						removablePrep.add(nW);
					}
				}	
			}				
		}		
		//remove preposition
		for(Word w : removablePrep) {
			cuttedSentence.remove(w);
		}
		
		//find verb with "to" in front (infinitives)
		List<Word> infinitives = new ArrayList<Word>();
		List<Word> removableWords = new ArrayList<Word>();		 				
		
		for(Word w : cuttedSentence) {	
			String wv = w.getValue();
			List<WordType> wtl = w.getWordTypes();
			if(w.getValue().equals("to") && w.getWordTypes().contains(WordType.preposition)) {
				int indexOfW = cuttedSentence.indexOf(w);
				Word nextWord = cuttedSentence.get(indexOfW + 1);
				if (nextWord.getWordTypes().contains(WordType.verb)) {
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
			if(w.getWordTypes().contains(WordType.auxiliaryVerb)) {
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
		List<Word> afterMainVerb = new ArrayList<Word>();
		int mVIndex = 0;
		
		for (Word w : cuttedSentence) {
			if (w.getWordTypes().get(0) == WordType.verb) {								
				//define potential verb as verb
				List<WordType> verbType = new ArrayList<WordType>();
				verbType.add(WordType.verb);
				w.setWordTypes(verbType);
				
				mainVerb = w;
				
				cuttedSentence.set(cuttedSentence.indexOf(w), w);
				mVIndex = cuttedSentence.indexOf(w);					
				
				//does it have a preposition behind it
				if(mVIndex + 1 < cuttedSentence.size()) {					
					Word nextWord = cuttedSentence.get(mVIndex + 1);
					if(nextWord.getWordTypes().contains(WordType.preposition)) {
						afterMainVerb.add(nextWord);
						cuttedSentence.remove(nextWord);
					}
				}											
				break;
			}
		}
		//the auxiliary verb is the main verb, if no other verb is found
		if(mainVerb == null) {
			mainVerb = auxiliaryVerbs.get(0);
			mVIndex = firstAVIndex;
		}
		
		//find pronoun-noun construction and cut pronoun
		List<Word> removablePronoun = new ArrayList<Word>();	
		
		for(int c = 1; c < cuttedSentence.size(); c++) {
			Word w = cuttedSentence.get(c);
			Word lW = cuttedSentence.get(c - 1);
			if(lW.getWordTypes().contains(WordType.pronoun) && w.getWordTypes().contains(WordType.noun)) {
				removablePronoun.add(lW);
				
				//define potential noun as noun
				List<WordType> nounType = new ArrayList<WordType>();
				nounType.add(WordType.noun);
				w.setWordTypes(nounType);
			}
		}
		
		//remove pronoun
		for(Word w : removablePronoun) {
			cuttedSentence.remove(w);
		}	
		
		//find subject (in front of main verb)
		List<Word> subjects = new ArrayList<Word>();		
		Word verbInFrontOfMV = null;
		
		for(Word w : auxiliaryVerbs) {
			if(w.getIndex() < mainVerb.getIndex()) {
				if(w.getIndex() + 1 != mainVerb.getIndex()) {
					verbInFrontOfMV = w;
				}
			}
		}
		if(verbInFrontOfMV == null) {
			for(int c = 0; c < mainVerb.getIndex(); c++) {
				if(cuttedSentence.contains(sentence.get(c))) {
					subjects.add(sentence.get(c));	
				}				
			}
		}
		else {
			for(int c = verbInFrontOfMV.getIndex(); c < mainVerb.getIndex(); c++) {
				if(cuttedSentence.contains(sentence.get(c))) {
					subjects.add(sentence.get(c));	
				}	
			}
		}
		
		//divide subjects
		List<Word> usableSubjects = new ArrayList<Word>();
		
		if(subjects.size() == 1) {			
			usableSubjects = subjects;
			cuttedSentence.remove(usableSubjects.get(0));
		}	
		else {
			for (Word w : subjects) {
				for (WordType wt : w.getWordTypes()) {
					switch(wt) {
						case adverb:
							inFrontOfMainVerb = w;
							cuttedSentence.remove(w);
							break;
						case noun:
						case pronoun:
						case properNoun:
							usableSubjects.add(w);
							cuttedSentence.remove(w);
							break;		
						default:
							break;
				}
				}
				
			}
		}
				
		//is in front of the subject (, and or...) => make new statement
		
		removableWords = new ArrayList<Word>();
							
		//remove coordinatingConjungtion and subject
		for (Word w : removableWords) {
			cuttedSentence.remove(w);
		}
		
		
		//rest is object
		List<Word> objects = new ArrayList<Word>();
		List<Word> modifier = new ArrayList<Word>();
		
		for (Word w : cuttedSentence) {
			//todo: adj. !
			//todo: preposition !
			/*if(w.getWordTypes().get(0) == WordType.adjective 
			|| w.getWordTypes().get(0) == WordType.preposition			
			|| w.getWordTypes().get(0) == WordType.prossessivePronoun) {
				modifier.add(w);
			}*/
			if(w.getWordTypes().get(0) == WordType.adjective
					
			|| w.getWordTypes().get(0) == WordType.noun 
			|| w.getWordTypes().get(0) == WordType.properNoun 
			|| w.getWordTypes().get(0) == WordType.pronoun) {				
				for(Word m : modifier) {
					w.setValue(m.getValue() + "_" + w.getValue());
				}
				objects.add(w);
			}			
		}
		
		//take the modifier as object, if no object is already in the list
		if(objects.size() == 0 && modifier.size() != 0) {
			for(Word w : modifier) {
				objects.add(w);
			}			
		}
		
		//todo: is in front of the object (, and or..) => make new statement
		
		//todo: delete?
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
		if(afterMainVerb.size() != 0) {
			for(Word w : afterMainVerb) {
				predicate += w.getValue().substring(0, 1).toUpperCase() + w.getValue().substring(1);
			}			
		}
		
		
		//arange usabale statements
		UsableStatement respond = new UsableStatement();
		respond.subjects = usableSubjects;		
		respond.predicate = predicate;												 				
		respond.objects = objects;
		respondStatements.add(respond);
		
		return respondStatements;
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
		List<UsableStatement> statement = getUsableStatement(questionWords);
		
		for(UsableStatement uS : statement) {
			if(dataInstance.searchRestrictionExist(uS.subjects.get(0), uS.predicate, uS.objects.get(0)) == true) {
				return "Yes";
			}			
		}	
		return "No";
	}	
	
	//todo: delete
	/*private static String openAnswer(List<Word> questionWords) {		
		for(Word w : questionWords) {
			if(w.getWordTypes().get(0) == WordType.questionWord) {
				List<UsableStatement> uS = new ArrayList<UsableStatement>();
				switch(w.getValue()) {
					case "who":						
						questionWords.remove(w);
						uS = getUsableStatement(questionWords);
						
						return dataInstance.getSubject(uS.get(0).predicate, uS.get(0).objects.get(0).getValue());						
					case "what":
						questionWords.remove(w);
						uS = getUsableStatement(questionWords);
						
						return dataInstance.getObject(uS.get(0).subjects.get(0).getValue(), uS.get(0).predicate);
					default:
						break;
				}
			}
		}
		
		return null;
	}*/
	
	private static SentenceType getSentenceType(List<Word> sentence) {
		//is it a question?
		//closed Question / yes/no question
		if(sentence.get(0).getWordTypes().contains(WordType.verb)) {
			String baseVerb = dataInstance.getBaseOfVerb(sentence.get(0).getValue());
			if(
					sentence.get(0).getWordTypes().contains(WordType.modalVerb) ||			
					sentence.get(0).getValue().equals("be") ||
					baseVerb.equals("be") ||
					sentence.get(0).getValue().equals("do") ||
					baseVerb.equals("do") ||
					sentence.get(0).getValue().equals("have") ||
					baseVerb.equals("have")
					
				) {
				return SentenceType.ClosedQuestion;					
			}
		}		
		//open Question / wh- question
		if(
			sentence.get(0).getWordTypes().contains(WordType.questionWord)
			) {
			return SentenceType.OpenQuestion;
		}		
		
		//todo: anyMatch wäre bessere Lösung?
		//statement sentence
		for(Word w : sentence) {
			if(w.getWordTypes().contains(WordType.interjection)) {				
				return SentenceType.Greeting;
			}
		}
		
		//todo: anyMatch wäre bessere Lösung?
		//statement sentence
		for(Word w : sentence) {
			if(w.getWordTypes().contains(WordType.verb)) {				
				return SentenceType.Statement;
			}
		}		
		
		return null;
	}
	
	private static String generateQuestion(String person) {
		//Ask for a Name
		//todo: Not rly random yet
		if(person == "") {
			return "What is your Name";
		}
		
		//search for name in database
		//dataInstance.getPersonByName(firstname, surname)
		
		
		
		return null;
	}
	
	private static String readSentence(List<Word> sentence) {
		List<UsableStatement> statement = new ArrayList<UsableStatement>();
		
		SentenceType sentenceType = getSentenceType(sentence);
		switch(sentenceType) {
			case Statement:
				statement = getUsableStatement(sentence);
				String respond = "";
				for(UsableStatement uS : statement) {
					for(int sCounter = 0; sCounter < uS.subjects.size(); sCounter++) {					
						for(int oCounter = 0; oCounter < uS.objects.size(); oCounter++) {
							//dataInstance.insertRestriction(uS.subjects.get(sCounter), uS.predicate, uS.objects.get(oCounter));
							respond = respond + uS.subjects.get(sCounter).getValue() + " " + uS.predicate + " " + uS.objects.get(oCounter).getValue() + "\n";
						}			
					}
				}
				
				return respond;
			case OpenQuestion:
				transformWQuestionToSentence(sentence);
				return null;
			case ClosedQuestion:
				statement = getUsableStatement(transformYNQuestionToSentence(sentence));
				
				//todo: restriction with opposite meaning (to say "no") else say "I don't know" 
				for(UsableStatement uS : statement) {
					if(dataInstance.searchRestrictionExist(uS.subjects.get(0), uS.predicate, uS.objects.get(0)) == true) {
						return "Yes";
					}
				}	
				return "I don't know";	
			case Greeting:	
				//todo: delete insert
				if(
						sentence.get(0).getWordTypes().contains(WordType.interjection) ||
						sentence.get(0).getWordTypes().contains(WordType.noun)
						) {					
					dataInstance.insertInstance(sentence.get(0), "greeting");
				}				
				return dataInstance.getRandomIndividual("greeting");
			default:
				return "I don't understand"; 				
		}								
	}
	
	public static void main (String[] args) {
		/*String state = "ok";
		
		SearchWiki a = new SearchWiki();
		try {
			a.searchWiki(state);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.print(e.getMessage());
		} */			
		
		dataInstance.openData("src/projectBot/new.xml", "RDF/XML");
			
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter a sentence:");
		String input = scan.nextLine();		
		
		List<Word> wordList = new ArrayList();
		String[] words = input.split("\\s+");		
		for (int i = 0; i < words.length; i++) {			    
			words[i] = words[i].replaceAll("[^\\w]", "");		    
		    wordList.add(new Word(words[i], dataInstance.getType(words[i]), i));
		}
		
		System.out.print(readSentence(wordList));
		
		dataInstance.closeData();
	}	
}
