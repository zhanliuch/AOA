package ch.hevs.aoa.jersey;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.json.JSONObject;

import ch.hevs.ontoFuse.OntoFuse;
import ch.hevs.ontoFuse.OntoFuse.repoSrc;

/*
 * For the dev and tests, this web services runs in Eclipse-Tomcat 7 Server
 * Configured with a Java 8 VM
 * 
 * Call example from a Web Browser:
 *  Runned in Eclipse 
 * 		http://localhost:8081/AOAServer/Search/synsetsFromKw?kw=valais&maxResults=10
 * 		http://localhost:8081/AOAServer/Search/relatedSynsets?synsetURI=http://babelnet.org/rdf/s00082104n&maxResults=10
 *  Fabian's local Tomcat 7
 *  	http://localhost:8080/AOAServer/Search/synsetsFromKw?kw=valais&maxResults=10
 *  On our Server
 *  	http://vmhwebsem.hevs.ch:8082/AOAServer/Search/synsetsFromKw?kw=valais&maxResults=10
 *  
 *  The tests:
 *  	http://localhost:8081/AOAServer/TestREST/JavaCodeGeeks?value=blabla235678
 *  
 * First tests for this server where done in the class TestREST.java, see there for details 
 *  
 * When the server is started in Eclipse, and the code here is modified
 * -> 	the modification is automatically done when saving the file and the client will see the new code 
 * 		without restarting the server
 * 
 * If the POM file is modified, do a 'Maven'-'Update project'
 * 
 * Libraries:
 * 	Most dependencies are handled by Maven
 *  But OntoFuse, (and BabelNet/Babelfy for instance) are not available through Maven
 *  -> to include them in this project, and also in the generated WAR file
 *  add the libraries to "Deployed Resources\WEB-INF\lib" (is "Deployed Resources" it the same as the current "WebContent"
 *  	BabelNet/Babelfy is not used so far, but it was used in Swicico and I keep them in case we want to call them too
 *  	they are originally found in the Swicico dev folder: SWICICO\Dev\lib\Babelnet-Babelfy
 *  OntoFuse.jar currently don't include its dependencies, they were added to this project POM:
 *  	logback-classic, 1.1.2
 *		mysql-connector-java, 5.1.6
 *	 	then "commons-logging, 1.1.1" was not added as a newer version is included in WEB-INF\lib 
 *  
 * Libraries update
 *  	It did happen that swicico.jar was updated: a Maven:Install was done on the swicico project
 *  		so that the new version is available in the local Maven repository
 *  	Then a Maven:Build with goal eclipse:eclipse was done here, but still the new method were not visible
 *  	-> I did open Java Resources/Libraries/swicico-0.0.2-SNAPSHOT.jar, and saw the new method there
 *  	-> and by doing this, the editor was updated and the method availabe
 *  	-> but then the exception came when running the server: java.lang.ClassNotFoundException: com.sun.jersey.spi.container.servlet.ServletContainer
 *  	I had to: project -> properties -> development assembly -> add -> java build path entries --> maven dependency (et là toutes ces dépendances). 
 * 
 * Warning when adapting SwicicoServer to this AOAServer:
 * - the new OntoFuse relies on RDF4J, and thus needs Java 8 -> changed in the settings of the project
 * - SwicicoServer had those 3 libraries in WEB-INF\Lib: httpclient-4.3.6.jar, httpcore-4.3.3.jar, httpmime-4.3.6.jar
 * 		when creating AOAServer, they were in conflict with new versions required by RDF4J
 * 		and so I removed them
 * - I also removed libraries which have newer versions: commons-codec-1.8.jar, commons-lang-2.3.jar,
 * 		commons-logging-1.1.3.jar, gson-2.2.4.jar, lucene-analyzers-common-4.9.0.jar, lucene-core-4.9.0.jar
 * 
 * 	All removed libraries where saved in subfolder "AOA\Dev_Server\old lib"
 * 
 * Deploying this server on a local Tomcat (instead of running Tomcat in Eclipse):
 *  - from Eclipse, export the project to a war
 *    and copy that war to the Tomcat server: Tomcat 7.0\webapps
 *    the contained WEB-INF\lib does included all libraries (from Maven, but also from "Deployed Resources\WEB-INF\lib")
 *  - ** if babelNet/Babelfy are used ** copy the BabelNet/Babelfy config folder to "Tomcat 7.0" (and not \webapps, right ? to be validated)
 *  - after first accessing the page, the .war is decompressed and lib files are found in:
 *  	Tomcat 7.0\webapps\AOAServer\WEB-INF\lib
 *    
 *    
 * Optimization question:
 * 	Should an instance of OntoFuse be created once and for all ?
 */
