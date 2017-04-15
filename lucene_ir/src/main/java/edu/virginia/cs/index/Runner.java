package edu.virginia.cs.index;

import edu.virginia.cs.IndexConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Runner {
	//please keep those constants 
    public final static String DATASET = "npl";


////This enables you to interact with the program in command line
//    public static void main(String[] args) throws IOException {
//        if (args.length == 1 && args[0].equalsIgnoreCase("--index"))
//            Indexer.index(INDEX_PATH, PREFIX, FILE);
//        else if (args.length >= 1 && args[0].equalsIgnoreCase("--search"))
//        {
//            String method = null;
//            if (args.length == 2)
//                method = args[1];
//            interactiveSearch(method);
//        }
//        else
//        {
//            System.out.println("Usage: --index to index or --search to search an index");
//            System.out.println("If using \"--search\",");
//            printUsage();
//        }
//    }
    
////This makes it easier for you to run the program in an IDE
    public static void main(String[] args) throws IOException {
    	//To crate the index
    	//NOTE: you need to create the index once, and you cannot call this function twice without removing the existing index files
    	Indexer.index(IndexConfig.INDEX_PATH, IndexConfig.PREFIX, IndexConfig.FILE);

        //Interactive searching function with your selected ranker
    	//NOTE: you have to create the index before searching!
        Searcher searcher = SearcherFactory.getInstance().getSearcher(IndexConfig.INDEX_PATH, MethodType.BooleanDotProduct);
        if(searcher == null){
            printUsage();
            System.exit(1);
        }
        interactiveSearch(searcher);
    }

    /**
     * Feel free to modify this function, if you want different display!
     *
     * @throws IOException
     */
    private static void interactiveSearch(Searcher searcher) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Type text to search, blank to quit.");
        System.out.print("> ");
        String input;
        while ((input = br.readLine()) != null && !input.equals("")) {
            SearchResult result = searcher.search(input);
            ArrayList<ResultDoc> results = result.getDocs();
            int rank = 1;
            if (results.size() == 0)
                System.out.println("No results found!");
            for (ResultDoc rdoc : results) {
                System.out.println("\n------------------------------------------------------");
                System.out.println(rank + ". " + rdoc.title());
                System.out.println("------------------------------------------------------");
                System.out.println(result.getSnippet(rdoc)
                        .replaceAll("\n", " "));
                ++rank;
            }
            System.out.print("> ");
        }
    }

    private static void printUsage()
    {
        System.out.println("To specify a ranking function, make your last argument one of the following:");
        System.out.println("\t--dp\tDirichlet Prior");
        System.out.println("\t--jm\tJelinek-Mercer");
        System.out.println("\t--ok\tOkapi BM25");
        System.out.println("\t--pl\tPivoted Length Normalization");
        System.out.println("\t--tfidf\tTFIDF Dot Product");
        System.out.println("\t--bdp\tBoolean Dot Product");
    }
}
