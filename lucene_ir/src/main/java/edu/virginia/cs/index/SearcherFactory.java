package edu.virginia.cs.index;

import org.apache.lucene.search.similarities.Similarity;

/**
 * Created by cutehuazai on 4/7/17.
 */
public class SearcherFactory {
    private static final String PREFIX = "edu.virginia.cs.index.similarities.";
    private static SearcherFactory factory = new SearcherFactory();

    private SearcherFactory(){

    }

    public static SearcherFactory getInstance(){
        return factory;
    }

    public Searcher getSearcher(String path, MethodType type){
        try {
            Searcher searcher = new Searcher(path);
            Similarity similarity = (Similarity)Class.forName(PREFIX + type.toString()).newInstance();
            searcher.setSimilarity(similarity);
            return searcher;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
