package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.*;

import org.apache.http.protocol.ResponseDate;
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
import org.apache.jena.sparql.lang.UpdateParserFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.GraphStore;
import org.apache.jena.update.GraphStoreFactory;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.iterator.ExtendedIterator;

import projectBot.UsableStatement;
import projectBot.Word;
import projectBot.WordType;

public class dataFunctions {	
	private static String uri = "http://www.semanticweb.org/z003da4t/ontologies/2017/7/untitled-ontology-3#";
	private static OntModel m = null;
	private String fileSource = "";
	private String type = "RDF/XML";
	private String prefixUri = "prefix uri: <" + uri + "> ";
	private String prefixRdf = "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
	private String prefixRdfs = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>";
	private String prefixOwl = "prefix owl:<http://www.w3.org/2002/07/owl#>";
	private String prefixFoaf = "prefix foaf:<http://xmlns.com/foaf/0.1/>";	
	
	public void openData(String _fileSource, String _type) {
		fileSource = _fileSource;
		type = _type;
		
		m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		m.read(fileSource, type);
		m.setStrictMode(false);
	}
	
	public void closeData() {					
		try {	
			FileWriter fw = null;
		    try {
		    	fw = new FileWriter(fileSource);		
			    m.write( fw, type );
		    }
		    finally {
		    	fw.close();	
		    }
		}
		catch(IOException e) {
			System.out.print(e);
		}
		m.close();	
	}
	
