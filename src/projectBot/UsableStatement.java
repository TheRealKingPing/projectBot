package projectBot;

import java.util.ArrayList;
import java.util.List;

public class UsableStatement {
	
	public UsableStatement () {
		subjects = new ArrayList<Word>();
		objects = new ArrayList<Word>();
	}
	public List<Word> subjects;
	public String predicate;
	public List<Word> objects;
}
