package de.citec.sc.matoll.patterns.japanese;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

import de.citec.sc.bimmel.core.FeatureVector;
import de.citec.sc.matoll.core.LexiconWithFeatures;
import de.citec.sc.matoll.patterns.SparqlPattern;
import de.citec.sc.matoll.patterns.Templates;
import de.citec.sc.matoll.patterns.english.SparqlPattern_EN_1;

public class SparqlPattern_JA_2 extends SparqlPattern {

	Logger logger = LogManager.getLogger(SparqlPattern_JA_2.class.getName());
	String query = 	"SELECT ?lemma ?e1_arg ?e2_arg WHERE {" +
					"?y <conll:cpostag>  \"名詞\" ."  +
					"?y <conll:form> ?lemma . " +
					"?e1 <conll:cpostag> \"名詞\" . "  +
					"?e1 <conll:head> ?pe1 ." +
					"?pe1 <conll:postag> \"係助詞\" ." +
					"?pe1 <conll:form> \"は\" ." +
					"?pe1 <conll:head> ?y ." +
					"?e2 <conll:cpostag> \"名詞\" ." +
					"?e2 <conll:head> ?pe2 ." +
					"?pe2 <conll:postag> \"連体化\" ." +
					"?pe2 <conll:form> \"の\" . " +
					"?pe2 <conll:head> ?y ." +
					"?e1 <own:senseArg> ?e1_arg ." +
					"?e2 <own:senseArg> ?e2_arg ." +
					"}";
	/* [entity1]は[entity2]の[lemma]    '[entity1] is the [lemma] of [entity2]'
	 * 武良 布枝は、漫画家・水木しげるの妻。  'Mura Nunoe is the wife of cartoonist Mizuki Shigeru'*/
	
	@Override
	public String getID() {
		return "SPARQLPattern_JA_2";
	}

	@Override
	public void extractLexicalEntries(Model model, LexiconWithFeatures lexicon) {
FeatureVector vector = new FeatureVector();
		
		vector.add("freq",1.0);
		vector.add(this.getID(),1.0);
		
		List<String> sentences = this.getSentences(model);
		// TODO further templates for Japanese?
		Templates.getNoun(model, lexicon, vector, sentences, query, this.getReference(model), logger, this.getLemmatizer(), this.getDebugger());

	}

}
