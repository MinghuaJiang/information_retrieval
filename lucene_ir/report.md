# CS6501 Information Retrieval MP Report
Minghua Jiang (mj2eu) 
## Problem 1
#### Score Function
###### Boolean Dot Product
####
```Java
protected float score(BasicStats stats, float termFreq, float docLength) {
    return 1;
}
```
###### Vector Space Models 
####
```Java
protected float score(BasicStats stats, float termFreq, float docLength){
    double result = getTFDocument(stats, termFreq, docLength) * getIDFDocument(stats) * getTFQuery(stats);
    return (float)result;
}
```
###### TFIDF Dot Product 
####
```Java
    @Override
    protected double getTFDocument(BasicStats stats, float termFreq, float docLength){
        return (1 + Math.log10(termFreq));
    }

    @Override
    protected double getIDFDocument(BasicStats stats){
        return Math.log10((stats.getNumberOfDocuments() + 1.0) / stats.getDocFreq());
    }

    @Override
    protected double getTFQuery(BasicStats stats) {
        return 1;
    }
```
###### Okapi BM25
####
```Java
    @Override
    protected double getTFDocument(BasicStats stats, float termFreq, float docLength){
        return ((k1 + 1) * termFreq / (k1 * (1 - b + b * docLength / stats.getAvgFieldLength()) + termFreq));
    }

    @Override
    protected double getIDFDocument(BasicStats stats) {
        return Math.log((stats.getNumberOfDocuments() - stats.getDocFreq() + 0.5)/ (stats.getDocFreq() + 0.5));
    }

    @Override
    protected double getTFQuery(BasicStats stats) {
        int queryFrequency = 1;
        return (double) ((k2 + 1) * queryFrequency / (k2 + queryFrequency));
    }

```
###### Pivoted Length Normalization
####
```Java
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
```
###### Language Model
####
```Java
 protected float score(BasicStats stats, float termFreq, float docLength) {
        double score = Math.log10(getSmoothing(stats, termFreq, docLength) /
                (getAlpha(stats, termFreq, docLength) * model.computeProbability(stats)));
        return (float) score;
    }
```
###### Jelinek-Mercer Smoothing
####
```Java
    @Override
    protected double getSmoothing(BasicStats stats, float termFreq, float docLength){
        return (1 - lambda) * (termFreq / docLength) + lambda * getModel().computeProbability(stats);
    }

    @Override
    protected double getAlpha(BasicStats stats, float termFreq, float docLength) {
        return lambda;
    }
```

###### Dirichlet Prior Smoothing
####
```Java
    @Override
    protected double getSmoothing(BasicStats stats, float termFreq, float docLength) {
        return (termFreq + mew * getModel().computeProbability(stats)) / (docLength + mew);
    }

    @Override
    protected double getAlpha(BasicStats stats, float termFreq, float docLength) {
        return mew / (mew + docLength);
    }

```
###### Dirichlet Prior Smoothing Post Processing 
####
```Java
if(sim instanceof DirichletPrior){
    QueryParser parser = new QueryParser(Version.LUCENE_46, field, analyzer);
    Set<Term> terms = new HashSet<Term>();
    luceneQuery.extractTerms(terms);
    docs = indexSearcher.search(luceneQuery, Integer.MAX_VALUE);
    ScoreDoc[] docHits = docs.scoreDocs;
    docs.totalHits = Math.min(docHits.length, searchQuery.fromDoc() + searchQuery.numResults());
    for(ScoreDoc hit : docHits){
        double mew = ((DirichletPrior)sim).getMew();
        Document document = indexSearcher.doc(hit.doc);
        double documentLen = parser.parse(document.getField(field).stringValue()).toString().split("\\s+").length;
        double alpha = mew / (mew + documentLen);
        hit.score += (float)(terms.size() * Math.log10(alpha));
    }
    Arrays.sort(docHits, new Comparator<ScoreDoc>() {
        @Override
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
```
#### Evaluation
###### Average Precision
####
```Java
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
```
###### P@K
####
```Java
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
```
###### RR
####
```Java
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
```
###### NDCG
####
```Java
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
```
#### Evaluation Result
| Ranking Function | MAP | P@10 | MRR | NDCG@10 |
| ------ | ------ | ------ | ------ | ------ | 
| Boolean Dot Product | 0.226 | 0.288 | 0.595 | 0.350 |
| TFIDF Dot Product | 0.272 | 0.356 | 0.679 | 0.421 |
| Okapi BM25 | 0.234 | 0.308 | 0.594 | 0.368 |
| Pivoted Length Normalization | 0.168 | 0.239 | 0.434 | 0.273 |
| Jelinek-Mercer Smoothing | 0.278 | 0.342 | 0.678 | 0.420 |
| Dirichlet Prior Smoothing | 0.196 | 0.237 | 0.545 | 0.292 |

