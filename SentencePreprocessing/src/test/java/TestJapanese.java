import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import de.citec.sc.sentence.preprosessing.lucene.IndexReader;
import de.citec.sc.sentence.preprosessing.process.OntologyImporter;


public class TestJapanese {
	// right now this only serves to RDF example sentences without having to stick to any specific set of properties
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 */
	/*public static void main(String[] args) throws IOException, ParseException {
		//System.setOut(new PrintStream(new FileOutputStream("output")));
		//IndexCreator index = new IndexCreator();
		org.apache.log4j.BasicConfigurator.configure();
		String pathToIndex = "/home/bettina/Dokumente/jawikiExtracted/AA/AA_index02";
		//String pathToIndex = "/home/bettina/jaIdealSents/idealSents_index";
		String pathToSentences = "/home/bettina/Dokumente/jawikiExtracted/output_perl_mecab_jdepp_rmvPunct_CoNLLU";
		//String pathToSentences = "/home/bettina/jaIdealSents/idealSents_mecab_jdepp_rmvPunct_CoNLLU";
		
		HashMap<String, String> cache = new HashMap<String, String>();
		List<List<String>> results = new ArrayList<List<String>>();
		
		index(pathToIndex, pathToSentences);
		
		String folderToSaveResourcesSentences = "/home/bettina/Dokumente/jawikiExtracted/AA/AA_resources";
		//String folderToSaveResourcesSentences = "/home/bettina/jaIdealSents/idealSents_resources";
		String language = "ja";
		
		//IndexReader indexReader = new IndexReader(pathToIndex,language);
		
		if(!folderToSaveResourcesSentences.endsWith("/")) folderToSaveResourcesSentences+="/";
		
		String pathWriteSentences = folderToSaveResourcesSentences+"Sentences02/";
		
		if(checkFolder(pathWriteSentences)){
			DirectoryReader dirReader = DirectoryReader.open(FSDirectory.open(new File(pathToIndex)));
			IndexSearcher searcher = new IndexSearcher(dirReader);
			int hitsPerPage = 10000;
			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
			BooleanQuery booleanQuery = new BooleanQuery();
			//Analyzer analyzer = new JapaneseAnalyzer();
			QueryParser queryParser = new QueryParser("sentence", new JapaneseAnalyzer());
			booleanQuery.add(queryParser.parse("1"), BooleanClause.Occur.MUST);
			System.out.print("collecting sentences...");
	        searcher.search(booleanQuery, collector);
	        System.out.println("done");
	        
	        ScoreDoc[] hits = collector.topDocs().scoreDocs;
	        
	        System.out.print("adding sentences to ArrayList...");
	        for(int i=0;i<hits.length;++i) {
		          int docId = hits[i].doc;
		          Document d = searcher.doc(docId);
		          ArrayList<String> result = new ArrayList<String>();
		          String sentence = d.get("sentence");
		          //if (i==0) System.out.println("first sentence: "+sentence);
		          //if (i==1) System.out.println("second sentence: "+sentence);
		          System.out.println("sentence number "+i+": "+sentence);
	        	  if(!cache.containsKey(sentence)){
	        		  result.add(sentence);
	        		  result.add("");
			          result.add("");
			          results.add(result);
			          cache.put(sentence, "");
	        	  }
		        	  
	        }
	        /*System.out.println("first sentence in Array List: ");
	        for (int i=0;i<results.get(0).size();i++) {
	        	System.out.println(results.get(0).get(i));
	        }*/
	        /*System.out.println("collected "+results.size()+" sentences");
	        System.out.println("done");
	        System.out.print("writing model...");
	        de.citec.sc.sentence.preprosessing.rdf.RDF.writeModel(results, pathWriteSentences, language, "");
	        System.out.println("done");
	        
	        
		}
	}
	
	private static boolean checkFolder(String path) {
		File f = new File(path);
		if(f.exists()){
			return true;
		}
		else{
			File newDir = new File(path);
			try{
				newDir.mkdir();
				return true;
			}
			catch(Exception e){
				System.err.println("Could not create "+path);
				return false;
			}
		}
	}
	
	public static void index(String path_to_index,String path_to_text) throws IOException {
		System.out.println("Start index generation");
		JapaneseAnalyzer analyzer = new JapaneseAnalyzer();
		MMapDirectory index = new MMapDirectory(new File(path_to_index));
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
	    IndexWriter w = new IndexWriter(index, config);
	    
	    int counter = 0;
	    InputStream in = new FileInputStream(new File(path_to_text));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        
        //tagged += string_list[0] +"\t\t"+counter+"\t\t"+id+"\t\t"+url+"\n";
        
        while ((line = reader.readLine()) != null) {
            line = line.replace("\n", "");
            Document doc = new Document();
    	    doc.add(new TextField("sentence", line, Field.Store.YES));
    	    w.addDocument(doc);
            counter +=1;
            if(counter%100000 == 0){
            	System.out.println(counter);
            	//System.out.println("current sentence: "+line);
            }
        }
        
        reader.close();
        System.out.println(Integer.toString(counter)+" Sentences added");
        System.out.println("Stop index generation");
        w.close();
        index.close();
        analyzer.close();

	}
}*/
	/* tests if sentence preprocessing works for Japanese */
	/* for the most part same as Process.java in package de.citec.sc.sentence.preprosessing.process */

