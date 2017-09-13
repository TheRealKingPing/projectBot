package projectBot;

public class Word {	
	private String value;
	private WordType type;
	
	public Word(String _value, WordType _type) {				
		value = _value;		
		type = _type;
	}
	
	//set
	public void setValue(String _value) {
		value = _value;
	}
	
	public void setType(WordType _type) {
		type = _type;
	}

	//get
	public String getValue() {
		return value;
	}
	
	public WordType getType() {
		return type;
	}		
}