## Problem 2
#### Performance Tuning
| Ranking Function | Best MAP | Parameter |
| ------ | ------ | ------ |
| Okapi BM25 | 0.279 | k1 = 1.2, k2 = 0.1, b = 0.75 |
| Dirichlet Prior Smoothing | 0.203 | μ= 2000 |

## Problem 3
#### Document Analyzer With BM25
| Document Analyzer | MAP Before Removing | MAP After Removing | Analysis |
| ------ | ------ | ------ | ------ |
| LowerCaseFilter | 0.279 | 0.279 | We have no capital letter in data set. |
| LengthFilter | 0.279 | 0.279 | We have no word with length longer than 35.|
| StopFilter | 0.279 | 0.191 | Stop words removal reduce the noises from head words.|
| PorterStemFilter | 0.279 | 0.207 | Stemming reduce inflected or derived words to their root form.|

In a conclusion, document analyzer like stop words removal and stemming do have positive effect to the retrieval performance.  

## Problem 4
| Query | Score Function A | Score Function B | AP A | AP B |
| ------ | ------ | ------ | ------ | ------ |
| measurement of plasma temperatures in arc discharge using shock wave techniques| TFIDF Dot Product | Boolean Dot Product | 0.5 | 0.25 |
| the effect of small distortions in the surface of a cavity resonator | TFIDF Dot Product | Okapi BM25 | 0.46 | 0.20 | 
| characteristics of the single electrode discharge in the rare gases at low  pressures | Okapi BM25 | Dirichlet Prior Smoothing | 0.53 | 0.06 |

#### Analysis
###### TFIDF VS Boolean (Doc 3774 is one of the ground truth while Doc 2720 is not.)
####
###### Doc 2720 (Rank 2 in Boolean but Rank 6 in TFIDF)
####

| | measurement | plasma | temperatures | arc | discharge | shock | wave | techniques |
| ------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ |
|TF| 2| 1 | 3 | 0 | 1 | 0 | 0 | 0 |
|IDF|1.42|1.31|2.28|1.55|2.06|2.58|1.02|1.45|
- Boolean Score: 4.0
- TFIDF Score: 6.16

####
###### Doc 3774 (Rank 2 in TFIDF but Rank 4 in Boolean)
####
| | measurement | plasma | temperatures | arc | discharge | shock | wave | techniques | 
| ------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ |
|TF| 0 | 2 | 1 | 0 | 0 | 4 | 4 | 0 |
|IDF|1.42|1.31|2.28|1.55|2.06|2.58|1.02|1.45|
- Boolean Score: 4.0
- TFIDF: 8.93

The major reason for this query that TFIDF performs better than Boolean Dot Product is because Boolean Dot Product can't effectively ranking document actually, it's based on how many query words matched in the documents to rank the documents.
In the case of this query, TFIDF rank document 3774 ahead of 2720, while Boolean rank it after 2720, which was because the two documents match the same count of words in Boolean ranking and 2720 has smaller docid than 3774, and the problem is it treats each query term as equal weight without taking into account term frequency and document frequency.

###### TFIDF VS BM25 (Doc 8230 is ground truth while Doc 3616 isn't)
####
###### Doc 3616 (Rank 2 in TFIDF but Rank 1 in BM25)
####
- Doc Length 41

| | effect | small | distortions | surface | cavity | resonator | 
| ------ | ------ | ------ | ------ | ------ | ------ | ------ | 
|TF|0|1|0|3|2|2|
|DF|1370|337|163|316|219|555|
|TF Transform (TFIDF)| 0 | 1.0 | 0 | 1.48 | 1.30 | 1.30 | 
|TF Transform (BM25)| 0 | 0.77 | 0 | 1.36 | 1.15 | 1.15 | 
|IDF (TFIDF)|0.92|1.53|1.85|1.56|1.72|1.31|
|IDF (BM25) |1.99|3.49|4.23|3.56|3.93|2.97|

- TFIDF Score: 7.78
- BM25 Score: 15.46

###### Doc 8230 (Rank 1 in TFIDF but Rank 3 in BM25)
####
- DocLength 64

