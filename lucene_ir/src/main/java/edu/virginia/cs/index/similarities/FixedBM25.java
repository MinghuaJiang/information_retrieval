package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;

/**
 * Created by cutehuazai on 4/14/17.
 */
public class FixedBM25 extends VSMBase{
    private double s = 0.25;

    public FixedBM25(){
        this(0.22);
    }
    public FixedBM25(double s){
        this.s = s;
    }
    @Override
    protected double getTFDocument(BasicStats stats, float termFreq, float docLength) {
        return termFreq / (termFreq + s + s * docLength / stats.getAvgFieldLength());
    }

    @Override
    protected double getIDFDocument(BasicStats stats) {
        return Math.log((stats.getNumberOfDocuments() + 1)/stats.getDocFreq());
    }


    @Override
    protected double getTFQuery(BasicStats stats) {
        return 1;
    }

    @Override
    public String toString() {
        return "Fixed BM25";
    }

    public String getName() {
        return "Fixed BM25";
    }
}