@Path("/Search")
public class Search {
	private String SesameEndPoint = "http://vmhwebsem.hevs.ch:8082/openrdf-sesame" ;
	private String SesameRep = "SWICICO2" ;
	
	
	@GET
	@Path("/conceptsWikidata")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String getConceptsFromWikidata(@QueryParam("kw") String kw, 
			@DefaultValue("15") @QueryParam("maxResults") Integer maxResults) 
	{
        if (kw == null) {
            return "Missing Parameter 'keyword'";
        }
        
        WikidataSearch wikidata = new WikidataSearch();
        String searchJsonResults = wikidata.WikidataAPI(kw);
		
		return searchJsonResults ;
	}
	
	@GET
	@Path("/userConceptFromWikidata")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String getConceptsFromUserConceptWikidata(@QueryParam("url") String url, 
			@DefaultValue("15") @QueryParam("maxResults") Integer maxResults) 
	{
        if (url == null) {
            return "Missing Parameter 'Concept URL'";
        }
        
        WikidataSearch wikidata = new WikidataSearch();
        String searchJsonResults = wikidata.getInfoFromUserConceptWikidata(url);
		
		return searchJsonResults ;
	}
	
	
	//Get the Json value from client side to analyze and store in PMO
	@POST
	@Path("/store2pmo")
	//@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")	
	@Produces(MediaType.APPLICATION_JSON)
	public Response storeResults2PMO(String results) 
	{
       /*
		if (results == null) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Exception ");
        }
        */
		ResponseBuilder rb;
		Response updateResults = null;
		String returnResult ="";
		WikidataSearch wikidata = new WikidataSearch();
		
		System.out.println("results : " + results) ;
	    //return Response.status(200).entity(output).build();
	
		try {
			rb = Response.status(Status.OK).entity(results);
	 		System.out.println("OKKKKKKKKKKKKKKKKKKKKKKKKK") ;
	 		
	 		//WikidataSearch wikidata = new WikidataSearch();
	 		//updateResults = wikidata.updatePMO(results);
	 		//updateResults = Response.status(200).entity(wikidata.updatePMO(results)).build();

			//returnResult = wikidata.updatePMO(results);
	 		//working for eclipse
			//updateResults = Response.status(200).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").entity(returnResult).build();
			
			//working for tomcat server
			updateResults = Response.status(200).entity(wikidata.updatePMO(results)).build();
			
	 	}catch (Exception e) {
	 		e.printStackTrace();
	 		rb = Response.status(Status.INTERNAL_SERVER_ERROR);
			System.out.println("err") ;

	 	}
		//System.out.println("-----------------" + returnResult);
	 	//return rb.build();
	 	return updateResults;
	}
	
	
	/*
	 * From a keyword in english, get synsets from the Triple Store
	 * 	based on a Regex on the synsets' labels
	 * 
	 * For the demonstrator: the code called to display the list of synsets corresponding to the input text (kw)
	 * 
	 * This code is not based on Swicico.jar, but directly on OntoFuse (which is included in Swicico.jar)
	 * 
	 * The query is done one the rdfs:label, a synset might have more than one label but this seems
	 * 	reasonable for different spelling. 
	 *  This might be changed to also query the skos:altLabel (redirections from wikipedia) - which are not necessary loaded in the store
	 * 
	 * If there is a problem with the returned encoding (utf-8), see:
	 * http://stackoverflow.com/questions/5514087/jersey-rest-default-character-encoding/20569571
	 * http://stephen.genoprime.com/2011/05/29/jersey-charset-in-content-type.html
	 * -> to avoir specifying everywhere: @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	 * If this encoding is not specifed here, than calling from the browser would get the correct string (as "utf-8" is specified for the Response here under
	 *  	but we then need to change "View-Text encoding" to unicode
	 */
	@GET
	@Path("/synsetsFromKw")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getSynsetsFromKw(@QueryParam("kw") String kw, 
			@DefaultValue("10") @QueryParam("maxResults") Integer maxResults) 
	{
        if (kw == null) {
            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter 'kw'").build();
        }

		// From the saved query "US10Q1_SynsetFromKW" 
		// Find all synsets according to their labels and a given english keyword 
		// Return: ?synset-list of synsets, ?weiboCnt-number of corresponding weibos, ?labels-concatenated labels that all correspond to the keyword. 
		// Ordered by the number of weibos, maximum first
        // This query has been improved afterwards by materializing some data as:
        // - concatenated list of anchorTexts as swicicoOnto:hasSynsetAnchorTexts ?anchorTexts
        // - concatenated list of labels as swicicoOnto:hasSynsetLabels ?labels
		// - and the weibos count materialization had to be added
		String queryString = "PREFIX nlp:<http://www.websemantique.ch/onto/ontoManageNLP#> " + 
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
				"PREFIX swicicoOnto:<http://www.websemantic.ch/SWICICO/onto#> " +
				//"SELECT DISTINCT ?synset ?prefLabel ?anchorTexts ?labels (COUNT(?weibo) as ?weiboCnt) (SAMPLE(?imageURL) as ?anImageURL) (SAMPLE(?imageThumbURL) as ?anImageThumbURL) ?gloss " + 
				"SELECT DISTINCT ?synset ?prefLabel ?anchorTexts ?labels ?weiboCnt (SAMPLE(?dbPediaURI) as ?aDBPediaURI) (SAMPLE(?geonamesURI) as ?aGeonamesURI) (SAMPLE(?imageURL) as ?anImageURL) (SAMPLE(?imageThumbURL) as ?anImageThumbURL) ?gloss " +
				"{ " + 
				//"?weibo a nlp:Text; " + 
				//"nlp:hasTextAnnotations ?ann . " + 
				//"?ann nlp:hasLookupURI ?synset . " +
				"?synset swicicoOnto:hasSynsetLabels ?labels ;" +
				"swicicoOnto:hasSynsetAnchorTexts ?anchorTexts ; " +
				"swicicoOnto:hasSynsetWeibosCnt ?weiboCnt . " +
				"OPTIONAL {?synset skos:prefLabel ?prefLabel}. " +
				"OPTIONAL {?synset swicicoOnto:gloss ?gloss}. " +
				"OPTIONAL {?synset swicicoOnto:imageURL ?imageURL}. " +				
				"OPTIONAL {?synset swicicoOnto:imageThumbURL ?imageThumbURL}. " +
				"OPTIONAL {?synset swicicoOnto:dbPediaURI_en ?dbPediaURI}. " +
				"OPTIONAL {?synset swicicoOnto:geonamesURI_en ?geonamesURI}. " +
				"FILTER regex(str(?labels), \"" + kw + "\", \"i\") " +
				"} GROUP BY ?synset ?prefLabel ?anchorTexts ?labels ?weiboCnt ?gloss " + 
				"ORDER BY DESC(?weiboCnt) " + 
				"LIMIT " + maxResults ;
        
		System.out.println("queryGetSynsetsFromKw : " + queryString) ;
		
		return runSPARQLSelectToJSONOutputStream(queryString) ;
	}
	
