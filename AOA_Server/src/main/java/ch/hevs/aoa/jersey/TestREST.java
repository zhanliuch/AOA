package ch.hevs.aoa.jersey;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/*
 * For the tests, this web services runs in Eclipse, on the Tomcat 7 Server
 * Call example from the browser: 
 * 	http://localhost:8081/SwicicoServer/TestREST/JavaCodeGeeks?value=blabla235678
 *  Here parameter="JavaCodeGeeks", value="blabla235678"
 *  
 * This is a test class, to test the server functionalities
 *  
 * See Search.java for the real implementation and more details 
 */
@Path("/TestREST")
public class TestREST {

	@GET
	@Path("/{parameter}")
	/*
	 * This is a test, where the path is not fixed: any path THAT IS NOT CORRESPONDING TO ANOTHER DEFINED PATH IN THE CLASS
	 * will be catched here
	 * For instance, a @Path("/test1") was defined here under, so a call to
	 * 	http://localhost:8081/SwicicoServer/TestREST/test1?value=blabla235678 goes to that specific metho
	 * but any other call goes here
	 * 		
	 */
	public Response responseParameter( @PathParam("parameter") String parameter,
			@DefaultValue("Nothing to say") @QueryParam("value") String value) {

//		Swicico swicico = new Swicico() ;
		//swicico.testLogger();
		
		// FC: both tests work now
//		swicico.testRetrieveGloss();
//		String res = testRetrieveGloss() ; // this webService instantiate a swicico.DBConnection
				
//		swicico.testOntoFuse();
		
		String output = "Hello from WS: " + parameter + " : " + value;
//		 output += "  " + res ;
		
		return Response.status(200).entity(output).build();
	}
	
	/*
	 * http://localhost:8081/SwicicoServer/TestREST/testString?value=aString
	 */
	@GET
	@Path("/testString")
	public Response responseTestString(@QueryParam("value") String value) {

		String output = "Hello from WS/testString : " + value;
		
		return Response.status(200).entity(output).build();
	}


}