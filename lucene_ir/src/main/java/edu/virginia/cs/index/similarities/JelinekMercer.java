package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;

public class JelinekMercer extends LMBase {
    private double lambda = 0.1;
    public JelinekMercer(){
    }

    @Override
    protected double getSmoothing(BasicStats stats, float termFreq, float docLength) {
        return (1 - lambda) * (termFreq / docLength) + lambda * getModel().computeProbability(stats);
    }

    @Override
    protected double getAlpha(BasicStats stats, float termFreq, float docLength) {
        return lambda;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getName() {
        return "Jelinek-Mercer Language Model";
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }
}
