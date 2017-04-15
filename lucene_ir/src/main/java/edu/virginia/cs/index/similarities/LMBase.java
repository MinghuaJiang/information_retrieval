package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMSimilarity;

/**
 * Created by cutehuazai on 4/10/17.
 */
public abstract class LMBase extends LMSimilarity {
    private LMSimilarity.DefaultCollectionModel model;
    private float queryLength = 0;

    public LMBase() {
        model = new LMSimilarity.DefaultCollectionModel();
    }

    /**
     * Returns a score for a single term in the document.
     *
     * @param stats     Provides access to corpus-level statistics
     * @param termFreq
     * @param docLength
     */

    @Override
    protected float score(BasicStats stats, float termFreq, float docLength) {
        double score = Math.log10(getSmoothing(stats, termFreq, docLength) /
                (getAlpha(stats, termFreq, docLength) * model.computeProbability(stats)));
        return (float) score;
    }

    protected abstract double getSmoothing(BasicStats stats, float termFreq, float docLength);

    protected abstract double getAlpha(BasicStats stats, float termFreq, float docLength);

    public void setQueryLength(float length) {
        queryLength = length;
    }

    public DefaultCollectionModel getModel() {
        return model;
    }
}