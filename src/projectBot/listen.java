package projectBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import data.dataFunctions;

public class listen {
	private static dataFunctions dataInstance = new dataFunctions();	
	private static Boolean learnMode = true;
	
	private static void memoriseStatement(List<Word> statement) {
		for(int counter = 0; counter < statement.size(); counter++) {			
			String word = statement.get(counter).getValue();	
			switch (statement.get(counter).getType()) {
				case auxiliaryVerb:									
					break;
				case verb:
					break;
				case pronoun:																
					break;				
				case noun:
					
					break;
				case properNoun:					
					break;
				case adjective:										
					break;
				default:
					break;
			}			
		}
	}
	
	public static void main(String[] args) {
		dataInstance.openData("src/projectBot/new.xml", "RDF/XML");
		
		//get statement
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter a statement:");
		String input = scan.nextLine();		
		
		List<Word> wordList = new ArrayList();
		String[] words = input.split("\\s+");		
		for (int i = 0; i < words.length; i++) {			    
			words[i] = words[i].replaceAll("[^\\w]", "");		    
		    wordList.add(new Word(words[i], dataInstance.getType(words[i])));
		}
		
		if(learnMode == true) {
			memoriseStatement(wordList);
		}
		else {
			
		}
	}

}
