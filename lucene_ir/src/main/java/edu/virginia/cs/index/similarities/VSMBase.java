package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

/**
 * Created by cutehuazai on 4/10/17.
 */
public abstract class VSMBase extends SimilarityBase{
    /**
     * Returns a score for a single term in the document.
     *
     * @param stats     Provides access to corpus-level statistics
     * @param termFreq
     * @param docLength
     */
    protected float score(BasicStats stats, float termFreq, float docLength){
        double result = getTFDocument(stats, termFreq, docLength) * getIDFDocument(stats) *
                getTFQuery(stats);
        return (float)result;
    }

    protected abstract double getTFDocument(BasicStats stats, float termFreq, float docLength);
    protected abstract double getIDFDocument(BasicStats stats);
    protected abstract double getTFQuery(BasicStats stats);

}