	/*
	 * Get more information about a synset:
	 * - number of occurences
	 * - anchor texts
	 * - prefLabel
	 * - all labels
	 */
	@GET
	@Path("/synsetBabelfyInfos")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getSynsetBabelfyInfos(@QueryParam("synsetURI") String synsetURI) 
	{
        if (synsetURI == null) {
            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter 'synsetURI'").build();
        }

        String synsetUriBrackets = "<" + synsetURI + ">" ;
        
        //rem: there should be only one prefLabel
        // and there might be no label
		String queryString = "PREFIX nlp:<http://www.websemantique.ch/onto/ontoManageNLP#> " + 
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
				"PREFIX swicicoOnto:<http://www.websemantic.ch/SWICICO/onto#> " +
				"SELECT (GROUP_CONCAT(DISTINCT ?prefLabel;separator=\"; \") as ?prefLabels)  (COUNT(?weibo) as ?weiboCnt)  (GROUP_CONCAT(DISTINCT ?anchorText;separator=\"; \") as ?anchorTexts) (GROUP_CONCAT(DISTINCT ?label;separator=\"; \") as ?labels) " + 
				"{ " + 
				"?weibo a nlp:Text; " + 
				"nlp:hasTextAnnotations ?ann . " + 
				"?ann nlp:hasLookupURI " + synsetUriBrackets + "; " +
				"nlp:hasAnchorText ?anchorText ." + 
				"OPTIONAL {"+synsetUriBrackets + " rdfs:label ?label} ." +
				"OPTIONAL {"+synsetUriBrackets +" skos:prefLabel ?prefLabel}. " +
				"} " ; 

		System.out.println("queryGetSynsetBabelfyInfos : " + queryString) ;
		
		return runSPARQLSelectToJSONOutputStream(queryString) ;
	}	
	
