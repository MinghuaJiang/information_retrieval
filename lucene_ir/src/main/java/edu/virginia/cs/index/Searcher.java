package edu.virginia.cs.index;

import edu.virginia.cs.index.similarities.DirichletPrior;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Searcher
{
    private IndexSearcher indexSearcher;
    private SpecialAnalyzer analyzer;
    private static SimpleHTMLFormatter formatter;
    private static final int numFragments = 4;
    private static final String defaultField = "content";

    /**
     * Sets up the Lucene index Searcher with the specified index.
     *
     * @param indexPath
     *            The path to the desired Lucene index.
     */
    public Searcher(String indexPath)
    {
        try
        {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
            indexSearcher = new IndexSearcher(reader);
            analyzer = new SpecialAnalyzer();
            formatter = new SimpleHTMLFormatter("****", "****");
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public void setSimilarity(Similarity sim)
    {
        indexSearcher.setSimilarity(sim);
    }

    /**
     * The main search function.
     * @param searchQuery Set this object's attributes as needed.
     * @return
     */
    public SearchResult search(SearchQuery searchQuery)
    {
        BooleanQuery combinedQuery = new BooleanQuery();
        for(String field: searchQuery.fields())
        {
            QueryParser parser = new QueryParser(Version.LUCENE_46, field, analyzer);
            try
            {
                Query textQuery = parser.parse(searchQuery.queryText());
                combinedQuery.add(textQuery, BooleanClause.Occur.MUST);
            }
            catch(ParseException exception)
            {
                exception.printStackTrace();
            }
        }

        return runSearch(combinedQuery, searchQuery);
    }

    /**
     * The simplest search function. Searches the abstract field and returns a
     * the default number of results.
     *
     * @param queryText
     *            The text to search
     * @return the SearchResult
     */
    public SearchResult search(String queryText)
    {
        return search(new SearchQuery(queryText, defaultField));
    }

    public SearchResult search(String queryText, int numOfResults)
    {
        return search(new SearchQuery(queryText, defaultField).numResults(numOfResults));
    }

    public SearchResult searchAll(String queryText)
    {
        return search(queryText, Integer.MAX_VALUE);
    }


    /**
     * Performs the actual Lucene search.
     *
     * @param luceneQuery
     * @return the SearchResult
     */
    private SearchResult runSearch(Query luceneQuery, SearchQuery searchQuery)
    {
        try
        {
            String field = searchQuery.fields().get(0);
            System.out.println("\nScoring documents with " + indexSearcher.getSimilarity().toString());
            Similarity sim = indexSearcher.getSimilarity();
            TopDocs docs = null;
            ScoreDoc[] hits = null;
            // have to do this to figure out query length in the LM scorers
            if(sim instanceof DirichletPrior)
            {
                QueryParser parser = new QueryParser(Version.LUCENE_46, field, analyzer);
                Set<Term> terms = new HashSet<Term>();
                luceneQuery.extractTerms(terms);
                docs = indexSearcher.search(luceneQuery, Integer.MAX_VALUE);
                ScoreDoc[] docHits = docs.scoreDocs;
                docs.totalHits = Math.min(docHits.length, searchQuery.fromDoc() + searchQuery.numResults());
                for(ScoreDoc hit : docHits)
                {
                    double mew = ((DirichletPrior)sim).getMew();
                    Document document = indexSearcher.doc(hit.doc);
                    double documentLen = parser.parse(document.getField(field).stringValue()).toString().split("\\s+").length;
                    double alpha = mew / (mew + documentLen);
                    hit.score += (float)(terms.size() * Math.log10(alpha));
                }
                Arrays.sort(docHits, new Comparator<ScoreDoc>() {
                    public int compare(ScoreDoc t1, ScoreDoc t2) {
                        if(t1.score > t2.score){
                            return -1;
                        }else if(t1.score < t2.score){
                            return 1;
                        }else{
                            return 0;
                        }
                    }
                });
                hits = new ScoreDoc[docs.totalHits];
                for(int i = 0;i < hits.length;i++){
                    hits[i] = docHits[i];
                }
            }else {
                docs = indexSearcher.search(luceneQuery, searchQuery.fromDoc() + searchQuery.numResults());
                hits = docs.scoreDocs;
            }

            SearchResult searchResult = new SearchResult(searchQuery, docs.totalHits);
            for(ScoreDoc hit : hits)
            {
                Document doc = indexSearcher.doc(hit.doc);
                ResultDoc rdoc = new ResultDoc(hit.doc);

                String highlighted = null;
                try
                {
                    Highlighter highlighter = new Highlighter(formatter, new QueryScorer(luceneQuery));
                    rdoc.title("" + (hit.doc + 1));
                    String contents = doc.getField(field).stringValue();
                    rdoc.content(contents);
                    String[] snippets = highlighter.getBestFragments(analyzer, field, contents, numFragments);
                    highlighted = createOneSnippet(snippets);
                }
                catch(InvalidTokenOffsetsException exception)
                {
                    exception.printStackTrace();
                    highlighted = "(no snippets yet)";
                }

                searchResult.addResult(rdoc);
                searchResult.setSnippet(rdoc, highlighted);
            }

            searchResult.trimResults(searchQuery.fromDoc());
            return searchResult;
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
        catch (ParseException e){
            e.printStackTrace();
        }
        return new SearchResult(searchQuery);
    }

    /**
     * Create one string of all the extracted snippets from the highlighter
     * @param snippets
     * @return
     */
    private String createOneSnippet(String[] snippets)
    {
        String result = " ... ";
        for(String s: snippets)
            result += s + " ... ";
        return result;
    }
}
