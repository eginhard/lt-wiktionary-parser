package parser;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ParserApp {
	
	// Patterns to find different parts of speech (POS)
	public static final Pattern LANG_DELIMITER = Pattern.compile("==\\s?\\{\\{(?=...?v\\}\\})");
	public static final String LANG_LT = "ltv}}";
	public static final Pattern TRANS = Pattern.compile("(?<=\\{\\{t\\+\\|en\\|)[^}]*(?=\\}\\})");
	public static final Pattern TRANS2 = Pattern.compile("(?<=\\{\\{env1\\}\\}\\s?\\[\\[)[^\\]]*(?=\\]\\])");
	
	public static final Pattern POS_DELIMITER = Pattern.compile("(?<=[^=])===\\s?''");
	
	public static final String NOUN = "Daiktavardis";
	public static final Pattern NOUN_INFO = Pattern.compile("\\{\\{ltdkt[^}]*\\}\\}");
	public static final Pattern NOUN_DECL = Pattern.compile("[0-9]l");
	public static final Pattern NOUN_ACC = Pattern.compile("[0-9]k");
	public static final Pattern NOUN_END = Pattern.compile("(?<=[0-9]l-([0-9]k-)?)[a-zė]{1,4}(?=-vard)?");
	
	public static final String VERB = "Veiksmažodis";
	public static final Pattern VERB_INFO = Pattern.compile("\\{\\{ltvks[^}]*\\}\\}");
	
	public static final String ADJ = "Būdvardis";
	public static final String NUM = "Skaitvardis";
	public static final Pattern ADJ_INFO = Pattern.compile("\\{\\{ltbdv[^}]*\\}\\}");
	
	public static final String ADV = "Prieveiksmis";
	public static final Pattern ADV_INFO = Pattern.compile("\\{\\{ltprv[^}]*\\}\\}");
	
	public static final String DAL = "Dalyvis";
	public static final String PADAL = "Padalyvis";
	public static final String PUSDAL = "Pusdalyvis";
	
	public static final String UNKNOWN = "UNKNOWN";
	
	public static void main(String[] args) throws IOException {
		
		// for some statistics
		final long startTime = System.currentTimeMillis();
		int count1 = 0;
		int count2 = 0;
		int countVerb = 0;
		int countVerbE = 0;
		int countAdj = 0;
		int countAdjE = 0;
		int countAdv = 0;
		int countAdvE = 0;
		int countOther = 0;
		int countOtherE = 0;
		
		Terminal term = new Terminal();
		
		// allows processing the (huge) database in smaller chunks
		int limit = term.readInt("LIMIT: ");
		int offset = term.readInt("OFFSET: ");
		
	    Extractor ext = new Extractor();
	    // target files for different POS
	    PrintWriter outNoun = new PrintWriter(new FileWriter("nouns.txt", true));
	    PrintWriter outNounEng = new PrintWriter(new FileWriter("nouns-eng.txt", true));
	    PrintWriter outVerb = new PrintWriter(new FileWriter("verbs.txt", true));
	    PrintWriter outVerbEng = new PrintWriter(new FileWriter("verbs-eng.txt", true));
	    PrintWriter outAdj = new PrintWriter(new FileWriter("adj.txt", true));
	    PrintWriter outAdjEng = new PrintWriter(new FileWriter("adj-eng.txt", true));
	    PrintWriter outAdv = new PrintWriter(new FileWriter("adv.txt", true));
	    PrintWriter outAdvEng = new PrintWriter(new FileWriter("adv-eng.txt", true));
	    PrintWriter outOther = new PrintWriter(new FileWriter("other.txt", true));
	    PrintWriter outOtherEng = new PrintWriter(new FileWriter("other-eng.txt", true));
	    PrintWriter log = new PrintWriter(new FileWriter("log.txt"));
	    PrintWriter check = new PrintWriter(new FileWriter("check.txt", true));

	    ext.loadWordlist(offset, limit);
	    
	    // checks the POS of a word and fetches the according grammatical information
	    for (int i = 0; i < limit; i++)
	    {
	    	//term.println(ext.fetchWikitext(ext.getWordlist(i)));
	    	String word = ext.getWordlist(i);
	    	//term.println(word);
	    	String wikitext = ext.fetchWikitext(word);
	    	word = word.replace("_", " ");
	    	String[] langs = LANG_DELIMITER.split(wikitext);
	    	
	    	for (String lang : langs) {
	    		if (lang.startsWith(LANG_LT)) {
			    	String[] psos = POS_DELIMITER.split(lang);
			    	
			    	for (String pos : psos) {
		    			ArrayList<String> eng = new ArrayList<String>();
			    		Matcher transMatcher = TRANS.matcher(pos);
			    		while (transMatcher.find()) {
			    			String tr = transMatcher.group();
			    			if (!eng.contains(tr)) eng.add(tr);
			    		}
			    		transMatcher = TRANS2.matcher(pos);
			    		while (transMatcher.find()) {
			    			String tr = transMatcher.group();
			    			if (!eng.contains(tr)) eng.add(tr);
			    		}
			    		
			    		if (pos.contains(NOUN)) {
			    			//term.println(word);
			    			String nounInfo = "";
			    			Noun noun = new Noun(word);
			    			Matcher nounMatcher = NOUN_INFO.matcher(pos);
			    			if (nounMatcher.find()) {
			    				nounInfo = pos.substring(nounMatcher.start()+7, nounMatcher.end()-2);
			    				String[] infos = nounInfo.split("\\|");
			    				noun.gender = UNKNOWN;
			    				
			    				for (String info : infos) {
			    					//term.println(info);
			    					if (info.startsWith("forma=")) {
			    						noun.unchangeable = info.contains("f-nekait");
			    						noun.gender = info.contains("f-vyr") ? "m" : (info.contains("f-mot") ? "f" : UNKNOWN);
			    						//term.println(noun.gender);
			    						Matcher declMatcher = NOUN_DECL.matcher(info);
			    						if (declMatcher.find()) noun.declension = Integer.parseInt(info.substring(declMatcher.start(), declMatcher.end()-1));
			    						Matcher accMatcher = NOUN_ACC.matcher(info);
			    						if (accMatcher.find()) noun.accent = Integer.parseInt(info.substring(accMatcher.start(), accMatcher.end()-1));
			    						Matcher endMatcher = NOUN_END.matcher(info);
			    						if (endMatcher.find()) noun.ending = endMatcher.group();
			    					}
			    					
			    					else if (info.startsWith("šakn="))
			    						noun.root = info.substring(info.indexOf("=")+1).trim();
			    					
			    					else if (info.startsWith("šakn2="))
			    						noun.root2 = info.substring(info.indexOf("=")+1).trim();
			    					
			    					else if (info.startsWith("šakn3="))
			    						noun.root3 = info.substring(info.indexOf("=")+1).trim();
			    					
			    					else if (info.startsWith("šakn4="))
			    						noun.root4 = info.substring(info.indexOf("=")+1).trim();
			    					
			    					else if (info.startsWith("skiem="))
			    						noun.syllables = info.substring(info.indexOf("=")+1).trim();
			    					
			    					else if (info.startsWith("vkirt="))
			    						noun.accent = Integer.parseInt(info.split("=")[1].trim().substring(0, 1));
			    					
			    					else if (info.contains("tikr=tikr"))
			    						noun.proper = true;
			    					
			    					else if (info.contains("vnsdgst=vienask"))
			    						noun.number = "sg";
			    					
			    					else if (info.contains("vnsdgst=daugisk"))
			    						noun.number = "pl";
			    					
			    					else if (info.startsWith("gim="))
			    						noun.gender = info.contains("vyr. g.") ? "m" : (info.contains("mot. g.") ? "f" : UNKNOWN);
			    					
			    					//else if (!info.trim().equals(""))
			    						//term.println("ERROR: unknown info " + info + " " + word);
			    				}
			    				
	    						if (noun.gender.equals(UNKNOWN)) log.println("ERROR: Gender unknown (" + word + ")");
			    			}
			    			else {
			    				log.println("ERROR: no noun info available (" + word + ")");
			    				noun.gender = UNKNOWN;
			    			}

			    			if (word.equalsIgnoreCase(stripAccents(noun.root) + noun.ending) || 
			    					noun.number.equals("pl") ||
			    					noun.unchangeable ||
			    					noun.gender.equals(UNKNOWN) ||
			    					noun.root.equals(" ")) {
			    				count1++;
			    				String output = word + "|" +
	    										noun.syllables + "|" +
			    								noun.root + "|" +
			    								noun.root2 + "|" +
			    								noun.root3 + "|" +
			    								noun.root4 + "|" +
			    								noun.ending + "|" +
			    								noun.gender + "|" +
			    								noun.number + "|" +
			    								noun.declension + "|" +
			    								noun.accent + "|" +
			    								noun.unchangeable + "|" +
			    								noun.proper;
				    			
	    						if (eng.size() > 0) {
				    				count2++;
				    				String trans = "";
			    					for (String en : eng)
			    						trans = trans + (trans.length() == 0 ? "" : "#") + en;
			    					output = output + "|" + trans;
			    					outNounEng.println(output);
			    				}
	    						else
	    							outNoun.println(output);
			    			}
			    			else {
			    				//term.println(i + " " + noun.root + noun.ending + " " + word);
			    				check.println(noun.root + noun.ending + " " + word);
			    			}
			    		}
			    		else if (pos.contains(VERB)) {
			    			//term.println(word);
			    			String verbInfo = "";
			    			Verb verb = new Verb(word);
			    			String infinitive = "";
			    			Matcher verbMatcher = VERB_INFO.matcher(pos);
			    			if (verbMatcher.find()) {
			    				verbInfo = pos.substring(verbMatcher.start()+7, verbMatcher.end()-2);
			    				String[] infos = verbInfo.split("\\|");
			    				
			    				for (String info : infos) {
				    				if (info.startsWith("bšakn="))
			    						verb.root = info.substring(info.indexOf("=")+1).trim();
				    				
				    				else if (info.startsWith("eslšakn="))
			    						verb.rootPresent = info.substring(info.indexOf("=")+1).trim();
				    				
				    				else if (info.startsWith("btklšakn="))
			    						verb.rootPast = info.substring(info.indexOf("=")+1).trim();
				    				
				    				else if (info.startsWith("pform="))
			    						infinitive = stripBrackets(info.substring(info.indexOf("=")+1).trim());
				    				
				    				else if (info.startsWith("pform1="))
			    						verb.present = stripBrackets(info.substring(info.indexOf("=")+1).trim());
				    				
				    				else if (info.startsWith("pform2="))
			    						verb.past = stripBrackets(info.substring(info.indexOf("=")+1).trim());
				    				
				    				else if (info.startsWith("skiem="))
			    						verb.syllables = info.substring(info.indexOf("=")+1).trim();
				    			}
			    				
			    			}
			    			else
			    				log.println("ERROR: no verb info available (" + word + ")");
			    			
			    			if (word.equalsIgnoreCase(stripAccents(infinitive))) {
			    				countVerb++;
			    				String output = word + "|" + 
	    								verb.syllables  + "|" + 
	    								verb.present  + "|" + 
	    								verb.past  + "|" + 
	    								verb.root  + "|" + 
	    								verb.rootPresent  + "|" + 
	    								verb.rootPast;
			    				
			    				if (eng.size() > 0) {
				    				countVerbE++;
				    				String trans = "";
			    					for (String en : eng)
			    						trans = trans + (trans.length() == 0 ? "" : "#") + en;
			    					output = output + "|" + trans;
			    					outVerbEng.println(output);
			    				}
	    						else
	    							outVerb.println(output);
			    			}
			    			
			    		}
			    		else if (pos.contains(ADJ) || pos.contains(NUM)) {
			    			//term.println(word);
			    			String adjInfo = "";
			    			Adj adj = new Adj(word);
			    			if (pos.contains(NUM)) adj.numeral = true;
			    			String male = "";
			    			Matcher adjMatcher = ADJ_INFO.matcher(pos);
			    			if (adjMatcher.find()) {
			    				adjInfo = pos.substring(adjMatcher.start()+7, adjMatcher.end()-2);
			    				String[] infos = adjInfo.split("\\|");
			    				
			    				for (String info : infos) {
				    				if (info.startsWith("šakn="))
			    						adj.root = info.substring(info.indexOf("=")+1).trim();
				    				
				    				else if (info.startsWith("pform="))
			    						male = stripBrackets(info.substring(info.indexOf("=")+1).trim());
				    				
				    				else if (info.startsWith("pformm="))
			    						adj.female = stripBrackets(info.substring(info.indexOf("=")+1).trim());
				    				
				    				else if (info.startsWith("skiem="))
			    						adj.syllables = info.substring(info.indexOf("=")+1).trim();
				    			}
			    				
			    			}
			    			else
			    				log.println("ERROR: no adjective info available (" + word + ")");
			    			
			    			if (word.equalsIgnoreCase(stripAccents(male)) || male.equals("")) {
			    				countAdj++;
			    				String output = word + "|" + 
	    								adj.syllables  + "|" + 
	    								adj.female  + "|" + 
	    								adj.numeral  + "|" + 
	    								adj.root;
			    				
			    				if (eng.size() > 0) {
				    				countAdjE++;
				    				String trans = "";
			    					for (String en : eng)
			    						trans = trans + (trans.length() == 0 ? "" : "#") + en;
			    					output = output + "|" + trans;
			    					outAdjEng.println(output);
			    				}
	    						else
	    							outAdj.println(output);
			    			}
			    			
			    		}
			    		else if (pos.contains(ADV)) {
			    			//term.println(word);
			    			String advInfo = "";
			    			Adv adv = new Adv(word);
			    			String base = "";
			    			Matcher advMatcher = ADV_INFO.matcher(pos);
			    			if (advMatcher.find()) {
			    				advInfo = pos.substring(advMatcher.start()+7, advMatcher.end()-2);
			    				String[] infos = advInfo.split("\\|");
			    				
			    				for (String info : infos) {
				    				if (info.startsWith("nl="))
			    						base = info.substring(info.indexOf("=")+1).trim();
				    				
				    				else if (info.startsWith("l1="))
			    						adv.l1 = stripBrackets(info.substring(info.indexOf("=")+1).trim());
				    				
				    				else if (info.startsWith("l2="))
			    						adv.l2 = stripBrackets(info.substring(info.indexOf("=")+1).trim());
				    				
				    				else if (info.startsWith("l3="))
			    						adv.l3 = stripBrackets(info.substring(info.indexOf("=")+1).trim());
				    				
				    				else if (info.startsWith("skiem="))
			    						adv.syllables = info.substring(info.indexOf("=")+1).trim();
				    			}
			    				
			    			}
			    			else
			    				log.println("ERROR: no adverb info available (" + word + ")");
			    			
			    			if (word.equalsIgnoreCase(stripAccents(base))) {
			    				countAdv++;
			    				String output = word + "|" + 
	    								adv.syllables  + "|" + 
	    								adv.l1  + "|" + 
	    								adv.l2  + "|" + 
	    								adv.l3;
			    				
			    				if (eng.size() > 0) {
				    				countAdvE++;
				    				String trans = "";
			    					for (String en : eng)
			    						trans = trans + (trans.length() == 0 ? "" : "#") + en;
			    					output = output + "|" + trans;
			    					outAdvEng.println(output);
			    				}
	    						else
	    							outAdv.println(output);
			    			}
			    			
			    		}			    		
			    		
			    		else if (!pos.contains(DAL) && !pos.contains(PADAL) && !pos.contains(PUSDAL) && (pos.contains("''===") || pos.contains("'' ==="))) {
			    			//term.println(pos);
			    			String thisPos = pos.substring(0, pos.indexOf("''"));
		    				countOther++;
		    				String output = word + "|" + thisPos;
		    				
		    				if (eng.size() > 0) {
			    				countOtherE++;
			    				String trans = "";
		    					for (String en : eng)
		    						trans = trans + (trans.length() == 0 ? "" : "#") + en;
		    					output = output + "|" + trans;
		    					outOtherEng.println(output);
		    				}
    						else
    							outOther.println(output);

			    		}
			    	}
	    		}
	    	}
			term.println((i+1) + "/" + limit);
	    }
	    
	    ext.close();
	    outNoun.close();
	    outNounEng.close();
	    outVerb.close();
	    outVerbEng.close();
	    outAdj.close();
	    outAdjEng.close();
	    outAdv.close();
	    outAdvEng.close();
	    outOther.close();
	    outOtherEng.close();
	    check.close();
	    
	    final long endTime = System.currentTimeMillis();
	    int min = (int) ((endTime - startTime)/1000)/60;
	    int sec = (int) ((endTime - startTime)/1000)%60;
	    term.println("Noun count: " + count1 + "/" + count2);
	    term.println("Verb count: " + countVerb + "/" + countVerbE);
	    term.println("Adjective count: " + countAdj + "/" + countAdjE);
	    term.println("Adverb count: " + countAdv + "/" + countAdvE);
	    term.println("Other count: " + countOther + "/" + countOtherE);
	    term.println("Total execution time: " + min + "m " + sec + "s");
	    log.println("Noun count: " + count1 + "/" + count2);
	    log.println("Verb count: " + countVerb + "/" + countVerbE);
	    log.println("Adjective count: " + countAdj + "/" + countAdjE);
	    log.println("Adverb count: " + countAdv + "/" + countAdvE);
	    log.println("Other count: " + countOther + "/" + countOtherE);
	    log.println("Total execution time: " + min + "m " + sec + "s");
	    log.close();
	}

	private static String stripBrackets(String str) {
		String result = str;
		result = result.replace("[", "");
		result = result.replace("]", "");
		return result;
	}

	private static String stripAccents(String str) {
		String result = str;
		result = result.replace('á', 'a');
		result = result.replace('à', 'a');
		result = result.replace('ã', 'a');
		result = result.replace("ė́", "ė");
		result = result.replace('ẽ', 'e');
		result = result.replace('ì', 'i');
		result = result.replace('ĩ', 'i');
		result = result.replace('õ', 'o');
		result = result.replace('ó', 'o');
		result = result.replace('ú', 'u');
		result = result.replace('ù', 'u');
		result = result.replace("ū̃", "ū");
		result = result.replace("ū́", "ū");
		result = result.replace('ý', 'y');
		return result;
	}
	
} 