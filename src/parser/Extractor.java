package parser;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Blob;

public class Extractor {
	
	private MySQL conn;
	private String[] wordlist;
	
	Extractor()
	{
		this.conn = new MySQL();
	}
	
	public String fetchWikitext(String word)
	{
		String wikitext = "";
		
		Terminal term = new Terminal();
	    ResultSet result = null;
	    
	    // fetches the Wiktionary text for the specified word from the DB
	    try
	    {
		    result = this.conn.executeQuery("SELECT page_id FROM page " +
		    							"WHERE page_title = ?", word);
		    
		    
		    result.next();
		    String pageID = result.getString(1);
		    
		    result = this.conn.executeQuery("SELECT rev_text_id FROM revision " +
		    							"WHERE rev_page = '" + pageID + "'");
		    
		    result.next();
		    String revID = result.getString(1);
		    
		    result = this.conn.executeQuery("SELECT old_text FROM text " +
		    							"WHERE old_id = '" + revID + "'");
		    
		    result.next();
		    Blob text = result.getBlob(1);
		    byte[] bytes = convertBlob(text);
		    wikitext = decodeCharByteArray(bytes, "UTF-8");
	    }
	    catch (SQLException ex) {
	  	    term.println("SQLException: " + ex.getMessage());
	  	    term.println("SQLState: " + ex.getSQLState());
	  	    term.println("VendorError: " + ex.getErrorCode());
	  	    term.println("Word: " + word);
	    }
	    finally
	    {
	    	try
	    	{
		    	if (result != null)
		    	{
		    		result.close();
		    		result = null;
		    	}
	    	}
	    	catch (SQLException ex) { }
	    }
		
		return wikitext;
	}

	// fetches the Wiktionary page titles within the specified range from the DB
	public void loadWordlist (int offset, int limit)
	{
		Terminal term = new Terminal();
	    ResultSet result = null;
	    
	    try
	    {
		    result = this.conn.executeQuery("SELECT page_title FROM page LIMIT " + limit + " OFFSET " + offset);
		    
		    this.wordlist = new String[limit];
		    
		    for (int i = 0; i < limit; i++)
		    {
		    	result.next();
		    	wordlist[i] = result.getString(1);
		    }
	    }
	    catch (SQLException ex) {
	  	    term.println("SQLException: " + ex.getMessage());
	  	    term.println("SQLState: " + ex.getSQLState());
	  	    term.println("VendorError: " + ex.getErrorCode());
	    }
	    finally
	    {
	    	try
	    	{
		    	if (result != null)
		    	{
		    		result.close();
		    		result = null;
		    	}
	    	}
	    	catch (SQLException ex) { }
	    }
	    
	    term.println("Wordlist loaded.");
	}
	
	public String getWordlist (int index)
	{
		if (index >= 0 && index < this.wordlist.length)
			return wordlist[index];
		else
			return null;
	}
	
	public void close()
	{
		this.conn.disconnect();
	}
	
	private static byte[] convertBlob(Blob blob) {
		if(blob==null)return null;
		try {
		    InputStream in = blob.getBinaryStream();
		    int len = (int) blob.length(); //read as long	    
		    long pos = 1; //indexing starts from 1
		    byte[] bytes = blob.getBytes(pos, len); 		    in.close();
		    return bytes;	    
	         } catch (Exception e) {
		        System.out.println(e.getMessage());
		 }
		 return null;
	}
	
	private static String decodeCharByteArray(byte[] inputArray, String charSet) {
	  	Charset theCharset = Charset.forName(charSet);
		CharsetDecoder decoder = theCharset.newDecoder();
		ByteBuffer theBytes = ByteBuffer.wrap(inputArray);
		CharBuffer inputArrayChars = null;
		try {
			inputArrayChars = decoder.decode(theBytes);
		} catch (CharacterCodingException e) {
			System.err.println("Error decoding");
		}
		return inputArrayChars.toString();
	}
}
