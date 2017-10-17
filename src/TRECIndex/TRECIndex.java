package TRECIndex;


import TRECParser.Path;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;

public class TRECIndex {
    public static void main(String[] args) throws Exception {
        File f = new File(Path.Data_Path+"/raw_parsed/CR93E-220");
        String indexPath = Path.Project_Path + "/res/testing/";
        IndexWriter writer;
        Directory dir = FSDirectory.open(new File(indexPath));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LATEST, analyzer);

        writer = new IndexWriter(dir, iwc);

        System.out.println("Indexing file " + "CR93E-220");



        Document doc = new Document();
        Field filePathField = new Field(Path.Test_Path,f.getCanonicalPath(),Field.Store.YES,Field.Index.NOT_ANALYZED);
        doc.add(filePathField);
        writer.addDocument(doc);
    }


}


