package ch.hevs.aoa.jersey;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.Model;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.hevs.ontoFuse.OntoFuse;

public class testStardog {
	
	public static void main(String[] args) {
		testStardog ts = new testStardog() ;
		
		ts.testStardog();
		
	}

	public static void testStardog() 
	{
		Model resultModel = null ;
		
		// Wikidata end-point
		
		// Read trial on the Sparql end-point -> does work
		try (OntoFuse ontoFuse = new OntoFuse("http://www.datasemlab.ch:5820/AOA/query"))
		{
			String keyword = "rosac";
			ByteArrayOutputStream out = new ByteArrayOutputStream() ;

			
			ontoFuse.setSPARQLEndPointUsernameAndPassword("anonymous", "anonymous");
			ontoFuse.initialize();
	
	    	String query = "PREFIX schema: <http://schema.org/> "
	    	+ " PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
			+ "SELECT DISTINCT ?s ?label ?score ?def "
			+ " WHERE { "
			+ " ?s a skos:Concept ; "
			+ " skos:definition ?def ;"
			+ " skos:altLabel ?label . "
			+ "(?label ?score) <tag:stardog:api:property:textMatch> ('"
			+ keyword
			+ "*' "
			+ "15).}" ;
	    	
	    	System.out.println(query);
	    	
	    	//ontoFuse.SPARQLSelectToSystemOut(OntoFuse.repoSrc.SPARQL_ENDPOINT, query) ;	
	    	ontoFuse.runSPARQLSelectToJSONOutputStream(OntoFuse.repoSrc.SPARQL_ENDPOINT, query, out);
	    	
	    	String results = new String(out.toString().getBytes(), "UTF-8");
	    	System.out.println(results) ;
	    	JSONObject resultsAsJson = new JSONObject(results);
	    	JSONArray bindings = resultsAsJson.getJSONObject("results").getJSONArray("bindings");
	    	for(int i=0; i<bindings.length(); i++){
	    		JSONObject myObject = bindings.getJSONObject(i);

	    		System.out.println(myObject.getJSONObject("s").getString("value"));
	    		System.out.println(myObject.getJSONObject("def").getString("value"));
	    		System.out.println(myObject.getJSONObject("label").getString("value"));
	    	}
			
			//System.out.println("-----:"+searchObjects.size());





	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
    	// Write test - SPARQL update
		try (OntoFuse ontoFuse = new OntoFuse("http://88.99.66.215:5820/AOA/update"))
		{
			ontoFuse.setSPARQLEndPointUsernameAndPassword("aoa_datamanager", "aoamanager");
			ontoFuse.initialize();
			
			//algo for update
			//1. get json result from interface
			JSONObject resultsAsJson = new JSONObject(results);
			
			boolean hasWikidata = false;
			boolean hasNewUserConcept = false;
			boolean hasUserConcept = false;
			
			for(int i=0; i<5; i++){
				JSONObject concepts = resultsAsJson.getJSONObject(i);
				.....
				for (all keywords){
					if (xxx= "wikidata"){
						String query = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"  
			    		+ "	INSERT DATA {"
		 				+ "<http://www.wikidata.org/entity/Q831530> a skos:Concept ; "
			    		+ "skos:altLabel "rosacée"@fr, "couperose"@fr ; "
		 				+ "skos:definition "maladie cutanéee qui se manifeste par des rougeurs chroniques au niveau du nez, des joues, etc."@fr ; "
		 				+ "dc:source <http://www.wikidata.org> . "
			    		+ "}" ;
			    		hasWikidata = true;
			    		idWikidata.add = xxx;
					}else if (xxx= "useConcept"){
							hasUserConcept = true;
							idUserConcept.add = xxx;
							}else if (xxx= "newUseConcept"){
								hasNewUserConcept = true;
								labelNewUserConcept.add = "xxx";
							}
				}
				//handle all to one concept
				//1. only user concept
				if (hasUserConcept && !hasNewUserConcept){
				//put all userconcept togeter
					for (int i = 0; i < idUserConcept.size(); i++) {
						//update and delete others
						//System.out.println(idUserConcept.get(i));
					}
					if (hasWikidata){
						add exactMatch
					}
				}
				//2. has user concept and new user concept
				if (hasUserConcept && hasNewUserConcept){
					for (int i = 0; i < idUserConcept.size(); i++) {
							//update and delete others
							//add all new user concept to altlabel
					}
					if (hasWikidata){
						add exactMatch
					}
				}
				
				//3. has new user concept and not user concept
				if (!hasUserConcept && hasNewUserConcept){
					for (int i = 0; i < idNewUserConcept.size(); i++) {
							//check exising or not
							// create new one
							//add others new user concept to altlabel
					}
					if (hasWikidata){
						add exactMatch
					}
				}
				
			}
			
	
			// INSERT
//	    	String query = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"  
//	    		+ "	INSERT DATA {"
 * 				+ "<http://www.wikidata.org/entity/Q831530> a skos:Concept ; "
//	    		+ "skos:altLabel "rosacée"@fr, "couperose"@fr ; "
 * skos:definition "maladie cutanéee qui se manifeste par des rougeurs chroniques au niveau du nez, des joues, etc."@fr
 * dc:source <http://www.wikidata.org>
//	    		+ "}" ;
	    	
	    	// si deux URL (un wikidata, un userConcept)
	    	 // "<http://www.wikidata.org/entity/Q831530> skos:exactMatch <http://datasemlab.ch/vpx/userConcept/yeux> ."
	    	
	    	// DELETE
	    	String query = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"  
		    		+ "	DELETE DATA {"
		    		+ "GRAPH <http://datasemlab.ch/aoa/testOntoFuse> {" 
		    		+ "<http://datasemlab.ch/aoa/testConcept/conceptFromOntoFuse> skos:altLabel \"un autre label\"@fr . "
		    		+ "}}" ;
	    	
	    	System.out.println (query) ;
	    	
	    	ontoFuse.runSPARQLUpdate(OntoFuse.repoSrc.SPARQL_ENDPOINT, query);

		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
	
}
