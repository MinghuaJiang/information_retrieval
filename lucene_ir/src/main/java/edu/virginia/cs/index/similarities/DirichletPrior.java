package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;

public class DirichletPrior extends LMBase {
    private double mew;

    public DirichletPrior(){
        this(2000.0);
    }
    public DirichletPrior(double mean) {
        this.mew = mean;
    }

    @Override
    protected double getSmoothing(BasicStats stats, float termFreq, float docLength) {
        return (termFreq + mew * getModel().computeProbability(stats)) / (docLength + mew);
    }

    @Override
    protected double getAlpha(BasicStats stats, float termFreq, float docLength) {
        return mew / (mew + docLength);
    }

    @Override
    public String getName() {
        return "Dirichlet Prior";
    }

    @Override
    public String toString() {
        return getName();
    }

    public double getMew() {
        return mew;
    }
}
