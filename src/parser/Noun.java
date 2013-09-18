package parser;

public class Noun {
	public String word;
	public boolean unchangeable = false;
	public String gender = " ";
	public int declension = 0;
	public int accent = 0;
	public String root = " ";
	public String ending = " ";
	public String root2 = " ";
	public String root3 = " ";
	public String root4 = " ";
	public String number = " ";
	public String syllables = " ";
	public boolean proper = false;
	
	Noun(String word) {
		this.word = word;
	}
}
