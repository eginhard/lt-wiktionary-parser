package parser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Terminal {
	private BufferedReader reader;
	private InputStreamReader streamReader;
	
	public Terminal() {
		this.streamReader = new InputStreamReader(System.in);
		this.reader = new BufferedReader(streamReader);
	}
	
	public int readInt(){
		String line = null;
		int val = 0;
		while(true){
			try {
				line = reader.readLine(); // can throw an IOException.
				val = Integer.parseInt(line); // can throw a NumberFormatException.
				break; // break out of enclosing while 
			}
			catch (IOException e) {
				System.err.println("Unexpected IO ERROR: " + e);
				System.exit(1); 
			}
			catch (NumberFormatException e) {
				System.err.println("Not a valid integer: " + line);
				System.err.println("Enter an integer: ");
			}
		} // end of while
		return val;
	}
	
	public int readPositiveInt(){
		int val = readInt();
		while (val < 1) {
			System.out.println("Not a positive integer: " + val);
			System.out.println("Enter a positive integer:");
			val = readInt();
		} // end of while
		return val;
	}
	
	public String readString(String prompt) {
		print(prompt);
		return readString();
	}
	
	public String readString() {
		String str = "";
		try {
			str = reader.readLine();
		}
		catch (IOException error) {
			print("Something bad happened, exiting...");
			System.exit(1);
		}
		return str;
	}
	
	public int readInt(String prompt) {
		print(prompt);
		return readInt();
	}
	
	public void println(String message) {
		System.out.println(message);
	}
	
	public void println() {
		System.out.print("\n");
	}
	
	public void print(String message) {
		System.out.print(message);
	}
}