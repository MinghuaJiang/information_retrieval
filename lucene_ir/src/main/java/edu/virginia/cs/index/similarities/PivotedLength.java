package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;

public class PivotedLength extends VSMBase {
    private double s = 0.75;
    /**
     * Returns a score for a single term in the document.
     *
     * @param stats
     *            Provides access to corpus-level statistics
     * @param termFreq
     * @param docLength
     */

    @Override
    protected double getTFDocument(BasicStats stats, float termFreq, float docLength) {
        return (1 + Math.log(1 + Math.log(termFreq)))/(1 - s + s * docLength / stats.getAvgFieldLength());
    }

    @Override
    protected double getIDFDocument(BasicStats stats) {
        return Math.log((double)(stats.getNumberOfDocuments() + 1) / stats.getDocFreq());
    }

    @Override
    protected double getTFQuery(BasicStats stats) {
        int queryFrequency = 1;
        return queryFrequency;
    }

    @Override
    public String toString() {
        return "Pivoted Length Normalization";
    }

}
