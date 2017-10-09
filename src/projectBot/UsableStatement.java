package projectBot;

import java.util.ArrayList;
import java.util.List;

public class UsableStatement {
	
	public UsableStatement () {
		subjects = new ArrayList<String>();
		objects = new ArrayList<Word>();
	}
	public List<String> subjects;
	public String predicate;
	public List<Word> objects;
}
