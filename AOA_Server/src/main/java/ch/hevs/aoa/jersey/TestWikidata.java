package ch.hevs.aoa.jersey;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import jdk.nashorn.internal.parser.JSONParser;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestWikidata {
	
	public static void main(String[] args) {
		long tsBegin = 0 ;
		long tsEnd = 0 ;
		tsBegin = System.nanoTime();

		TestWikidata tw = new TestWikidata() ;
		
		// view.testLogger();
		//view.testOntoFuse();
		//view.testDBConnection();
		
		//view.getTagsWikidataInfos();
		String keyword = "yeux";
		tw.testWikidataAPI(keyword) ;
		
		tsEnd = System.nanoTime();
		double seconds = (double)(tsEnd - tsBegin) / 1000000000.0;
		System.out.println("View done in " + seconds + " seconds!") ;
	}
	
	private void testWikidataAPI(String keyword)
	{
		//To get the entity in French
		//https://www.wikidata.org/w/api.php?action=wbsearchentities&search=yeux&language=fr&uselang=fr&formatversion=2
				
		//To get the synonyms FR:
		//https://www.wikidata.org/w/api.php?action=wbgetentities&ids=Q174876&props=aliases&languages=fr&formatversion=2			
				
		String searchedTerm = keyword ;
		
		Client client = Client.create();
		
		// Here, instead of .queryParam, we could also just concatenate the values to the URL
		// Fabian: valider si la valeur numérique est ok ainsi, j'ai du mettre en "" pour l'instant
		// sinon voir aussi: http://stackoverflow.com/questions/13750010/jersey-client-how-to-add-a-list-as-query-parameter
		//   où le queryParam est ajouté directement à l'appel 
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
		
		String responseJSONString = response.getEntity(String.class);
		
		//add triplestore, we have to merge the results from triplestore to the same json, check the key and add the value
		//http://stackoverflow.com/questions/5245840/how-to-convert-string-to-jsonobject-in-java
		//String triplestoreData = "{'searchinfo':{'search':'yeux'},'search':[{'id':'123','concepturi':'http://www.wikidata.org/entity/Q22083347','url':'//www.wikidata.org/wiki/Q22083347','title':'Q22083347','pageid':24114586,'label':'test','description':'livre de Michel Serres','resource':'triplestore'}]}";
		
		JSONObject resultsObject = new JSONObject();
		//JSONArray  resultsArray = new JSONArray();
		Collection<JSONObject> items = new ArrayList<JSONObject>();
		
		/*
		JSONObject jo = new JSONObject();
	    Collection<JSONObject> items = new ArrayList<JSONObject>();
	    
	    JSONObject item1 = new JSONObject();
	    try {
			item1.put("id", "123456");
			item1.put("concepturi", "http://www.wikidata.org/entity/123456");
		    item1.put("label", "rosa, rosacée");
		    item1.put("description", "add later");
		    item1.put("resource", "tripestore");
		    items.add(item1);
		    
		    jo.put(responseJSONString, new JSONArray(items));
		    System.out.println(jo.toString());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		
		if(response.getStatus() == 200) {
			System.out.println(responseJSONString) ; 
		
			ObjectMapper mapper = new ObjectMapper(); // Jackson object mapper
			try {
				Map<String,Object>wikiDataResults = mapper.readValue(responseJSONString, Map.class);
				// Here are the map entries (string / object)
				// 	searchinfo/{search=breastfeeding}
				// 	search/[{id=Q174876, concepturi=http://www.wikidata.org/entity/Q174876, url=//www.wikidata.org/wiki/Q174876, title=Q174876, pageid=174753, label=breastfeeding, description=natural method of feeding human babies, match={type=label, language=en, text=breastfeeding}}, {id=Q4959811, concepturi=http://www.wikidata.org/entity/Q4959811, url=//www.wikidata.org/wiki/Q4959811, title=Q4959811, pageid=4740478, label=breastfeeding difficulties, match={type=label, language=en, text=breastfeeding difficulties}}, {id=Q3537720, concepturi=http://www.wikidata.org/entity/Q3537720, url=//www.wikidata.org/wiki/Q3537720, title=Q3537720, pageid=3367992, label=Breastfeeding and HIV, match={type=label, language=en, text=Breastfeeding and HIV}}, {id=Q27333703, concepturi=http://www.wikidata.org/entity/Q27333703, url=//www.wikidata.org/wiki/Q27333703, title=Q27333703, pageid=29139758, label=BReastfeeding Attitude and Volume Optimization (BRAVO) trial: study protocol for a randomized controlled trial, match={type=label, language=en, text=BReastfeeding Attitude and Volume Optimization (BRAVO) trial: study protocol for a randomized controlled trial}}, {id=Q27027685, concepturi=http://www.wikidata.org/entity/Q27027685, url=//www.wikidata.org/wiki/Q27027685, title=Q27027685, pageid=28906120, label=Breastfeeding after anaesthesia: a review of the pharmacological impact on children, description=scientific article, match={type=label, language=en, text=Breastfeeding after anaesthesia: a review of the pharmacological impact on children}}, {id=Q21203590, concepturi=http://www.wikidata.org/entity/Q21203590, url=//www.wikidata.org/wiki/Q21203590, title=Q21203590, pageid=23250709, label=Breastfeeding and HIV: experiences from a decade of prevention of postnatal HIV transmission in sub-Saharan Africa, description=scientific article, match={type=label, language=en, text=Breastfeeding and HIV: experiences from a decade of prevention of postnatal HIV transmission in sub-Saharan Africa}}, {id=Q26739956, concepturi=http://www.wikidata.org/entity/Q26739956, url=//www.wikidata.org/wiki/Q26739956, title=Q26739956, pageid=28669319, label=Breastfeeding and Opiate Substitution Therapy: Starting to Understand Infant Feeding Choices, description=scientific article, match={type=label, language=en, text=Breastfeeding and Opiate Substitution Therapy: Starting to Understand Infant Feeding Choices}}]
				// 	search-continue/7
				// 	success/1
				
				
				for (Map.Entry<String, Object> entry : wikiDataResults.entrySet())
				{
				    System.out.println("one entry: " + entry.getKey() + "------" + entry.getValue());
				}
				
				
				
				// The "search" is a JSON array of results
				// It is a java ArrayList of LinkedHashMap, each LinkedHashMap being one result
				// Then each LinkedHashMap (result) contains an array list of LinkedHashMap, one for each key/value pair as:
				//  "id":"Q174876"
				//	"concepturi":"http://www.wikidata.org/entity/Q174876"
				ArrayList<LinkedHashMap> searchObjects = (ArrayList<LinkedHashMap>) wikiDataResults.get("search") ;
				
				//for (int i = 0; i < searchObjects.size(); i++)
				//	LinkedHashMap<String, String> oneResultHMap = searchObjects.get(i) ;
				System.out.println(searchObjects.size());
				for (LinkedHashMap<String, String> oneResultHMap : searchObjects)
				{
					if (oneResultHMap.containsKey("id")){
						System.out.println("id: " + oneResultHMap.get("id") + " '" + oneResultHMap.get("label") + "' '" + oneResultHMap.get("description") + "'" );
						try {
							JSONObject innerObject = new JSONObject();
							innerObject.put("id", oneResultHMap.get("id"));
							innerObject.put("concepturi", oneResultHMap.get("concepturi"));
							innerObject.put("label", oneResultHMap.get("label"));
							innerObject.put("description", oneResultHMap.get("description"));
							innerObject.put("aliases", oneResultHMap.get("aliases"));
							innerObject.put("source", "wikidata");
							//System.out.println("!!!!!!!!!!!!!!"+innerObject.toString());
							items.add(innerObject);
							//System.out.println("-------"+items.toString());
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					    
					}
				}
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	     }
		//add outer "results"
		try {
			resultsObject.put("search", new JSONArray(items));
			System.out.println("!!!!!!!!!!!--"+resultsObject.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	

}
