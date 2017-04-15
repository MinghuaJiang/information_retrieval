package edu.virginia.cs.eval;

import edu.virginia.cs.IndexConfig;
import edu.virginia.cs.index.*;
import edu.virginia.cs.index.similarities.DirichletPrior;
import edu.virginia.cs.index.similarities.OkapiBM25;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Evaluator {
	/**
	 * Format for judgements.txt is:
	 * 
	 * line 0: <query 1 text> line 1: <space-delimited list of relevant URLs>
	 * line 2: <query 2 text> line 3: <space-delimited list of relevant URLs>
	 * ...
	 * Please keep all these constants!
	 */
	private Logger log = Logger.getLogger(Evaluator.class);
	private static final String JUDGE_FILE = "npl-judgements.txt";
	private Searcher searcher = null;

	public void compareAP(MethodType a, MethodType b) throws Exception{

		BufferedReader br = new BufferedReader(new FileReader(JUDGE_FILE));
		String line = null, judgement = null;
		int k = 10;
		double meanAvgPrec = 0.0, p_k = 0.0, mRR = 0.0, nDCG = 0.0;
		double numQueries = 0.0;
		double diff = 0.0;
		double maxDiff = 0.0;
		String result = null;
		double max_ap1 = 0.0;
		double max_ap2 = 0.0;
		while ((line = br.readLine()) != null) {
			judgement = br.readLine();
			searcher = SearcherFactory.getInstance().getSearcher(IndexConfig.INDEX_PATH, a);
			//compute corresponding AP
			double ap1 = AvgPrec(line, judgement);
			searcher = SearcherFactory.getInstance().getSearcher(IndexConfig.INDEX_PATH, b);
			//compute corresponding AP
			double ap2 = AvgPrec(line, judgement);
			diff = ap1 - ap2;
			if(diff > maxDiff){
				result = line;
				maxDiff = diff;
				max_ap1 = ap1;
				max_ap2 = ap2;
			}
		}
		log.info("query:" + result);
		//verifyQuery(a, result.split(" "));
		//verifyQuery(b, result.split(" "));
		log.info("ap1:" + max_ap1);
		log.info("ap2:" + max_ap2);
		log.info("max diff:" + maxDiff);
	}

	public void evaluate(MethodType type) throws Exception{
		this.searcher = SearcherFactory.getInstance().getSearcher(IndexConfig.INDEX_PATH, type);
		BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(JUDGE_FILE)));
		String line = null, judgement = null;
		int k = 10;
		double meanAvgPrec = 0.0, p_k = 0.0, mRR = 0.0, nDCG = 0.0;
		double numQueries = 0.0;
		while ((line = br.readLine()) != null) {
			judgement = br.readLine();

			//compute corresponding AP
			meanAvgPrec += AvgPrec(line, judgement);
			//compute corresponding P@K
			p_k += Prec(line, judgement, k);
			//compute corresponding MRR
			mRR += RR(line, judgement);
			//compute corresponding NDCG
			nDCG += NDCG(line, judgement, k);

			++numQueries;
		}
		br.close();
		log.info("\nMethod name: " + type.name());
		log.info("\nMAP: " + meanAvgPrec / numQueries);//this is the final MAP performance of your selected ranker
		log.info("\nP@" + k + ": " + p_k / numQueries);//this is the final P@K performance of your selected ranker
		log.info("\nMRR: " + mRR / numQueries);//this is the final MRR performance of your selected ranker
		log.info("\nNDCG: " + nDCG / numQueries); //this is the final NDCG performance of your selected ranker
	}

	public void tuneMAP_DPS() throws Exception{
		this.searcher = new Searcher(IndexConfig.INDEX_PATH);
		double bestMAP = 0.0;
		double best_mean = 2000;
		for(int i = 2000; i <= 2020;i++) {
			searcher.setSimilarity(new DirichletPrior((double)i));
			BufferedReader br = new BufferedReader(new FileReader(JUDGE_FILE));
			String line = null, judgement = null;
			double meanAvgPrec = 0.0;
			double numQueries = 0.0;
			while ((line = br.readLine()) != null) {
				judgement = br.readLine();
				meanAvgPrec += AvgPrec(line, judgement);
				++numQueries;
			}
			double mAP = meanAvgPrec / numQueries;
			log.info("MAP:" + mAP);
			log.info("mean:" + i);
			if(mAP > bestMAP){
				best_mean = i;
				bestMAP = mAP;
			}
		}
		log.info("best MAP:" + bestMAP);
		log.info("best mean:" + best_mean);
	}

	public void tuneMAP_BM25() throws Exception{
		this.searcher = new Searcher(IndexConfig.INDEX_PATH);
		double bestMAP = 0.0;
		double best_mean = 0.75;
		for(double i = 0; i <= 1; i+=0.1) {
			searcher.setSimilarity(new OkapiBM25(1.2,0.0, 0.75));
			BufferedReader br = new BufferedReader(new FileReader(JUDGE_FILE));
			String line = null, judgement = null;
			double meanAvgPrec = 0.0;
			double numQueries = 0.0;
			while ((line = br.readLine()) != null) {
				judgement = br.readLine();
				meanAvgPrec += AvgPrec(line, judgement);
				++numQueries;
			}
			double mAP = meanAvgPrec / numQueries;
			log.info("MAP:" + mAP);
			log.info("k2:" + i);
			if(mAP > bestMAP){
				best_mean = i;
				bestMAP = mAP;
			}
		}
		log.info("best MAP:" + bestMAP);
		log.info("best k2:" + best_mean);
	}

	public void verifyQuery(MethodType type, String[] queryTerms){
		this.searcher = SearcherFactory.getInstance().getSearcher(IndexConfig.INDEX_PATH, type);
		System.out.println("Method:"+type);
		for(String term: queryTerms) {
			List<ResultDoc> result = searcher.searchAll(term).getDocs();
			System.out.println("Query Term:" + term);
			for (ResultDoc doc : result) {
				System.out.print(doc.title() + " ");

			}
			System.out.println();
		}
	}

	//Please implement P@K, MRR and NDCG accordingly
	public static void main(String[] args) throws Exception {
		Indexer.index(IndexConfig.INDEX_PATH, IndexConfig.PREFIX, IndexConfig.FILE);
		Evaluator evaluator = new Evaluator();
		evaluator.evaluate(MethodType.FixedBM25);
		evaluator.evaluate(MethodType.TFIDFDotProduct);
		evaluator.evaluate(MethodType.OkapiBM25);
		evaluator.evaluate(MethodType.PivotedLength);
		evaluator.evaluate(MethodType.JelinekMercer);
		evaluator.evaluate(MethodType.DirichletPrior);
		//evaluator.tuneMAP_DPS();
		//evaluator.tuneMAP_BM25();
		//evaluator.compareAP(MethodType.TFIDFDotProduct, MethodType.BooleanDotProduct);
		//evaluator.compareAP(MethodType.TFIDFDotProduct, MethodType.OkapiBM25);
		//evaluator.compareAP(MethodType.OkapiBM25, MethodType.DirichletPrior);
	}

	private double AvgPrec(String query, String docString) {
		List<ResultDoc> results = searcher.searchAll(query).getDocs();
		if (results.size() == 0)
			return 0; // no result returned

		Set<String> relDocs = new HashSet<String>(Arrays.asList(docString.split("\\s+")));
		double avgPrec = 0.0;
		int numRel = 0;
		System.out.println("\nQuery: " + query);
		for(int i = 0; i < results.size();i++){
			ResultDoc resultDoc = results.get(i);
			if (relDocs.contains(resultDoc.title())) {
				//how to accumulate average precision (avgp) when we encounter a relevant document
				numRel++;
				System.out.print("  ");
				avgPrec += (double)numRel / (i + 1);
			} else {
				//how to accumulate average precision (avgp) when we encounter an irrelevant document
				System.out.print("X ");
			}
			System.out.println(i + ". " + resultDoc.title());
		}
		
		//compute average precision here
		avgPrec = avgPrec / relDocs.size();
		System.out.println("Average Precision: " + avgPrec);
		return avgPrec;
	}
	
	//precision at K
	private double Prec(String query, String docString, int k) {
		double p_k = 0;
		List<ResultDoc> results = searcher.search(query, k).getDocs();
		if (results.size() == 0)
			return 0; // no result returned
		Set<String> relDocs = new HashSet<String>(Arrays.asList(docString.split("\\s+")));
		int numRel = 0;
		System.out.println("\nQuery: " + query);
		for(int i = 0;i < Math.min(k, results.size());i++){
			ResultDoc doc = results.get(i);
			if(relDocs.contains(doc.title())){
				numRel++;
			}
		}
		p_k = (double)numRel / k;
		//your code for computing precision at K here
		System.out.println("P@"+k+": " + p_k);
		return p_k;
	}
	
	//Reciprocal Rank
	private double RR(String query, String docString) {
		double rr = 0;
		List<ResultDoc> results = searcher.searchAll(query).getDocs();
		if (results.size() == 0)
			return 0; // no result returned
		Set<String> relDocs = new HashSet<String>(Arrays.asList(docString.split("\\s+")));
		System.out.println("\nQuery: " + query);
		for(int i = 0;i < results.size();i++){
			ResultDoc doc = results.get(i);
			if(relDocs.contains(doc.title())){
				rr = 1.0 / (i + 1);
				break;
			}
		}
		System.out.println("RR: " + rr);
		//your code for computing Reciprocal Rank here
		return rr;
	}
	
	//Normalized Discounted Cumulative Gain
	private double NDCG(String query, String docString, int k) {
		List<ResultDoc> results = searcher.search(query,k).getDocs();
		if (results.size() == 0)
			return 0; // no result returned
		Set<String> relDocs = new HashSet<String>(Arrays.asList(docString.split("\\s+")));
		System.out.println("\nQuery: " + query);
		double ndcg = 0;
		double dcg = 0;
		double idcg = 0;
		for(int i = 0; i < Math.min(k, results.size()); i++){
			ResultDoc doc = results.get(i);
			if(relDocs.contains(doc.title())){
				dcg += 1 / (Math.log(1 + i + 1) / Math.log(2));
			}
		}
		for(int i = 0; i < Math.min(k, relDocs.size());i++){
			idcg += 1 / (Math.log(1 + i + 1) / Math.log(2));
		}
		ndcg = dcg / idcg;
		System.out.println("NDCG: " + ndcg);
		//your code for computing Normalized Discounted Cumulative Gain here
		return ndcg;
	}
}