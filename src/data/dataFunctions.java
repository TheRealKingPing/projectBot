package data;

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
				System.out.print(results.next().get("infinitive").toString());							
				
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
				default: 				
					break;
			}				
		}			
		qe.close();
		return null;
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
	
	public boolean searchRestrictionExist(String subjectName, String propertyName, String objectName) {
		// Create a new query
		String queryString =
				prefixUri +	
				"SELECT ?isRight WHERE { \r\n" + 				
				"  BIND( EXISTS { uri:"+ subjectName + " uri:" + propertyName + " uri:" + objectName + " } as ?isRight )\r\n" + 
				"}";
		
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
		return false;
	}	
}
