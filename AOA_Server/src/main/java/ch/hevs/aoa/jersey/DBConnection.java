package ch.hevs.aoa.jersey;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/*
 * A trial instead of the WebService DBSearch
 * might not be used, keep for later
 */
public class DBConnection {
	static public String dbUrl = "jdbc:mysql://153.109.124.88:3306/";
	static public String dbName = "swicico" ;
	static public String dbDriver = "com.mysql.jdbc.Driver";
	static public String dbUserName = "root"; 
	static public String dbPassword = "pwd4SOFTCUST";
	
	static public String getWeibosFromList(String weibosList) throws ClassNotFoundException, SQLException 
	{
	   	Connection conn = null;
		Statement statement = null;
	   	ResultSet rs = null;

        String result = "" ;

   		// Registering the driver was not necessary when running the current projects
   		// but an exception occured when swicico.jar was used in a server application in TomCat 7.0
   		// Exception: java.sql.SQLException: No suitable driver found for jdbc:mysql
   		// Adding this line did correct the problem
   		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl+dbName,dbUserName,dbPassword);
			
			statement = conn.createStatement();
            rs = statement.executeQuery("SELECT text from weibo where weibo_id in ("+weibosList+")");            
            
            
            while (rs.next())
            	result += rs.getString("text") + "<br>";
		} finally
		{
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
            if (statement != null)
				try {
					statement.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            
			try {
	    	        if (conn != null)
	    	        	conn.close();
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		return result;
	}
}