	public static void main(String[] args) throws IOException {
		FileOutputStream output = new FileOutputStream("output");
		PrintStream printer = new PrintStream(output);
		//System.setOut(printer);
		System.setErr(printer);
		
		// comment out if log4j: WARN No appenders could be found... shows up
		org.apache.log4j.BasicConfigurator.configure();
		// use e.g. ../resources/properties as argument
		if (args.length == 0) {
			// TODO left out until I know how to translate English property names
			//System.out.println("Run: java TestJapanese ontology/pathToPropertyFile");
			//System.exit(1);
		}
		
		String endpointEN = "http://dbpedia.org/sparql";
		String endpointJA = "http://ja.dbpedia.org/sparql";
		String pathToPropertiesEN = "/home/bettina/GitHub/mymatoll/matoll/SentencePreprocessing/src/test/resources/properties";
		String pathToPropertiesJA = "/home/bettina/GitHub/mymatoll/matoll/SentencePreprocessing/src/test/resources/propertiesJA";
		Boolean with_sentences = true;
		/*
		 *in pathToIndex only one index for one language can be found 
		 */
		// TODO change these to relative paths
		String pathToIndex = "/home/bettina/Dokumente/jawikiExtracted/ZZAAAC/AAAC_index";
		String folderToSaveResourcesSentences = "/home/bettina/Dokumente/jawikiExtracted/ZZAAAC/AAAC_resources";
		String language = "ja";
		
		IndexReader index = new IndexReader(pathToIndex,language);
		List<List<String>> propertiesEN = new ArrayList<List<String>>();
		List<List<String>> propertiesJA = new ArrayList<List<String>>();
		List<List<String>> properties = new ArrayList<List<String>>();
		try {
			if(pathToPropertiesEN.endsWith(".owl")){
				loadOntology(pathToPropertiesEN,propertiesEN,language);
			}
			else{
				loadPropertyList(pathToPropertiesEN,propertiesEN,language);
			}
			if(pathToPropertiesJA.endsWith(".owl")){
				loadOntology(pathToPropertiesJA,propertiesJA,language);
			}
			else{
				loadPropertyList(pathToPropertiesJA,propertiesJA,language);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!folderToSaveResourcesSentences.endsWith("/")) folderToSaveResourcesSentences+="/";
		System.out.println("Retrieve Entities");
		// TODO does this work?
		de.citec.sc.sentence.preprosessing.sparql.Resources.retrieveEntities(propertiesEN, folderToSaveResourcesSentences, endpointEN);
		de.citec.sc.sentence.preprosessing.sparql.Resources.retrieveEntities(propertiesJA, folderToSaveResourcesSentences, endpointJA);
		System.out.println("Done");
		System.out.println();
		properties.addAll(propertiesEN);
		properties.addAll(propertiesJA);
		if(with_sentences){
			String pathWriteSentences = folderToSaveResourcesSentences+"Sentences/";
			if(checkFolder(pathWriteSentences)){
				for (List<String> property: properties){
					try {
						String pathToSentenceModel = pathWriteSentences+property.get(1)+"/"+property.get(2)+"/"+property.get(3)+"/"+property.get(4);
						boolean check_ontologyfolder = checkFolder(pathWriteSentences+property.get(1));
						boolean check_namespacefolder = checkFolder(pathWriteSentences+property.get(1)+"/"+property.get(2));
						boolean check_languagefolder = checkFolder(pathWriteSentences+property.get(1)+"/"+property.get(2)+"/"+property.get(3));
						boolean check_modelfolder = checkFolder(pathToSentenceModel);
						if(check_ontologyfolder&&check_languagefolder&&check_namespacefolder&&check_modelfolder){
							System.out.println("Processing:"+property.get(0));
							List<List<String>> entities = de.citec.sc.sentence.preprosessing.sparql.Resources.loadEntities(property, folderToSaveResourcesSentences);
							List<List<String>> sentences = index.search(entities);
							int value = 10000;
							if(sentences.size()<=value){
								de.citec.sc.sentence.preprosessing.rdf.RDF.writeModel(sentences, pathToSentenceModel, language, property.get(0));
							}
							else{
								int begin = 0;
								int end = 0;
								for(int i= 0; i<Math.floor((double)sentences.size()/value);i++){
									begin = i*value;
									end = begin+value;
									de.citec.sc.sentence.preprosessing.rdf.RDF.writeModel(sentences.subList(begin, end), pathToSentenceModel, language, property.get(0));
								}
								de.citec.sc.sentence.preprosessing.rdf.RDF.writeModel(sentences.subList(end, sentences.size()), pathToSentenceModel, language, property.get(0));
							}
							
							System.out.println("Done");
							System.out.println();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/* taken from Process.java */
	private static void loadPropertyList(String pathToProperties,
			List<List<String>> properties, String language) throws IOException {
		String properties_raw = "";
		/*
		 * each line contains one property
		 */
		FileInputStream inputStream = new FileInputStream(pathToProperties);
	    try {
	        properties_raw = IOUtils.toString(inputStream);
	    } finally {
	        inputStream.close();
	    }
	    
	    for(String p: properties_raw.split("\n")){
	    	String ontologyName =findOntologyName(p);
	    	String[] tmp = p.split("/");
	    	String name = tmp[tmp.length-1];
	    	String namespace = tmp[tmp.length-2];
	    	List<String> property = new ArrayList<String>();
	    	property.add(p);
	    	property.add(ontologyName);
	    	property.add(namespace);
	    	property.add(language);
	    	property.add(name);
	    	properties.add(property);
	    	//System.out.println(property.toString());
	    	
	    }
		
	}
	
	private static void loadOntology(String pathToOntology,
			List<List<String>> properties, String language) throws IOException {
		OntologyImporter importer = new OntologyImporter(pathToOntology,"RDF/XML");
	    
	    for(String p: importer.getProperties()){
	    	String ontologyName =findOntologyName(p);
	    	String[] tmp = p.split("/");
	    	String name = tmp[tmp.length-1];
	    	String namespace = tmp[tmp.length-2];
	    	List<String> property = new ArrayList<String>();
	    	property.add(p);
	    	property.add(ontologyName);
	    	property.add(namespace);
	    	property.add(language);
	    	property.add(name);
	    	properties.add(property);
	    	//System.out.println(property.toString());
	    	
	    }
		
	}
	
	private static String findOntologyName(String p) {
		 // String to be scanned to find the pattern.
	      String pattern ="^http://(\\w*).*\\W.*";
	      String ontologyName = "";
	      // Create a Pattern object
	      Pattern r = Pattern.compile(pattern);

	      // Now create matcher object.
	      Matcher m = r.matcher(p);
	      if (m.find( )) {
	         ontologyName = m.group(1);
	      } else {
	         System.out.println("NO MATCH");
	      }
		return ontologyName;
	}
	
	private static boolean checkFolder(String path) {
		File f = new File(path);
		if(f.exists()){
			return true;
		}
		else{
			File newDir = new File(path);
			try{
				newDir.mkdir();
				return true;
			}
			catch(Exception e){
				System.err.println("Could not create "+path);
				return false;
			}
		}
	}

}
