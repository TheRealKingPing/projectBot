package projectBot;

import java.util.List;
import java.util.Optional;

public class Word {	
	private String value;
	private List<WordType> types;
	private int index;
	
	public Word(String _value, List<WordType> _types, int _index) {				
		value = _value.toLowerCase();		
		types = _types;
		index = _index;
	}
	
	//set
	public void setValue(String _value) {
		value = _value;
	}
	
	public void setWordTypes(List<WordType> _types) {
		types = _types;
	}
	
	public void setIndex(int _index) {
		index = _index;
	}

	//get
	public String getValue() {
		return value;
	}
	
	public List<WordType> getWordTypes() {
		return types;
	}
	
	public int getIndex() {
		return index;
	}
}
