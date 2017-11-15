package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.util.iterator.ExtendedIterator;

import projectBot.Word;
import projectBot.WordType;

public class dataFunctions {	
	private static String uri = "http://www.semanticweb.org/z003da4t/ontologies/2017/7/untitled-ontology-3#";
	private static OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);	
	private String prefixUri = "prefix uri: <" + uri + "> ";
	private String prefixRdf = "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
	private String prefixRdfs = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>";
	private String prefixOwl = "prefix owl:<http://www.w3.org/2002/07/owl#>";
	
	//todo: not only infinitive => every verb
	//get subject name by infinitive todo: delete
	public String getPropertyName(String infinitive) {
		switch(infinitive) {
			case "be":
				return "is";
			case "have":
				return "has";
			default:
				return null;
		}		
	}
	
	//get subject name by pronoun tdodo: delete
	public String getSubjectName(String pronoun) {
		switch(pronoun) {
			case "you":
				return "UserMe";				
			case "your":
				return "UserMe";
			default:
				return null;
		}
	}	
	
	public void openData(String fileSource, String type) {
		m.read(fileSource, type);
		m.setStrictMode(false);		
	}
	
	public void closeData() {
		m.close();
	}
	
	public String getInfinitive(Word word) {
		if (word.getType().equals(WordType.auxiliaryVerb) || word.getType().equals(WordType.verb)) {
			// Create a new query
			String queryString =
					prefixUri + 			
					"SELECT ?infinitive \n" +
					"WHERE { uri:" + word.getValue() + " uri:hasInfinitive ?infinitive }";
			
			Query query = QueryFactory.create(queryString);
			 
			// Execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.create(query, m);
			ResultSet results = qe.execSelect();
			
			// return query results 
			while(results.hasNext()) {
				String infinitive = results.next().get("infinitive").toString();
				qe.close();
				return infinitive;
			}								 		
			
			qe.close();
			return null;
		}
		return null;
	}
	
	public WordType getType(String word) {
		WordType type = null;
		// Create a new query
		String queryString =
				prefixUri +	prefixRdf +	prefixRdfs +			
				"SELECT ?type WHERE { \r\n" + 				
				" uri:" + word + " rdf:type ?type . \r\n" + 
				" ?type rdfs:subClassOf uri:Word . \r\n" +
				" FILTER( ?type != uri:Word ) \r\n" + 
				"}";
		
		Query query = QueryFactory.create(queryString);
		 
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();
				
		// return query results 
		while(results.hasNext()) {					
			switch(results.next().getResource("type").getURI().toString().replaceAll(uri, "")) {
				case "WordVerb":
					qe.close();
					return WordType.verb;					
				case "AuxiliaryVerb":
					qe.close();
					return WordType.auxiliaryVerb;					
				case "WordNoun":
					qe.close();
					return WordType.noun;					
				case "WordPronoun":
					qe.close();
					return WordType.pronoun;					
				case "WordAdjective":
					qe.close();
					return WordType.adjective;		
				case "QuestionWord":
					qe.close();
					return WordType.questionWord;
				case "ProperNoun":
					qe.close();
					return WordType.properNoun;
				case "Article":
					qe.close();
					return WordType.article;
				case "CoordinatingConjunction":
					qe.close();
					return WordType.coordinatingConjunction;
				default:					
					System.out.print("'" + word + "' type not found\n");
					break;
			}				
		}		
		qe.close();
		WordType dictonaryType = getWordTypeViaDictionary(word);
		if (dictonaryType != null) {
			return dictonaryType;
		}
		else {
			System.out.print("'" + word + "' not found\n");
			return null;
		}		
	}
	
	//Bind pronoun and noun todo: delete
	public String bindPronounAndNoun(String userName, String nounName) {						
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
					if(scNounName.equals(nounName)) {
						return scNounName;						
					}
				}
			}
		}												
		return null;
	}
	
	public boolean searchRestrictionExist(String subjectName, String propertyName, Word objectName) {
		// Create a new query
		String queryString =
				prefixUri +	prefixRdf +
				" SELECT ?isRight WHERE { \r\n"; 				
						
		if(objectName.getType() != null && objectName.getType().equals(WordType.noun) && propertyName.equals("is")) {
			queryString = queryString + "  BIND( EXISTS { uri:" + subjectName + " rdf:type uri:" + objectName.getValue() + " } as ?isRight ) \r\n ";			
		}
		else {
			queryString = queryString + "  BIND( EXISTS { uri:"+ subjectName + " uri:" + propertyName + " uri:" + objectName.getValue() + " } as ?isRight )\r\n";
		}		
		queryString = queryString + "}";
		
		Query query = QueryFactory.create(queryString);
		 
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();
		
		// return query results 
		while(results.hasNext()) {		
			Object n = results.next().get("isRight");
			LiteralImpl li = (LiteralImpl)n;
			boolean val = li.getBoolean();
			qe.close();
			return val;			
		}		
		qe.close();
		return false;
	}	
	
	public String getPersonByName (String firstname, String surname) {
		String queryString =
				prefixUri +	prefixRdf +				
				"SELECT ?person WHERE { \r\n" + 				
				" ?person rdf:type uri:person . \r\n" + 
				" ?person uri:hasFirstname uri:" + firstname + " . \r\n" +
				" ?person uri:hasSurname uri:" + surname + " . \r\n" +
				"}";		
		Query query = QueryFactory.create(queryString);
				 
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();
		
		// return query results 
		while(results.hasNext()) {							
			String person = results.next().get("person").toString().replaceAll(uri, "");
			qe.close();
			return person;
		}		
		qe.close();				
		return null;
	}
	
	public WordType getWordTypeViaDictionary(String word) {
		WordType returnType = null;
		
		File file = new File("src/data/Oxford_English_Dictionary.txt");
		if (!file.canRead() || !file.isFile()) 
		    System.exit(0); 
		
		    BufferedReader in = null; 
		try { 
		    in = Files.newBufferedReader(Paths.get("src/data/Oxford_English_Dictionary.txt")); 
		    String zeile = null;
		    int counter = 0;
		    while ((zeile = in.readLine()) != null) { 
		    	counter++;
		        //System.out.println("Gelesene Zeile: " + zeile);
		    	//System.out.print(zeile.length() + "\n");
		        if(zeile.length() != 0 && zeile.contains("  ")) {		 
		        	String zeilenWord = "";
		        	if (zeile.indexOf("  ") != -1) {
		        		zeilenWord= zeile.substring(0, zeile.indexOf("  "));
		        	}
		        	//idk what that means, but i will delete it
		        	//int countWordTypes
		        	/*if(zeile.contains("�")) {		        		
		        		System.out.print("oh wow\n");
		        	}*/
		        	zeile = zeile.replace("�", "");
		        	
		        	String nextWord = zeile.replace(zeilenWord + "  ", "").substring(0, zeile.replace(zeilenWord + "  ", "").indexOf(" "));
		        	if(nextWord.equals("artc.")) {
		        		System.out.print(zeile + "\n\n");
		        	}		        			        	
	        		//String test = zeile.substring(zeilenWord.length() + 2, zeile.length() - 1);			        				        	
					if (word.equals(zeilenWord.toLowerCase()) == true) {						
						System.out.print("line: " + counter + " | das wort '" + zeilenWord + "' gibt es!\n");
						System.out.print(zeile + "\n");
						//String test = zeile.replace(zeilenWord + "  ", "");														
						
						
						System.out.print("Wortart: " + nextWord + "\n");
			        	switch(nextWord) {		        	
		        			case "n.":
		        				returnType = WordType.noun;
								break;
		        			case "v.":
		        				returnType = WordType.verb;
		        				break;
		        			case "adj.":
		        				returnType = WordType.adjective;
		        				break;
		        			case "adv.":
		        				returnType = WordType.adverb;
		        				//adverb
		        				break;
		        			case "abbr.":
		        				//Abbreviation
		        				break;
		        			case "symb.":
		        				//symbol
		        			case "past":
		        				//verb
		        				//check past and past part.!
		        				break;
							default:
								System.out.print("L: " + counter + " " + zeilenWord + ": " + nextWord + ": " + zeile + "\n");
								break;
						}
						break;
					}
		        			        	
		        }
		    } 
		} catch (IOException e) { 
		    e.printStackTrace(); 
		} finally { 
		    if (in != null) 
		        try { 
		        	in.close();
		        } catch (IOException e) { 
		        	System.out.print(e.getMessage());
		        } 		    
		    
		}
		return returnType;
	}
}

