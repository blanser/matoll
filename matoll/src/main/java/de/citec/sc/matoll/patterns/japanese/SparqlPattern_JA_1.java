package de.citec.sc.matoll.patterns.japanese;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;

import de.citec.sc.bimmel.core.FeatureVector;
import de.citec.sc.matoll.core.LexiconWithFeatures;
import de.citec.sc.matoll.patterns.SparqlPattern;
import de.citec.sc.matoll.patterns.Templates;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SparqlPattern_JA_1 extends SparqlPattern {
	
	Logger logger = LogManager.getLogger(SparqlPattern_JA_1.class.getName());
	// TODO ?prep is only queried for bc NounWithPrep requires it
	String query =	"SELECT ?lemma ?prep ?e1_arg ?e2_arg WHERE {" +
					"?y <conll:cpostag> \"名詞\" ." +
					"?y <conll:form> ?lemma ." +
					"?y <conll:head> ?py ." + 
					"?py <conll:postag> \"係助詞\" ." +
					"?py <conll:form> \"は\" ." +
					"?py <conll:head> ?cop2 ." +
					"?e1 <conll:head> ?prep ." +
					"?e1 <conll:cpostag> \"名詞\" ."  +
					"?prep <conll:postag> \"連体化\" ." +
					"?prep <conll:form> \"の\" ." +
					"?prep <conll:head> ?py ." +
					"?e2 <conll:head> ?cop1 ." +
					"?e2 <conll:cpostag> \"名詞\" ." +
					"?cop1 <conll:feats> ?cop1feat ."+
					"FILTER regex (?cop1feat, \"特殊・ダ\") ." +
					"?cop1 <conll:head> ?cop2 ." +
					"?cop2 <conll:feats> ?cop2feat ." +
					"FILTER regex ( ?cop2feat , \"五段・ラ行アル\" ) . " +
					//"OPTIONAL { ?e1 <own:senseArg> ?e1_arg." +
					//"?e2 <own:senseArg> ?e2_arg. }" +
					"?e1 <own:senseArg> ?e1_arg."+
					"?e2 <own:senseArg> ?e2_arg."+
					"}";
	/* [entity1]の[lemma]は[entity2]COP   '[entity2] is the [lemma] of [entity1]' 
	 * イヌの祖先はオオカミである   'the ancestor of the dog is the wolf' */
	
	

	@Override
	public String getID() {
		return "SPARQLPattern_JA_1";
	}

	@Override
	public void extractLexicalEntries(Model model, LexiconWithFeatures lexicon) {
		FeatureVector vector = new FeatureVector();
		
		vector.add("freq",1.0);
		vector.add(this.getID(),1.0);
		
		List<String> sentences = this.getSentences(model);
		// TODO further templates for Japanese?
		Templates.getNounWithPrep(model, lexicon, vector, sentences, query, this.getReference(model), logger, this.getLemmatizer(), this.getDebugger());

	}

}
