package de.citec.sc.matoll.patterns.japanese;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

import de.citec.sc.matoll.core.LexiconWithFeatures;
import de.citec.sc.matoll.patterns.SparqlPattern;

public class SparqlPattern_JA_3 extends SparqlPattern {
	
	Logger logger = LogManager.getLogger(SparqlPattern_JA_3.class.getName());
	
	String query = "";
	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void extractLexicalEntries(Model model, LexiconWithFeatures lexicon) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
