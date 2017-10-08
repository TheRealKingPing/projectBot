package projectBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import data.dataFunctions;

public class listen {
	private static dataFunctions dataInstance = new dataFunctions();	
	private static Boolean learnMode = true;
	
	private static void memoriseSentence(List<Word> sentence) {
		UsableStatement test = answer.getUsableStatement(sentence);
		
		System.out.print(test.subject + " " + test.predicate + " " + test.object.getValue());
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
			memoriseSentence(wordList);
		}
		else {
			
		}
	}

}