| | effect | small | distortions | surface | cavity | resonator | 
| ------ | ------ | ------ | ------ | ------ | ------ | ------ | 
|TF|1|1|0|5|2|1|
|DF|1370|337|163|316|219|555|
|TF Transform (TFIDF)| 1 | 1 | 0 | 1.70 | 1.30 | 1 | 
|TF Transform (BM25)| 0.59 | 0.59 | 0 | 1.43 | 0.93 | 0.59 | 
|IDF (TFIDF)|0.92|1.53|1.85|1.56|1.72|1.31|
|IDF (BM25) |1.99|3.49|4.23|3.56|3.93|2.97|

- TFIDF Score: 8.65
- BM25 Score: 13.77

Based on above two tables, we can see the major reason for this query that TFIDF performs better than BM25 is because BM25 did document length normalization explicitly based on average document length to penalize long document as well as term frequency while TFIDF just use log to penalty the term frequency, so for this query, BM25 may penalty the document length too much when the document length is much larger than the average document length. Even with higher term frequency, because of this penalty, it may rank relevant document after unrelevant document.   

###### BM25 VS Dirichlet Prior (Doc 7014 is ground truth while Doc 5300 is not)
####
###### Doc 5300 (Rank 1 in Dirichlet Prior but Rank 3 in BM25)
####
- Doclen: 28
- QueryLength: 8

| | characteristics | single | electrode | discharge | rare | gases | low | pressures| 
| ------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ |
|TF|0|0|0|0|1|1|1|1|
|DF|826|313|112|325|6|99|749|184|
|IDF (BM25)|2.55|3.56|4.61|3.53|7.47|4.74|2.66|4.11|

- BM25 Score: 17.6
- DP Score: 1.877

###### Doc 7014 (Rank 11 in Dirichlet Prior but Rank 1 in BM25)
####

- Doclen: 21
- QueryLength: 8

| | characteristics | single | electrode | discharge | rare | gases | low | pressures| 
| ------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ |
|TF|1|1|1|1|3|0|0|2|
|DF|826|313|112|325|6|99|749|184|
|IDF (BM25)|2.55|3.56|4.61|3.53|7.47|4.74|2.66|4.11|
 - BM25 Score: 22.87
- DP Score: 1.036

The major reason for this query that BM25 performs better than DP is because in DP both the document length normalization and term frequency normalization are done in the log, however BM25 did it without log, so BM25 may be more sensitive to higher term frequency.　And also when the document length is smaller than the average document length, BM25 will reward the score function, while DP will not.   

## Problem 5
####
After readinging the paper, I find the original problem of the BM25 is that the IDF part of the function may less than 0, which would cause poor performance on verbose queries. After using a new IDF component(the log weight function mentioned in the paper), the issue can be solved.
```Java
    @Override
    protected double getTFDocument(BasicStats stats, float termFreq, float docLength){
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

```
###### Performance after fix
####
| Ranking Function | MAP | P@10 | MRR | NDCG@10 | Parameter
| ------ | ------ | ------ | ------ | ------ | ------ |
| BM 25 Log Weight | 0.294 | 0.373 | 0.680 | 0.445 | s = 0.22 |























[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)


   [dill]: <https://github.com/joemccann/dillinger>
   [git-repo-url]: <https://github.com/joemccann/dillinger.git>
   [john gruber]: <http://daringfireball.net>
   [df1]: <http://daringfireball.net/projects/markdown/>
   [markdown-it]: <https://github.com/markdown-it/markdown-it>
   [Ace Editor]: <http://ace.ajax.org>
   [node.js]: <http://nodejs.org>
   [Twitter Bootstrap]: <http://twitter.github.com/bootstrap/>
   [jQuery]: <http://jquery.com>
   [@tjholowaychuk]: <http://twitter.com/tjholowaychuk>
   [express]: <http://expressjs.com>
   [AngularJS]: <http://angularjs.org>
   [Gulp]: <http://gulpjs.com>

   [PlDb]: <https://github.com/joemccann/dillinger/tree/master/plugins/dropbox/README.md>
   [PlGh]: <https://github.com/joemccann/dillinger/tree/master/plugins/github/README.md>
   [PlGd]: <https://github.com/joemccann/dillinger/tree/master/plugins/googledrive/README.md>
   [PlOd]: <https://github.com/joemccann/dillinger/tree/master/plugins/onedrive/README.md>
   [PlMe]: <https://github.com/joemccann/dillinger/tree/master/plugins/medium/README.md>
   [PlGa]: <https://github.com/RahulHP/dillinger/blob/master/plugins/googleanalytics/README.md>