	/*
	 * Get some weibos for a specific synset: synsetURI
	 * - weibos are ordered by a ranking property
	 * which is a value that was exporter from the DB, a sum of:reposts_count comments_count likes_count
	 * 
	 * A reference synset can be specified in: referenceURI
	 * 	This is the case when the synsetURI is a co-occurence, and we want weibos that contain the two synsets
	 * 
	 * Don't take weibos from users living in Switzerland
	 * 
	 * An ordering is done on a materialized value: swicicoOnto:weiboRanking ?ranking
	 * 	wich is a sum of reposts_count+comments_count+likes_count
	 * 
	 * Return:
	 * - a Group_Concat of the weibos'ID, which can be further used in a SELECT IN query
	 * - a Group_Concat of the anchor texts, in the same order as the weibos'ID, for the words visual highlight in the display
	 */
	@GET
	@Path("/synsetWeibosList")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getSynsetWeibosList(@QueryParam("synsetURI") String synsetURI,
			@QueryParam("referenceURI") String referenceURI) 
	{
        if (synsetURI == null) {
            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter 'synsetURI'").build();
        }

        String synsetUriBrackets = "<" + synsetURI + ">" ;
        
        // The subquery was needed to do the group_concat
        // The ordering of the weibo_id list should correspond to the ordering of the anchorTexts list
        // as the anchor texts are mandatory (could not be empty for some annotations)
        // -> to be validated that this ordering does work
        
		String queryString = "PREFIX nlp:<http://www.websemantique.ch/onto/ontoManageNLP#> " + 
				"PREFIX swicicoOnto:<http://www.websemantic.ch/SWICICO/onto#> " +
				"SELECT (GROUP_CONCAT(?weibo_id;separator=\",\") as ?weibo_id_list) (GROUP_CONCAT(?anchorTexts;separator=\"/\") as ?anchorTexts_list)  " + 
				"{ " + 
				"SELECT ?weibo_id (GROUP_CONCAT(DISTINCT ?anchorText;separator=';') as ?anchorTexts) " +
				"{" +
				"?ann nlp:hasLookupURI " + synsetUriBrackets + " ; " +
				"nlp:hasAnchorText ?anchorText . " ;
		
		if (referenceURI == null)
			queryString += "?weibo nlp:hasTextAnnotations ?ann . " ;
		else // co-occurence
			queryString += "?weibo nlp:hasTextAnnotations ?ann, ?annRef . " +
					"?annRef nlp:hasLookupURI <" +  referenceURI + "> . " ;
		
		queryString += "?weibo swicicoOnto:fromUserLivingInSwitzerland false ; " +
				"nlp:hasDBKey ?weibo_id ; " + 
				"swicicoOnto:weiboRanking ?ranking . " + 
				"} GROUP BY ?weibo_id " +
				"ORDER BY DESC(?ranking) DESC(?weibo_id) " + 
				"LIMIT 7 " +
				"}" ;
		
		System.out.println("query getSynsetWeibosList : " + queryString) ;
		
		return runSPARQLSelectToJSONOutputStream(queryString) ;
		/*
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("ClassNotFoundException Exception for jdbc.Driver: " + e.getMessage()).build();		
		} catch (SQLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("SQL Exception: " + e.getMessage()).build();		
		}*/
	}
	
