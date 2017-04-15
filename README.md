## lucene_information_retrieval
Implemented Retrieval Functions and Evaluations based on Lucene

| Ranking Function | MAP | P@10 | MRR | NDCG@10 |
| ------ | ------ | ------ | ------ | ------ | 
| Boolean Dot Product | 0.226 | 0.288 | 0.595 | 0.350 |
| TFIDF Dot Product | 0.272 | 0.356 | 0.679 | 0.421 |
| Okapi BM25 | 0.234 | 0.308 | 0.594 | 0.368 |
| Pivoted Length Normalization | 0.168 | 0.239 | 0.434 | 0.273 |
| Jelinek-Mercer Smoothing | 0.278 | 0.342 | 0.678 | 0.420 |
| Dirichlet Prior Smoothing | 0.196 | 0.237 | 0.545 | 0.292 |