	public String getInfinitive(Word word) {
		if (word.getWordTypes().equals(WordType.auxiliaryVerb) || word.getWordTypes().equals(WordType.verb)) {
			// Create a new query
			String queryString =
					prefixUri + 			
					"SELECT ?infinitive \n" +
					"WHERE { uri:" + word.getValue().toLowerCase() + " uri:hasInfinitive ?infinitive }";
			
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
	
	public List<WordType> getType(String word) {
		List<WordType> respondTypes = new ArrayList<WordType>();
		
		//number
		//todo: to a regex
		if (word.matches("^[0-9]+$") == true) {					
			respondTypes.add(WordType.numeral);
			return respondTypes;
		}				
		// Create a new query
		String queryString =
				prefixUri +	prefixRdf +	prefixRdfs +			
				"SELECT ?type WHERE { \r\n" + 				
				" uri:" + word.toLowerCase() + " rdf:type ?type . \r\n" + 
				" ?type rdfs:subClassOf uri:Word . \r\n" +
				" FILTER( ?type != uri:Word ) \r\n" + 
				"}";
		
		Query query = QueryFactory.create(queryString);
		 
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();
				
		// return query results 
		while(results.hasNext()) {		
			String type = results.next().getResource("type").getURI().toString().replaceAll(uri, "");
			switch(type) {
				case "verb":					
					respondTypes.add(WordType.verb);					
					break;
				case "modalVerb":
					respondTypes.add(WordType.modalVerb);
					break;
				case "auxiliaryVerb":					
					respondTypes.add(WordType.auxiliaryVerb);
					break;
				case "noun":					
					respondTypes.add(WordType.noun);
					break;
				case "pronoun":					
					respondTypes.add(WordType.pronoun);
					break;
				case "adjective":					
					respondTypes.add(WordType.adjective);
					break;
				case "questionWord":					
					respondTypes.add(WordType.questionWord);
					break;
				case "properNoun":					
					respondTypes.add(WordType.properNoun);
					break;
				case "article":					
					respondTypes.add(WordType.article);				
					break;
				case "adverb":
					respondTypes.add(WordType.adverb);
					break;
				case "coordinatingConjunction":					
					respondTypes.add(WordType.coordinatingConjunction);
					break;
				case "preposition":					
					respondTypes.add(WordType.preposition);
					break;
				case "interjection":
					respondTypes.add(WordType.interjection);	
				default:		
					System.out.print("'" + word + "' not found in the database\n--\n");
					break;
			}				
		}		
		qe.close();		
												
		//get base verb
		String baseVerb = getBaseOfVerb(word);	    
		if(baseVerb != null) {
			System.out.print("The base verb of '" + word + "' is '" + baseVerb + "'\n");	
		}
		
		//todo: dk bout that		
		//search again with adjective
		/*String adjective = getAdjectiveOfAdverb(word);
		if(adjective != null) {
			dictonaryType = getWordTypeViaDictionary(adjective);											
			if (dictonaryType != null) {
				List<WordType> respond = new ArrayList<WordType>();
				respond.add(WordType.adverb);
				return respond;
			}						
		}*/
			
		//get singular noun
		String singularNoun = getSingularOfPlural(word);	
		//ToDo: delete System.out.print
		if(singularNoun != null) {
			System.out.print("The singular noun of '" + word + "' is '" + singularNoun + "'\n");	
		}		

		//search in dictionary
		List<WordType> dictionaryList = new ArrayList<WordType>();
		String[] thoseWords = {word, baseVerb, singularNoun};
		for(int c = 0; c < thoseWords.length; c++) {
			if(thoseWords[c] != null) {
				dictionaryList = getWordTypeViaDictionary(thoseWords[c]);
				if(dictionaryList != null) {
					for(WordType type : dictionaryList) {
						if(!respondTypes.contains(type)) {
							respondTypes.add(type);
						}
					}
				}				
			}			
		}
					
		return respondTypes;
	}	
	
	public void insertRestriction(Word subjectName, String propertyName, Word objectName) {						
		String updateString =
				prefixUri +	prefixRdf + prefixOwl +
				"INSERT DATA { \r\n" +
				"uri:" + propertyName + " rdf:type owl:ObjectProperty . \r\n" +
				"uri:" + subjectName.getValue().toLowerCase() + " uri:" + propertyName + " uri:" + objectName.getValue().toLowerCase() + " . \r\n" +
				"uri:" + subjectName.getValue().toLowerCase() + " rdf:type uri:" + subjectName.getWordTypes().get(0) + " . \r\n" +
				"uri:" + objectName.getValue().toLowerCase() + " rdf:type uri:" + objectName.getWordTypes().get(0) + " . \r\n" +
				"}";
		
		if(propertyName.equals("is" )) {
			//is object a class?
			String queryString =
					prefixUri +	prefixRdfs +			
					"SELECT ?object WHERE { \r\n" + 								
					"uri:" + objectName.getValue().toLowerCase() + " rdfs:subClassOf ?object . \r\n" + 					
					"}";		
			Query query = QueryFactory.create(queryString);
			
			//todo: nach namen suchen anstatt subclassof (btw subclassof ist evt falsch
			
			// Execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.create(query, m);
			ResultSet results = qe.execSelect();
				
			if(results.hasNext() == true) {
				updateString =
						prefixUri +	prefixRdf +		
						"INSERT DATA { \r\n" +
						"uri:" + subjectName.getValue() + " rdf:type uri:" + objectName.getValue() + " . \r\n" +						
						"}";
			}
			
			qe.close();							
			
		}				
		
		GraphStore graphStore = GraphStoreFactory.create(m);
		UpdateRequest request = UpdateFactory.create(updateString);			
		
		//Execute the update
		UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);		
		proc.execute();					
	}
	
	public boolean searchRestrictionExist(Word subjectName, String propertyName, Word objectName) {
		// Create a new query
		String queryString =
				prefixUri +	prefixRdf +
				" SELECT ?isRight WHERE { \r\n"; 				
						
		if(objectName.getWordTypes() != null && objectName.getWordTypes().equals(WordType.noun) && propertyName.equals("is")) {
			queryString = queryString + "  BIND( EXISTS { uri:" + subjectName.getValue().toLowerCase() + " rdf:type uri:" + objectName.getValue().toLowerCase() + " } as ?isRight ) \r\n ";			
		}
		else {
			queryString = queryString + "  BIND( EXISTS { uri:"+ subjectName.getValue().toLowerCase() + " uri:" + propertyName + " uri:" + objectName.getValue().toLowerCase() + " } as ?isRight )\r\n";
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
	
	//todo: getSubject, getPredicate und getObject zusammen tun?
	public String getSubject(String predicate, String object) {
		String queryString =
				prefixUri +	prefixRdf +				
				"SELECT ?subject WHERE { \r\n" + 								
				"?subject uri:" + predicate.toLowerCase() + " uri:" +	object.toLowerCase() +			
				"}";		
		Query query = QueryFactory.create(queryString);
				 
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();
		
		// return query results 
		while(results.hasNext()) {							
			String subject = results.next().get("subject").toString().replaceAll(uri, "");
			qe.close();
			return subject;
		}		
		qe.close();				
		return null;
	}	
	
	public List<String> getObjects(String subject, String predicate) {
		String queryString =
				prefixUri +	prefixRdf +				
				"SELECT ?object WHERE { \r\n" + 								
				"uri:" + subject.toLowerCase() + " uri:" + predicate.toLowerCase() + " ?object " +				
				"}";		
		Query query = QueryFactory.create(queryString);
				 
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();
		
		List<String> obejcts = new ArrayList<String>();
		// return query results 
		while(results.hasNext()) {							
			obejcts.add(results.next().get("object").toString().replaceAll(uri, ""));					
		}		
		qe.close();				
		return obejcts;
	}
	
	public List<String> getSuperClass(String value) {
		String queryString =
				prefixUri +	prefixRdf + prefixRdfs +				
				"SELECT ?superClass WHERE { \r\n" + 	
				"?superClass rdfs:subClassOf uri:knowledge . \r\n" +		
				"{ uri:" + value + " rdfs:subClassOf ?superClass} \r\n" +
				"UNION { uri:" + value + " rdf:type ?superClass } \r\n" +
				"}";		
		Query query = QueryFactory.create(queryString);
				 
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();
		
		List<String> respond = new ArrayList<String>();
		// return query results 
		while(results.hasNext()) {							
			String superClass = results.next().get("superClass").toString().replaceAll(uri, "");
			if(!superClass.equals("knowledge")) {
				respond.add(superClass);
			}									
		}		
		qe.close();				
		return respond;
	}
	
	public int createNewPersonID (String _firstname, String _surname) {		
		String firstname = _firstname.toLowerCase();
		String surname = _surname.toLowerCase();
		
		int newID = 0;
		//number
		String queryString =
				prefixUri +	prefixRdf +				
				"SELECT ?person WHERE { \r\n" + 				
				" ?person rdf:type uri:person \r\n" + 
				"}";		
		Query query = QueryFactory.create(queryString);
				 
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();
		
		// return query results 
		while(results.hasNext()) {			
			String resultPerson = results.next().get("person").toString().replaceAll(uri, "");
			if(resultPerson.contains("person")) {
				int personID = Integer.parseInt(resultPerson.replaceAll("person", ""));
				if(newID < personID) {
					newID = personID;
				}			
			}					
		}	
		newID++;
		qe.close();									
		
		GraphStore graphStore = GraphStoreFactory.create(m);
		String updateString =
				prefixUri +	prefixRdf +	prefixOwl +			
				"INSERT DATA { \r\n" +
				"uri:" + firstname.toLowerCase() + " rdf:type owl:NamedIndividual . \r\n" +
				"uri:" + firstname.toLowerCase() + " rdf:type uri:properNoun . \r\n" +
				"uri:" + surname.toLowerCase() + " rdf:type owl:NamedIndividual . \r\n" +
				"uri:" + surname.toLowerCase() + " rdf:type uri:properNoun . \r\n" +
				"uri:person" + newID + " rdf:type owl:NamedIndividual . \r\n" +
				"uri:person" + newID + " rdf:type uri:person . \r\n" +
				"uri:person" + newID + " uri:hasFirstname uri:" + firstname.toLowerCase() + " . \r\n" +
				"uri:person" + newID + " uri:hasSurname uri:" + surname.toLowerCase() + " . \r\n" +				
				"}";
		UpdateRequest request = UpdateFactory.create(updateString);			
		
		//Execute the update
		UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);		
		proc.execute();
			
		//todo: delete me
		System.out.print("created a person; " + firstname.toLowerCase() + " " + surname.toLowerCase() + " => Person" + newID + "\n");
		
		return newID; 
	}
	
	public String getPersonByName (String firstname, String surname) {
		String queryString =
				prefixUri +	prefixRdf +				
				"SELECT ?person WHERE { \r\n" + 				
				" ?person rdf:type uri:person . \r\n" + 
				" ?person uri:hasFirstname uri:" + firstname.toLowerCase() + " . \r\n" +
				" ?person uri:hasSurname uri:" + surname.toLowerCase() + " . \r\n" +
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
	
	public String getSingularOfPlural(String word) {
		//-es
		if (word.length() > 3 && word.substring(word.length() - 3, word.length()).equals("ses")) {
			return word.substring(0, word.length() - 3) + "s";
		}
		if (word.length() > 4 && word.substring(word.length() - 4, word.length()).equals("shes")) {
			return word.substring(0, word.length() - 4) + "sh";
		}
		if (word.length() > 4 && word.substring(word.length() - 4, word.length()).equals("ches")) {
			return word.substring(0, word.length() - 4) + "ch";
		}
		if (word.length() > 3 && word.substring(word.length() - 3, word.length()).equals("xes")) {
			return word.substring(0, word.length() - 3) + "x";
		}
		if (word.length() > 3 && word.substring(word.length() - 3, word.length()).equals("zes")) {
			return word.substring(0, word.length() - 3) + "z";
		}	
		
		//ies
		if (word.length() > 3 && word.substring(word.length() - 3, word.length()).equals("ies")) {
			return word.substring(0, word.length() - 3) + "y";
		}	
				
		//-s
		if (word.length() > 1 && word.substring(word.length() - 1, word.length()).equals("s")) {
			return word.substring(0, word.length() - 1);
		}				
		
		//irregular nouns
		String iNoun = getSingularNounInIrregularNouns(word);
		if(iNoun != null) {
			return iNoun;
		}			
		else {
			return null;
		}
	}
	
	public String getAdjectiveOfAdverb(String word) {		
		//-ably -ibly -ly
		if (word.length() > 4 && word.substring(word.length() - 4, word.length()).equals("ably")) {
			return word.substring(0, word.length() - 4) + "able";
		}
		if (word.length() > 4 && word.substring(word.length() - 4, word.length()).equals("ibly")) {
			return word.substring(0, word.length() - 4) + "ible";
		}
		
		//-ally. Exception: public -> publicly
		if (word.length() > 6 && word.substring(word.length() - 6, word.length()).equals("ically")) {
			return word.substring(0, word.length() - 6) + "ic";
		}
		
		//-ily
		if (word.length() > 3 && word.substring(word.length() - 3, word.length()).equals("ily")) {
			return word.substring(0, word.length() - 3) + "y";
		}	
		
		//-ly
		if (word.length() > 2 && word.substring(word.length() - 2, word.length()).equals("ly")) {
			return word.substring(0, word.length() - 2);
		}	
		
		return null;
	}
	
	public String getBaseOfVerb(String word) {
		//irregular verbs
		String iVerb = getBaseVerbInIrregularVerbs(word);
		if(iVerb != null) {
			return iVerb;
		}			
		
		//3rd person singular present tense -es -s		
		if (word.length() > 2 && word.substring(word.length() - 2, word.length()).equals("es")) {
			return word.substring(0, word.length() - 2);
		}
		if (word.length() > 1 && word.substring(word.length() - 1, word.length()).equals("s")) {
			return word.substring(0, word.length() - 1);
		}		
		
		//past tense & past participle -ied -ed		
		if (word.length() > 3 && word.substring(word.length() - 3, word.length()).equals("ied")) {
			return word.substring(0, word.length() - 3) + "y";
		}		
		if (word.length() > 2 && word.substring(word.length() - 2, word.length()).equals("ed")) {
			return word.substring(0, word.length() - 2);
		}
		
		//present participle -ing
		if (word.length() > 3 && word.substring(word.length() - 3, word.length()).equals("ing")) {
			return word.substring(0, word.length() - 3);
		}		
		
		else {
			return null;
		}
	}
	
	public String getSingularNounInIrregularNouns(String sWord) {
		String path = "src/data/irregular_nouns.txt";
		File file = new File(path);
		if (!file.canRead() || !file.isFile()) 
		    System.exit(0); 
		
		    BufferedReader in = null; 
		try { 
		    in = Files.newBufferedReader(Paths.get(path)); 
		    String zeile = null;		    
		    while ((zeile = in.readLine()) != null) { 		    	
		    	
		    	if(zeile.equals(" " + sWord)) {
		    		int firstSpaceIndex = zeile.indexOf(" ");
		    		return zeile.substring(0, firstSpaceIndex);
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
		return null;
	}
	
	public String getBaseVerbInIrregularVerbs(String sWord) {
		String path = "src/data/irregular_verbs.txt";
		File file = new File(path);
		if (!file.canRead() || !file.isFile()) 
		    System.exit(0); 
		
		    BufferedReader in = null; 
		try { 
		    in = Files.newBufferedReader(Paths.get(path)); 
		    String zeile = null;
		    
		    while ((zeile = in.readLine()) != null) { 		    			    	
		    	if(zeile.contains(" " + sWord)) {
		    		int firstSpaceIndex = zeile.indexOf(" ");
		    		return zeile.substring(0, firstSpaceIndex);
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
		return null;
	}
		
	public List<WordType> getWordTypeViaDictionary(String word) {						
		List<WordType> respondTypes = new ArrayList<WordType>();
		
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
		    	
		    	//ignore () part
		    	if(zeile.length() != 0 && zeile.contains("  (") && zeile.contains(") ")) {		    		
		    		if(zeile.indexOf(" ") == zeile.indexOf("  (")) {
		    			int firstBracketIndex = zeile.indexOf("  (");
			    		int lastBracketIndex = zeile.indexOf(") ");
			    		zeile = zeile.substring(0, firstBracketIndex) + " " + zeile.substring(lastBracketIndex + 1);
		    		}		    		
		    	}
		    	
		        if(zeile.length() != 0 && zeile.contains("  ")) {		 
		        	String zeilenWord = "";
		        	if (zeile.indexOf("  ") != -1) {
		        		zeilenWord= zeile.substring(0, zeile.indexOf("  "));
		        	}
		        	
		        	String nextWord = zeile.replace(zeilenWord + "  ", "").substring(0, zeile.replace(zeilenWord + "  ", "").indexOf(" "));
		        	if(nextWord.equals("artc.")) {
		        		System.out.print(zeile + "\n\n");
		        	}
		        	if (
		        			word.trim().equals(zeilenWord.toLowerCase()) == true ||
		        			zeilenWord.toLowerCase().equals(word + "e") ||
		        			zeilenWord.toLowerCase().equals(word + "1")
		        			
		        			) {
		        		do {	
		        			if(zeile.contains("�")) {
								zeile = zeile.substring(zeile.indexOf("�") + 1);
								if(zeile.contains(" ")) {
									nextWord = zeile.substring(0, zeile.indexOf(" "));								
								}
								else {
									nextWord = zeile;
								}							
							}
							if(nextWord.contains("�")) {
			        			nextWord = nextWord.replace("�", "");
			        		}
				        	switch(nextWord) {		        	
				        		case "int.":
				        			respondTypes.add(WordType.interjection);
				        			break;
				        		case "n.":
			        				respondTypes.add(WordType.noun);	
			        				break;
			        			case "v.":
			        				respondTypes.add(WordType.verb);
			        				break;
			        			case "adj.":
			        				respondTypes.add(WordType.adjective);
			        				break;
			        			case "adv.":
			        				respondTypes.add(WordType.adverb);
			        				break;
			        				//adverb		        				
			        			case "abbr.":
			        				//Abbreviation
			        				break;
			        			case "conj.":
			        				respondTypes.add(WordType.conjunction);
			        				break;
			        			case "pron.":
			        				respondTypes.add(WordType.pronoun);
			        				break;
			        			case "prep.":
			        				respondTypes.add(WordType.preposition);
			        				break;
			        			case "symb.":
			        				//symbol		
			        				break;
			        			case "past":
			        				//verb
			        				//check past and past part.!
			        				break;
			        			case "interrog.":
			        				//idk
			        				respondTypes.add(WordType.pronoun);
			        				break;
			        			case "poss.":
			        				//possessive pronoun
			        				respondTypes.add(WordType.prossessivePronoun);
			        				break;
			        			case "article":
			        				respondTypes.add(WordType.article);
			        				break;
			        			case "n.pl.":
			        				//noun plural!
			        				respondTypes.add(WordType.noun);
			        				break;
								default:									
									break;								
							}										        	
		        		} while (zeile.indexOf("�") != -1);			        		
		        		if (zeilenWord.toLowerCase().equals(word + "e")) {
		        			if(!respondTypes.contains(WordType.verb) || !respondTypes.contains(WordType.auxiliaryVerb)) {
		        				respondTypes = new ArrayList<WordType>();
		        			}
		        			else {
		        				break;
		        			}
		        		}
		        		else {
		        			break;
		        		}		        		
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
		
		if(respondTypes.size() != 0) {
			return respondTypes;
		}
		
		//mehrzahl?
		if(word.substring(word.length() - 1, word.length()).equals("s")) {
    		word = word.substring(0, word.length() - 1);
    		return getWordTypeViaDictionary(word);
    	}		
		
		return null;
	}
	
	//insert a class with the specific superclass (if <null> => Knowledge)
	public void insertClass(String className, String subClassOf) {
		String subClassOfSuperClass = null;
		
		//superclass exist? else create it with superclass knowledge
		String queryString =
				prefixUri +	prefixRdfs +
				"SELECT ?superclass WHERE { \r\n" + 								
				"uri:" + subClassOf + " rdfs:subClassOf ?superclass . \r\n" +
				"}";		
		Query query = QueryFactory.create(queryString);
				 
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();
		
		// return query results 
		List<String> allIndividuals = new ArrayList<String>();
		while(results.hasNext()) {										
			subClassOfSuperClass = results.next().get("superclass").toString().replaceAll(uri, "");				
		}		
		qe.close();				
		
		if(className != null) {
			if(subClassOf == null) {
				subClassOf = "knowledge";
			}		
			
			GraphStore graphStore = GraphStoreFactory.create(m);
			String updateString =
					prefixUri +	prefixRdfs +			
					"INSERT DATA { \r\n";
										
			if(subClassOfSuperClass == null || subClassOfSuperClass == "knowledge") {
				updateString = updateString + "uri:" + subClassOf.toLowerCase() + " rdfs:subClassOf uri:knowledge . \r\n";
			}
			
			updateString = updateString + "uri:" + className.toLowerCase() + " rdfs:subClassOf uri:" + subClassOf.toLowerCase() + " . \r\n }";
			UpdateRequest request = UpdateFactory.create(updateString);			
			
			//Execute the update
			UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);		
			proc.execute();		
			
			//todo: delete me
			System.out.print("\"" + className.toLowerCase() + "\" with super class \"" + subClassOf.toLowerCase() + "\"\n");
		}				
	}
	
	public void insertInstance(Word instanceName, String subClassOf) {
		if(instanceName != null && subClassOf != null) {						
			GraphStore graphStore = GraphStoreFactory.create(m);
			String updateString =
					prefixUri + prefixRdf + prefixOwl +		
					"INSERT DATA { \r\n" +		
					"uri:" + instanceName.getValue().toLowerCase() + " rdf:type owl:NamedIndividual . \r\n" +
					"uri:" + instanceName.getValue().toLowerCase() + " rdf:type uri:" + instanceName.getWordTypes().get(0) + " . \r\n" +	
					"uri:" + instanceName.getValue().toLowerCase() + " rdf:type uri:" + subClassOf.toLowerCase() + " . \r\n" +				
					"}";					
			
			UpdateRequest request = UpdateFactory.create(updateString);			
			
			//Execute the update
			UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);		
			proc.execute();		
			
			//todo: delete me
			System.out.print("Individual: \"" + instanceName.getValue().toLowerCase() + "\" with super class \"" + subClassOf.toLowerCase() + "\"\n");
		}	
	}
	
	public String getRandomIndividual(String className) {
		String queryString =
				prefixUri +	prefixRdf + prefixOwl +				
				"SELECT ?individual WHERE { \r\n" + 								
				"?individual rdf:type uri:" + className + " . \r\n" +
				"}";		
		Query query = QueryFactory.create(queryString);
				 
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();
		
		// return query results 
		List<String> allIndividuals = new ArrayList<String>();
		while(results.hasNext()) {										
			allIndividuals.add(results.next().get("individual").toString().replaceAll(uri, ""));				
		}		
		qe.close();				
		
		if(!allIndividuals.isEmpty()) {
			//Return random Individual
			Random randomGenerator = new Random();
			return allIndividuals.get(randomGenerator.nextInt(allIndividuals.size()));
		}
		return null;
	}
}