	/*
	 * Get synsets related to a specific one
	 * 	i.e. the synsets found in the same weibos as a specific one
	 * 
	 * For the demonstrator: the code called to display the graph of a selected Synset in the list
	 * 
	 * Order them by the number of messages where both synsets are found together
	 * 
	 * For the main label, this query returns the skos:prefLabel, 
	 * 	a main label for each synset (coming from BabelNet synset.getMainSense())
	 * 
	 * AnchorTexts: 
	 * 	Aggregating the anchorTexts during this query made the query to slow
	 *  But now that anchorTexts have been materialized, it works fine
	 * 
	 * Filters
	 *   Filters are optional
	 *   They can be combined
	 *   
	 *   Except Year, months, tsStart+tsEnd -> they can not be combined and the priority is in that order
	 *   Only one of those set of parameters can be used (to filter on the dates)
	 *
	 *   Year
	 *  	Only from weibos of a specific year
	 *  	parameter name: year
	 *  	The year of the weibos, currently in the store: 2013, 2014, 2015
	 *  	Example: http://localhost:8081/SwicicoServer/Search/relatedSynsets?synsetURI=http://babelnet.org/rdf/s00082104n&maxResults=3&year=2014
	 *  
	 *   Months
	 *   	a comma separated list of months (numeric value from 1 to 12), as: "1,2,3" 
	 *     
	 *   TimeStamp range
	 *   	in between two dates
	 *      values are xsd:dateTime of the form: YYYY-MM-DDTHH:MM:SS
	 *      parameter name: tsStart and tsEnd
	 *   	tsStart: "2013-01-01T00:00:00"
	 * 		tsEnd: "2013-12-31T23:59:59"
	 *  	Example: http://localhost:8081/SwicicoServer/Search/relatedSynsets?synsetURI=http://babelnet.org/rdf/s00082104n&maxResults=3&tsStart=2013-10-01T00:00:00&tsEnd=2013-10-31T23:59:59
	 * 
	 * 	 FromUsersInSwitzerland
	 * 		take into account only the weibos sent from users living in Switzerland or not living in Switzerland
	 * 		parameter name: fromUsersInCH
	 * 		value: true/false
	 * 		Example: http://localhost:8081/SwicicoServer/Search/relatedSynsets?synsetURI=http://babelnet.org/rdf/s00082104n&maxResults=3&fromUsersInCH=true
	 * 
	 * Example with all parameters
	 * 
	 * SynsetURI, without brackets, as "http://babelnet.org/rdf/s00082104n"
	 */
	@GET
	@Path("/relatedSynsets")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getRelatedSynsets(@QueryParam("synsetURI") String synsetURI,
			@QueryParam("year") String yearParam,
			@QueryParam("months") String months,
			@QueryParam("tsStart") String tsStart,
			@QueryParam("tsEnd") String tsEnd,
			@QueryParam("fromUsersInCH") Boolean fromUsersInCH,
			@DefaultValue("0") @QueryParam("offset") Integer offset,
			@DefaultValue("10") @QueryParam("maxResults") Integer maxResults) 
	{
		// parameters check
        if (synsetURI == null) 
            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter 'synsetURI'").build();

        if ((tsStart != null && tsEnd == null) || (tsStart == null && tsEnd != null)) 
            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter: only one of tsStart or tsEnd has been specified. Both are needed for the timestamp range.").build();

		// From the saved query "US10Q2_relatedSynsets" 
		// all BabelSynsets in same messages as a specific BabelSynset
		// But don't return that specific one
		// Also get the number of messages for each synset
		// to be tried with a subquery too
		String queryString = "PREFIX nlp:<http://www.websemantique.ch/onto/ontoManageNLP#> " + 
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
				"PREFIX swicicoOnto:<http://www.websemantic.ch/SWICICO/onto#> " +
				"SELECT ?synset ?label (SAMPLE(?dbPediaURI) as ?aDBPediaURI) (SAMPLE(?geonamesURI) as ?aGeonamesURI) (SAMPLE(?imageURL) as ?anImageURL) (SAMPLE(?imageThumbURL) as ?anImageThumbURL) (COUNT(DISTINCT ?weibo) as ?weiboCnt) ?anchorTexts ?gloss " + 
				"{ " + 
				"?weibo nlp:hasTextAnnotations ?ann1, ?ann2 . " ; 
				
		if (yearParam != null) // specific year
			queryString += "?weibo swicicoOnto:weiboYear " + yearParam + " . ";
		else if (months != null) // months: comma separated list of months
		{
			queryString += "?weibo swicicoOnto:weiboMonth ?month ." +
					"FILTER (?month IN(" + months + ")) ." ;
		}
		else if(tsStart != null) // timestamp range
			{
			queryString += "?weibo swicicoOnto:weiboDateTime ?ts ." +
					"FILTER (?ts >= \"" + tsStart + "\"^^xsd:dateTime && ?ts <= \"" + tsEnd + "\"^^xsd:dateTime) ." ;
			}
			
		if (fromUsersInCH != null)
			queryString += "?weibo swicicoOnto:fromUserLivingInSwitzerland " + fromUsersInCH + " . ";
			
		queryString +=	"?ann1 nlp:hasLookupURI <"+ synsetURI +"> . " + 
				"?ann2 nlp:hasLookupURI ?synset ." +
				"OPTIONAL {?synset skos:prefLabel ?label}. " +
				"OPTIONAL {?synset swicicoOnto:gloss ?gloss}. " +
				"OPTIONAL {?synset swicicoOnto:imageURL ?imageURL}. " +				
				"OPTIONAL {?synset swicicoOnto:imageThumbURL ?imageThumbURL}. " +
				"OPTIONAL {?synset swicicoOnto:dbPediaURI_en ?dbPediaURI}. " +
				"OPTIONAL {?synset swicicoOnto:geonamesURI_en ?geonamesURI}. " +
				"OPTIONAL {?synset swicicoOnto:hasSynsetAnchorTexts ?anchorTexts}. " +
				"FILTER (?synset != <"+ synsetURI +">) " + 
				"} GROUP BY ?synset ?label  ?gloss ?anchorTexts " + 
				"ORDER BY DESC(?weiboCnt) " + 
				"OFFSET " + offset +
				" LIMIT " + maxResults ;

		System.out.println("query getRelatedSynsets : " + queryString) ;
		
		return runSPARQLSelectToJSONOutputStream(queryString) ;
	}
	
