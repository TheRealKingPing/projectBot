package projectBot;

import java.util.ArrayList;
import java.util.List;

public class Skill {	
	private Word action;	
	
	public Skill(Word verb) {		
		if(verb.getWordTypes().contains(WordType.verb)) {
			List<WordType> wordTypes = new ArrayList<WordType>();
			wordTypes.add(WordType.verb);
			action.setWordTypes(wordTypes);
			
			action.setValue(verb.getValue());
		}
		else {
			System.out.print("not possible");
		}		
	}	
}
