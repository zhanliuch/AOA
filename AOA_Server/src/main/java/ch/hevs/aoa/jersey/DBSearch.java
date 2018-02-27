package ch.hevs.aoa.jersey;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/DBSearch")
public class DBSearch {
	public String dbUrl = "jdbc:mysql://153.109.124.88:3306/";
	public String dbName = "swicico" ;
	public String dbDriver = "com.mysql.jdbc.Driver";
	public String dbUserName = "root"; 
	public String dbPassword = "pwd4SOFTCUST";
	
	/*
	 * From a comma separated list of weibo id, get all texts returned in a single string
	 * Each weibo is separated by a <BR> html tag
	 * Example call: http://localhost:8081/SwicicoServer/DBSearch/weibosFromList?weibosList=3629713752949137,3787717748445959,3704485891485291,3699341946965552,3677033039580951
	 * 
	 */
	@GET
	@Path("/weibosFromList")
	@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8") // MediaType.APPLICATION_JSON
	public Response getWeibosFromList(@QueryParam("weibosList") String weibosList) 
	{
		
	   	Connection conn = null;
		Statement statement = null;
	   	ResultSet rs = null;

        if (weibosList == null) {
            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter 'weibosList'").build();
        }
        
   		// Registering the driver was not necessary when running the current projects
   		// but an exception occured when swicico.jar was used in a server application in TomCat 7.0
   		// Exception: java.sql.SQLException: No suitable driver found for jdbc:mysql
   		// Adding this line did correct the problem
   		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl+dbName,dbUserName,dbPassword);
			
			statement = conn.createStatement();
			// the weibosList also defines the ordering -> ORDER BY FIELD
            rs = statement.executeQuery("SELECT text from weibo where weibo_id in ("+weibosList+") ORDER BY FIELD(weibo_id,"+weibosList+")");            
            
            String result = "" ;
            
            while (rs.next())
            	result += cleanWeiboMessage(rs.getString("text")) + "<br>";
	        
			return Response.status(200).entity(result).build();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("ClassNotFoundException Exception for jdbc.Driver: " + e.getMessage()).build();		
		} catch (SQLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("SQL Exception: " + e.getMessage()).build();		
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

	}
	
	@GET
	@Path("/saveSearchHistory")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response saveSearchHistory(@QueryParam("synsetURI") String synsetURI,
			@QueryParam("synsetLabel") String synsetLabel) 
	{
	   	Connection conn = null;
		//Statement statement = null;
	   	//ResultSet rs = null;
	    PreparedStatement preparedStmt = null ;
		
		// parameters check
        if (synsetURI == null) 
            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter 'synsetURI'").build();

        if (synsetLabel == null) 
            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter 'synsetLabel'").build();

   		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl+dbName,dbUserName,dbPassword);
			
    		String query = "INSERT INTO search_history(url, label, search_time) values(?, ?, ?)";
   			preparedStmt = conn.prepareStatement(query);
    		
   			preparedStmt.setString(1, synsetURI);
   			preparedStmt.setString(2, synsetLabel);
   			String timeStamp = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
   			preparedStmt.setString(3, timeStamp);
   			
   			if (preparedStmt.executeUpdate() != 1)
   				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("SQL Exception while saving the history: " + preparedStmt.toString()).build();

			// return Response.status(200).entity(result).build();
	        return Response.status(Status.OK).entity("search saved").build();
			
		} catch (ClassNotFoundException e) {
   			System.out.println("Exceptoin: " + e.getMessage() ) ;
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("ClassNotFoundException Exception for jdbc.Driver: " + e.getMessage()).build();		
		} catch (SQLException e) {
   			System.out.println("Exceptoin: " + e.getMessage() ) ;
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("SQL Exception: " + e.getMessage()).build();		
		} finally
		{
			if (preparedStmt != null)
				try {
					preparedStmt.close();
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
	}
	/**
	 * Original method from Zhan, copied here from Swicico.java
	 * To remove the location information from a weibo
	 * 	'瑞士�?镇和悠闲地天鹅 我在:http://t.cn/zQGupnE'
	 * @param weiboText
	 * @return
	 */
	public String cleanWeiboMessage(String weiboText)
	{
        int index = weiboText.indexOf("http://");   
        if(index > 0)
        	weiboText = weiboText.substring(0, index);

        weiboText = weiboText.replace("我在这里:","");
        weiboText = weiboText.replace("我在:","");
        
        return weiboText ;
	}
}