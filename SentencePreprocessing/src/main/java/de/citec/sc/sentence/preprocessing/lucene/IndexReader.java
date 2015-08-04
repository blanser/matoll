package de.citec.sc.sentence.preprocessing.lucene;



import de.citec.sc.sentence.preprocessing.process.Language;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;


public class IndexReader {
	private static String pathIndex = "";
	/* TODO make class more flexible w.r.t. type of analyzer being used */
	private static Analyzer analyzer;
	//private static MMapDirectory index;
	private static DirectoryReader indexReader;
    private static IndexSearcher searcher;
    private static Language language;
	
	public IndexReader(String pathToIndex, Language language) throws IOException{
		IndexReader.pathIndex = pathToIndex;
		IndexReader.language = language;
		if (language.equals(Language.JA)) {
			IndexReader.analyzer = new JapaneseAnalyzer();
		} else {
			IndexReader.analyzer = new StandardAnalyzer(Version.LATEST);
		}
		indexReader = DirectoryReader.open(FSDirectory.open(new File(pathToIndex)));
		searcher = new IndexSearcher(indexReader);
		//IndexReader.index = new MMapDirectory(new File(IndexReader.pathIndex));
	}
	
	
	private List<List<String>> runSearch(String subj, String obj,int sentence_lenght)
			throws IOException {
		Set<String> cache = new HashSet<String>();
		List<List<String>> results = new ArrayList<List<String>>();
		try {
			
			//Generate Boolean query out of term
			BooleanQuery booleanQuery = new BooleanQuery();
			//System.out.println("before preprocessing: "+subj+" *** "+obj);
			subj = preprocessing(subj);
			obj = preprocessing(obj);
			if (!IndexReader.language.equals(Language.JA)) {
				if(subj.length()<=2||obj.length()<=2) return results;
			}
			String term = subj+" "+obj;
			if (IndexReader.language.equals(Language.JA)) {
				QueryParser queryParser = new QueryParser("sentence", IndexReader.analyzer);
				queryParser.setDefaultOperator(QueryParser.Operator.AND);
				try {
					// TODO JapaneseAnalyzer removes stop words by default; leave it that way?
					if (queryParser.parse(subj).toString().length()==0 || queryParser.parse(obj).toString().length()==0) return results;
					booleanQuery.add(queryParser.parse(term), BooleanClause.Occur.MUST);
					System.out.println("parse term: "+booleanQuery);
				} catch (Exception e) {
					System.err.println("Problem with "+subj+" *** "+obj);
				}
			} else {
				String[] tmp = term.split(" ");
				//or/and/not has to be checked here, otherwise I would for example remove the or from order, or notice etc
				for (String x : tmp){
					if(!x.equals("")&&!x.toLowerCase().equals("or")&&!x.toLowerCase().equals("and")&&!x.toLowerCase().equals("not")&&x.length()>2){
						try{
							booleanQuery.add(new QueryParser("sentence", analyzer).parse(x), BooleanClause.Occur.MUST);
						}
						catch(Exception e){
							System.err.println("Problem with "+x);
						}
					}
				}
			}
		    //int hitsPerPage = 1000;
                    int hitsPerPage = 100;
		    
		    
	        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
	        searcher.search(booleanQuery, collector);
	        
	        ScoreDoc[] hits = collector.topDocs().scoreDocs;

	        for(int i=0;i<hits.length;++i) {
	          int docId = hits[i].doc;
	          Document d = searcher.doc(docId);
	          ArrayList<String> result = new ArrayList<>();
	          String sentence = d.get("sentence");
	          if((sentence.split("\t\t")).length<=sentence_lenght){
	        	  if(!cache.contains(sentence)){
	        		  result.add(sentence);
			          result.add(subj);
			          result.add(obj);
			          results.add(result);
			          cache.add(sentence);
	        	  }
	        	  
	          }
	          
	        }
		}
		catch(Exception e){
			//System.out.println("Error in term: "+subj+" "+obj);
			//e.printStackTrace();
			//System.out.println();
			//indexReader.close();
			//return results_fail;
		}
		
		return results;
	}
	
	
	private String preprocessing(String term) {
		term = term.replace("/","");
		term = term.replace(":","");
		term = term.replace("!","");
		term = term.replace("\"","");
		term = term.replace("+","");
		term = term.replace("","");
		// hyphen seems to affect output of QueryParser
		// not sure if this may affect e.g. processing of dates
		// for other languages
		if (language.equals(Language.JA)) term = term.replace("-", "");
		// JapaneseAnalyzer does not like stars
		if (language.equals(Language.JA)) term = term.replace("*", "");
		return term;
	}


	public List<List<String>> search(List<List<String>> entities){
		List<List<String>> sentences = new ArrayList<List<String>>();
                Set<String> unique_sentence = new HashSet<String>();
		
		for(List<String> entity : entities){
			//System.out.println("entity1: "+entity.get(0)+" *** entity2: "+entity.get(1));
			try {
				for(List<String> sentence_item : this.runSearch(entity.get(0), entity.get(1), 60)){
                                    if(!unique_sentence.contains(sentence_item.get(0))){
                                        sentences.add(sentence_item);
                                        unique_sentence.add(sentence_item.get(0));
                                    }
                                }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println(Integer.toString(sentences.size())+" #sentences");
		return sentences;
	}

}
