package TRECIndex;


import IRLit.FileLoader;
import TRECParser.Path;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;

public class TRECIndex {
    public static void main(String[] args) throws Exception {

        String indexPath = Path.Project_Path + "/res/testing/";
        Directory dir = FSDirectory.open(new File(indexPath));

        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
        IndexWriter writer = null;
        writer = new IndexWriter(dir, iwc);

        for (File f : new File(Path.Data_Path + "/raw_parsed/").listFiles()) {

            FileLoader loader = new FileLoader(f);
            System.out.println("Indexing file..." + f.getName());

            FieldType type = new FieldType();
            type.setIndexed(true);
            type.setStored(true);
            type.setStoreTermVectors(true);

            Document doc = new Document();

            Field filePathField = new Field("content", loader.loadContent().trim(), type);

            doc.add(filePathField);
            writer.addDocument(doc);
        }
        writer.close();

    }


}


