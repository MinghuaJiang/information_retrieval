package index;


import analyzer.SpecialAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Created by cutehuazai on 5/12/17.
 */
public class Indexer {

    public IndexWriter getWriter(String indexPath) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        Analyzer analyzer = new SpecialAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        config.setRAMBufferSizeMB(2048.0);
        IndexWriter writer = new IndexWriter(dir, config);
        return writer;
    }

    public void index(String indexPath, Function<String, Field[]> function, Iterator<String> contents) {
        IndexWriter writer = null;
        try {
            writer = getWriter(indexPath);

            int indexed = 0;
            while (contents.hasNext()) {
                String content = contents.next();
                Field[] fields = function.apply(content);
                Document document = new Document();
                for (Field field : fields) {
                    document.add(field);
                }
                writer.addDocument(document);
                ++indexed;
                if (indexed % 100 == 0)
                    System.out.println(" -> indexed " + indexed + " docs...");
            }
            System.out.println(" -> indexed " + indexed + " total docs.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void indexDocs(String indexPath, Path inputPath) throws Exception {
        if (Files.isDirectory(inputPath)) {
            Files.walkFileTree(inputPath, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexDoc(indexPath, file);
                    } catch (IOException e) {
                        // don't index files that can't be read.
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public void indexDoc(String indexPath, Path file) throws IOException {
        IndexWriter writer = getWriter(indexPath);
        try (InputStream stream = Files.newInputStream(file)) {
            Document doc = new Document();
            doc.add(new TextField("content", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
            writer.addDocument(doc);
        }
    }

}
