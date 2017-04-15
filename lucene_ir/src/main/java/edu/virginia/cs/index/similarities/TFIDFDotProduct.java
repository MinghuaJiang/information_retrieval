package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;

public class TFIDFDotProduct extends VSMBase {
    /**
     * Returns a score for a single term in the document.
     *
     * @param stats     Provides access to corpus-level statistics
     * @param termFreq
     * @param docLength
     */
    @Override
    protected double getTFDocument(BasicStats stats, float termFreq, float docLength) {
        return (1 + Math.log10(termFreq));
    }

    @Override
    protected double getIDFDocument(BasicStats stats) {
        return Math.log10((stats.getNumberOfDocuments() + 1.0) / stats.getDocFreq());
    }

    @Override
    protected double getTFQuery(BasicStats stats) {
        return 1;
    }

    @Override
    public String toString() {
        return "TF-IDF Dot Product";
    }
}
