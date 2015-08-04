package de.citec.sc.sentence.preprocessing.sparql;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class Sparql {

	public static List<String> retrieveEntities(String endpoint, String uri, String language){
		List<String> entities = new ArrayList<String>();
		/*
		 * If label is given, this is the case for the dbpedia object property
		 */
		entities.addAll(getValues(endpoint, getQueryLabel(language,uri),language));
		
		/*
		 * if no label is found try to get everything from the right side, e.g. if datatype property is given
		 */
		if (entities.size()==0){
			entities.addAll(getValues(endpoint, getQueryData(language,uri),language));
		}
		return entities;
		
	}
	
	private static String getQueryLabel(String language, String uri){
		/*String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT DISTINCT ?y ?subj ?obj ?x WHERE {"
				+ "?y <"+uri+"> ?x." 
				+ "?y rdfs:label ?subj." 
				+ "FILTER ((lang(?subj) = '"+language+"') ||"
				+ "(lang(?subj) = 'en' && "
				+ "NOT EXISTS {?y rdfs:label ?osubj. FILTER (lang(?osubj) = '"+language+"' )} )) "
				+ "?x rdfs:label ?obj."
				+ "FILTER ((lang(?obj) = 'ja') ||"
				+ "(lang(?obj) = 'en' && "
				+"NOT EXISTS {?x rdfs:label ?oobj. FILTER (lang(?oobj) = '"+language+"') } ))}";*/
                
                                /*
                                UNION in SPARQL is leftbound
                                 */
            
                String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT DISTINCT ?y ?subj ?obj ?x WHERE {"
				+ "?y <"+uri+"> ?x." 
				+ "{?y rdfs:label ?subj. FILTER (lang(?subj) = '"+language+"')} UNION" 
                                + "{?y rdfs:label ?subj. FILTER (lang(?subj) = 'en')}" 
                                + "{?x rdfs:label ?obj. FILTER (lang(?obj) = '"+language+"')} UNION" 
                                + "{?x rdfs:label ?obj. FILTER (lang(?obj) = 'en')}" 
				+ "}";
		return query;
		
			
	}
	
	private static String getQueryData(String language, String uri){
//		String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
//				+ "SELECT DISTINCT ?y ?subj ?obj WHERE "
//				+ "{?y <"+uri+"> ?obj. "
//				+ "?y rdfs:label ?subj. FILTER ((lang(?subj) = '"+language+"') ||"
//				+"(lang(?subj) = 'en' && NOT EXISTS {?y rdfs:label ?osubj. FILTER (lang(?osubj) = '"+language+"')} ))"
//				+ "}";
                String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT DISTINCT ?y ?subj ?obj WHERE "
				+ "{?y <"+uri+"> ?obj. "
                                + "{?y rdfs:label ?subj. FILTER (lang(?subj) = '"+language+"')} UNION" 
                                + "{?y rdfs:label ?subj. FILTER (lang(?subj) = 'en')}" 
				+ "}";
		return query;
	}
	
	private static List<String> getValues(String endpoint, String queryString, String language){
		System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		List<String> entities = new ArrayList<String>();
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
		try {
		    ResultSet results = qexec.execSelect();
		    while ( results.hasNext() ) {
	        	 QuerySolution qs = results.next();
	        	 try{
	        			String subj = qs.get("?subj").toString();
	        			String subj_uri = qs.get("?y").toString();
	        			String obj_uri = "";
	        			try{
	        				obj_uri = qs.get("?x").toString();
	        			}
	        			catch(Exception e){
	        				obj_uri = qs.get("?obj").toString();
	        				if (language.equals("ja")) {
	        					obj_uri = obj_uri.replace("\n", "");
	        				}
	        			}
	              		String obj = qs.get("?obj").toString();
	              		if (language.equals("ja")) { 
	              			obj = obj.replace("\n", "");
	              		}
	              		System.err.println("?obj: "+obj+" *** ?obj_uri: "+obj_uri);
	              		String entityPair = subj_uri+"\t"+subj+"\t"+obj+"\t"+obj_uri;
	              		entityPair = entityPair.replace("@en", "");
	              		entityPair = entityPair.replace("@"+language, "");
	              		//System.out.println("entityPair:"+entityPair);
	              		entities.add(entityPair);
	        	 }
	        	 catch(Exception e){
		     	    	e.printStackTrace();
		        		 //ignore those without Frequency TODO:Check Source of Error
		     	 }
		    }
		}
		 catch(Exception e){
  	    	e.printStackTrace();
     		 //ignore those without Frequency TODO:Check Source of Error
		 }
		
		return entities;
	}
	
}
