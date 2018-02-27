package ch.hevs.aoa.jersey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.hevs.ontoFuse.OntoFuse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonldjava.utils.Obj;

public class WikidataSearch {

	public String WikidataAPI(String keyword)
	{
		Collection<JSONObject> items = new ArrayList<JSONObject>();
		String searchedTerm = keyword ;


		// get result from our triplestore
		
		try (OntoFuse ontoFuse = new OntoFuse("http://www.datasemlab.ch:5820/AOA/query?reasoning=true"))
		{
			
			//String keyword = "rosac";
			ByteArrayOutputStream out = new ByteArrayOutputStream() ;

			
			ontoFuse.setSPARQLEndPointUsernameAndPassword("anonymous", "anonymous");
			ontoFuse.initialize();

			/*
			String query = "PREFIX schema: <http://schema.org/> "
	    	+ " PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
			+ "SELECT DISTINCT ?s ?label ?score ?def "
			+ " WHERE { "
			+ " ?s a skos:Concept ; "
			+ " skos:definition ?def ;"
			+ " skos:altLabel ?label . "
			+ "(?label ?score) <tag:stardog:api:property:textMatch> ('"
			+ searchedTerm
			+ "*' "
			+ "15).}" ;
			*/
	    	
			String query = "PREFIX dcterm:<http://purl.org/dc/terms/> "
			+ " SELECT ?uc ?wdc (CONCAT(?ucLabels, ';', ?wdcLabels) as ?labels) (CONCAT(?wdcDefs, ';', ?ucDefs) as ?defs) "
			+ " { "	
			+ " SELECT DISTINCT ?uc ?wdc (GROUP_CONCAT(DISTINCT ?ucLabel; separator=';') as ?ucLabels) (GROUP_CONCAT(DISTINCT ?wdcLabel; separator=';') as ?wdcLabels) "
			+ " (GROUP_CONCAT(DISTINCT ?wdcDef; separator=';') as ?wdcDefs) (GROUP_CONCAT(DISTINCT ?ucDef; separator=';') as ?ucDefs) "
			+ " WHERE { "
			+ " { "
			+ " ?uc a skos:Concept ; "
			+ " dcterm:source <http://datasemlab.ch>; "
			+ " skos:altLabel ?ucLabelSearch ; "
			+ " skos:altLabel ?ucLabel . "
			+ " OPTIONAL{ ?uc skos:definition ?ucDef } "
			+ " (?ucLabelSearch ?score) <tag:stardog:api:property:textMatch> ('"
			+ searchedTerm
			+ "*' "
			+ "). " 
			+ " OPTIONAL{ "
			+ " ?uc skos:exactMatch ?wdc . "
			+ " ?wdc dcterm:source <http://www.wikidata.org> ; "
			+ " skos:altLabel ?wdcLabel "
			+ " } "
			+ " OPTIONAL{ ?uc skos:definition ?wdcDef } "
			+ " } UNION { "
			+ " ?wdc a skos:Concept ;  "
			+ " dcterm:source <http://www.wikidata.org> ; "
			+ " skos:altLabel ?wdcLabelSearch ; "
			+ " skos:altLabel ?wdcLabel . "
			+ " OPTIONAL{ ?wdc skos:definition ?wdcDef . } "
			+ " (?wdcLabelSearch ?score) <tag:stardog:api:property:textMatch> ('"
			+ searchedTerm
			+ "*' "
			+ "). " 
			+ " ?wdc skos:exactMatch ?uc . "
			+ " ?uc a skos:Concept ; "
			+ " dcterm:source <http://datasemlab.ch>. "
			+ " OPTIONAL {?uc skos:altLabel ?ucLabel } "
			+ " OPTIONAL {?uc skos:definition ?ucDef } "
			+ " } "
			+ " } GROUP BY ?uc ?wdc "
			+ " } ";
			
			
	    	System.out.println("QUERYYYY:"+query);
	    	
	    	//ontoFuse.SPARQLSelectToSystemOut(OntoFuse.repoSrc.SPARQL_ENDPOINT, query) ;	
	    	ontoFuse.runSPARQLSelectToJSONOutputStream(OntoFuse.repoSrc.SPARQL_ENDPOINT, query, out);
	    	
	    	String results = new String(out.toString().getBytes(), "UTF-8");
	    	System.out.println("PMO:"+results) ;
	    	JSONObject resultsAsJson = new JSONObject(results);
	    	JSONArray bindings = resultsAsJson.getJSONObject("results").getJSONArray("bindings");
	    	//TODO: need to concat all synonyms for one concept
	    	for(int i=0; i<bindings.length(); i++){
	    		JSONObject myObject = bindings.getJSONObject(i);
	    	

	    		//System.out.println("11111111111:"+myObject.getJSONObject("uc").getString("value"));
	    		//System.out.println("22222222222:"+myObject.getJSONObject("labels").getString("value"));
	    		//System.out.println("33333333333:"+myObject.getJSONObject("defs").getString("value"));
	    		//System.out.println("idididididid:"+myObject.getJSONObject("uc").getString("value").substring(46));

				//ArrayList<String> altlabs = new ArrayList<String>();
	    		JSONObject innerObject = new JSONObject();
	    		
	    		String altLab = "";
	    		String preLab = "";
	    		
				innerObject.put("id", myObject.getJSONObject("uc").getString("value").substring(46));
	    		//innerObject.put("id", "12345678");
				innerObject.put("concepturi",myObject.getJSONObject("uc").getString("value"));
				
				altLab =  myObject.getJSONObject("labels").getString("value");
				preLab = altLab.split(";")[0];
				//System.out.println("preLab1: "+preLab);
				if (preLab.isEmpty()){
					preLab = altLab.split(";")[1];
				}
				//System.out.println("preLab2: "+preLab);
				innerObject.put("label",preLab);
				if(myObject.getJSONObject("defs").getString("value").equals(";")){
					innerObject.put("description", "");
				}else{
					innerObject.put("description", myObject.getJSONObject("defs").getString("value"));
				}
				//altlabs.add(myObject.getJSONObject("label").getString("value"));
				ArrayList altlabs = new ArrayList(Arrays.asList(altLab.split(";")));
				altlabs.removeAll(Collections.singleton(null));
				altlabs.removeAll(Collections.singleton(""));				
				/*
				for(int i1=0;i1<altlabs.size();i1++)
				{
				    System.out.println(" -->"+altlabs.get(i1));
				}
				*/
				innerObject.put("aliases", altlabs);
				innerObject.put("source", "http://datasemlab.ch");
				items.add(innerObject);
	    	}
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//To get the entity in French
		//https://www.wikidata.org/w/api.php?action=wbsearchentities&search=yeux&language=fr&uselang=fr&formatversion=2
				
		//To get the synonyms FR:
		//https://www.wikidata.org/w/api.php?action=wbgetentities&ids=Q174876&props=aliases&languages=fr&formatversion=2			
				
		
		String responseJSONString = "";
		
		Client client = Client.create();
		
		// Here, instead of .queryParam, we could also just concatenate the values to the URL
		// Fabian: valider si la valeur numérique est ok ainsi, j'ai du mettre en "" pour l'instant
		// sinon voir aussi: http://stackoverflow.com/questions/13750010/jersey-client-how-to-add-a-list-as-query-parameter
		//   où le queryParam est ajouté directement à l'appel 
		//Zhan: linux: /var/lib/tomcat8/webapps
		WebResource webResource = client.resource("https://www.wikidata.org/w/api.php");
		MultivaluedMap queryParams = new MultivaluedMapImpl();
		   queryParams.add("action", "wbsearchentities");
		   queryParams.add("format", "json");
		   queryParams.add("language", "fr");
		   queryParams.add("uselang", "fr");
		   queryParams.add("formatversion", "2");
		   queryParams.add("type", "item");
		   queryParams.add("continue", "0"); 
		   queryParams.add("limit", "15");
		   queryParams.add("search", searchedTerm);

		// other params: 
		// limit: the number of results to return, No more than 50 (500 for bots) allowed.
		// continue: Offset where to continue a search -> 0 for the first results 
		
		ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		
		JSONObject resultsObject = new JSONObject();
		
		if(response.getStatus() == 200) {
			//System.out.println(responseJSONString) ; 
			responseJSONString = response.getEntity(String.class);
			
			//create the table to get the results by key
			
			ObjectMapper mapper = new ObjectMapper(); // Jackson object mapper
			try {
				Map<String,Object>wikiDataResults = mapper.readValue(responseJSONString, Map.class);
				ArrayList<LinkedHashMap> searchObjects = (ArrayList<LinkedHashMap>) wikiDataResults.get("search") ;
				
				
				for (LinkedHashMap<String, String> oneResultHMap : searchObjects)
				{
					if (oneResultHMap.containsKey("id")){
						System.out.println("id: " + oneResultHMap.get("id") + " '" + oneResultHMap.get("label") + "' '" + oneResultHMap.get("description") + "'" );
						if(oneResultHMap.get("description") != null && !oneResultHMap.get("description").equalsIgnoreCase("page d'homonymie d'un projet Wikimédia") && !oneResultHMap.get("description").equalsIgnoreCase("page d'homonymie de Wikimedia") && !oneResultHMap.get("description").equalsIgnoreCase("page d'homonymie")){
							try {
								JSONObject innerObject = new JSONObject();
								innerObject.put("id", oneResultHMap.get("id"));
								innerObject.put("concepturi", oneResultHMap.get("concepturi"));
								innerObject.put("label", oneResultHMap.get("label"));
								innerObject.put("description", oneResultHMap.get("description"));
								innerObject.put("aliases", oneResultHMap.get("aliases"));
								innerObject.put("source", "http://www.wikidata.org");
								items.add(innerObject);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	     }

		
		//add outer "results" to json
		try {
			resultsObject.put("search", new JSONArray(items));
			System.out.println("!!!!!!!!!!!--"+resultsObject.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return responseJSONString;
		return resultsObject.toString();
	}
	
	public String updatePMO(String results){
		
		//algo for update
		/*
		1.	The function receives a set of values from the client-side, with (optional): uc (user concept), wd (wikidata), one or more user’s labels.
		2.	URL management for SKOS concepts
			a.	It takes at least 1 URL - mandatory: uc-url
			b.	In addtion, if we received a wd-url in the se
		3.	Memorize these 1 or 2 url for the following operations, taking them first in the set.
		4.	Should I create a uc-url?
		We must create a uc-url if uc-url is null
			a.	If wd-url exists
				i.	Create the uc-url based on the id of wd-url
				ii.	And test that it does not already exist, if not error
			b.	Else
					i.	If there are labels
					ii.	Create the uc-url based on a label (_1, _2, _3), to find one that does not exist
		5.	The url is already exists
			a.	uc-url: mandatory
			b.	wd-url: optional
		6.	Now we save the information
			a.	If there is wd-url
				i.	Save the Wikidata information to SKOS, and add exactMatch to uc-url
			b.	If there are labels
				i.	Add the labels to uc-url
		*/

		
		
		OntoFuse ontoFuseSelect = new OntoFuse("http://88.99.66.215:5820/AOA/query?reasoning=true");
		JSONObject returnJson = new JSONObject();
		
		JSONObject resultsAsJson = new JSONObject(results);
		
		boolean hasWikidata;
		boolean hasUserConcept;
		boolean hasNewUserLabel;
		
		String url_wikidata = "";
		String url_userConcept = "";
		//String url_concept = "";
		
		JSONObject wikidataObject = null;
		JSONObject userConceptObject = null;
		JSONObject userLabelObject = null;
		
		try (OntoFuse ontoFuseUpdate = new OntoFuse("http://88.99.66.215:5820/AOA/update"))
		{
			ontoFuseUpdate.setSPARQLEndPointUsernameAndPassword("aoa_datamanager", "aoamanager");
			ontoFuseUpdate.initialize();
			
			
			ontoFuseSelect.setSPARQLEndPointUsernameAndPassword("anonymous", "anonymous");
			ontoFuseSelect.initialize();
			
			//1. get json result from interface
			//get each concept
			for(int i=0; i<5; i++){
				hasWikidata = false;
				hasUserConcept = false;
				hasNewUserLabel = false;
				url_wikidata = "";
				url_userConcept = "";
				//url_concept = "";
				wikidataObject = null;
				userConceptObject = null;
				userLabelObject = null;
				
				
				JSONObject concept = resultsAsJson.getJSONObject(Integer.toString(i));
				//JSONObject concept;
				
				
		        if(concept != null){
		        	
					Iterator<String> keys_temp = concept.keys();
				
					while(keys_temp.hasNext()) {
						String uriKeyword=keys_temp.next();
				        System.out.println("uriKeyword:" +uriKeyword);


						if(uriKeyword.equals("newUserLabel")){
							
							
							
							hasNewUserLabel = true;
							userLabelObject = concept.getJSONObject(uriKeyword);
							
							//store and return the newUserLabel
							JSONArray userLabel = concept.getJSONObject(uriKeyword).getJSONArray("label");
							if(userLabel.length() > 0){
					 			for(int j=0; j<userLabel.length(); j++) {
					 				returnJson.put(i+"_"+j+"_"+uriKeyword, userLabel.getString(j).replace("'", "\\'"));
					 			}
		        			}
							
						}else{
				        	String source = concept.getJSONObject(uriKeyword).getString("source");
				        	if(source.equals("http://www.wikidata.org")){	
				        		hasWikidata = true;
				        		url_wikidata = uriKeyword;
				        		wikidataObject = concept.getJSONObject(uriKeyword);
							
				        		//store and return the wikidata altLab
								JSONArray wikidataAltLabel = concept.getJSONObject(uriKeyword).getJSONArray("altLabel");
								if(wikidataAltLabel.length() > 0){
						 			for(int j=0; j<wikidataAltLabel.length(); j++) {
						 				returnJson.put(i+"_"+j+"_"+uriKeyword, wikidataAltLabel.getString(j).replace("'", "\\'"));
						 			}
						 			//store and return the wikidata label
					        		returnJson.put(i+"_"+wikidataAltLabel.length()+"_"+uriKeyword, wikidataObject.getString("label"));
			        			}else{
			        				//store and return the wikidata label
					        		returnJson.put(i+"_"+0+"_"+uriKeyword, wikidataObject.getString("label"));
			        			}
				        		
				        	}else if (source.equals("http://datasemlab.ch")){
				        	 //else if (source.equals("pmo")){
				        		hasUserConcept = true;

						        System.out.println("hasUserConcept- http://datasemlab.ch: "+hasUserConcept);
				        		url_userConcept = uriKeyword;
				        		userConceptObject = concept.getJSONObject(uriKeyword);
				        		
				        		//store and return the userConcept altLab
								JSONArray ucAltLabel = concept.getJSONObject(uriKeyword).getJSONArray("altLabel");
								if(ucAltLabel.length() > 0){
						 			for(int j=0; j<ucAltLabel.length(); j++) {
						 				returnJson.put(i+"_"+j+"_"+uriKeyword, ucAltLabel.getString(j).replace("'", "\\'"));
						 			}
			        			}
								
				        	}
						} 
					}

				}
		        System.out.println("hasWikidata: "+hasWikidata+" hasUserConcept: "+hasUserConcept+" hasNewUserLabel: "+hasNewUserLabel);
		        System.out.println("iiiiiiiiiiiiiiiiiiii: "+i);

		        if(concept != null){
			        //Get or create an URL
		        	if(url_userConcept == ""){
		        		if(url_wikidata !=""){
		        			//Créer le uc-url basé sur l'id de wd-url
		        			//et tester s'elle n'existe pas déjà, sinon erreur
		        			String idWikidata = wikidataObject.getString("id");
					 		
		        			String query_checkExistingWikidataUserConcept = "SELECT * {<http://datasemlab.ch/vpx/userConcept/wikidata/"+ idWikidata + "> a skos:Concept ; a ?type. }";
					 		System.out.println("-----query_checkExistingWikidataUserConcept-----");
						 	System.out.println(query_checkExistingWikidataUserConcept);	
						 	ByteArrayOutputStream out = new ByteArrayOutputStream() ;
						 	ontoFuseSelect.runSPARQLSelectToJSONOutputStream(OntoFuse.repoSrc.SPARQL_ENDPOINT, query_checkExistingWikidataUserConcept, out);
						 	String result = new String(out.toString().getBytes(), "UTF-8");
						 	
						 	boolean checkConcept = checkExistingConcept(result);
						 	if (!checkConcept){
			        			String query_createUserConceptWikidata = "INSERT DATA {<http://datasemlab.ch/vpx/userConcept/wikidata/"+idWikidata+">"
							 			+" a skos:Concept ; "
							 			+" dc:source <http://datasemlab.ch> . "
							 			//+" skos:exactMatch " +conceptUri + ". "
							 			+"}";
							 	System.out.println("-----query_createUserConceptWikidata-----");
							 	System.out.println(query_createUserConceptWikidata);	
							 	ontoFuseUpdate.runSPARQLUpdate(OntoFuse.repoSrc.SPARQL_ENDPOINT, query_createUserConceptWikidata);
						 	}
						 	url_userConcept = "http://datasemlab.ch/vpx/userConcept/wikidata/"+idWikidata;

		        		}else if(hasNewUserLabel){
		        			//If des labels
		        			//Créer le uc-url basé sur un label (_1, _2, _3), pour trouver un qui n'existe pas
		        			
		        			//we get the first newUserLabel to check and create the id
			        		int idCount = 1;
			        		String query_checkExistingConceptWithNewLabel = "";
			        		JSONArray newUserAltLabel = userLabelObject.getJSONArray("label");
			        		String newUserConceptId = newUserAltLabel.getString(0);
			        		String urlToCheck = "http://datasemlab.ch/vpx/userConcept/user/"+newUserConceptId.replaceAll("[-+.^:,]","");
			        		System.out.println("-----urlToCheck-----");
						 	System.out.println(urlToCheck);
						 	
			        		query_checkExistingConceptWithNewLabel = "SELECT * {<" + urlToCheck + "> a skos:Concept ; a ?type. }";
			        		System.out.println("-----query_checkExistingConceptWithNewLabel_1-----");
						 	System.out.println(query_checkExistingConceptWithNewLabel);
			        		ByteArrayOutputStream out = new ByteArrayOutputStream() ;
			        		ontoFuseSelect.runSPARQLSelectToJSONOutputStream(OntoFuse.repoSrc.SPARQL_ENDPOINT, query_checkExistingConceptWithNewLabel, out);
			        		String result = new String(out.toString().getBytes(), "UTF-8");
						 	
						 	boolean checkConcept = checkExistingConcept(result);
						 	if (!checkConcept){
			        			//if there is not existing concept
			        			String query_createNewUserConceptWithNewLabel = "INSERT DATA {<"  
						 				+ urlToCheck + "> a skos:Concept ; dc:source <http://datasemlab.ch> ; ";
				 				for(int countAltLabel=0; countAltLabel<=newUserAltLabel.length()-1; countAltLabel++) {
				 					query_createNewUserConceptWithNewLabel += "skos:altLabel '"+newUserAltLabel.getString(countAltLabel).replace("'", "\\'") +"'@fr";
				        			if(countAltLabel < (newUserAltLabel.length()-1)){
				        				query_createNewUserConceptWithNewLabel += "; ";
					 				}else{
					 					query_createNewUserConceptWithNewLabel += ".} ";
					 				}
				        		}
				 				url_userConcept = urlToCheck;
				 				System.out.println("-----query_createNewUserConceptWithNewLabel_1-----");
							 	System.out.println(query_createNewUserConceptWithNewLabel);
							 	ontoFuseUpdate.runSPARQLUpdate(OntoFuse.repoSrc.SPARQL_ENDPOINT, query_createNewUserConceptWithNewLabel);
			        		}else{
			        			//if there is existing concept continue to check the _number
			        			boolean existingConcept = true;

			        			while(existingConcept){
					        		ByteArrayOutputStream out_checkLabel = new ByteArrayOutputStream() ;
			        				query_checkExistingConceptWithNewLabel = "SELECT * {<" + urlToCheck +"_" + idCount + "> a skos:Concept ; a ?type. }";
			        				System.out.println("-----query_checkExistingConceptWithNewLabel_2-----");
								 	System.out.println(query_checkExistingConceptWithNewLabel);
			        				ontoFuseSelect.runSPARQLSelectToJSONOutputStream(OntoFuse.repoSrc.SPARQL_ENDPOINT, query_checkExistingConceptWithNewLabel, out_checkLabel);
					        		String result_checkLabel = new String(out_checkLabel.toString().getBytes(), "UTF-8");
					        		System.out.println("-----result_checkLabel-----");
					        		System.out.println(result_checkLabel);
								 
					        		boolean checkConceptLabel = checkExistingConcept(result_checkLabel);
								 	if (!checkConceptLabel){
			        					existingConcept = false;
						        		System.out.println("existingConcept = false");

			        				}
			        				idCount++;
			        			}
			        			if(!existingConcept){
			        				idCount = idCount-1;
			        				String newUserConceptUrl = urlToCheck + "_" + idCount;
			        				String query_createNewUserConceptWithNewLabel = "INSERT DATA {<"  
			        		 				+ newUserConceptUrl + "> a skos:Concept ; dc:source <http://datasemlab.ch> ; ";
			        					for(int countAltLabel=0; countAltLabel<=newUserAltLabel.length()-1; countAltLabel++) {
			        						query_createNewUserConceptWithNewLabel += "skos:altLabel '"+newUserAltLabel.getString(countAltLabel).replace("'", "\\'") +"'@fr";
				        					if(countAltLabel < (newUserAltLabel.length()-1)){
				        						query_createNewUserConceptWithNewLabel += "; ";
				        						}else{
				        							query_createNewUserConceptWithNewLabel += ".} ";
				        						}
			        				}
			        				System.out.println("-----query_createNewUserConceptWithNewLabel_2-----");
			        			 	System.out.println(query_createNewUserConceptWithNewLabel);
			        			 	ontoFuseUpdate.runSPARQLUpdate(OntoFuse.repoSrc.SPARQL_ENDPOINT, query_createNewUserConceptWithNewLabel);
			        			 	
			        			 	url_userConcept = newUserConceptUrl;
			        			}
			        		}

		        			
		        		}
		        	}//end if us-url is null
		        	
		        	//start to sauver les information:
		        	//Si wd-url
		        	//Sauver en SKOS les infos de Wikidata, ajout exactMatch sur uc-url
		        	
		        	if(url_wikidata !=""){
		        		String query_insertWikidata = "INSERT DATA {<"  
				 				+ url_wikidata + "> a skos:Concept ; ";
	
		        				String labelWikidata = "";
		        				String definitionWikidata = "";
		        				JSONArray altLabel = wikidataObject.getJSONArray("altLabel");
		        				
		        				
		        				if (wikidataObject.has("label")) {
		        					labelWikidata = wikidataObject.getString("label");
				        			query_insertWikidata += "skos:altLabel '" + labelWikidata.replace("'", "\\'") + "'@fr ; ";
							    }
		        				
		        				if(altLabel.length() > 0){
			        				query_insertWikidata += "skos:altLabel ";
						 			for(int j=0; j<altLabel.length(); j++) {
						 				query_insertWikidata += "'" + altLabel.getString(j).replace("'", "\\'") + "'@fr ";
						 				if(j < (altLabel.length()-1)){
						 					query_insertWikidata += ", ";
						 				}else{
						 					query_insertWikidata += "; ";
						 				}
						 			}
			        			}
		        				
		        				if (wikidataObject.has("definition")) {
		        					definitionWikidata = wikidataObject.getString("definition");
				        			query_insertWikidata += "skos:definition '" + definitionWikidata.replace("'", "\\'") + "'@fr ; ";
							    }
			        			
					 			query_insertWikidata += "dc:source <http://www.wikidata.org> . ";
					 			query_insertWikidata += "}" ;
						 	System.out.println("-----query_insertWikidata----- ");
						 	System.out.println(query_insertWikidata);
	        			 	ontoFuseUpdate.runSPARQLUpdate(OntoFuse.repoSrc.SPARQL_ENDPOINT, query_insertWikidata);

						 	
	        				String query_matchUserConcept2Wikidata = "INSERT DATA{<" + url_wikidata + "> skos:exactMatch <" + url_userConcept + ">.}";	
	        				System.out.println("-----query_matchUserConcept2Wikidata----- ");
						 	System.out.println(query_matchUserConcept2Wikidata);
						 	ontoFuseUpdate.runSPARQLUpdate(OntoFuse.repoSrc.SPARQL_ENDPOINT, query_matchUserConcept2Wikidata);
		        	}
		        	
		        	if(hasNewUserLabel){
		        		//Si des labels
			        	//Ajouter les altLabel au uc-url
		        		String query_addLatLabel = "INSERT DATA {<"  
		        				+ url_userConcept + "> a skos:Concept ; ";
		        		
		        		JSONArray newUserAltLabel = userLabelObject.getJSONArray("label");
		        		if(newUserAltLabel.length() > 0){
		        			query_addLatLabel += "skos:altLabel ";
				 			for(int j=0; j<newUserAltLabel.length(); j++) {
				 				query_addLatLabel += "'" + newUserAltLabel.getString(j).replace("'", "\\'") + "'@fr ";
				 				if(j < (newUserAltLabel.length()-1)){
				 					query_addLatLabel += ", ";
				 				}else{
				 					query_addLatLabel += ".} ";
				 				}
				 			}
	        			}
		        		System.out.println("-----query_addLatLabel----- ");
					 	System.out.println(query_addLatLabel);
					 	ontoFuseUpdate.runSPARQLUpdate(OntoFuse.repoSrc.SPARQL_ENDPOINT, query_addLatLabel);

		        	}

		        	
		        }
		        
			}

		ontoFuseUpdate.close();
		
	} catch (Exception e) {
		e.printStackTrace();
	}
		ontoFuseSelect.close();
		
  	   /*
		JSONObject json = new JSONObject();

	    // put some value pairs into the JSON object .
	    json.put("Mobile", "9999988'888");
	    json.put("Name", "ManojSarnaik");
	   
	   
	    Iterator<?> keys = json.keys();
	    while( keys.hasNext() ) {
	        String key = (String)keys.next();
	        System.out.print(json.get(key));
	    }
	    */

	    // finally output the json string       
		
		return returnJson.toString();
	}
	
	
	public String getInfoFromUserConceptWikidata(String url)
	{
		//if user selects an existing 
		//ex: http://datasemlab.ch/vpx/userConcept/wikidata/Q942537
		String searchedUrl = url ;
		Collection<JSONObject> items = new ArrayList<JSONObject>();
		ByteArrayOutputStream out = new ByteArrayOutputStream() ;
		JSONObject resultsObject = new JSONObject();


		//get result from our triplestore
		
		try (OntoFuse ontoFuse = new OntoFuse("http://www.datasemlab.ch:5820/AOA/query?reasoning=true"))
		{
			ontoFuse.setSPARQLEndPointUsernameAndPassword("anonymous", "anonymous");
			ontoFuse.initialize();
			
			String query = "PREFIX dcterm:<http://purl.org/dc/terms/> "
					+ " SELECT ?wdc (CONCAT(?ucLabels, ';', ?wdcLabels) as ?labels) (CONCAT(?wdcDefs, ';', ?ucDefs) as ?defs) "
					+ " { "	
					+ " SELECT DISTINCT ?wdc (GROUP_CONCAT(DISTINCT ?ucLabel; separator=';') as ?ucLabels) (GROUP_CONCAT(DISTINCT ?wdcLabel; separator=';') as ?wdcLabels) "
					+ " (GROUP_CONCAT(DISTINCT ?wdcDef; separator=';') as ?wdcDefs) (GROUP_CONCAT(DISTINCT ?ucDef; separator=';') as ?ucDefs) "
					+ " WHERE "
					+ " { "
					+ " <"
					+ searchedUrl
					+ "> a skos:Concept . "
					+ " OPTIONAL { <" + searchedUrl + "> skos:altLabel ?ucLabel }"
					+ " OPTIONAL { <" + searchedUrl + "> skos:definition ?ucDef }"
					+ " OPTIONAL { <" + searchedUrl + "> skos:exactMatch ?wdc ."
					+ " ?wdc dcterm:source <http://www.wikidata.org> . "
					+ " OPTIONAL{?wdc skos:altLabel ?wdcLabel } "
					+ " OPTIONAL{?wdc skos:definition ?wdcDef } "
					+ " } "
					+ " } GROUP BY ?wdc "
					+ " } ";
					
			System.out.println("getInfoFromUserConceptWikidata: ");
			System.out.println(query);

	    	ontoFuse.runSPARQLSelectToJSONOutputStream(OntoFuse.repoSrc.SPARQL_ENDPOINT, query, out);
	    	String results = new String(out.toString().getBytes(), "UTF-8");

	    	System.out.println("getInfoFromUserConceptWikidata:"+results) ;
	    	JSONObject resultsAsJson = new JSONObject(results);
	    	JSONArray bindings = resultsAsJson.getJSONObject("results").getJSONArray("bindings");

	    	JSONObject myObject = bindings.getJSONObject(0);
	    	//System.out.println("11111111111:"+myObject.getJSONObject("wdc").getString("value"));
    		//System.out.println("22222222222:"+myObject.getJSONObject("labels").getString("value"));
    		//System.out.println("33333333333:"+myObject.getJSONObject("defs").getString("value"));
	    	
	    	JSONObject innerObject = new JSONObject();
	    	String altLab = "";
    		String preLab = "";
    		
    		altLab =  myObject.getJSONObject("labels").getString("value");
			preLab = altLab.split(";")[0];
			innerObject.put("label",preLab);
    		
			ArrayList altlabs = new ArrayList(Arrays.asList(altLab.split(";")));
			innerObject.put("aliases", altlabs);
			
    		innerObject.put("id", searchedUrl.substring(46));
    		//innerObject.put("id", "12345678");
			innerObject.put("concepturi",searchedUrl);
			if(myObject.getJSONObject("defs").getString("value").equals(";")){
				innerObject.put("description", "");
			}else{
				innerObject.put("description", myObject.getJSONObject("defs").getString("value"));
			}
			innerObject.put("source", "http://datasemlab.ch");
			items.add(innerObject);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			resultsObject.put("search", new JSONArray(items));
			System.out.println("!!!!!!!!!!!--"+resultsObject.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return responseJSONString;
		return resultsObject.toString();
	}
	
	public boolean checkExistingConcept(String result){
		JSONObject check = new JSONObject(result); 
    	JSONArray bindings = check.getJSONObject("results").getJSONArray("bindings");
    	if (bindings.length() > 0){
    		return true;
    	}else{
    		return false;
    	}
	}
	
	
}
