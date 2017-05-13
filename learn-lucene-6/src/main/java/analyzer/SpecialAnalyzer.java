package analyzer;


import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Created by cutehuazai on 5/12/17.
 */
public class SpecialAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        TokenStream filter = new StandardFilter(source);
        filter = new LowerCaseFilter(filter);
        filter = new LengthFilter(filter, 2, 35);
        filter = new StopFilter(filter,
                StopFilter.makeStopSet(Stopwords.STOPWORDS));
        filter = new PorterStemFilter(filter);
        return new TokenStreamComponents(source, filter);
    }
}
