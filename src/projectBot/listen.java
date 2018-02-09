package projectBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.dataFunctions;

public class listen {
	private static dataFunctions dataInstance = new dataFunctions();	
	private static Boolean learnMode = true;	
	
	private static void memoriseSentence(String sentences) {				
		
		List<String> sentenceList = new ArrayList<String>(Arrays.asList(sentences.split("[\\.\\!\\?]")));
		
		for(String sentence : sentenceList) {
			//check for brackets
			Pattern p = Pattern.compile("\\w*\\s\\(.*\\)");
			Matcher m = p.matcher(sentence);
			if(m.find()) {
				System.out.print(m.group(0));
			}
			
			
			List<Word> wordList = new ArrayList();
			String[] words = sentence.split("\\s+");		
			for (int i = 0; i < words.length; i++) {			    
				words[i] = words[i].replaceAll("[^\\w]", "");		    
				//remove to lower case!
			    wordList.add(new Word(words[i], dataInstance.getType(words[i].toLowerCase()), i));
			}
			
			List<UsableStatement> usableStatements = answer.getUsableStatement(wordList);
			
			for(UsableStatement uS : usableStatements) {
				for(int sCounter = 0; sCounter < uS.subjects.size(); sCounter++) {					
					for(int oCounter = 0; oCounter < uS.objects.size(); oCounter++) {
						System.out.print(uS.subjects.get(sCounter).getValue() + " " + uS.predicate + " " + uS.objects.get(oCounter).getValue() + "\n");				
					}			
				}
			}	
			Scanner scan = new Scanner(System.in);
			System.out.println("Save? Yes/No");
			String input = scan.nextLine();	
			
			if (input.toLowerCase().equals("yes")) {
				for(UsableStatement uS : usableStatements) {
					for(int sCounter = 0; sCounter < uS.subjects.size(); sCounter++) {					
						for(int oCounter = 0; oCounter < uS.objects.size(); oCounter++) {
							dataInstance.insertRestriction(uS.subjects.get(sCounter), uS.predicate, uS.objects.get(oCounter));
							System.out.print(uS.subjects.get(sCounter).getValue() + " " + uS.predicate + " " + uS.objects.get(oCounter).getValue() + " - inserted\n");
						}			
					}
				}
				dataInstance.closeData();
			}		
		}
		
		
		
	}
	
	public static void main(String[] args) {
		dataInstance.openData("src/projectBot/new.xml", "RDF/XML");
		
		//get statement
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter a statement:");
		String input = scan.nextLine();		
		
		
		
		if(learnMode == true) {
			memoriseSentence(input);
		}
		else {
			
		}		
	}

}
