package de.citec.sc.matoll.preprocessor;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import de.citec.sc.matoll.core.Language;
import de.citec.sc.matoll.coreference.Coreference;
import java.util.logging.Level;
import java.util.logging.Logger;


import de.citec.sc.matoll.process.Matoll;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class ModelPreprocessor {
	
	private static Logger logger = LogManager.getLogger(Matoll.class.getName());

	HashMap<String,String> Resource2Lemma;
	HashMap<String,String> Resource2Head;
	HashMap<String,String> Resource2Dependency;
	
	HashMap<Integer,String> Int2NodeMapping;
	HashMap<String,Integer> Node2IntMapping;
	
	HashMap<String,String> senseArgs;

    
        boolean     doCoref;
        Coreference coreference = new Coreference();
	
        Language    language;
        
	Set<String> POS;

	
	public ModelPreprocessor(Language language)
	{
                this.language = language; 
		POS = new HashSet<String>();
                doCoref = false;
	}
	        
	/**
         * @param model
         * @param subjectEntity
         * @param objectEntity 
         */
	public void preprocess(Model model, String subjectEntity,
			String objectEntity) {
		//logger.info("preprocess with subjEnt "+subjectEntity+"; objEnt "+objectEntity+"\n");
		//System.out.println("preprocess with subjEnt "+subjectEntity+"; objEnt "+objectEntity);
		Resource2Lemma = getResource2Lemma(model);
		Resource2Head = getResource2Head(model);
		Resource2Dependency = getResource2Dependency(model);
		
		Int2NodeMapping = new HashMap<Integer,String>();
		Node2IntMapping = new HashMap<String,Integer>();
		
		senseArgs = new HashMap<String,String>();
		
		getMapings(Int2NodeMapping,Node2IntMapping,model);
		
		List<Hypothesis> hypotheses;
		
		String root;
		
		if (objectEntity != null)
		{
			//System.out.println("objectEntity not empty!!!");
			List<List<String>> objectResources = getResources(model,objectEntity);
			hypotheses = getHypotheses(objectResources);
			
			for (Hypothesis hypo: hypotheses)
			{
				//System.out.print("Final hypo: "+hypo.toString());
				
				root = hypo.checkValidAndReturnRoot(Resource2Head,Resource2Dependency,POS);
				
				if (root != null) 
				{
					model.add(model.getResource(root), model.createProperty("own:senseArg"), model.createResource("http://lemon-model.net/lemon#objOfProp"));
					senseArgs.put(root, "http://lemon-model.net/lemon#objOfProp");
					logger.info("added senseArg "+root+" *** objOfProp\n");
				}
			}
		}
		
		if (subjectEntity != null)
		{
			//System.out.println("subjectEntity not empty!!!");
			List<List<String>> subjectResources = getResources(model,subjectEntity);
			hypotheses = getHypotheses(subjectResources);
			for (Hypothesis hypo: hypotheses)
			{
				
				//System.out.println("Final hypo: "+hypo.toString());
				
				root = hypo.checkValidAndReturnRoot(Resource2Head,Resource2Dependency,POS);
				
				if (root != null) 
				{	
					model.add(model.getResource(root), model.createProperty("own:senseArg"), model.createResource("http://lemon-model.net/lemon#subjOfProp"));
					senseArgs.put(root, "http://lemon-model.net/lemon#subjOfProp");
					//logger.info("added senseArg "+root+" *** subjOfProp\n");
					//System.out.println("added senseArg "+root+" *** subjOfProp\n");
				}	
			}
		}
		
                if (doCoref) try {
                    coreference.computeCoreference(model,language);
                } catch (Exception ex) {
                    Logger.getLogger(ModelPreprocessor.class.getName()).log(Level.SEVERE, null, ex);
                }
				
	}
        /**
         * 
         * @param int2NodeMapping
         * @param node2IntMapping
         * @param model 
         */
	private void getMapings(HashMap<Integer, String> int2NodeMapping,
			HashMap<String, Integer> node2IntMapping, Model model) {
		
		StmtIterator iter;
		
		Statement stmt;
		
		String node;
		
		String number;
		
		iter = model.listStatements(null,model.getProperty("conll:wordnumber"), (RDFNode) null);
		
		while (iter.hasNext()) {
					
			stmt = iter.next();
			
			node = stmt.getSubject().toString();
			number = stmt.getObject().toString();
			
			int2NodeMapping.put(new Integer(number), node);
			node2IntMapping.put(node, new Integer(number));
		}
		
	}

        /**
         * 
         * @param resources
         * @return 
         */
	private List<Hypothesis> getHypotheses(List<List<String>> resources) {
		
		List<Hypothesis> hypotheses = new ArrayList<Hypothesis>();
		
		List<Hypothesis> expanded_hypotheses;
		
		hypotheses.add(new Hypothesis());
				
		for (List<String> nodes: resources)
		{
			//System.out.print("Checking nodes: "+nodes+"\n");
			
			expanded_hypotheses = new ArrayList<Hypothesis>();
			
			
			for (Hypothesis hypo: hypotheses)
			{
				//System.out.print("Expanding: "+hypo.toString());
				
				for (Hypothesis hypot: hypo.expand(nodes))
				{
			
					//System.out.print("Adding: "+hypot.toString());
					expanded_hypotheses.add(hypot);
				}
				
			}
			
			hypotheses = expanded_hypotheses;
		}
		
		return hypotheses;
		
	}
        /**
         * 
         * @param model
         * @param string
         * @return 
         */
	private List<List<String>> getResources(Model model,
			String string) {
		//System.out.println("getting resources for "+string);
		String[] tokens;
		if (language.equals("JA")) {
			Analyzer analyzer = new JapaneseAnalyzer();
			List<String> result = new ArrayList<String>();
			try {
			      TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
			      stream.reset();
			      while (stream.incrementToken()) {
			        result.add(stream.getAttribute(CharTermAttribute.class).toString());
			        //System.out.println("next token: "+result.get(result.size()-1));
			      }
			      stream.end();
			      stream.close();
			    } catch (IOException e) {
			      // not thrown b/c we're using a string reader...
			      throw new RuntimeException(e);
			    }
			analyzer.close();
			tokens = new String[result.size()];
			for (int i=0;i<result.size();i++) {
				tokens[i] = result.get(i);
			}
			
		}
		else { 
			tokens = string.split(" ");
		}
		
		ArrayList<List<String>> resourceList = new ArrayList<List<String>>();
		
		ArrayList<String> wordResources;
		
		StmtIterator iter;
		
		Statement stmt;
		
		for (int i=0; i < tokens.length; i++)
		{
			wordResources = new ArrayList<String>();
			
			iter = model.listStatements(null,model.getProperty("conll:form"), (RDFNode) null);
			//System.out.println("tokens[i] is now "+tokens[i]);
		
			while (iter.hasNext()) {
						
				stmt = iter.next();
				//System.out.println("stmt is now "+stmt);
				if (stmt.getObject().toString().equals(tokens[i]))
				{
					wordResources.add(stmt.getSubject().toString());
					//System.out.println(stmt.getSubject().toString()+" has form "+stmt.getObject().toString());
				}
				
			}
			
			resourceList.add(wordResources);
							
		}	

		 return resourceList;
	}
        /**
         * 
         * @param model
         * @return 
         */
	private HashMap<String, String> getResource2Dependency(Model model) {
		
		HashMap<String,String> resource2Dep = new HashMap<String,String>();
		
		StmtIterator iter;
		
		Statement stmt;
		
		iter = model.listStatements(null,model.getProperty("conll:deprel"), (RDFNode) null);
		
		while (iter.hasNext()) {
					
			stmt = iter.next();
			
			resource2Dep.put(stmt.getSubject().toString(), stmt.getObject().toString());
			
			// System.out.println(stmt.getSubject().toString()+" has dependency "+stmt.getObject().toString());
		
			
		}
		
		return resource2Dep;
	   
	}
/**
 * 
 * @param model
 * @return 
 */
	private HashMap<String, String> getResource2Head(Model model) {
		
		HashMap<String,String> resource2Head = new HashMap<String,String>();
		
		StmtIterator iter;
		
		Statement stmt;
		
		iter = model.listStatements(null,model.getProperty("conll:head"), (RDFNode) null);
		
		while (iter.hasNext()) {
					
			stmt = iter.next();
			
			resource2Head.put(stmt.getSubject().toString(), stmt.getObject().toString());
			
			// System.out.println(stmt.getSubject().toString()+" has head "+stmt.getObject().toString());
						
		}
		
		return resource2Head;
	}
        /**
         * 
         * @param model
         * @return 
         */
	private HashMap<String, String> getResource2Lemma(Model model) {
		
		HashMap<String,String> resource2Lemma = new HashMap<String,String>();
		
		StmtIterator iter;
		
		Statement stmt;
		
		iter = model.listStatements(null,model.getProperty("conll:form"), (RDFNode) null);
		
		while (iter.hasNext()) {
					
			stmt = iter.next();
			
			resource2Lemma.put(stmt.getSubject().toString(), stmt.getObject().toString());
			
			// System.out.println(stmt.getSubject().toString()+" has lemma "+stmt.getObject().toString());
			
		}
		
		return resource2Lemma;
	}

	public void setCoreferenceResolution(boolean b) {
		doCoref = b;	
	}

	public void setPOS(Set<String> pos) {
		POS = pos;		
	}
        
        public void setLanguage(Language l) {
               language = l;
        }

}