	/*
	 * Related Synsets - Trends
	 * For a specific synset, retrieve 5 related synsets by year: 2013, 2014, 2015
	 */
	@GET
	@Path("/relatedSynsetsByYear")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getRelatedSynsetsByYear(@QueryParam("synsetURI") String synsetURI,
			@QueryParam("fromUsersInCH") Boolean fromUsersInCH,
			@DefaultValue("0") @QueryParam("offset") Integer offset,
			@DefaultValue("5") @QueryParam("maxResults") Integer maxResults) 
	{
        if (synsetURI == null) 
        {
            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter 'synsetURI'").build();
        }
        
		// From the saved query "US10_Q4_relatedSynsets_byYear" 
        // One UNION per year
        // Prepare the 'subQuery', for the UNION
        // in which the <year> string value will be replaced for each year
        
        String subQuery = "SELECT ?synset ?year ?label (SAMPLE(?dbPediaURI) as ?aDBPediaURI) (SAMPLE(?geonamesURI) as ?aGeonamesURI) (SAMPLE(?imageURL) as ?anImageURL) (SAMPLE(?imageThumbURL) as ?anImageThumbURL) (COUNT(DISTINCT ?weibo) as ?weiboCnt) ?gloss " +
        		"{" +
        		"?weibo nlp:hasTextAnnotations ?ann1, ?ann2 . " ;
        
		if (fromUsersInCH != null)
			subQuery += "?weibo swicicoOnto:fromUserLivingInSwitzerland " + fromUsersInCH + " . ";

		subQuery +=	"?weibo swicicoOnto:weiboYear <year>, ?year . " + 
        		"?ann1 nlp:hasLookupURI <" + synsetURI + "> .  " + 
        		"?ann2 nlp:hasLookupURI ?synset.  " + 
        		"OPTIONAL {?synset skos:prefLabel ?label}. " +
				"OPTIONAL {?synset swicicoOnto:gloss ?gloss}. " +
				"OPTIONAL {?synset swicicoOnto:imageURL ?imageURL}. " +				
				"OPTIONAL {?synset swicicoOnto:imageThumbURL ?imageThumbURL}. " +
				"OPTIONAL {?synset swicicoOnto:dbPediaURI_en ?dbPediaURI}. " +
				"OPTIONAL {?synset swicicoOnto:geonamesURI_en ?geonamesURI}. " +
        		"FILTER (?synset != <"+ synsetURI +">)  " + 
        		"} GROUP BY ?synset ?label ?year ?gloss " + 
				"ORDER BY DESC(?weiboCnt) " + 
				"OFFSET " + offset +
				" LIMIT " + maxResults ;
        
		String queryString = "PREFIX nlp:<http://www.websemantique.ch/onto/ontoManageNLP#> " + 
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
				"PREFIX swicicoOnto:<http://www.websemantic.ch/SWICICO/onto#> " +
				"SELECT ?synset ?year ?label ?aDBPediaURI ?aGeonamesURI ?anImageURL ?anImageThumbURL ?weiboCnt ?gloss " + 
				"{ " + 
				"{ " + subQuery.replace("<year>", "2013") + "}" +
				"UNION " +
				"{ " + subQuery.replace("<year>", "2014") + "}" +
				"UNION " +
				"{ " + subQuery.replace("<year>", "2015") + "}" +
				"} ORDER BY ?year DESC(?weiboCnt)" ;
				
		System.out.println("query : " + queryString) ;
		
		return runSPARQLSelectToJSONOutputStream(queryString) ;
	}
	/*
	 * Call ontoFuse to run a SPARQL select query, get the results as a JSON String sent back in a Response object
	 */
	private Response runSPARQLSelectToJSONOutputStream(String query)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream() ;

		try (OntoFuse ontoFuse = new OntoFuse(SesameEndPoint, SesameRep))
		{
			ontoFuse.initialize();

			ontoFuse.runSPARQLSelectToJSONOutputStream(repoSrc.SESAM, query, out);
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Exception: " + e.getMessage()).build();
		}
		
		try {
			// return Response.status(200).entity(out.toString()).build();
			return Response.status(200).entity(out.toString("utf-8")).build();
		} catch (UnsupportedEncodingException e) 
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Exception - can't send back result in utf-8: " + e.getMessage()).build();		
		} finally
		{
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
