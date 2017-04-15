package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;

public class OkapiBM25 extends VSMBase {
    private double k1;
    private double k2;
    private double b;


    public OkapiBM25(){
        this(1.2, 0.1, 0.75);
    }

    public OkapiBM25(double k1, double k2, double b){
        this.k1 = k1;
        this.k2 = k2;
        this.b = b;
    }


    /**
     * Returns a score for a single term in the document.
     *
     * @param stats     Provides access to corpus-level statistics
     * @param termFreq
     * @param docLength
     */

    @Override
    protected double getTFDocument(BasicStats stats, float termFreq, float docLength) {
        return ((k1 + 1) * termFreq / (k1 * (1 - b +
                b * docLength / stats.getAvgFieldLength()) + termFreq));
    }

    @Override
    protected double getIDFDocument(BasicStats stats) {
        return Math.log((stats.getNumberOfDocuments() - stats.getDocFreq() + 0.5)
                / (stats.getDocFreq() + 0.5));
    }

    @Override
    protected double getTFQuery(BasicStats stats) {
        int queryFrequency = 1;
        return (double) ((k2 + 1) * queryFrequency / (k2 + queryFrequency));
    }


    public double getK1() {
        return k1;
    }

    public void setK1(double k1) {
        this.k1 = k1;
    }

    public double getK2() {
        return k2;
    }

    public void setK2(double k2) {
        this.k2 = k2;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    @Override
    public String toString() {
        return "Okapi BM25";
    }

}
