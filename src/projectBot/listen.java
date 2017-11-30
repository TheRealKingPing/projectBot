package projectBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import data.dataFunctions;

public class listen {
	private static dataFunctions dataInstance = new dataFunctions();	
	private static Boolean learnMode = true;
	
	private static void memoriseSentence(List<Word> sentence) {
		List<UsableStatement> test = answer.getUsableStatement(sentence);
		
		for(UsableStatement uS : test) {
			for(int sCounter = 0; sCounter < uS.subjects.size(); sCounter++) {					
				for(int oCounter = 0; oCounter < uS.objects.size(); oCounter++) {
					System.out.print(uS.subjects.get(sCounter).getValue() + " " + uS.predicate + " " + uS.objects.get(oCounter).getValue() + "\n");				
				}			
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
			//remove to lower case!
		    wordList.add(new Word(words[i], dataInstance.getType(words[i].toLowerCase()), i));
		}
		
		if(learnMode == true) {
			memoriseSentence(wordList);
		}
		else {
			
		}
		
	}

}
