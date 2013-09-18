package parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import java.util.Properties;
//import java.util.Collections;

public class MySQL {
	
	/* ##### MySQL Settings #####
     * Set your MySQL settings in 'database.properties' **/
	
	private Connection conn;

	MySQL()
	{
		Properties settings = readSettings();
		this.connect(settings);
	}
	
	private Properties readSettings()
	{
		Terminal term = new Terminal();
		Properties settings = new Properties();
        FileInputStream in = null;

        try {
            in = new FileInputStream("database.properties");
            settings.load(in);

        } catch (FileNotFoundException ex) {

            term.println("File database.properties not found.");

        } catch (IOException ex) {

        	term.println("Error opening database.properties.");

        } finally {
            
            try {
                 if (in != null) {
                     in.close();
                 }
            } catch (IOException ex) {
            	term.println("Error closing database.properties.");
            }
        }
		
        return settings;
	}

	private void connect(Properties settings)
	{
		Terminal term = new Terminal();
		
		String url = settings.getProperty("db.url");
	    String db = settings.getProperty("db.name");
	    String user = settings.getProperty("db.user");
	    String password = settings.getProperty("db.password");
	    
	    String driver = "com.mysql.jdbc.Driver";
	    
	    try {
	      Class.forName(driver).newInstance();
	      
	      try {
	    	  this.conn = DriverManager.getConnection(url + db + "?useUnicode=true&characterEncoding=UTF-8", user, password);
		      term.println("Connected to database '" + db + "'.\n");
	      }
	      catch (SQLException ex) {
	    	    term.println("SQLException: " + ex.getMessage());
	    	    term.println("SQLState: " + ex.getSQLState());
	    	    term.println("VendorError: " + ex.getErrorCode());
	      }
	    } 
	    catch (Exception e) {
	      e.printStackTrace();
	    }
	}
	
	public ResultSet executeQuery(String query)
	{
		Terminal term = new Terminal();
		Statement st = null;
	    ResultSet rs = null;
	    
	    if (this.conn != null)
	    {
	    	try
	    	{
	    		st = this.conn.createStatement();
	    		rs = st.executeQuery(query);
	    	}
		    catch (SQLException ex) {
		    	term.println("SQLException: " + ex.getMessage());
		    	term.println("SQLState: " + ex.getSQLState());
		    	term.println("VendorError: " + ex.getErrorCode());
		    }
	    }
	    else
	    	System.out.println("Connection is null");
	    
	    return rs;
	}
	
	public ResultSet executeQuery(String query, String parameter)
	{
		Terminal term = new Terminal();
		PreparedStatement st = null;
	    ResultSet rs = null;
	    
	    if (this.conn != null)
	    {
	    	try
	    	{
	    		st = this.conn.prepareStatement(query);
	    		st.setString(1, parameter);
	    		rs = st.executeQuery();
	    	}
		    catch (SQLException ex) {
		    	term.println("SQLException: " + ex.getMessage());
		    	term.println("SQLState: " + ex.getSQLState());
		    	term.println("VendorError: " + ex.getErrorCode());
		    }
	    }
	    else
	    	System.out.println("Connection is null");
	    
	    return rs;
	}
	
	public void disconnect()
	{
		Terminal term = new Terminal();
		
		if (this.conn != null) {
            try {
                this.conn.close();
            } catch (SQLException sqlEx) { }

            this.conn = null;
  	      term.println("Disconnected from database.");
        }
	}
}
